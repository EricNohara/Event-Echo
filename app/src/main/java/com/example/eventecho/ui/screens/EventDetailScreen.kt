package com.example.eventecho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Details
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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

    val venue = event._embedded?.venues?.firstOrNull()

    // display relevant information about the event

    Column(
        modifier = Modifier.fillMaxHeight().padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Row{
                Icon(Icons.Default.AccountCircle, contentDescription = "Host")
                Spacer(modifier = Modifier.width(8.dp))
                Column{
                    Text(text = event.name, fontWeight = FontWeight.Bold)
                    if (event.promoter != null) {
                        Text(event.promoter.name.toString())
                    } else {
                        Text("${venue?.name}")
                    }

                }
            }
            var isSaved by rememberSaveable { mutableStateOf(false) }
            IconButton(onClick = {
                isSaved = !isSaved
                // TODO: Save event to DataStore
            }) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )

            }
        }

        // Image of event (TODO: Multiple Swipeable Images)
        AsyncImage(
            model = event.images?.get(0)?.url,
            contentDescription = "Image of event",
            modifier=Modifier.height(300.dp)
        )

//        val genre = event.classifications?.firstOrNull()?.genre?.name
//        if (genre != null) {
//            Spacer(modifier = Modifier.height(8.dp))
//            Text("Genre: $genre")
//        }

        // Location
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = "Location")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Location: ${venue?.address?.line1}, ${venue?.city?.name}, ${venue?.state?.stateCode} (${venue?.name})")
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Date
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, contentDescription = "Date")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Date: ${event.dates.start.localDate}")
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Description, if provided
        if (event.description != null) {
            Text(event.description)
        } else {
            Text("(No description found.)")
        }

        // Info, if provided
        if (event.info != null) {
            Text(event.info)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(12.dp))

        // Buttons
        Row {
            // View Memory Wall
            Button(onClick = {
                navController.navigate("memory_wall/${event.id}")
            }) {
                Text("View Memory Wall")
            }
            Spacer(modifier = Modifier.width(8.dp))

            // Sign up for event
            Button(onClick = {
                // TODO: Event Signup
            }) {
                Text("Join Event")
            }
            Spacer(modifier = Modifier.width(8.dp))

            // Back button
            Button(onClick = {
                navController.popBackStack()
            }) {
                Text("Back")
            }
        }
    }
}