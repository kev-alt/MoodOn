package com.example.moodon.data.repository

import android.content.Context
import com.example.moodon.data.local.AppDatabase
import com.example.moodon.data.local.entity.DiaryEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DiaryRepository(context: Context) {

    private val diaryDao = AppDatabase.getInstance(context).diaryDao()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getEntries(uid: String): List<DiaryEntry> {
        val localEntries = diaryDao.getEntriesByUid(uid)
        val remoteEntries = firestore.collection("diary_entries")
            .whereEqualTo("uid", uid)
            .get()
            .await()
            .toObjects(DiaryEntry::class.java)

        return (remoteEntries + localEntries).distinctBy { it.id }
    }

    suspend fun getEntry(uid: String, date: String): DiaryEntry? {
        val local = diaryDao.getEntryByUidAndDate(uid, date)
        if (local != null) return local

        val snapshot = firestore.collection("diary_entries")
            .whereEqualTo("uid", uid)
            .whereEqualTo("date", date)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject(DiaryEntry::class.java)
    }

    suspend fun insertEntry(entry: DiaryEntry) {
        diaryDao.insertEntry(entry)
        firestore.collection("diary_entries")
            .document(entry.id)
            .set(entry)
            .await()
    }

    suspend fun deleteEntry(entry: DiaryEntry) {
        diaryDao.deleteEntry(entry)
        firestore.collection("diary_entries")
            .document(entry.id)
            .delete()
            .await()
    }
}
