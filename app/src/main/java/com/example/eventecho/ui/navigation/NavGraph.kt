package com.example.eventecho.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eventecho.ui.screens.AddToMemoryWallScreen
import com.example.eventecho.ui.screens.CreateEventScreen
import com.example.eventecho.ui.screens.EditProfileScreen
import com.example.eventecho.ui.screens.EventDetailScreen
import com.example.eventecho.ui.screens.EventMapHomeScreen
import com.example.eventecho.ui.screens.MapFullScreen
import com.example.eventecho.ui.screens.MemoryWallScreen
import com.example.eventecho.ui.screens.SavedEventsScreen
import com.example.eventecho.ui.screens.SignInScreen
import com.example.eventecho.ui.screens.SignUpScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController();

    NavHost(
        navController = navController,
        // start at the home screen
        startDestination = Routes.EventMapHome.route
    ) {
        // static routes
        composable (Routes.EventMapHome.route) { EventMapHomeScreen(navController) }
        composable(Routes.SignIn.route) { SignInScreen(navController) }
        composable(Routes.SignUp.route) { SignUpScreen(navController) }
        composable(Routes.CreateEvent.route) { CreateEventScreen(navController) }
        composable(Routes.EditProfile.route) { EditProfileScreen(navController) }
        composable(Routes.SavedEvents.route) { SavedEventsScreen(navController) }
        composable(Routes.MapFullScreen.route) { MapFullScreen(navController) }

        // dynamic routes
        composable(
            route = Routes.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(navController, eventId)
        }

        composable(
            route = Routes.MemoryWall.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            MemoryWallScreen(navController, eventId)
        }

        composable(
            route = Routes.AddToMemoryWall.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
                backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            AddToMemoryWallScreen(navController, eventId)
        }
    }
}