package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.sqrt

data class SessionCaptureResult(
    val transcript: String,
    val spokenWords: List<SpokenWordTiming>,
    val grammarWords: List<SpokenWordTiming> = emptyList(),
    val energyWindows: List<AudioEnergyWindow> = emptyList(),
    val wavFilePath: String,
    val audioBytesWritten: Long
)

/**
 * Decoupled Producer-Consumer offline Vosk speech session:
 * - Dedicated audio-priority capture thread starts AudioRecord immediately (before recognizers finish
 *   compiling) with a large 3-second ring buffer, streams 16kHz mono PCM16 losslessly to WAV,
 *   computes RMS energy windows, and pushes PCM chunks onto non-blocking queues.
 * - Parallel decoder threads consume the exact same queued PCM stream for:
 *   1) Free open-vocabulary recognizer (honest "You spoke" transcript & live partials)
 *   2) Optional target grammar hint recognizer (helps match target words)
 * - Because capture never waits on Vosk decoding, WAV playback never stutters or cuts off abruptly,
 *   and zero audio frames are dropped during heavy graph decoding.
 */
class LiveSpeechSession(
    private val context: Context,
    private val model: Model,
    private val targetText: String,
    private val wavFile: File,
    private val useGrammarMode: Boolean = true,
    private val onAmplitude: ((Float) -> Unit)? = null,
    private val onPartialText: ((String) -> Unit)? = null
) {
    companion object {
        private const val TAG = "LiveSpeechSession"
        const val RATE = 16000
        private const val CHANNELS = 1
        private const val BITS_PER_SAMPLE = 16
        private const val BYTES_PER_SECOND = RATE * CHANNELS * (BITS_PER_SAMPLE / 8)
        private const val CAPTURE_CHUNK_BYTES = 1600 // 50 ms mono PCM16
        private const val SILENCE_CHUNK_BYTES = 3200 // 100 ms mono PCM16
        private const val STOP_DRAIN_MS = 300L
        private const val SILENCE_TAIL_CHUNKS = 8 // 800 ms zeroed PCM16 to flush trailing words
        private const val PARTIAL_EMIT_INTERVAL_MS = 200L
    }

    private val captureExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "AudioCaptureThread")
    }
    private val captureDispatcher = captureExecutor.asCoroutineDispatcher()

    private val freeDecodeExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "VoskFreeDecodeThread")
    }
    private val freeDecodeDispatcher = freeDecodeExecutor.asCoroutineDispatcher()

    private val grammarDecodeExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "VoskGrammarDecodeThread")
    }
    private val grammarDecodeDispatcher = grammarDecodeExecutor.asCoroutineDispatcher()

    private val freeQueue = LinkedBlockingQueue<ByteArray>()
    private val grammarQueue = LinkedBlockingQueue<ByteArray>()

    private val committedText = StringBuilder()
    private val spokenWords = mutableListOf<SpokenWordTiming>()
    private val grammarWords = mutableListOf<SpokenWordTiming>()
    private val energyWindows = mutableListOf<AudioEnergyWindow>()

    @Volatile
    private var recording = false
    @Volatile
    private var stopRequestedAtMs = 0L
    @Volatile
    private var isReleased = false

    private var captureJob: Job? = null
    private var freeDecodeJob: Job? = null
    private var grammarDecodeJob: Job? = null

    private var lastEmittedTranscript = ""
    @Volatile
    private var totalAudioBytes = 0L

    @SuppressLint("MissingPermission")
    fun start(scope: CoroutineScope) {
        if (isReleased || recording) return
        recording = true
        stopRequestedAtMs = 0L
        MicrophoneCaptureService.start(context)

        // Start parallel decoder threads immediately while capture begins without waiting for FST setup
        freeDecodeJob = scope.launch(freeDecodeDispatcher) {
            runFreeDecoder()
        }
        if (useGrammarMode) {
            grammarDecodeJob = scope.launch(grammarDecodeDispatcher) {
                runGrammarDecoder()
            }
        }

        captureJob = scope.launch(captureDispatcher) {
            try {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            } catch (_: Throwable) {}

            var audioRecord: AudioRecord? = null
            var raf: RandomAccessFile? = null
            try {
                val minBuf = AudioRecord.getMinBufferSize(
                    RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(CAPTURE_CHUNK_BYTES)
                // Generous 3-second kernel ring buffer prevents AudioRecord overflow
                val bufferSize = maxOf(minBuf * 8, BYTES_PER_SECOND * 3)

                val ar = createAudioRecordWithFallback(bufferSize)
                if (ar == null) {
                    Log.e(TAG, "AudioRecord failed to initialize on all audio sources")
                    recording = false
                    return@launch
                }
                audioRecord = ar

                // Open WAV file and write 44-byte placeholder header
                wavFile.parentFile?.mkdirs()
                if (wavFile.exists()) wavFile.delete()
                raf = RandomAccessFile(wavFile, "rw")
                raf.write(ByteArray(44))
                totalAudioBytes = 0L

                ar.startRecording()
                val buf = ByteArray(CAPTURE_CHUNK_BYTES)
                var drainedAfterStop = false

                while (!isReleased) {
                    val stopReqMs = stopRequestedAtMs
                    if (stopReqMs > 0L && drainedAfterStop && System.currentTimeMillis() - stopReqMs >= STOP_DRAIN_MS) {
                        recording = false
                        break
                    }
                    if (!recording && stopReqMs == 0L) break

                    val n = ar.read(buf, 0, buf.size)
                    if (stopReqMs > 0L) {
                        drainedAfterStop = true
                    }
                    if (n <= 0) continue

                    val startSec = totalAudioBytes.toFloat() / BYTES_PER_SECOND
                    raf.write(buf, 0, n)
                    totalAudioBytes += n
                    val endSec = totalAudioBytes.toFloat() / BYTES_PER_SECOND

                    val rms = computeRms(buf, n)
                    synchronized(energyWindows) {
                        energyWindows.add(AudioEnergyWindow(startSec = startSec, endSec = endSec, rms = rms))
                    }
                    emitAmplitude(rms)

                    val chunk = buf.copyOf(n)
                    freeQueue.offer(chunk)
                    if (useGrammarMode) {
                        grammarQueue.offer(chunk)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during audio capture loop", e)
            } finally {
                try {
                    if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        audioRecord.stop()
                    }
                } catch (_: Exception) {}
                try {
                    audioRecord?.release()
                } catch (_: Exception) {}

                try {
                    raf?.let { file ->
                        patchWavHeader(file, totalAudioBytes)
                        runCatching { file.fd.sync() }
                        file.close()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error patching/closing WAV file", e)
                }

                // Always signal EOF to decoders so they drain cleanly and never hang
                signalDecoderEof()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun createAudioRecordWithFallback(bufferSize: Int): AudioRecord? {
        val sources = listOf(
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.VOICE_RECOGNITION
        )
        for (source in sources) {
            try {
                val ar = AudioRecord(
                    source,
                    RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
                if (ar.state == AudioRecord.STATE_INITIALIZED) {
                    return ar
                }
                ar.release()
            } catch (e: Exception) {
                Log.w(TAG, "AudioRecord init failed for source=$source", e)
            }
        }
        return null
    }

    private fun signalDecoderEof() {
        val sentinel = ByteArray(0)
        freeQueue.offer(sentinel)
        if (useGrammarMode) {
            grammarQueue.offer(sentinel)
        }
    }

    private fun runFreeDecoder() {
        var freeRec: Recognizer? = null
        try {
            freeRec = synchronized(model) {
                Recognizer(model, RATE.toFloat()).apply {
                    setWords(true)
                }
            }

            var eof = false
            var lastPartialMs = 0L
            val batch = ArrayList<ByteArray>(8)

            while (!eof && !isReleased) {
                val first = freeQueue.take()
                if (first.isEmpty()) {
                    eof = true
                    break
                }
                batch.clear()
                batch.add(first)
                freeQueue.drainTo(batch)

                for (chunk in batch) {
                    if (chunk.isEmpty()) {
                        eof = true
                        break
                    }
                    if (isReleased) break
                    if (freeRec.acceptWaveForm(chunk, chunk.size)) {
                        appendFreeResult(freeRec.result)
                        emitTranscript(committedText.toString())
                        lastPartialMs = System.currentTimeMillis()
                    }
                }

                if (!eof && !isReleased && stopRequestedAtMs == 0L && freeQueue.isEmpty()) {
                    val now = System.currentTimeMillis()
                    if (now - lastPartialMs >= PARTIAL_EMIT_INTERVAL_MS) {
                        lastPartialMs = now
                        val partial = parsePartial(freeRec.partialResult)
                        val combined = combineTranscript(committedText.toString(), partial)
                        emitTranscript(combined)
                    }
                }
            }

            if (!isReleased) {
                val silenceChunk = ByteArray(SILENCE_CHUNK_BYTES)
                repeat(SILENCE_TAIL_CHUNKS) {
                    if (freeRec.acceptWaveForm(silenceChunk, silenceChunk.size)) {
                        appendFreeResult(freeRec.result)
                    }
                }
                appendFreeResult(freeRec.finalResult)
                emitTranscript(committedText.toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in free decoder thread", e)
        } finally {
            if (freeRec != null) {
                synchronized(model) {
                    try {
                        freeRec.close()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun runGrammarDecoder() {
        var grammarRec: Recognizer? = null
        try {
            val grammarJson = PronunciationScorer.buildGrammarJson(targetText) ?: return
            grammarRec = synchronized(model) {
                createGrammarRecognizerOrNull(grammarJson)?.apply {
                    setWords(true)
                }
            } ?: return

            var eof = false
            val batch = ArrayList<ByteArray>(8)
            while (!eof && !isReleased) {
                val first = grammarQueue.take()
                if (first.isEmpty()) {
                    eof = true
                    break
                }
                batch.clear()
                batch.add(first)
                grammarQueue.drainTo(batch)

                for (chunk in batch) {
                    if (chunk.isEmpty()) {
                        eof = true
                        break
                    }
                    if (isReleased) break
                    if (grammarRec.acceptWaveForm(chunk, chunk.size)) {
                        appendGrammarResult(grammarRec.result)
                    }
                }
            }

            if (!isReleased) {
                val silenceChunk = ByteArray(SILENCE_CHUNK_BYTES)
                repeat(SILENCE_TAIL_CHUNKS) {
                    if (grammarRec.acceptWaveForm(silenceChunk, silenceChunk.size)) {
                        appendGrammarResult(grammarRec.result)
                    }
                }
                appendGrammarResult(grammarRec.finalResult)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in grammar decoder thread", e)
        } finally {
            if (grammarRec != null) {
                synchronized(model) {
                    try {
                        grammarRec.close()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    suspend fun stop(): SessionCaptureResult {
        if (stopRequestedAtMs == 0L) {
            stopRequestedAtMs = System.currentTimeMillis()
        }
        captureJob?.join()
        recording = false

        freeDecodeJob?.join()
        grammarDecodeJob?.join()

        val result = SessionCaptureResult(
            transcript = committedText.toString().trim(),
            spokenWords = spokenWords.toList(),
            grammarWords = grammarWords.toList(),
            energyWindows = synchronized(energyWindows) { energyWindows.toList() },
            wavFilePath = wavFile.absolutePath,
            audioBytesWritten = totalAudioBytes
        )

        release()
        return result
    }

    suspend fun release() {
        if (isReleased) return
        isReleased = true
        recording = false
        if (stopRequestedAtMs == 0L) {
            stopRequestedAtMs = System.currentTimeMillis()
        }
        try {
            captureJob?.join()
        } catch (_: Exception) {}
        signalDecoderEof()
        try {
            freeDecodeJob?.join()
        } catch (_: Exception) {}
        try {
            grammarDecodeJob?.join()
        } catch (_: Exception) {}

        MicrophoneCaptureService.stop(context)

        withContext(captureDispatcher) {}
        try { captureDispatcher.close() } catch (_: Exception) {}
        try { freeDecodeDispatcher.close() } catch (_: Exception) {}
        try { grammarDecodeDispatcher.close() } catch (_: Exception) {}
        try { captureExecutor.shutdownNow() } catch (_: Exception) {}
        try { freeDecodeExecutor.shutdownNow() } catch (_: Exception) {}
        try { grammarDecodeExecutor.shutdownNow() } catch (_: Exception) {}
    }

    private fun appendFreeResult(json: String?) {
        val (segmentText, segmentWords) = PronunciationScorer.parseVoskWordTimings(json)
        if (segmentWords.isNotEmpty()) {
            spokenWords.addAll(segmentWords)
        }
        if (segmentText.isNotBlank()) {
            if (committedText.isNotEmpty()) committedText.append(' ')
            committedText.append(segmentText)
        }
    }

    private fun appendGrammarResult(json: String?) {
        val (_, segmentWords) = PronunciationScorer.parseVoskWordTimings(json)
        if (segmentWords.isNotEmpty()) {
            grammarWords.addAll(segmentWords)
        }
    }

    private fun createGrammarRecognizerOrNull(grammarJson: String?): Recognizer? {
        if (grammarJson.isNullOrBlank()) return null
        return try {
            Recognizer(model, RATE.toFloat(), grammarJson)
        } catch (e: Exception) {
            Log.w(TAG, "Grammar mode unavailable on this model; continuing with free recognizer only", e)
            null
        }
    }

    private fun patchWavHeader(raf: RandomAccessFile, totalAudioLen: Long) {
        val totalDataLen = totalAudioLen + 36
        val byteRate = (RATE * CHANNELS * BITS_PER_SAMPLE / 8).toLong()
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xffL).toByte()
        header[5] = ((totalDataLen shr 8) and 0xffL).toByte()
        header[6] = ((totalDataLen shr 16) and 0xffL).toByte()
        header[7] = ((totalDataLen shr 24) and 0xffL).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = CHANNELS.toByte()
        header[23] = 0
        header[24] = (RATE and 0xff).toByte()
        header[25] = ((RATE shr 8) and 0xff).toByte()
        header[26] = ((RATE shr 16) and 0xff).toByte()
        header[27] = ((RATE shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xffL).toByte()
        header[29] = ((byteRate shr 8) and 0xffL).toByte()
        header[30] = ((byteRate shr 16) and 0xffL).toByte()
        header[31] = ((byteRate shr 24) and 0xffL).toByte()
        header[32] = (CHANNELS * BITS_PER_SAMPLE / 8).toByte()
        header[33] = 0
        header[34] = BITS_PER_SAMPLE.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xffL).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xffL).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xffL).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xffL).toByte()

        raf.seek(0L)
        raf.write(header)
    }

    private fun computeRms(buf: ByteArray, bytesRead: Int): Float {
        var sumSquares = 0.0
        val sampleCount = bytesRead / 2
        if (sampleCount <= 0) return 0f
        var i = 0
        while (i + 1 < bytesRead) {
            val sample = ((buf[i + 1].toInt() shl 8) or (buf[i].toInt() and 0xFF)).toShort()
            sumSquares += sample * sample
            i += 2
        }
        return sqrt(sumSquares / sampleCount).toFloat()
    }

    private fun emitAmplitude(rms: Float) {
        val cb = onAmplitude ?: return
        val normalized = ((rms - 150f) / 3500f).coerceIn(0f, 1f)
        cb(normalized)
    }

    private fun emitTranscript(text: String) {
        val trimmed = text.trim()
        if (trimmed != lastEmittedTranscript) {
            lastEmittedTranscript = trimmed
            onPartialText?.invoke(trimmed)
        }
    }

    private fun combineTranscript(committed: String, partial: String): String {
        return when {
            committed.isBlank() -> partial.trim()
            partial.isBlank() -> committed.trim()
            else -> "$committed $partial".trim()
        }
    }

    private fun parsePartial(json: String?): String {
        if (json.isNullOrBlank()) return ""
        return try {
            val raw = JSONObject(json).optString("partial", "").trim()
            PronunciationScorer.formatTranscriptWithUnk(raw)
        } catch (_: Exception) {
            ""
        }
    }
}