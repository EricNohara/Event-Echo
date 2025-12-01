package com.example.eventecho.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EditProfileUiState(
    val name: String = "bro",
    val username: String = "",
    val bio: String = "",
    val isLoading: Boolean = false
)

class EditProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun onNameChange(newValue: String) {
        _uiState.value = _uiState.value.copy(name = newValue)
    }

    fun onUsernameChange(newValue: String) {
        _uiState.value = _uiState.value.copy(username = newValue)
    }

    fun onBioChange(newValue: String) {
        _uiState.value = _uiState.value.copy(bio = newValue)
    }

    // TODO: Do this after backend implemneted
    fun saveProfile() {
    }
}