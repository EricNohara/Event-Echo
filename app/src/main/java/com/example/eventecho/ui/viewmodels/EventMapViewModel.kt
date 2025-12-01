package com.example.eventecho.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.hsr.geohash.GeoHash
import com.example.eventecho.data.firebase.EventRepository
import com.example.eventecho.ui.dataclass.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class EventMapViewModel(
    val repo: EventRepository,
    app: Application
) : AndroidViewModel(app) {


    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    // current user location
    private var currentLat: Double = 42.3555    // fallback: Boston
    private var currentLon: Double = -71.0565

    // filters
    var radius: Int = 50
    var selectedDate: LocalDate = LocalDate.now()

    private val fusedLocation = LocationServices.getFusedLocationProviderClient(app)

    init {
        loadLocationThenEvents()
    }

    /** Load GPS → Sync TM → Load Firestore events */
    private fun loadLocationThenEvents() {
        viewModelScope.launch {
            getUserLocation()
            refreshEvents()
        }
    }

    /** Refresh = Sync Ticketmaster + Load from Firestore */
    fun refreshEvents() {
        viewModelScope.launch {
            val geoHash = getGeoHash()
            val startDateTime = selectedDate.atStartOfDay().toInstant(ZoneOffset.UTC)
                .toString() // ISO-8601

            // 1️⃣ Sync TM events into Firestore
            repo.syncTicketmasterEvents(
                geoPoint = geoHash,
                startDateTime = startDateTime,
                radius = radius.toString()
            )

            // 2️⃣ Load all events from Firestore
            val allEvents = repo.getEventsFromFirestore()

            // 3️⃣ Filter by location + date
            val filtered = allEvents.filter { event ->
                isWithinRadius(event.latitude, event.longitude) &&
                        isOnOrAfterDate(event.date)
            }

            _events.value = filtered
        }
    }

    /** Request real device location */
    @SuppressLint("MissingPermission")
    private suspend fun getUserLocation() {
        try {
            val location: Location? = fusedLocation.lastLocation.await()
            if (location != null) {
                currentLat = location.latitude
                currentLon = location.longitude
            }
        } catch (_: Exception) { }
    }

    /** Convert lat/lon → GeoHash */
    private fun getGeoHash(): String {
        return GeoHash.withCharacterPrecision(currentLat, currentLon, 6).toBase32()
    }

    /** Check if event is within radius */
    private fun isWithinRadius(eventLat: Double, eventLon: Double): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLat, currentLon,
            eventLat, eventLon,
            results
        )
        val distanceKm = results[0] / 1000.0
        return distanceKm <= radius
    }

    /** Check date filter */
    private fun isOnOrAfterDate(eventDate: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val eventLocal = LocalDate.parse(eventDate, formatter)
            !eventLocal.isBefore(selectedDate)
        } catch (e: Exception) {
            true
        }
    }
}
