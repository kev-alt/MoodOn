package com.example.moodon.data.converter

import androidx.room.TypeConverter
import com.example.moodon.data.remote.model.ChatMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatMessageConverter {

    @TypeConverter
    fun fromChatMessageList(value: List<ChatMessage>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toChatMessageList(value: String): List<ChatMessage> {
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return Gson().fromJson(value, type)
    }
}