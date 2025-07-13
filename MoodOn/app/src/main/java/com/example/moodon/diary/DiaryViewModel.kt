package com.example.moodon.diary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodon.data.local.entity.DiaryEntry
import com.example.moodon.data.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DiaryViewModel(context: Context) : ViewModel() {

    private val repository = DiaryRepository(context)
    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries: StateFlow<List<DiaryEntry>> = _entries

    fun loadEntries(uid: String) {
        viewModelScope.launch {
            _entries.value = repository.getEntries(uid)
        }
    }

    fun saveEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.insertEntry(entry)
            loadEntries(entry.uid)
        }
    }

    fun deleteEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            loadEntries(entry.uid)
        }
    }

    suspend fun getEntry(uid: String, date: String): DiaryEntry? {
        return repository.getEntry(uid, date)
    }
}
