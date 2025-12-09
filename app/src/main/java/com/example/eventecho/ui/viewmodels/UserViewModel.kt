package com.example.eventecho.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.eventecho.data.firebase.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserUiState(
    val profilePicUrl: String? = null
)

class UserViewModel(
    private val userRepo: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState

    init {
        loadUserProfilePic()
    }

    private fun loadUserProfilePic() {
        val uid = userRepo.getUid() ?: return  // no crash — just skip

        viewModelScope.launch {
            userRepo.getUser { data ->
                val url = data["profilePicUrl"] as? String
                _uiState.value = UserUiState(profilePicUrl = url)
            }
        }
    }
}

class UserViewModelFactory(
    private val userRepo: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
