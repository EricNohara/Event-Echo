package com.example.eventecho.ui.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.hsr.geohash.GeoHash
import com.example.eventecho.data.api.ticketmaster.EventRepository
import com.example.eventecho.data.api.ticketmaster.TicketmasterEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class EventMapViewModel(private val repo: EventRepository): ViewModel() {
    private val _events = MutableStateFlow<List<TicketmasterEvent>>(emptyList())
    val events: StateFlow<List<TicketmasterEvent>> = _events

    // Inputs to the API
    // default Boston
    var lat: Double = 42.3555
    var long: Double = 71.0565

    @RequiresApi(Build.VERSION_CODES.O)
    var startDateTime: String = ZonedDateTime.now(ZoneOffset.UTC)
        .withNano(0) // remove milliseconds
        .format(DateTimeFormatter.ISO_INSTANT)

    var radius: String = "50"
    var page: String = "1"

    // helper function for calculating geo hashes used as input for the API
    fun getGeoHash(): String {
        return GeoHash.withCharacterPrecision(lat, long, 6).toBase32()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchEvents() {
        viewModelScope.launch {
            try {
                Log.d("EventMapViewModel", "Fetching events with radius=$radius, start=$startDateTime")

                // fetch the events for the specified inputs and set it to the events list
                val geoPoint = getGeoHash()
                val response = repo.getEvents(geoPoint, startDateTime, radius, page)
                _events.value = response._embedded?.events ?: emptyList()
            } catch (e: Exception) {
                // set events list to empty
                Log.e("EventMapViewModel", "Error fetching events", e)
                _events.value = emptyList()
            }
        }
    }
}