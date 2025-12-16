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
    val description: String?,
    val info: String?,
    val dates: Dates,
    val distance: Double,
    val url: String?,
    val images: List<TicketmasterImage>?,
    val promoter: TicketmasterPromoter?,
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
    val location: TicketmasterLocation?,
    val address: TicketmasterAddress?,
    val city: TicketmasterCity?,
    val state: TicketmasterState?,
)

data class TicketmasterLocation(
    val latitude: String?,
    val longitude: String?
)

data class TicketmasterAddress(
    val line1: String?,
    val line2: String?
)

data class TicketmasterPromoter(
    val name: String?,
)

data class TicketmasterCity(
    val name: String?
)

data class TicketmasterState(
    val state: String?,
    val stateCode: String?
)

data class TicketmasterImage(
    val url: String?,
    val fallback: Boolean?
)