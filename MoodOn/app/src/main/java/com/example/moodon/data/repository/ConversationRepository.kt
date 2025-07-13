package com.example.moodon.data.repository

import android.util.Log
import com.example.moodon.BuildConfig
import com.example.moodon.data.local.dao.ConversationDao
import com.example.moodon.data.local.dao.UserProfileDao
import com.example.moodon.data.local.entity.PastConversationEntity
import com.example.moodon.data.local.entity.toChatMessage
import com.example.moodon.data.local.entity.toEntity
import com.example.moodon.data.remote.api.ApiService
import com.example.moodon.data.remote.api.OpenAiService
import com.example.moodon.data.remote.model.ChatMessage
import com.example.moodon.data.remote.model.ChatRequest
import com.example.moodon.data.remote.model.SimpleMessage
import com.example.moodon.data.remote.model.TextInput
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException

class ConversationRepository(
    private val api: ApiService,
    private val openAi: OpenAiService,
    private val dao: ConversationDao,
    private val userProfileDao: UserProfileDao
) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun detectEmotion(sentence: String): String {
        Log.d("ConversationRepo", "Duygu analizi başlatılıyor: $sentence")
        return try {
            val emotion = api.getEmotion(TextInput(sentence)).emotion
            Log.d("ConversationRepo", "Duygu analizi sonucu: $emotion")
            emotion
        } catch (e: Exception) {
            Log.e("ConversationRepo", "Duygu analizi hatası: ${e.localizedMessage}")
            "neutral"
        }
    }

    suspend fun askOpenAI(prompt: String): String {
        return try {
            val request = ChatRequest(
                model = "gpt-4",
                messages = listOf(SimpleMessage(role = "user", content = prompt))
            )
            Log.d("OpenAI", "İstek gönderiliyor: $request")

            val response = openAi.askChatGpt(request, "Bearer ${BuildConfig.OPENAI_API_KEY}")
            val rawReply = response.choices.firstOrNull()?.message?.content ?: "Yanıt alınamadı"
            rawReply.replace(Regex("^(assistant|bot|asistan|neutral):\\s*", RegexOption.IGNORE_CASE), "").trim()
        } catch (e: HttpException) {
            Log.e("OpenAI", "HTTP error: ${e.code()}")
            "Sunucuda hata oluştu."
        } catch (e: Exception) {
            Log.e("OpenAI", "Hata: ${e.localizedMessage}")
            "Bir hata oluştu."
        }
    }

    suspend fun sendMessage(
        userId: String,
        input: String,
        previousMessages: List<ChatMessage>,
        isFirst: Boolean
    ): ChatMessage {
        Log.d("ConversationRepo", "Kullanıcı mesajı alındı: $input")

        val riskyWords = listOf("intihar", "ölmek", "kendimi öldürmek", "yaşamak istemiyorum")
        if (riskyWords.any { input.contains(it, ignoreCase = true) }) {
            return ChatMessage(userId, "assistant", "Bu duygularla yalnız olmadığını bilmeni istiyorum. Lütfen güvendiğin bir yetişkinle ya da bir uzmandan destek almayı düşün.")
        }

        val emotion = if (isFirst) detectEmotion(input) else ""
        val context = previousMessages.takeLast(4).joinToString("\n") { "${it.role}: ${it.content}" }

        val profile = userProfileDao.getUserByUid(userId)
        val namePart = profile?.let {
            "Adı: ${it.firstName} ${it.lastName}, Yaş: ${it.age}, Cinsiyet: ${it.gender}"
        } ?: "Kullanıcı bilgisi mevcut değil."

        val prompt = if (isFirst) {
            """
            Sen bir mobil terapist uygulamasında çalışan, adı "Dr. Moo" olan, son derece empatik, güven veren ve profesyonel bir yapay zekâ terapistisin. Görevin, kullanıcıların duygularını anlamak, onları dinlemek, empati kurmak, gerektiğinde öneriler sunmak ve onlara psikolojik açıdan destek olmaktır. 
            
            Dr. Moo olarak bir insan gibi doğal konuşur, karşısındakini asla yargılamazsın. Sabırlı, anlayışlı ve her koşulda destekleyici bir yapay zekâsın. Mobil uygulama üzerinden kullanıcıya metin tabanlı şekilde konuşma terapisi yaparsın.

            ---
            📌 KULLANICI BİLGİLERİ:
            $namePart
            Duygu analizi sonucu: "$emotion"
            İlk mesaj: "$input"

            ---
            Lütfen şu akışa göre bir yanıt oluştur:

            🧠 1. DUYGU ANALİZİ YAP:
            "Yazdıklarından, şu anda kendini $emotion hissediyor olabileceğini anlıyorum."
            "Bu duyguyu yaşaman tamamen doğal ve geçerli."

            🧘‍♀️ 2. EMPATİ KUR & GÜVEN VER:
            - "Bu şekilde hissetmen çok anlaşılır. Dr. Moo olarak seni yargılamadan dinliyorum."
            - "Güvendesin, burada tamamen senin duygularını anlamak için buradayım."

            🔍 3. DETAY SORUSU SOR:
            - "Bu duygu bir süredir devam ediyor mu?"
            - "Sence seni en çok etkileyen olay ne oldu?"

            💡 4. DURUMA ÖZEL DESTEK VER:
            - "Dilersen kısa bir nefes egzersizi deneyebiliriz."
            - "İstersen duygularını günlüğüne yazabilirsin, bazen içindekileri dışa vurmak çok iyi gelir."

            🕊️ 5. MOTİVASYON VER:
            - "Unutma, bu duygular gelip geçici. Sen çok daha güçlüsün."
            - "Dr. Moo her zaman burada, ne zaman istersen konuşabiliriz."

            ⚠️ 6. GÜVENLİK:
            - "Eğer bu hisler seni çok zorluyorsa, bir uzmandan destek almayı düşünebilirsin. Bu çok cesurca bir adım olur."

            İlk mesajda kendini tanıt ve "Merhaba" demeyi unutma.
            """.trimIndent()
        } else {
            """
            Aşağıda kullanıcının bilgileri, son konuşmalar ve yeni mesajı verilmiştir.
            - $namePart
            - Son konuşmalar:
            $context
            - Yeni mesaj: "$input"

            Lütfen:
            - Nazik ve destekleyici bir tonda cevap ver.
            - CBT teknikleri gibi faydalı yönlendirmeler yapabilirsin.
            - Ancak tekrar "Merhaba" deme. Konuşma devamı gibi davran.
            - Kullanıcıyı rahatlatan ve içten hissettiren bir kapanış sorusu sor.

            Dr. Moo gibi davran. Kısa, içten ve yargılamadan yaz.
            """.trimIndent()
        }

        val reply = askOpenAI(prompt)
        return ChatMessage(userId, "assistant", reply)
    }

    suspend fun saveMessageLocally(message: ChatMessage) {
        dao.insertMessage(message.toEntity())
    }

    suspend fun getLocalMessages(userId: String): List<ChatMessage> {
        return dao.getMessagesByUser(userId).map { it.toChatMessage() }
    }

    suspend fun clearLocalMessages(userId: String) {
        dao.deleteMessagesByUser(userId)
    }

    suspend fun saveConversationSnapshot(userId: String, messages: List<ChatMessage>) {
        if (messages.isEmpty()) return

        val content = messages.joinToString("\n") { "${it.role}: ${it.content}" }
        val timestamp = System.currentTimeMillis()

        val entity = PastConversationEntity(
            userId = userId,
            content = content,
            timestamp = timestamp
        )
        dao.insertPastConversation(entity)

        val firestoreData = hashMapOf(
            "userId" to userId,
            "content" to content,
            "timestamp" to timestamp
        )
        try {
            firestore.collection("conversations")
                .add(firestoreData)
                .await()
            Log.d("ConversationRepo", "Konuşma Firestore'a başarıyla kaydedildi.")
        } catch (e: Exception) {
            Log.e("ConversationRepo", "Firestore'a kaydedilemedi: ${e.localizedMessage}")
        }
    }

    suspend fun getPastConversations(userId: String): List<PastConversationEntity> {
        return dao.getPastConversations(userId)
    }

    suspend fun getConversationById(id: Int): PastConversationEntity? {
        return dao.getConversationById(id)
    }

    suspend fun deleteConversationById(id: Int) {
        dao.deleteConversationById(id)
    }
}
