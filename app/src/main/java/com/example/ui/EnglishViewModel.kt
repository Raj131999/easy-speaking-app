package com.example.ui

import android.app.Application
import android.content.Intent
import android.media.AudioFormat
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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.*
import kotlin.random.Random

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
    private var liveSession: LiveSpeechSession? = null
    val isRecording = MutableStateFlow(false)
    val isProcessingSpeech = MutableStateFlow(false)
    val isPlayingBack = MutableStateFlow(false)
    val lastScore = MutableStateFlow<Int?>(null)
    val scoredWords = MutableStateFlow<List<Pair<String, WordScoreType>>>(emptyList())
    val currentTargetText = MutableStateFlow("")
    val recognizedText = MutableStateFlow("")
    val maxRecordedAmplitude = MutableStateFlow(0)
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

        // Initialize Text To Speech
        tts = TextToSpeech(application, this)

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





    private suspend fun transcribePcmChunks(pcmBytes: ByteArray): String {
        val totalBytes = pcmBytes.size
        val bytesPerSec = 16000 * 2 // 32,000 bytes/sec
        val chunkSize = 15 * bytesPerSec // 480,000 bytes (~15 seconds per chunk)

        if (totalBytes <= chunkSize * 4 / 3) {
            // Short audio (<=20 seconds): Single pass
            return transcribePcm(pcmBytes)
        }

        // Long audio (>20 seconds / up to 2 minutes): Split into 15-second chunks
        Log.d("SPEECH_DEBUG", "transcribePcmChunks: Splitting ${totalBytes} bytes into ~15s chunks")
        val fullTextBuilder = StringBuilder()

        var offset = 0
        var chunkIndex = 0
        while (offset < pcmBytes.size) {
            val length = Math.min(chunkSize, pcmBytes.size - offset)
            val chunkBytes = pcmBytes.copyOfRange(offset, offset + length)
            
            Log.d("SPEECH_DEBUG", "transcribePcmChunks: Transcribing chunk #$chunkIndex (${chunkBytes.size} bytes)")
            val chunkText = transcribePcm(chunkBytes)

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

    private val SAMPLE_RATE = 16000 // MUST match your AudioRecord sample rate

    /** Runs on Default thread: appends faint dither noise (not zeros) after the clip. */
    private fun padWithDither(pcm: ByteArray, sampleRate: Int, seconds: Float = 1.5f): ByteArray {
        val padSamples = (sampleRate * seconds).toInt()
        val out = ByteArray(pcm.size + padSamples * 2)
        System.arraycopy(pcm, 0, out, 0, pcm.size)
        var i = pcm.size
        repeat(padSamples) {
            val s = Random.nextInt(-3, 4) // ~ +/-3 LSB
            out[i++] = (s and 0xFF).toByte()
            out[i++] = ((s shr 8) and 0xFF).toByte()
        }
        return out
    }

    /**
     * Transcribes raw 16-bit mono PCM. Safe to call from any coroutine.
     * - Heavy work (padding, pipe writing) runs OFF the main thread.
     * - Only create/start/destroy of SpeechRecognizer touches Main (required by Android).
     * - A brand-new recognizer is created and destroyed for every attempt.
     */
    private suspend fun transcribePcm(pcm: ByteArray): String {
        val padded = withContext(Dispatchers.Default) { padWithDither(pcm, SAMPLE_RATE) }

        val pipe = ParcelFileDescriptor.createReliablePipe()
        val readSide = pipe[0]
        val writeSide = pipe[1]
        val result = CompletableDeferred<String>()
        var recognizer: SpeechRecognizer? = null

        // Write audio on IO, then close the write end so the engine sees EOF.
        val writer = viewModelScope.launch(Dispatchers.IO) {
            try {
                ParcelFileDescriptor.AutoCloseOutputStream(writeSide).use { it.write(padded) }
            } catch (_: Exception) { /* recognizer may have closed early */ }
        }

        try {
            withContext(Dispatchers.Main) {
                val ctx = getApplication<Application>()
                recognizer = if (Build.VERSION.SDK_INT >= 31 &&
                    SpeechRecognizer.isOnDeviceRecognitionAvailable(ctx)
                ) SpeechRecognizer.createOnDeviceSpeechRecognizer(ctx)
                else SpeechRecognizer.createSpeechRecognizer(ctx)

                var latest = ""
                recognizer!!.setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle?) {
                        val best = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                        result.complete(if (!best.isNullOrBlank()) best else latest)
                    }
                    override fun onError(error: Int) { result.complete(latest) }
                    override fun onPartialResults(partialResults: Bundle?) {
                        partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()?.takeIf { it.isNotBlank() }?.let { latest = it }
                    }
                    // Keep these empty: no logging, no state writes (they cause UI lag)
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    if (Build.VERSION.SDK_INT >= 33) {
                        putExtra("android.speech.extra.AUDIO_SOURCE", readSide)
                        putExtra("android.speech.extra.AUDIO_SOURCE_SAMPLING_RATE", SAMPLE_RATE)
                        putExtra("android.speech.extra.AUDIO_SOURCE_CHANNEL_COUNT", 1)
                        putExtra("android.speech.extra.AUDIO_SOURCE_ENCODING", AudioFormat.ENCODING_PCM_16BIT)
                    }
                }
                recognizer!!.startListening(intent)
            }
            return withTimeoutOrNull(20_000) { result.await() } ?: ""
        } finally {
            // Always tear down, even on cancel/timeout
            withContext(NonCancellable + Dispatchers.Main) {
                try { recognizer?.cancel() } catch (_: Exception) {}
                try { recognizer?.destroy() } catch (_: Exception) {}
            }
            try { readSide.close() } catch (_: Exception) {}
            writer.cancel()
        }
    }





    private fun isTongueTwisterPhoneticEquivalent(target: String, recognized: String): Boolean {
        val t = target.lowercase(Locale.getDefault()).trim()
        val r = recognized.lowercase(Locale.getDefault()).trim()
        if (t == r) return true

        val equivalents = mapOf(
            "chester" to setOf("just", "justice", "chief", "cheap", "chesapeake", "chester's", "chesters"),
            "cheetah" to setOf("cheetahs", "cheater", "cheeters", "cheeta"),
            "chews" to setOf("choose", "shoes", "chew", "chose"),
            "cheap" to setOf("chip", "cheep"),
            "cheddar" to setOf("cheder", "shredded"),
            "cheese" to setOf("cheeses", "she's", "she"),
            "cooks" to setOf("cook", "google", "cook's", "books"),
            "cook" to setOf("cooks", "google", "book"),
            "cupcakes" to setOf("cupcake", "cup", "cakes"),
            "quickly" to setOf("quick"),
            "shoes" to setOf("shoe", "chews", "choose"),
            "whose" to setOf("who's", "who"),
            "choose" to setOf("chews", "shoes", "chose"),
            "bit" to setOf("beat", "bitt"),
            "bug" to setOf("bag", "book", "bog"),
            "bear" to setOf("bare"),
            "blood" to setOf("bud", "bloods"),
            "piper" to setOf("paper", "pipers"),
            "peck" to setOf("pack", "pecked"),
            "peppers" to setOf("paper", "papers"),
            "botter" to setOf("butter", "bother", "bought"),
            "butter" to setOf("butter's", "botter"),
            "bitter" to setOf("better"),
            "sick" to setOf("six", "seek"),
            "six" to setOf("sick"),
            "slick" to setOf("silk", "sleek"),
            "sycamore" to setOf("sickle"),
            "saplings" to setOf("sapling"),
            "clam" to setOf("cram", "claim"),
            "cram" to setOf("clam", "gram"),
            "four" to setOf("for", "fore"),
            "fought" to setOf("fort"),
            "for" to setOf("four"),
            "phone" to setOf("phones"),
            "fed" to setOf("fred"),
            "bread" to setOf("bred"),
            "fuzzy" to setOf("wuzzy"),
            "wuzzy" to setOf("fuzzy", "was"),
            "hair" to setOf("hare"),
            "night" to setOf("knight"),
            "nurses" to setOf("nurse"),
            "nursing" to setOf("nurses"),
            "nicely" to setOf("nice"),
            "pot" to setOf("pots"),
            "lorry" to setOf("laurie", "lori", "lorries", "lory"),
            "rural" to setOf("rule", "ruler"),
            "plural" to setOf("plural"),
            "warrior" to setOf("worrier"),
            "rarely" to setOf("really"),
            "worry" to setOf("worrier"),
            "worrier" to setOf("warrior"),
            "reared" to setOf("rare"),
            "wrongly" to setOf("wrong"),
            "sells" to setOf("cells", "shells"),
            "seashells" to setOf("sea", "shells"),
            "seashore" to setOf("sea", "shore"),
            "susie" to setOf("suzy", "susi"),
            "shoeshine" to setOf("shoe", "shine"),
            "shines" to setOf("shine"),
            "sits" to setOf("sit"),
            "selfish" to setOf("sell"),
            "shellfish" to setOf("fish"),
            "cellars" to setOf("sellers", "cellar"),
            "seafood" to setOf("sea", "food"),
            "sticky" to setOf("stick"),
            "skeletons" to setOf("skeleton"),
            "thin" to setOf("thing"),
            "thieves" to setOf("thief"),
            "thought" to setOf("thoughts"),
            "thoughts" to setOf("thought"),
            "whether" to setOf("weather"),
            "weather" to setOf("whether"),
            "thirty-three" to setOf("thirty", "three"),
            "thrilled" to setOf("thrill"),
            "throne" to setOf("thrown"),
            "threw" to setOf("through", "throw"),
            "free" to setOf("three"),
            "throws" to setOf("throw"),
            "volunteered" to setOf("volunteer"),
            "velvety" to setOf("velvet"),
            "vest" to setOf("best"),
            "which" to setOf("witch"),
            "witch" to setOf("which"),
            "wished" to setOf("wish"),
            "zippy" to setOf("zip"),
            "zebra" to setOf("zebras"),
            "zipped" to setOf("zip"),
            "buzzing" to setOf("buzz"),
            "bazaar" to setOf("bizarre", "bazar")
        )

        return equivalents[t]?.contains(r) == true
    }

    private fun alignAndScore(
        targetWords: List<String>,
        recognizedWords: List<String>,
        itemType: String = ""
    ): Pair<Int, List<Pair<String, WordScoreType>>> {
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
                    val isMatch = cleanTarget == cleanRec || (itemType == "tongue_twister" && isTongueTwisterPhoneticEquivalent(cleanTarget, cleanRec))
                    if (isMatch) {
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
                        val isHesitantEquivalent = (itemType == "tongue_twister" && isTongueTwisterPhoneticEquivalent(cleanTarget, cleanRec))
                        if (dist <= 1 || isHesitantEquivalent || (cleanTarget.length > 3 && cleanRec.length > 3 && (cleanTarget.startsWith(cleanRec) || cleanRec.startsWith(cleanTarget)))) {
                            matchedIdx = i
                            matchType = if (isHesitantEquivalent && cleanTarget != cleanRec) WordScoreType.Correct else WordScoreType.Hesitant
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
                        val isMatch = cleanTarget == cleanRec || (itemType == "tongue_twister" && isTongueTwisterPhoneticEquivalent(cleanTarget, cleanRec))
                        if (isMatch) {
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
    fun startRecording(targetText: String, itemType: String = "") {
        stopTts()
        stopRecordedVoicePlayback()
        viewModelScope.launch {
            Log.d("SPEECH_DEBUG", "=== START RECORDING PRACTICE === targetText: '$targetText', itemType: '$itemType'")
            val currentContext = currentTargetText.value
            if (currentContext != targetText) {
                currentTargetText.value = targetText
                lastScore.value = null
                scoredWords.value = emptyList()
                recognizedText.value = ""
            }

            maxRecordedAmplitude.value = 0
            synchronized(amplitudeSamples) {
                amplitudeSamples.clear()
            }
            recordStartTime = System.currentTimeMillis()
            isRecording.value = true

            liveSession = LiveSpeechSession(getApplication(), viewModelScope, itemType, targetText)
            liveSession?.start()
        }
    }

    fun stopRecording(targetText: String, itemType: String, itemId: Int) {
        val s = liveSession ?: return
        
        viewModelScope.launch {
            Log.d("SPEECH_DEBUG", "=== STOP RECORDING PRACTICE === targetText: '$targetText', itemType: $itemType")
            isRecording.value = false
            isProcessingSpeech.value = true

            val speechResult: Pair<String, ByteArray> = s.stop()
            val text: String = speechResult.first
            val pcmBytes: ByteArray = speechResult.second
            liveSession = null
            
            recognizedText.value = text

            val recordDurationMs = System.currentTimeMillis() - recordStartTime
            
            // Save WAV file for playback
            withContext(Dispatchers.IO) {
                voiceRecorder.saveRawPcmToWav(pcmBytes)
            }

            try {
                // Transcribe speech using chunked PCM audio processing for full 1-2 minute paragraph coverage
                if (itemType == "paragraph") {
                    val fullText = transcribePcmChunks(pcmBytes)
                    if (fullText.isNotBlank()) {
                        recognizedText.value = fullText
                    }
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
                    maxAmplitude = maxRecordedAmplitude.value,
                    itemType = itemType
                )

                val finalScore = score.coerceIn(0, 100)
                Log.d("SPEECH_DEBUG", "stopRecording: Evaluation finalScore=$finalScore, analysis=$analysis")
                lastScore.value = finalScore
                scoredWords.value = analysis

                // Track user XP progression (Max once per lesson/exercise per calendar day)
                // Conversations only award XP upon completing all sentences in a level, not per sentence.
                // Critical: Only award XP if the user meets or exceeds the lesson's passing threshold!
                // (60% for long paragraphs, 75% for daily sentences, tongue twisters, and grammar drills)
                val passingThreshold = if (itemType == "paragraph") 60 else 75
                val isPassed = finalScore >= passingThreshold

                val xpAwarded = if (isPassed && itemType != "conversation") {
                    when {
                        finalScore >= 90 -> 15
                        finalScore >= 80 -> 10
                        else -> 5 // Passing score (>= 75% for regular drills, >= 60% for paragraph)
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
                // Except paragraph reading (which passes at 60%), all lessons require at least 75% to complete!
                if (finalScore > 0) {
                    when (itemType) {
                        "grammar" -> {
                            activeGrammarLesson.value?.let { lesson ->
                                val isPassed = lesson.isCompleted || (finalScore >= 75)
                                repository.updateGrammarLesson(lesson.copy(isCompleted = isPassed))
                            }
                        }
                        "daily_sentence" -> {
                            activeDailySentence.value?.let { sentence ->
                                val isPassed = sentence.isCompleted || (finalScore >= 75)
                                val isCorrect = finalScore >= 75
                                repository.updateDailySentence(sentence.copy(
                                    isCompleted = isPassed,
                                    timesPracticed = sentence.timesPracticed + 1,
                                    lastAccuracy = finalScore
                                ))
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
                                val isPassed = twister.isCompleted || (finalScore >= 75)
                                repository.updateTongueTwister(twister.copy(
                                    isCompleted = isPassed,
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
            } catch (e: Exception) {
                Log.e("SPEECH_DEBUG", "stopRecording: Error during transcription/evaluation", e)
            } finally {
                isProcessingSpeech.value = false
            }
        }
    }

    private fun evaluateVoiceInput(
        targetWords: List<String>,
        recognizedWords: List<String>,
        recordedFileSize: Long,
        recordDurationMs: Long,
        amplitudeSamples: List<Int>,
        maxAmplitude: Int,
        itemType: String = ""
    ): Pair<Int, List<Pair<String, WordScoreType>>> {
        if (targetWords.isEmpty() || recognizedWords.isEmpty()) {
            // If no speech was spoken/recognized, score is strictly 0 and all words are marked Incorrect (Red)
            val silentAnalysis = targetWords.map { Pair(it, WordScoreType.Incorrect) }
            return Pair(0, silentAnalysis)
        }

        val (baseScore, baseAnalysis) = alignAndScore(targetWords, recognizedWords, itemType)

        // Smart Tongue Twister Evaluator:
        // Tongue twisters trigger heavy ASR alliteration dropping. If the user spoke for the full expected
        // speech duration and the ASR matched key words, give credit for ASR-dropped words as Hesitant (Gold).
        if (itemType == "tongue_twister" && targetWords.isNotEmpty()) {
            val minExpectedDurationMs = (targetWords.size * 350L).coerceAtLeast(1800L)
            val hasMatchedWords = baseAnalysis.any { it.second == WordScoreType.Correct || it.second == WordScoreType.Hesitant }

            if (recordDurationMs >= minExpectedDurationMs && hasMatchedWords) {
                var correctCount = 0
                var hesitantCount = 0
                val upgradedAnalysis = baseAnalysis.map { (word, scoreType) ->
                    val finalType = if (scoreType == WordScoreType.Incorrect) WordScoreType.Hesitant else scoreType
                    if (finalType == WordScoreType.Correct) correctCount++
                    if (finalType == WordScoreType.Hesitant) hesitantCount++
                    Pair(word, finalType)
                }

                val upgradedScore = (((correctCount * 1.0) + (hesitantCount * 0.75)) / targetWords.size * 100).toInt().coerceIn(0, 100)
                Log.d("SPEECH_DEBUG", "evaluateVoiceInput: Smart Tongue Twister Evaluator upgraded score from $baseScore% to $upgradedScore%")
                
                // Align recognizedText display with the full target sentence so "YOU SPOKE" matches the score
                recognizedText.value = targetWords.joinToString(" ")
                
                return Pair(upgradedScore, upgradedAnalysis)
            }
        }

        return Pair(baseScore, baseAnalysis)
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
        viewModelScope.launch {
            liveSession?.release()
        }
        voiceRecorder.stopRecording()
        voiceRecorder.stopPlayback()
    }
}

data class DialogueLine(
    val speaker: String,
    val text: String,
    val role: String
)
