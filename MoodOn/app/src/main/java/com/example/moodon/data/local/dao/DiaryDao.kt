package com.example.moodon.data.local.dao

import androidx.room.*
import com.example.moodon.data.local.entity.DiaryEntry

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries WHERE uid = :uid AND date = :date LIMIT 1")
    suspend fun getEntryByUidAndDate(uid: String, date: String): DiaryEntry?

    @Query("SELECT * FROM diary_entries WHERE uid = :uid ORDER BY date DESC")
    suspend fun getEntriesByUid(uid: String): List<DiaryEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntry)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)
}
