package com.example.eventecho.ui.navigation

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eventecho.data.firebase.EventRepository
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
import com.google.firebase.firestore.FirebaseFirestore

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as Application

    // Create the repository ONCE
    val repo = remember {
        EventRepository(
            api = TicketmasterClient.apiService,
            firestore = FirebaseFirestore.getInstance()
        )
    }

    // Create the shared map view model using that repo
    val viewModel = remember { EventMapViewModel(repo) }

    NavHost(
        navController = navController,
        startDestination = Routes.SignIn.route
    ) {
        composable(Routes.EventMapHome.route) {
            EventMapHomeScreen(navController, viewModel)
        }
        composable(Routes.SignIn.route) { SignInScreen(navController) }
        composable(Routes.SignUp.route) { SignUpScreen(navController) }

        // FIX HERE — now repo exists
        composable(Routes.CreateEvent.route) {
            CreateEventScreen(
                navController = navController,
                repo = repo
            )
        }

        composable(Routes.EditProfile.route) { EditProfileScreen(navController) }
        composable(Routes.SavedEvents.route) { SavedEventsScreen(navController) }
        composable(Routes.MapFullScreen.route) { MapFullScreen(navController) }
        composable(Routes.Profile.route) { ProfileScreen(navController) }

        composable(
            route = Routes.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(
                navController = navController,
                repo = repo,
                eventId = eventId
            )
        }

        composable(
            route = Routes.MemoryWall.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            MemoryWallScreen(navController, eventId)
        }

        composable(
            route = Routes.AddToMemoryWall.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            AddToMemoryWallScreen(navController, eventId)
        }
    }
}
