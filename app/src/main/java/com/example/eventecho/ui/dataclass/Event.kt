package com.example.eventecho.ui.dataclass

import com.google.firebase.firestore.DocumentSnapshot

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = "",
    val source: String = "", // "ticketmaster" or "user"
    val createdBy: String? = null
)

fun DocumentSnapshot.toEvent(): Event {
    return Event(
        id = getString("id") ?: id,
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        date = getString("date") ?: "",
        location = getString("location") ?: "",
        latitude = getDouble("latitude") ?: 0.0,
        longitude = getDouble("longitude") ?: 0.0,
        imageUrl = getString("imageUrl") ?: "",
        source = getString("source") ?: "",
        createdBy = getString("createdBy")
    )
}