package com.example.eventecho.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eventecho.data.api.ticketmaster.EventRepository
import com.example.eventecho.data.api.ticketmaster.TicketmasterClient
import com.example.eventecho.ui.screens.AddToMemoryWallScreen
import com.example.eventecho.ui.screens.CreateEventScreen
import com.example.eventecho.ui.screens.EditProfileScreen
import com.example.eventecho.ui.screens.EventDetailScreen
import com.example.eventecho.ui.screens.EventMapHomeScreen
import com.example.eventecho.ui.screens.MapFullScreen
import com.example.eventecho.ui.screens.MemoryWallScreen
import com.example.eventecho.ui.screens.ProfileScreen
import com.example.eventecho.ui.screens.SavedEventsScreen
import com.example.eventecho.ui.screens.SignInScreen
import com.example.eventecho.ui.screens.SignUpScreen
import com.example.eventecho.ui.viewmodels.EventMapViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController) {
    // shared event map view model
    val viewModel = remember { EventMapViewModel(EventRepository(TicketmasterClient.apiService)) }

    NavHost(
        navController = navController,
        // start at the home screen
        startDestination = Routes.SignIn.route
    ) {
        // static routes
        composable (Routes.EventMapHome.route) { EventMapHomeScreen(navController, viewModel) }
        composable(Routes.SignIn.route) { SignInScreen(navController) }
        composable(Routes.SignUp.route) { SignUpScreen(navController) }
        composable(Routes.CreateEvent.route) { CreateEventScreen(navController) }
        composable(Routes.EditProfile.route) { EditProfileScreen(navController) }
        composable(Routes.SavedEvents.route) { SavedEventsScreen(navController) }
        composable(Routes.MapFullScreen.route) { MapFullScreen(navController) }
        composable(Routes.Profile.route) { ProfileScreen(navController)}

        // dynamic routes
        composable(
            route = Routes.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(navController, viewModel, eventId)
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