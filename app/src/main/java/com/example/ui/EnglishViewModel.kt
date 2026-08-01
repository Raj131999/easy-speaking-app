package com.example.ui

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
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
    val currentScreen = MutableStateFlow(Screen.Home)

    // Curriculum and User metrics from Room
    val userProgress = repository.userProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
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
    val selectedAccent = MutableStateFlow("US") // "US", "UK", "IN", "AU"
    val selectedSpeed = MutableStateFlow(1.0f)   // 0.5f, 0.75f, 1.0f, 1.25f, 1.5f

    // Voice recording and play states
    private val voiceRecorder = VoiceRecorder(application)
    val isRecording = MutableStateFlow(false)
    val isPlayingBack = MutableStateFlow(false)
    val lastScore = MutableStateFlow<Int?>(null)
    val scoredWords = MutableStateFlow<List<Pair<String, WordScoreType>>>(emptyList())
    val currentTargetText = MutableStateFlow("")
    val useSimulatedMic = MutableStateFlow(false) // Default to physical mic so users hear their real voice
    val maxRecordedAmplitude = MutableStateFlow(0)
    private var amplitudeJob: Job? = null

    // Offline Speech Recognition State
    private var speechRecognizer: SpeechRecognizer? = null
    val recognizedText = MutableStateFlow("")

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
        } else {
            Log.e("EnglishViewModel", "TTS Initialization failed!")
        }
    }

    private fun initSpeechRecognizer() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(getApplication())) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication())
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
                            Log.e("EnglishViewModel", "SpeechRecognizer error: $error")
                        }
                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                recognizedText.value = matches[0]
                                Log.d("EnglishViewModel", "SpeechRecognizer final result: ${matches[0]}")
                            }
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
    }

    private fun startListeningOffline() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                recognizedText.value = ""
                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                    delay(200)
                }
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
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
                speechRecognizer?.stopListening()
                Log.d("EnglishViewModel", "SpeechRecognizer stopped listening")
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
            val startIndex = (lastFoundIndex + 1).coerceAtLeast(0)
            val endIndex = (startIndex + 3).coerceAtMost(recognizedWords.size - 1)
            
            var foundIndex = -1
            for (i in startIndex..endIndex) {
                val cleanRec = recognizedWords[i].lowercase(Locale.getDefault()).trim()
                if (cleanTarget == cleanRec) {
                    foundIndex = i
                    break
                }
            }
            
            if (foundIndex == -1) {
                for (i in (lastFoundIndex + 1).coerceAtLeast(0) until recognizedWords.size) {
                    val cleanRec = recognizedWords[i].lowercase(Locale.getDefault()).trim()
                    if (cleanTarget == cleanRec) {
                        foundIndex = i
                        break
                    }
                }
            }

            if (foundIndex != -1) {
                correctCount++
                analysis.add(Pair(targetWord, WordScoreType.Correct))
                lastFoundIndex = foundIndex
            } else {
                var partialIndex = -1
                for (i in (lastFoundIndex + 1).coerceAtLeast(0) until (lastFoundIndex + 4).coerceAtMost(recognizedWords.size)) {
                    val cleanRec = recognizedWords[i].lowercase(Locale.getDefault()).trim()
                    if (getLevenshteinDistance(cleanTarget, cleanRec) <= 2) {
                        partialIndex = i
                        break
                    }
                }
                
                if (partialIndex != -1) {
                    hesitantCount++
                    analysis.add(Pair(targetWord, WordScoreType.Hesitant))
                    lastFoundIndex = partialIndex
                } else {
                    analysis.add(Pair(targetWord, WordScoreType.Incorrect))
                }
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

    fun speak(text: String) {
        if (ttsReady.value) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "easy_speaking_tts")
        } else {
            Log.e("EnglishViewModel", "TTS is not ready yet.")
        }
    }

    fun navigateTo(screen: Screen) {
        currentScreen.value = screen
        // Reset states
        lastScore.value = null
        scoredWords.value = emptyList()
        isRecording.value = false
        isPlayingBack.value = false
        voiceRecorder.stopRecording()
        voiceRecorder.stopPlayback()
    }

    // --- SPEAKING PRACTICE LOOP ---
    fun startRecording(targetText: String) {
        viewModelScope.launch {
            lastScore.value = null
            scoredWords.value = emptyList()
            currentTargetText.value = targetText
            maxRecordedAmplitude.value = 0
            recordStartTime = System.currentTimeMillis()
            val started = voiceRecorder.startRecording()
            isRecording.value = started

            if (started) {
                startListeningOffline()
                amplitudeJob?.cancel()
                amplitudeJob = viewModelScope.launch {
                    while (isRecording.value) {
                        delay(100)
                        val amp = voiceRecorder.getMaxAmplitude()
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
            isRecording.value = false
            amplitudeJob?.cancel()
            stopListeningOffline()
            voiceRecorder.stopRecording()

            // Processing delay for speech recognition results to finalize
            delay(1200)

            val cleanTarget = targetText.replace(Regex("[^a-zA-Z\\s]"), "")
            val words = cleanTarget.split(" ").filter { it.isNotEmpty() }

            var score = 0
            val analysis = mutableListOf<Pair<String, WordScoreType>>()

            // Evaluate using actual recognized text from offline SpeechRecognizer
            val cleanRecognized = recognizedText.value.replace(Regex("[^a-zA-Z\\s]"), "")
            val recognizedWords = cleanRecognized.split(" ").filter { it.isNotEmpty() }
            
            val (computedScore, wordAnalysis) = alignAndScore(words, recognizedWords)
            score = computedScore
            analysis.addAll(wordAnalysis)

            score = score.coerceIn(0, 100)
            lastScore.value = score
            scoredWords.value = analysis

            // Track user XP progression
            val xpAwarded = if (score > 0) {
                when {
                    score >= 90 -> 15
                    score >= 80 -> 10
                    else -> 5
                }
            } else {
                0
            }

            if (xpAwarded > 0) {
                repository.awardXP(xpAwarded)
                showRewardOverlay.value = xpAwarded
            } else {
                showRewardOverlay.value = null
            }

            // Persistent progress update based on module type
            if (score > 0) {
                when (itemType) {
                    "grammar" -> {
                        activeGrammarLesson.value?.let { lesson ->
                            repository.updateGrammarLesson(lesson.copy(isCompleted = true))
                        }
                    }
                    "daily_sentence" -> {
                        activeDailySentence.value?.let { sentence ->
                            val isCorrect = score >= 85
                            repository.scheduleSpacedRepetition(sentence.id, isCorrect, score)
                        }
                    }
                    "paragraph" -> {
                        activeParagraph.value?.let { para ->
                            val currentMax = if (score > para.maxAccuracy) score else para.maxAccuracy
                            repository.updateParagraph(para.copy(isCompleted = true, maxAccuracy = currentMax))
                        }
                    }
                    "tongue_twister" -> {
                        activeTongueTwister.value?.let { twister ->
                            val currentMax = if (score > twister.maxAccuracy) score else twister.maxAccuracy
                            repository.updateTongueTwister(twister.copy(
                                isCompleted = true,
                                maxAccuracy = currentMax,
                                practiceCount = twister.practiceCount + 1
                            ))
                        }
                    }
                }
            }
        }
    }



    fun playRecordedVoice() {
        if (useSimulatedMic.value) {
            val textToSpeak = currentTargetText.value
            if (textToSpeak.isNotEmpty()) {
                isPlayingBack.value = true
                // Slightly altered pitch and rate to sound like a student/learner practicing
                tts?.setPitch(0.95f)
                tts?.setSpeechRate(0.85f)
                
                val params = HashMap<String, String>()
                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "user_simulated_speech"
                tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "user_simulated_speech")
                
                // Keep state active for simulated speech duration
                viewModelScope.launch {
                    val wordsCount = textToSpeak.split(" ").filter { it.isNotEmpty() }.size
                    val delayMs = (wordsCount * 450L).coerceAtLeast(1200L)
                    delay(delayMs)
                    isPlayingBack.value = false
                    // Reset to normal values
                    tts?.setPitch(1.0f)
                    updateTtsSettings()
                }
            }
        } else {
            if (voiceRecorder.getRecordedFile() != null) {
                isPlayingBack.value = true
                voiceRecorder.startPlayback {
                    isPlayingBack.value = false
                }
            }
        }
    }

    fun stopRecordedVoicePlayback() {
        if (useSimulatedMic.value) {
            tts?.stop()
            tts?.setPitch(1.0f)
            updateTtsSettings()
        } else {
            voiceRecorder.stopPlayback()
        }
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

    fun advanceDialogue() {
        val conv = activeConversation.value ?: return
        val lines = conv.dialogueJson
        // Deserialize lines simply or parsing tags
        val totalLines = 6 // Standard dialogues have 6-7 lines
        if (activeConversationIndex.value < totalLines - 1) {
            activeConversationIndex.value++
            triggerTtsForCurrentDialogue()
        } else {
            // Dialogue complete!
            viewModelScope.launch {
                repository.updateConversation(conv.copy(isCompleted = true))
                repository.awardXP(30)
                showRewardOverlay.value = 30
                navigateTo(Screen.Home)
            }
        }
    }

    fun triggerTtsForCurrentDialogue() {
        val conv = activeConversation.value ?: return
        // Simplistic parser of lines from Dialogue JSON
        val lines = parseDialogueJson(conv.dialogueJson)
        val currentIndex = activeConversationIndex.value
        if (currentIndex < lines.size) {
            val currentLine = lines[currentIndex]
            val speakerRole = currentLine.role // "A" or "B"
            if (speakerRole != userConversationRole.value) {
                // Speak this line!
                speak(currentLine.text)
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
            repository.updateProgress(UserProgress(id = 1, currentStreak = 0, longestStreak = 0, lastActiveDate = "", totalXP = 0))
            // Mark all items as uncompleted
            grammarLessons.value.forEach {
                repository.updateGrammarLesson(it.copy(isCompleted = false))
            }
            conversations.value.forEach {
                repository.updateConversation(it.copy(isCompleted = false))
            }
            dailySentences.value.forEach {
                repository.updateDailySentence(it.copy(timesPracticed = 0, lastAccuracy = 0, consecutiveCorrect = 0, reviewScheduledTime = 0))
            }
            paragraphs.value.forEach {
                repository.updateParagraph(it.copy(isCompleted = false, maxAccuracy = 0))
            }
            tongueTwisters.value.forEach {
                repository.updateTongueTwister(it.copy(isCompleted = false, maxAccuracy = 0, practiceCount = 0))
            }
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
    val role: String
)
