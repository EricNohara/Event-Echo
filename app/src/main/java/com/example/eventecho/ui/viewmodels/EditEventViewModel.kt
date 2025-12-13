package com.example.eventecho.ui.viewmodels

import android.app.Application
import android.location.Geocoder
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.eventecho.data.firebase.EventRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.Locale

data class EditEventUiState(
    val title: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),

    val locationName: String = "",
    val locationLat: Double? = null,
    val locationLng: Double? = null,

    val imageUri: Uri? = null,
    val existingImageUrl: String? = null,

    val usingCurrentLocation: Boolean = false,
    val isLoading: Boolean = false
)

class EditEventViewModel(
    private val repo: EventRepository,
    private val eventId: String,
    app: Application
) : AndroidViewModel(app) {

    private val _ui = MutableStateFlow(EditEventUiState())
    val ui: StateFlow<EditEventUiState> = _ui

    private val fused = LocationServices
        .getFusedLocationProviderClient(app)

    private val geocoder = Geocoder(app, Locale.getDefault())

    init {
        loadEvent()
    }

    private fun loadEvent() = viewModelScope.launch {
        val event = repo.getEventById(eventId) ?: return@launch

        _ui.update {
            it.copy(
                title = event.title,
                description = event.description,
                date = LocalDate.parse(event.date),
                locationName = event.location,
                locationLat = event.latitude,
                locationLng = event.longitude,
                existingImageUrl = event.imageUrl
            )
        }
    }

    // --------- FIELD UPDATES ---------

    fun onTitleChange(v: String) =
        _ui.update { it.copy(title = v) }

    fun onDescriptionChange(v: String) =
        _ui.update { it.copy(description = v) }

    fun onDateChange(v: LocalDate) =
        _ui.update { it.copy(date = v) }

    fun onLocationNameChange(v: String) =
        _ui.update { it.copy(locationName = v) }

    fun onLocationSelected(name: String, lat: Double, lng: Double) =
        _ui.update {
            it.copy(
                locationName = name,
                locationLat = lat,
                locationLng = lng
            )
        }

    fun onImageSelected(uri: Uri) =
        _ui.update { it.copy(imageUri = uri) }

    // --------- CURRENT LOCATION ---------

    fun enableCurrentLocation() {
        viewModelScope.launch {
            try {
                val loc = fused.lastLocation.await() ?: return@launch

                val address = geocoder
                    .getFromLocation(loc.latitude, loc.longitude, 1)
                    ?.firstOrNull()

                val readableName =
                    address?.getAddressLine(0) ?: "Current Location"

                _ui.update {
                    it.copy(
                        usingCurrentLocation = true,
                        locationName = readableName,
                        locationLat = loc.latitude,
                        locationLng = loc.longitude
                    )
                }
            } catch (_: Exception) {
                _ui.update {
                    it.copy(
                        usingCurrentLocation = true,
                        locationName = "Current Location"
                    )
                }
            }
        }
    }

    fun disableCurrentLocation() {
        _ui.update { it.copy(usingCurrentLocation = false) }
    }

    fun clearManualLocation() {
        _ui.update {
            it.copy(
                locationName = "",
                locationLat = null,
                locationLng = null
            )
        }
    }

    // --------- SAVE ---------

    fun saveChanges(onSuccess: () -> Unit) = viewModelScope.launch {
        val s = _ui.value
        _ui.update { it.copy(isLoading = true) }

        repo.updateEvent(
            eventId = eventId,
            title = s.title,
            description = s.description,
            date = s.date.toString(),
            latitude = s.locationLat!!,
            longitude = s.locationLng!!,
            location = s.locationName,
            newImageUri = s.imageUri,
            oldImageUrl = s.existingImageUrl
        )

        _ui.update { it.copy(isLoading = false) }
        onSuccess()
    }
}

class EditEventViewModelFactory(
    private val repo: EventRepository,
    private val eventId: String,
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditEventViewModel(repo, eventId, app) as T
    }
}
