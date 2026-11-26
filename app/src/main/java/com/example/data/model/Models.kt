package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LabLanguage(val label: String) {
    HTML("HTML5"),
    C("C Programming")
}

enum class EditorThemeMode(val displayName: String) {
    DARK_SLATE("Dark Slate"),
    CLASSIC_LIGHT("Classic Light"),
    SOLARIZED_WARM("Solarized Warm")
}

enum class UserRole(val labelBn: String) {
    STUDENT("শিক্ষার্থী (Student)"),
    ADMIN("অ্যাডমিন (Admin)")
}

enum class TicketStatus(val labelBn: String, val labelEn: String) {
    PENDING("অপেক্ষমান", "Pending"),
    ANSWERED("উত্তর দেওয়া হয়েছে", "Answered"),
    RESOLVED("সমাধান হয়েছে", "Resolved")
}

enum class SyncStatus(val labelEn: String) {
    QUEUED_OFFLINE("Queued Offline"),
    SYNCED("Synced")
}

@Entity(tableName = "practice_problems")
data class PracticeProblemEntity(
    @PrimaryKey val id: String,
    val language: String, // "HTML" or "C"
    val moduleKey: String,
    val moduleTitleBn: String,
    val moduleTitleEn: String,
    val titleBn: String,
    val titleEn: String,
    val difficulty: String, // "Easy", "Medium", "Board"
    val instructionsBn: String,
    val starterCode: String,
    val solutionCode: String,
    val hintBn: String,
    val commonMistakeBn: String,
    val testInput: String = "",
    val expectedOutput: String = "",
    val requiredTagsCsv: String = "",
    val isCustom: Boolean = false,
    val sortOrder: Int = 0
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val problemId: String,
    val language: String,
    val moduleKey: String,
    val completed: Boolean = false,
    val attempts: Int = 0,
    val bestAccuracy: Int = 0,
    val lastCode: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val dateKey: String, // yyyy-MM-dd
    val problemsSolved: Int = 0,
    val runsCount: Int = 0,
    val accuracySum: Int = 0,
    val accuracySamples: Int = 0,
    val synced: Boolean = false
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val language: String,
    val code: String,
    val noteBn: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mistake_notebook")
data class MistakeEntryEntity(
    @PrimaryKey val id: String,
    val language: String,
    val title: String,
    val brokenCode: String,
    val errorTitleBn: String,
    val explanationBn: String,
    val fixTipBn: String,
    val createdAt: Long = System.currentTimeMillis(),
    val resolved: Boolean = false
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val studentName: String,
    val subject: String,
    val category: String, // "HTML", "C Programming", "Exam"
    val message: String,
    val codeSnippet: String = "",
    val errorMessage: String = "",
    val attachmentUri: String? = null,
    val status: String = TicketStatus.PENDING.name,
    val adminReply: String = "",
    val syncStatus: String = SyncStatus.QUEUED_OFFLINE.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val tag: String, // "Exam Tip", "Notice", "Update"
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey val id: String,
    val category: String, // "ALL", "HTML", "C"
    val score: Int,
    val total: Int,
    val accuracyPercent: Int,
    val durationSec: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

data class ExamQuestion(
    val id: String,
    val category: String, // "HTML" or "C"
    val questionBn: String,
    val codeSnippet: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    val explanationBn: String
)

data class FaqItem(
    val id: String,
    val category: String,
    val questionBn: String,
    val answerBn: String,
    val codeExample: String? = null
)

data class BatchStudentSummary(
    val name: String,
    val batch: String,
    val htmlPercent: Int,
    val cPercent: Int,
    val weakTopic: String,
    val lastActive: String
)
