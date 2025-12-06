package com.example.eventecho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.eventecho.ui.components.MemoryCard
import com.example.eventecho.ui.viewmodels.MemoryWallViewModel
import com.example.eventecho.ui.viewmodels.MemoryWallViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.eventecho.data.firebase.MemoryRepository
import com.example.eventecho.ui.navigation.Routes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryWallScreen(
    navController: NavController,
    eventId: String
) {
    val memoryRepo = remember {
        MemoryRepository(
            db = FirebaseFirestore.getInstance(),
            storage = FirebaseStorage.getInstance()
        )
    }

    val viewModel: MemoryWallViewModel = viewModel(
        factory = MemoryWallViewModelFactory(memoryRepo)
    )

    val memories by viewModel.memories.collectAsState()
    val hasUploaded by viewModel.hasUploaded.collectAsState()

    // Load memories on first composition
    LaunchedEffect(eventId) {
        viewModel.loadMemories(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Wall") },
                navigationIcon = {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // add memory button (ONLY IF USER HASN'T POSTED ONE)
            if (!hasUploaded) {
                Button(
                    onClick = {
                        navController.navigate(Routes.AddToMemoryWall.createRoute(eventId))
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Add Your Memory")
                }
            } else {
                Text(
                    text = "You have already added a memory for this event.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // MEMORY LIST
            LazyColumn {
                items(
                    items = memories,
                    key = { it.userId }
                ) { memory ->
                    MemoryCard(memory)
                }
            }
        }
    }
}
