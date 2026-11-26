package com.example.ui

import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class SpokenWordTiming(
    val word: String,
    val startSec: Float,
    val endSec: Float,
    val conf: Float
)

data class AudioEnergyWindow(
    val startSec: Float,
    val endSec: Float,
    val rms: Float
)

enum class WordAlignmentStatus {
    Correct,
    Close,
    Missed,
    Extra
}

data class AlignedWordFeedback(
    val targetWord: String,
    val spokenWord: String? = null,
    val status: WordAlignmentStatus,
    val startSec: Float? = null,
    val endSec: Float? = null,
    val confidence: Float? = null,
    val isUnclear: Boolean = false
)

data class PronunciationEvaluationResult(
    val score: Int,
    val wordScores: List<Pair<String, WordScoreType>>,
    val alignedDetails: List<AlignedWordFeedback>,
    val missedWords: List<String>,
    val unclearWords: List<String>,
    val extraWords: List<String>
)

object PronunciationScorer {

    const val UNCLEAR_CONFIDENCE_THRESHOLD = 0.6f
    const val UNKNOWN_WORD_DISPLAY = "???"

    private val CONTRACTIONS = mapOf(
        "i'm" to listOf("i", "am"),
        "you're" to listOf("you", "are"),
        "we're" to listOf("we", "are"),
        "they're" to listOf("they", "are"),
        "it's" to listOf("it", "is"),
        "he's" to listOf("he", "is"),
        "she's" to listOf("she", "is"),
        "that's" to listOf("that", "is"),
        "there's" to listOf("there", "is"),
        "here's" to listOf("here", "is"),
        "what's" to listOf("what", "is"),
        "who's" to listOf("who", "is"),
        "where's" to listOf("where", "is"),
        "how's" to listOf("how", "is"),
        "when's" to listOf("when", "is"),
        "why's" to listOf("why", "is"),
        "let's" to listOf("let", "us"),
        "i've" to listOf("i", "have"),
        "you've" to listOf("you", "have"),
        "we've" to listOf("we", "have"),
        "they've" to listOf("they", "have"),
        "would've" to listOf("would", "have"),
        "could've" to listOf("could", "have"),
        "should've" to listOf("should", "have"),
        "i'll" to listOf("i", "will"),
        "you'll" to listOf("you", "will"),
        "he'll" to listOf("he", "will"),
        "she'll" to listOf("she", "will"),
        "we'll" to listOf("we", "will"),
        "they'll" to listOf("they", "will"),
        "it'll" to listOf("it", "will"),
        "that'll" to listOf("that", "will"),
        "i'd" to listOf("i", "would"),
        "you'd" to listOf("you", "would"),
        "he'd" to listOf("he", "would"),
        "she'd" to listOf("she", "would"),
        "we'd" to listOf("we", "would"),
        "they'd" to listOf("they", "would"),
        "don't" to listOf("do", "not"),
        "doesn't" to listOf("does", "not"),
        "didn't" to listOf("did", "not"),
        "can't" to listOf("can", "not"),
        "cannot" to listOf("can", "not"),
        "won't" to listOf("will", "not"),
        "wouldn't" to listOf("would", "not"),
        "couldn't" to listOf("could", "not"),
        "shouldn't" to listOf("should", "not"),
        "isn't" to listOf("is", "not"),
        "aren't" to listOf("are", "not"),
        "wasn't" to listOf("was", "not"),
        "weren't" to listOf("were", "not"),
        "hasn't" to listOf("has", "not"),
        "haven't" to listOf("have", "not"),
        "hadn't" to listOf("had", "not"),
        "mustn't" to listOf("must", "not"),
        "needn't" to listOf("need", "not"),
        "shan't" to listOf("shall", "not"),
        "daren't" to listOf("dare", "not"),
        "mightn't" to listOf("might", "not"),
        "oughtn't" to listOf("ought", "not"),
        "what're" to listOf("what", "are"),
        "who're" to listOf("who", "are"),
        "who'd" to listOf("who", "would"),
        "who'll" to listOf("who", "will"),
        "who've" to listOf("who", "have"),
        "what'll" to listOf("what", "will"),
        "what've" to listOf("what", "have"),
        "what'd" to listOf("what", "did"),
        "where'd" to listOf("where", "did"),
        "where'll" to listOf("where", "will"),
        "when'd" to listOf("when", "did"),
        "when'll" to listOf("when", "will"),
        "why'd" to listOf("why", "did"),
        "how'd" to listOf("how", "did"),
        "how'll" to listOf("how", "will"),
        "there'd" to listOf("there", "would"),
        "there'll" to listOf("there", "will"),
        "there've" to listOf("there", "have"),
        "that'd" to listOf("that", "would"),
        "it'd" to listOf("it", "would")
    )

