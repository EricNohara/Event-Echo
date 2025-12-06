package com.example.eventecho.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventecho.data.firebase.MemoryRepository
import com.example.eventecho.ui.dataclass.Memory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider

class MemoryWallViewModel(
    private val memoryRepo: MemoryRepository
) : ViewModel() {

    private val _memories = MutableStateFlow<List<Memory>>(emptyList())
    val memories = _memories.asStateFlow()

    private val _hasUploaded = MutableStateFlow(false)
    val hasUploaded = _hasUploaded.asStateFlow()

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser!!.uid

    fun loadMemories(eventId: String) {
        viewModelScope.launch {
            val items = memoryRepo.getMemories(eventId)

            _memories.value = items

            _hasUploaded.value = items.any { it.userId == currentUserId }
        }
    }
}

class MemoryWallViewModelFactory(
    private val memoryRepo: MemoryRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoryWallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemoryWallViewModel(memoryRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
