package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class EnglishRepository(private val englishDao: EnglishDao) {

    val userProgress: Flow<UserProgress?> = englishDao.getUserProgressFlow()
    val grammarLessons: Flow<List<GrammarLesson>> = englishDao.getAllGrammarLessons()
    val conversations: Flow<List<ConversationSet>> = englishDao.getAllConversations()
    val dailySentences: Flow<List<DailySentence>> = englishDao.getAllDailySentences()
    val paragraphs: Flow<List<ParagraphSet>> = englishDao.getAllParagraphs()
    val tongueTwisters: Flow<List<TongueTwister>> = englishDao.getAllTongueTwisters()

    suspend fun checkAndPrepopulate(context: Context) {
        Log.d("EnglishRepository", "Syncing database with InitialData...")

        val existingProgress = englishDao.getUserProgress()
        if (existingProgress == null) {
            englishDao.insertUserProgress(UserProgress(id = 1, currentStreak = 0, longestStreak = 0, lastActiveDate = ""))
        }

        // Sync Grammar Lessons
        val lessons = englishDao.getAllGrammarLessons().firstOrNull() ?: emptyList()
        
        // Load useful lessons from assets
        val usefulLessons = mutableListOf<GrammarLesson>()
        try {
            val jsonString = context.assets.open("useful_lessons.json").bufferedReader().use { it.readText() }
            val jsonArray = org.json.JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                usefulLessons.add(
                    GrammarLesson(
                        id = jsonObject.getInt("id"),
                        title = jsonObject.getString("title"),
                        level = jsonObject.optString("level", "Intermediate"),
                        explanation = jsonObject.getString("explanation"),
                        exampleText = jsonObject.getString("exampleText"),
                        exampleTranslation = jsonObject.optString("exampleTranslation", ""),
                        optionsString = jsonObject.optString("optionsString", ""),
                        correctOption = jsonObject.optString("correctOption", ""),
                        speechPrompt = jsonObject.getString("speechPrompt"),
                        isCompleted = jsonObject.optBoolean("isCompleted", false),
                        orderIndex = jsonObject.optInt("orderIndex", i),
                        category = jsonObject.optString("category", "Useful Lessons")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("EnglishRepository", "Error loading useful_lessons.json from assets", e)
        }

        val allInitialLessons = InitialData.grammarLessons + usefulLessons

        val updatedLessons = allInitialLessons.map { initLesson ->
            val existing = lessons.find { it.id == initLesson.id }
            if (existing != null) {
                initLesson.copy(isCompleted = existing.isCompleted)
            } else {
                initLesson
            }
        }
        englishDao.insertGrammarLessons(updatedLessons)

        // Sync Conversations
        val convs = englishDao.getAllConversations().firstOrNull() ?: emptyList()
        val updatedConvs = InitialData.conversations.map { initConv ->
            val existing = convs.find { it.id == initConv.id }
            if (existing != null) {
                initConv.copy(isCompleted = existing.isCompleted)
            } else {
                initConv
            }
        }
        englishDao.insertConversations(updatedConvs)
        if (InitialData.conversations.isNotEmpty()) {
            englishDao.deleteConversationsWithIdGreaterThan(InitialData.conversations.maxOf { it.id })
        }

        // Sync Daily Sentences
        val sents = englishDao.getAllDailySentences().firstOrNull() ?: emptyList()
        val updatedSentences = InitialData.dailySentences.map { initSent ->
            val existing = sents.find { it.id == initSent.id }
            if (existing != null) {
                initSent.copy(
                    reviewScheduledTime = existing.reviewScheduledTime,
                    consecutiveCorrect = existing.consecutiveCorrect,
                    timesPracticed = existing.timesPracticed,
                    lastAccuracy = existing.lastAccuracy
                )
            } else {
                initSent
            }
        }
        englishDao.insertDailySentences(updatedSentences)

        // Sync Paragraphs
        val paras = englishDao.getAllParagraphs().firstOrNull() ?: emptyList()

        val tedParagraphs = mutableListOf<ParagraphSet>()
        try {
            val jsonString = context.assets.open("ted_paragraphs.json").bufferedReader().use { it.readText() }
            val jsonArray = org.json.JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                tedParagraphs.add(
                    ParagraphSet(
                        id = jsonObject.getInt("id"),
                        title = jsonObject.getString("title"),
                        text = jsonObject.getString("text"),
                        level = jsonObject.optString("level", "Intermediate"),
                        estimatedReadingTime = jsonObject.optInt("estimatedReadingTime", 60),
                        isCompleted = jsonObject.optBoolean("isCompleted", false),
                        maxAccuracy = jsonObject.optInt("maxAccuracy", 0)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("EnglishRepository", "Error loading ted_paragraphs.json from assets", e)
        }

        val allInitialParas = if (tedParagraphs.isNotEmpty()) {
            tedParagraphs
        } else {
            InitialData.paragraphs
        }

        val updatedParas = allInitialParas.map { initPara ->
            val existing = paras.find { it.id == initPara.id }
            if (existing != null) {
                initPara.copy(
                    isCompleted = existing.isCompleted,
                    maxAccuracy = existing.maxAccuracy
                )
            } else {
                initPara
            }
        }
        englishDao.insertParagraphs(updatedParas)
        if (allInitialParas.isNotEmpty()) {
            englishDao.deleteParagraphsWithIdGreaterThan(allInitialParas.maxOf { it.id })
        }

        // Sync Tongue Twisters
        val twisters = englishDao.getAllTongueTwisters().firstOrNull() ?: emptyList()
        val updatedTwisters = InitialData.tongueTwisters.map { initTwister ->
            val existing = twisters.find { it.id == initTwister.id }
            if (existing != null) {
                initTwister.copy(
                    maxAccuracy = existing.maxAccuracy,
                    practiceCount = existing.practiceCount,
                    isCompleted = existing.isCompleted
                )
            } else {
                initTwister
            }
        }
        englishDao.insertTongueTwisters(updatedTwisters)
    }

    suspend fun updateGrammarLesson(lesson: GrammarLesson) {
        englishDao.updateGrammarLesson(lesson)
    }

    suspend fun updateConversation(conversation: ConversationSet) {
        englishDao.updateConversation(conversation)
    }

    suspend fun updateDailySentence(sentence: DailySentence) {
        englishDao.updateDailySentence(sentence)
    }

    suspend fun updateParagraph(paragraph: ParagraphSet) {
        englishDao.updateParagraph(paragraph)
    }

    suspend fun updateTongueTwister(twister: TongueTwister) {
        englishDao.updateTongueTwister(twister)
    }

    suspend fun updateProgress(progress: UserProgress) {
        englishDao.insertUserProgress(progress)
    }

    // Dynamic Spaced-Repetition Review Scheduler
    suspend fun scheduleSpacedRepetition(sentenceId: Int, isCorrect: Boolean, accuracy: Int) {
        // Query sentence
        val sentences = englishDao.getAllDailySentences().firstOrNull() ?: return
        val sentence = sentences.find { it.id == sentenceId } ?: return

        val newConsecutive = if (isCorrect) sentence.consecutiveCorrect + 1 else 0
        // Exponential backoff intervals in milliseconds: 1 min, 5 min, 30 min, 1 day, 3 days, 7 days
        val intervalMs = when (newConsecutive) {
            0 -> 60_000L // Review in 1 minute
            1 -> 300_000L // Review in 5 minutes
            2 -> 1_800_000L // Review in 30 minutes
            3 -> 86_400_000L // Review in 1 day
            4 -> 259_200_000L // Review in 3 days
            else -> 604_800_000L // Review in 7 days
        }

        val updatedSentence = sentence.copy(
            consecutiveCorrect = newConsecutive,
            timesPracticed = sentence.timesPracticed + 1,
            lastAccuracy = accuracy,
            reviewScheduledTime = System.currentTimeMillis() + intervalMs
        )
        englishDao.updateDailySentence(updatedSentence)
    }

    // Award XP and process Streak logic offline
    suspend fun awardXP(xpAmount: Int) {
        val currentProgress = englishDao.getUserProgress() ?: UserProgress(id = 1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        val lastActive = currentProgress.lastActiveDate
        val newStreak: Int
        val todayXp: Int

        if (lastActive.isEmpty()) {
            newStreak = 1
            todayXp = xpAmount
        } else if (lastActive == todayStr) {
            newStreak = currentProgress.currentStreak
            todayXp = currentProgress.todayXP + xpAmount
        } else {
            // Check if last active was yesterday
            val yesterdayCal = Calendar.getInstance()
            yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(yesterdayCal.time)

            if (lastActive == yesterdayStr) {
                newStreak = currentProgress.currentStreak + 1
                todayXp = xpAmount
            } else {
                // Streak broken
                newStreak = 1
                todayXp = xpAmount
            }
        }

        val longest = if (newStreak > currentProgress.longestStreak) newStreak else currentProgress.longestStreak

        val updatedProgress = currentProgress.copy(
            currentStreak = newStreak,
            longestStreak = longest,
            lastActiveDate = todayStr,
            totalXP = currentProgress.totalXP + xpAmount,
            todayXP = todayXp
        )
        englishDao.insertUserProgress(updatedProgress)
    }
}
