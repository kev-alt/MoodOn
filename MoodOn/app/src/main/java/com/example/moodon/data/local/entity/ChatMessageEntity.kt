package com.example.moodon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val role: String,       // "user" veya "assistant"
    val content: String,
    val timestamp: Long     // Zaman damgası
)
