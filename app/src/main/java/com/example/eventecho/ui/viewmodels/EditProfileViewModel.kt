package com.example.eventecho.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventecho.data.firebase.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val repo: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState

    init {
        loadUser()
    }

    private fun loadUser() {
        repo.getUser { data ->
            _uiState.value = EditProfileUiState(
                username = data["username"] as? String ?: "",
                bio = data["bio"] as? String ?: "",
                profilePicUrl = data["profilePicUrl"] as? String,
                isLoading = false
            )
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun onBioChange(value: String) {
        _uiState.value = _uiState.value.copy(bio = value)
    }

    fun saveProfile() {
        val state = _uiState.value
        repo.updateUserFields(state.username, state.bio)
    }

    fun uploadImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(uploadingPicture = true)

        repo.uploadProfilePicture(
            imageUri = uri,
            onSuccess = { url ->
                repo.updateProfilePic(url)
                _uiState.value = _uiState.value.copy(
                    profilePicUrl = url,
                    uploadingPicture = false
                )
            },
            onError = {
                _uiState.value = _uiState.value.copy(uploadingPicture = false)
            }
        )
    }
}
