package com.example.moodon.data.remote.api

import com.example.moodon.data.remote.model.PredictionOut
import com.example.moodon.data.remote.model.TextInput
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/predict")
    suspend fun getEmotion(@Body input: TextInput): PredictionOut

    companion object {
        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl("http://192.168.7.147:8000/") // 🔁 Burayı kendi sunucuna göre değiştir
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
