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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.eventecho.ui.dataclass.MemoryWithUser

class MemoryWallViewModel(
    private val memoryRepo: MemoryRepository
) : ViewModel() {

    private val _memories = MutableStateFlow<List<MemoryWithUser>>(emptyList())
    val memories = _memories.asStateFlow()

    private val _hasUploaded = MutableStateFlow(false)
    val hasUploaded = _hasUploaded.asStateFlow()

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser!!.uid

    fun loadMemories(eventId: String) {
        viewModelScope.launch {
            val raw = memoryRepo.getMemories(eventId)

            val enriched = raw.map { mem ->
                val userSnap = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(mem.userId)
                    .get()
                    .await()

                MemoryWithUser(
                    memory = mem,
                    username = userSnap.getString("username") ?: "Unknown",
                    profilePicUrl = userSnap.getString("profilePicUrl")
                )
            }

            _memories.value = enriched
            _hasUploaded.value = enriched.any { it.memory.userId == currentUserId }
        }
    }

    fun toggleUpvote(eventId: String, memoryOwnerId: String) {
        viewModelScope.launch {
            memoryRepo.toggleUpvote(eventId, memoryOwnerId, currentUserId)
            loadMemories(eventId)
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
