package com.example.moodon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.moodon.data.converter.ChatMessageConverter
import com.example.moodon.data.remote.model.ChatMessage
import androidx.room.ColumnInfo


@Entity(tableName = "past_conversations")
data class PastConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT) // 🔧 Bu satırı ekle
    val content: String,
    val timestamp: Long
)
