package com.example.ui

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    val recentPracticeAttempts = repository.recentPracticeAttempts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val missedWordReviews = repository.missedWordReviews.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // TTS Engine States
    private var tts: TextToSpeech? = null
    val ttsReady = MutableStateFlow(false)
    val isTtsSpeaking = MutableStateFlow(false)
    val currentTtsText = MutableStateFlow<String?>(null)
    val selectedAccent = MutableStateFlow("IN") // Default aligned with Indian English
    val selectedSpeed = MutableStateFlow(1.0f)   // 0.5f, 0.75f, 1.0f, 1.25f, 1.5f

    // Offline Speech Model (Vosk)
    private val voskModelManager = VoskModelManager(application)
    val modelDownloadState: StateFlow<ModelDownloadState> = voskModelManager.state
    val selectedSpeechModel: StateFlow<SpeechModelOption> = voskModelManager.selectedOption
    val installedSpeechModels: StateFlow<Set<String>> = voskModelManager.installedModelIds
    val useGrammarMode: StateFlow<Boolean> = voskModelManager.useGrammarMode
    val speechStatusNotice = MutableStateFlow<String?>(null)
    val needsMicPermissionRequest = MutableStateFlow(false)

    // Voice recording and play states
    private val voiceRecorder = VoiceRecorder(application)
    private val recordingMutex = Mutex()
    private var liveSession: LiveSpeechSession? = null
    private var playbackTrackerJob: Job? = null
    val isRecording = MutableStateFlow(false)
    val isProcessingSpeech = MutableStateFlow(false)
    val isPlayingBack = MutableStateFlow(false)
    val playbackPositionSec = MutableStateFlow<Float?>(null)
    val lastScore = MutableStateFlow<Int?>(null)
    val scoredWords = MutableStateFlow<List<Pair<String, WordScoreType>>>(emptyList())
    val alignedWordTimings = MutableStateFlow<List<AlignedWordFeedback>>(emptyList())
    val missedWordsSummary = MutableStateFlow<List<String>>(emptyList())
    val unclearWordsSummary = MutableStateFlow<List<String>>(emptyList())
    val extraWordsSummary = MutableStateFlow<List<String>>(emptyList())
    val currentTargetText = MutableStateFlow("")
    val recognizedText = MutableStateFlow("")
    val maxRecordedAmplitude = MutableStateFlow(0)
    val liveAudioLevel = MutableStateFlow(0f)
    private val amplitudeSamples = mutableListOf<Int>()

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

        // Download (first launch) and load offline Vosk speech recognition model
        voskModelManager.ensureModelReady(viewModelScope)

        // Initialize Text To Speech
        tts = TextToSpeech(application, this)
    }

    fun retryModelDownload() {
        speechStatusNotice.value = null
        voskModelManager.ensureModelReady(viewModelScope)
    }

    fun selectSpeechModel(option: SpeechModelOption) {
        speechStatusNotice.value = null
        voskModelManager.selectModel(option, viewModelScope)
    }

    fun setGrammarModeEnabled(enabled: Boolean) {
        voskModelManager.setGrammarModeEnabled(enabled)
    }

    fun redownloadSpeechModel() {
        speechStatusNotice.value = null
        voskModelManager.redownloadSelectedModel(viewModelScope)
    }

    fun clearSpeechStatusNotice() {
        speechStatusNotice.value = null
    }

    fun clearAttemptFeedback() {
        stopRecordedVoicePlayback()
        lastScore.value = null
        scoredWords.value = emptyList()
        alignedWordTimings.value = emptyList()
        missedWordsSummary.value = emptyList()
        unclearWordsSummary.value = emptyList()
        extraWordsSummary.value = emptyList()
        recognizedText.value = ""
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

    fun extractSpokenExample(text: String): String = Companion.extractSpokenExample(text)

    companion object {
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
        // Reset states and release any active recording session cleanly
        stopTts()
        stopRecordedVoicePlayback()
        lastScore.value = null
        scoredWords.value = emptyList()
        isRecording.value = false
        isProcessingSpeech.value = false
        isPlayingBack.value = false
        liveAudioLevel.value = 0f
        speechStatusNotice.value = null
        val activeSession = liveSession
        liveSession = null
        if (activeSession != null) {
            viewModelScope.launch {
                recordingMutex.withLock {
                    activeSession.release()
                }
            }
        }
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
    fun startRecording(targetText: String, itemType: String = "") {
        stopTts()
        stopRecordedVoicePlayback()
        speechStatusNotice.value = null

        val hasMicPermission = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasMicPermission) {
            speechStatusNotice.value = "Microphone permission is required. Please allow microphone access to practice speaking."
            needsMicPermissionRequest.value = true
            return
        }

        val model = voskModelManager.getLoadedModel()
        if (model == null) {
            if (modelDownloadState.value is ModelDownloadState.Error) {
                voskModelManager.ensureModelReady(viewModelScope)
            }
            speechStatusNotice.value = "Voice recognition setup is still finishing. Please wait a moment."
            return
        }

        viewModelScope.launch {
            recordingMutex.withLock {
                liveSession?.let { oldSession ->
                    liveSession = null
                    oldSession.release()
                }

                Log.d("SPEECH_DEBUG", "=== START RECORDING PRACTICE (Vosk) === targetText: '$targetText', itemType: '$itemType'")
                val currentContext = currentTargetText.value
                if (currentContext != targetText) {
                    currentTargetText.value = targetText
                }
                lastScore.value = null
                scoredWords.value = emptyList()
                alignedWordTimings.value = emptyList()
                missedWordsSummary.value = emptyList()
                unclearWordsSummary.value = emptyList()
                extraWordsSummary.value = emptyList()
                recognizedText.value = ""

                maxRecordedAmplitude.value = 0
                synchronized(amplitudeSamples) {
                    amplitudeSamples.clear()
                }
                recordStartTime = System.currentTimeMillis()
                isRecording.value = true

                val recordingsDir = File(getApplication<Application>().filesDir, "recordings")
                if (!recordingsDir.exists()) recordingsDir.mkdirs()
                // Keep disk usage tidy by pruning recordings older than latest 20
                recordingsDir.listFiles()?.sortedByDescending { it.lastModified() }?.drop(20)?.forEach {
                    it.delete()
                }
                val wavFile = File(recordingsDir, "attempt_${itemType.ifBlank { "drill" }}_${System.currentTimeMillis()}.wav")

                val session = LiveSpeechSession(
                    context = getApplication(),
                    model = model,
                    targetText = targetText,
                    wavFile = wavFile,
                    useGrammarMode = voskModelManager.useGrammarMode.value,
                    onAmplitude = { amp -> liveAudioLevel.value = amp },
                    onPartialText = { partial -> recognizedText.value = partial }
                )
                liveSession = session
                session.start(viewModelScope)
            }
        }
    }

    fun stopRecording(targetText: String, itemType: String, itemId: Int) {
        val s = liveSession ?: return
        liveSession = null

        viewModelScope.launch {
            recordingMutex.withLock {
                Log.d("SPEECH_DEBUG", "=== STOP RECORDING PRACTICE (Vosk) === targetText: '$targetText', itemType: $itemType")
                isRecording.value = false
                liveAudioLevel.value = 0f
                isProcessingSpeech.value = true

                val captureResult = s.stop()
                recognizedText.value = captureResult.transcript
                voiceRecorder.setActiveRecordedFile(captureResult.wavFilePath)

                val recordDurationMs = System.currentTimeMillis() - recordStartTime

                try {
                    Log.d("SPEECH_DEBUG", "stopRecording: Vosk transcript='${captureResult.transcript}', words=${captureResult.spokenWords.size}")

                    val displayWords = targetText
                        .replace(Regex("[^a-zA-Z0-9'\\s]"), " ")
                        .split(Regex("\\s+"))
                        .filter { it.isNotBlank() }

                    var eval = PronunciationScorer.evaluatePronunciation(
                        targetDisplayWords = displayWords,
                        spokenWords = captureResult.spokenWords,
                        itemType = itemType,
                        grammarWords = captureResult.grammarWords,
                        energyWindows = captureResult.energyWindows
                    )

                    // Smart Tongue Twister credit for fast alliterative speech when user spoke full duration and matched words
                    if (itemType == "tongue_twister" && displayWords.isNotEmpty()) {
                        val minExpectedDurationMs = (displayWords.size * 350L).coerceAtLeast(1800L)
                        val hasMatchedWords = eval.wordScores.any {
                            it.second == WordScoreType.Correct || it.second == WordScoreType.Hesitant
                        }
                        if (recordDurationMs >= minExpectedDurationMs && hasMatchedWords && eval.score < 75) {
                            var correctCount = 0
                            var hesitantCount = 0
                            val upgradedPairs = eval.wordScores.map { (word, scoreType) ->
                                val finalType = if (scoreType == WordScoreType.Incorrect) WordScoreType.Hesitant else scoreType
                                if (finalType == WordScoreType.Correct) correctCount++
                                if (finalType == WordScoreType.Hesitant) hesitantCount++
                                word to finalType
                            }
                            val upgradedScore = (((correctCount * 1.0) + (hesitantCount * 0.75)) / displayWords.size * 100)
                                .toInt()
                                .coerceIn(0, 100)
                            eval = eval.copy(score = upgradedScore, wordScores = upgradedPairs)
                        }
                    }

                    val finalScore = eval.score.coerceIn(0, 100)
                    lastScore.value = finalScore
                    scoredWords.value = eval.wordScores
                    alignedWordTimings.value = eval.alignedDetails
                    missedWordsSummary.value = eval.missedWords
                    unclearWordsSummary.value = eval.unclearWords
                    extraWordsSummary.value = eval.extraWords

                    // Step F: Save score, missed words, WAV file path, and timestamps to Room + feed spaced repetition
                    val correctWords = eval.alignedDetails
                        .filter { it.status == WordAlignmentStatus.Correct && !it.isUnclear }
                        .map { it.targetWord }
                    repository.savePracticeAttemptAndFeedSpacedRepetition(
                        itemType = itemType,
                        itemId = itemId,
                        targetText = targetText,
                        recognizedText = captureResult.transcript,
                        score = finalScore,
                        missedWords = eval.missedWords,
                        unclearWords = eval.unclearWords,
                        extraWords = eval.extraWords,
                        correctWords = correctWords,
                        wavFilePath = captureResult.wavFilePath,
                        wordTimingsJson = PronunciationScorer.serializeAlignedTimings(eval.alignedDetails)
                    )

                    // Track user XP progression (Max once per lesson/exercise per calendar day)
                    val passingThreshold = if (itemType == "paragraph") 60 else 75
                    val isPassed = finalScore >= passingThreshold

                    val xpAwarded = if (isPassed && itemType != "conversation") {
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
                        showRewardOverlay.value = if (xpClaimed) xpAwarded else null
                    } else {
                        showRewardOverlay.value = null
                    }

                    if (finalScore > 0) {
                        when (itemType) {
                            "grammar" -> {
                                activeGrammarLesson.value?.let { lesson ->
                                    val completed = lesson.isCompleted || (finalScore >= 75)
                                    repository.updateGrammarLesson(lesson.copy(isCompleted = completed))
                                }
                            }
                            "daily_sentence" -> {
                                activeDailySentence.value?.let { sentence ->
                                    val completed = sentence.isCompleted || (finalScore >= 75)
                                    val isCorrect = finalScore >= 75
                                    repository.updateDailySentence(sentence.copy(
                                        isCompleted = completed,
                                        timesPracticed = sentence.timesPracticed + 1,
                                        lastAccuracy = finalScore
                                    ))
                                    repository.scheduleSpacedRepetition(sentence.id, isCorrect, finalScore)
                                }
                            }
                            "paragraph" -> {
                                activeParagraph.value?.let { para ->
                                    val currentMax = if (finalScore > para.maxAccuracy) finalScore else para.maxAccuracy
                                    val completed = para.isCompleted || (finalScore >= 60)
                                    repository.updateParagraph(para.copy(isCompleted = completed, maxAccuracy = currentMax))
                                }
                            }
                            "tongue_twister" -> {
                                activeTongueTwister.value?.let { twister ->
                                    val currentMax = if (finalScore > twister.maxAccuracy) finalScore else twister.maxAccuracy
                                    val completed = twister.isCompleted || (finalScore >= 75)
                                    repository.updateTongueTwister(twister.copy(
                                        isCompleted = completed,
                                        maxAccuracy = currentMax,
                                        practiceCount = twister.practiceCount + 1
                                    ))
                                }
                            }
                            "conversation" -> {
                                // Roleplay line scored
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SPEECH_DEBUG", "stopRecording: Error during transcription/evaluation", e)
                } finally {
                    isProcessingSpeech.value = false
                }
            }
        }
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
            playbackPositionSec.value = 0f
            voiceRecorder.startPlayback {
                playbackTrackerJob?.cancel()
                playbackTrackerJob = null
                isPlayingBack.value = false
                playbackPositionSec.value = null
            }
            playbackTrackerJob?.cancel()
            playbackTrackerJob = viewModelScope.launch {
                while (isPlayingBack.value) {
                    val posMs = voiceRecorder.getCurrentPositionMs()
                    if (posMs >= 0) {
                        playbackPositionSec.value = posMs / 1000f
                    }
                    delay(50L)
                }
            }
        } else if (currentTargetText.value.isNotEmpty()) {
            speak(currentTargetText.value)
        }
    }

    fun stopRecordedVoicePlayback() {
        playbackTrackerJob?.cancel()
        playbackTrackerJob = null
        voiceRecorder.stopPlayback()
        isPlayingBack.value = false
        playbackPositionSec.value = null
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
            repository.clearPracticeLogs()
            restoreStruggledSection()
        }
    }

    fun updateDailyGoalXP(target: Int) {
        val current = userProgress.value ?: return
        viewModelScope.launch {
            repository.updateProgress(current.copy(dailyGoalXP = target))
        }
    }

    fun releaseResources() {
        playbackTrackerJob?.cancel()
        playbackTrackerJob = null
        val activeSession = liveSession
        liveSession = null
        CoroutineScope(Dispatchers.IO).launch {
            activeSession?.release()
            voskModelManager.release()
        }
        voiceRecorder.stopRecording()
        voiceRecorder.stopPlayback()
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
        releaseResources()
    }
}

data class DialogueLine(
    val speaker: String,
    val text: String,
    val role: String
)
