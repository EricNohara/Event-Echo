package com.example.eventecho.ui.viewmodels

import android.app.Application
import android.location.Geocoder
import android.net.Uri
import android.util.Log
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
import java.util.Locale

data class CreateEventUiState(
    val title: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val imageUri: Uri? = null,

    // The text typed by the user OR selected suggestion
    val locationName: String = "",

    // FINAL location chosen for the event
    val locationLat: Double? = null,
    val locationLng: Double? = null,

    // ACTUAL USER’S CURRENT DEVICE LOCATION (for distance calculations)
    val userLat: Double? = null,
    val userLng: Double? = null,

    val isLoading: Boolean = false,
    val usingCurrentLocation: Boolean = false,
)

class CreateEventViewModel(
    private val repo: EventRepository,
    app: Application
) : AndroidViewModel(app) {

    private val _ui = MutableStateFlow(CreateEventUiState())
    val ui: StateFlow<CreateEventUiState> = _ui

    private val storage = FirebaseStorage.getInstance().reference
    private val fused = LocationServices.getFusedLocationProviderClient(app)

    init {
        // Fetch user's device location when screen opens
        viewModelScope.launch {
            try {
                val loc = fused.lastLocation.await()
                _ui.value = _ui.value.copy(
                    userLat = loc?.latitude,
                    userLng = loc?.longitude
                )
            } catch (_: Exception) {
                // silently fail — location not required for event creation
            }
        }
    }

    // --- Standard field updates ---
    fun onTitleChange(v: String) = applyUpdate { copy(title = v) }
    fun onDescriptionChange(v: String) = applyUpdate { copy(description = v) }
    fun onDateChange(v: LocalDate) = applyUpdate { copy(date = v) }
    fun onImageSelected(uri: Uri) = applyUpdate { copy(imageUri = uri) }
    fun onLocationNameChange(v: String) = applyUpdate { copy(locationName = v) }

    fun onLocationSelected(name: String, lat: Double, lng: Double) {
        _ui.value = _ui.value.copy(
            locationName = name,
            locationLat = lat,
            locationLng = lng
        )
    }

    private fun applyUpdate(update: CreateEventUiState.() -> CreateEventUiState) {
        _ui.value = _ui.value.update()
    }

    fun useCurrentLocation() {
        viewModelScope.launch {
            try {
                val loc = fused.lastLocation.await() ?: return@launch

                val geocoder = Geocoder(getApplication<Application>(), Locale.getDefault())
                val results = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                val address = results?.firstOrNull()

                // Build clean readable name
                val readableName = address?.getAddressLine(0) ?: "Current Location"

                _ui.value = _ui.value.copy(
                    usingCurrentLocation = true,
                    locationName = readableName,
                    locationLat = loc.latitude,
                    locationLng = loc.longitude
                )

            } catch (e: Exception) {
                println("Reverse geocoding failed: $e")

                _ui.value = _ui.value.copy(
                    usingCurrentLocation = true,
                    locationName = "Current Location",
                    locationLat = null,
                    locationLng = null
                )
            }
        }
    }

    fun enableCurrentLocation() {
        useCurrentLocation()
    }

    fun disableCurrentLocation() {
        _ui.value = _ui.value.copy(
            usingCurrentLocation = false,
            locationLat = null,
            locationLng = null
        )
    }

    fun clearManualLocation() {
        _ui.value = _ui.value.copy(
            locationName = "",
            locationLat = null,
            locationLng = null
        )
    }

    // --- EVENT CREATION ---
    fun createEvent(onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isLoading = true)

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw Exception("Not logged in")

                // Ensure location is selected
                val lat = ui.value.locationLat
                    ?: throw Exception("Location not selected")

                val lng = ui.value.locationLng
                    ?: throw Exception("Location not selected")

                // Upload image
                val imageUrl = ui.value.imageUri?.let { uploadImage(it) }

                // Save event to Firestore
                val id = repo.createUserEvent(
                    title = ui.value.title,
                    description = ui.value.description,
                    date = ui.value.date.toString(),
                    latitude = lat,
                    longitude = lng,
                    imageUrl = imageUrl,
                    location = ui.value.locationName,
                    createdBy = uid
                )

                // Add to user's profile list
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
