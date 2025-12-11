package com.example.eventecho.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.West
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.example.eventecho.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController,
    currentRoute: String?,
    profilePicUrl: String?,
    onBackClick: () -> Unit)
{
    var expanded by remember { mutableStateOf(false) }

    // Routes where the back button appears
    val isBackButtonRoute = currentRoute == Routes.EventDetail.route ||
            currentRoute == Routes.MemoryWall.route ||
            currentRoute == Routes.AddToMemoryWall.route ||
            currentRoute == Routes.SavedEvents.route ||
            currentRoute == Routes.EditProfile.route ||
            currentRoute == Routes.CreatedEvents.route ||
            currentRoute == Routes.AttendedEvents.route ||
            currentRoute == Routes.MemoryView.route

    val overrideTitle = when (currentRoute) {
        Routes.AddToMemoryWall.route -> "Add Memory"
        Routes.MemoryWall.route -> "Memory Wall"
        Routes.EditProfile.route -> "Edit Profile"
        Routes.CreatedEvents.route -> "Events You Created"
        Routes.AttendedEvents.route -> "Attended Events"
        Routes.SavedEvents.route -> "Saved Events"
        Routes.MemoryView.route -> "Memory Details"
        Routes.EventDetail.route -> "Event Details"
        Routes.CreateEvent.route -> "Create Event"
        else -> "EventEcho"  // default global title
    }

    TopAppBar(
        title = {
            Text(
                overrideTitle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
                if (isBackButtonRoute) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.West,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            },
        actions = {
            IconButton(onClick = { expanded = true }) {
                if (profilePicUrl != null) {
                    AsyncImage(
                        model = profilePicUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Profile") },
                    onClick = {
                        expanded = false
                        navController.navigate(Routes.EditProfile.route)
                    }
                )

                DropdownMenuItem(
                    text = { Text("Sign Out") },
                    onClick = {
                        expanded = false
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Routes.SignIn.route) {
                            popUpTo(0)
                        }
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}