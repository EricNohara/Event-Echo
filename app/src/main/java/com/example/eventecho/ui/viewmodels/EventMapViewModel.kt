package com.example.eventecho.ui.viewmodels

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.hsr.geohash.GeoHash
import com.example.eventecho.data.firebase.EventRepository
import com.example.eventecho.data.firebase.UserRepository
import com.example.eventecho.ui.components.EventPin
import com.example.eventecho.ui.dataclass.Event
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class EventMapViewModel(
    private val repo: EventRepository,
    private val userRepo: UserRepository
) : ViewModel()
    {

    // --- UI State ---
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _mapPins = MutableStateFlow<List<EventPin>>(emptyList())
    val mapPins: StateFlow<List<EventPin>> = _mapPins

    private val _cameraMoveEvent = MutableStateFlow<LatLng?>(null)
    val cameraMoveEvent: StateFlow<LatLng?> = _cameraMoveEvent

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // --- Filters ---
    var radiusKm: Int = 10

    var selectedStartDate: LocalDate? = null
    var selectedEndDate: LocalDate? = null

    var showTicketmasterEvents by mutableStateOf(true)
    var showUserEvents by mutableStateOf(true)
    var showCreatedEvents by mutableStateOf(false)
    var showAttendedEvents by mutableStateOf(false)
    var showFavoriteEvents by mutableStateOf(false)

    var lastCenter: LatLng? = null

    // CAMERA IDLE → LOAD FIRESTORE EVENTS
    @RequiresApi(Build.VERSION_CODES.O)
    fun onMapCameraIdle(center: LatLng) {
        lastCenter = center

        viewModelScope.launch {
            try {
                _isLoading.value = true
                refreshEvents(center)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // FULL PIPELINE: Ticketmaster, Firestore, Filter, Pins
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun refreshEvents(center: LatLng) {
        try {
            // 1. Create geohash
            val geoHash = GeoHash.withCharacterPrecision(
                center.latitude,
                center.longitude,
                6
            ).toBase32()

            // 2. Current time in ISO
            val nowIso = ZonedDateTime.now(ZoneOffset.UTC)
                .withNano(0)
                .format(DateTimeFormatter.ISO_INSTANT)

            // 3. Sync Ticketmaster → Firestore
            if (showTicketmasterEvents) {
                repo.syncTicketmasterEvents(
                    geoPoint = geoHash,
                    startDateTime = nowIso,
                    radius = radiusKm.toString()
                )
            }

            // 4. Load Firestore events
            val allEvents = repo.getEventsFromFirestore()

            // 5. Apply radius + date filtering
            val filtered = allEvents
                .filter { isEventIncluded(it, center) }
                .sortedBy { LocalDate.parse(it.date) }

            // 6. Push to UI
            _events.value = filtered

            _mapPins.value = filtered.map {
                EventPin(
                    id = it.id,
                    title = it.title,
                    snippet = it.date,
                    location = LatLng(it.latitude, it.longitude)
                )
            }

        } catch (e: Exception) {
            Log.e("EventMapVM", "Error refreshing events", e)
            _events.value = emptyList()
            _mapPins.value = emptyList()
        }
    }


    // Getting user events - AI-Assisted Feature
    private var userEventsCreated: Set<String> = emptySet() // FIXED/ADDED
    private var userEventsAttended: Set<String> = emptySet() // FIXED/ADDED
    private var userEventsFavorite: Set<String> = emptySet() // FIXED/ADDED

    init {
        viewModelScope.launch {
            // Load user document
            val data = userRepo.getUser() ?: return@launch

            userEventsCreated =
                (data["eventsCreated"] as? List<*>)?.filterIsInstance<String>()?.toSet()
                    ?: emptySet()

            userEventsAttended =
                (data["eventsAttended"] as? List<*>)?.filterIsInstance<String>()?.toSet()
                    ?: emptySet()

            userEventsFavorite =
                (data["savedEvents"] as? List<*>)?.filterIsInstance<String>()?.toSet()
                    ?: emptySet()
        }
    }


    // --- FILTERING ---
    private fun isEventIncluded(event: Event, center: LatLng): Boolean {
        // RADIUS FILTER
        val dist = FloatArray(1)
        android.location.Location.distanceBetween(
            center.latitude, center.longitude,
            event.latitude, event.longitude,
            dist
        )
        val km = dist[0] / 1000.0
        if (km > radiusKm) return false

        // DATE RANGE FILTER
        try {
            val eventDate = LocalDate.parse(event.date)

            val afterStart = selectedStartDate?.let { eventDate >= it } ?: true
            val beforeEnd = selectedEndDate?.let { eventDate <= it } ?: true

            if (!afterStart) return false
            if (!beforeEnd) return false

        } catch (e: Exception) {
            // If the date is invalid, exclude it so it doesn't break filtering
            return false
        }

        // EVENT SOURCE FILTER

        // Ticketmaster events
        if (!showTicketmasterEvents && event.source == "ticketmaster") return false

        // User events
        if (!showUserEvents && event.source == "user") return false

        // CREATED / ATTENDED / FAVORITE FILTER
        val filtersSelected = listOf(showCreatedEvents, showAttendedEvents, showFavoriteEvents)
        if (filtersSelected.any { it }) {
            var passesCustomFilter = false
            if (showCreatedEvents && userEventsCreated.contains(event.id)) passesCustomFilter = true
            if (showAttendedEvents && userEventsAttended.contains(event.id)) passesCustomFilter = true
            if (showFavoriteEvents && userEventsFavorite.contains(event.id)) passesCustomFilter = true

            if (!passesCustomFilter) return false // if none of the selected filters match, exclude event
        }
        // if none of the created/attended/favorite filters are selected, do not filter on them

        return true
    }


    // --- SEARCH BAR ---
    fun performSearch(query: String, context: Context) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context)
                    val results = geocoder.getFromLocationName(trimmed, 1)

                    if (!results.isNullOrEmpty()) {
                        val loc = results[0]
                        _cameraMoveEvent.value = LatLng(loc.latitude, loc.longitude)
                    }
                } catch (e: Exception) {
                    Log.e("EventSearch", "Geocoder error", e)
                }
            }
        }
    }

    // --- GPS & CAMERA EVENTS ---
    fun updateUserLocation(location: LatLng) {
        _userLocation.value = location
    }

    fun onCameraMoved() {
        _cameraMoveEvent.value = null
    }
}
