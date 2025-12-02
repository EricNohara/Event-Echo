package com.example.eventecho

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.eventecho.data.datastore.readDarkMode
import com.example.eventecho.ui.components.ProfileDrawerContent
import com.example.eventecho.ui.components.TopBar
import com.example.eventecho.ui.components.BottomBar
import com.example.eventecho.ui.navigation.AppNavGraph
import com.example.eventecho.ui.navigation.Routes
import com.example.eventecho.ui.theme.EventEchoTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventEchoApp() {

    val navController = rememberNavController()
    val profileDrawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val title = when (currentRoute) {
        Routes.EventMapHome.route -> "Home"
        Routes.CreateEvent.route -> "Create Event"
        Routes.SavedEvents.route -> "Saved Events"
        Routes.EditProfile.route -> "Edit Profile"
        Routes.SignIn.route -> "Sign In"
        Routes.SignUp.route -> "Sign Up"
        Routes.MemoryWall.route -> "Memory Wall"
        Routes.AddToMemoryWall.route -> "Add to Memory Wall"
        Routes.MapFullScreen.route -> "Map Full Screen"
        Routes.EventDetail.route -> "View Event"
        Routes.Settings.route -> "Settings"
        else -> "Event Echo"
    }

    // Determine Light/Dark mode
    val context = LocalContext.current
    val isDarkMode by context.readDarkMode().collectAsState(initial = false)

    EventEchoTheme (
        darkTheme = isDarkMode
    ) {

        // Profile Drawer
        ModalNavigationDrawer(
            drawerState = profileDrawerState,
            drawerContent = {
                ProfileDrawerContent(
                    navController = navController,
                    drawerState = profileDrawerState,
                    scope = scope
                )
            }
        ) {

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopBar(
                        profileDrawerState = profileDrawerState,
                        scope = scope,
                        title = title,
                        currentRoute = navBackStackEntry.value?.destination?.route,
                        onBackClick = { navController.popBackStack() }
                    )
                },
                bottomBar = {
                    BottomBar(navController)
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AppNavGraph(navController)
                }
            }
        }
    }
}