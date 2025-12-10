package com.example.eventecho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.example.eventecho.ui.components.CreatorInfoSection
import com.example.eventecho.utils.formatPrettyDate
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

    var creatorUsername by remember { mutableStateOf<String?>(null) }
    var creatorProfileUrl by remember { mutableStateOf<String?>(null) }

    // Lookup creator details IF event is user-created
    LaunchedEffect(event) {
        val ev = event
        if (ev?.source == "user" && ev.createdBy != null) {
            val (username, profileUrl) = repo.getUserProfile(ev.createdBy)
            creatorUsername = username
            creatorProfileUrl = profileUrl
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (event == null) {
        EventNotFound(navController)
        return
    }

    val e = event!!
    val formattedDate = formatPrettyDate(e.date)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            EventBottomActionBar(
                onMemoryWall = {
                    navController.navigate("memory_wall/${e.id}")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            // --- HERO IMAGE WITH TITLE OVERLAY ---
            Box {
                AsyncImage(
                    model = e.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentScale = ContentScale.Crop
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 100f
                            )
                        )
                )

                // Title on Image
                Text(
                    text = e.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )

                // Floating Favorite Button
                IconButton(
                    onClick = {
                        if (uid != null) {
                            isFavorited = !isFavorited
                            scope.launch {
                                if (isFavorited) repo.addFavoriteEvent(uid, e.id)
                                else repo.removeFavoriteEvent(uid, e.id)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorited) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- MAIN CONTENT CARD ---
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                tonalElevation = 3.dp,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    CreatorInfoSection(
                        source = e.source,
                        creatorUsername = creatorUsername,
                        creatorProfileUrl = creatorProfileUrl
                    )

                    // Location Chip
                    if (e.location.isNotBlank()) {
                        AssistChip(
                            onClick = {},
                            label = { Text(e.location) },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            },
                        )
                    }

                    // Date Chip
                    AssistChip(
                        onClick = {},
                        label = { Text(formattedDate) },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 1.dp,
                        color = Color.Gray
                    )

                    // Description
                    Text(
                        text = if (e.description.isNotBlank()) e.description else "No description available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(80.dp))
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
