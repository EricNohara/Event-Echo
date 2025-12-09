package com.example.eventecho.ui.screens

import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun EventDetailScreen(
    navController: NavController,
    repo: EventRepository,
    eventId: String
) {
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val scope = rememberCoroutineScope()

    // SNACKBAR
    val snackbarHostState = remember { SnackbarHostState() }

    // Load event
    LaunchedEffect(eventId) {
        event = repo.getEventById(eventId)
        isLoading = false
        if (uid != null) repo.addRecentEvent(uid, eventId)
    }

    // Favorite state
    var isFavorited by remember { mutableStateOf(false) }
    LaunchedEffect(uid) {
        if (uid != null) {
            val saved = repo.getUserSavedEvents(uid)
            isFavorited = saved.contains(eventId)
        }
    }

    // Parse event date safely
    val eventDate = remember(event) {
        try {
            event?.date?.let { LocalDate.parse(it) }
        } catch (e: Exception) {
            null
        }
    }

    // Did this event occur already?
    val hasEventOccurred = remember(eventDate) {
        eventDate?.isBefore(LocalDate.now()) == true
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        event == null -> {
            EventNotFound(navController)
        }

        else -> {
            val e = event!!

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    EventBottomActionBar(
                        onMemoryWall = {
                            if (hasEventOccurred) {
                                navController.navigate("memory_wall/${e.id}")
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "This event hasn’t happened yet — you can add memories after it begins."
                                    )
                                }
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    // Title
                    Text(
                        text = e.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    // Date + Location + Favorite Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date: ${e.date}", style = MaterialTheme.typography.bodyMedium)
                            if (e.location.isNotBlank()) {
                                Text("Location: ${e.location}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        IconButton(
                            onClick = {
                                if (uid != null) {
                                    isFavorited = !isFavorited
                                    scope.launch {
                                        if (isFavorited) repo.addFavoriteEvent(uid, e.id)
                                        else repo.removeFavoriteEvent(uid, e.id)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isFavorited)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Image
                    if (e.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = e.imageUrl,
                            contentDescription = "Event image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // Description
                    if (e.description.isNotBlank()) {
                        Text(
                            text = e.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// BOTTOM BAR
@Composable
fun EventBottomActionBar(
    onMemoryWall: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onMemoryWall,
                modifier = Modifier.weight(1f)
            ) {
                Text("Memory Wall")
            }
        }
    }
}

@Composable
fun EventNotFound(navController: NavController) {
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
