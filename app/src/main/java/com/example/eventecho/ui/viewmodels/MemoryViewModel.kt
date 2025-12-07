package com.example.eventecho.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventecho.data.firebase.MemoryRepository
import com.example.eventecho.ui.dataclass.MemoryWithUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.ViewModelProvider

class MemoryViewModel(
    private val repo: MemoryRepository
) : ViewModel() {

    private val _memory = MutableStateFlow<MemoryWithUser?>(null)
    val memory = _memory.asStateFlow()

    fun loadSingleMemory(eventId: String, ownerId: String) {
        viewModelScope.launch {

            val mem = repo.getMemory(eventId, ownerId) ?: return@launch

            val userSnap = FirebaseFirestore.getInstance()
                .collection("users")
                .document(ownerId)
                .get()
                .await()

            _memory.value = MemoryWithUser(
                memory = mem,
                username = userSnap.getString("username") ?: "Unknown",
                profilePicUrl = userSnap.getString("profilePicUrl")
            )
        }
    }

    fun toggleUpvote(eventId: String, ownerId: String) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            repo.toggleUpvote(eventId, ownerId, userId)
            loadSingleMemory(eventId, ownerId) // refresh
        }
    }
}

class MemoryViewModelFactory(
    private val repo: MemoryRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemoryViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

