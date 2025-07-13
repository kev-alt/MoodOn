package com.example.moodon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.moodon.data.remote.model.ChatMessage

@Entity(tableName = "chat_messages")
data class ChatMessageData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val role: String,
    val message: String,
    val timestamp: Long
)

fun ChatMessageData.toChatMessage(): ChatMessage {
    return ChatMessage(
        userId = userId,
        role = role,
        content = message,
        timestamp = timestamp
    )
}

fun ChatMessage.toChatMessageData(): ChatMessageData {
    return ChatMessageData(
        userId = userId,
        role = role,
        message = content,
        timestamp = timestamp
    )
}
