package com.example.eventecho

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.eventecho.ui.components.DrawerContent
import com.example.eventecho.ui.components.ProfileDrawerContent
import com.example.eventecho.ui.components.TopBar
import com.example.eventecho.ui.navigation.AppNavGraph
import com.example.eventecho.ui.navigation.Routes

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventEchoApp() {
    val navController = rememberNavController()
    val mainDrawerState = rememberDrawerState(DrawerValue.Closed)
    val profileDrawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // current route
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // title based on route
    val title = when (currentRoute) {
        Routes.EventMapHome.route -> "Event Map"
        Routes.CreateEvent.route -> "Create Event"
        Routes.SavedEvents.route -> "Saved Events"
        Routes.EditProfile.route -> "Edit Profile"
        Routes.SignIn.route -> "Sign In"
        Routes.SignUp.route -> "Sign Up"
        Routes.MemoryWall.route -> "Memory Wall"
        Routes.AddToMemoryWall.route -> "Add to Memory Wall"
        Routes.MapFullScreen.route -> "Map Full Screen"
        else -> "Event Echo"
    }

    // Main navigation drawer
    ModalNavigationDrawer(
        drawerState = mainDrawerState,
        drawerContent = {
            DrawerContent(navController, mainDrawerState, scope)
        }
    ) {
        // Profile drawer
        ModalNavigationDrawer(
            drawerState = profileDrawerState,
            drawerContent = {
                ProfileDrawerContent(navController, profileDrawerState, scope)
            }
        ) {
            Column {
                // TopBar with both drawers
                TopBar(
                    drawerState = mainDrawerState,
                    profileDrawerState = profileDrawerState,
                    scope = scope,
                    title = title
                )

                // Main nav graph content
                AppNavGraph(navController)
            }
        }
    }
}