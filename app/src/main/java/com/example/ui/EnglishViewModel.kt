package com.example.ui

import android.app.Application
import android.content.Intent
import android.os.Bundle
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.*

enum class Screen {
    Home,
    Grammar,
    Conversation,
    DailySentences,
    Paragraph,
    TongueTwister,
    Settings,
}

enum class WordScoreType {
    Correct,
    Hesitant,
    Incorrect,
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
    val isRecording = MutableStateFlow(false)
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
    private var recognitionDeferred: CompletableDeferred<String>? = null
    val isProcessingSpeech = MutableStateFlow(false)

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
        try {
            if (SpeechRecognizer.isRecognitionAvailable(getApplication())) {
                // Use on-device recognizer if API level is 31+ (Android 12) for faster offline support
                speechRecognizer = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(getApplication())
                } else {
                    SpeechRecognizer.createSpeechRecognizer(getApplication())
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d("EnglishViewModel", "onReadyForSpeech")
                    }
                    override fun onBeginningOfSpeech() {
                        Log.d("EnglishViewModel", "onBeginningOfSpeech")
                    }
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        Log.d("EnglishViewModel", "onEndOfSpeech")
                    }
                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No recognition result matched"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                            else -> "Unknown error: $error"
                        }
                        Log.e("EnglishViewModel", "SpeechRecognizer error: $message")
                        recognitionDeferred?.complete("")
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.getOrNull(0) ?: ""
                        recognizedText.value = text
                        Log.d("EnglishViewModel", "SpeechRecognizer final result: $text")
                        recognitionDeferred?.complete(text)
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            recognizedText.value = matches[0]
                            Log.d("EnglishViewModel", "SpeechRecognizer partial result: ${matches[0]}")
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            } else {
                Log.w("EnglishViewModel", "SpeechRecognizer not available on this device")
            }
        } catch (e: Exception) {
            Log.e("EnglishViewModel", "Failed to initialize SpeechRecognizer", e)
        }
    }

    private fun startListeningOffline() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                recognizedText.value = ""
                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                }
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                // Don't explicitly prefer offline as it can fail silently on some OS versions; 
                // the system will prioritize offline models if they are installed anyway.
                speechRecognizer?.cancel()
                speechRecognizer?.startListening(intent)
                Log.d("EnglishViewModel", "SpeechRecognizer started listening")
            } catch (e: Exception) {
                Log.e("EnglishViewModel", "Error starting SpeechRecognizer", e)
            }
        }
    }

    private fun stopListeningOffline() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                // stopListening() allows the service to finish processing currently buffered audio
                speechRecognizer?.stopListening()
                Log.d("EnglishViewModel", "SpeechRecognizer stopListening() called")
            } catch (e: Exception) {
                Log.e("EnglishViewModel", "Error stopping SpeechRecognizer", e)
            }
        }
    }

    private fun alignAndScore(targetWords: List<String>, recognizedWords: List<String>): Pair<Int, List<Pair<String, WordScoreType>>> {
        if (recognizedWords.isEmpty()) {
            val analysis = targetWords.map { Pair(it, WordScoreType.Incorrect) }
            return Pair(0, analysis)
        }

        val analysis = mutableListOf<Pair<String, WordScoreType>>()
        var correctCount = 0
        var hesitantCount = 0
        var lastFoundIndex = -1
        
        for (targetWord in targetWords) {
            val cleanTarget = targetWord.lowercase(Locale.getDefault()).trim()
            if (cleanTarget.isEmpty()) continue
            
            // Search window: look ahead up to 6 words from the last match
            val startIndex = (lastFoundIndex + 1).coerceAtLeast(0)
            val endIndex = (startIndex + 6).coerceAtMost(recognizedWords.size - 1)
            
            var foundIndex = -1
            var bestDistance = 999
            
            // 1. Priority: Exact match in search window
            for (i in startIndex..endIndex) {
                val cleanRec = recognizedWords[i].lowercase(Locale.getDefault()).trim()
                if (cleanTarget == cleanRec) {
                    foundIndex = i
                    bestDistance = 0
                    break
                }
            }
            
            // 2. Secondary: Fuzzy match (Levenshtein distance 1) in window
            if (foundIndex == -1) {
                for (i in startIndex..endIndex) {
                    val cleanRec = recognizedWords[i].lowercase(Locale.getDefault()).trim()
                    val distance = getLevenshteinDistance(cleanTarget, cleanRec)
                    if (distance <= 1) {
                        foundIndex = i
                        bestDistance = distance
                        break
                    }
                }
            }
            
            // 3. Fallback: Search remaining list for exact match if word is long (length > 3)
            if (foundIndex == -1 && cleanTarget.length > 3) {
                for (i in (endIndex + 1) until recognizedWords.size) {
                    val cleanRec = recognizedWords[i].lowercase(Locale.getDefault()).trim()
                    if (cleanTarget == cleanRec) {
                        foundIndex = i
                        bestDistance = 0
                        break
                    }
                }
            }

            if (foundIndex != -1) {
                if (bestDistance == 0) {
                    correctCount++
                    analysis.add(Pair(targetWord, WordScoreType.Correct))
                } else {
                    hesitantCount++
                    analysis.add(Pair(targetWord, WordScoreType.Hesitant))
                }
                lastFoundIndex = foundIndex
            } else {
                analysis.add(Pair(targetWord, WordScoreType.Incorrect))
            }
        }

        val totalWords = targetWords.size
        val rawScore = if (totalWords > 0) {
            ((correctCount.toFloat() + hesitantCount.toFloat() * 0.5f) / totalWords.toFloat() * 100f).toInt()
        } else {
            0
        }

        return Pair(rawScore.coerceIn(0, 100), analysis)
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
        // 1. Check if an active practice item is open
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

        // 2. Pop screen stack if deeper than root
        val currentList = screenStack.value
        if (currentList.size > 1) {
            screenStack.value = currentList.dropLast(1)
            return true
        }

        return false
    }

    // --- SPEAKING PRACTICE LOOP ---
    fun startRecording(targetText: String) {
        if (isRecording.value) return
        stopTts()
        stopRecordedVoicePlayback()
        viewModelScope.launch {
            lastScore.value = null
            scoredWords.value = emptyList()
            recognizedText.value = ""
            isProcessingSpeech.value = false
            recognitionDeferred = CompletableDeferred()
            currentTargetText.value = targetText
            maxRecordedAmplitude.value = 0
            synchronized(amplitudeSamples) {
                amplitudeSamples.clear()
            }
            recordStartTime = System.currentTimeMillis()
            
            // 1. Start audio capture first
            val started = voiceRecorder.startRecording()
            isRecording.value = started

            if (started) {
                // 2. Delay significantly to ensure MediaRecorder has initialized hardware
                delay(600)
                startListeningOffline()
                
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
            } else {
                stopListeningOffline()
            }
        }
    }

    fun stopRecording(targetText: String, itemType: String, itemId: Int) {
        if (!isRecording.value) return
        viewModelScope.launch {
            isRecording.value = false
            isProcessingSpeech.value = true
            amplitudeJob?.cancel()
            
            // Stop recorder to finalize the audio file
            voiceRecorder.stopRecording()
            
            // 3. CRITICAL: Allow the SpeechRecognizer to continue processing buffered audio
            stopListeningOffline()

            // Wait for SpeechRecognizer results with a timeout (e.g. 2.5 seconds)
            val result = withTimeoutOrNull(2500) {
                recognitionDeferred?.await()
            } ?: recognizedText.value // fallback to partial if final never came
            
            recognizedText.value = result
            isProcessingSpeech.value = false

            val recordDurationMs = System.currentTimeMillis() - recordStartTime

            // Final processing delay to let state flows propagate
            delay(300)

            val cleanTarget = targetText.replace(Regex("[^a-zA-Z\\s]"), "")
            val words = cleanTarget.split(" ").filter { it.isNotEmpty() }

            val cleanRecognized = recognizedText.value.replace(Regex("[^a-zA-Z\\s]"), "").trim()
            val recognizedWords = cleanRecognized.split(" ").filter { it.isNotEmpty() }

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
            lastScore.value = finalScore
            scoredWords.value = analysis

            // Track user XP progression (Max once per lesson/exercise per calendar day)
            val xpAwarded = if (finalScore > 0) {
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
                            repository.updateParagraph(para.copy(isCompleted = true, maxAccuracy = currentMax))
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
        if (targetWords.isEmpty()) {
            return Pair(0, emptyList())
        }

        // 1. If SpeechRecognizer returned recognized text, combine text alignment + audio metrics
        if (recognizedWords.isNotEmpty()) {
            val (textScore, wordAnalysis) = alignAndScore(targetWords, recognizedWords)
            
            // Check if audio was actually heard during recognition
            val activeSpeechCount = amplitudeSamples.count { it > 250 }
            
            // If text matches well, prioritize it. 
            // We use a high multiplier for text alignment.
            val finalScore = if (activeSpeechCount > 0) {
                // Boost score if words matched, ensuring 90%+ is possible if alignment is perfect
                val alignmentBonus = when {
                    textScore >= 95 -> 10
                    textScore >= 80 -> 5
                    else -> 0
                }
                (textScore + alignmentBonus).coerceAtMost(100)
            } else {
                textScore
            }
            return Pair(finalScore.coerceIn(0, 100), wordAnalysis)
        }

        // 2. SpeechRecognizer text is empty (e.g. error / offline / mic channel held by recorder).
        // Check if user actually spoke into the microphone!
        val activeSpeechSamples = amplitudeSamples.filter { it >= 350 }
        val maxAmp = if (amplitudeSamples.isNotEmpty()) amplitudeSamples.maxOrNull() ?: maxAmplitude else maxAmplitude

        // If file is empty, duration too short, or mic signal is silent (< 350 amp), NO speech was captured
        if (recordedFileSize < 1200 || recordDurationMs < 350 || maxAmp < 350 || activeSpeechSamples.isEmpty()) {
            Log.d("EnglishViewModel", "No voice sound detected. Silence score = 0.")
            val silentAnalysis = targetWords.map { Pair(it, WordScoreType.Incorrect) }
            return Pair(0, silentAnalysis)
        }

        // 3. FALLBACK: Compute pronunciation score based on audio metrics if recognition failed but sound was detected.
        // We reduce the maximum possible score significantly because we couldn't verify the words spoken.
        val totalWords = targetWords.size
        val expectedDurationMs = totalWords * 400L + 800L

        // Volume & Clarity Score (35%)
        val avgSpeechAmp = activeSpeechSamples.average().toFloat()
        val volumeScore = when {
            avgSpeechAmp >= 4000f -> 95f
            avgSpeechAmp >= 2500f -> 85f
            avgSpeechAmp >= 1200f -> 75f
            else -> 50f
        }

        // Pace & Duration Match Score (35%)
        val actualSpeechDurationMs = (activeSpeechSamples.size * 100L).coerceAtLeast(recordDurationMs)
        val paceRatio = actualSpeechDurationMs.toFloat() / expectedDurationMs.toFloat()
        val paceScore = when {
            paceRatio in 0.75f..1.25f -> 95f
            paceRatio in 0.5f..1.8f -> 75f
            else -> 40f
        }

        // Articulation & Vocal Energy Dynamics Score (30%)
        // This detects if the user is actually speaking (changing energy) or just making noise
        val mean = avgSpeechAmp
        val variance = activeSpeechSamples.map { (it - mean) * (it - mean) }.average()
        val stdDev = Math.sqrt(variance).toFloat()
        val articulationScore = when {
            stdDev >= 1200f -> 95f
            stdDev >= 600f -> 80f
            stdDev >= 300f -> 60f
            else -> 10f // Very low energy variation (likely noise/humming, not speech)
        }

        // Cap fallback score to 75% as we cannot confirm the user actually said the correct words,
        // but high energy and good pace suggest a valid attempt.
        val computedAcousticScore = (volumeScore * 0.35f + paceScore * 0.35f + articulationScore * 0.30f).toInt().coerceIn(10, 75)

        // Per-word timeline segmentation
        val wordAnalysis = mutableListOf<Pair<String, WordScoreType>>()
        val samplesPerWord = (amplitudeSamples.size.toFloat() / totalWords.toFloat()).coerceAtLeast(1f)

        for (i in 0 until totalWords) {
            val word = targetWords[i]
            val startSample = (i * samplesPerWord).toInt().coerceIn(0, amplitudeSamples.size)
            val endSample = ((i + 1) * samplesPerWord).toInt().coerceIn(startSample, amplitudeSamples.size)

            val wordSlice = if (startSample < endSample && startSample < amplitudeSamples.size) {
                amplitudeSamples.subList(startSample, endSample)
            } else {
                emptyList()
            }

            val sliceMax = if (wordSlice.isNotEmpty()) wordSlice.maxOrNull() ?: 0 else maxAmp
            val sliceAvg = if (wordSlice.isNotEmpty()) wordSlice.average() else 0.0

            val wordStatus = when {
                sliceMax >= 1200 || sliceAvg >= 600 -> WordScoreType.Correct
                sliceMax >= 400 || sliceAvg >= 200 -> WordScoreType.Hesitant
                else -> WordScoreType.Incorrect
            }
            wordAnalysis.add(Pair(word, wordStatus))
        }

        Log.d("EnglishViewModel", "Voice input acoustic evaluation score: $computedAcousticScore for $totalWords words")
        return Pair(computedAcousticScore, wordAnalysis)
    }

    fun playRecordedVoice() {
        if (isPlayingBack.value || voiceRecorder.isPlaying) {
            stopRecordedVoicePlayback()
            return
        }

        stopTts()

        if (voiceRecorder.getRecordedFile() != null) {
            isPlayingBack.value = true
            voiceRecorder.startPlayback {
                isPlayingBack.value = false
            }
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
        } else {
            // Dialogue complete!
            viewModelScope.launch {
                repository.updateConversation(conv.copy(isCompleted = true))
                val itemKey = "conversation_${conv.id}"
                val xpClaimed = repository.awardLessonXp(itemKey, 30)
                if (xpClaimed) {
                    showRewardOverlay.value = 30
                } else {
                    showRewardOverlay.value = null
                }
                navigateTo(Screen.Home)
            }
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
                repository.updateConversation(it.copy(isCompleted = false))
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
        voiceRecorder.stopRecording()
        voiceRecorder.stopPlayback()
    }
}

data class DialogueLine(
    val speaker: String,
    val text: String,
    val role: String,
)
