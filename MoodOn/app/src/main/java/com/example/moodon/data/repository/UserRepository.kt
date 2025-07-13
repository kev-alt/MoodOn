package com.example.moodon.data.repository

import android.content.Context
import com.example.moodon.data.local.AppDatabase
import com.example.moodon.data.local.entity.UserProfileEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(context: Context) {

    private val userDao = AppDatabase.getInstance(context).userProfileDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("user_profiles")

    suspend fun insertUser(user: UserProfileEntity) {
        userDao.insertUser(user)
        collection.document(user.uid).set(user).await()
    }

    suspend fun getUserByEmail(email: String): UserProfileEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getUserByUid(uid: String): UserProfileEntity? {
        return userDao.getUserByUid(uid)
    }

    suspend fun updateUser(user: UserProfileEntity) {
        userDao.updateUser(user)
        collection.document(user.uid).set(user).await()
    }

    suspend fun deleteUser(user: UserProfileEntity) {
        userDao.deleteUser(user)
        collection.document(user.uid).delete().await()
    }

    suspend fun getAllUsers(): List<UserProfileEntity> {
        return userDao.getAllUsers()
    }

    suspend fun syncUserProfileIfNeeded(uid: String, email: String, displayName: String?) {
        var profile = userDao.getUserByUid(uid)
        if (profile == null) {
            val snapshot = collection.document(uid).get().await()
            profile = if (snapshot.exists()) {
                snapshot.toObject(UserProfileEntity::class.java)?.copy(uid = uid)
            } else {
                val parts = displayName?.split(" ") ?: listOf("Kullanıcı", "Adı")
                UserProfileEntity(
                    uid = uid,
                    firstName = parts.firstOrNull() ?: "",
                    lastName = parts.getOrNull(1) ?: "",
                    email = email,
                    age = 0,
                    gender = ""
                )
            }

            profile?.let {
                userDao.insertUser(it)
                collection.document(uid).set(it).await()
            }
        }
    }

}

