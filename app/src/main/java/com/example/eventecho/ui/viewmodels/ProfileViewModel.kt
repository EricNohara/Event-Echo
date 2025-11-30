package com.example.eventecho.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



data class UserProfile(
    val name: String,
    val location: String,
    val bio: String,
    val eventsAttended: Int,
    val eventsCreated: Int,
    val favorites: Int,
    val memberSince: String,
)

data class RecentEvent(val id: String, val title: String, val date: String)

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: UserProfile = UserProfile("", "", "", 0, 0, 0, ""),
    val recentEvents: List<RecentEvent> = emptyList()
)


class ProfileViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            delay(1000)

            //hardcoded data for now until backend
            _uiState.value = ProfileUiState(
                isLoading = false,
                user = UserProfile(
                    name = "Guest User",
                    location = "New York, NY(Hardcoded)",
                    bio = "Event lover Bio",
                    eventsAttended = 12,
                    eventsCreated = 3,
                    favorites = 8,
                    memberSince = "November 2025",//join date
                ),
                recentEvents = listOf(
                    RecentEvent("1", "Music Festival", "Nov 20, 2025"),
                    RecentEvent("2", "Tech Conference 2025", "Nov 15, 2025")
                )
            )
        }
    }
}
