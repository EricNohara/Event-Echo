package com.example.eventecho.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.eventecho.ui.components.MemoryTile
import com.example.eventecho.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.TextFieldDefaults


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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

    // Load memories
    LaunchedEffect(eventId) {
        viewModel.loadMemories(eventId)
    }

    // FILTER STATE
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("Upvotes") }

    // FILTERING
    val filtered = memories.filter { mem ->
        val q = searchQuery.lowercase()
        mem.username.lowercase().contains(q) ||
                mem.memory.description.lowercase().contains(q)
    }

    // SORTING
    val sorted = when (sortOption) {
        "Newest" -> filtered.sortedByDescending { it.memory.createdAt }
        "Oldest" -> filtered.sortedBy { it.memory.createdAt }
        "Upvotes" -> filtered.sortedByDescending { it.memory.upvoteCount }
        else -> filtered
    }

    Scaffold(
        floatingActionButton = {
            if (!hasUploaded) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Routes.AddToMemoryWall.createRoute(eventId))
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Memory"
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // FILTER ROW
            FilterRow(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                sortOption = sortOption,
                onSortOptionChange = { sortOption = it }
            )

            Spacer(Modifier.height(16.dp))

            // GRID
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(
                    count = sorted.size,
                    key = { idx -> sorted[idx].memory.userId }
                ) { index ->

                    val mem = sorted[index]

                    MemoryTile(
                        memory = mem.memory,
                        username = mem.username,
                        onClick = {
                            navController.navigate(
                                Routes.MemoryView.createRoute(eventId, mem.memory.userId)
                            )
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterRow(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortOption: String,
    onSortOptionChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Search") },
            modifier = Modifier.weight(0.6f),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,

                focusedIndicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,

                focusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            )
        )

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(0.4f)
        ) {
            OutlinedTextField(
                readOnly = true,
                value = sortOption,
                onValueChange = {},
                label = { Text("Sort") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,

                    focusedIndicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,

                    focusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("Newest", "Oldest", "Upvotes").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSortOptionChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


