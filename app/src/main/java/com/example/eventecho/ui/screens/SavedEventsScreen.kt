package com.example.eventecho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventecho.data.firebase.EventRepository
import com.example.eventecho.ui.components.EventGrid
import com.example.eventecho.ui.dataclass.Event
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SavedEventsScreen(
    navController: NavController,
    repo: EventRepository
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    var savedEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // --- SEARCH STATE ---
    val searchQuery = remember { mutableStateOf("") }

    // Fetch saved event IDs → fetch event objects
    LaunchedEffect(uid) {
        if (uid != null) {
            val savedIds = repo.getUserSavedEvents(uid)
            val events = mutableListOf<Event>()

            for (id in savedIds) {
                val event = repo.getEventById(id)
                if (event != null) events.add(event)
            }

            savedEvents = events
        }
        isLoading = false
    }

    // --- FILTERED EVENTS (by title only) ---
    val filteredEvents by remember(savedEvents, searchQuery.value) {
        derivedStateOf {
            val q = searchQuery.value.trim().lowercase()
            if (q.isBlank()) savedEvents
            else savedEvents.filter { event ->
                event.title.lowercase().contains(q)
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // ------------------ SEARCH BAR ------------------
            TextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                placeholder = { Text("Search events") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )

            // ------------------ EVENT LIST ------------------
            Box(modifier = Modifier.fillMaxSize()) {

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    filteredEvents.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No events found.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        EventGrid(
                            navController = navController,
                            events = filteredEvents
                        )
                    }
                }
            }
        }
    }
}
