package com.example.eventecho.ui.viewmodels

data class ProfileUser(
    val username: String = "",
    val bio: String = "",
    val profilePicUrl: String? = null,
    val eventsAttended: Int = 0,
    val eventsCreated: Int = 0,
    val favorites: Int = 0,
    val memberSince: String = ""
)