package com.example.eventecho.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.eventecho.data.firebase.EventRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

data class CreateEventUiState(
    val title: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val imageUri: Uri? = null,
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLoading: Boolean = false
)

class CreateEventViewModel(
    private val repo: EventRepository,
    app: Application
) : AndroidViewModel(app) {

    private val _ui = MutableStateFlow(CreateEventUiState())
    val ui: StateFlow<CreateEventUiState> = _ui

    private val storage = FirebaseStorage.getInstance().reference
    private val fused = LocationServices.getFusedLocationProviderClient(app)

    fun onTitleChange(v: String) { _ui.value = _ui.value.copy(title = v) }
    fun onDescriptionChange(v: String) { _ui.value = _ui.value.copy(description = v) }
    fun onDateChange(v: LocalDate) { _ui.value = _ui.value.copy(date = v) }
    fun onImageSelected(uri: Uri) { _ui.value = _ui.value.copy(imageUri = uri) }
    fun onLocationNameChange(v: String) { _ui.value = _ui.value.copy(locationName = v) }
    fun onLocationSelected(name: String, lat: Double, lng: Double) {
        _ui.value = _ui.value.copy(locationName = name, latitude = lat, longitude = lng)
    }

    /** Upload image + create Firestore doc */
    fun createEvent(onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isLoading = true)

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw Exception("Not logged in")

                // Get location
                val lat = ui.value.latitude ?: throw Exception("Location not selected")
                val lon = ui.value.longitude ?: throw Exception("Location not selected")

                // Upload image (if exists)
                val imageUrl = ui.value.imageUri?.let { uploadImage(it) }

                // Save event to Firestore
                val id = repo.createUserEvent(
                    title = ui.value.title,
                    description = ui.value.description,
                    date = ui.value.date.toString(),
                    latitude = lat,
                    longitude = lon,
                    imageUrl = imageUrl,
                    location = ui.value.locationName,
                    createdBy = uid
                )

                // add event to user's events created array
                repo.addEventToUserCreatedList(uid, id)

                _ui.value = _ui.value.copy(isLoading = false)
                onSuccess(id)

            } catch (e: Exception) {
                _ui.value = _ui.value.copy(isLoading = false)
                onError(e)
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): String {
        val ref = storage.child("event_images/${System.currentTimeMillis()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}

class CreateEventViewModelFactory(
    private val repo: EventRepository,
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateEventViewModel::class.java)) {
            return CreateEventViewModel(repo, app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
