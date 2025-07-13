package com.example.moodon.conversation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodon.data.remote.model.ChatMessage
import com.example.moodon.data.repository.ConversationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConversationViewModel(
    private val repository: ConversationRepository
) : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private var isFirstMessage = true
    private var hasLoaded = false

    fun loadUserMessages(userId: String) {
        if (!hasLoaded) {
            Log.d("ConversationVM", "🔄 Kullanıcı mesajları Room'dan yükleniyor: $userId")
            viewModelScope.launch {
                val savedMessages = repository.getLocalMessages(userId)
                Log.d("ConversationVM", "💬 Yüklenen mesaj sayısı: ${savedMessages.size}")
                _chatMessages.value = savedMessages
                isFirstMessage = savedMessages.isEmpty()
                hasLoaded = true
            }
        }
    }

    fun startConversation(userId: String, input: String) {
        Log.d("ConversationVM", "✍️ Yeni kullanıcı mesajı: $input")
        viewModelScope.launch {
            val userMessage = ChatMessage(
                userId = userId,
                role = "user",
                content = input,
                timestamp = System.currentTimeMillis()
            )
            addMessage(userMessage)
            saveMessageToFirebase(userMessage)

            val botMessage = try {
                val response = repository.sendMessage(
                    userId = userId,
                    input = input,
                    previousMessages = _chatMessages.value,
                    isFirst = isFirstMessage
                )
                isFirstMessage = false
                response
            } catch (e: Exception) {
                Log.e("ConversationVM", "❌ Yapay zekâ yanıt hatası: ${e.localizedMessage}")
                ChatMessage(
                    userId = userId,
                    role = "assistant",
                    content = "Yanıt alınamadı.",
                    timestamp = System.currentTimeMillis()
                )
            }

            addMessage(botMessage)
            saveMessageToFirebase(botMessage)
        }
    }

    fun endConversationAndSave(userId: String, onFinished: () -> Unit) {
        Log.d("ConversationVM", "📦 Konuşma kaydediliyor ve sıfırlanıyor...")
        viewModelScope.launch {
            val messages = _chatMessages.value
            Log.d("ConversationVM", "📚 Kaydedilecek toplam mesaj sayısı: ${messages.size}")
            messages.forEachIndexed { index, msg ->
                Log.d("ConversationVM", "[$index] ${msg.role}: ${msg.content}")
            }

            if (messages.isNotEmpty()) {
                repository.saveConversationSnapshot(userId, messages)
                repository.clearLocalMessages(userId)
            }
            _chatMessages.value = emptyList()
            isFirstMessage = true
            hasLoaded = false
            onFinished()
        }
    }

    /**
     * Aynı içerikteki ardışık mesajlar tekrar eklenmez.
     */
    private fun addMessage(message: ChatMessage) {
        val last = _chatMessages.value.lastOrNull()
        if (last == null || last.content != message.content || last.role != message.role) {
            _chatMessages.value = _chatMessages.value + message
            viewModelScope.launch {
                repository.saveMessageLocally(message)
            }
            Log.d("ConversationVM", "✅ Mesaj eklendi: [${message.role}] ${message.content}")
        } else {
            Log.d("ConversationVM", "⚠️ Aynı içerikte ardışık mesaj engellendi: [${message.role}] ${message.content}")
        }
    }

    private fun saveMessageToFirebase(message: ChatMessage) {
        FirebaseFirestore.getInstance()
            .collection("conversations")
            .document(message.userId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                Log.d("ConversationVM", "✅ Mesaj Firestore'a kaydedildi.")
            }
            .addOnFailureListener {
                Log.e("ConversationVM", "❌ Mesaj Firestore'a kaydedilemedi: ${it.localizedMessage}")
            }
    }
}
