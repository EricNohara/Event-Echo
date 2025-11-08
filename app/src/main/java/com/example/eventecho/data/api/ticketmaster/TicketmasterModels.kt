package com.example.eventecho.data.api.ticketmaster

data class TicketmasterEvent(
    val name: String,
    val dates: Dates,
    val url: String
)

data class Dates(val start: Start)

data class Start(val localDate: String)

data class TicketmasterResponse(
    val _embedded: Embedded?
)

data class Embedded(
    val events: List<TicketmasterEvent>?,
)