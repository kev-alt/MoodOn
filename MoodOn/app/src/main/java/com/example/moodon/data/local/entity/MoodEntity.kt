package com.example.moodon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey val date: String = "", // yyyy-MM-dd
    val moodName: String = "",
    val day: Int = 0,
    val month: Int = 0,
    val year: Int = 0,
    val weekday: String = "",
    val userId: String = ""
)