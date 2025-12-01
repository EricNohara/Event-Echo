package com.example.eventecho.ui.navigation

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eventecho.data.api.ticketmaster.TicketmasterClient
import com.example.eventecho.data.firebase.EventRepository
import com.example.eventecho.data.firebase.UserRepository
import com.example.eventecho.ui.screens.*
import com.example.eventecho.ui.viewmodels.*
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.ViewModelProvider


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as Application

    // Create 1 shared EventRepository
    val repo = remember {
        EventRepository(
            api = TicketmasterClient.apiService,
            firestore = FirebaseFirestore.getInstance()
        )
    }

    // Shared Map VM
    val mapViewModel = remember { EventMapViewModel(repo) }

    NavHost(
        navController = navController,
        startDestination = Routes.SignIn.route
    ) {
        composable(Routes.EventMapHome.route) {
            EventMapHomeScreen(navController, mapViewModel)
        }

        composable(Routes.SignIn.route) { SignInScreen(navController) }
        composable(Routes.SignUp.route) { SignUpScreen(navController) }

        composable(Routes.CreateEvent.route) {
            CreateEventScreen(
                navController = navController,
                repo = repo
            )
        }

        composable(Routes.EditProfile.route) {
            EditProfileScreen(navController)
        }

        composable(Routes.SavedEvents.route) {
            SavedEventsScreen(navController)
        }

        composable(Routes.MapFullScreen.route) {
            MapFullScreen(navController, mapViewModel)
        }

        composable(Routes.Profile.route) {
            val userRepo = UserRepository()

            val vm: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(
                    userRepo = userRepo,
                    eventRepo = repo   // <--- USE SHARED REPO HERE
                )
            )

            ProfileScreen(navController, vm)
        }

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