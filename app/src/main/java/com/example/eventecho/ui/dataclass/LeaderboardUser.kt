package com.example.eventecho.ui.dataclass

data class LeaderboardUser(
    val uid: String,
    val username: String,
    val profilePicUrl: String?,
    val totalUpvotes: Int,
    val eventsCreated: Int,
    val eventsAttended: Int
)