package com.example.eventecho.data.firebase

import android.net.Uri
import com.example.eventecho.ui.dataclass.Memory
import com.example.eventecho.ui.dataclass.toMemory
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class MemoryRepository(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    // Upload memory picture in firebase storage
    suspend fun uploadMemoryImage(eventId: String, userId: String, imageUri: Uri): String {
        val ref = storage.reference.child("memory_wall/$eventId/$userId.jpg")

        // Prevent overwrite: if metadata exists, memory already uploaded
        try {
            ref.metadata.await()
            throw Exception("Memory already exists for this user. Cannot upload again.")
        } catch (_: Exception) {
            // If metadata lookup fails, file does not exist → safe to upload
        }

        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    // Create a user's memory for an event - cant be edited or deleted after upload
    suspend fun createMemory(
        eventId: String,
        userId: String,
        description: String,
        imageUrl: String
    ) {
        val memoryRef = db.collection("events")
            .document(eventId)
            .collection("memories")
            .document(userId)

        // check if memory already exists
        val existing = memoryRef.get().await()
        if (existing.exists()) {
            throw Exception("Memory already exists for this user. Cannot create again.")
        }

        val memoryDoc = mapOf(
            "userId" to userId,
            "description" to description,
            "imageUrl" to imageUrl,
            "upvoteCount" to 0,
            "upvotedBy" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        // safe to write once
        memoryRef.set(memoryDoc).await()
    }

    // Get all memories for a wall for a given event
    suspend fun getMemories(eventId: String): List<Memory> {
        val snap = db.collection("events")
            .document(eventId)
            .collection("memories")
            .get()
            .await()

        return snap.documents.mapNotNull { it.toMemory() }
    }

    // Upvote a memory
    suspend fun toggleUpvote(eventId: String, memoryOwnerId: String, voterId: String) {
        val memoryRef = db.collection("events")
            .document(eventId)
            .collection("memories")
            .document(memoryOwnerId)

        // 1. Run upvote toggle
        db.runTransaction { tx ->
            val snapshot = tx.get(memoryRef)
            val upvotedBy = snapshot.get("upvotedBy") as? List<String> ?: emptyList()

            val isCurrentlyUpvoted = voterId in upvotedBy

            if (isCurrentlyUpvoted) {
                // Remove upvote
                tx.update(memoryRef, mapOf(
                    "upvoteCount" to FieldValue.increment(-1),
                    "upvotedBy" to FieldValue.arrayRemove(voterId)
                ))

                // Decrement owner's total
                tx.update(
                    db.collection("users").document(memoryOwnerId),
                    "totalUpvotesReceived",
                    FieldValue.increment(-1)
                )

            } else {
                // Add upvote
                tx.update(memoryRef, mapOf(
                    "upvoteCount" to FieldValue.increment(1),
                    "upvotedBy" to FieldValue.arrayUnion(voterId)
                ))

                // Increment owner's total
                tx.update(
                    db.collection("users").document(memoryOwnerId),
                    "totalUpvotesReceived",
                    FieldValue.increment(1)
                )
            }
        }.await()
    }
}
