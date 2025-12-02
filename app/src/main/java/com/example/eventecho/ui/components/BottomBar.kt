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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun BottomBar(navController: NavController) {

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val selectedColor = MaterialTheme.colorScheme.onPrimaryContainer
        val unselectedColor = MaterialTheme.colorScheme.onPrimary

        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor,
            indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
        )

        // EVENTS
        NavigationBarItem(
            modifier = Modifier.testTag("bottomNavHome"),
            selected = currentRoute == Routes.EventMapHome.route,
            onClick = { navController.navigate(Routes.EventMapHome.route) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == Routes.EventMapHome.route)
                        Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Events"
                )
            },
            label = { Text("Events") },
            colors = itemColors
        )

        // MAP
        NavigationBarItem(
            modifier = Modifier.testTag("bottomNavMap"),
            selected = currentRoute == Routes.MapFullScreen.route,
            onClick = { navController.navigate(Routes.MapFullScreen.route) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == Routes.MapFullScreen.route)
                        Icons.Filled.Map else Icons.Outlined.Map,
                    contentDescription = "Map"
                )
            },
            label = { Text("Map") },
            colors = itemColors
        )

        // CREATE
        NavigationBarItem(
            modifier = Modifier.testTag("bottomNavCreate"),
            selected = currentRoute == Routes.CreateEvent.route,
            onClick = { navController.navigate(Routes.CreateEvent.route) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == Routes.CreateEvent.route)
                        Icons.Filled.AddCircle else Icons.Outlined.AddCircleOutline,
                    contentDescription = "Create"
                )
            },
            label = { Text("Create") },
            colors = itemColors
        )

        // PROFILE
        NavigationBarItem(
            modifier = Modifier.testTag("bottomNavProfile"),
            selected = currentRoute == Routes.Profile.route,
            onClick = { navController.navigate(Routes.Profile.route) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == Routes.Profile.route)
                        Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            colors = itemColors
        )
    }
}

