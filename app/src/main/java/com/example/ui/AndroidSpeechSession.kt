package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.Process
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.sqrt

/**
 * Offline speech practice session powered by Android's built-in SpeechRecognizer
 * (on-device recognizer when available, EXTRA_PREFER_OFFLINE = true) paired with AudioRecord
 * PCM capture for lossless WAV playback and real-time RMS visualizer feedback:
 *
 * - On Android 13+ (API 33+): streams 16 kHz mono PCM16 directly from AudioRecord into
 *   SpeechRecognizer via a ParcelFileDescriptor pipe (EXTRA_AUDIO_SOURCE), and for longer
 *   paragraphs or incomplete streams splits the captured PCM buffer into ~15-second chunks
 *   ([transcribePcmChunks]) on pause boundaries before alignment.
 * - Below Android 13 (< API 33): lets SpeechRecognizer capture directly (avoiding concurrent
 *   AudioRecord mic contention) with offline preference and RMS callbacks.
 */
class AndroidSpeechSession(
    private val context: Context,
    private val targetText: String,
    private val itemType: String,
    private val accentCode: String,
    private val wavFile: File,
    private val onAmplitude: ((Float) -> Unit)? = null,
    private val onPartialText: ((String) -> Unit)? = null,
    private val onProcessingProgress: ((Int, Int) -> Unit)? = null
) : SpeechPracticeSession {

    companion object {
        private const val TAG = "AndroidSpeechSession"
        const val RATE = 16000
        private const val CHANNELS = 1
        private const val BITS_PER_SAMPLE = 16
        private const val BYTES_PER_SECOND = RATE * CHANNELS * (BITS_PER_SAMPLE / 8)
        private const val CAPTURE_CHUNK_BYTES = 1600 // 50 ms PCM16
        private const val STOP_DRAIN_MS = 250L
        private const val PIPE_TAIL_SILENCE_BYTES = 16000 // 500 ms silence
        private const val PIPE_LEAD_SILENCE_BYTES = 6400  // 200 ms silence
        private const val MAX_PCM_BUFFER_BYTES = 5 * 60 * BYTES_PER_SECOND // 5 min safety cap
        private const val TARGET_CHUNK_BYTES = 15 * BYTES_PER_SECOND // 15 s
        private const val MIN_CHUNK_SEEK_BYTES = 11 * BYTES_PER_SECOND
        private const val MAX_CHUNK_SEEK_BYTES = 16 * BYTES_PER_SECOND
        private const val PARAGRAPH_MIN_CHUNK_BYTES = 8 * BYTES_PER_SECOND
        private const val PARAGRAPH_TARGET_CHUNK_BYTES = 10 * BYTES_PER_SECOND
        private const val PARAGRAPH_MAX_CHUNK_BYTES = 12 * BYTES_PER_SECOND
        private const val LONG_UTTERANCE_SECONDS = 13.5f

        private const val ERROR_LANGUAGE_UNAVAILABLE_CODE = 12
        private const val ERROR_LANGUAGE_NOT_SUPPORTED_CODE = 13

        fun languageTagForAccent(accentCode: String): String = when (accentCode) {
            "IN" -> "en-IN"
            "UK" -> "en-GB"
            "AU" -> "en-AU"
            else -> "en-US"
        }
    }

    private val captureExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "AndroidSpeechCaptureThread")
    }
    private val captureDispatcher = captureExecutor.asCoroutineDispatcher()

    private val energyWindows = mutableListOf<AudioEnergyWindow>()
    private val pcmOutput = ByteArrayOutputStream()

    @Volatile
    private var recording = false
    @Volatile
    private var stopRequestedAtMs = 0L
    @Volatile
    private var isReleased = false
    @Volatile
    private var totalAudioBytes = 0L

    private var captureJob: Job? = null
    private var recognizerStartJob: Job? = null

    private var activeRecognizer: SpeechRecognizer? = null
    private var pipeReadFd: ParcelFileDescriptor? = null
    private var pipeWriteFd: ParcelFileDescriptor? = null

    @Volatile
    private var liveBestPartial = ""
    @Volatile
    private var liveFinalTranscript: String? = null
    @Volatile
    private var liveFinalConfidence = 0.92f
    @Volatile
    private var liveFinishedAtBytes = -1L
    @Volatile
    private var liveErrorCode: Int? = null
    private var liveResultDeferred: CompletableDeferred<String>? = null
    private var legacyStartTimeMs = 0L
    @Volatile
    private var lastEmittedTranscript = ""

    // Paragraph-only non-blocking live preview & cancellation state
    @Volatile
    private var isCancelled = false
    @Volatile
    private var paragraphLiveStreamEnded = false
    private var paragraphPipeChannel: Channel<ByteArray>? = null
    private var paragraphPipeWriterJob: Job? = null
    @Volatile
    private var activeChunkDeferred: CompletableDeferred<Triple<String, Float, Int?>>? = null
    @Volatile
    private var activeChunkReadFd: ParcelFileDescriptor? = null
    @Volatile
    private var activeChunkWriteFd: ParcelFileDescriptor? = null
    @Volatile
    private var activeChunkRecognizer: SpeechRecognizer? = null

    fun wasCancelled(): Boolean = isCancelled

    fun cancelProcessing() {
        isCancelled = true
        recording = false
        if (stopRequestedAtMs == 0L) {
            stopRequestedAtMs = System.currentTimeMillis()
        }
        paragraphPipeChannel?.close()
        paragraphPipeWriterJob?.cancel()
        liveResultDeferred?.let { if (!it.isCompleted) it.complete("") }
        activeChunkDeferred?.let { if (!it.isCompleted) it.complete(Triple("", 0.9f, null)) }
        runCatching { activeChunkReadFd?.close() }
        runCatching { activeChunkWriteFd?.close() }
        runCatching { pipeReadFd?.close() }
        runCatching { pipeWriteFd?.close() }
    }

    @SuppressLint("MissingPermission")
    override fun start(scope: CoroutineScope) {
        if (isReleased || recording) return
        recording = true
        isCancelled = false
        paragraphLiveStreamEnded = false
        stopRequestedAtMs = 0L
        liveBestPartial = ""
        liveFinalTranscript = null
        liveFinalConfidence = 0.92f
        liveFinishedAtBytes = -1L
        liveErrorCode = null
        lastEmittedTranscript = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (itemType == "paragraph") {
                startApi33ParagraphSession(scope)
            } else {
                startApi33StreamingSession(scope)
            }
        } else {
            startLegacyDirectSession(scope)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    private fun startApi33ParagraphSession(scope: CoroutineScope) {
        MicrophoneCaptureService.start(context)

        val pipe = try {
            ParcelFileDescriptor.createPipe()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to create paragraph audio pipe", e)
            null
        }
        val readFd = pipe?.get(0)
        val writeFd = pipe?.get(1)
        pipeReadFd = readFd
        pipeWriteFd = writeFd

        val readFdClosed = AtomicBoolean(false)
        fun closeLocalReadFdOnce() {
            if (readFd != null && readFdClosed.compareAndSet(false, true)) {
                runCatching { readFd.close() }
                if (pipeReadFd === readFd) pipeReadFd = null
            }
        }

        val deferred = CompletableDeferred<String>()
        liveResultDeferred = deferred

        val channel = Channel<ByteArray>(capacity = 32, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        paragraphPipeChannel = channel

        if (readFd != null && writeFd != null) {
            recognizerStartJob = scope.launch(Dispatchers.Main.immediate) {
                if (isReleased || isCancelled) {
                    closeLocalReadFdOnce()
                    if (!deferred.isCompleted) deferred.complete("")
                    return@launch
                }
                try {
                    destroyActiveRecognizerOnMain()
                    val rec = createBestOfflineRecognizer()
                    activeRecognizer = rec
                    rec.setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            // Remote service now owns its duplicated copy of readFd
                            closeLocalReadFdOnce()
                        }
                        override fun onBeginningOfSpeech() {
                            closeLocalReadFdOnce()
                        }
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {}
                        override fun onEvent(eventType: Int, params: Bundle?) {}

                        override fun onPartialResults(partialResults: Bundle?) {
                            val text = extractRecognitionText(partialResults)
                            if (text.isNotBlank() && (text.length >= liveBestPartial.length || liveBestPartial.isBlank())) {
                                liveBestPartial = text
                                emitTranscript(text)
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            closeLocalReadFdOnce()
                            paragraphLiveStreamEnded = true
                            channel.close()
                            val text = extractRecognitionText(results)
                            val conf = extractConfidence(results)
                            liveFinalTranscript = text
                            liveFinalConfidence = conf
                            liveFinishedAtBytes = totalAudioBytes
                            if (text.isNotBlank()) emitTranscript(text)
                            if (!deferred.isCompleted) deferred.complete(text)
                        }

                        override fun onError(error: Int) {
                            Log.w(TAG, "Paragraph live SpeechRecognizer onError=$error")
                            closeLocalReadFdOnce()
                            paragraphLiveStreamEnded = true
                            channel.close()
                            liveErrorCode = error
                            liveFinishedAtBytes = totalAudioBytes
                            if (!deferred.isCompleted) deferred.complete("")
                        }
                    })
                    val intent = buildOfflineIntent(
                        languageTag = languageTagForAccent(accentCode),
                        readFd = readFd
                    )
                    rec.startListening(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed starting paragraph streaming SpeechRecognizer", e)
                    closeLocalReadFdOnce()
                    paragraphLiveStreamEnded = true
                    channel.close()
                    liveErrorCode = SpeechRecognizer.ERROR_CLIENT
                    if (!deferred.isCompleted) deferred.complete("")
                }
            }

            // Dedicated non-blocking IO writer so AudioRecord capture NEVER blocks on the pipe
            paragraphPipeWriterJob = scope.launch(Dispatchers.IO) {
                try {
                    ParcelFileDescriptor.AutoCloseOutputStream(writeFd).use { out ->
                        for (chunk in channel) {
                            if (paragraphLiveStreamEnded || isCancelled || isReleased) break
                            out.write(chunk)
                        }
                        if (!paragraphLiveStreamEnded && !isCancelled && !isReleased) {
                            writeSilenceInChunksCancellable(out, PIPE_TAIL_SILENCE_BYTES) {
                                paragraphLiveStreamEnded || isCancelled || isReleased
                            }
                            out.flush()
                        }
                    }
                } catch (_: IOException) {
                    // Expected when recognizer stops reading early
                } catch (_: Exception) {
                } finally {
                    if (pipeWriteFd === writeFd) pipeWriteFd = null
                }
            }
        } else {
            channel.close()
            deferred.complete("")
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
                val bufferSize = maxOf(minBuf * 8, BYTES_PER_SECOND * 3)

                val ar = createAudioRecordWithFallback(bufferSize)
                if (ar == null) {
                    Log.e(TAG, "AudioRecord failed to initialize for paragraph")
                    recording = false
                    return@launch
                }
                audioRecord = ar

                wavFile.parentFile?.mkdirs()
                if (wavFile.exists()) wavFile.delete()
                raf = RandomAccessFile(wavFile, "rw")
                raf.write(ByteArray(44))
                totalAudioBytes = 0L
                synchronized(pcmOutput) { pcmOutput.reset() }

                ar.startRecording()
                val buf = ByteArray(CAPTURE_CHUNK_BYTES)
                var drainedAfterStop = false

                while (!isReleased && !isCancelled) {
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
                    synchronized(pcmOutput) {
                        if (pcmOutput.size() + n <= MAX_PCM_BUFFER_BYTES) {
                            pcmOutput.write(buf, 0, n)
                        }
                    }
                    totalAudioBytes += n
                    val endSec = totalAudioBytes.toFloat() / BYTES_PER_SECOND

                    val rms = computeRms(buf, n)
                    synchronized(energyWindows) {
                        energyWindows.add(AudioEnergyWindow(startSec = startSec, endSec = endSec, rms = rms))
                    }
                    emitAmplitude(rms)

                    // Non-blocking enqueue to live preview pipe (never stalls AudioRecord)
                    if (!paragraphLiveStreamEnded) {
                        channel.trySend(buf.copyOf(n))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during paragraph AudioRecord capture", e)
            } finally {
                channel.close()
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
                    Log.e(TAG, "Error patching/closing paragraph WAV file", e)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    private fun startApi33StreamingSession(scope: CoroutineScope) {
        MicrophoneCaptureService.start(context)

        val pipe = try {
            ParcelFileDescriptor.createPipe()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to create audio pipe", e)
            null
        }
        val readFd = pipe?.get(0)
        val writeFd = pipe?.get(1)
        pipeReadFd = readFd
        pipeWriteFd = writeFd

        val deferred = CompletableDeferred<String>()
        liveResultDeferred = deferred

        if (readFd != null) {
            recognizerStartJob = scope.launch(Dispatchers.Main.immediate) {
                if (isReleased) return@launch
                try {
                    destroyActiveRecognizerOnMain()
                    val rec = createBestOfflineRecognizer()
                    activeRecognizer = rec
                    rec.setRecognitionListener(
                        createStreamingListener(
                            deferred = deferred,
                            onPartial = { text ->
                                if (text.length >= liveBestPartial.length || liveBestPartial.isBlank()) {
                                    liveBestPartial = text
                                    emitTranscript(text)
                                }
                            },
                            onFinal = { text, conf ->
                                liveFinalTranscript = text
                                liveFinalConfidence = conf
                                liveFinishedAtBytes = totalAudioBytes
                                if (text.isNotBlank()) emitTranscript(text)
                            },
                            onFailure = { error ->
                                liveErrorCode = error
                                liveFinishedAtBytes = totalAudioBytes
                            }
                        )
                    )
                    val intent = buildOfflineIntent(
                        languageTag = languageTagForAccent(accentCode),
                        readFd = readFd
                    )
                    rec.startListening(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed starting streaming SpeechRecognizer", e)
                    liveErrorCode = SpeechRecognizer.ERROR_CLIENT
                    if (!deferred.isCompleted) deferred.complete("")
                }
            }
        } else {
            deferred.complete("")
        }

        captureJob = scope.launch(captureDispatcher) {
            try {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            } catch (_: Throwable) {}

            var audioRecord: AudioRecord? = null
            var raf: RandomAccessFile? = null
            var pipeOut: ParcelFileDescriptor.AutoCloseOutputStream? =
                writeFd?.let { ParcelFileDescriptor.AutoCloseOutputStream(it) }
            var pipeBroken = (pipeOut == null)

            try {
                val minBuf = AudioRecord.getMinBufferSize(
                    RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(CAPTURE_CHUNK_BYTES)
                val bufferSize = maxOf(minBuf * 8, BYTES_PER_SECOND * 3)

                val ar = createAudioRecordWithFallback(bufferSize)
                if (ar == null) {
                    Log.e(TAG, "AudioRecord failed to initialize")
                    recording = false
                    return@launch
                }
                audioRecord = ar

                wavFile.parentFile?.mkdirs()
                if (wavFile.exists()) wavFile.delete()
                raf = RandomAccessFile(wavFile, "rw")
                raf.write(ByteArray(44))
                totalAudioBytes = 0L
                synchronized(pcmOutput) { pcmOutput.reset() }

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
                    synchronized(pcmOutput) {
                        if (pcmOutput.size() + n <= MAX_PCM_BUFFER_BYTES) {
                            pcmOutput.write(buf, 0, n)
                        }
                    }
                    totalAudioBytes += n
                    val endSec = totalAudioBytes.toFloat() / BYTES_PER_SECOND

                    val rms = computeRms(buf, n)
                    synchronized(energyWindows) {
                        energyWindows.add(AudioEnergyWindow(startSec = startSec, endSec = endSec, rms = rms))
                    }
                    emitAmplitude(rms)

                    if (!pipeBroken) {
                        try {
                            pipeOut?.write(buf, 0, n)
                        } catch (ioe: IOException) {
                            Log.w(TAG, "SpeechRecognizer pipe closed early; continuing WAV capture", ioe)
                            pipeBroken = true
                            runCatching { pipeOut?.close() }
                            pipeOut = null
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during AudioRecord capture", e)
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

                // Append trailing silence and close write end so SpeechRecognizer commits last words
                if (!pipeBroken && pipeOut != null) {
                    try {
                        writeSilenceInChunks(pipeOut!!, PIPE_TAIL_SILENCE_BYTES)
                        pipeOut?.flush()
                    } catch (_: IOException) {
                    } finally {
                        runCatching { pipeOut?.close() }
                    }
                } else {
                    runCatching { pipeOut?.close() }
                }
                pipeWriteFd = null
            }
        }
    }

    private fun startLegacyDirectSession(scope: CoroutineScope) {
        val deferred = CompletableDeferred<String>()
        liveResultDeferred = deferred
        legacyStartTimeMs = System.currentTimeMillis()
        totalAudioBytes = 0L

        recognizerStartJob = scope.launch(Dispatchers.Main.immediate) {
            if (isReleased) return@launch
            try {
                destroyActiveRecognizerOnMain()
                val rec = createBestOfflineRecognizer()
                activeRecognizer = rec
                rec.setRecognitionListener(
                    createStreamingListener(
                        deferred = deferred,
                        onPartial = { text ->
                            if (text.length >= liveBestPartial.length || liveBestPartial.isBlank()) {
                                liveBestPartial = text
                                emitTranscript(text)
                            }
                        },
                        onFinal = { text, conf ->
                            liveFinalTranscript = text
                            liveFinalConfidence = conf
                            if (text.isNotBlank()) emitTranscript(text)
                        },
                        onFailure = { error ->
                            liveErrorCode = error
                        },
                        onRmsDb = { rmsDb ->
                            val norm = ((rmsDb + 2f) / 12f).coerceIn(0f, 1f)
                            onAmplitude?.invoke(norm)
                            val nowSec = ((System.currentTimeMillis() - legacyStartTimeMs).coerceAtLeast(0L)) / 1000f
                            val syntheticRms = 120f + norm * 2000f
                            synchronized(energyWindows) {
                                val prevEnd = energyWindows.lastOrNull()?.endSec ?: 0f
                                val start = prevEnd.coerceAtMost(nowSec)
                                val end = maxOf(start + 0.05f, nowSec)
                                energyWindows.add(AudioEnergyWindow(start, end, syntheticRms))
                            }
                        }
                    )
                )
                val intent = buildOfflineIntent(languageTag = languageTagForAccent(accentCode))
                rec.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed starting legacy SpeechRecognizer", e)
                liveErrorCode = SpeechRecognizer.ERROR_CLIENT
                if (!deferred.isCompleted) deferred.complete("")
            }
        }
    }

    override suspend fun stop(): SessionCaptureResult {
        if (stopRequestedAtMs == 0L) {
            stopRequestedAtMs = System.currentTimeMillis()
        }

        val expectedWordCount = PronunciationScorer.extractTargetWords(targetText).size

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (itemType == "paragraph") {
                return stopApi33ParagraphSession(expectedWordCount)
            }
            recognizerStartJob?.join()
            captureJob?.join()
            recording = false

            withTimeoutOrNull(2500L) {
                liveResultDeferred?.await()
            }

            withContext(Dispatchers.Main.immediate) {
                destroyActiveRecognizerOnMain()
                closePipeFds()
            }

            val pcmBytes = synchronized(pcmOutput) { pcmOutput.toByteArray() }
            val windowsCopy = synchronized(energyWindows) { energyWindows.toList() }
            val durationSec = pcmBytes.size.toFloat() / BYTES_PER_SECOND
            val speechDetected = hasSpeechAudio(windowsCopy)

            val rawLive = liveFinalTranscript?.takeIf { it.isNotBlank() } ?: liveBestPartial
            val liveEndedEarlyWithSpeech = liveFinishedAtBytes in 1 until (totalAudioBytes - BYTES_PER_SECOND) &&
                hasSpeechAfterByteOffset(windowsCopy, liveFinishedAtBytes)

            val shouldRunChunked = when {
                pcmBytes.size < 3200 -> false
                itemType == "paragraph" -> true
                durationSec > LONG_UTTERANCE_SECONDS -> true
                rawLive.isBlank() && speechDetected -> true
                liveEndedEarlyWithSpeech -> true
                else -> false
            }

            var finalTranscript = rawLive.trim()
            var finalConfidence = liveFinalConfidence
            var notice: String? = null

            if (shouldRunChunked) {
                val (chunkedText, chunkConf, chunkError) = transcribePcmChunks(pcmBytes)
                if (chunkedText.isNotBlank() &&
                    (itemType == "paragraph" ||
                        chunkedText.split(Regex("\\s+")).size >= finalTranscript.split(Regex("\\s+")).filter { it.isNotBlank() }.size)
                ) {
                    finalTranscript = chunkedText
                    finalConfidence = chunkConf
                    emitTranscript(finalTranscript)
                } else if (finalTranscript.isBlank()) {
                    notice = mapErrorToUserNotice(chunkError ?: liveErrorCode)
                }
            } else if (finalTranscript.isBlank() && liveErrorCode != null) {
                notice = mapErrorToUserNotice(liveErrorCode)
            }

            val spokenWords = PronunciationScorer.buildSpokenWordsFromTranscript(
                transcript = finalTranscript,
                energyWindows = windowsCopy,
                confidence = finalConfidence,
                expectedWordCount = expectedWordCount
            )

            release()

            return SessionCaptureResult(
                transcript = finalTranscript,
                spokenWords = spokenWords,
                grammarWords = emptyList(),
                energyWindows = windowsCopy,
                wavFilePath = wavFile.absolutePath,
                audioBytesWritten = totalAudioBytes,
                errorNotice = notice
            )
        } else {
            recognizerStartJob?.join()
            recording = false
            withContext(Dispatchers.Main.immediate) {
                try {
                    activeRecognizer?.stopListening()
                } catch (_: Exception) {}
            }

            withTimeoutOrNull(3500L) {
                liveResultDeferred?.await()
            }

            withContext(Dispatchers.Main.immediate) {
                destroyActiveRecognizerOnMain()
            }

            val finalTranscript = (liveFinalTranscript?.takeIf { it.isNotBlank() } ?: liveBestPartial).trim()
            val windowsCopy = synchronized(energyWindows) { energyWindows.toList() }
            val notice = if (finalTranscript.isBlank()) mapErrorToUserNotice(liveErrorCode) else null

            val spokenWords = PronunciationScorer.buildSpokenWordsFromTranscript(
                transcript = finalTranscript,
                energyWindows = windowsCopy,
                confidence = liveFinalConfidence,
                expectedWordCount = expectedWordCount
            )

            release()

            return SessionCaptureResult(
                transcript = finalTranscript,
                spokenWords = spokenWords,
                grammarWords = emptyList(),
                energyWindows = windowsCopy,
                wavFilePath = "",
                audioBytesWritten = 0L,
                errorNotice = notice
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun stopApi33ParagraphSession(expectedWordCount: Int): SessionCaptureResult {
        withTimeoutOrNull(2000L) {
            recognizerStartJob?.join()
        }
        withTimeoutOrNull(2500L) {
            captureJob?.join()
        }
        recording = false
        paragraphPipeChannel?.close()

        if (!paragraphLiveStreamEnded && !isCancelled) {
            withTimeoutOrNull(1500L) {
                liveResultDeferred?.await()
            }
        }

        paragraphLiveStreamEnded = true
        withContext(NonCancellable + Dispatchers.Main.immediate) {
            destroyActiveRecognizerOnMain()
            closePipeFds()
        }
        paragraphPipeWriterJob?.cancel()

        if (isCancelled) {
            release()
            return SessionCaptureResult(
                transcript = "",
                spokenWords = emptyList(),
                grammarWords = emptyList(),
                energyWindows = emptyList(),
                wavFilePath = "",
                audioBytesWritten = 0L,
                errorNotice = null
            )
        }

        val pcmBytes = synchronized(pcmOutput) { pcmOutput.toByteArray() }
        val windowsCopy = synchronized(energyWindows) { energyWindows.toList() }
        val durationSec = pcmBytes.size.toFloat() / BYTES_PER_SECOND
        val speechDetected = hasSpeechAudio(windowsCopy)

        val rawLive = (liveFinalTranscript?.takeIf { it.isNotBlank() } ?: liveBestPartial).trim()
        val liveEndedEarlyWithSpeech = liveFinishedAtBytes in 1 until (totalAudioBytes - BYTES_PER_SECOND) &&
            hasSpeechAfterByteOffset(windowsCopy, liveFinishedAtBytes)
        val liveCoveredShortAttempt = rawLive.isNotBlank() &&
            !liveFinalTranscript.isNullOrBlank() &&
            durationSec <= LONG_UTTERANCE_SECONDS &&
            !liveEndedEarlyWithSpeech

        val shouldRunChunked = pcmBytes.size >= 3200 && speechDetected && !liveCoveredShortAttempt

        var finalTranscript = rawLive
        var finalConfidence = liveFinalConfidence
        var notice: String? = null

        if (shouldRunChunked && !isCancelled) {
            // Brief cooldown so Android's SpeechRecognitionService releases the live session cleanly
            delay(150L)
            val (chunkedText, chunkConf, chunkError) = transcribeParagraphPcmChunks(pcmBytes)
            if (isCancelled) {
                release()
                return SessionCaptureResult(
                    transcript = "",
                    spokenWords = emptyList(),
                    grammarWords = emptyList(),
                    energyWindows = emptyList(),
                    wavFilePath = "",
                    audioBytesWritten = 0L,
                    errorNotice = null
                )
            }
            val chunkWords = chunkedText.split(Regex("\\s+")).count { it.isNotBlank() }
            val liveWords = finalTranscript.split(Regex("\\s+")).count { it.isNotBlank() }
            if (chunkedText.isNotBlank() && (chunkWords >= liveWords || durationSec > LONG_UTTERANCE_SECONDS || liveEndedEarlyWithSpeech)) {
                finalTranscript = chunkedText
                finalConfidence = chunkConf
                emitTranscript(finalTranscript)
            } else if (finalTranscript.isBlank()) {
                notice = mapErrorToUserNotice(chunkError ?: liveErrorCode)
            }
        } else if (finalTranscript.isBlank() && liveErrorCode != null) {
            notice = mapErrorToUserNotice(liveErrorCode)
        }

        val spokenWords = PronunciationScorer.buildSpokenWordsFromTranscript(
            transcript = finalTranscript,
            energyWindows = windowsCopy,
            confidence = finalConfidence,
            expectedWordCount = expectedWordCount
        )

        release()

        return SessionCaptureResult(
            transcript = finalTranscript,
            spokenWords = spokenWords,
            grammarWords = emptyList(),
            energyWindows = windowsCopy,
            wavFilePath = wavFile.absolutePath,
            audioBytesWritten = totalAudioBytes,
            errorNotice = notice
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun transcribeParagraphPcmChunks(pcmBytes: ByteArray): Triple<String, Float, Int?> {
        if (pcmBytes.isEmpty() || isCancelled) return Triple("", 0.9f, null)

        val allChunks = splitParagraphPcmIntoChunks(pcmBytes)
        val speechChunks = allChunks.filter { chunkHasSpeech(it) }
        if (speechChunks.isEmpty()) return Triple("", 0.9f, null)

        val combinedParts = mutableListOf<String>()
        val confidences = mutableListOf<Float>()
        var lastError: Int? = null

        var activeLang = languageTagForAccent(accentCode)
        var activePreferOnDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

        val totalDeadlineMs = System.currentTimeMillis() + maxOf(45_000L, speechChunks.size * 14_000L)

        for ((idx, chunk) in speechChunks.withIndex()) {
            if (isCancelled || isReleased || !currentCoroutineContext().isActive) break
            if (System.currentTimeMillis() >= totalDeadlineMs) {
                Log.w(TAG, "Paragraph transcription reached overall deadline after $idx/${speechChunks.size} chunks")
                break
            }

            onProcessingProgress?.invoke(idx + 1, speechChunks.size)

            var result = recognizeSingleParagraphChunk(chunk, languageTag = activeLang, preferOnDevice = activePreferOnDevice)

            if (isCancelled || isReleased || !currentCoroutineContext().isActive) break

            // Transient busy error: brief backoff and retry once
            if (result.first.isBlank() && result.third == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                delay(300L)
                if (!isCancelled && !isReleased && currentCoroutineContext().isActive) {
                    result = recognizeSingleParagraphChunk(chunk, languageTag = activeLang, preferOnDevice = activePreferOnDevice)
                }
            }

            // If regional offline pack (e.g. en-IN) is unavailable, switch to en-US for this and all remaining chunks
            if (result.first.isBlank() &&
                (result.third == ERROR_LANGUAGE_UNAVAILABLE_CODE || result.third == ERROR_LANGUAGE_NOT_SUPPORTED_CODE) &&
                activeLang != "en-US" &&
                !isCancelled
            ) {
                activeLang = "en-US"
                result = recognizeSingleParagraphChunk(chunk, languageTag = activeLang, preferOnDevice = activePreferOnDevice)
            }

            // If on-device recognizer itself failed to initialize (ERROR_CLIENT), fall back once
            if (result.first.isBlank() &&
                activePreferOnDevice &&
                result.third == SpeechRecognizer.ERROR_CLIENT &&
                !isCancelled
            ) {
                activePreferOnDevice = false
                result = recognizeSingleParagraphChunk(chunk, languageTag = activeLang, preferOnDevice = false)
            }

            if (result.first.isNotBlank()) {
                combinedParts.add(result.first.trim())
                confidences.add(result.second)
                emitTranscript(combinedParts.joinToString(" "))
            } else if (result.third != null) {
                lastError = result.third
            }

            if (idx < speechChunks.lastIndex && !isCancelled) {
                delay(80L)
            }
        }

        val joined = combinedParts.joinToString(" ").trim()
        val avgConf = if (confidences.isNotEmpty()) confidences.average().toFloat() else 0.9f
        return Triple(joined, avgConf, lastError)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun recognizeSingleParagraphChunk(
        chunkBytes: ByteArray,
        languageTag: String,
        preferOnDevice: Boolean
    ): Triple<String, Float, Int?> {
        if (isCancelled || isReleased) return Triple("", 0.9f, null)

        val pipe = try {
            ParcelFileDescriptor.createPipe()
        } catch (e: IOException) {
            Log.e(TAG, "Paragraph chunk pipe creation failed", e)
            return Triple("", 0.9f, SpeechRecognizer.ERROR_AUDIO)
        }
        val readFd = pipe[0]
        val writeFd = pipe[1]
        val readFdClosed = AtomicBoolean(false)
        fun closeReadFdOnce() {
            if (readFdClosed.compareAndSet(false, true)) {
                runCatching { readFd.close() }
            }
        }

        val deferred = CompletableDeferred<Triple<String, Float, Int?>>()
        val partialBackup = AtomicReference("")
        var chunkRec: SpeechRecognizer? = null
        activeChunkDeferred = deferred
        activeChunkReadFd = readFd
        activeChunkWriteFd = writeFd

        withContext(Dispatchers.Main.immediate) {
            try {
                if (isCancelled || isReleased) {
                    closeReadFdOnce()
                    if (!deferred.isCompleted) deferred.complete(Triple("", 0.9f, null))
                    return@withContext
                }
                chunkRec = if (preferOnDevice && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
                ) {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                } else {
                    SpeechRecognizer.createSpeechRecognizer(context)
                }
                activeChunkRecognizer = chunkRec

                chunkRec?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        // Remote RecognitionService has received its duplicated readFd;
                        // closing our local reference prevents write-end pipe deadlocks.
                        closeReadFdOnce()
                    }
                    override fun onBeginningOfSpeech() {
                        closeReadFdOnce()
                    }
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}

                    override fun onPartialResults(partialResults: Bundle?) {
                        val txt = extractRecognitionText(partialResults)
                        if (txt.isNotBlank()) {
                            val prev = partialBackup.get()
                            if (txt.length >= prev.length) {
                                partialBackup.set(txt)
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        closeReadFdOnce()
                        val txt = extractRecognitionText(results).ifBlank { partialBackup.get() }
                        val conf = extractConfidence(results)
                        if (!deferred.isCompleted) {
                            deferred.complete(Triple(txt, conf, null))
                        }
                    }

                    override fun onError(error: Int) {
                        closeReadFdOnce()
                        if (!deferred.isCompleted) {
                            deferred.complete(Triple(partialBackup.get(), 0.85f, error))
                        }
                    }
                })
                val intent = buildOfflineIntent(languageTag = languageTag, readFd = readFd)
                chunkRec?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Paragraph chunk recognizer failed to start", e)
                closeReadFdOnce()
                if (!deferred.isCompleted) {
                    deferred.complete(Triple("", 0.9f, SpeechRecognizer.ERROR_CLIENT))
                }
            }
        }

        // Write PCM concurrently on Dispatchers.IO while awaiting deferred with a bounded timeout
        // so a recognizer that stops reading early can NEVER deadlock the coroutine!
        val writerJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                ParcelFileDescriptor.AutoCloseOutputStream(writeFd).use { out ->
                    writeSilenceInChunksCancellable(out, PIPE_LEAD_SILENCE_BYTES) {
                        deferred.isCompleted || isCancelled || isReleased
                    }
                    var offset = 0
                    val step = 3200
                    while (offset < chunkBytes.size && !deferred.isCompleted && !isCancelled && !isReleased) {
                        val len = minOf(step, chunkBytes.size - offset)
                        out.write(chunkBytes, offset, len)
                        offset += len
                    }
                    if (!deferred.isCompleted && !isCancelled && !isReleased) {
                        writeSilenceInChunksCancellable(out, PIPE_TAIL_SILENCE_BYTES) {
                            deferred.isCompleted || isCancelled || isReleased
                        }
                        out.flush()
                    }
                }
            } catch (_: IOException) {
                // Recognizer closed read end early or analysis was cancelled
            } catch (_: Exception) {
            }
        }

        val chunkDurationMs = (chunkBytes.size.toLong() * 1000L) / BYTES_PER_SECOND
        val chunkTimeoutMs = (chunkDurationMs + 6_000L).coerceIn(8_000L, 18_000L)

        return try {
            withTimeoutOrNull(chunkTimeoutMs) {
                deferred.await()
            } ?: run {
                val rescued = partialBackup.get()
                Log.w(TAG, "Paragraph chunk timed out after ${chunkTimeoutMs}ms; rescued='${rescued.take(40)}'")
                if (rescued.isNotBlank()) {
                    Triple(rescued, 0.85f, null)
                } else {
                    Triple("", 0.9f, SpeechRecognizer.ERROR_SPEECH_TIMEOUT)
                }
            }
        } finally {
            activeChunkDeferred = null
            activeChunkReadFd = null
            activeChunkWriteFd = null
            activeChunkRecognizer = null
            withContext(NonCancellable + Dispatchers.Main.immediate) {
                try { chunkRec?.cancel() } catch (_: Exception) {}
                try { chunkRec?.destroy() } catch (_: Exception) {}
                closeReadFdOnce()
                runCatching { writeFd.close() }
            }
            writerJob.cancel()
        }
    }

    private fun splitParagraphPcmIntoChunks(pcmBytes: ByteArray): List<ByteArray> {
        if (pcmBytes.size <= PARAGRAPH_MAX_CHUNK_BYTES) {
            return listOf(pcmBytes)
        }
        val chunks = mutableListOf<ByteArray>()
        var start = 0
        while (start < pcmBytes.size) {
            val remaining = pcmBytes.size - start
            if (remaining <= PARAGRAPH_MAX_CHUNK_BYTES) {
                chunks.add(pcmBytes.copyOfRange(start, pcmBytes.size))
                break
            }
            val searchFrom = (start + PARAGRAPH_MIN_CHUNK_BYTES) and 1.inv()
            val searchTo = minOf(pcmBytes.size - 2, start + PARAGRAPH_MAX_CHUNK_BYTES) and 1.inv()
            var bestCut = (start + PARAGRAPH_TARGET_CHUNK_BYTES) and 1.inv()
            var lowestRms = Float.MAX_VALUE

            val frameSize = 3200 // 100ms
            var pos = searchFrom
            while (pos + frameSize <= searchTo) {
                val rms = computeRangeRms(pcmBytes, pos, frameSize)
                if (rms < lowestRms) {
                    lowestRms = rms
                    bestCut = (pos + frameSize / 2) and 1.inv()
                }
                pos += frameSize
            }
            bestCut = bestCut.coerceIn(start + 3200, pcmBytes.size) and 1.inv()
            chunks.add(pcmBytes.copyOfRange(start, bestCut))
            start = bestCut
        }
        return chunks
    }

    private fun writeSilenceInChunksCancellable(
        out: java.io.OutputStream,
        totalBytes: Int,
        shouldStop: () -> Boolean
    ) {
        val step = 3200
        val zero = ByteArray(step)
        var written = 0
        while (written < totalBytes && !shouldStop()) {
            val len = minOf(step, totalBytes - written)
            out.write(zero, 0, len)
            written += len
        }
    }

    override suspend fun release() {
        if (isReleased) return
        isReleased = true
        recording = false
        if (stopRequestedAtMs == 0L) {
            stopRequestedAtMs = System.currentTimeMillis()
        }
        paragraphPipeChannel?.close()
        paragraphPipeWriterJob?.cancel()

        withContext(NonCancellable + Dispatchers.Main.immediate) {
            try { activeChunkRecognizer?.cancel() } catch (_: Exception) {}
            try { activeChunkRecognizer?.destroy() } catch (_: Exception) {}
            activeChunkRecognizer = null
            runCatching { activeChunkReadFd?.close() }
            runCatching { activeChunkWriteFd?.close() }
            activeChunkReadFd = null
            activeChunkWriteFd = null
            destroyActiveRecognizerOnMain()
            closePipeFds()
        }

        try {
            withTimeoutOrNull(1500L) {
                captureJob?.join()
            }
        } catch (_: Exception) {}

        MicrophoneCaptureService.stop(context)

        try { captureDispatcher.close() } catch (_: Exception) {}
        try { captureExecutor.shutdownNow() } catch (_: Exception) {}
    }

    /**
     * Splits captured 16 kHz PCM16 audio into ~15-second chunks (snapping to low-energy pauses
     * when available) and runs Android offline SpeechRecognizer sequentially via EXTRA_AUDIO_SOURCE.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun transcribePcmChunks(pcmBytes: ByteArray): Triple<String, Float, Int?> {
        if (pcmBytes.isEmpty()) return Triple("", 0.9f, null)

        val chunks = splitPcmIntoChunks(pcmBytes)
        val combinedParts = mutableListOf<String>()
        val confidences = mutableListOf<Float>()
        var lastError: Int? = null
        val primaryLang = languageTagForAccent(accentCode)

        for (chunk in chunks) {
            if (!chunkHasSpeech(chunk)) continue

            var result = recognizeSinglePcmChunk(chunk, languageTag = primaryLang, preferOnDevice = true)
            if (result.first.isBlank() &&
                (result.third == ERROR_LANGUAGE_UNAVAILABLE_CODE || result.third == ERROR_LANGUAGE_NOT_SUPPORTED_CODE) &&
                primaryLang != "en-US"
            ) {
                result = recognizeSinglePcmChunk(chunk, languageTag = "en-US", preferOnDevice = true)
            }
            if (result.first.isBlank() &&
                result.third != null &&
                result.third != SpeechRecognizer.ERROR_NO_MATCH &&
                result.third != SpeechRecognizer.ERROR_SPEECH_TIMEOUT
            ) {
                result = recognizeSinglePcmChunk(chunk, languageTag = primaryLang, preferOnDevice = false)
                if (result.first.isBlank() && primaryLang != "en-US") {
                    result = recognizeSinglePcmChunk(chunk, languageTag = "en-US", preferOnDevice = false)
                }
            }

            if (result.first.isNotBlank()) {
                combinedParts.add(result.first.trim())
                confidences.add(result.second)
                emitTranscript(combinedParts.joinToString(" "))
            } else if (result.third != null) {
                lastError = result.third
            }
            delay(60L)
        }

        val joined = combinedParts.joinToString(" ").trim()
        val avgConf = if (confidences.isNotEmpty()) confidences.average().toFloat() else 0.9f
        return Triple(joined, avgConf, lastError)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun recognizeSinglePcmChunk(
        chunkBytes: ByteArray,
        languageTag: String,
        preferOnDevice: Boolean
    ): Triple<String, Float, Int?> {
        val pipe = try {
            ParcelFileDescriptor.createPipe()
        } catch (e: IOException) {
            Log.e(TAG, "Chunk pipe creation failed", e)
            return Triple("", 0.9f, SpeechRecognizer.ERROR_AUDIO)
        }
        val readFd = pipe[0]
        val writeFd = pipe[1]

        val deferred = CompletableDeferred<Triple<String, Float, Int?>>()
        var chunkRec: SpeechRecognizer? = null

        withContext(Dispatchers.Main.immediate) {
            try {
                chunkRec = if (preferOnDevice && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
                ) {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                } else {
                    SpeechRecognizer.createSpeechRecognizer(context)
                }
                var partialBackup = ""
                chunkRec?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}

                    override fun onPartialResults(partialResults: Bundle?) {
                        val txt = extractRecognitionText(partialResults)
                        if (txt.length >= partialBackup.length) {
                            partialBackup = txt
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val txt = extractRecognitionText(results).ifBlank { partialBackup }
                        val conf = extractConfidence(results)
                        if (!deferred.isCompleted) {
                            deferred.complete(Triple(txt, conf, null))
                        }
                    }

                    override fun onError(error: Int) {
                        if (!deferred.isCompleted) {
                            deferred.complete(Triple(partialBackup, 0.85f, error))
                        }
                    }
                })
                val intent = buildOfflineIntent(languageTag = languageTag, readFd = readFd)
                chunkRec?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Chunk recognizer failed to start", e)
                if (!deferred.isCompleted) {
                    deferred.complete(Triple("", 0.9f, SpeechRecognizer.ERROR_CLIENT))
                }
            }
        }

        // Feed leading silence + chunk + trailing silence on IO dispatcher
        withContext(Dispatchers.IO) {
            ParcelFileDescriptor.AutoCloseOutputStream(writeFd).use { out ->
                try {
                    writeSilenceInChunks(out, PIPE_LEAD_SILENCE_BYTES)
                    var offset = 0
                    val step = 3200
                    while (offset < chunkBytes.size) {
                        val len = minOf(step, chunkBytes.size - offset)
                        out.write(chunkBytes, offset, len)
                        offset += len
                    }
                    writeSilenceInChunks(out, PIPE_TAIL_SILENCE_BYTES)
                    out.flush()
                } catch (_: IOException) {
                    // Recognizer closed read end early
                }
            }
        }

        val outcome = withTimeoutOrNull(20_000L) {
            deferred.await()
        } ?: Triple("", 0.9f, SpeechRecognizer.ERROR_SPEECH_TIMEOUT)

        withContext(Dispatchers.Main.immediate) {
            try {
                chunkRec?.cancel()
            } catch (_: Exception) {}
            try {
                chunkRec?.destroy()
            } catch (_: Exception) {}
            runCatching { readFd.close() }
        }

        return outcome
    }

    private fun splitPcmIntoChunks(pcmBytes: ByteArray): List<ByteArray> {
        if (pcmBytes.size <= MAX_CHUNK_SEEK_BYTES) {
            return listOf(pcmBytes)
        }
        val chunks = mutableListOf<ByteArray>()
        var start = 0
        while (start < pcmBytes.size) {
            val remaining = pcmBytes.size - start
            if (remaining <= MAX_CHUNK_SEEK_BYTES) {
                chunks.add(pcmBytes.copyOfRange(start, pcmBytes.size))
                break
            }
            val searchFrom = (start + MIN_CHUNK_SEEK_BYTES) and 1.inv()
            val searchTo = minOf(pcmBytes.size - 2, start + MAX_CHUNK_SEEK_BYTES) and 1.inv()
            var bestCut = (start + TARGET_CHUNK_BYTES) and 1.inv()
            var lowestRms = Float.MAX_VALUE

            val frameSize = 3200 // 100ms
            var pos = searchFrom
            while (pos + frameSize <= searchTo) {
                val rms = computeRangeRms(pcmBytes, pos, frameSize)
                if (rms < lowestRms) {
                    lowestRms = rms
                    bestCut = (pos + frameSize / 2) and 1.inv()
                }
                pos += frameSize
            }
            bestCut = bestCut.coerceIn(start + 3200, pcmBytes.size) and 1.inv()
            chunks.add(pcmBytes.copyOfRange(start, bestCut))
            start = bestCut
        }
        return chunks
    }

    private fun chunkHasSpeech(pcmBytes: ByteArray): Boolean {
        val frameSize = 3200
        var pos = 0
        var maxRms = 0f
        while (pos + 2 <= pcmBytes.size) {
            val len = minOf(frameSize, pcmBytes.size - pos) and 1.inv()
            if (len > 0) {
                val rms = computeRangeRms(pcmBytes, pos, len)
                if (rms > maxRms) maxRms = rms
                if (maxRms >= 220f) return true
            }
            pos += frameSize
        }
        return maxRms >= 220f
    }

    private fun writeSilenceInChunks(out: java.io.OutputStream, totalBytes: Int) {
        val step = 3200
        val zero = ByteArray(step)
        var written = 0
        while (written < totalBytes) {
            val len = minOf(step, totalBytes - written)
            out.write(zero, 0, len)
            written += len
        }
    }

    private fun createBestOfflineRecognizer(): SpeechRecognizer {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        ) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
    }

    private fun buildOfflineIntent(
        languageTag: String,
        readFd: ParcelFileDescriptor? = null
    ): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && readFd != null) {
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, readFd)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, CHANNELS)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, RATE)
            }
        }
    }

    private fun createStreamingListener(
        deferred: CompletableDeferred<String>,
        onPartial: (String) -> Unit,
        onFinal: (String, Float) -> Unit,
        onFailure: (Int) -> Unit,
        onRmsDb: ((Float) -> Unit)? = null
    ): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {
                onRmsDb?.invoke(rmsdB)
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onPartialResults(partialResults: Bundle?) {
                val txt = extractRecognitionText(partialResults)
                if (txt.isNotBlank()) {
                    onPartial(txt)
                }
            }

            override fun onResults(results: Bundle?) {
                val txt = extractRecognitionText(results)
                val conf = extractConfidence(results)
                onFinal(txt, conf)
                if (!deferred.isCompleted) deferred.complete(txt)
            }

            override fun onError(error: Int) {
                Log.w(TAG, "SpeechRecognizer onError=$error")
                onFailure(error)
                if (!deferred.isCompleted) deferred.complete("")
            }
        }
    }

    private fun extractRecognitionText(bundle: Bundle?): String {
        val matches = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        return matches?.firstOrNull()?.trim().orEmpty()
    }

    private fun extractConfidence(bundle: Bundle?): Float {
        val scores = bundle?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
        val raw = scores?.firstOrNull()
        return if (raw != null && raw in 0.01f..1.0f) raw else 0.92f
    }

    private fun destroyActiveRecognizerOnMain() {
        val rec = activeRecognizer ?: return
        activeRecognizer = null
        try {
            rec.cancel()
        } catch (_: Exception) {}
        try {
            rec.destroy()
        } catch (_: Exception) {}
    }

    private fun closePipeFds() {
        runCatching { pipeWriteFd?.close() }
        pipeWriteFd = null
        runCatching { pipeReadFd?.close() }
        pipeReadFd = null
    }

    private fun hasSpeechAudio(windows: List<AudioEnergyWindow>): Boolean {
        if (windows.isEmpty()) return false
        val speechCount = windows.count { it.rms >= 260f }
        return speechCount >= 3
    }

    private fun hasSpeechAfterByteOffset(windows: List<AudioEnergyWindow>, byteOffset: Long): Boolean {
        val thresholdSec = byteOffset.toFloat() / BYTES_PER_SECOND
        val trailingSpeechWins = windows.count { it.startSec >= thresholdSec && it.rms >= 280f }
        return trailingSpeechWins >= 4
    }

    private fun mapErrorToUserNotice(errorCode: Int?): String? {
        return when (errorCode) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "Microphone permission is required to analyze your speech."
            ERROR_LANGUAGE_UNAVAILABLE_CODE, ERROR_LANGUAGE_NOT_SUPPORTED_CODE ->
                "Offline English speech recognition is not yet enabled on this device. Install Offline Speech Recognition in Android Settings."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                "Speech recognition was busy. Please tap the orb and try again."
            SpeechRecognizer.ERROR_CLIENT ->
                "On-device speech recognition is unavailable on this device."
            else -> null
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
        return computeRangeRms(buf, 0, bytesRead)
    }

    private fun computeRangeRms(buf: ByteArray, offset: Int, bytesRead: Int): Float {
        var sumSquares = 0.0
        val sampleCount = bytesRead / 2
        if (sampleCount <= 0) return 0f
        var i = offset
        val limit = minOf(buf.size, offset + bytesRead)
        while (i + 1 < limit) {
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
}
