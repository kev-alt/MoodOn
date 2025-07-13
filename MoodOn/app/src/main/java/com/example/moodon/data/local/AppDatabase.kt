package com.example.moodon.data.local

import android.content.Context
import androidx.room.*
import com.example.moodon.data.converter.ChatMessageConverter
import com.example.moodon.data.local.dao.ConversationDao
import com.example.moodon.data.local.dao.DiaryDao
import com.example.moodon.data.local.dao.MoodDao
import com.example.moodon.data.local.dao.UserProfileDao
import com.example.moodon.data.local.entity.ChatMessageEntity
import com.example.moodon.data.local.entity.DiaryEntry
import com.example.moodon.data.local.entity.MoodEntity
import com.example.moodon.data.local.entity.PastConversationEntity
import com.example.moodon.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        MoodEntity::class,
        DiaryEntry::class,
        PastConversationEntity::class,
        ChatMessageEntity::class  // ← Eksik olan bu satırdı
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(ChatMessageConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun moodDao(): MoodDao
    abstract fun diaryDao(): DiaryDao
    abstract fun conversationDao(): ConversationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moodon_db_$uid"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
