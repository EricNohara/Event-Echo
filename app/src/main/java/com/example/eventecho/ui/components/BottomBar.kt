package com.example.eventecho.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
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

@Composable
fun BottomBar(navController: NavController) {
    NavigationBar(containerColor = Color.White) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Events") },
            label = { Text("Events") },
            selected = currentRoute == Routes.EventMapHome.route,
            onClick = { navController.navigate(Routes.EventMapHome.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
            label = { Text("Map") },
            selected = currentRoute == Routes.MapFullScreen.route,
            onClick = { navController.navigate(Routes.MapFullScreen.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = "Create") },
            label = { Text("Create") },
            selected = currentRoute == Routes.CreateEvent.route,
            onClick = { navController.navigate(Routes.CreateEvent.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == Routes.Profile.route,
            onClick = { navController.navigate(Routes.Profile.route) }
        )
    }
}