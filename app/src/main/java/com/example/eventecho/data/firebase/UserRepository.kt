package com.example.eventecho.data.firebase

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

class UserRepository {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val uid: String get() = FirebaseAuth.getInstance().currentUser!!.uid

    /** CREATE USER DOCUMENT AFTER SIGNUP */
    fun createUser(uid: String, email: String, username: String) {
        val userData = mapOf(
            "email" to email,
            "username" to username,
            "bio" to "",
            "profilePicUrl" to null,
            "createdAt" to FieldValue.serverTimestamp(),
            "eventsAttended" to emptyList<String>(),
            "eventsCreated" to emptyList<String>(),
            "savedEvents" to emptyList<String>(),
            "recentEvents" to emptyList<String>()
        )

        db.collection("users").document(uid).set(userData)
    }

    /** FETCH USER PROFILE */
    fun getUser(onSuccess: (Map<String, Any>) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) onSuccess(doc.data ?: emptyMap())
            }
    }

    /** UPDATE TEXT FIELDS */
    fun updateUserFields(username: String, bio: String) {
        db.collection("users").document(uid).update(
            mapOf(
                "username" to username,
                "bio" to bio
            )
        )
    }

    /** UPDATE PROFILE PICTURE URL IN FIRESTORE */
    fun updateProfilePic(url: String) {
        db.collection("users").document(uid)
            .update("profilePicUrl", url)
    }

    /** UPLOAD PROFILE PICTURE TO STORAGE */
    fun uploadProfilePicture(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ref = storage.reference.child("profile_pictures/$uid.jpg")

        ref.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                ref.downloadUrl
            }
            .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
            .addOnFailureListener(onError)
    }
}
