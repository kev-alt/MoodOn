package com.example.moodon.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodon.data.local.entity.MoodEntity
import com.example.moodon.data.repository.MoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MoodUiState {
    object Loading : MoodUiState()
    data class Success(val mood: MoodEntity?) : MoodUiState()
    data class Error(val message: String) : MoodUiState()
}

class RoomMoodViewModel(
    private val repository: MoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MoodUiState>(MoodUiState.Loading)
    val uiState: StateFlow<MoodUiState> = _uiState

    fun loadTodayMood(date: String) {
        viewModelScope.launch {
            try {
                val mood = repository.getMoodByDate(date)
                _uiState.value = MoodUiState.Success(mood)
            } catch (e: Exception) {
                _uiState.value = MoodUiState.Error("Veri alınamadı: ${e.message}")
            }
        }
    }

    fun saveTodayMood(mood: MoodEntity) {
        viewModelScope.launch {
            try {
                repository.insertMood(mood)
                _uiState.value = MoodUiState.Success(mood)
            } catch (e: Exception) {
                _uiState.value = MoodUiState.Error("Kaydedilemedi: ${e.message}")
            }
        }
    }
}

