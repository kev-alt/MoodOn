package com.example.moodon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.UUID

@IgnoreExtraProperties
@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val uid: String = "",
    val date: String = "",
    val content: String = ""
)
