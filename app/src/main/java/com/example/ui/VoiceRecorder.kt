package com.example.ui

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class VoiceRecorder(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var mediaPlayer: MediaPlayer? = null
    private val pcmFile = File(context.cacheDir, "user_voice_attempt.raw")
    private val wavFile = File(context.cacheDir, "user_voice_attempt.wav")

    @Volatile
    private var isRecordingInternal = false
    private var recordingThread: Thread? = null

    @Volatile
    private var currentMaxAmplitude = 0

    val isPlaying: Boolean
        get() = try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }

    fun startRecording(): Boolean {
        stopRecording() // Clean up active recording
        stopPlayback()  // Clean up active playback

        try {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = Math.max(minBufferSize, 2048)

            val recorder = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("SPEECH_DEBUG", "VoiceRecorder: AudioRecord failed to initialize")
                recorder.release()
                return false
            }

            recorder.startRecording()
            audioRecord = recorder
            isRecordingInternal = true
            currentMaxAmplitude = 0

            recordingThread = Thread {
                writePcmData(recorder, bufferSize)
            }.apply { start() }

            Log.d("SPEECH_DEBUG", "VoiceRecorder: Started AudioRecord into ${wavFile.absolutePath}")
            return true
        } catch (e: Exception) {
            Log.e("SPEECH_DEBUG", "VoiceRecorder: Failed to start AudioRecord", e)
            return false
        }
    }

    private fun writePcmData(recorder: AudioRecord, bufferSize: Int) {
        val buffer = ByteArray(bufferSize)
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream(pcmFile)
            while (isRecordingInternal) {
                val read = recorder.read(buffer, 0, bufferSize)
                if (read > 0) {
                    os.write(buffer, 0, read)
                    var peak = 0
                    for (i in 0 until read - 1 step 2) {
                        val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
                        val abs = Math.abs(sample.toShort().toInt())
                        if (abs > peak) peak = abs
                    }
                    currentMaxAmplitude = peak
                }
            }
        } catch (e: Exception) {
            Log.e("SPEECH_DEBUG", "VoiceRecorder: Error writing PCM data", e)
        } finally {
            try {
                os?.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun stopRecording() {
        isRecordingInternal = false
        try {
            audioRecord?.apply {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            }
            recordingThread?.join(500)
            convertPcmToWav(pcmFile, wavFile, 16000, 1, 16)
            Log.d("SPEECH_DEBUG", "VoiceRecorder: Stopped AudioRecord and generated WAV (${wavFile.length()} bytes)")
        } catch (e: Exception) {
            Log.e("SPEECH_DEBUG", "VoiceRecorder: Failed to stop AudioRecord", e)
        } finally {
            audioRecord = null
            recordingThread = null
        }
    }

    fun getMaxAmplitude(): Int {
        val amp = currentMaxAmplitude
        currentMaxAmplitude = 0
        return amp
    }

    fun startPlayback(onComplete: () -> Unit = {}) {
        stopPlayback()

        if (!wavFile.exists() || wavFile.length() <= 44) {
            Log.e("SPEECH_DEBUG", "VoiceRecorder: No recorded WAV file found to play.")
            onComplete()
            return
        }

        try {
            val player = MediaPlayer().apply {
                setDataSource(wavFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    onComplete()
                }
            }
            mediaPlayer = player
            Log.d("SPEECH_DEBUG", "VoiceRecorder: Started playback of: ${wavFile.absolutePath}")
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
        return if (wavFile.exists() && wavFile.length() > 44) wavFile else null
    }

    private fun convertPcmToWav(
        rawFile: File,
        waveFile: File,
        sampleRate: Long,
        channels: Int,
        bitsPerSample: Int
    ) {
        if (!rawFile.exists()) return

        val byteRate = sampleRate * channels * bitsPerSample / 8
        val totalAudioLen = rawFile.length()
        val totalDataLen = totalAudioLen + 36

        var `in`: FileInputStream? = null
        var out: FileOutputStream? = null

        try {
            `in` = FileInputStream(rawFile)
            out = FileOutputStream(waveFile)

            val header = ByteArray(44)
            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = (totalDataLen shr 8 and 0xff).toByte()
            header[6] = (totalDataLen shr 16 and 0xff).toByte()
            header[7] = (totalDataLen shr 24 and 0xff).toByte()
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16 // 4 bytes: size of 'fmt ' chunk
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1 // Format = 1 (PCM)
            header[21] = 0
            header[22] = channels.toByte()
            header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = (sampleRate shr 8 and 0xff).toByte()
            header[26] = (sampleRate shr 16 and 0xff).toByte()
            header[27] = (sampleRate shr 24 and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = (byteRate shr 8 and 0xff).toByte()
            header[30] = (byteRate shr 16 and 0xff).toByte()
            header[31] = (byteRate shr 24 and 0xff).toByte()
            header[32] = (channels * bitsPerSample / 8).toByte() // block align
            header[33] = 0
            header[34] = bitsPerSample.toByte()
            header[35] = 0
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = (totalAudioLen shr 8 and 0xff).toByte()
            header[42] = (totalAudioLen shr 16 and 0xff).toByte()
            header[43] = (totalAudioLen shr 24 and 0xff).toByte()

            out.write(header, 0, 44)

            val buffer = ByteArray(2048)
            var bytesRead: Int
            while (`in`.read(buffer).also { bytesRead = it } != -1) {
                out.write(buffer, 0, bytesRead)
            }
        } catch (e: Exception) {
            Log.e("SPEECH_DEBUG", "VoiceRecorder: Error writing WAV header/data", e)
        } finally {
            try {
                `in`?.close()
                out?.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
