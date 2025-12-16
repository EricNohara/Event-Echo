package com.example.eventecho.data.api.ticketmaster

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TicketmasterRepository(private val api: TicketmasterApi) {
    suspend fun getEvents(
        geoPoint: String,
        startDateTime: String,
        radius: String = "50",
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