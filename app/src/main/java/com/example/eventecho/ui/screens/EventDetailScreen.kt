package com.example.eventecho.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventecho.ui.viewmodels.EventMapViewModel

@Composable
fun EventDetailScreen(
    navController: NavController,
    viewModel: EventMapViewModel,
    eventId: String,
) {
    // get the event from view model by its inputted id
    val events by viewModel.events.collectAsState()
    val event = events.find { it.id == eventId }

    if (event == null) {
        Text("Event not found")
        return
    }

    // display relevant information about the event
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = event.name, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Text("Date: ${event.dates.start.localDate}")

        Spacer(modifier = Modifier.height(8.dp))

        val venue = event._embedded?.venues?.firstOrNull()
        if (venue != null) {
            Text("Venue: ${venue.name ?: "Unknown"}")
            venue.location?.let {
                Text("Location: ${it.latitude}, ${it.longitude}")
            }
        }

        val genre = event.classifications?.firstOrNull()?.genre?.name
        if (genre != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Genre: $genre")
        }

       // back button
        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Back")
        }
    }
}