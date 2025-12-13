package com.example.eventecho.data.firebase

import android.net.Uri
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

        Log.d("EventRepository", "Syncing Ticketmaster events: geo=$geoPoint start=$startDateTime radius=$radius page=$page")

        val response = api.getEventsNearby(
            geoPoint = geoPoint,
            startDateTime = startDateTime,
            radius = radius,
            page = page
        )

        val tmEvents = response._embedded?.events ?: emptyList()

        Log.d("EventRepository", "Ticketmaster returned ${tmEvents.size} events")

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

            Log.v("EventRepository", "Writing event ${tmEvent.id} to Firestore")

            firestore.collection("events")
                .document(tmEvent.id) // avoid duplicates
                .set(eventDoc, SetOptions.merge())
                .addOnSuccessListener {
                    Log.v("EventRepository", "Successfully wrote event ${tmEvent.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("EventRepository", "Failed writing event ${tmEvent.id}", e)
                }
        }
    }

    /** Read all events from Firestore */
    suspend fun getEventsFromFirestore(): List<Event> = withContext(Dispatchers.IO) {
        Log.d("EventRepository", "Fetching ALL events from Firestore")
        val snap = firestore.collection("events").get().await()

        Log.d("EventRepository", "Firestore returned ${snap.size()} events")
        snap.documents.map { it.toEvent() }
    }

    suspend fun getEventById(id: String): Event? = withContext(Dispatchers.IO) {
        Log.d("EventRepository", "Fetching event by ID: $id")
        val doc = firestore.collection("events").document(id).get().await()

        if (doc.exists()) {
            Log.d("EventRepository", "Event $id FOUND")
            doc.toEvent()
        } else {
            Log.d("EventRepository", "Event $id NOT FOUND")
            null
        }

        return@withContext if (doc.exists()) doc.toEvent() else null
    }

    /** Add a user-created event */
    suspend fun createUserEvent(
        title: String,
        description: String,
        date: String,
        latitude: Double,
        longitude: Double,
        location: String? = "",
        imageUrl: String?,
        createdBy: String
    ) = withContext(Dispatchers.IO) {

        val id = firestore.collection("events").document().id
        Log.d("EventRepository", "Creating user event id=$id title=$title ($latitude,$longitude)")

        val eventDoc = mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "date" to date,
            "location" to location,
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
            .addOnSuccessListener {
                Log.d("EventRepository", "User event $id successfully created")
            }
            .addOnFailureListener {
                Log.e("EventRepository", "Failed to create user event $id", it)
            }

        id // return event ID
    }

    // used to track user created events
    suspend fun addEventToUserCreatedList(uid: String, eventId: String) {
        Log.d("EventRepository", "Adding event $eventId to userCreated list for $uid")
        val userDoc = firestore.collection("users").document(uid)

        userDoc.update("eventsCreated", FieldValue.arrayUnion(eventId))
            .addOnFailureListener {
                Log.e("Firestore", "Failed to update eventsCreated", it)
            }
    }

    // used to track last 10 viewed events
    suspend fun addRecentEvent(userId: String, eventId: String) {
        Log.d("EventRepository", "Running transaction: add recent event $eventId for user=$userId")
        val userRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val current = snapshot.get("recentEvents") as? List<String> ?: emptyList()

            // Remove if exists, then add to front
            val updated = (listOf(eventId) + current.filter { it != eventId })
                .take(10) // keep max 10

            transaction.update(userRef, "recentEvents", updated)
            Log.d("EventRepository", "Transaction: recent events updated for $userId")
        }
    }

    // favorite events
    suspend fun addFavoriteEvent(uid: String, eventId: String) {
        Log.d("EventRepository", "Adding favorite event $eventId for $uid")
        val userRef = firestore.collection("users").document(uid)
        userRef.update("savedEvents", FieldValue.arrayUnion(eventId))
    }

    suspend fun removeFavoriteEvent(uid: String, eventId: String) {
        Log.d("EventRepository", "Removing favorite event $eventId for $uid")
        val userRef = firestore.collection("users").document(uid)
        userRef.update("savedEvents", FieldValue.arrayRemove(eventId))
    }

    suspend fun getUserSavedEvents(uid: String): List<String> {
        Log.d("EventRepository", "Fetching saved events for $uid")
        val doc = firestore.collection("users").document(uid).get().await()

        val saved = doc.get("savedEvents") as? List<String> ?: emptyList()
        Log.d("EventRepository", "User $uid has ${saved.size} saved events")

        return doc.get("savedEvents") as? List<String> ?: emptyList()
    }

    // user created events
    suspend fun getEventsCreatedByUser(uid: String): List<Event> {
        Log.d("EventRepository", "Fetching user-created events for $uid")
        val snapshot = firestore.collection("events")
            .whereEqualTo("createdBy", uid)
            .get()
            .await()
        Log.d("EventRepository", "User $uid has ${snapshot.size()} created events")
        return snapshot.documents.mapNotNull { it.toObject(Event::class.java)?.copy(id = it.id) }
    }

    // get user's profile who created the event
    suspend fun getUserProfile(uid: String): Pair<String?, String?> = withContext(Dispatchers.IO) {
        Log.d("EventRepository", "Fetching user profile for $uid")

        val doc = firestore.collection("users").document(uid).get().await()

        if (!doc.exists()) {
            Log.w("EventRepository", "User profile not found for $uid")
            return@withContext (null to null)
        }

        val username = doc.getString("username")
        val profileUrl = doc.getString("profilePicUrl")

        return@withContext (username to profileUrl)
    }

    // helper to delete images from firebase storage
    private suspend fun deleteImageFromStorage(imageUrl: String) {
        if (imageUrl.isBlank()) return

        val storageRef = com.google.firebase.storage.FirebaseStorage
            .getInstance()
            .getReferenceFromUrl(imageUrl)

        storageRef.delete().await()
    }

    // delete event
    suspend fun deleteEvent(eventId: String, ownerUid: String) = withContext(Dispatchers.IO) {
        val eventRef = firestore.collection("events").document(eventId)
        val memoryWallRef = eventRef.collection("memories")

        // Fetch memory wall entries
        val memories = memoryWallRef.get().await()

        for (doc in memories.documents) {
            val imageUrl = doc.getString("imageUrl") ?: ""
            val upvoteCount = doc.getLong("upvoteCount") ?: 0L
            val memoryOwner = doc.getString("userId")

            // Delete image from Storage
            deleteImageFromStorage(imageUrl)

            if (memoryOwner != null) {

                val updates = mutableMapOf<String, Any>()

                // Subtract upvotes from poster
                if (upvoteCount > 0) {
                    updates["totalUpvotesReceived"] = FieldValue.increment(-upvoteCount)
                }

                // Remove event from attended list
                updates["eventsAttended"] = FieldValue.arrayRemove(eventId)

                // Single user update (atomic)
                firestore.collection("users")
                    .document(memoryOwner)
                    .update(updates)
                    .await()
            }

            // Delete memory document
            doc.reference.delete().await()
        }

        // ️Delete the event document itself
        eventRef.delete().await()

        // Remove from creator’s eventsCreated
        firestore.collection("users")
            .document(ownerUid)
            .update("eventsCreated", FieldValue.arrayRemove(eventId))
            .await()

        // Cleanup references (favorites / recents)
        val usersSnap = firestore.collection("users").get().await()
        for (user in usersSnap.documents) {
            user.reference.update(
                mapOf(
                    "savedEvents" to FieldValue.arrayRemove(eventId),
                    "recentEvents" to FieldValue.arrayRemove(eventId)
                )
            )
        }
    }

    // update event
    suspend fun updateEvent(
        eventId: String,
        title: String,
        description: String,
        date: String,
        latitude: Double,
        longitude: Double,
        location: String,
        newImageUri: Uri?,
        oldImageUrl: String?
    ) = withContext(Dispatchers.IO) {

        var imageUrl = oldImageUrl ?: ""

        // Replace image if user selected a new one
        if (newImageUri != null) {
            // delete old image if it exists
            if (!oldImageUrl.isNullOrBlank()) {
                try {
                    val oldRef = com.google.firebase.storage.FirebaseStorage
                        .getInstance()
                        .getReferenceFromUrl(oldImageUrl)
                    oldRef.delete().await()
                } catch (_: Exception) {}
            }

            // upload new image
            val newRef = com.google.firebase.storage.FirebaseStorage
                .getInstance()
                .reference
                .child("event_images/$eventId/${System.currentTimeMillis()}.jpg")

            newRef.putFile(newImageUri).await()
            imageUrl = newRef.downloadUrl.await().toString()
        }

        firestore.collection("events")
            .document(eventId)
            .update(
                mapOf(
                    "title" to title,
                    "description" to description,
                    "date" to date,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "location" to location,
                    "imageUrl" to imageUrl,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }
}
