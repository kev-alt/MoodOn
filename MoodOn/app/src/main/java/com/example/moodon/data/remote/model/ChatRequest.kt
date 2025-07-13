package com.example.moodon.data.remote.model

import com.example.moodon.data.local.entity.ChatMessageData

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: SimpleMessage
)

data class ChatRequest(
    val model: String = "gpt-4",
    val messages: List<SimpleMessage>
)
