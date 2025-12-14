package com.example.eventecho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.eventecho.data.firebase.UserRepository
import com.example.eventecho.ui.dataclass.LeaderboardUser
import com.example.eventecho.ui.viewmodels.*


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

        Text(
            "Leaderboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "Top users in EventEcho",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

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
                CircularProgressIndicator()
            }
        } else {
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
            label = { Text("Rank by") }
        )

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
                    }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(
            if (rank <= 3) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "#$rank",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp)
            )

            AsyncImage(
                model = user.profilePicUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
            )

            Spacer(Modifier.width(12.dp))

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
        }
    }
}

