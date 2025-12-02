package com.example.eventecho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventecho.ui.components.EventGridNonScrollable
import com.example.eventecho.ui.navigation.Routes
import com.example.eventecho.ui.viewmodels.ProfileViewModel
import com.example.eventecho.data.datastore.readDarkMode
import com.example.eventecho.data.datastore.setDarkMode
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // READ DARK MODE FROM DATASTORE
    val isDarkMode by context.readDarkMode().collectAsState(initial = false)

    Scaffold(containerColor = Color(0xFFF8F9FA)) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                item {
                    ProfileHeader(
                        username = uiState.user.username,
                        bio = uiState.user.bio,
                        profilePicUrl = uiState.user.profilePicUrl,
                        onEditClick = { navController.navigate(Routes.EditProfile.route) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Member Since
                item {
                    MemberSinceFooter(date = uiState.user.memberSince)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SETTINGS SECTION (Dark Mode toggle)
                item {
                    SettingsSection(
                        isDark = isDarkMode,
                        onToggle = { enabled ->
                            scope.launch { context.setDarkMode(enabled) }
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Stats
                item {
                    StatsRow(
                        attended = uiState.user.eventsAttended,
                        created = uiState.user.eventsCreated,
                        favorites = uiState.user.favorites
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Recent Events Header
                // Recent Events Header
                item {
                    SectionTitle("Recent Events")
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Recent Events Grid
                item {
                    EventGridNonScrollable(
                        navController = navController,
                        events = uiState.recentEvents
                    )
                }

                if (uiState.recentEvents.isEmpty()) {
                    item {
                        Text(
                            text = "No recent events",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    isDark: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(
            "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    "Dark Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Enable system-wide dark theme",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = isDark,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun ProfileHeader(
    username: String,
    bio: String,
    profilePicUrl: String?,
    onEditClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (profilePicUrl != null) {
                AsyncImage(
                    model = profilePicUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = Color(0xFFD81B60),
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = username, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = bio, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = onEditClick, shape = RoundedCornerShape(8.dp)) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile")
        }
    }
}

@Composable
fun StatsRow(attended: Int, created: Int, favorites: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCard(icon = Icons.Default.CalendarToday, count = attended.toString(), label = "Events Attended")
        StatCard(icon = Icons.Default.Group, count = created.toString(), label = "Events Created")
        StatCard(icon = Icons.Default.FavoriteBorder, count = favorites.toString(), label = "Favorites")
    }
}

@Composable
fun StatCard(icon: ImageVector, count: String, label: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(105.dp)
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = count, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun MemberSinceFooter(date: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Member since $date", color = Color.Gray)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
}
