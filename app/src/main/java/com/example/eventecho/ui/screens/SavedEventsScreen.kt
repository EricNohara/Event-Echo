package com.example.eventecho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventecho.data.firebase.EventRepository
import com.example.eventecho.ui.components.EventGrid
import com.example.eventecho.ui.components.EventGridNonScrollable
import com.example.eventecho.ui.dataclass.Event
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedEventsScreen(
    navController: NavController,
    repo: EventRepository
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val scope = rememberCoroutineScope()

    var savedEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch saved event IDs → fetch each event object
    LaunchedEffect(uid) {
        if (uid != null) {
            val savedIds = repo.getUserSavedEvents(uid)
            val events = mutableListOf<Event>()

            // Fetch each full event
            for (id in savedIds) {
                val event = repo.getEventById(id)
                if (event != null) events.add(event)
            }

            savedEvents = events
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Events") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                savedEvents.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("You have no saved events.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                else -> {
                    EventGrid(
                        navController = navController,
                        events = savedEvents
                    )
                }
            }
        }
    }
}
