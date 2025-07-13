// File: com.example.moodon.data.local.entity.ChatMessageMapper.kt
package com.example.moodon.data.local.entity

import com.example.moodon.data.remote.model.ChatMessage

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        userId = this.userId,
        role = this.role,
        content = this.content,
        timestamp = this.timestamp
    )
}

fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        userId = this.userId,
        role = this.role,
        content = this.content,
        timestamp = this.timestamp
    )
}
