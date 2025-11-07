package com.example.eventecho.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventecho.ui.navigation.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    ModalDrawerSheet(
        modifier = Modifier.width(250.dp) // partial width
    ) {
        Column(
            modifier = Modifier
                .padding(start = 0.dp, top = 24.dp, end = 0.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = "Event Map",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        navController.navigate(Routes.EventMapHome.route) {
                            popUpTo(Routes.EventMapHome.route) { inclusive = true }
                        }
                        scope.launch { drawerState.close() }
                    }
                    .padding(12.dp)
            )

            Text(
                text = "Saved Events",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Routes.SavedEvents.route)
                        scope.launch { drawerState.close() }
                    }
                    .padding(12.dp)
            )

            Text(
                text = "Map Full Screen",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        navController.navigate(Routes.MapFullScreen.route)
                        scope.launch { drawerState.close() }
                    }
                    .padding(12.dp)
            )
        }
    }
}

@Composable
fun ProfileDrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    // Use ModalDrawerSheet to limit width
    ModalDrawerSheet(
        modifier = Modifier.width(250.dp) // partial width
    ) {
        Column(
            modifier = Modifier
                .padding(start = 0.dp, top = 24.dp, end = 0.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        // Navigate to EditProfile screen
                        navController.navigate(Routes.EditProfile.route)
                        scope.launch { drawerState.close() }
                    }
                    .padding(12.dp)
            )

            Text(
                text = "Sign Out",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // No-op for now
                        scope.launch { drawerState.close() }
                    }
                    .padding(12.dp)
            )
        }
    }
}