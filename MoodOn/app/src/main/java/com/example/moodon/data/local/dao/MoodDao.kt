package com.example.moodon.data.local.dao

import androidx.room.*
import com.example.moodon.data.local.entity.MoodEntity

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntity)

    @Query("SELECT * FROM moods WHERE date = :date LIMIT 1")
    suspend fun getMoodByDate(date: String): MoodEntity?

    @Query("SELECT * FROM moods WHERE userId = :uid ORDER BY date DESC")
    suspend fun getMoodsByUserId(uid: String): List<MoodEntity>

    @Query("SELECT * FROM moods WHERE date = :date AND userId = :uid LIMIT 1")
    suspend fun getMoodByDateForUser(date: String, uid: String): MoodEntity?

}
