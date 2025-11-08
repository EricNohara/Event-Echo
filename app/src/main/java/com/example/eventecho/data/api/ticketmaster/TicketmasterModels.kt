package com.example.eventecho.data.api.ticketmaster

// data classes for extracting data from response from ticketmaster api
data class TicketmasterResponse(
    val _embedded: Embedded?
)

data class Embedded(
    val events: List<TicketmasterEvent>?,
)

data class TicketmasterEvent(
    val id: String,
    val name: String,
    val dates: Dates,
    val classifications: List<TicketmasterClassification>?,
    val _embedded: TicketmasterEmbeddedDetails?
)

data class Dates(val start: Start)

data class Start(val localDate: String)

data class TicketmasterClassification(
    val genre: TicketmasterGenre?
)

data class TicketmasterGenre(
    val name: String?
)

data class TicketmasterEmbeddedDetails(
    val venues: List<TicketmasterVenue>?
)

data class TicketmasterVenue(
    val name: String?,
    val location: TicketmasterLocation?
)

data class TicketmasterLocation(
    val latitude: String?,
    val longitude: String?
)

