package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ActivityLogEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.CodeDraftEntity
import com.example.data.model.MistakeEntryEntity
import com.example.data.model.NoticeEntity
import com.example.data.model.PracticeProblemEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.SupportTicketEntity
import com.example.data.model.UserProgressEntity

@Database(
    entities = [
        PracticeProblemEntity::class,
        UserProgressEntity::class,
        ActivityLogEntity::class,
        BookmarkEntity::class,
        MistakeEntryEntity::class,
        SupportTicketEntity::class,
        NoticeEntity::class,
        QuizAttemptEntity::class,
        CodeDraftEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun practiceProblemDao(): PracticeProblemDao
    abstract fun progressDao(): ProgressDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun mistakeDao(): MistakeDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun noticeDao(): NoticeDao
    abstract fun quizAttemptDao(): QuizAttemptDao
    abstract fun codeDraftDao(): CodeDraftDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `code_drafts` (
                        `problemId` TEXT NOT NULL,
                        `language` TEXT NOT NULL,
                        `code` TEXT NOT NULL,
                        `stdin` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`problemId`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hsc_ict_lab_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
