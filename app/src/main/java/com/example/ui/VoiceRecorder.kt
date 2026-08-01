package com.example.ui

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class VoiceRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordFile: File? = null

    init {
        recordFile = File(context.cacheDir, "user_voice_attempt.m4a")
    }

    fun startRecording(): Boolean {
        stopRecording() // Clean up any active recording
        stopPlayback()  // Clean up any active playback

        try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                // VOICE_RECOGNITION applies high-pass filter, noise suppression and AGC (Automatic Gain Control)
                setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                // 16kHz sampling rate is the universal standard for speech recognition and extremely stable on emulator virtual mics
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(32000)
                setOutputFile(recordFile?.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            Log.d("VoiceRecorder", "Started recording into: ${recordFile?.absolutePath}")
            return true
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Failed to start recording", e)
            return false
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Failed to stop recording (perhaps stopped too early)", e)
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

        if (recordFile == null || !recordFile!!.exists()) {
            Log.e("VoiceRecorder", "No recorded voice file found to play.")
            return
        }

        try {
            val player = MediaPlayer().apply {
                setDataSource(recordFile!!.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    onComplete()
                }
            }
            mediaPlayer = player
            Log.d("VoiceRecorder", "Started playback of: ${recordFile!!.absolutePath}")
        } catch (e: IOException) {
            Log.e("VoiceRecorder", "Failed to start playback", e)
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
            Log.e("VoiceRecorder", "Failed to stop playback", e)
        } finally {
            mediaPlayer = null
        }
    }

    fun getRecordedFile(): File? {
        return if (recordFile != null && recordFile!!.exists()) recordFile else null
    }
}
