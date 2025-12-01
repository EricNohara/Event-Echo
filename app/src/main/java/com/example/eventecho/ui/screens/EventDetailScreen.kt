package com.example.eventecho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventecho.data.firebase.EventRepository
import com.example.eventecho.ui.dataclass.Event
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.font.FontWeight

@Composable
fun EventDetailScreen(
    navController: NavController,
    repo: EventRepository,
    eventId: String
) {
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // load from Firestore once for this eventId
    LaunchedEffect(eventId) {
        event = repo.getEventById(eventId)
        isLoading = false
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        event == null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Event not found")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Back")
                }
            }
        }

        else -> {
            val e = event!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // title
                Text(
                    text = e.title,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                // date + location
                Text("Date: ${e.date}")
                if (e.location.isNotBlank()) {
                    Text("Location: ${e.location}")
                }

                Spacer(Modifier.height(12.dp))

                // image
                if (e.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = e.imageUrl,
                        contentDescription = "Event image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )

                    Spacer(Modifier.height(16.dp))
                }

                // description
                if (e.description.isNotBlank()) {
                    Text(e.description)
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(16.dp))

                // buttons
                Row {
                    Button(onClick = {
                        navController.navigate("memory_wall/${e.id}")
                    }) {
                        Text("View Memory Wall")
                    }

                    Spacer(Modifier.width(12.dp))

                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            }
        }
    }
}
