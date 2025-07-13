package com.example.moodon.data.local.entity

import androidx.room.TypeConverter
import com.example.moodon.data.remote.model.ChatMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
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
