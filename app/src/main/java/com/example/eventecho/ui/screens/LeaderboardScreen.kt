package com.example.eventecho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.eventecho.data.firebase.UserRepository
import com.example.eventecho.ui.dataclass.LeaderboardUser
import com.example.eventecho.ui.viewmodels.*
import com.example.eventecho.R

@Composable
fun LeaderboardScreen() {
    val viewModel: LeaderboardViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return LeaderboardViewModel(UserRepository()) as T
            }
        }
    )

    val ui by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display Filter Dropdown
        LeaderboardFilterDropdown(
            selected = ui.filter,
            onSelected = viewModel::setFilter
        )

        Spacer(Modifier.height(16.dp))

        if (ui.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        } else {
            // Display Leaderboard
            LazyColumn {
                items(ui.users.size) { index ->
                    LeaderboardRow(
                        rank = index + 1,
                        user = ui.users[index],
                        filter = ui.filter
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardFilterDropdown(
    selected: LeaderboardFilter,
    onSelected: (LeaderboardFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = when (selected) {
                LeaderboardFilter.UPVOTES -> "Total Upvotes"
                LeaderboardFilter.CREATED -> "Events Created"
                LeaderboardFilter.ATTENDED -> "Events Attended"
            },
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            label = {
                Text("Rank by")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Leaderboard filter"
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        )

        // Exposed dropdown to select which filter to use for leaderboard
        ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            LeaderboardFilter.values().forEach {
                DropdownMenuItem(
                    text = {
                        Text(
                            when (it) {
                                LeaderboardFilter.UPVOTES -> "Total Upvotes"
                                LeaderboardFilter.CREATED -> "Events Created"
                                LeaderboardFilter.ATTENDED -> "Events Attended"
                            }
                        )
                    },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun LeaderboardRow(
    rank: Int,
    user: LeaderboardUser,
    filter: LeaderboardFilter
) {
    val borderModifier =
        if (rank == 1) {
            // Unique card for rank 1
            Modifier.border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Modifier
        }

    // Card to display leaderboard info for user and filter
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .then(borderModifier),
        elevation = CardDefaults.cardElevation(
            if (rank <= 3) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "#$rank",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp)
            )

            Spacer(Modifier.width(8.dp))

            AsyncImage(
                model = user.profilePicUrl ?: R.drawable.default_avatar,
                contentDescription = "${user.username} avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(user.username, fontWeight = FontWeight.SemiBold)
                Text(
                    when (filter) {
                        LeaderboardFilter.UPVOTES ->
                            "${user.totalUpvotes} upvotes"
                        LeaderboardFilter.CREATED ->
                            "${user.eventsCreated} events created"
                        LeaderboardFilter.ATTENDED ->
                            "${user.eventsAttended} events attended"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (rank == 1) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Top ranked",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}