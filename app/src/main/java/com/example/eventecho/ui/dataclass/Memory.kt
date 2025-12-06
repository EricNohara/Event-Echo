package com.example.eventecho.ui.dataclass

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class Memory(
    val userId: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val upvoteCount: Int = 0,
    val upvotedBy: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

fun DocumentSnapshot.toMemory(): Memory? {
    return try {
        Memory(
            userId = getString("userId") ?: "",
            description = getString("description") ?: "",
            imageUrl = getString("imageUrl") ?: "",
            upvoteCount = getLong("upvoteCount")?.toInt() ?: 0,
            upvotedBy = get("upvotedBy") as? List<String> ?: emptyList(),
            createdAt = getTimestamp("createdAt"),
            updatedAt = getTimestamp("updatedAt")
        )
    } catch (e: Exception) {
        null
    }
}
