package com.example.eventecho.utils

import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

// helper function to format firebase dates well
fun timeAgo(timestamp: Timestamp?): String {
    if (timestamp == null) return "Unknown"

    val now = System.currentTimeMillis()
    val time = timestamp.toDate().time
    val diff = now - time

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        seconds < 5 -> "Just now"
        seconds < 60 -> "$seconds seconds ago"
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        days < 30 -> "${days / 7} weeks ago"
        days < 365 -> "${days / 30} months ago"
        else -> "${days / 365} years ago"
    }
}
