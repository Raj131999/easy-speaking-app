package com.example

import com.example.ui.PronunciationScorer
import com.example.ui.SpokenTextNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpokenTextNormalizerTest {

    @Test
    fun convertsCurrenciesPercentagesTimesDatesOrdinalsAndSymbolsToWords() {
        val norm = SpokenTextNormalizer::normalizeToSpokenWords

        assertEquals("It costs seven dollars.", norm("It costs 7$."))
        assertEquals("It costs seven dollars.", norm("It costs $7."))
        assertEquals("It costs one dollar.", norm("It costs $1."))
        assertEquals("It costs one dollar.", norm("It costs 1$."))
        assertEquals("Total is five dollars and fifty cents.", norm("Total is $5.50."))
        assertEquals("Only fifty cents.", norm("Only $0.50."))
        assertEquals("A ten dollars t-shirt.", norm("A $10 t-shirt."))
        assertEquals("They raised one million dollars.", norm("They raised $1 million."))
        assertEquals("About sixty five million dollars.", norm("About $65 million."))
        assertEquals("Salary is ten thousand dollars.", norm("Salary is $10K."))
        assertEquals("Price is five hundred rupees.", norm("Price is ₹500."))
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
        assertEquals("A forty five-minute walk.", norm("A 45-minute walk."))
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

        // formatTranscriptWithUnk also converts numbers and symbols to spoken words
        assertEquals(
            "i paid seven dollars for 2 tickets".replace("2", "two"),
            PronunciationScorer.formatTranscriptWithUnk("i paid 7$ for 2 tickets")
        )
    }

    @Test
    fun informalShortenedWordsScore100WhenRecognizedInExpandedOrShortForm() {
        fun displayWordsFor(sentence: String): List<String> =
            sentence
                .replace('\u2019', '\'')
                .replace('\u2018', '\'')
                .replace(Regex("[^a-zA-Z0-9'\\s]"), " ")
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }

        fun assertPerfect(targetSentence: String, spokenTranscript: String) {
            val target = displayWordsFor(targetSentence)
            val spoken = PronunciationScorer.buildSpokenWordsFromTranscript(
                transcript = spokenTranscript,
                energyWindows = emptyList(),
                confidence = 0.95f
            )
            val eval = PronunciationScorer.evaluatePronunciation(target, spoken)
            assertEquals("Expected 100% for '$targetSentence' <- '$spokenTranscript'", 100, eval.score)
            assertTrue("Expected no missed words for '$targetSentence'", eval.missedWords.isEmpty())
            assertTrue("Expected no extra words for '$targetSentence' but got ${eval.extraWords}", eval.extraWords.isEmpty())
            assertTrue(
                "Expected all chips green (Correct) for '$targetSentence' but got ${eval.wordScores}",
                eval.wordScores.all { it.second == com.example.ui.WordScoreType.Correct }
            )
        }

        // Exact screenshot scenario: Gimme -> "Give me", I'll -> "I will"
        assertPerfect(
            "Gimme a second to grab my shoes and I'll meet you.",
            "Give me a second to grab my shoes and I will meet you"
        )
        assertPerfect(
            "Gimme a second to grab my shoes and I'll meet you.",
            "Gimme a second to grab my shoes and I'll meet you"
        )

        // All connected-speech lessons in the curriculum
        assertPerfect(
            "I'm gonna learn English and I wanna speak fluently.",
            "I am going to learn English and I want to speak fluently"
        )
        assertPerfect(
            "I betcha we will have a great time at the park today.",
            "I bet you we will have a great time at the park today"
        )
        assertPerfect(
            "I betcha we will have a great time at the park today.",
            "I bet ya we will have a great time at the park today"
        )
        assertPerfect(
            "Gotcha! Lemme know when you are ready.",
            "Got you let me know when you are ready"
        )
        assertPerfect(
            "It's sorta raining and I'm kinda tired.",
            "It is sort of raining and I am kind of tired"
        )
        assertPerfect(
            "Wouldja mind opening the window for some fresh air?",
            "Would you mind opening the window for some fresh air"
        )
        assertPerfect(
            "Didja have a chance to finish that report?",
            "Did you have a chance to finish that report"
        )
        assertPerfect(
            "We coulda won the game if we had practiced more.",
            "We could have won the game if we had practiced more"
        )
        assertPerfect(
            "We coulda won the game if we had practiced more.",
            "We could've won the game if we had practiced more"
        )
        assertPerfect(
            "We coulda won the game if we had practiced more.",
            "We could of won the game if we had practiced more"
        )
        assertPerfect(
            "I'm packin' my bags because I'm travelin' tomorrow.",
            "I'm packing my bags because I'm traveling tomorrow"
        )
        assertPerfect(
            "I'm packin' my bags because I'm travelin' tomorrow.",
            "I am packing my bags because I am travelling tomorrow"
        )
        assertPerfect(
            "You needn't carry the documents with you today.",
            "You need not carry the documents with you today"
        )
    }

    @Test
    fun informalReductionFixAppliesOnlyToSentencesContainingThoseWords() {
        // Normal sentence without "gimme": saying "gimme" should NOT be treated as 100% match for "Give me"
        val normalTarget = listOf("Give", "me", "a", "second")
        val spokenShort = PronunciationScorer.buildSpokenWordsFromTranscript(
            transcript = "gimme a second",
            energyWindows = emptyList(),
            confidence = 0.95f
        )
        val eval1 = PronunciationScorer.evaluatePronunciation(normalTarget, spokenShort)
        assertTrue("Non-gimme target sentence should remain strict (got ${eval1.score})", eval1.score < 100)

        // Normal sentence without dropped-g "-in'": "win" must NOT match "wing" as Correct
        val winTarget = listOf("I", "can", "win", "today")
        val spokenWing = PronunciationScorer.buildSpokenWordsFromTranscript(
            transcript = "I can wing today",
            energyWindows = emptyList(),
            confidence = 0.95f
        )
        val eval2 = PronunciationScorer.evaluatePronunciation(winTarget, spokenWing)
        assertTrue("Non-dropped-g word 'win' must not match 'wing' at 100% (got ${eval2.score})", eval2.score < 100)
    }
}
