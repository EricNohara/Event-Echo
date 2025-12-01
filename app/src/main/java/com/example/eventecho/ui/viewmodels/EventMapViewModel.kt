package com.example.eventecho.ui.viewmodels

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.hsr.geohash.GeoHash
import com.example.eventecho.data.api.ticketmaster.EventRepository
import com.example.eventecho.data.api.ticketmaster.TicketmasterEvent
import com.example.eventecho.ui.components.EventPin
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class SearchCircle(
    val center: LatLng,
    val radiusMeters: Double
)

class EventMapViewModel(private val repo: EventRepository) : ViewModel() {

    private val _events = MutableStateFlow<List<TicketmasterEvent>>(emptyList())
    val events: StateFlow<List<TicketmasterEvent>> = _events

    private val _mapPins = MutableStateFlow<List<EventPin>>(emptyList())
    val mapPins: StateFlow<List<EventPin>> = _mapPins

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    //move camera once search by geocoder
    private val _cameraMoveEvent = MutableStateFlow<LatLng?>(null)
    val cameraMoveEvent: StateFlow<LatLng?> = _cameraMoveEvent
    private val _searchCircle = MutableStateFlow<SearchCircle?>(null)
    val searchCircle: StateFlow<SearchCircle?> = _searchCircle


    private var lastFetchLocation: LatLng? = null
    fun performSearch(query: String, context: Context) {
        val query = query.trim()
        if (query.isBlank()) return
        if (query.length > 50) return

        // user provided literal lat, lng coordinates
        val latlngCoords = try {
            val parts = query.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null) {
                    LatLng(lat,lng)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        if (latlngCoords != null ) {
            _cameraMoveEvent.value = latlngCoords
            return
        }

        //user types the name of a place
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context)
                    val results = geocoder.getFromLocationName(query, 1)

                    if (!results.isNullOrEmpty()) {
                        val location = results[0]
                        val latLng = LatLng(location.latitude, location.longitude)

                        _cameraMoveEvent.value = latLng
                    } else {
                        Log.w("Search", "No location was found for this search by user: $query")
                    }
                } catch (e: Exception) {
                    Log.e("Search", "Geocoding has failed", e)
                }
            }
        }
    }

    // after the UI moves camera, we call this  so we don't move camera again
    fun onCameraMoved() {
        _cameraMoveEvent.value = null
    }

    fun updateUserLocation(location: LatLng) {
        _userLocation.value = location
        // fetch events just the first time user location is known
        if (_events.value.isEmpty()) {
            // TODO:  figure out what if system version isn't supporting the functioanlity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fetchEvents(location.latitude, location.longitude)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onMapCameraIdle(center: LatLng) {
        val last = lastFetchLocation
        if (last != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(last.latitude,last.longitude,center.latitude,center.longitude,results)
            if (results[0] < 500) return
        }
        fetchEvents(center.latitude, center.longitude)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchEvents(lat: Double, long: Double) {
        viewModelScope.launch {
            try {
                lastFetchLocation = LatLng(lat, long)

                // ticketmaster uses miles, google maps uses meters.
                val radiusInMiles = repo.defaultRadius.toDoubleOrNull() ?: 10.0
                val radiusInMeters = radiusInMiles * 1609.34

                _searchCircle.value = SearchCircle(
                    center = LatLng(lat, long),
                    radiusMeters = radiusInMeters
                )
                val geoPoint = GeoHash.withCharacterPrecision(lat, long, 6).toBase32()
                val startDateTime = ZonedDateTime.now(ZoneOffset.UTC)
                    .withNano(0) //remove nanoseconds to simplify format
                    .format(DateTimeFormatter.ISO_INSTANT) //convert date to ISO 8601 format

                val response = repo.getEvents(geoPoint, startDateTime) // default= radius=50
                val rawEvents = response._embedded?.events ?: emptyList()
                _events.value = rawEvents

                //create the map pins for events and skips the events that are null
                _mapPins.value = rawEvents.mapNotNull { event ->
                    val venue = event._embedded?.venues?.firstOrNull()?.location
                    if (venue?.latitude != null && venue.longitude != null) { //make sure the location exists from the response
                        EventPin(
                            id = event.id,
                            title = event.name,
                            snippet = event.dates.start.localDate,
                            location = LatLng(venue.latitude.toDouble(), venue.longitude.toDouble())
                        )
                    }
                    else {
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e("VM", "Fetch error", e)
                _events.value = emptyList() //empty the events list if the fetch fails
            }
        }
    }
}