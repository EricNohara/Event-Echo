package com.example.eventecho.utils

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
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

fun timeAgoOrAhead(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "Unknown"

    return try {
        val eventDate = LocalDate.parse(dateString)
        val today = LocalDate.now(ZoneId.systemDefault())

        val daysDiff = ChronoUnit.DAYS.between(eventDate, today) // positive if past, negative if future

        // --- FUTURE ---
        if (daysDiff < 0) {
            val days = -daysDiff

            return when {
                days == 0L -> "Today"
                days == 1L -> "Tomorrow"
                days < 7 -> "in $days days"
                days < 30 -> "in ${days / 7} weeks"
                days < 365 -> "in ${days / 30} months"
                else -> "in ${days / 365} years"
            }
        }

        // --- PAST ---
        if (daysDiff == 0L) return "Today"
        if (daysDiff == 1L) return "Yesterday"

        return when {
            daysDiff < 7 -> "$daysDiff days ago"
            daysDiff < 30 -> "${daysDiff / 7} weeks ago"
            daysDiff < 365 -> "${daysDiff / 30} months ago"
            else -> "${daysDiff / 365} years ago"
        }

    } catch (e: Exception) {
        "Unknown"
    }
}

fun formatPrettyDate(dateString: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        val outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)

        val date = LocalDate.parse(dateString, inputFormatter)
        date.format(outputFormatter)
    } catch (e: Exception) {
        dateString // fallback to raw string if invalid
    }
}
