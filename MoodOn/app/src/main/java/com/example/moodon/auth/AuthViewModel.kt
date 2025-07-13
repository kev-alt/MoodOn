package com.example.moodon.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodon.data.local.entity.UserProfileEntity
import com.example.moodon.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val email: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val repository = UserRepository(application.applicationContext)

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        age: Int,
        gender: String
    ) {
        _state.value = AuthUiState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                viewModelScope.launch {
                    val profile = UserProfileEntity(
                        uid = uid,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        age = age,
                        gender = gender
                    )
                    repository.insertUser(profile)
                    db.collection("user_profiles").document(uid).set(profile)
                    _state.value = AuthUiState.Success(email)
                }
            }
            .addOnFailureListener {
                _state.value = AuthUiState.Error(it.message ?: "Kayıt başarısız.")
            }
    }

    fun login(email: String, password: String) {
        _state.value = AuthUiState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                viewModelScope.launch {
                    val doc = db.collection("user_profiles").document(uid).get().await()
                    if (doc.exists()) {
                        val name = auth.currentUser?.displayName ?: ""
                        repository.syncUserProfileIfNeeded(uid, email, name)
                        _state.value = AuthUiState.Success(email)
                    } else {
                        auth.signOut()
                        _state.value = AuthUiState.Error("Bu hesap kayıtlı değil")
                    }
                }
            }
            .addOnFailureListener {
                _state.value = AuthUiState.Error(it.message ?: "Giriş başarısız.")
            }
    }

    fun syncAfterSocialLogin() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val email = user.email ?: "noemail_${uid}@twitter.com"
        val name = user.displayName ?: ""

        viewModelScope.launch {
            val doc = db.collection("user_profiles").document(uid).get().await()
            if (doc.exists()) {
                repository.syncUserProfileIfNeeded(uid, email, name)
                _state.value = AuthUiState.Success(email)
            } else {
                auth.signOut()
                _state.value = AuthUiState.Error("Bu hesap kayıtlı değil")
            }
        }
    }
}
