package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: String = "", // e.g. "2026-07-07"
    val totalXP: Int = 0,
    val dailyGoalXP: Int = 50, // default daily goal is 50 XP
    val todayXP: Int = 0
)

@Entity(tableName = "grammar_lessons")
data class GrammarLesson(
    @PrimaryKey val id: Int,
    val title: String,
    val level: String, // "Beginner", "Intermediate", "Advanced"
    val explanation: String,
    val exampleText: String,
    val exampleTranslation: String,
    val optionsString: String, // Comma-separated quiz options
    val correctOption: String,
    val speechPrompt: String, // A sentence for speaking practice
    val isCompleted: Boolean = false,
    val orderIndex: Int,
    val category: String = ""
)

@Entity(tableName = "conversations")
data class ConversationSet(
    @PrimaryKey val id: Int,
    val title: String,
    val scenario: String,
    val basicDialogueJson: String = "",
    val intermediateDialogueJson: String = "",
    val advancedDialogueJson: String = "",
    val dialogueJson: String = "", // JSON list of dialogue turns
    val vocabularyCallout: String = "", // Comma-separated or short info
    val comprehensionQuestion: String = "",
    val comprehensionOptions: String = "",
    val comprehensionAnswer: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "daily_sentences")
data class DailySentence(
    @PrimaryKey val id: Int,
    val text: String,
    val translation: String,
    val category: String, // e.g. "Greetings", "Small Talk"
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val reviewScheduledTime: Long = 0, // Spaced-repetition next practice timestamp
    val consecutiveCorrect: Int = 0, // Track correct streak for spaced-repetition levels
    val timesPracticed: Int = 0,
    val lastAccuracy: Int = 0,
    val soundFocus: String = "" // e.g. "th", "v vs w"
)

@Entity(tableName = "paragraphs")
data class ParagraphSet(
    @PrimaryKey val id: Int,
    val title: String,
    val text: String,
    val level: String, // "Beginner", "Intermediate", "Advanced"
    val estimatedReadingTime: Int, // in seconds
    val isCompleted: Boolean = false,
    val maxAccuracy: Int = 0
)

@Entity(tableName = "tongue_twisters")
data class TongueTwister(
    @PrimaryKey val id: Int,
    val soundFocus: String, // e.g. "th", "r vs l", "s vs sh"
    val text: String,
    val description: String = "",
    val level: String = "Medium",
    val maxAccuracy: Int = 0,
    val practiceCount: Int = 0,
    val isCompleted: Boolean = false
)
