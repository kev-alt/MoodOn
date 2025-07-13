package com.example.moodon.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moodon.data.repository.MoodRepository

class RoomMoodViewModelFactory(
    private val repository: MoodRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomMoodViewModel::class.java)) {
            return RoomMoodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}