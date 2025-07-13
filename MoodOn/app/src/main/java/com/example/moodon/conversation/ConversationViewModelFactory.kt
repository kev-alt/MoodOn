package com.example.moodon.conversation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moodon.data.local.AppDatabase
import com.example.moodon.data.remote.api.ApiService
import com.example.moodon.data.remote.api.OpenAiService
import com.example.moodon.data.repository.ConversationRepository

class ConversationViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
            val db = AppDatabase.getInstance(context)

            val api = ApiService.create()                  // Retrofit ile oluşturulmuş ApiService
            val openAi = OpenAiService.create()            // Retrofit ile oluşturulmuş OpenAI API servisi
            val conversationDao = db.conversationDao()
            val userProfileDao = db.userProfileDao()

            val repository = ConversationRepository(
                api = api,
                openAi = openAi,
                dao = conversationDao,
                userProfileDao = userProfileDao
            )

            @Suppress("UNCHECKED_CAST")
            return ConversationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
