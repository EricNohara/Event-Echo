package com.example.eventecho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventecho.ui.viewmodels.MemoryWallViewModel
import com.example.eventecho.ui.viewmodels.MemoryWallViewModelFactory
import com.example.eventecho.data.firebase.MemoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryViewScreen(
    navController: NavController,
    eventId: String,
    memoryOwnerId: String
) {
    val scrollState = rememberScrollState()

    // Repo + VM
    val repo = remember {
        MemoryRepository(
            db = FirebaseFirestore.getInstance(),
            storage = FirebaseStorage.getInstance()
        )
    }
    val viewModel: MemoryWallViewModel = viewModel(
        factory = MemoryWallViewModelFactory(repo)
    )

    val memories by viewModel.memories.collectAsState()

    // Load all memories for the event
    LaunchedEffect(eventId) {
        viewModel.loadMemories(eventId)
    }

    val memory = memories.find { it.userId == memoryOwnerId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (memory == null) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Memory not found.")
            }
            return@Scaffold
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val hasUpvoted = memory.upvotedBy.contains(currentUserId)

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .fillMaxSize()
        ) {

            // Full-size image
            AsyncImage(
                model = memory.imageUrl,
                contentDescription = "Memory Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = memory.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Posted by: ${memory.userId}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            // Upvote Count
            Text(
                text = "Upvotes: ${memory.upvoteCount}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(20.dp))

            // UPVOTE / REMOVE UPVOTE button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.toggleUpvote(eventId, memoryOwnerId)
                }
            ) {
                Text(if (hasUpvoted) "Remove Upvote 👎" else "Upvote 👍")
            }
        }
    }
}
