package com.example.eventecho.ui.dataclass

data class EditProfileUiState(
    val username: String = "",
    val bio: String = "",
    val profilePicUrl: String? = null,
    val isLoading: Boolean = true,
    val uploadingPicture: Boolean = false
)