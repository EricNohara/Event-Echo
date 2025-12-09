package com.example.eventecho.data.firebase

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    /** Safely return UID or null */
    fun getUid(): String? = FirebaseAuth.getInstance().currentUser?.uid

    /** CREATE USER DOCUMENT AFTER SIGNUP */
    fun createUser(uid: String, email: String, username: String) {
        val userData = mapOf(
            "email" to email,
            "username" to username,
            "bio" to "",
            "profilePicUrl" to null,
            "createdAt" to FieldValue.serverTimestamp(),
            "totalUpvotesReceived" to 0,
            "eventsAttended" to emptyList<String>(),
            "eventsCreated" to emptyList<String>(),
            "savedEvents" to emptyList<String>(),
            "recentEvents" to emptyList<String>()
        )

        db.collection("users").document(uid).set(userData)
    }

    /** FETCH USER PROFILE (safe if no user logged in) */
    fun getUser(onSuccess: (Map<String, Any>) -> Unit) {
        val uid = getUid() ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) onSuccess(doc.data ?: emptyMap())
            }
    }

    /** UPDATE TEXT FIELDS (safe) */
    fun updateUserFields(username: String, bio: String) {
        val uid = getUid() ?: return
        db.collection("users").document(uid)
            .update("username", username, "bio", bio)
    }

    /** UPDATE PROFILE PICTURE URL IN FIRESTORE */
    fun updateProfilePic(url: String) {
        val uid = getUid() ?: return
        db.collection("users").document(uid)
            .update("profilePicUrl", url)
    }

    /** UPLOAD PROFILE PICTURE TO STORAGE */
    fun uploadProfilePicture(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = getUid() ?: return onError(Exception("User not logged in."))
        val ref = storage.reference.child("profile_pictures/$uid.jpg")

        ref.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                ref.downloadUrl
            }
            .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
            .addOnFailureListener(onError)
    }

    // Add event to attended events - safe
    suspend fun addEventToAttended(eventId: String) {
        val uid = getUid() ?: return
        db.collection("users")
            .document(uid)
            .update("eventsAttended", FieldValue.arrayUnion(eventId))
            .await()
    }

    // Get events user attended - safe
    suspend fun getEventsAttended(): List<String> {
        val uid = getUid() ?: return emptyList()
        val doc = db.collection("users").document(uid).get().await()
        return doc.get("eventsAttended") as? List<String> ?: emptyList()
    }

    // Update total upvotes for a specific user (does not depend on current user)
    suspend fun updateTotalUpvotes(userId: String, newTotal: Int) {
        db.collection("users")
            .document(userId)
            .update("totalUpvotesReceived", newTotal)
            .await()
    }
}
