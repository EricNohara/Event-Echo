package com.example.eventecho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventecho.ui.viewmodels.MemoryViewModel
import com.example.eventecho.ui.viewmodels.MemoryViewModelFactory
import com.example.eventecho.data.firebase.MemoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryViewScreen(
    navController: NavController,
    eventId: String,
    memoryOwnerId: String
) {
    val scrollState = rememberScrollState()

    // Repo + correct VM
    val repo = remember {
        MemoryRepository(
            db = FirebaseFirestore.getInstance(),
            storage = FirebaseStorage.getInstance()
        )
    }

    val viewModel: MemoryViewModel = viewModel(
        factory = MemoryViewModelFactory(repo)
    )

    val memoryWithUser by viewModel.memory.collectAsState(null)

    LaunchedEffect(eventId, memoryOwnerId) {
        viewModel.loadSingleMemory(eventId, memoryOwnerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (memoryWithUser == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Memory not found.")
            }
            return@Scaffold
        }

        val memory = memoryWithUser!!.memory
        val username = memoryWithUser!!.username
        val profilePic = memoryWithUser!!.profilePicUrl

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val hasUpvoted = memory.upvotedBy.contains(userId)

        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {

            AsyncImage(
                model = memory.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            )

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = profilePic,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Text(username, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(20.dp))

            Text(memory.description, style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ThumbUp, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("${memory.upvoteCount} upvotes")
            }

            Spacer(Modifier.height(24.dp))

            IconButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = {
                    viewModel.toggleUpvote(eventId, memoryOwnerId)
                }
            ) {
                Icon(
                    if (hasUpvoted) Icons.Default.ThumbDown else Icons.Default.ThumbUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}

