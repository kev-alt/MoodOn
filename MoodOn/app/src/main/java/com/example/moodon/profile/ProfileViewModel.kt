package com.example.moodon.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodon.data.local.AppDatabase
import com.example.moodon.data.local.entity.UserProfileEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Data(val profile: UserProfileEntity) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val auth = FirebaseAuth.getInstance()
    private val db = AppDatabase.getInstance(context)
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: "noemail_${uid}@twitter.com"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dao = db.userProfileDao()
                var profile = dao.getUserByUid(uid)

                if (profile == null) {
                    val snapshot = firestore.collection("user_profiles").document(uid).get().await()
                    profile = snapshot.toObject(UserProfileEntity::class.java)?.copy(uid = uid)

                    if (profile == null) {
                        profile = UserProfileEntity(
                            uid = uid,
                            firstName = auth.currentUser?.displayName.orEmpty(),
                            lastName = "",
                            email = email,
                            age = 0,
                            gender = ""
                        )
                    }

                    dao.insertUser(profile)
                    firestore.collection("user_profiles").document(uid).set(profile)
                }

                _uiState.value = ProfileUiState.Data(profile)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Profil yükleme hatası: ${e.message}")
            }
        }
    }

    fun save(
        firstName: String,
        lastName: String,
        age: Int,
        gender: String,
        onResult: (String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: "noemail_${uid}@twitter.com"

        val profile = UserProfileEntity(
            uid = uid,
            firstName = firstName,
            lastName = lastName,
            email = email,
            age = age,
            gender = gender
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.userProfileDao().insertUser(profile)
                firestore.collection("user_profiles").document(uid).set(profile).await()
                _uiState.value = ProfileUiState.Data(profile)
                onResult(null)
            } catch (e: Exception) {
                onResult("Kaydetme hatası: ${e.message}")
            }
        }
    }
}
