package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.ByteArrayOutputStream
import kotlin.random.Random

/**
 * ONE AudioRecord (owned here) feeds BOTH the WAV buffer and the recognizer, live.
 * By the time the user taps Stop, the engine has already heard (and mostly decoded)
 * the speech, so the result arrives ~0.7-1.5 s after Stop instead of 4-5 s.
 * A fresh recognizer is created per session and always destroyed.
 */
class LiveSpeechSession(
    private val app: Application,
    private val scope: CoroutineScope,
    private val itemType: String = "",
    private val targetText: String = ""
) {
    companion object { const val RATE = 16000 }

    private val pcm = ByteArrayOutputStream()
    private val queue = Channel<ByteArray>(Channel.UNLIMITED)
    private val ready = CompletableDeferred<Unit>()
    private val result = CompletableDeferred<String>()

    @Volatile private var recording = false
    @Volatile private var latest = ""
    private var recognizer: SpeechRecognizer? = null
    private var readSide: ParcelFileDescriptor? = null
    private var recordJob: Job? = null
    private var writerJob: Job? = null

    /** Call on the main thread when the user taps Record. */
    @SuppressLint("MissingPermission")
    fun start() {
        val pipe = ParcelFileDescriptor.createReliablePipe()
        readSide = pipe[0]
        val writeSide = pipe[1]

        // 1) Recognizer (must be created/started on Main)
        val r = if (Build.VERSION.SDK_INT >= 33 &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(app)
        ) SpeechRecognizer.createOnDeviceSpeechRecognizer(app)
        else SpeechRecognizer.createSpeechRecognizer(app)
        recognizer = r

        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { ready.complete(Unit) }
            override fun onResults(results: Bundle?) {
                val best = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                result.complete(if (!best.isNullOrBlank()) best else latest)
            }
            override fun onError(error: Int) { result.complete(latest) }
            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()?.takeIf { it.isNotBlank() }?.let { latest = it }
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val lmModel = if (itemType == "tongue_twister") {
            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
        } else {
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, lmModel)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            if (targetText.isNotBlank()) {
                putExtra(RecognizerIntent.EXTRA_PROMPT, targetText)
                putExtra("android.speech.extra.PROMPT", targetText)
            }
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            if (Build.VERSION.SDK_INT >= 33) {
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, readSide)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, RATE)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
            }
        }
        r.startListening(intent)

        // 2) Pipe writer (IO): waits for the engine, then forwards live chunks
        writerJob = scope.launch(Dispatchers.IO) {
            try {
                ParcelFileDescriptor.AutoCloseOutputStream(writeSide).use { out ->
                    withTimeoutOrNull(5_000) { ready.await() }
                    for (chunk in queue) { 
                        out.write(chunk)
                        out.flush() 
                        delay(100) // Ensure real-time pacing even when flushing backlog
                    }
                }
            } catch (_: Exception) { }
        }

        // 3) Single AudioRecord (IO): tee to WAV buffer + recognizer queue
        recording = true
        recordJob = scope.launch(Dispatchers.IO) {
            val minBuf = AudioRecord.getMinBufferSize(
                RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val chunkBytes = RATE / 10 * 2 // 100 ms
            val rec = AudioRecord(
                MediaRecorder.AudioSource.MIC, RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minBuf, chunkBytes * 4)
            )
            val buf = ByteArray(chunkBytes)
            try {
                rec.startRecording()
                while (recording) {
                    val n = rec.read(buf, 0, buf.size)
                    if (n > 0) {
                        val copy = buf.copyOf(n)
                        synchronized(pcm) { pcm.write(copy) }
                        queue.trySend(copy)
                    }
                }
            } finally {
                try { rec.stop() } catch (_: Exception) {}
                rec.release()
            }
        }
    }

    /** Returns (recognizedText, rawPcm). Typically ~0.7-1.5 s. */
    suspend fun stop(): Pair<String, ByteArray> {
        recording = false
        recordJob?.join()

        // Short 0.4 s real-time tail of faint noise so the endpointer closes cleanly
        repeat(4) {
            val tail = ByteArray(RATE / 10 * 2)
            for (i in tail.indices step 2) {
                val s = Random.nextInt(-3, 4)
                tail[i] = (s and 0xFF).toByte()
                tail[i + 1] = ((s shr 8) and 0xFF).toByte()
            }
            queue.trySend(tail)
            delay(100)
        }
        queue.close()          // writer drains, closes pipe = end of audio
        writerJob?.join()

        val text = withTimeoutOrNull(3_000) { result.await() } ?: latest
        release()
        val bytes = synchronized(pcm) { pcm.toByteArray() }
        return text to bytes
    }

    /** Also call this if the user leaves the screen mid-recording. */
    suspend fun release() {
        recording = false
        withContext(NonCancellable + Dispatchers.Main) {
            try { recognizer?.cancel() } catch (_: Exception) {}
            try { recognizer?.destroy() } catch (_: Exception) {}
            recognizer = null
        }
        try { readSide?.close() } catch (_: Exception) {}
    }
}