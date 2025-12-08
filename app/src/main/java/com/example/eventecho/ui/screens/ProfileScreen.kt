package com.example.eventecho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.SolidColor
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

    Scaffold { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
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
                        memberSince = uiState.user.memberSince
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // SETTINGS SECTION (Dark Mode toggle)
                item {
                    SettingsSection(
                        isDark = isDarkMode,
                        onToggle = { enabled ->
                            scope.launch { context.setDarkMode(enabled) }
                        },
                        onEditClick = { navController.navigate(Routes.EditProfile.route) }
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // Stats
                item {
                    StatsRow(
                        attended = uiState.user.eventsAttended,
                        created = uiState.user.eventsCreated,
                        favorites = uiState.user.favorites,
                        totalUpvotes = uiState.user.totalUpvotesReceived,
                        onAttendedClick = {navController.navigate("attended_events")},
                        onFavoritesClick = { navController.navigate("saved_events") },
                        onCreatedClick = { navController.navigate("created_events") }
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // Recent Events Header
                item {
                    SectionTitle("Recent Events")
                    Spacer(modifier = Modifier.height(12.dp))
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
                            modifier = Modifier.padding(16.dp)
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
    onToggle: (Boolean) -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            // DARK MODE TOGGLE
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
                        "Enable app-wide dark theme",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = isDark,
                    onCheckedChange = onToggle
                )
            }

            Spacer(Modifier.height(20.dp))

            // Edit button
            OutlinedButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(MaterialTheme.colorScheme.onBackground)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Edit Profile",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun ProfileHeader(
    username: String,
    bio: String,
    profilePicUrl: String?,
    memberSince: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Profile Image
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Name + Member Since (vertical)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = username,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Member since $memberSince",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // -------- BIO CARD --------
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (bio.isNotBlank()) bio else "No bio added.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun StatsRow(
    attended: Int,
    created: Int,
    favorites: Int,
    totalUpvotes: Int,
    onAttendedClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onCreatedClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        StatCard(
            icon = Icons.Default.CalendarToday,
            count = attended.toString(),
            label = "Attended",
            onClick = onAttendedClick
        )

        StatCard(
            icon = Icons.Default.CreateNewFolder,
            count = created.toString(),
            label = "Created",
            onClick = onCreatedClick
        )

        StatCard(
            icon = Icons.Default.Favorite,
            count = favorites.toString(),
            label = "Favorites",
            onClick = onFavoritesClick
        )

        StatCard(
            icon = Icons.Default.ThumbUp,
            count = totalUpvotes.toString(),
            label = "Upvotes"
        )
    }
}

@Composable
fun RowScope.StatCard(
    icon: ImageVector,
    count: String,
    label: String,
    onClick: (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = Modifier
            .weight(1f)
            .height(110.dp)
            .padding(horizontal = 4.dp)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Icon circle with subtle tint
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Stat number (onSurface)
            Text(
                text = count,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            // Label (onSurfaceVariant)
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MemberSinceFooter(date: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
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
