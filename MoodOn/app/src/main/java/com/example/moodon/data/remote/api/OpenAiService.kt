package com.example.moodon.data.remote.api

import com.example.moodon.data.remote.model.ChatRequest
import com.example.moodon.data.remote.model.ChatResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface OpenAiService {

    @POST("v1/chat/completions")
    suspend fun askChatGpt(
        @Body body: ChatRequest,
        @Header("Authorization") token: String
    ): ChatResponse

    companion object {
        fun create(): OpenAiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.openai.com/") // OpenAI API base URL
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(OpenAiService::class.java)
        }
    }
}
