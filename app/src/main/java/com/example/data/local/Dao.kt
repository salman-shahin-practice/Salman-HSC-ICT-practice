package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ActivityLogEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.CodeDraftEntity
import com.example.data.model.MistakeEntryEntity
import com.example.data.model.NoticeEntity
import com.example.data.model.PracticeProblemEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.SupportTicketEntity
import com.example.data.model.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeProblemDao {
    @Query("SELECT * FROM practice_problems ORDER BY sortOrder ASC, titleEn ASC")
    fun observeAllProblems(): Flow<List<PracticeProblemEntity>>

    @Query("SELECT * FROM practice_problems WHERE language = :language ORDER BY sortOrder ASC, titleEn ASC")
    fun observeProblemsByLanguage(language: String): Flow<List<PracticeProblemEntity>>

    @Query("SELECT COUNT(*) FROM practice_problems")
    suspend fun countProblems(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(problems: List<PracticeProblemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProblem(problem: PracticeProblemEntity)

    @Query("DELETE FROM practice_problems WHERE id = :id")
    suspend fun deleteProblemById(id: String)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM user_progress")
    fun observeAllProgress(): Flow<List<UserProgressEntity>>

    @Query("SELECT * FROM user_progress WHERE problemId = :problemId LIMIT 1")
    suspend fun getProgressForProblem(problemId: String): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: UserProgressEntity)

    @Query("UPDATE user_progress SET synced = 1 WHERE synced = 0")
    suspend fun markAllSynced()

    @Query("SELECT * FROM user_progress")
    suspend fun getAllProgressSnapshot(): List<UserProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProgress(items: List<UserProgressEntity>)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY dateKey DESC LIMIT 14")
    fun observeRecentLogs(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getLogForDate(dateKey: String): ActivityLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: ActivityLogEntity)

    @Query("UPDATE activity_logs SET synced = 1 WHERE synced = 0")
    suspend fun markAllSynced()

    @Query("SELECT * FROM activity_logs")
    suspend fun getAllLogsSnapshot(): List<ActivityLogEntity>
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: String)

    @Query("SELECT * FROM bookmarks")
    suspend fun getAllBookmarksSnapshot(): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBookmarks(items: List<BookmarkEntity>)
}

@Dao
interface MistakeDao {
    @Query("SELECT * FROM mistake_notebook ORDER BY createdAt DESC")
    fun observeMistakes(): Flow<List<MistakeEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMistake(mistake: MistakeEntryEntity)

    @Query("UPDATE mistake_notebook SET resolved = :resolved WHERE id = :id")
    suspend fun setResolved(id: String, resolved: Boolean)

    @Query("DELETE FROM mistake_notebook WHERE id = :id")
    suspend fun deleteMistake(id: String)

    @Query("SELECT * FROM mistake_notebook")
    suspend fun getAllMistakesSnapshot(): List<MistakeEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMistakes(items: List<MistakeEntryEntity>)
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY updatedAt DESC")
    fun observeTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTicket(ticket: SupportTicketEntity)

    @Query("UPDATE support_tickets SET adminReply = :reply, status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun replyToTicket(id: String, reply: String, status: String, updatedAt: Long)

    @Query("UPDATE support_tickets SET syncStatus = 'SYNCED' WHERE syncStatus = 'QUEUED_OFFLINE'")
    suspend fun markQueuedTicketsSynced()

    @Query("SELECT * FROM support_tickets")
    suspend fun getAllTicketsSnapshot(): List<SupportTicketEntity>
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY pinned DESC, createdAt DESC")
    fun observeNotices(): Flow<List<NoticeEntity>>

    @Query("SELECT COUNT(*) FROM notices")
    suspend fun countNotices(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notices: List<NoticeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotice(notice: NoticeEntity)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: String)
}

@Dao
interface QuizAttemptDao {
    @Query("SELECT * FROM quiz_attempts ORDER BY createdAt DESC LIMIT 20")
    fun observeAttempts(): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity)

    @Query("UPDATE quiz_attempts SET synced = 1 WHERE synced = 0")
    suspend fun markAllSynced()
}

@Dao
interface CodeDraftDao {
    @Query("SELECT * FROM code_drafts ORDER BY updatedAt DESC")
    fun observeAllDrafts(): Flow<List<CodeDraftEntity>>

    @Query("SELECT * FROM code_drafts WHERE problemId = :problemId LIMIT 1")
    suspend fun getDraftForProblem(problemId: String): CodeDraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDraft(draft: CodeDraftEntity)

    @Query("DELETE FROM code_drafts WHERE problemId = :problemId")
    suspend fun deleteDraft(problemId: String)

    @Query("SELECT * FROM code_drafts")
    suspend fun getAllDraftsSnapshot(): List<CodeDraftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDrafts(items: List<CodeDraftEntity>)
}
