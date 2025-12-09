package com.example.eventecho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.calculateStandardPaneScaffoldDirective
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
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

    //Determine if Tablet Mode
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val scaffoldDirective = calculateStandardPaneScaffoldDirective(adaptiveInfo)
    val isTablet = scaffoldDirective.maxHorizontalPartitions > 1

    Scaffold { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                if (isTablet) {
                    // TABLET LAYOUT
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left : Info
                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            ProfileInfoContent(
                                user = uiState.user,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { scope.launch { context.setDarkMode(it) } },
                                onEditClick = { navController.navigate(Routes.EditProfile.route) },
                                onStatClick = { route -> navController.navigate(route) }
                            )
                        }

                        // Right : Events
                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            RecentEventsContent(
                                events = uiState.recentEvents,
                                navController = navController
                            )
                        }
                    }
                } else {
                    //  PHONE LAYOUT
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfileInfoContent(
                            user = uiState.user,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { scope.launch { context.setDarkMode(it) } },
                            onEditClick = { navController.navigate(Routes.EditProfile.route) },
                            onStatClick = { route -> navController.navigate(route) }
                        )

                        RecentEventsContent(
                            events = uiState.recentEvents,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoContent(
    user: com.example.eventecho.ui.dataclass.ProfileUser,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onStatClick: (String) -> Unit
) {
    Column {
        ProfileHeader(
            username = user.username,
            bio = user.bio,
            profilePicUrl = user.profilePicUrl,
            memberSince = user.memberSince
        )
        Spacer(modifier = Modifier.height(28.dp))

        SettingsSection(
            isDark = isDarkMode,
            onToggle = onToggleDarkMode,
            onEditClick = onEditClick
        )
        Spacer(modifier = Modifier.height(28.dp))

        StatsRow(
            attended = user.eventsAttended,
            created = user.eventsCreated,
            favorites = user.favorites,
            totalUpvotes = user.totalUpvotesReceived,
            onAttendedClick = { onStatClick("attended_events") },
            onFavoritesClick = { onStatClick("saved_events") },
            onCreatedClick = { onStatClick("created_events") }
        )
        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun RecentEventsContent(
    events: List<com.example.eventecho.ui.dataclass.Event>,
    navController: NavController
) {
    Column {
        SectionTitle("Recent Events")
        Spacer(modifier = Modifier.height(12.dp))

        if (events.isEmpty()) {
            Text(
                text = "No recent events",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            EventGridNonScrollable(
                navController = navController,
                events = events
            )
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
                    .size(100.dp)
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

        Spacer(Modifier.height(16.dp))

        // -------- BIO CARD --------
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Bio",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
            )
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
