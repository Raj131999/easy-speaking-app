package com.example.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

class VoiceRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private val recordedFile = File(context.cacheDir, "user_voice_attempt.wav")
    private val legacyRecordedFile = File(context.cacheDir, "user_voice_attempt.m4a")

    private val playbackScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var playbackJob: Job? = null
    private val audioTrackLock = Any()
    @Volatile
    private var activeAudioTrack: AudioTrack? = null
    @Volatile
    private var activeSampleRate: Int = 16000
    private val playbackCancelled = AtomicBoolean(false)

    @Volatile
    private var isRecordingInternal = false

    val isRecording: Boolean
        get() = isRecordingInternal

    val isPlaying: Boolean
        get() = try {
            (playbackJob?.isActive == true) || (mediaPlayer?.isPlaying == true)
        } catch (_: Exception) {
            false
        }

    fun saveRawPcmToWav(pcmData: ByteArray, sampleRate: Int = 16000, channels: Int = 1, bitsPerSample: Int = 16) {
        try {
            clearRecordedFile()
            val totalAudioLen = pcmData.size.toLong()
            val totalDataLen = totalAudioLen + 36
            val byteRate = (sampleRate * channels * bitsPerSample / 8).toLong()

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
            header[22] = channels.toByte()
            header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xffL).toByte()
            header[29] = ((byteRate shr 8) and 0xffL).toByte()
            header[30] = ((byteRate shr 16) and 0xffL).toByte()
            header[31] = ((byteRate shr 24) and 0xffL).toByte()
            header[32] = (channels * bitsPerSample / 8).toByte()
            header[33] = 0
            header[34] = bitsPerSample.toByte()
            header[35] = 0
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xffL).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xffL).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xffL).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xffL).toByte()

            FileOutputStream(recordedFile).use { fos ->
                fos.write(header)
                fos.write(pcmData)
                fos.flush()
            }
            Log.d("SPEECH_DEBUG", "VoiceRecorder: Saved ${pcmData.size} bytes PCM as WAV to ${recordedFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("SPEECH_DEBUG", "VoiceRecorder: Error saving raw PCM to WAV", e)
        }
    }

    fun startRecording(): Boolean {
        stopRecording()
        stopPlayback()
        clearRecordedFile()

        val sources = listOf(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.MIC
        )

        for (source in sources) {
            try {
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }

                recorder.apply {
                    setAudioSource(source)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(96000)
                    setOutputFile(legacyRecordedFile.absolutePath)
                    prepare()
                    start()
                }
                mediaRecorder = recorder
                isRecordingInternal = true
                Log.d("SPEECH_DEBUG", "VoiceRecorder: Started recording with source $source to ${legacyRecordedFile.absolutePath}")
                return true
            } catch (e: Exception) {
                Log.w("SPEECH_DEBUG", "VoiceRecorder: Failed starting with source $source: ${e.message}")
                try {
                    mediaRecorder?.release()
                } catch (_: Exception) {}
                mediaRecorder = null
            }
        }
        return false
    }

    fun stopRecording() {
        isRecordingInternal = false
        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    Log.w("SPEECH_DEBUG", "VoiceRecorder: Exception during stop()", e)
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("SPEECH_DEBUG", "VoiceRecorder: Error stopping MediaRecorder", e)
        } finally {
            mediaRecorder = null
        }
    }

    fun getMaxAmplitude(): Int {
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private var activeCustomFile: File? = null

    fun setActiveRecordedFile(filePath: String?) {
        activeCustomFile = filePath?.takeIf { it.isNotBlank() }?.let { File(it) }
    }

    fun getCurrentPositionMs(): Int {
        return try {
            val track = activeAudioTrack
            if (track != null && track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                val frames = track.playbackHeadPosition.toLong() and 0xFFFFFFFFL
                ((frames * 1000L) / activeSampleRate.coerceAtLeast(1)).toInt()
            } else {
                val player = mediaPlayer
                if (player != null && player.isPlaying) player.currentPosition else -1
            }
        } catch (_: Exception) {
            -1
        }
    }

    fun startPlayback(onComplete: () -> Unit = {}) {
        stopPlayback()

        val fileToPlay = getRecordedFile()
        if (fileToPlay == null || fileToPlay.length() <= 44L) {
            Log.w("SPEECH_DEBUG", "VoiceRecorder: No recorded file found to play.")
            onComplete()
            return
        }

        if (fileToPlay.extension.equals("wav", ignoreCase = true)) {
            startWavAudioTrackPlayback(fileToPlay, onComplete)
        } else {
            startLegacyMediaPlayerPlayback(fileToPlay, onComplete)
        }
    }

    private data class WavSpec(
        val sampleRate: Int,
        val channels: Int,
        val bitsPerSample: Int,
        val dataOffset: Long,
        val dataSize: Long
    )

    private fun parseWavSpec(file: File): WavSpec? {
        val fileLen = file.length()
        if (fileLen <= 44L) return null
        FileInputStream(file).use { fis ->
            val header = ByteArray(minOf(4096, fileLen.toInt()))
            val read = fis.read(header)
            if (read < 44) return null
            if (header[0] != 'R'.code.toByte() || header[1] != 'I'.code.toByte() ||
                header[2] != 'F'.code.toByte() || header[3] != 'F'.code.toByte() ||
                header[8] != 'W'.code.toByte() || header[9] != 'A'.code.toByte() ||
                header[10] != 'V'.code.toByte() || header[11] != 'E'.code.toByte()
            ) {
                return null
            }

            var sampleRate = 16000
            var channels = 1
            var bitsPerSample = 16
            var pos = 12
            val bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

            while (pos + 8 <= read) {
                val id = String(header, pos, 4, Charsets.US_ASCII)
                val chunkSize = bb.getInt(pos + 4).toLong() and 0xFFFFFFFFL
                val chunkDataStart = pos + 8
                when (id) {
                    "fmt " -> {
                        if (chunkDataStart + 16 <= read) {
                            channels = (bb.getShort(chunkDataStart + 2).toInt() and 0xFFFF).coerceAtLeast(1)
                            sampleRate = bb.getInt(chunkDataStart + 4).coerceAtLeast(8000)
                            bitsPerSample = (bb.getShort(chunkDataStart + 14).toInt() and 0xFFFF).coerceAtLeast(8)
                        }
                    }
                    "data" -> {
                        val actualDataSize = minOf(chunkSize, fileLen - chunkDataStart.toLong()).coerceAtLeast(0L)
                        return WavSpec(
                            sampleRate = sampleRate,
                            channels = channels,
                            bitsPerSample = bitsPerSample,
                            dataOffset = chunkDataStart.toLong(),
                            dataSize = actualDataSize
                        )
                    }
                }
                val padded = if (chunkSize % 2L == 1L) chunkSize + 1L else chunkSize
                val next = chunkDataStart.toLong() + padded
                if (next > read) break
                pos = next.toInt()
            }
            return WavSpec(sampleRate, channels, bitsPerSample, 44L, fileLen - 44L)
        }
    }

    private fun startWavAudioTrackPlayback(file: File, onComplete: () -> Unit) {
        val spec = parseWavSpec(file)
        if (spec == null || spec.dataSize <= 0L) {
            Log.w("SPEECH_DEBUG", "VoiceRecorder: Invalid WAV spec for ${file.absolutePath}; falling back to MediaPlayer")
            startLegacyMediaPlayerPlayback(file, onComplete)
            return
        }

        playbackCancelled.set(false)
        activeSampleRate = spec.sampleRate

        playbackJob = playbackScope.launch {
            var track: AudioTrack? = null
            try {
                val channelMask = if (spec.channels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
                val encoding = if (spec.bitsPerSample == 8) AudioFormat.ENCODING_PCM_8BIT else AudioFormat.ENCODING_PCM_16BIT
                val minBuf = AudioTrack.getMinBufferSize(spec.sampleRate, channelMask, encoding)
                    .coerceAtLeast(4096)
                val bytesPerSec = spec.sampleRate * spec.channels * (spec.bitsPerSample / 8)
                val bufSize = maxOf(minBuf * 2, bytesPerSec / 4) // 250ms stream buffer

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(spec.sampleRate)
                            .setChannelMask(channelMask)
                            .setEncoding(encoding)
                            .build()
                    )
                    .setBufferSizeInBytes(bufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                if (track.state != AudioTrack.STATE_INITIALIZED) {
                    throw IllegalStateException("AudioTrack init failed (state=${track.state})")
                }

                synchronized(audioTrackLock) {
                    if (playbackCancelled.get() || !isActive) {
                        track.release()
                        return@launch
                    }
                    activeAudioTrack = track
                }

                track.play()

                val bytesPerFrame = (spec.channels * (spec.bitsPerSample / 8)).coerceAtLeast(1)
                val totalAudioFrames = spec.dataSize / bytesPerFrame

                FileInputStream(file).use { fis ->
                    var toSkip = spec.dataOffset
                    while (toSkip > 0) {
                        val skipped = fis.skip(toSkip)
                        if (skipped <= 0) break
                        toSkip -= skipped
                    }

                    val buffer = ByteArray(4096)
                    var remaining = spec.dataSize
                    while (remaining > 0 && isActive && !playbackCancelled.get()) {
                        val maxRead = minOf(buffer.size.toLong(), remaining).toInt()
                        val read = fis.read(buffer, 0, maxRead)
                        if (read <= 0) break
                        remaining -= read

                        var offset = 0
                        while (offset < read && isActive && !playbackCancelled.get()) {
                            val written = track.write(buffer, offset, read - offset, AudioTrack.WRITE_NON_BLOCKING)
                            if (written < 0) break
                            if (written == 0) {
                                delay(10L)
                            } else {
                                offset += written
                            }
                        }
                    }
                }

                // Append 250ms of trailing silence so hardware sink plays every final syllable cleanly
                if (isActive && !playbackCancelled.get()) {
                    val tailSilence = ByteArray((bytesPerSec / 4).coerceAtLeast(1024))
                    var tailOffset = 0
                    while (tailOffset < tailSilence.size && isActive && !playbackCancelled.get()) {
                        val written = track.write(tailSilence, tailOffset, tailSilence.size - tailOffset, AudioTrack.WRITE_NON_BLOCKING)
                        if (written < 0) break
                        if (written == 0) {
                            delay(10L)
                        } else {
                            tailOffset += written
                        }
                    }

                    val drainDeadline = System.currentTimeMillis() + 1200L
                    while (isActive && !playbackCancelled.get() && System.currentTimeMillis() < drainDeadline) {
                        val head = track.playbackHeadPosition.toLong() and 0xFFFFFFFFL
                        if (head >= totalAudioFrames) break
                        delay(25L)
                    }
                }
            } catch (e: Exception) {
                Log.e("SPEECH_DEBUG", "VoiceRecorder: AudioTrack playback failed", e)
            } finally {
                synchronized(audioTrackLock) {
                    if (activeAudioTrack === track) {
                        activeAudioTrack = null
                    }
                    try {
                        track?.apply {
                            if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                                stop()
                            }
                            release()
                        }
                    } catch (_: Exception) {}
                }

                if (!playbackCancelled.get()) {
                    withContext(Dispatchers.Main) {
                        onComplete()
                    }
                }
            }
        }
    }

    private fun startLegacyMediaPlayerPlayback(fileToPlay: File, onComplete: () -> Unit) {
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setOnCompletionListener { onComplete() }
                setOnErrorListener { _, what, extra ->
                    Log.e("SPEECH_DEBUG", "VoiceRecorder: MediaPlayer error: what=$what, extra=$extra")
                    onComplete()
                    true
                }
                setDataSource(fileToPlay.absolutePath)
                prepare()
                start()
            }
            mediaPlayer = player
            Log.d("SPEECH_DEBUG", "VoiceRecorder: Started MediaPlayer playback of ${fileToPlay.absolutePath} (${fileToPlay.length()} bytes)")
        } catch (e: Exception) {
            Log.e("SPEECH_DEBUG", "VoiceRecorder: Failed to start playback", e)
            onComplete()
        }
    }

    fun stopPlayback() {
        playbackCancelled.set(true)
        synchronized(audioTrackLock) {
            try {
                activeAudioTrack?.apply {
                    if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                        pause()
                        flush()
                    }
                }
            } catch (e: Exception) {
                Log.e("SPEECH_DEBUG", "VoiceRecorder: Failed to pause/flush AudioTrack", e)
            }
        }
        playbackJob?.cancel()
        playbackJob = null

        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("SPEECH_DEBUG", "VoiceRecorder: Failed to stop playback", e)
        } finally {
            mediaPlayer = null
        }
    }

    fun getRecordedFile(): File? {
        val custom = activeCustomFile
        if (custom != null && custom.exists() && custom.length() > 44L) return custom
        if (recordedFile.exists() && recordedFile.length() > 44L) return recordedFile
        if (legacyRecordedFile.exists() && legacyRecordedFile.length() > 0L) return legacyRecordedFile
        return null
    }

    fun clearRecordedFile() {
        try {
            if (recordedFile.exists()) {
                recordedFile.delete()
                Log.d("SPEECH_DEBUG", "VoiceRecorder: Deleted recorded WAV file")
            }
            if (legacyRecordedFile.exists()) {
                legacyRecordedFile.delete()
            }
        } catch (e: Exception) {
            Log.e("SPEECH_DEBUG", "VoiceRecorder: Error clearing old recording file", e)
        }
    }
}
