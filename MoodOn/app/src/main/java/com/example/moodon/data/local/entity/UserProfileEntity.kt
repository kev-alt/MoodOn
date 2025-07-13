package com.example.moodon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val age: Int = 0,
    val gender: String = ""
)
