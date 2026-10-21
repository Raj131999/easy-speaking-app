package com.example.ui

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.io.File
import java.util.*

enum class Screen {
    Home,
    Grammar,
    Conversation,
    DailySentences,
    Paragraph,
    TongueTwister,
    Settings
}

enum class WordScoreType {
    Correct,
    Hesitant,
    Incorrect
}

class EnglishViewModel(
    application: Application,
    private val repository: EnglishRepository
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    // Navigation state
    val screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val currentScreen = screenStack.map { it.lastOrNull() ?: Screen.Home }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Screen.Home
    )

    // Curriculum and User metrics from Room
    val userProgress = repository.userProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val appOpenDates = repository.appOpenDates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val grammarLessons = repository.grammarLessons.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val conversations = repository.conversations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dailySentences = repository.dailySentences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val paragraphs = repository.paragraphs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tongueTwisters = repository.tongueTwisters.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // TTS Engine States
    private var tts: TextToSpeech? = null
    val ttsReady = MutableStateFlow(false)
    val isTtsSpeaking = MutableStateFlow(false)
    val currentTtsText = MutableStateFlow<String?>(null)
    val selectedAccent = MutableStateFlow("US") // "US", "UK", "IN", "AU"
    val selectedSpeed = MutableStateFlow(1.0f)   // 0.5f, 0.75f, 1.0f, 1.25f, 1.5f

    // Voice recording and play states
    private val voiceRecorder = VoiceRecorder(application)
    private val recordedAudioStream = java.io.ByteArrayOutputStream()
    val isRecording = MutableStateFlow(false)
    val isProcessingSpeech = MutableStateFlow(false)
    val isPlayingBack = MutableStateFlow(false)
    val lastScore = MutableStateFlow<Int?>(null)
    val scoredWords = MutableStateFlow<List<Pair<String, WordScoreType>>>(emptyList())
    val currentTargetText = MutableStateFlow("")
    val maxRecordedAmplitude = MutableStateFlow(0)
    private val amplitudeSamples = mutableListOf<Int>()
    private var amplitudeJob: Job? = null

    // Offline Speech Recognition State
    private var speechRecognizer: SpeechRecognizer? = null
    val recognizedText = MutableStateFlow("")
    private var speechResultDeferred: kotlinx.coroutines.CompletableDeferred<String>? = null

    // Voice recording timing states
    private var recordStartTime: Long = 0

    // Active learning items
    val activeGrammarLesson = MutableStateFlow<GrammarLesson?>(null)
    val activeConversation = MutableStateFlow<ConversationSet?>(null)
    val activeDailySentence = MutableStateFlow<DailySentence?>(null)
    val activeParagraph = MutableStateFlow<ParagraphSet?>(null)
    val activeTongueTwister = MutableStateFlow<TongueTwister?>(null)

    // Roleplay indices
    val activeConversationIndex = MutableStateFlow(0)
    val userConversationRole = MutableStateFlow("B") // User is B, TTS is A

    // For tracking general events
    val showRewardOverlay = MutableStateFlow<Int?>(null) // Contains XP amount if showing

    // Review Session States
    val isReviewSession = MutableStateFlow(false)
    val reviewSentencesList = MutableStateFlow<List<DailySentence>>(emptyList())
    val currentReviewIndex = MutableStateFlow(0)

    fun startReviewSession(targetSentence: DailySentence? = null) {
        viewModelScope.launch {
            val allSentences = repository.dailySentences.firstOrNull() ?: dailySentences.value
            val currentTime = System.currentTimeMillis()

            val dueOrWeak = allSentences.filter { sentence ->
                // Sentences scheduled for spaced repetition review or with low accuracy
                (sentence.reviewScheduledTime in 1..currentTime) || 
                (sentence.timesPracticed > 0 && sentence.lastAccuracy in 1..84)
            }.sortedWith(
                compareBy<DailySentence> { it.lastAccuracy }
                    .thenBy { it.reviewScheduledTime }
            )

            val sessionList = if (targetSentence != null) {
                val remaining = dueOrWeak.filter { it.id != targetSentence.id }
                listOf(targetSentence) + remaining.take(4)
            } else if (dueOrWeak.isNotEmpty()) {
                dueOrWeak.take(5)
            } else {
                val practiced = allSentences.filter { it.timesPracticed > 0 }
                if (practiced.isNotEmpty()) {
                    practiced.sortedBy { it.lastAccuracy }.take(5)
                } else {
                    allSentences.take(5)
                }
            }

            if (sessionList.isNotEmpty()) {
                isReviewSession.value = true
                reviewSentencesList.value = sessionList
                currentReviewIndex.value = 0
                activeDailySentence.value = sessionList.first()
                lastScore.value = null
                scoredWords.value = emptyList()
                recognizedText.value = ""
                navigateTo(Screen.DailySentences)
            }
        }
    }

    fun endReviewSession() {
        isReviewSession.value = false
        reviewSentencesList.value = emptyList()
        currentReviewIndex.value = 0
        activeDailySentence.value = null
        lastScore.value = null
        scoredWords.value = emptyList()
        recognizedText.value = ""
    }

    // Struggled sounds & phrases dismissal states
    val dismissedWeakItemKeys = MutableStateFlow<Set<String>>(emptySet())
    val isStruggledSectionDismissed = MutableStateFlow(false)

    fun dismissWeakItem(key: String) {
        dismissedWeakItemKeys.value = dismissedWeakItemKeys.value + key
    }

    fun dismissStruggledSection() {
        isStruggledSectionDismissed.value = true
    }

    fun restoreStruggledSection() {
        isStruggledSectionDismissed.value = false
        dismissedWeakItemKeys.value = emptySet()
    }

    init {
        // Initialize Database Content on startup
        viewModelScope.launch {
            repository.checkAndPrepopulate(getApplication())
        }

        // Initialize Text To Speech
        tts = TextToSpeech(application, this)

        // Initialize Speech Recognizer
        initSpeechRecognizer()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            updateTtsSettings()
            ttsReady.value = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isTtsSpeaking.value = true
                }
                override fun onDone(utteranceId: String?) {
                    isTtsSpeaking.value = false
                    currentTtsText.value = null
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    isTtsSpeaking.value = false
                    currentTtsText.value = null
                }
            })
        } else {
            Log.e("EnglishViewModel", "TTS Initialization failed!")
        }
    }

    private fun initSpeechRecognizer() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                val available = SpeechRecognizer.isRecognitionAvailable(getApplication())
                Log.d("SPEECH_DEBUG", "initSpeechRecognizer: SpeechRecognizer.isRecognitionAvailable = $available")
                if (available) {
                    try {
                        speechRecognizer?.destroy()
                    } catch (e: Exception) {
                        // ignore
                    }
                    if (Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(getApplication())) {
                        Log.d("SPEECH_DEBUG", "initSpeechRecognizer: Creating On-Device SpeechRecognizer")
                        speechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(getApplication())
                    } else {
                        Log.d("SPEECH_DEBUG", "initSpeechRecognizer: Creating System SpeechRecognizer")
                        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication())
                    }
                    Log.d("SPEECH_DEBUG", "initSpeechRecognizer: SpeechRecognizer created successfully: $speechRecognizer")
                    speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            Log.d("SPEECH_DEBUG", "RecognitionListener: onReadyForSpeech")
                        }
                        override fun onBeginningOfSpeech() {
                            Log.d("SPEECH_DEBUG", "RecognitionListener: onBeginningOfSpeech")
                        }
                        override fun onRmsChanged(rmsdB: Float) {
                            if (rmsdB > 0.5f) {
                                val scaledAmp = (rmsdB * 1200).toInt().coerceIn(100, 32767)
                                synchronized(amplitudeSamples) {
                                    amplitudeSamples.add(scaledAmp)
                                }
                                if (scaledAmp > maxRecordedAmplitude.value) {
                                    maxRecordedAmplitude.value = scaledAmp
                                }
                                Log.d("SPEECH_DEBUG", "RecognitionListener: onRmsChanged audio level = $rmsdB (amp = $scaledAmp)")
                            }
                        }
                        override fun onBufferReceived(buffer: ByteArray?) {
                            if (buffer != null && buffer.isNotEmpty()) {
                                try {
                                    recordedAudioStream.write(buffer)
                                    Log.d("SPEECH_DEBUG", "RecognitionListener: onBufferReceived size = ${buffer.size} (total = ${recordedAudioStream.size()})")
                                } catch (e: Exception) {
                                    Log.e("SPEECH_DEBUG", "onBufferReceived error", e)
                                }
                            }
                        }
                        override fun onEndOfSpeech() {
                            Log.d("SPEECH_DEBUG", "RecognitionListener: onEndOfSpeech")
                        }
                        override fun onError(error: Int) {
                            val errorName = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "ERROR_AUDIO (3)"
                                SpeechRecognizer.ERROR_CLIENT -> "ERROR_CLIENT (5)"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "ERROR_INSUFFICIENT_PERMISSIONS (9)"
                                SpeechRecognizer.ERROR_NETWORK -> "ERROR_NETWORK (2)"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ERROR_NETWORK_TIMEOUT (1)"
                                SpeechRecognizer.ERROR_NO_MATCH -> "ERROR_NO_MATCH (7)"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "ERROR_RECOGNIZER_BUSY (8)"
                                SpeechRecognizer.ERROR_SERVER -> "ERROR_SERVER (4)"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "ERROR_SPEECH_TIMEOUT (6)"
                                else -> "UNKNOWN_ERROR ($error)"
                            }
                            Log.e("SPEECH_DEBUG", "RecognitionListener: onError: $errorName. Current recognizedText: '${recognizedText.value}'")
                            if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) {
                                try {
                                    speechRecognizer?.cancel()
                                    speechRecognizer?.destroy()
                                } catch (e: Exception) {}
                                speechRecognizer = null
                            }
                            speechResultDeferred?.complete(recognizedText.value)
                        }
                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            Log.d("SPEECH_DEBUG", "RecognitionListener: onResults matches = $matches")
                            if (!matches.isNullOrEmpty() && matches[0].isNotBlank()) {
                                recognizedText.value = matches[0]
                                Log.d("SPEECH_DEBUG", "RecognitionListener: final recognizedText set to '${matches[0]}'")
                            } else {
                                Log.d("SPEECH_DEBUG", "RecognitionListener: onResults matches was empty/null. Preserving existing recognizedText = '${recognizedText.value}'")
                            }

                            try {
                                if (results != null) {
                                    for (key in results.keySet()) {
                                        Log.d("SPEECH_DEBUG", "onResults Bundle key: $key = ${results.get(key)}")
                                    }
                                    @Suppress("DEPRECATION")
                                    val audioUri = results.getParcelable<android.net.Uri>("android.speech.extra.AUDIO_SOURCE")
                                        ?: results.getParcelable<android.net.Uri>("android.speech.extra.AUDIO")
                                    val audioBytes = results.getByteArray("android.speech.extra.AUDIO_DATA")
                                        ?: results.getByteArray("android.speech.extra.AUDIO")

                                    Log.d("SPEECH_DEBUG", "RecognitionListener: onResults audioUri=$audioUri, audioBytes=${audioBytes?.size}, recordedAudioStream=${recordedAudioStream.size()}")

                                    if (recordedAudioStream.size() > 0) {
                                        voiceRecorder.saveRawPcmToWav(recordedAudioStream.toByteArray())
                                    } else if (audioBytes != null && audioBytes.isNotEmpty()) {
                                        voiceRecorder.saveRawPcmToWav(audioBytes)
                                    } else if (audioUri != null) {
                                        voiceRecorder.copyUriToWav(getApplication(), audioUri)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("SPEECH_DEBUG", "Error extracting audio from SpeechRecognizer results", e)
                            }

                            speechResultDeferred?.complete(recognizedText.value)
                        }
                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            Log.d("SPEECH_DEBUG", "RecognitionListener: onPartialResults matches = $matches")
                            if (!matches.isNullOrEmpty() && matches[0].isNotBlank()) {
                                val newText = matches[0]
                                if (newText.length >= recognizedText.value.length || recognizedText.value.isBlank()) {
                                    recognizedText.value = newText
                                    Log.d("SPEECH_DEBUG", "RecognitionListener: partial recognizedText set to '$newText'")
                                }
                            }
                        }
                        override fun onEvent(eventType: Int, params: Bundle?) {
                            Log.d("SPEECH_DEBUG", "RecognitionListener: onEvent type = $eventType")
                        }
                    })
                } else {
                    Log.w("SPEECH_DEBUG", "initSpeechRecognizer: SpeechRecognizer NOT available on this device!")
                }
            } catch (e: Exception) {
                Log.e("SPEECH_DEBUG", "initSpeechRecognizer: Failed to initialize SpeechRecognizer", e)
            }
        }
    }

    private fun startListeningOffline() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                Log.d("SPEECH_DEBUG", "startListeningOffline: starting...")
                recognizedText.value = ""
                speechResultDeferred = kotlinx.coroutines.CompletableDeferred()
                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                    delay(250)
                }
                val langTag = Locale.US.toLanguageTag()
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication<Application>().packageName)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra("android.speech.extra.GET_AUDIO", true)
                    putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR")
                }
                speechRecognizer?.startListening(intent)
                Log.d("SPEECH_DEBUG", "startListeningOffline: startListening intent sent (langTag: $langTag)")
            } catch (e: Exception) {
                Log.e("SPEECH_DEBUG", "startListeningOffline: Error starting SpeechRecognizer", e)
                speechResultDeferred?.complete("")
            }
        }
    }

    private suspend fun transcribePcmChunks(pcmFile: File): String {
        val totalBytes = pcmFile.length()
        val bytesPerSec = 16000 * 2 // 32,000 bytes/sec
        val chunkSize = 15 * bytesPerSec // 480,000 bytes (~15 seconds per chunk)

        if (totalBytes <= chunkSize * 4 / 3) {
            // Short audio (<=20 seconds): Single pass
            return transcribeSinglePcmFile(pcmFile)
        }

        // Long audio (>20 seconds / up to 2 minutes): Split into 15-second chunks
        Log.d("SPEECH_DEBUG", "transcribePcmChunks: Splitting ${totalBytes} bytes into ~15s chunks")
        val pcmBytes = pcmFile.readBytes()
        val fullTextBuilder = StringBuilder()

        var offset = 0
        var chunkIndex = 0
        while (offset < pcmBytes.size) {
            val length = Math.min(chunkSize, pcmBytes.size - offset)
            val chunkBytes = pcmBytes.copyOfRange(offset, offset + length)
            val tempChunkFile = File(getApplication<Application>().cacheDir, "temp_chunk_$chunkIndex.raw")
            tempChunkFile.writeBytes(chunkBytes)

            Log.d("SPEECH_DEBUG", "transcribePcmChunks: Transcribing chunk #$chunkIndex (${tempChunkFile.length()} bytes)")
            val chunkText = transcribeSinglePcmFile(tempChunkFile)
            try { tempChunkFile.delete() } catch (e: Exception) {}

            if (chunkText.isNotBlank()) {
                if (fullTextBuilder.isNotEmpty()) fullTextBuilder.append(" ")
                fullTextBuilder.append(chunkText)
            }

            offset += length
            chunkIndex++
        }

        val result = fullTextBuilder.toString().trim()
        Log.d("SPEECH_DEBUG", "transcribePcmChunks: Final concatenated recognized text (${result.length} chars) = '$result'")
        return result
    }

    private suspend fun transcribeSinglePcmFile(pcmFile: File): String {
        val deferred = kotlinx.coroutines.CompletableDeferred<String>()
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                try { speechRecognizer?.cancel() } catch (e: Exception) {}
                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                    delay(150)
                }
                speechResultDeferred = deferred
                recognizedText.value = ""

                val langTag = Locale.US.toLanguageTag()
                val pfd = ParcelFileDescriptor.open(pcmFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication<Application>().packageName)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra("android.speech.extra.AUDIO_SOURCE", pfd)
                    putExtra("android.speech.extra.AUDIO_SOURCE_ENCODING", 2)
                    putExtra("android.speech.extra.AUDIO_SOURCE_SAMPLING_RATE", 16000)
                    putExtra("android.speech.extra.AUDIO_SOURCE_CHANNELS", 1)
                }
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e("SPEECH_DEBUG", "transcribeSinglePcmFile error", e)
                deferred.complete("")
            }
        }

        return try {
            kotlinx.coroutines.withTimeout(15000L) {
                deferred.await()
            }
        } catch (e: Exception) {
            Log.e("SPEECH_DEBUG", "transcribeSinglePcmFile timeout", e)
            ""
        }
    }



    private fun stopListeningOffline() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                Log.d("SPEECH_DEBUG", "stopListeningOffline: calling stopListening()...")
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e("SPEECH_DEBUG", "stopListeningOffline: Error stopping SpeechRecognizer: ${e.message}")
                speechResultDeferred?.complete(recognizedText.value)
            }
        }
    }

    private fun alignAndScore(targetWords: List<String>, recognizedWords: List<String>): Pair<Int, List<Pair<String, WordScoreType>>> {
        if (recognizedWords.isEmpty()) {
            val analysis = targetWords.map { Pair(it, WordScoreType.Incorrect) }
            return Pair(0, analysis)
        }

        val cleanRecognizedList = recognizedWords.map { it.lowercase(Locale.getDefault()).trim() }
        val usedRecognizedIndices = mutableSetOf<Int>()

        val analysis = mutableListOf<Pair<String, WordScoreType>>()
        var correctCount = 0
        var hesitantCount = 0
        var lastMatchedIdx = -1

        for (targetIdx in targetWords.indices) {
            val targetWord = targetWords[targetIdx]
            val cleanTarget = targetWord.lowercase(Locale.getDefault()).trim()

            // Calculate expected relative position in recognizedWords
            val expectedPos = if (targetWords.isNotEmpty()) {
                ((targetIdx.toDouble() / targetWords.size) * cleanRecognizedList.size).toInt()
            } else 0

            // Define sliding search window around expected position and last matched index
            val searchStart = (if (lastMatchedIdx >= 0) Math.min(expectedPos - 12, lastMatchedIdx + 1) else expectedPos - 12)
                .coerceIn(0, cleanRecognizedList.size - 1)
            val searchEnd = (Math.max(expectedPos + 18, searchStart + 25))
                .coerceIn(searchStart, cleanRecognizedList.size - 1)

            var matchedIdx = -1
            var matchType = WordScoreType.Incorrect

            // 1. Exact match in sliding window
            for (i in searchStart..searchEnd) {
                if (i !in usedRecognizedIndices) {
                    val cleanRec = cleanRecognizedList[i]
                    if (cleanTarget == cleanRec) {
                        matchedIdx = i
                        matchType = WordScoreType.Correct
                        break
                    }
                }
            }

            // 2. Near match (Levenshtein distance <= 1 or singular/plural stem) in sliding window
            if (matchedIdx == -1) {
                for (i in searchStart..searchEnd) {
                    if (i !in usedRecognizedIndices) {
                        val cleanRec = cleanRecognizedList[i]
                        val dist = getLevenshteinDistance(cleanTarget, cleanRec)
                        if (dist <= 1 || (cleanTarget.length > 3 && cleanRec.length > 3 && (cleanTarget.startsWith(cleanRec) || cleanRec.startsWith(cleanTarget)))) {
                            matchedIdx = i
                            matchType = WordScoreType.Hesitant
                            break
                        }
                    }
                }
            }

            // 3. Fallback search in entire recognized list if not found in local window
            if (matchedIdx == -1) {
                val fallbackStart = (lastMatchedIdx + 1).coerceIn(0, cleanRecognizedList.size - 1)
                for (i in fallbackStart until cleanRecognizedList.size) {
                    if (i !in usedRecognizedIndices) {
                        val cleanRec = cleanRecognizedList[i]
                        if (cleanTarget == cleanRec) {
                            matchedIdx = i
                            matchType = WordScoreType.Correct
                            break
                        }
                    }
                }
            }

            if (matchedIdx != -1) {
                usedRecognizedIndices.add(matchedIdx)
                lastMatchedIdx = Math.max(lastMatchedIdx, matchedIdx)
                if (matchType == WordScoreType.Correct) {
                    correctCount++
                } else {
                    hesitantCount++
                }
                analysis.add(Pair(targetWord, matchType))
            } else {
                analysis.add(Pair(targetWord, WordScoreType.Incorrect))
            }
        }

        val totalWords = targetWords.size
        if (totalWords == 0) return Pair(0, emptyList())

        val score = (((correctCount * 1.0) + (hesitantCount * 0.5)) / totalWords * 100).toInt()
        return Pair(score.coerceIn(0, 100), analysis)
    }

    private fun getLevenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[len1][len2]
    }

    fun updateTtsSettings() {
        val locale = when (selectedAccent.value) {
            "UK" -> Locale.UK
            "IN" -> Locale("en", "IN")
            "AU" -> Locale("en", "AU")
            else -> Locale.US
        }
        tts?.language = locale
        tts?.setSpeechRate(selectedSpeed.value)
    }

    fun setAccent(accent: String) {
        selectedAccent.value = accent
        updateTtsSettings()
    }

    fun setPlaybackSpeed(speed: Float) {
        selectedSpeed.value = speed
        updateTtsSettings()
    }

    fun extractSpokenExample(text: String): String {
        return text.lines()
            .map { line ->
                var cleaned = line.trim()
                if (cleaned.startsWith("•") || cleaned.startsWith("-") || cleaned.startsWith("*")) {
                    cleaned = cleaned.replaceFirst("^[•\\-*]\\s*".toRegex(), "").trim()
                    if (cleaned.contains(":")) {
                        val afterColon = cleaned.substringAfter(":").trim()
                        if (afterColon.isNotBlank()) {
                            cleaned = afterColon
                        }
                    }
                } else if (cleaned.contains(":")) {
                    val prefix = cleaned.substringBefore(":").trim()
                    // If it's a topic header (short label without sentence terminators)
                    if (prefix.length <= 60 && !prefix.contains(".") && !prefix.contains("?") && !prefix.contains("!")) {
                        val afterColon = cleaned.substringAfter(":").trim()
                        if (afterColon.isNotBlank()) {
                            cleaned = afterColon
                        }
                    }
                }
                cleaned
            }
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    fun speak(text: String, forceStart: Boolean = false) {
        if (!ttsReady.value) {
            Log.e("EnglishViewModel", "TTS is not ready yet.")
            return
        }

        val spokenText = extractSpokenExample(text)
        if (spokenText.isBlank()) return

        val currentlySpeaking = isTtsSpeaking.value || (tts?.isSpeaking == true)
        if (currentlySpeaking && !forceStart) {
            stopTts()
        } else {
            stopTts()
            stopRecordedVoicePlayback()
            currentTtsText.value = spokenText
            isTtsSpeaking.value = true
            val params = Bundle()
            val utteranceId = "easy_speaking_tts_${System.currentTimeMillis()}"
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            tts?.speak(spokenText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }

    fun stopTts() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("EnglishViewModel", "Error stopping TTS", e)
        }
        isTtsSpeaking.value = false
        currentTtsText.value = null
    }

    fun navigateTo(screen: Screen) {
        if (screen != Screen.DailySentences) {
            isReviewSession.value = false
            reviewSentencesList.value = emptyList()
            currentReviewIndex.value = 0
        }
        if (screen == Screen.Home) {
            screenStack.value = listOf(Screen.Home)
        } else {
            val currentList = screenStack.value.toMutableList()
            val existingIndex = currentList.indexOf(screen)
            if (existingIndex >= 0) {
                screenStack.value = currentList.subList(0, existingIndex + 1)
            } else {
                currentList.add(screen)
                screenStack.value = currentList
            }
        }
        // Reset states
        stopTts()
        stopRecordedVoicePlayback()
        lastScore.value = null
        scoredWords.value = emptyList()
        isRecording.value = false
        isPlayingBack.value = false
        voiceRecorder.stopRecording()
        voiceRecorder.stopPlayback()
    }

    fun goBack(): Boolean {
        // 1. Check if review session is active
        if (isReviewSession.value) {
            endReviewSession()
            if (screenStack.value.lastOrNull() == Screen.DailySentences) {
                screenStack.value = screenStack.value.dropLast(1)
            }
            return true
        }

        // 2. Check if an active practice item is open
        if (activeGrammarLesson.value != null) {
            activeGrammarLesson.value = null
            return true
        }
        if (activeConversation.value != null) {
            activeConversation.value = null
            return true
        }
        if (activeDailySentence.value != null) {
            activeDailySentence.value = null
            return true
        }
        if (activeParagraph.value != null) {
            activeParagraph.value = null
            return true
        }
        if (activeTongueTwister.value != null) {
            activeTongueTwister.value = null
            return true
        }

        // 3. Pop screen stack if deeper than root
        val currentList = screenStack.value
        if (currentList.size > 1) {
            screenStack.value = currentList.dropLast(1)
            return true
        }

        return false
    }

    // --- SPEAKING PRACTICE LOOP ---
    fun startRecording(targetText: String) {
        stopTts()
        stopRecordedVoicePlayback()
        viewModelScope.launch {
            Log.d("SPEECH_DEBUG", "=== START RECORDING PRACTICE === targetText: '$targetText'")
            lastScore.value = null
            scoredWords.value = emptyList()
            currentTargetText.value = targetText
            maxRecordedAmplitude.value = 0
            synchronized(amplitudeSamples) {
                amplitudeSamples.clear()
            }
            recordStartTime = System.currentTimeMillis()
            isRecording.value = true

            // Clear previous recording so old audio is never replayed
            voiceRecorder.clearRecordedFile()

            // Start VoiceRecorder as sole microphone recorder while user speaks
            val started = voiceRecorder.startRecording()
            Log.d("SPEECH_DEBUG", "startRecording: VoiceRecorder started = $started")

            if (started) {
                amplitudeJob?.cancel()
                amplitudeJob = viewModelScope.launch {
                    while (isRecording.value) {
                        delay(100)
                        val amp = voiceRecorder.getMaxAmplitude()
                        synchronized(amplitudeSamples) {
                            amplitudeSamples.add(amp)
                        }
                        if (amp > maxRecordedAmplitude.value) {
                            maxRecordedAmplitude.value = amp
                        }
                    }
                }
            }
        }
    }

    fun stopRecording(targetText: String, itemType: String, itemId: Int) {
        viewModelScope.launch {
            Log.d("SPEECH_DEBUG", "=== STOP RECORDING PRACTICE === targetText: '$targetText', itemType: $itemType")
            isRecording.value = false
            amplitudeJob?.cancel()

            // Stop VoiceRecorder first so microphone is released and PCM file is finalized
            voiceRecorder.stopRecording()

            val recordDurationMs = System.currentTimeMillis() - recordStartTime
            val pcmFile = voiceRecorder.getPcmFile()
            Log.d("SPEECH_DEBUG", "stopRecording: Record duration = ${recordDurationMs}ms. PCM file = ${pcmFile?.absolutePath} (${pcmFile?.length()} bytes)")

            // Set background processing state
            isProcessingSpeech.value = true
            recognizedText.value = ""

            // Transcribe speech using chunked PCM audio processing for full 1-2 minute paragraph coverage
            try {
                if (pcmFile != null && pcmFile.length() > 0) {
                    val fullText = transcribePcmChunks(pcmFile)
                    if (fullText.isNotBlank()) {
                        recognizedText.value = fullText
                    }
                } else {
                    startListeningOffline()
                }
            } catch (e: Exception) {
                Log.e("SPEECH_DEBUG", "stopRecording: Error during chunked PCM transcription", e)
            } finally {
                isProcessingSpeech.value = false
            }

            Log.d("SPEECH_DEBUG", "stopRecording: Transcription completed! Final recognizedText = '${recognizedText.value}'")

            val cleanTarget = targetText.replace(Regex("[^a-zA-Z\\s]"), "")
            val words = cleanTarget.split(" ").filter { it.isNotEmpty() }

            val cleanRecognized = recognizedText.value.replace(Regex("[^a-zA-Z\\s]"), "").trim()
            val recognizedWords = cleanRecognized.split(" ").filter { it.isNotEmpty() }

            Log.d("SPEECH_DEBUG", "stopRecording: Evaluation targetWords=$words, recognizedWords=$recognizedWords")

            val recordedFile = voiceRecorder.getRecordedFile()
            val fileSize = recordedFile?.length() ?: 0L
            val currentSamples = synchronized(amplitudeSamples) { amplitudeSamples.toList() }

            val (score, analysis) = evaluateVoiceInput(
                targetWords = words,
                recognizedWords = recognizedWords,
                recordedFileSize = fileSize,
                recordDurationMs = recordDurationMs,
                amplitudeSamples = currentSamples,
                maxAmplitude = maxRecordedAmplitude.value
            )

            val finalScore = score.coerceIn(0, 100)
            Log.d("SPEECH_DEBUG", "stopRecording: Evaluation finalScore=$finalScore, analysis=$analysis")
            lastScore.value = finalScore
            scoredWords.value = analysis

            // Track user XP progression (Max once per lesson/exercise per calendar day)
            // Conversations only award XP upon completing all sentences in a level, not per sentence
            val xpAwarded = if (finalScore > 0 && itemType != "conversation") {
                when {
                    finalScore >= 90 -> 15
                    finalScore >= 80 -> 10
                    else -> 5
                }
            } else {
                0
            }

            if (xpAwarded > 0) {
                val itemKey = if (itemId > 0) "${itemType}_${itemId}" else "${itemType}_${targetText.trim().hashCode()}"
                val xpClaimed = repository.awardLessonXp(itemKey, xpAwarded)
                if (xpClaimed) {
                    showRewardOverlay.value = xpAwarded
                } else {
                    showRewardOverlay.value = null
                }
            } else {
                showRewardOverlay.value = null
            }

            // Persistent progress update based on module type
            if (finalScore > 0) {
                when (itemType) {
                    "grammar" -> {
                        activeGrammarLesson.value?.let { lesson ->
                            repository.updateGrammarLesson(lesson.copy(isCompleted = true))
                        }
                    }
                    "daily_sentence" -> {
                        activeDailySentence.value?.let { sentence ->
                            val isCorrect = finalScore >= 85
                            repository.updateDailySentence(sentence.copy(isCompleted = true))
                            repository.scheduleSpacedRepetition(sentence.id, isCorrect, finalScore)
                        }
                    }
                    "paragraph" -> {
                        activeParagraph.value?.let { para ->
                            val currentMax = if (finalScore > para.maxAccuracy) finalScore else para.maxAccuracy
                            val isPassed = para.isCompleted || (finalScore >= 60)
                            repository.updateParagraph(para.copy(isCompleted = isPassed, maxAccuracy = currentMax))
                        }
                    }
                    "tongue_twister" -> {
                        activeTongueTwister.value?.let { twister ->
                            val currentMax = if (finalScore > twister.maxAccuracy) finalScore else twister.maxAccuracy
                            repository.updateTongueTwister(twister.copy(
                                isCompleted = true,
                                maxAccuracy = currentMax,
                                practiceCount = twister.practiceCount + 1
                            ))
                        }
                    }
                    "conversation" -> {
                        // Advance conversation roleplay line upon scoring
                    }
                }
            }
        }
    }

    private fun evaluateVoiceInput(
        targetWords: List<String>,
        recognizedWords: List<String>,
        recordedFileSize: Long,
        recordDurationMs: Long,
        amplitudeSamples: List<Int>,
        maxAmplitude: Int
    ): Pair<Int, List<Pair<String, WordScoreType>>> {
        if (targetWords.isEmpty() || recognizedWords.isEmpty()) {
            // If no speech was spoken/recognized, score is strictly 0 and all words are marked Incorrect (Red)
            val silentAnalysis = targetWords.map { Pair(it, WordScoreType.Incorrect) }
            return Pair(0, silentAnalysis)
        }

        // Compute pronunciation score strictly based on recognized spoken words vs target words
        return alignAndScore(targetWords, recognizedWords)
    }

    fun playRecordedVoice() {
        if (isPlayingBack.value || voiceRecorder.isPlaying) {
            stopRecordedVoicePlayback()
            return
        }

        stopTts()

        val recordedFile = voiceRecorder.getRecordedFile()
        Log.d("SPEECH_DEBUG", "playRecordedVoice: recordedFile = ${recordedFile?.absolutePath}, length = ${recordedFile?.length()}")

        if (recordedFile != null && recordedFile.length() > 44) {
            isPlayingBack.value = true
            voiceRecorder.startPlayback {
                isPlayingBack.value = false
            }
        } else if (currentTargetText.value.isNotEmpty()) {
            speak(currentTargetText.value)
        }
    }

    fun stopRecordedVoicePlayback() {
        voiceRecorder.stopPlayback()
        isPlayingBack.value = false
    }

    // --- CONVERSATION ROLE PLAY ENGINE ---
    fun selectConversation(conversation: ConversationSet) {
        activeConversation.value = conversation
        activeConversationIndex.value = 0
        navigateTo(Screen.Conversation)

        // If the starting role is Alex (A) and user is Sarah (B), speak the first line automatically!
        triggerTtsForCurrentDialogue()
    }

    fun toggleRole() {
        userConversationRole.value = if (userConversationRole.value == "A") "B" else "A"
        activeConversationIndex.value = 0
        triggerTtsForCurrentDialogue()
    }

    fun advanceDialogue(activeJson: String? = null) {
        val conv = activeConversation.value ?: return
        val jsonToUse = activeJson ?: conv.dialogueJson
        val lines = parseDialogueJson(jsonToUse)
        val totalLines = lines.size.coerceAtLeast(1)
        if (activeConversationIndex.value < totalLines - 1) {
            activeConversationIndex.value++
            triggerTtsForCurrentDialogue(jsonToUse)
        }
    }

    fun completeConversationLevel(convId: Int, level: String, onCompleted: (Boolean, Int) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val conv = conversations.value.find { it.id == convId } ?: activeConversation.value ?: return@launch
            val isBasic = level.equals("Basic", ignoreCase = true)
            val isInter = level.equals("Intermediate", ignoreCase = true)
            val isAdv = level.equals("Advanced", ignoreCase = true)

            val newBasic = if (isBasic) true else conv.isBasicCompleted
            val newInter = if (isInter) true else conv.isIntermediateCompleted
            val newAdv = if (isAdv) true else conv.isAdvancedCompleted
            val allCompleted = newBasic && newInter && newAdv

            val updatedConv = conv.copy(
                isBasicCompleted = newBasic,
                isIntermediateCompleted = newInter,
                isAdvancedCompleted = newAdv,
                isCompleted = allCompleted
            )
            repository.updateConversation(updatedConv)
            activeConversation.value = updatedConv

            val xpAmount = when {
                isBasic -> 20
                isInter -> 25
                isAdv -> 35
                else -> 20
            }
            val itemKey = "conversation_${conv.id}_${level.lowercase()}"
            val xpClaimed = repository.awardLessonXp(itemKey, xpAmount)
            if (xpClaimed) {
                showRewardOverlay.value = xpAmount
            }
            onCompleted(allCompleted, xpAmount)
        }
    }

    fun triggerTtsForCurrentDialogue(activeJson: String? = null) {
        val conv = activeConversation.value ?: return
        val jsonToUse = activeJson ?: conv.dialogueJson
        val lines = parseDialogueJson(jsonToUse)
        val currentIndex = activeConversationIndex.value
        if (currentIndex < lines.size) {
            val currentLine = lines[currentIndex]
            val speakerRole = currentLine.role // "A" or "B"
            if (speakerRole != userConversationRole.value) {
                // Speak this line!
                speak(currentLine.text, forceStart = true)
            }
        }
    }

    fun parseDialogueJson(json: String): List<DialogueLine> {
        val lines = mutableListOf<DialogueLine>()
        // Lightweight local parser without pulling in external libraries
        val regex = Regex("""\{\s*"speaker"\s*:\s*"([^"]+)"\s*,\s*"text"\s*:\s*"([^"]+)"\s*,\s*"role"\s*:\s*"([^"]+)"\s*\}""")
        val matches = regex.findAll(json)
        for (match in matches) {
            val speaker = match.groupValues[1]
            val text = match.groupValues[2]
            val role = match.groupValues[3]
            lines.add(DialogueLine(speaker, text, role))
        }
        return lines
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.clearAppOpenLogs()
            repository.updateProgress(UserProgress(id = 1, currentStreak = 0, longestStreak = 0, lastActiveDate = "", totalXP = 0))
            repository.recordAppOpen()
            // Mark all items as uncompleted
            grammarLessons.value.forEach {
                repository.updateGrammarLesson(it.copy(isCompleted = false))
            }
            conversations.value.forEach {
                repository.updateConversation(it.copy(
                    isBasicCompleted = false,
                    isIntermediateCompleted = false,
                    isAdvancedCompleted = false,
                    isCompleted = false
                ))
            }
            dailySentences.value.forEach {
                repository.updateDailySentence(it.copy(isCompleted = false, timesPracticed = 0, lastAccuracy = 0, consecutiveCorrect = 0, reviewScheduledTime = 0))
            }
            paragraphs.value.forEach {
                repository.updateParagraph(it.copy(isCompleted = false, maxAccuracy = 0))
            }
            tongueTwisters.value.forEach {
                repository.updateTongueTwister(it.copy(isCompleted = false, maxAccuracy = 0, practiceCount = 0))
            }
            repository.deleteAllDailyXpClaims()
            restoreStruggledSection()
        }
    }

    fun updateDailyGoalXP(target: Int) {
        val current = userProgress.value ?: return
        viewModelScope.launch {
            repository.updateProgress(current.copy(dailyGoalXP = target))
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // ignore
        }
        speechRecognizer = null
        voiceRecorder.stopRecording()
        voiceRecorder.stopPlayback()
    }
}

data class DialogueLine(
    val speaker: String,
    val text: String,
    val role: String
)
