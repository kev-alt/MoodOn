package com.example.moodon.data.remote.model

data class ChatMessage(
    val userId: String = "",
    val role: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