    // Informal / connected-speech shortened forms — ONLY activated when the target sentence itself
    // contains that shortened word, so regular sentences without these words remain untouched.
    private val INFORMAL_SPOKEN_REDUCTIONS = mapOf(
        "gimme" to listOf("give", "me"),
        "lemme" to listOf("let", "me"),
        "gonna" to listOf("going", "to"),
        "wanna" to listOf("want", "to"),
        "gotta" to listOf("got", "to"),
        "gotcha" to listOf("got", "you"),
        "betcha" to listOf("bet", "you"),
        "kinda" to listOf("kind", "of"),
        "sorta" to listOf("sort", "of"),
        "didja" to listOf("did", "you"),
        "wouldja" to listOf("would", "you"),
        "couldja" to listOf("could", "you"),
        "shoulda" to listOf("should", "have"),
        "woulda" to listOf("would", "have"),
        "coulda" to listOf("could", "have"),
        "musta" to listOf("must", "have"),
        "mighta" to listOf("might", "have"),
        "oughta" to listOf("ought", "to"),
        "outta" to listOf("out", "of"),
        "lotta" to listOf("lot", "of"),
        "hafta" to listOf("have", "to"),
        "hasta" to listOf("has", "to"),
        "usta" to listOf("used", "to"),
        "supposta" to listOf("supposed", "to"),
        "dunno" to listOf("do", "not", "know"),
        "doncha" to listOf("do", "not", "you"),
        "whatcha" to listOf("what", "are", "you"),
        "c'mon" to listOf("come", "on"),
        "cmon" to listOf("come", "on"),
        "y'all" to listOf("you", "all"),
        "yall" to listOf("you", "all"),
        "tryna" to listOf("trying", "to"),
        "cuppa" to listOf("cup", "of"),
        "imma" to listOf("i", "am", "going", "to"),
        "ima" to listOf("i", "am", "going", "to")
    )

    // Common apostrophe-dropped forms emitted by small acoustic models
    private val APOSTROPHE_DROPPED_CONTRACTIONS = mapOf(
        "its" to "it's",
        "dont" to "don't",
        "doesnt" to "doesn't",
        "didnt" to "didn't",
        "cant" to "can't",
        "wont" to "won't",
        "isnt" to "isn't",
        "arent" to "aren't",
        "wasnt" to "wasn't",
        "werent" to "weren't",
        "hasnt" to "hasn't",
        "havent" to "haven't",
        "hadnt" to "hadn't",
        "wouldnt" to "wouldn't",
        "couldnt" to "couldn't",
        "shouldnt" to "shouldn't",
        "mustnt" to "mustn't",
        "neednt" to "needn't",
        "wouldve" to "would've",
        "couldve" to "could've",
        "shouldve" to "should've",
        "youre" to "you're",
        "theyre" to "they're",
        "ive" to "i've",
        "youve" to "you've",
        "weve" to "we've",
        "theyve" to "they've",
        "thats" to "that's",
        "whats" to "what's",
        "whos" to "who's",
        "wheres" to "where's",
        "hows" to "how's"
    )

    // Short / unstressed function words small models most frequently drop even when spoken clearly
    private val SHORT_UNSTRESSED_WORDS = setOf(
        "a", "an", "the", "to", "of", "in", "on", "at", "by", "for", "with", "from", "into",
        "i", "me", "my", "you", "your", "he", "him", "his", "she", "her", "it", "its", "we", "us", "our", "they", "them", "their",
        "am", "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did",
        "will", "would", "can", "could", "shall", "should", "may", "might", "must",
        "and", "or", "but", "so", "if", "as", "than", "that", "this", "these", "those",
        "not", "no", "up", "out", "off", "all", "one", "who", "what", "when", "just", "then"
    )

    fun normalizeToken(raw: String): String {
        val unified = raw
            .replace('\u2019', '\'')
            .replace('\u2018', '\'')
            .replace('`', '\'')
            .lowercase(Locale.US)
            .trim()
        if (unified == "[unk]" || unified == UNKNOWN_WORD_DISPLAY) return UNKNOWN_WORD_DISPLAY
        return unified
            .replace(Regex("[^a-z0-9']"), "")
            .trim('\'')
    }

