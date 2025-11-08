package com.example.eventecho.data.api.ticketmaster

import com.example.eventecho.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface TicketmasterApi {
    @GET("discovery/v2/events.json")
    suspend fun getEventsNearby(
        @Query("apikey") apiKey: String = BuildConfig.TICKETMASTER_API_KEY,

        // location of the event
        @Query("geoPoint") geoPoint: String,

        // time of the event
        @Query("startDateTime") startDateTime: String,

        // search radius of event
        @Query("radius") radius: String = "50",

        // current page of events
        @Query("page") page: String = "1",
    ): TicketmasterResponse
}