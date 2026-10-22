package com.example.ui

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class VoiceRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private val recordedFile = File(context.cacheDir, "user_voice_attempt.wav")
    private val legacyRecordedFile = File(context.cacheDir, "user_voice_attempt.m4a")

    @Volatile
    private var isRecordingInternal = false

    val isRecording: Boolean
        get() = isRecordingInternal

    val isPlaying: Boolean
        get() = try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }

    fun saveRawPcmToWav(pcmData: ByteArray, sampleRate: Int = 16000, channels: Int = 1, bitsPerSample: Int = 16) {
        try {
            clearRecordedFile()
            val totalAudioLen = pcmData.size.toLong()
            val totalDataLen = totalAudioLen + 36
            val byteRate = (sampleRate * channels * bitsPerSample / 8).toLong()

            val header = ByteArray(44)
            // RIFF header
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
            // 'fmt ' chunk
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16 // SubChunk1Size (16 for PCM)
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1 // AudioFormat 1 = PCM
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
            header[32] = (channels * bitsPerSample / 8).toByte() // BlockAlign
            header[33] = 0
            header[34] = bitsPerSample.toByte()
            header[35] = 0
            // 'data' chunk
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
                } catch (ex: Exception) {
                    // ignore
                }
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
        } catch (e: Exception) {
            0
        }
    }

    fun startPlayback(onComplete: () -> Unit = {}) {
        stopPlayback()

        val fileToPlay = getRecordedFile()
        if (fileToPlay == null || fileToPlay.length() == 0L) {
            Log.w("SPEECH_DEBUG", "VoiceRecorder: No recorded file found to play.")
            onComplete()
            return
        }

        try {
            val player = MediaPlayer().apply {
                setDataSource(fileToPlay.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    onComplete()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("SPEECH_DEBUG", "VoiceRecorder: MediaPlayer error: what=$what, extra=$extra")
                    onComplete()
                    true
                }
            }
            mediaPlayer = player
            Log.d("SPEECH_DEBUG", "VoiceRecorder: Started playback of: ${fileToPlay.absolutePath} (${fileToPlay.length()} bytes)")
        } catch (e: Exception) {
            Log.e("SPEECH_DEBUG", "VoiceRecorder: Failed to start playback", e)
            onComplete()
        }
    }

    fun stopPlayback() {
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
        if (recordedFile.exists() && recordedFile.length() > 0L) return recordedFile
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
