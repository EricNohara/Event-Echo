package com.example.eventecho

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.eventecho.ui.components.BottomBar
import com.example.eventecho.ui.navigation.AppNavGraph
import com.example.eventecho.ui.navigation.Routes
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    // Grant runtime permissions to avoid sensor/location dialogs
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACTIVITY_RECOGNITION
    )

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun bottomBar_navigatesBetweenScreens() {
        lateinit var navController: TestNavHostController

        composeRule.setContent {
            val context = LocalContext.current
            navController = TestNavHostController(context).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            // Custom test Scaffold containing BottomBar and AppNavGraph
            BottomBarTestScaffold(navController)
        }

        // Navigation clicks
        bottomBarClick("bottomNavHome", Routes.EventMapHome.route, navController)
        bottomBarClick("bottomNavMap", Routes.MapFullScreen.route, navController)
        bottomBarClick("bottomNavCreate", Routes.CreateEvent.route, navController)
    }

    private fun bottomBarClick(tag: String, expectedRoute: String, navController: TestNavHostController) {
        composeRule.onNodeWithTag(tag)
            .assertIsDisplayed()
            .performClick()

        composeRule.waitForIdle()
        assert(navController.currentDestination?.route == expectedRoute)
    }
}

@Composable
fun BottomBarTestScaffold(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AppNavGraph(navController)
        }
    }
}