    fun formatTranscriptWithUnk(raw: String): String {
        val withUnk = raw.replace(Regex("(?i)\\[unk\\]"), UNKNOWN_WORD_DISPLAY)
        return SpokenTextNormalizer.normalizeToSpokenWords(withUnk)
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun expandNormalizedWord(
        cleanToken: String,
        targetContractionSet: Set<String> = emptySet(),
        targetInformalReductions: Set<String> = emptySet()
    ): List<String> {
        if (cleanToken.isBlank()) return emptyList()
        if (cleanToken == UNKNOWN_WORD_DISPLAY) return listOf(UNKNOWN_WORD_DISPLAY)

        if (targetInformalReductions.contains(cleanToken)) {
            INFORMAL_SPOKEN_REDUCTIONS[cleanToken]?.let { return it }
        }

        val contractionKey = when {
            CONTRACTIONS.containsKey(cleanToken) -> cleanToken
            APOSTROPHE_DROPPED_CONTRACTIONS[cleanToken]?.let { targetContractionSet.contains(it) } == true ->
                APOSTROPHE_DROPPED_CONTRACTIONS[cleanToken]!!
            else -> cleanToken
        }
        return CONTRACTIONS[contractionKey] ?: listOf(cleanToken)
    }

    /**
     * When a target word explicitly uses the casual dropped-'g' "-in'" spelling (e.g. "packin'",
     * "travelin'", "workin'"), returns the accepted spoken equivalents (e.g. "packin", "packing",
     * and "traveling"/"travelling") ONLY for that target word.
     */
    private fun droppedGEquivalentsForRawTarget(rawDisplayWord: String): Set<String> {
        val unified = rawDisplayWord
            .replace('\u2019', '\'')
            .replace('\u2018', '\'')
            .replace('`', '\'')
            .lowercase(Locale.US)
            .trim()
            .replace(Regex("[^a-z']"), "")
        if (!unified.matches(Regex("[a-z]{2,}in'"))) return emptySet()

        val stem = unified.dropLast(1) // e.g. "packin", "travelin"
        val equivalents = mutableSetOf(stem, "${stem}g")
        // Support British/Australian double-L spelling (e.g. travelin' -> traveling / travelling)
        if (stem.endsWith("lin") && !stem.endsWith("llin") && stem.length >= 5) {
            equivalents.add("${stem.dropLast(2)}ling")
        }
        return equivalents
    }

    fun isShortUnstressedWord(cleanWord: String): Boolean {
        if (cleanWord.isBlank()) return false
        return cleanWord.length <= 3 || SHORT_UNSTRESSED_WORDS.contains(cleanWord)
    }

    /**
     * Step B: Split target text into lowercase punctuation-stripped words,
     * keeping word order for alignment.
     */
    fun extractTargetWords(targetText: String): List<String> {
        return SpokenTextNormalizer.normalizeToSpokenWords(targetText)
            .replace('\u2019', '\'')
            .replace('\u2018', '\'')
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9'\\s]"), " ")
            .split(Regex("\\s+"))
            .map { it.trim('\'') }
            .filter { it.isNotBlank() }
    }

    /**
     * Step B: Build Vosk grammar JSON: ["word1", "word2", ..., "[unk]"]
     * using unique lowercase words from the target text.
     */
    fun buildGrammarJson(targetText: String): String? {
        val words = extractTargetWords(targetText)
        if (words.isEmpty()) return null
        val arr = JSONArray()
        if (words.size > 1) {
            arr.put(words.joinToString(" "))
        }
        words.distinct().forEach { arr.put(it) }
        arr.put("[unk]")
        return arr.toString()
    }

    /**
     * Parse Vosk `result` / `finalResult` JSON with `setWords(true)`:
     * {"result": [{"conf": 1.0, "end": 1.23, "start": 0.90, "word": "hello"}], "text": "hello"}
     * Preserves `[unk]` as `???` so unknown/garbled words remain visible in the UI.
     */
    fun parseVoskWordTimings(json: String?): Pair<String, List<SpokenWordTiming>> {
        if (json.isNullOrBlank()) return "" to emptyList()
        return try {
            val obj = JSONObject(json)
            val text = obj.optString("text", "").trim()
            val resultArray = obj.optJSONArray("result")
            val words = mutableListOf<SpokenWordTiming>()
            if (resultArray != null) {
                for (i in 0 until resultArray.length()) {
                    val wObj = resultArray.optJSONObject(i) ?: continue
                    val rawWord = wObj.optString("word", "").trim()
                    if (rawWord.isBlank()) continue
                    val cleanWord = normalizeToken(rawWord)
                    if (cleanWord.isBlank()) continue
                    words.add(
                        SpokenWordTiming(
                            word = cleanWord,
                            startSec = wObj.optDouble("start", 0.0).toFloat(),
                            endSec = wObj.optDouble("end", 0.0).toFloat(),
                            conf = wObj.optDouble("conf", 1.0).toFloat()
                        )
                    )
                }
            } else if (text.isNotBlank()) {
                formatTranscriptWithUnk(text)
                    .split(Regex("\\s+"))
                    .map { normalizeToken(it) }
                    .filter { it.isNotBlank() }
                    .forEach { w ->
                        words.add(SpokenWordTiming(word = w, startSec = 0f, endSec = 0f, conf = 1.0f))
                    }
            }
            val displayText = formatTranscriptWithUnk(text)
            displayText to words
        } catch (_: Exception) {
            "" to emptyList()
        }
    }

    /**
     * Converts a plain transcript (e.g. from Android SpeechRecognizer) into SpokenWordTiming
     * entries distributed monotonically across the active speech window in [energyWindows].
     * When [expectedWordCount] exceeds the recognized token count, the span is scaled down
     * proportionally so trailing or dropped short function words remain inside the active
     * speech energy region for unclear-word fallback detection.
     */
    fun buildSpokenWordsFromTranscript(
        transcript: String,
        energyWindows: List<AudioEnergyWindow> = emptyList(),
        confidence: Float = 0.92f,
        expectedWordCount: Int = 0
    ): List<SpokenWordTiming> {
        val tokens = formatTranscriptWithUnk(transcript)
            .split(Regex("\\s+"))
            .map { normalizeToken(it) }
            .filter { it.isNotBlank() }
        if (tokens.isEmpty()) return emptyList()

        val clampedConf = confidence.coerceIn(0.65f, 1.0f)
        val (speechStart, speechEnd) = if (energyWindows.isNotEmpty()) {
            val sortedRms = energyWindows.map { it.rms }.sorted()
            val noiseFloor = sortedRms.getOrElse((sortedRms.size * 0.15).toInt()) { 120f }.coerceIn(80f, 260f)
            val energyThreshold = maxOf(250f, noiseFloor * 1.75f)
            val speechWins = energyWindows.filter { it.rms >= energyThreshold }
            val start = speechWins.firstOrNull()?.startSec ?: energyWindows.first().startSec
            val end = (speechWins.lastOrNull()?.endSec ?: energyWindows.last().endSec)
                .coerceAtLeast(start + tokens.size * 0.12f)
            start to end
        } else {
            0f to (tokens.size * 0.35f)
        }

        val fullSpan = (speechEnd - speechStart).coerceAtLeast(tokens.size * 0.12f)
        val effectiveSpan = if (expectedWordCount > tokens.size && expectedWordCount > 0) {
            val ratio = (tokens.size.toFloat() / expectedWordCount.toFloat()).coerceIn(0.35f, 0.96f)
            (fullSpan * ratio).coerceAtLeast(tokens.size * 0.10f)
        } else {
            fullSpan
        }

        val weights = tokens.map { it.length.coerceIn(2, 10).toFloat() }
        val totalWeight = weights.sum().coerceAtLeast(1f)
        var cursor = speechStart

        return tokens.mapIndexed { idx, token ->
            val slice = effectiveSpan * (weights[idx] / totalWeight)
            val gap = if (idx < tokens.lastIndex) (slice * 0.14f).coerceIn(0.015f, 0.05f) else 0f
            val wordStart = cursor
            val wordEnd = if (idx == tokens.lastIndex) {
                (speechStart + effectiveSpan).coerceAtLeast(wordStart + 0.04f)
            } else {
                (cursor + slice - gap).coerceAtLeast(wordStart + 0.04f)
            }
            cursor = (wordEnd + gap)
            SpokenWordTiming(
                word = token,
                startSec = wordStart,
                endSec = wordEnd,
                conf = clampedConf
            )
        }
    }

    fun serializeAlignedTimings(aligned: List<AlignedWordFeedback>): String {
        val arr = JSONArray()
        aligned.forEach { item ->
            val obj = JSONObject()
            obj.put("targetWord", item.targetWord)
            obj.put("spokenWord", item.spokenWord ?: "")
            obj.put("status", item.status.name)
            if (item.startSec != null) obj.put("startSec", item.startSec.toDouble())
            if (item.endSec != null) obj.put("endSec", item.endSec.toDouble())
            if (item.confidence != null) obj.put("confidence", item.confidence.toDouble())
            obj.put("isUnclear", item.isUnclear)
            arr.put(obj)
        }
        return arr.toString()
    }

    private data class ExpandedTargetToken(
        val word: String,
        val displayIndex: Int,
        val equivalents: Set<String> = emptySet()
    )

    private data class SingleAlignmentOutcome(
        val perTarget: List<AlignedWordFeedback>,
        val extras: List<AlignedWordFeedback>
    )

    /**
     * Evaluate pronunciation using the honest free recognizer transcript as primary,
     * optionally boosted by the grammar hint recognizer and audio RMS energy windows.
     */
    fun evaluatePronunciation(
        targetDisplayWords: List<String>,
        spokenWords: List<SpokenWordTiming>,
        itemType: String = "",
        grammarWords: List<SpokenWordTiming> = emptyList(),
        energyWindows: List<AudioEnergyWindow> = emptyList()
    ): PronunciationEvaluationResult {
        if (targetDisplayWords.isEmpty()) {
            return PronunciationEvaluationResult(0, emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        }

        val cleanTargets = targetDisplayWords.map { normalizeToken(SpokenTextNormalizer.normalizeToSpokenWords(it)) }
        val targetContractions = cleanTargets.filter { CONTRACTIONS.containsKey(it) }.toSet()
        val targetInformalReductions = cleanTargets.filter { INFORMAL_SPOKEN_REDUCTIONS.containsKey(it) }.toSet()
        val allowYaForYou = targetInformalReductions.any { key ->
            INFORMAL_SPOKEN_REDUCTIONS[key]?.contains("you") == true
        }

        val expandedTargets = mutableListOf<ExpandedTargetToken>()
        targetDisplayWords.forEachIndexed { idx, rawDisplay ->
            val droppedGSet = droppedGEquivalentsForRawTarget(rawDisplay)
            val spokenForm = SpokenTextNormalizer.normalizeToSpokenWords(rawDisplay)
            val subTokens = spokenForm
                .split(Regex("\\s+"))
                .map { normalizeToken(it) }
                .filter { it.isNotBlank() }
                .flatMap { expandNormalizedWord(it, targetContractions, targetInformalReductions) }
            if (subTokens.isEmpty()) {
                expandedTargets.add(ExpandedTargetToken("", idx))
            } else {
                subTokens.forEach { tok ->
                    val tokenEquivs = when {
                        droppedGSet.isNotEmpty() -> droppedGSet
                        allowYaForYou && tok == "you" -> setOf("you", "ya")
                        else -> emptySet()
                    }
                    expandedTargets.add(ExpandedTargetToken(tok, idx, tokenEquivs))
                }
            }
        }

        val freeOutcome = alignAgainstExpandedTargets(
            targetDisplayWords = targetDisplayWords,
            cleanTargets = cleanTargets,
            expandedTargets = expandedTargets,
            spokenWords = spokenWords,
            targetContractions = targetContractions,
            targetInformalReductions = targetInformalReductions,
            itemType = itemType
        )

        val grammarOutcome = if (grammarWords.isNotEmpty()) {
            alignAgainstExpandedTargets(
                targetDisplayWords = targetDisplayWords,
                cleanTargets = cleanTargets,
                expandedTargets = expandedTargets,
                spokenWords = grammarWords,
                targetContractions = targetContractions,
                targetInformalReductions = targetInformalReductions,
                itemType = itemType
            )
        } else {
            null
        }

        // Merge: count a word as matched if EITHER recognizer heard it;
        // drop to missed/unclear only if BOTH recognizers missed it.
        val mergedPerTarget = targetDisplayWords.indices.map { idx ->
            val freeItem = freeOutcome.perTarget[idx]
            val gramItem = grammarOutcome?.perTarget?.getOrNull(idx)
            pickBetterTargetFeedback(freeItem, gramItem)
        }.toMutableList()

        // Apply RMS energy fallback ONLY for short unstressed words missed by BOTH recognizers
        if (energyWindows.isNotEmpty()) {
            applyEnergyUnclearFallback(
                mergedPerTarget = mergedPerTarget,
                cleanTargets = cleanTargets,
                energyWindows = energyWindows
            )
        }

        val finalDetails = mutableListOf<AlignedWordFeedback>()
        val uiWordScores = mutableListOf<Pair<String, WordScoreType>>()
        val missedWords = mutableListOf<String>()
        val unclearWords = mutableListOf<String>()
        val extraWords = mutableListOf<String>()

        var weightedScoreSum = 0.0

        mergedPerTarget.forEachIndexed { idx, feedback ->
            finalDetails.add(feedback)
            val cleanTarget = cleanTargets[idx].ifBlank { feedback.targetWord }
            when (feedback.status) {
                WordAlignmentStatus.Correct -> {
                    if (feedback.isUnclear) {
                        weightedScoreSum += 0.65
                        unclearWords.add(feedback.targetWord)
                        uiWordScores.add(feedback.targetWord to WordScoreType.Hesitant)
                    } else {
                        weightedScoreSum += 1.0
                        uiWordScores.add(feedback.targetWord to WordScoreType.Correct)
                    }
                }
                WordAlignmentStatus.Close -> {
                    weightedScoreSum += if (feedback.isUnclear) 0.6 else 0.5
                    if (feedback.isUnclear) {
                        unclearWords.add(feedback.targetWord)
                    }
                    uiWordScores.add(feedback.targetWord to WordScoreType.Hesitant)
                }
                WordAlignmentStatus.Missed -> {
                    missedWords.add(cleanTarget)
                    uiWordScores.add(feedback.targetWord to WordScoreType.Incorrect)
                }
                WordAlignmentStatus.Extra -> Unit
            }
        }

        // Keep genuine extra spoken words from the free transcript (filtering out tokens
        // that overlap a target word already rescued by the grammar recognizer).
        val rescuedWindows = mergedPerTarget.mapIndexedNotNull { idx, item ->
            val freeItem = freeOutcome.perTarget[idx]
            if (freeItem.status == WordAlignmentStatus.Missed &&
                item.status != WordAlignmentStatus.Missed &&
                item.startSec != null && item.endSec != null
            ) {
                item.startSec to item.endSec
            } else {
                null
            }
        }

        freeOutcome.extras.forEach { extra ->
            val overlapsRescued = extra.startSec != null && extra.endSec != null && rescuedWindows.any { (rStart, rEnd) ->
                val overlapStart = maxOf(extra.startSec, rStart)
                val overlapEnd = minOf(extra.endSec, rEnd)
                val extraDur = (extra.endSec - extra.startSec).coerceAtLeast(0.01f)
                (overlapEnd - overlapStart) / extraDur >= 0.4f
            }
            if (!overlapsRescued) {
                val word = extra.spokenWord.orEmpty()
                if (word.isNotBlank()) {
                    extraWords.add(word)
                }
                finalDetails.add(extra)
            }
        }

        val score = ((weightedScoreSum / targetDisplayWords.size.toDouble()) * 100.0)
            .toInt()
            .coerceIn(0, 100)

        return PronunciationEvaluationResult(
            score = score,
            wordScores = uiWordScores,
            alignedDetails = finalDetails,
            missedWords = missedWords.distinct(),
            unclearWords = unclearWords.distinct(),
            extraWords = extraWords.distinct()
        )
    }

    private fun alignAgainstExpandedTargets(
        targetDisplayWords: List<String>,
        cleanTargets: List<String>,
        expandedTargets: List<ExpandedTargetToken>,
        spokenWords: List<SpokenWordTiming>,
        targetContractions: Set<String>,
        targetInformalReductions: Set<String>,
        itemType: String
    ): SingleAlignmentOutcome {
        val expandedSpoken = mutableListOf<SpokenWordTiming>()
        spokenWords.forEach { sw ->
            val spokenNormalized = SpokenTextNormalizer.normalizeToSpokenWords(sw.word)
            val parts = spokenNormalized
                .split(Regex("\\s+"))
                .map { normalizeToken(it) }
                .filter { it.isNotBlank() }
                .flatMap { expandNormalizedWord(it, targetContractions, targetInformalReductions) }
            parts.forEach { tok ->
                expandedSpoken.add(sw.copy(word = tok))
            }
        }

        if (expandedSpoken.isEmpty()) {
            val missedList = targetDisplayWords.map { display ->
                AlignedWordFeedback(
                    targetWord = display,
                    spokenWord = null,
                    status = WordAlignmentStatus.Missed
                )
            }
            return SingleAlignmentOutcome(perTarget = missedList, extras = emptyList())
        }

        val n = expandedTargets.size
        val m = expandedSpoken.size

        val dp = Array(n + 1) { FloatArray(m + 1) }
        val back = Array(n + 1) { IntArray(m + 1) } // 0 = diag, 1 = up (missed target), 2 = left (extra spoken)

        for (i in 1..n) {
            dp[i][0] = i.toFloat()
            back[i][0] = 1
        }
        for (j in 1..m) {
            dp[0][j] = j.toFloat()
            back[0][j] = 2
        }

        for (i in 1..n) {
            for (j in 1..m) {
                val targetTok = expandedTargets[i - 1]
                val subCost = substitutionCost(
                    target = targetTok.word,
                    spoken = expandedSpoken[j - 1].word,
                    itemType = itemType,
                    targetEquivalents = targetTok.equivalents
                )
                val diag = dp[i - 1][j - 1] + subCost
                val up = dp[i - 1][j] + 1.0f
                val left = dp[i][j - 1] + 1.0f

                when {
                    diag <= up && diag <= left -> {
                        dp[i][j] = diag
                        back[i][j] = 0
                    }
                    up <= left -> {
                        dp[i][j] = up
                        back[i][j] = 1
                    }
                    else -> {
                        dp[i][j] = left
                        back[i][j] = 2
                    }
                }
            }
        }

        val reversedOps = mutableListOf<Pair<Int?, Int?>>()
        var i = n
        var j = m
        while (i > 0 || j > 0) {
            when {
                i > 0 && j > 0 && back[i][j] == 0 -> {
                    reversedOps.add((i - 1) to (j - 1))
                    i--
                    j--
                }
                i > 0 && (j == 0 || back[i][j] == 1) -> {
                    reversedOps.add((i - 1) to null)
                    i--
                }
                else -> {
                    reversedOps.add(null to (j - 1))
                    j--
                }
            }
        }
        val ops = reversedOps.asReversed()

        val perDisplayParts = Array(targetDisplayWords.size) { mutableListOf<AlignedWordFeedback>() }
        val extras = mutableListOf<AlignedWordFeedback>()

        for ((tIdx, sIdx) in ops) {
            if (tIdx != null && sIdx != null) {
                val targetTok = expandedTargets[tIdx]
                val displayWord = targetDisplayWords[targetTok.displayIndex]
                val spoken = expandedSpoken[sIdx]
                val isUnclear = spoken.conf < UNCLEAR_CONFIDENCE_THRESHOLD

                val exact = targetTok.word == spoken.word ||
                    targetTok.equivalents.contains(spoken.word) ||
                    isPhoneticMatch(targetTok.word, spoken.word, itemType)
                val editDist = if (spoken.word == UNKNOWN_WORD_DISPLAY) Int.MAX_VALUE else levenshtein(targetTok.word, spoken.word)
                val maxCloseDist = if (targetTok.word.length >= 5) 2 else 1
                val isPrefixStem = spoken.word != UNKNOWN_WORD_DISPLAY &&
                    targetTok.word.length > 3 && spoken.word.length > 3 &&
                    (targetTok.word.startsWith(spoken.word) || spoken.word.startsWith(targetTok.word))

                when {
                    exact -> {
                        perDisplayParts[targetTok.displayIndex].add(
                            AlignedWordFeedback(
                                targetWord = displayWord,
                                spokenWord = spoken.word,
                                status = WordAlignmentStatus.Correct,
                                startSec = spoken.startSec,
                                endSec = spoken.endSec,
                                confidence = spoken.conf,
                                isUnclear = isUnclear
                            )
                        )
                    }
                    editDist <= maxCloseDist || isPrefixStem -> {
                        perDisplayParts[targetTok.displayIndex].add(
                            AlignedWordFeedback(
                                targetWord = displayWord,
                                spokenWord = spoken.word,
                                status = WordAlignmentStatus.Close,
                                startSec = spoken.startSec,
                                endSec = spoken.endSec,
                                confidence = spoken.conf,
                                isUnclear = isUnclear
                            )
                        )
                    }
                    spoken.word == UNKNOWN_WORD_DISPLAY && isShortUnstressedWord(targetTok.word) -> {
                        perDisplayParts[targetTok.displayIndex].add(
                            AlignedWordFeedback(
                                targetWord = displayWord,
                                spokenWord = spoken.word,
                                status = WordAlignmentStatus.Close,
                                startSec = spoken.startSec,
                                endSec = spoken.endSec,
                                confidence = spoken.conf,
                                isUnclear = true
                            )
                        )
                    }
                    else -> {
                        perDisplayParts[targetTok.displayIndex].add(
                            AlignedWordFeedback(
                                targetWord = displayWord,
                                spokenWord = null,
                                status = WordAlignmentStatus.Missed
                            )
                        )
                        extras.add(
                            AlignedWordFeedback(
                                targetWord = "",
                                spokenWord = spoken.word,
                                status = WordAlignmentStatus.Extra,
                                startSec = spoken.startSec,
                                endSec = spoken.endSec,
                                confidence = spoken.conf,
                                isUnclear = isUnclear
                            )
                        )
                    }
                }
            } else if (tIdx != null) {
                val targetTok = expandedTargets[tIdx]
                perDisplayParts[targetTok.displayIndex].add(
                    AlignedWordFeedback(
                        targetWord = targetDisplayWords[targetTok.displayIndex],
                        spokenWord = null,
                        status = WordAlignmentStatus.Missed
                    )
                )
            } else if (sIdx != null) {
                val spoken = expandedSpoken[sIdx]
                extras.add(
                    AlignedWordFeedback(
                        targetWord = "",
                        spokenWord = spoken.word,
                        status = WordAlignmentStatus.Extra,
                        startSec = spoken.startSec,
                        endSec = spoken.endSec,
                        confidence = spoken.conf,
                        isUnclear = spoken.conf < UNCLEAR_CONFIDENCE_THRESHOLD
                    )
                )
            }
        }

        // Collapse expanded contraction sub-tokens back onto each display word
        val collapsedPerTarget = targetDisplayWords.mapIndexed { idx, displayWord ->
            val parts = perDisplayParts[idx]
            when {
                parts.isEmpty() -> AlignedWordFeedback(displayWord, null, WordAlignmentStatus.Missed)
                parts.size == 1 -> parts.first()
                else -> {
                    val matchedParts = parts.filter { it.status != WordAlignmentStatus.Missed }
                    when {
                        matchedParts.isEmpty() -> AlignedWordFeedback(displayWord, null, WordAlignmentStatus.Missed)
                        matchedParts.size == parts.size && matchedParts.all { it.status == WordAlignmentStatus.Correct } -> {
                            AlignedWordFeedback(
                                targetWord = displayWord,
                                spokenWord = matchedParts.mapNotNull { it.spokenWord }.joinToString(" "),
                                status = WordAlignmentStatus.Correct,
                                startSec = matchedParts.mapNotNull { it.startSec }.minOrNull(),
                                endSec = matchedParts.mapNotNull { it.endSec }.maxOrNull(),
                                confidence = matchedParts.mapNotNull { it.confidence }.minOrNull(),
                                isUnclear = matchedParts.any { it.isUnclear }
                            )
                        }
                        else -> {
                            AlignedWordFeedback(
                                targetWord = displayWord,
                                spokenWord = matchedParts.mapNotNull { it.spokenWord }.joinToString(" "),
                                status = WordAlignmentStatus.Close,
                                startSec = matchedParts.mapNotNull { it.startSec }.minOrNull(),
                                endSec = matchedParts.mapNotNull { it.endSec }.maxOrNull(),
                                confidence = matchedParts.mapNotNull { it.confidence }.minOrNull(),
                                isUnclear = matchedParts.any { it.isUnclear } || matchedParts.size < parts.size
                            )
                        }
                    }
                }
            }
        }

        return SingleAlignmentOutcome(perTarget = collapsedPerTarget, extras = extras)
    }

    private fun statusRank(feedback: AlignedWordFeedback?): Int {
        if (feedback == null) return 0
        return when {
            feedback.status == WordAlignmentStatus.Correct && !feedback.isUnclear -> 4
            feedback.status == WordAlignmentStatus.Correct && feedback.isUnclear -> 3
            feedback.status == WordAlignmentStatus.Close -> 2
            else -> 1
        }
    }

    private fun pickBetterTargetFeedback(
        freeItem: AlignedWordFeedback,
        grammarItem: AlignedWordFeedback?
    ): AlignedWordFeedback {
        if (grammarItem == null) return freeItem
        return if (statusRank(grammarItem) > statusRank(freeItem)) grammarItem else freeItem
    }

    private fun applyEnergyUnclearFallback(
        mergedPerTarget: MutableList<AlignedWordFeedback>,
        cleanTargets: List<String>,
        energyWindows: List<AudioEnergyWindow>
    ) {
        val sortedRms = energyWindows.map { it.rms }.sorted()
        val noiseFloor = sortedRms.getOrElse((sortedRms.size * 0.15).toInt()) { 120f }.coerceIn(80f, 260f)
        val energyThreshold = maxOf(250f, noiseFloor * 1.75f)
        val speechWins = energyWindows.filter { it.rms >= energyThreshold }
        val recordingStartSec = speechWins.firstOrNull()?.startSec ?: energyWindows.first().startSec
        val recordingEndSec = speechWins.lastOrNull()?.endSec ?: energyWindows.last().endSec

        for (idx in mergedPerTarget.indices) {
            val current = mergedPerTarget[idx]
            if (current.status != WordAlignmentStatus.Missed) continue
            if (!isShortUnstressedWord(cleanTargets[idx])) continue

            var prevAnchorEndSec = recordingStartSec
            var missedBeforeInGap = 0
            for (k in idx - 1 downTo 0) {
                val candidate = mergedPerTarget[k]
                if (candidate.status != WordAlignmentStatus.Missed && candidate.endSec != null) {
                    prevAnchorEndSec = candidate.endSec
                    break
                } else {
                    missedBeforeInGap++
                }
            }

            var nextAnchorStartSec = recordingEndSec
            var missedAfterInGap = 0
            for (k in idx + 1 until mergedPerTarget.size) {
                val candidate = mergedPerTarget[k]
                if (candidate.status != WordAlignmentStatus.Missed && candidate.startSec != null) {
                    nextAnchorStartSec = candidate.startSec
                    break
                } else {
                    missedAfterInGap++
                }
            }

            if (nextAnchorStartSec <= prevAnchorEndSec) continue

            val totalMissedInGap = (missedBeforeInGap + 1 + missedAfterInGap).coerceAtLeast(1)
            val gapDuration = (nextAnchorStartSec - prevAnchorEndSec).coerceAtLeast(0.06f)
            val slotDuration = gapDuration / totalMissedInGap
            val slotStart = prevAnchorEndSec + missedBeforeInGap * slotDuration
            val slotEnd = prevAnchorEndSec + (missedBeforeInGap + 1) * slotDuration

            // Stay strictly inside [prevAnchorEndSec, nextAnchorStartSec] so adjacent clear words don't bleed energy
            val queryStart = (slotStart - 0.03f).coerceAtLeast(prevAnchorEndSec)
            val queryEnd = (slotEnd + 0.03f).coerceAtMost(nextAnchorStartSec)

            var overlapDurSum = 0f
            var weightedRmsSum = 0f
            var peakInsideRms = 0f
            for (win in energyWindows) {
                val overlap = minOf(win.endSec, queryEnd) - maxOf(win.startSec, queryStart)
                if (overlap > 0.005f) {
                    overlapDurSum += overlap
                    weightedRmsSum += overlap * win.rms
                    if (win.rms > peakInsideRms) {
                        peakInsideRms = win.rms
                    }
                }
            }
            val effectiveRms = if (overlapDurSum > 0f) {
                maxOf(weightedRmsSum / overlapDurSum, peakInsideRms * 0.9f)
            } else {
                0f
            }

            if (effectiveRms >= energyThreshold) {
                mergedPerTarget[idx] = current.copy(
                    status = WordAlignmentStatus.Close,
                    startSec = slotStart,
                    endSec = slotEnd,
                    isUnclear = true
                )
            }
        }
    }

    private fun substitutionCost(
        target: String,
        spoken: String,
        itemType: String,
        targetEquivalents: Set<String> = emptySet()
    ): Float {
        if (spoken == UNKNOWN_WORD_DISPLAY) {
            return if (isShortUnstressedWord(target)) 0.85f else 2.0f
        }
        if (target == spoken || targetEquivalents.contains(spoken) || isPhoneticMatch(target, spoken, itemType)) return 0f
        val dist = levenshtein(target, spoken)
        val maxClose = if (target.length >= 5) 2 else 1
        if (dist <= maxClose) return 0.45f
        if (target.length > 3 && spoken.length > 3 && (target.startsWith(spoken) || spoken.startsWith(target))) {
            return 0.5f
        }
        return 1.35f
    }

    fun levenshtein(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[len1][len2]
    }

    fun isPhoneticMatch(target: String, recognized: String, itemType: String = ""): Boolean {
        val t = target.lowercase(Locale.US).trim()
        val r = recognized.lowercase(Locale.US).trim()
        if (t == r) return true

        val generalHomophones = mapOf(
            "their" to setOf("there", "they're"),
            "there" to setOf("their", "they're"),
            "to" to setOf("too", "two"),
            "too" to setOf("to", "two"),
            "two" to setOf("to", "too"),
            "for" to setOf("four"),
            "four" to setOf("for"),
            "your" to setOf("you're"),
            "you're" to setOf("your"),
            "its" to setOf("it's"),
            "it's" to setOf("its"),
            "know" to setOf("no"),
            "no" to setOf("know"),
            "right" to setOf("write"),
            "write" to setOf("right"),
            "hear" to setOf("here"),
            "here" to setOf("hear"),
            "weather" to setOf("whether"),
            "whether" to setOf("weather"),
            "wanna" to setOf("want"),
            "gonna" to setOf("going"),
            "been" to setOf("bin", "bean"),
            "have" to setOf("of")
        )
        if (generalHomophones[t]?.contains(r) == true) return true
        if (itemType != "tongue_twister") return false

        val twisterEquivalents = mapOf(
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
            "fought" to setOf("fort"),
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
        return twisterEquivalents[t]?.contains(r) == true
    }
}
