package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EnglishDao {

    // --- User Progress Queries ---
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgressFlow(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgress(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgress)

    @Update
    suspend fun updateUserProgress(progress: UserProgress)


    // --- Grammar Lesson Queries ---
    @Query("SELECT * FROM grammar_lessons ORDER BY orderIndex ASC")
    fun getAllGrammarLessons(): Flow<List<GrammarLesson>>

    @Query("SELECT * FROM grammar_lessons WHERE id = :id LIMIT 1")
    suspend fun getGrammarLessonById(id: Int): GrammarLesson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrammarLessons(lessons: List<GrammarLesson>)

    @Update
    suspend fun updateGrammarLesson(lesson: GrammarLesson)


    // --- Conversation Queries ---
    @Query("SELECT * FROM conversations ORDER BY id ASC")
    fun getAllConversations(): Flow<List<ConversationSet>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Int): ConversationSet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationSet>)

    @Update
    suspend fun updateConversation(conversation: ConversationSet)

    @Query("DELETE FROM conversations WHERE id > :maxId")
    suspend fun deleteConversationsWithIdGreaterThan(maxId: Int)


    // --- Daily Sentences Queries ---
    @Query("SELECT * FROM daily_sentences ORDER BY id ASC")
    fun getAllDailySentences(): Flow<List<DailySentence>>

    @Query("SELECT * FROM daily_sentences WHERE reviewScheduledTime <= :currentTime OR reviewScheduledTime = 0 ORDER BY reviewScheduledTime ASC")
    fun getDueDailySentences(currentTime: Long): Flow<List<DailySentence>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySentences(sentences: List<DailySentence>)

    @Update
    suspend fun updateDailySentence(sentence: DailySentence)


    // --- Paragraph Queries ---
    @Query("SELECT * FROM paragraphs ORDER BY id ASC")
    fun getAllParagraphs(): Flow<List<ParagraphSet>>

    @Query("SELECT * FROM paragraphs WHERE id = :id LIMIT 1")
    suspend fun getParagraphById(id: Int): ParagraphSet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParagraphs(paragraphs: List<ParagraphSet>)

    @Query("DELETE FROM paragraphs WHERE id > :maxId")
    suspend fun deleteParagraphsWithIdGreaterThan(maxId: Int)

    @Update
    suspend fun updateParagraph(paragraph: ParagraphSet)


    // --- Tongue Twister Queries ---
    @Query("SELECT * FROM tongue_twisters ORDER BY id ASC")
    fun getAllTongueTwisters(): Flow<List<TongueTwister>>

    @Query("SELECT * FROM tongue_twisters WHERE id = :id LIMIT 1")
    suspend fun getTongueTwisterById(id: Int): TongueTwister?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTongueTwisters(twisters: List<TongueTwister>)

    @Update
    suspend fun updateTongueTwister(twister: TongueTwister)
}
