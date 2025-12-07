package com.example.eventecho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventecho.data.firebase.EventRepository
import com.example.eventecho.ui.components.EventGrid
import com.example.eventecho.ui.dataclass.Event
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatedEventsScreen(
    navController: NavController,
    repo: EventRepository
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var createdEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch events created by this user
    LaunchedEffect(uid) {
        if (uid != null) {
            createdEvents = repo.getEventsCreatedByUser(uid)
        }
        isLoading = false
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                createdEvents.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("You have no saved events.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                else -> {
                    EventGrid(
                        navController = navController,
                        events = createdEvents
                    )
                }
            }
        }
    }
}
