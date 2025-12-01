package com.example.eventecho.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.eventecho.ui.navigation.Routes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.AccountCircle

@Composable
fun BottomBar(navController: NavController) {
    NavigationBar(containerColor = Color.White) {

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // ---- EVENTS ----
        val eventsSelected = currentRoute == Routes.EventMapHome.route
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (eventsSelected) Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Events"
                )
            },
            label = { Text("Events") },
            selected = eventsSelected,
            onClick = { navController.navigate(Routes.EventMapHome.route) }
        )

        // ---- MAP ----
        val mapSelected = currentRoute == Routes.MapFullScreen.route
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (mapSelected) Icons.Filled.Map else Icons.Outlined.Map,
                    contentDescription = "Map"
                )
            },
            label = { Text("Map") },
            selected = mapSelected,
            onClick = { navController.navigate(Routes.MapFullScreen.route) }
        )

        // ---- CREATE ----
        val createSelected = currentRoute == Routes.CreateEvent.route
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (createSelected)
                        Icons.Filled.AddCircle
                    else
                        Icons.Outlined.AddCircleOutline,   // <-- correct outlined match
                    contentDescription = "Create"
                )
            },
            label = { Text("Create") },
            selected = createSelected,
            onClick = { navController.navigate(Routes.CreateEvent.route) }
        )

        // ---- PROFILE ----
        val profileSelected = currentRoute == Routes.Profile.route
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (profileSelected) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            selected = profileSelected,
            onClick = { navController.navigate(Routes.Profile.route) }
        )
    }
}
