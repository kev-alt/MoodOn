package com.example.moodon.data.repository

import com.example.moodon.data.local.dao.MoodDao
import com.example.moodon.data.local.entity.MoodEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MoodRepository(
    private val dao: MoodDao,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun userId(): String = auth.currentUser?.uid ?: "unknown_user"

    private fun col() = db.collection("users")
        .document(userId())
        .collection("moods")

    suspend fun insertMood(mood: MoodEntity) {
        val moodWithUid = mood.copy(userId = userId())
        dao.insertMood(moodWithUid) // Yerel veritabanına ekle
        col().document(mood.date).set(moodWithUid).await() // Firestore'a ekle
    }

    suspend fun getMoodByDate(date: String): MoodEntity? {
        val uid = userId()
        val local = dao.getMoodByDateForUser(date, uid)
        return local ?: col().document(date).get().await().toObject(MoodEntity::class.java)?.also {
            dao.insertMood(it)
        }
    }


    suspend fun getLast7Days(): List<MoodEntity> {
        val uid = userId()
        val minDate = LocalDate.now().minusDays(6)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US))

        val remoteMoods = col()
            .whereGreaterThanOrEqualTo("date", minDate)
            .get().await()
            .documents.mapNotNull { it.toObject(MoodEntity::class.java) }

        // Senkronizasyon
        remoteMoods.forEach { dao.insertMood(it) }

        // 🔑 SADECE bu kullanıcıya ait olanlar
        return dao.getMoodsByUserId(uid)
            .filter { it.date >= minDate }
            .sortedBy { it.date }
    }


    suspend fun clearAll() {
        col().get().await().documents.forEach { it.reference.delete().await() }
    }
}