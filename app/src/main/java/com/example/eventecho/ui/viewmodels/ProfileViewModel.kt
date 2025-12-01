package com.example.eventecho.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.eventecho.data.firebase.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import com.example.eventecho.ui.dataclass.ProfileUser

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: ProfileUser = ProfileUser(),
    val recentEvents: List<RecentEvent> = emptyList()
)

data class RecentEvent(
    val title: String,
    val date: String
)

class ProfileViewModel(
    private val repo: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadUser()
    }

    private fun loadUser() {
        repo.getUser { data ->

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

            _uiState.value = ProfileUiState(
                isLoading = false,
                user = user,
                recentEvents = emptyList() // TODO: hook up real events later
            )
        }
    }
}
