package com.example.moodon.data.local.dao

import androidx.room.*
import com.example.moodon.data.local.entity.UserProfileEntity

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    suspend fun getUserByUid(uid: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfileEntity)

    @Update
    suspend fun updateUser(user: UserProfileEntity)

    @Delete
    suspend fun deleteUser(user: UserProfileEntity)

    @Query("SELECT * FROM user_profiles")
    suspend fun getAllUsers(): List<UserProfileEntity>
}
