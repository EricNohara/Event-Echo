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
import com.example.eventecho.ui.screens.EditEventScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as Application

    // Shared Event Repository
    val eventRepo = remember {
        EventRepository(
            api = TicketmasterClient.apiService,
            firestore = FirebaseFirestore.getInstance()
        )
    }

    // Shared User Repository
    val userRepo = remember { UserRepository() }

    // Shared ViewModels (scoped to NavGraph lifecycle)
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            userRepo = userRepo,
            eventRepo = eventRepo
        )
    )

    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(userRepo)
    )

    // Shared Map ViewModel
    val mapViewModel = remember { EventMapViewModel(eventRepo, userRepo) }

    NavHost(
        navController = navController,
        startDestination = Routes.SignIn.route
    ) {

        // ---------------------- AUTH ----------------------
        composable(Routes.SignIn.route) {
            SignInScreen(navController, userViewModel, profileViewModel)
        }

        composable(Routes.SignUp.route) {
            SignUpScreen(navController)
        }

        // ---------------------- MAP ----------------------
        composable(Routes.EventMapHome.route) {
            EventMapHomeScreen(navController, mapViewModel)
        }

        composable(Routes.MapFullScreen.route) {
            MapFullScreen(navController, mapViewModel)
        }

        // ---------------------- LEADERBOARD ----------------------
        composable(Routes.Leaderboard.route) {
            LeaderboardScreen()
        }

        // ---------------------- EVENTS ----------------------
        composable(Routes.CreateEvent.route) {
            CreateEventScreen(
                navController = navController,
                repo = eventRepo
            )
        }

        composable(Routes.EditEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            EditEventScreen(
                navController = navController,
                repo = eventRepo,
                eventId = it.arguments!!.getString("eventId")!!
            )
        }

        composable(
            route = Routes.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { entry ->
            val eventId = entry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(navController, eventRepo, eventId, profileViewModel)
        }

        composable(Routes.SavedEvents.route) {
            SavedEventsScreen(navController, eventRepo)
        }

        composable(Routes.CreatedEvents.route) {
            CreatedEventsScreen(navController, eventRepo)
        }

        composable(Routes.AttendedEvents.route) {
            AttendedEventsScreen(navController, eventRepo)
        }

        // ---------------------- PROFILE ----------------------
        composable(Routes.Profile.route) {
            ProfileScreen(
                navController = navController,
                viewModel = profileViewModel,
            )
        }

        composable(Routes.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                profileViewModel = profileViewModel,
                userViewModel = userViewModel
            )
        }

        // ---------------------- MEMORY WALL ----------------------
        composable(
            route = Routes.MemoryWall.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { entry ->
            val eventId = entry.arguments?.getString("eventId") ?: ""
            MemoryWallScreen(navController, eventId)
        }

        composable(
            route = Routes.AddToMemoryWall.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { entry ->
            val eventId = entry.arguments?.getString("eventId") ?: ""
            AddToMemoryWallScreen(navController, eventId)
        }

        composable(
            route = Routes.MemoryView.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType },
            )
        ) { entry ->
            val eventId = entry.arguments?.getString("eventId") ?: ""
            val userId = entry.arguments?.getString("userId") ?: ""
            MemoryViewScreen(navController, profileViewModel, eventId, userId)
        }
    }
}