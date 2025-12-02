package com.example.eventecho.data.firebase

import android.util.Log
import com.example.eventecho.data.api.ticketmaster.TicketmasterApi
import com.example.eventecho.ui.dataclass.Event
import com.example.eventecho.ui.dataclass.toEvent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EventRepository(
    private val api: TicketmasterApi,
    private val firestore: FirebaseFirestore
) {

    /** Sync Ticketmaster with Firestore */
    suspend fun syncTicketmasterEvents(
        geoPoint: String,
        startDateTime: String,
        radius: String = "50",
        page: String = "1"
    ) = withContext(Dispatchers.IO) {

        val response = api.getEventsNearby(
            geoPoint = geoPoint,
            startDateTime = startDateTime,
            radius = radius,
            page = page
        )

        val tmEvents = response._embedded?.events ?: emptyList()

        tmEvents.forEach { tmEvent ->
            val venue = tmEvent._embedded?.venues?.firstOrNull()
            val lat = venue?.location?.latitude?.toDoubleOrNull() ?: 0.0
            val lon = venue?.location?.longitude?.toDoubleOrNull() ?: 0.0
            val image = tmEvent.images?.firstOrNull()?.url ?: ""
            val date = tmEvent.dates.start.localDate

            val eventDoc = mapOf(
                "id" to tmEvent.id,
                "title" to tmEvent.name,
                "description" to (tmEvent.info ?: ""),
                "date" to date,
                "location" to (venue?.name ?: ""),
                "latitude" to lat,
                "longitude" to lon,
                "imageUrl" to image,
                "source" to "ticketmaster",
                "createdBy" to null,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("events")
                .document(tmEvent.id) // avoid duplicates
                .set(eventDoc, SetOptions.merge())
        }
    }

    /** Read all events from Firestore */
    suspend fun getEventsFromFirestore(): List<Event> = withContext(Dispatchers.IO) {
        val snap = firestore.collection("events").get().await()
        snap.documents.map { it.toEvent() }
    }

    suspend fun getEventById(id: String): Event? = withContext(Dispatchers.IO) {
        val doc = firestore.collection("events").document(id).get().await()
        return@withContext if (doc.exists()) doc.toEvent() else null
    }

    /** Add a user-created event */
    suspend fun createUserEvent(
        title: String,
        description: String,
        date: String,
        latitude: Double,
        longitude: Double,
        imageUrl: String?,
        createdBy: String
    ) = withContext(Dispatchers.IO) {

        val id = firestore.collection("events").document().id

        val eventDoc = mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "date" to date,
            "location" to "",               // optional, or reverse-geocode later
            "latitude" to latitude,
            "longitude" to longitude,
            "imageUrl" to (imageUrl ?: ""),
            "source" to "user",
            "createdBy" to createdBy,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("events")
            .document(id)
            .set(eventDoc, SetOptions.merge())

        id // return event ID
    }

    // used to track user created events
    suspend fun addEventToUserCreatedList(uid: String, eventId: String) {
        val userDoc = firestore.collection("users").document(uid)

        userDoc.update("eventsCreated", FieldValue.arrayUnion(eventId))
            .addOnFailureListener {
                Log.e("Firestore", "Failed to update eventsCreated", it)
            }
    }

    // used to track last 10 viewed events
    suspend fun addRecentEvent(userId: String, eventId: String) {
        val userRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val current = snapshot.get("recentEvents") as? List<String> ?: emptyList()

            // Remove if exists, then add to front
            val updated = (listOf(eventId) + current.filter { it != eventId })
                .take(10) // keep max 10

            transaction.update(userRef, "recentEvents", updated)
        }
    }

}
