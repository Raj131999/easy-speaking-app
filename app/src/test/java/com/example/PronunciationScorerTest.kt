package com.example

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.DailySentence
import com.example.data.EnglishRepository
import com.example.ui.AudioEnergyWindow
import com.example.ui.EnglishViewModel
import com.example.ui.PronunciationScorer
import com.example.ui.Screen
import com.example.ui.SpeechModelOption
import com.example.ui.SpokenWordTiming
import com.example.ui.WordAlignmentStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PronunciationScorerTest {

    @Test
    fun defaultSpeechModelIsIndianEnglishAndUsIsSelectable() {
        assertEquals(SpeechModelOption.INDIA, SpeechModelOption.fromId(null))
        assertEquals(SpeechModelOption.INDIA, SpeechModelOption.fromId("unknown"))
        assertEquals(SpeechModelOption.INDIA, SpeechModelOption.fromId("en-in"))
        assertEquals(SpeechModelOption.US, SpeechModelOption.fromId("en-us"))

        SpeechModelOption.entries.forEach { option ->
            assertEquals("vosk-model-en-us-0.22-lgraph", option.folderName)
            assertEquals("https://alphacephei.com/vosk/models/vosk-model-en-us-0.22-lgraph.zip", option.downloadUrl)
            val uiText = "${option.displayName} ${option.subtitle}".lowercase()
            assertFalse("UI copy must not mention vosk", uiText.contains("vosk"))
            assertFalse("UI copy must not mention 0.22", uiText.contains("0.22"))
            assertFalse("UI copy must not mention lgraph", uiText.contains("lgraph"))
        }
    }

    @Test
    fun viewModelNavigationAndSubStateSurviveAppSwitchRecreation() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("easy_speaking_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        val db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.englishDao()
        val sentence = DailySentence(
            id = 42,
            text = "How have you been?",
            translation = "Casual greeting",
            category = "Greetings",
            difficulty = "Beginner"
        )
        runBlocking {
            dao.insertDailySentences(listOf(sentence))
        }
        val repo = EnglishRepository(dao)

        val vm1 = EnglishViewModel(app, repo)
        vm1.navigateTo(Screen.DailySentences)
        vm1.activeDailySentence.value = sentence
        vm1.grammarSelectedTab.value = 1
        vm1.grammarSelectedCategory.value = "Modals"
        vm1.setAccent("US")
        vm1.setPlaybackSpeed(1.25f)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        vm1.releaseResources()

        val vm2 = EnglishViewModel(app, repo)
        assertEquals(Screen.DailySentences, vm2.currentScreen.value)
        assertEquals(listOf(Screen.Home, Screen.DailySentences), vm2.screenStack.value)
        assertEquals(1, vm2.grammarSelectedTab.value)
        assertEquals("Modals", vm2.grammarSelectedCategory.value)
        assertEquals("US", vm2.selectedAccent.value)
        assertEquals(1.25f, vm2.selectedSpeed.value, 0.001f)

        // Allow Room Flow restoration coroutine to emit activeDailySentence
        val deadline = System.currentTimeMillis() + 2000L
        while (vm2.activeDailySentence.value == null && System.currentTimeMillis() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(20)
        }
        assertEquals(42, vm2.activeDailySentence.value?.id)

        vm2.releaseResources()
        db.close()
    }

    @Test
    fun buildGrammarJsonStripsPunctuationDeduplicatesAndIncludesFullPhrase() {
        val grammar = PronunciationScorer.buildGrammarJson(
            "Hello, world! Hello again — let's speak clearly."
        )!!
        assertEquals(
            "[\"hello world hello again let's speak clearly\",\"hello\",\"world\",\"again\",\"let's\",\"speak\",\"clearly\",\"[unk]\"]",
            grammar
        )
    }

    @Test
    fun parseVoskWordTimingsExtractsTimestampsAndSurfacesUnkAsQuestionMarks() {
        val voskJson = """
            {
              "result": [
                {"conf": 0.96, "end": 0.48, "start": 0.12, "word": "she"},
                {"conf": 0.45, "end": 0.95, "start": 0.51, "word": "sells"},
                {"conf": 0.80, "end": 1.10, "start": 0.96, "word": "[unk]"},
                {"conf": 0.92, "end": 1.65, "start": 1.15, "word": "seashells"}
              ],
              "text": "she sells [unk] seashells"
            }
        """.trimIndent()

        val (text, timings) = PronunciationScorer.parseVoskWordTimings(voskJson)
        assertEquals("she sells ??? seashells", text)
        assertEquals(4, timings.size)
        assertEquals("sells", timings[1].word)
        assertEquals(0.51f, timings[1].startSec, 0.001f)
        assertEquals(0.95f, timings[1].endSec, 0.001f)
        assertEquals(0.45f, timings[1].conf, 0.001f)
        assertEquals("???", timings[2].word)
    }

    @Test
    fun evaluatePronunciationFlagsUnclearMissedAndExtraWordsWithTimestamps() {
        val targetWords = listOf("I", "would", "like", "two", "tickets")
        val spoken = listOf(
            SpokenWordTiming("i", 0.10f, 0.25f, 0.98f),
            SpokenWordTiming("um", 0.26f, 0.40f, 0.85f),       // Extra
            SpokenWordTiming("would", 0.42f, 0.72f, 0.52f),    // Clear text match but conf < 0.6 -> Unclear
            SpokenWordTiming("like", 0.75f, 1.05f, 0.94f),
            // "two" missed
            SpokenWordTiming("ticket", 1.10f, 1.55f, 0.88f)    // Close match (edit dist 1)
        )

        val result = PronunciationScorer.evaluatePronunciation(
            targetDisplayWords = targetWords,
            spokenWords = spoken,
            itemType = "daily_sentence"
        )

        assertTrue(result.missedWords.contains("two"))
        assertTrue(result.unclearWords.contains("would"))
        assertTrue(result.extraWords.contains("um"))

        val wouldFeedback = result.alignedDetails.first { it.targetWord == "would" }
        assertEquals(WordAlignmentStatus.Correct, wouldFeedback.status)
        assertTrue(wouldFeedback.isUnclear)
        assertEquals(0.42f, wouldFeedback.startSec ?: 0f, 0.001f)

        val ticketFeedback = result.alignedDetails.first { it.targetWord == "tickets" }
        assertEquals(WordAlignmentStatus.Close, ticketFeedback.status)
        assertFalse(ticketFeedback.isUnclear)
    }

    @Test
    fun evaluatePronunciationMatchesContractionsInBothDirectionsAndShowsUnkAsExtra() {
        // Target uses "It's", free recognizer outputs "it is" + "[unk]" (converted to "???")
        val targetFromContraction = listOf("It's", "great", "to", "see", "you")
        val spokenExpanded = listOf(
            SpokenWordTiming("it", 0.10f, 0.22f, 0.95f),
            SpokenWordTiming("is", 0.23f, 0.36f, 0.94f),
            SpokenWordTiming("great", 0.38f, 0.70f, 0.97f),
            SpokenWordTiming("???", 0.71f, 0.82f, 0.50f),
            SpokenWordTiming("to", 0.83f, 0.95f, 0.92f),
            SpokenWordTiming("see", 0.96f, 1.20f, 0.96f),
            SpokenWordTiming("you", 1.22f, 1.45f, 0.95f)
        )

        val res1 = PronunciationScorer.evaluatePronunciation(targetFromContraction, spokenExpanded)
        assertEquals(100, res1.score)
        assertTrue(res1.missedWords.isEmpty())
        assertTrue(res1.extraWords.contains("???"))
        assertEquals(WordAlignmentStatus.Correct, res1.alignedDetails.first { it.targetWord == "It's" }.status)

        // Target uses "It is", recognizer outputs "it's"
        val targetExpanded = listOf("It", "is", "great")
        val spokenContraction = listOf(
            SpokenWordTiming("it's", 0.10f, 0.35f, 0.96f),
            SpokenWordTiming("great", 0.38f, 0.70f, 0.97f)
        )
        val res2 = PronunciationScorer.evaluatePronunciation(targetExpanded, spokenContraction)
        assertEquals(100, res2.score)
        assertTrue(res2.missedWords.isEmpty())
    }

    @Test
    fun dualRecognizerRescuesMissedWordWhileKeepingHonestExtraWords() {
        val target = listOf("It's", "great", "to", "see", "you", "again")
        // Free recognizer missed "you" and heard an extra "well" at start
        val freeSpoken = listOf(
            SpokenWordTiming("well", 0.05f, 0.25f, 0.90f),
            SpokenWordTiming("it's", 0.30f, 0.55f, 0.95f),
            SpokenWordTiming("great", 0.58f, 0.90f, 0.96f),
            SpokenWordTiming("to", 0.92f, 1.05f, 0.92f),
            SpokenWordTiming("see", 1.08f, 1.30f, 0.94f),
            SpokenWordTiming("again", 1.50f, 1.88f, 0.93f)
        )
        // Grammar hint recognizer caught "you"
        val grammarSpoken = listOf(
            SpokenWordTiming("it's", 0.30f, 0.55f, 0.92f),
            SpokenWordTiming("great", 0.58f, 0.90f, 0.94f),
            SpokenWordTiming("to", 0.92f, 1.05f, 0.90f),
            SpokenWordTiming("see", 1.08f, 1.30f, 0.93f),
            SpokenWordTiming("you", 1.32f, 1.48f, 0.88f),
            SpokenWordTiming("again", 1.50f, 1.88f, 0.91f)
        )

        val result = PronunciationScorer.evaluatePronunciation(
            targetDisplayWords = target,
            spokenWords = freeSpoken,
            grammarWords = grammarSpoken
        )

        assertEquals(100, result.score)
        assertTrue(result.missedWords.isEmpty())
        assertTrue(result.extraWords.contains("well"))
        assertEquals(WordAlignmentStatus.Correct, result.alignedDetails.first { it.targetWord == "you" }.status)
    }

    @Test
    fun trailingMissedShortWordsBecomeUnclearWhenAudioHasEnergyAndMissedWhenSilent() {
        val target = listOf("It's", "great", "to", "see", "you", "again", "How", "have", "you", "been")
        val freeSpoken = listOf(
            SpokenWordTiming("it's", 0.20f, 0.45f, 0.96f),
            SpokenWordTiming("great", 0.48f, 0.80f, 0.95f),
            SpokenWordTiming("to", 0.82f, 0.95f, 0.94f),
            SpokenWordTiming("see", 0.98f, 1.22f, 0.96f),
            // "you" missed around 1.22..1.40
            SpokenWordTiming("again", 1.40f, 1.80f, 0.94f),
            SpokenWordTiming("how", 1.90f, 2.20f, 0.93f)
            // "have you been" dropped at tail 2.20..3.10
        )

        val speechEnergyWindows = listOf(
            AudioEnergyWindow(0.0f, 0.2f, 110f),   // noise floor
            AudioEnergyWindow(0.2f, 1.2f, 1400f),
            AudioEnergyWindow(1.2f, 1.4f, 950f),   // energy during "you"
            AudioEnergyWindow(1.4f, 2.2f, 1300f),
            AudioEnergyWindow(2.2f, 2.5f, 1100f),  // energy during "have"
            AudioEnergyWindow(2.5f, 2.8f, 1050f),  // energy during "you"
            AudioEnergyWindow(2.8f, 3.1f, 1200f)   // energy during "been"
        )

        val unclearResult = PronunciationScorer.evaluatePronunciation(
            targetDisplayWords = target,
            spokenWords = freeSpoken,
            energyWindows = speechEnergyWindows
        )

        assertTrue("Short unstressed words with audio energy should not be marked missed", unclearResult.missedWords.isEmpty())
        assertTrue(unclearResult.unclearWords.containsAll(listOf("you", "have", "been")))
        assertTrue("Score should be higher than harsh 60%", unclearResult.score >= 80)

        val silentTailWindows = listOf(
            AudioEnergyWindow(0.0f, 0.2f, 110f),
            AudioEnergyWindow(0.2f, 1.2f, 1400f),
            AudioEnergyWindow(1.2f, 1.4f, 120f),   // silent
            AudioEnergyWindow(1.4f, 2.2f, 1300f),
            AudioEnergyWindow(2.2f, 3.1f, 115f)    // silent tail
        )

        val missedResult = PronunciationScorer.evaluatePronunciation(
            targetDisplayWords = target,
            spokenWords = freeSpoken,
            energyWindows = silentTailWindows
        )

        assertTrue(missedResult.missedWords.containsAll(listOf("you", "have", "been")))
        assertTrue(missedResult.unclearWords.isEmpty())
    }

    @Test
    fun energyFallbackTrimsTrailingSilenceBeforeUserTappedStop() {
        val target = listOf("How", "have", "you", "been")
        val freeSpoken = listOf(
            SpokenWordTiming("how", 0.20f, 0.55f, 0.95f)
            // "have you been" spoken during 0.55..1.45, then 2.5s of silence while tapping stop
        )
        val windowsWithLongTrailingSilence = listOf(
            AudioEnergyWindow(0.00f, 0.20f, 110f),
            AudioEnergyWindow(0.20f, 0.55f, 1300f),
            AudioEnergyWindow(0.55f, 0.85f, 1050f),
            AudioEnergyWindow(0.85f, 1.15f, 1020f),
            AudioEnergyWindow(1.15f, 1.45f, 1100f),
            AudioEnergyWindow(1.45f, 2.50f, 115f),
            AudioEnergyWindow(2.50f, 4.00f, 105f)
        )

        val result = PronunciationScorer.evaluatePronunciation(
            targetDisplayWords = target,
            spokenWords = freeSpoken,
            energyWindows = windowsWithLongTrailingSilence
        )

        assertTrue("Long silence after speech ends must not push 'been' out of energy window", result.missedWords.isEmpty())
        assertTrue(result.unclearWords.containsAll(listOf("have", "you", "been")))
    }

    @Test
    fun unknownTokenInShortUnstressedSlotScoresAsUnclearInsteadOfMissed() {
        val target = listOf("It's", "great", "to", "see", "you", "again")
        val spokenWithUnk = listOf(
            SpokenWordTiming("it's", 0.15f, 0.40f, 0.96f),
            SpokenWordTiming("great", 0.42f, 0.75f, 0.95f),
            SpokenWordTiming("to", 0.77f, 0.90f, 0.94f),
            SpokenWordTiming("see", 0.92f, 1.18f, 0.96f),
            SpokenWordTiming("???", 1.20f, 1.38f, 0.65f),
            SpokenWordTiming("again", 1.40f, 1.78f, 0.94f)
        )

        val result = PronunciationScorer.evaluatePronunciation(
            targetDisplayWords = target,
            spokenWords = spokenWithUnk
        )

        assertTrue("Short unstressed target matched by ??? should not be missed", result.missedWords.isEmpty())
        assertTrue(result.unclearWords.contains("you"))
        assertFalse("Aligned ??? for short unstressed slot shouldn't leak into extraWords", result.extraWords.contains("???"))
        val youFeedback = result.alignedDetails.first { it.targetWord == "you" }
        assertEquals(WordAlignmentStatus.Close, youFeedback.status)
        assertTrue(youFeedback.isUnclear)
    }

    @Test
    fun compareGrammarModeVsOpenModeAcrossIndianAndUsAccentSamples() {
        data class AccentSample(
            val accent: SpeechModelOption,
            val targetText: String,
            val grammarModeSpoken: List<SpokenWordTiming>,
            val nonGrammarSpoken: List<SpokenWordTiming>
        )

        val samples = listOf(
            AccentSample(
                accent = SpeechModelOption.INDIA,
                targetText = "Please schedule the meeting for Thursday morning",
                grammarModeSpoken = listOf(
                    SpokenWordTiming("please", 0.1f, 0.4f, 0.93f),
                    SpokenWordTiming("schedule", 0.45f, 0.9f, 0.88f),
                    SpokenWordTiming("the", 0.92f, 1.05f, 0.97f),
                    SpokenWordTiming("meeting", 1.08f, 1.45f, 0.95f),
                    SpokenWordTiming("for", 1.48f, 1.65f, 0.91f),
                    SpokenWordTiming("thursday", 1.68f, 2.15f, 0.86f),
                    SpokenWordTiming("morning", 2.2f, 2.6f, 0.94f)
                ),
                nonGrammarSpoken = listOf(
                    SpokenWordTiming("please", 0.1f, 0.4f, 0.90f),
                    SpokenWordTiming("shedule", 0.45f, 0.9f, 0.54f),
                    SpokenWordTiming("the", 0.92f, 1.05f, 0.94f),
                    SpokenWordTiming("meeting", 1.08f, 1.45f, 0.92f),
                    SpokenWordTiming("four", 1.48f, 1.65f, 0.72f),
                    SpokenWordTiming("tuesday", 1.68f, 2.15f, 0.65f),
                    SpokenWordTiming("morning", 2.2f, 2.6f, 0.93f)
                )
            ),
            AccentSample(
                accent = SpeechModelOption.US,
                targetText = "Could you clarify the quarterly budget report",
                grammarModeSpoken = listOf(
                    SpokenWordTiming("could", 0.1f, 0.3f, 0.96f),
                    SpokenWordTiming("you", 0.32f, 0.48f, 0.97f),
                    SpokenWordTiming("clarify", 0.50f, 0.95f, 0.91f),
                    SpokenWordTiming("the", 0.98f, 1.1f, 0.95f),
                    SpokenWordTiming("quarterly", 1.12f, 1.62f, 0.89f),
                    SpokenWordTiming("budget", 1.65f, 2.05f, 0.94f),
                    SpokenWordTiming("report", 2.08f, 2.5f, 0.96f)
                ),
                nonGrammarSpoken = listOf(
                    SpokenWordTiming("could", 0.1f, 0.3f, 0.95f),
                    SpokenWordTiming("you", 0.32f, 0.48f, 0.96f),
                    SpokenWordTiming("glorify", 0.50f, 0.95f, 0.58f),
                    SpokenWordTiming("the", 0.98f, 1.1f, 0.95f),
                    SpokenWordTiming("quarter", 1.12f, 1.50f, 0.78f),
                    SpokenWordTiming("lee", 1.51f, 1.63f, 0.60f),
                    SpokenWordTiming("budget", 1.65f, 2.05f, 0.92f),
                    SpokenWordTiming("report", 2.08f, 2.5f, 0.95f)
                )
            )
        )

        var grammarErrorSum = 0.0
        var openErrorSum = 0.0

        samples.forEach { sample ->
            val displayWords = sample.targetText.split(" ")
            val grammarEval = PronunciationScorer.evaluatePronunciation(displayWords, sample.grammarModeSpoken)
            val openEval = PronunciationScorer.evaluatePronunciation(displayWords, sample.nonGrammarSpoken)

            val grammarErrRate = (100 - grammarEval.score) / 100.0
            val openErrRate = (100 - openEval.score) / 100.0
            grammarErrorSum += grammarErrRate
            openErrorSum += openErrRate

            assertTrue(
                "Expected grammar mode to outperform non-grammar mode for ${sample.accent}",
                grammarEval.score >= openEval.score
            )
        }

        // Grammar mode yields lower error rate on learner utterances and stays enabled by default
        assertTrue(grammarErrorSum < openErrorSum)
    }

    @Test
    fun androidSpeechTranscriptProducesMonotonicTimingsAndRespectsAccentMapping() {
        assertEquals("en-IN", com.example.ui.AndroidSpeechSession.languageTagForAccent("IN"))
        assertEquals("en-GB", com.example.ui.AndroidSpeechSession.languageTagForAccent("UK"))
        assertEquals("en-AU", com.example.ui.AndroidSpeechSession.languageTagForAccent("AU"))
        assertEquals("en-US", com.example.ui.AndroidSpeechSession.languageTagForAccent("US"))

        val energyWindows = listOf(
            AudioEnergyWindow(0.0f, 0.2f, 110f),   // noise floor
            AudioEnergyWindow(0.2f, 1.5f, 1250f),  // spoken words
            AudioEnergyWindow(1.5f, 2.4f, 1050f),  // trailing spoken short words
            AudioEnergyWindow(2.4f, 3.0f, 105f)    // trailing silence
        )

        // When SpeechRecognizer drops trailing short words ("have you been"), span scales so
        // energy fallback rescues those trailing short words inside [0.2, 2.4]
        val spokenWords = PronunciationScorer.buildSpokenWordsFromTranscript(
            transcript = "It's great to see again how",
            energyWindows = energyWindows,
            confidence = 0.94f,
            expectedWordCount = 10
        )

        assertEquals(6, spokenWords.size)
        for (i in 1 until spokenWords.size) {
            assertTrue(spokenWords[i].startSec >= spokenWords[i - 1].endSec)
        }
        assertTrue(spokenWords.first().startSec >= 0.2f)
        assertTrue(spokenWords.last().endSec < 2.4f)

        val target = listOf("It's", "great", "to", "see", "you", "again", "How", "have", "you", "been")
        val eval = PronunciationScorer.evaluatePronunciation(
            targetDisplayWords = target,
            spokenWords = spokenWords,
            itemType = "daily_sentence",
            energyWindows = energyWindows
        )

        assertTrue("Trailing spoken short words should be marked unclear instead of missed", eval.missedWords.isEmpty())
        assertTrue(eval.unclearWords.containsAll(listOf("have", "you", "been")))
        assertTrue(eval.score >= 80)
    }

    @Test
    fun defaultSpeakerAccentIsUnitedStates() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("easy_speaking_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        val db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val vm = EnglishViewModel(app, EnglishRepository(db.englishDao()))
        assertEquals("US", vm.selectedAccent.value)
        vm.releaseResources()
        db.close()
    }

    @Test
    fun spokenTextNormalizerConvertsNumbersCurrenciesAndSymbolsToWords() {
        val norm = com.example.ui.SpokenTextNormalizer::normalizeToSpokenWords

        assertEquals("It costs seven dollars.", norm("It costs 7$."))
        assertEquals("It costs seven dollars.", norm("It costs $7."))
        assertEquals("It costs one dollar.", norm("It costs $1."))
        assertEquals("Total is five dollars and fifty cents.", norm("Total is $5.50."))
        assertEquals("A ten dollars t-shirt.", norm("A $10 t-shirt."))
        assertEquals("They raised one million dollars.", norm("They raised $1 million."))
        assertEquals("About sixty five million dollars.", norm("About $65 million."))
        assertEquals("Salary is ten thousand dollars.", norm("Salary is $10K."))
        assertEquals("Save fifty percent today!", norm("Save 50% today!"))
        assertEquals("Meet at eight AM or nine thirty PM.", norm("Meet at 8:00 AM or 9:30 PM."))
        assertEquals("Call at nine oh five AM.", norm("Call at 9:05 AM."))
        assertEquals("In the eighteenth and twenty first century.", norm("In the 18th and 21ST century."))
        assertEquals("Back in the nineteen sixties and two thousands.", norm("Back in the 1960s and 2000s."))
        assertEquals("Born in nineteen ninety two, moving in two thousand seven to twenty fifty.", norm("Born in 1992, moving in 2007 to 2050."))
        assertEquals("Over twenty eight thousand users and ninety nine point nine uptime.", norm("Over 28,000 users and 99.9 uptime."))
        assertEquals("Watch three D movies with vitamin B twelve and CO two.", norm("Watch 3D movies with vitamin B12 and CO2."))
        assertEquals("Add three quarters cup at seventy two degrees fahrenheit.", norm("Add 3/4 cup at 72°F."))
        assertEquals("Rock and roll: two plus two equals four, number one!", norm("Rock & roll: 2 + 2 = 4, #1!"))
        assertEquals("Open twenty four seven.", norm("Open 24/7."))
    }

    @Test
    fun pronunciationScorerMatchesSpokenNumbersAndCurrencySymbolsInBothDirections() {
        val targetFromSymbols = PronunciationScorer.extractTargetWords("Total is $5.50.")
        assertEquals(listOf("total", "is", "five", "dollars", "and", "fifty", "cents"), targetFromSymbols)

        val spokenFromWords = PronunciationScorer.buildSpokenWordsFromTranscript(
            transcript = "total is five dollars and fifty cents",
            energyWindows = emptyList(),
            confidence = 0.95f
        )
        val eval1 = PronunciationScorer.evaluatePronunciation(targetFromSymbols, spokenFromWords)
        assertEquals(100, eval1.score)
        assertTrue(eval1.missedWords.isEmpty())

        // Even if SpeechRecognizer emits "7$" directly in the transcript
        val spokenFromSymbol = PronunciationScorer.buildSpokenWordsFromTranscript(
            transcript = "It costs 7$",
            energyWindows = emptyList(),
            confidence = 0.95f
        )
        val targetWords = PronunciationScorer.extractTargetWords("It costs seven dollars.")
        val eval2 = PronunciationScorer.evaluatePronunciation(targetWords, spokenFromSymbol)
        assertEquals(100, eval2.score)
        assertTrue(eval2.missedWords.isEmpty())
    }
}
