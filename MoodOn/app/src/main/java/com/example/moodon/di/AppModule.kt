package com.example.moodon.di

import android.content.Context
import com.example.moodon.data.local.AppDatabase
import com.example.moodon.data.local.dao.ConversationDao
import com.example.moodon.data.local.dao.DiaryDao
import com.example.moodon.data.local.dao.MoodDao
import com.example.moodon.data.local.dao.UserProfileDao
import com.example.moodon.data.remote.api.ApiService
import com.example.moodon.data.remote.api.OpenAiService
import com.example.moodon.data.repository.ConversationRepository
import com.example.moodon.data.repository.MoodRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext app: Context): AppDatabase {
        return AppDatabase.getInstance(app)
    }

    @Provides
    fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideMoodDao(db: AppDatabase): MoodDao = db.moodDao()

    @Provides
    fun provideDiaryDao(db: AppDatabase): DiaryDao = db.diaryDao()

    @Provides
    fun provideConversationDao(db: AppDatabase): ConversationDao = db.conversationDao()

    @Provides
    @Singleton
    fun provideMoodRepository(
        moodDao: MoodDao,
        auth: FirebaseAuth
    ): MoodRepository {
        return MoodRepository(
            dao = moodDao,
            auth = auth
        )
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(): ApiService = Retrofit.Builder()
        .baseUrl("192.168.7.147:8000/") // FastAPI local backend URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideOpenAiService(client: OkHttpClient): OpenAiService =
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAiService::class.java)

    @Provides
    @Singleton
    fun provideConversationRepository(
        api: ApiService,
        openAi: OpenAiService,
        conversationDao: ConversationDao,
        userProfileDao: UserProfileDao
    ): ConversationRepository =
        ConversationRepository(api, openAi, conversationDao, userProfileDao)
}
