package com.example.eventecho

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.eventecho.ui.components.BottomBar
import com.example.eventecho.ui.components.TopBar
import com.example.eventecho.ui.navigation.AppNavGraph
import com.example.eventecho.ui.navigation.Routes
import com.example.eventecho.ui.theme.EventEchoTheme
import com.example.eventecho.data.datastore.readDarkMode
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventecho.ui.viewmodels.UserViewModel
import com.example.eventecho.ui.viewmodels.UserViewModelFactory
import com.example.eventecho.data.firebase.UserRepository
import com.google.android.libraries.places.api.Places

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventEchoApp() {
    val navController = rememberNavController()

    // Logging for navigation between screens
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.i("Navigation", "Navigated to ${destination.route}")
        }
    }

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val hideBottomBar = currentRoute in listOf(
        Routes.SignIn.route,
        Routes.SignUp.route
    )
    val showBottomBar = !hideBottomBar

    val context = LocalContext.current
    val isDarkMode by context.readDarkMode().collectAsState(initial = false)

    val systemUiController = rememberSystemUiController()

    val userRepo = remember { UserRepository() }

    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(userRepo)
    )

    val userState by userViewModel.uiState.collectAsState()

    // initialize Places
    LaunchedEffect(Unit) {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.PLACES_API_KEY)
        }
    }


    EventEchoTheme(darkTheme = isDarkMode) {

        val bgColor = MaterialTheme.colorScheme.background

        SideEffect {
            systemUiController.setStatusBarColor(
                color = bgColor,
                darkIcons = false
            )
            systemUiController.setNavigationBarColor(
                color = bgColor,
                darkIcons = false
            )
        }

        val hideTopBar = currentRoute in listOf(
            Routes.SignIn.route,
            Routes.SignUp.route
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (!hideTopBar) {
                    TopBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        profilePicUrl = userState.profilePicUrl,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) BottomBar(navController)
            },
            containerColor = bgColor
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = bgColor
            ) {
                AppNavGraph(navController)
            }
        }
    }
}