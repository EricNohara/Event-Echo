package com.example.eventecho.data.firebase

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class UserRepository {
    private val db = Firebase.firestore

    // Create user profile with full schema
    fun createUserWithUsername(uid: String, email: String, username: String) {
        val userData = mapOf(
            "email" to email,
            "username" to username,
            "profilePicUrl" to null,
            "bio" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "eventsAttended" to emptyList<String>(),
            "eventsCreated" to emptyList<String>(),
            "savedEvents" to emptyList<String>(),
            "recentEvents" to emptyList<String>()
        )

        db.collection("users").document(uid).set(userData)
    }

    // Fetch user profile document
    fun getUser(uid: String, onSuccess: (DocumentSnapshot) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener(onSuccess)
    }

    // Update one field
    fun updateUserField(uid: String, field: String, value: Any?) {
        db.collection("users").document(uid).update(field, value)
    }

    // Array field: Add event
    fun addToArray(uid: String, field: String, value: String) {
        db.collection("users").document(uid)
            .update(field, FieldValue.arrayUnion(value))
    }

    // Array field: Remove event
    fun removeFromArray(uid: String, field: String, value: String) {
        db.collection("users").document(uid)
            .update(field, FieldValue.arrayRemove(value))
    }
}