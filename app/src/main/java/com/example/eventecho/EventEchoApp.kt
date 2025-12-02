package com.example.eventecho

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventEchoApp() {
    val navController = rememberNavController()

    // current route
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    //Screens in which we want a bottomBar to be shown
    val hideBottomBar = currentRoute in listOf(
        Routes.SignIn.route,
        Routes.SignUp.route
    )

    val showBottomBar = !hideBottomBar

    // Determine Light/Dark mode
    val context = LocalContext.current
    val isDarkMode by context.readDarkMode().collectAsState(initial = false)

    EventEchoTheme (
        darkTheme = isDarkMode
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {TopBar(navController)},
            bottomBar = {
                if (showBottomBar) {
                    BottomBar(navController)
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AppNavGraph(navController)
            }
        }
    }
}