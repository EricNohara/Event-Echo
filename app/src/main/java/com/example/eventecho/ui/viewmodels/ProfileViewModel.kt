package com.example.eventecho.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventecho.data.firebase.UserRepository
import com.example.eventecho.data.firebase.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.eventecho.ui.dataclass.ProfileUser
import com.example.eventecho.ui.dataclass.Event
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: ProfileUser = ProfileUser(),
    val recentEvents: List<Event> = emptyList()
)

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val eventRepo: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadUser()
    }

    private fun loadUser() {
        userRepo.getUser { data ->

            val recentIds: List<String> =
                (data["recentEvents"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            val user = ProfileUser(
                username = data["username"] as? String ?: "",
                bio = data["bio"] as? String ?: "",
                profilePicUrl = data["profilePicUrl"] as? String,
                eventsAttended = (data["eventsAttended"] as? List<*>)?.size ?: 0,
                eventsCreated = (data["eventsCreated"] as? List<*>)?.size ?: 0,
                favorites = (data["savedEvents"] as? List<*>)?.size ?: 0,
                memberSince = (data["createdAt"] as? Timestamp)?.toDate()?.let { date ->
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
                } ?: ""
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                user = user
            )

            // Load event objects from Firestore
            loadRecentEvents(recentIds)
        }
    }

    private fun loadRecentEvents(eventIds: List<String>) {
        viewModelScope.launch {
            if (eventIds.isEmpty()) {
                _uiState.value = _uiState.value.copy(recentEvents = emptyList())
                return@launch
            }

            // Fetch each event by its ID
            val events = eventIds.mapNotNull { id ->
                try {
                    eventRepo.getEventById(id)
                } catch (_: Exception) {
                    null
                }
            }

            _uiState.value = _uiState.value.copy(recentEvents = events)
        }
    }
}

class ProfileViewModelFactory(
    private val userRepo: UserRepository,
    private val eventRepo: EventRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userRepo, eventRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}