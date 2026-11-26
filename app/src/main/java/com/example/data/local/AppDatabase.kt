package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ActivityLogEntity
import com.example.data.model.BookmarkEntity
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
        QuizAttemptEntity::class
    ],
    version = 1,
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hsc_ict_lab_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
