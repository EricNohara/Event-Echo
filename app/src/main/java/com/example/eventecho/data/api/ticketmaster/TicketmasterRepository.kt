package com.example.eventecho.data.api.ticketmaster

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepository(private val api: TicketmasterApi) {
    var defaultRadius: String = "5" //radius for map circle display and event fetching
    suspend fun getEvents(
        geoPoint: String,
        startDateTime: String,
        radius: String = defaultRadius,
        page: String = "1"
    ) = withContext(Dispatchers.IO) {
        api.getEventsNearby(
            geoPoint = geoPoint,
            startDateTime = startDateTime,
            radius = radius,
            page = page
        )
    }
}