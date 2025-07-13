package com.example.moodon.data.local.dao

import androidx.room.*
import com.example.moodon.data.local.entity.ChatMessageEntity
import com.example.moodon.data.local.entity.PastConversationEntity

@Dao
interface ConversationDao {

    // 🔹 GEÇMİŞ KONUŞMALAR (Snapshot için)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPastConversation(conversation: PastConversationEntity)

    @Query("SELECT * FROM past_conversations WHERE userId = :uid ORDER BY timestamp DESC")
    suspend fun getPastConversations(uid: String): List<PastConversationEntity>

    @Query("SELECT * FROM past_conversations WHERE id = :id")
    suspend fun getConversationById(id: Int): PastConversationEntity?

    @Query("DELETE FROM past_conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Int)

    // 🔹 GÜNCEL MESAJLAR (ChatMessageEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    suspend fun getMessagesByUser(userId: String): List<ChatMessageEntity>

    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun deleteMessagesByUser(userId: String)
}
