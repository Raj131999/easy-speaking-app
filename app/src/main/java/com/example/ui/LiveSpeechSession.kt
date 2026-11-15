package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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
 * Single-capture offline Vosk speech session:
 * - Starts MicrophoneCaptureService on Android 14+ (API 34+)
 * - Runs two recognizers on the same PCM16 stream:
 *   1) Free open-vocabulary recognizer (honest "You spoke" transcript & live partials)
 *   2) Optional target grammar hint recognizer (helps rescue target words dropped by small acoustic models)
 * - Streams 16kHz mono PCM16 directly to a WAV file (44-byte placeholder header patched on stop)
 * - Drains tail hardware buffer on Stop and feeds ~700ms silence before finalResult so ending words commit
 * - Records per-chunk RMS energy windows alongside audio timestamps
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
        private const val CHUNK_BYTES = RATE / 10 * 2 // 100 ms = 3200 bytes
        private const val STOP_DRAIN_MS = 200L
        private const val SILENCE_TAIL_CHUNKS = 7 // 700 ms of zeroed PCM16 before finalResult
    }

    private val voskExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "VoskCaptureThread")
    }
    private val voskDispatcher = voskExecutor.asCoroutineDispatcher()

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

    private var freeRecognizer: Recognizer? = null
    private var grammarRecognizer: Recognizer? = null
    private var recordJob: Job? = null
    private var lastEmittedTranscript = ""
    private var totalAudioBytes = 0L

    @SuppressLint("MissingPermission")
    fun start(scope: CoroutineScope) {
        if (isReleased || recording) return
        recording = true
        stopRequestedAtMs = 0L
        MicrophoneCaptureService.start(context)

        recordJob = scope.launch(voskDispatcher) {
            var audioRecord: AudioRecord? = null
            var raf: RandomAccessFile? = null
            try {
                // Primary free recognizer (honest transcript & live partials)
                val freeRec = Recognizer(model, RATE.toFloat()).apply {
                    setWords(true)
                }
                freeRecognizer = freeRec

                // Secondary grammar recognizer (hint for target words)
                val grammarJson = if (useGrammarMode) PronunciationScorer.buildGrammarJson(targetText) else null
                val grammarRec = createGrammarRecognizerOrNull(grammarJson)?.apply {
                    setWords(true)
                }
                grammarRecognizer = grammarRec

                // Create AudioRecord (MIC, 16 kHz, mono, PCM16) with buffer >= getMinBufferSize * 2
                val minBuf = AudioRecord.getMinBufferSize(
                    RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(CHUNK_BYTES)
                val bufferSize = maxOf(minBuf * 2, CHUNK_BYTES * 4)

                val ar = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
                if (ar.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord failed to initialize (state=${ar.state})")
                    ar.release()
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

                // Capture loop on dedicated background thread
                ar.startRecording()
                val buf = ByteArray(CHUNK_BYTES)
                while (!isReleased) {
                    val stopReqMs = stopRequestedAtMs
                    if (stopReqMs > 0L && System.currentTimeMillis() - stopReqMs >= STOP_DRAIN_MS) {
                        recording = false
                        break
                    }
                    if (!recording && stopReqMs == 0L) break

                    val n = ar.read(buf, 0, buf.size)
                    if (n <= 0) continue

                    val startSec = totalAudioBytes.toFloat() / BYTES_PER_SECOND
                    raf.write(buf, 0, n)
                    totalAudioBytes += n
                    val endSec = totalAudioBytes.toFloat() / BYTES_PER_SECOND

                    val rms = computeRms(buf, n)
                    energyWindows.add(AudioEnergyWindow(startSec = startSec, endSec = endSec, rms = rms))
                    emitAmplitude(rms)

                    // Feed identical PCM16 chunk to both recognizers
                    if (freeRec.acceptWaveForm(buf, n)) {
                        appendFreeResult(freeRec.result)
                        emitTranscript(committedText.toString())
                    } else {
                        val partial = parsePartial(freeRec.partialResult)
                        val combined = combineTranscript(committedText.toString(), partial)
                        emitTranscript(combined)
                    }

                    if (grammarRec != null && grammarRec.acceptWaveForm(buf, n)) {
                        appendGrammarResult(grammarRec.result)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during Vosk single-capture loop", e)
            } finally {
                // Stop and release AudioRecord, patch WAV header, close file
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
                        file.close()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error patching/closing WAV file", e)
                }
            }
        }
    }

    suspend fun stop(): SessionCaptureResult {
        if (stopRequestedAtMs == 0L) {
            stopRequestedAtMs = System.currentTimeMillis()
        }
        recordJob?.join()
        recording = false

        val result = withContext(voskDispatcher) {
            try {
                // Feed ~700ms of silence into Vosk (without writing to WAV) so trailing words flush out cleanly
                val silenceChunk = ByteArray(CHUNK_BYTES)
                val freeRec = freeRecognizer
                val grammarRec = grammarRecognizer
                if (freeRec != null || grammarRec != null) {
                    repeat(SILENCE_TAIL_CHUNKS) {
                        if (freeRec != null && freeRec.acceptWaveForm(silenceChunk, silenceChunk.size)) {
                            appendFreeResult(freeRec.result)
                        }
                        if (grammarRec != null && grammarRec.acceptWaveForm(silenceChunk, silenceChunk.size)) {
                            appendGrammarResult(grammarRec.result)
                        }
                    }
                }

                appendFreeResult(freeRec?.finalResult)
                appendGrammarResult(grammarRec?.finalResult)
            } catch (e: Exception) {
                Log.e(TAG, "Error flushing/calling finalResult", e)
            }

            SessionCaptureResult(
                transcript = committedText.toString().trim(),
                spokenWords = spokenWords.toList(),
                grammarWords = grammarWords.toList(),
                energyWindows = energyWindows.toList(),
                wavFilePath = wavFile.absolutePath,
                audioBytesWritten = totalAudioBytes
            )
        }

        release()
        return result
    }

    suspend fun release() {
        if (isReleased) return
        isReleased = true
        recording = false
        try {
            recordJob?.join()
        } catch (_: Exception) {}

        withContext(voskDispatcher) {
            try {
                freeRecognizer?.close()
            } catch (_: Exception) {}
            freeRecognizer = null

            try {
                grammarRecognizer?.close()
            } catch (_: Exception) {}
            grammarRecognizer = null
        }

        MicrophoneCaptureService.stop(context)

        try {
            voskDispatcher.close()
        } catch (_: Exception) {}
        try {
            voskExecutor.shutdownNow()
        } catch (_: Exception) {}
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