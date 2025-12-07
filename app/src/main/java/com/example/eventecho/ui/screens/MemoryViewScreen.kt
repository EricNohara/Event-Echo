package com.example.eventecho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.eventecho.data.firebase.MemoryRepository
import com.example.eventecho.ui.viewmodels.MemoryViewModel
import com.example.eventecho.ui.viewmodels.MemoryViewModelFactory
import com.example.eventecho.utils.timeAgo
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

    val repo = remember {
        MemoryRepository(
            db = FirebaseFirestore.getInstance(),
            storage = FirebaseStorage.getInstance()
        )
    }

    val viewModel: MemoryViewModel = viewModel(factory = MemoryViewModelFactory(repo))

    val memoryWithUser by viewModel.memory.collectAsState(null)

    LaunchedEffect(eventId, memoryOwnerId) {
        viewModel.loadSingleMemory(eventId, memoryOwnerId)
    }

    Scaffold { padding ->

        val entry = memoryWithUser
        if (entry == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val memory = entry.memory
        val username = entry.username
        val userProfilePic = entry.profilePicUrl
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val hasUpvoted = memory.upvotedBy.contains(userId)

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
        ) {
            // IMAGE
            AsyncImage(
                model = memory.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                contentScale = ContentScale.Crop
            )

            // POSTER INFO RIBBON UNDER IMAGE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Profile Picture or Fallback
                if (userProfilePic != null) {
                    AsyncImage(
                        model = userProfilePic,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                // USERNAME + TIME AGO
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeAgo(memory.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // UPVOTE BUTTON ON RIGHT
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { viewModel.toggleUpvote(eventId, memoryOwnerId) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (hasUpvoted) Icons.Filled.ThumbUp else Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = if (hasUpvoted)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = memory.upvoteCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // DESCRIPTION
            Text(
                text = memory.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
