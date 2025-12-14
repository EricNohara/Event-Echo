package com.example.eventecho.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventecho.data.firebase.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.eventecho.ui.dataclass.LeaderboardUser

enum class LeaderboardFilter {
    UPVOTES,
    CREATED,
    ATTENDED
}

data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val users: List<LeaderboardUser> = emptyList(),
    val filter: LeaderboardFilter = LeaderboardFilter.UPVOTES
)

class LeaderboardViewModel(
    private val userRepo: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            val docs = userRepo.getAllUsers()

            val users = docs.map { (uid, data) ->
                LeaderboardUser(
                    uid = uid,
                    username = data["username"] as? String ?: "Unknown",
                    profilePicUrl = data["profilePicUrl"] as? String,
                    totalUpvotes = (data["totalUpvotesReceived"] as? Long)?.toInt() ?: 0,
                    eventsCreated = (data["eventsCreated"] as? List<*>)?.size ?: 0,
                    eventsAttended = (data["eventsAttended"] as? List<*>)?.size ?: 0
                )
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                users = sort(users, _uiState.value.filter)
            )
        }
    }

    fun setFilter(filter: LeaderboardFilter) {
        _uiState.value = _uiState.value.copy(
            filter = filter,
            users = sort(_uiState.value.users, filter)
        )
    }

    private fun sort(
        users: List<LeaderboardUser>,
        filter: LeaderboardFilter
    ): List<LeaderboardUser> {
        return when (filter) {
            LeaderboardFilter.UPVOTES ->
                users.sortedByDescending { it.totalUpvotes }

            LeaderboardFilter.CREATED ->
                users.sortedByDescending { it.eventsCreated }

            LeaderboardFilter.ATTENDED ->
                users.sortedByDescending { it.eventsAttended }
        }
    }
}