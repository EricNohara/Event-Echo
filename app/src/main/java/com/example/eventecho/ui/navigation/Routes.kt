package com.example.eventecho.ui.navigation

// sealed class for app's routes
sealed class Routes(val route: String) {
    // static routes
    object EventMapHome : Routes("event_map_home")
    object SignIn : Routes("sign_in")
    object SignUp : Routes("sign_up")
    object CreateEvent : Routes("create_event")
    object EditProfile : Routes("edit_profile")
    object SavedEvents : Routes("saved_events")
    object MapFullScreen : Routes("map_full_screen")
    object Profile: Routes("profile")

    // dynamic routes
    object EventDetail : Routes("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }

    object MemoryWall : Routes("memory_wall/{eventId}") {
        fun createRoute(eventId: String) = "memory_wall/$eventId"
    }

    object AddToMemoryWall : Routes("add_memory/{eventId}") {
        fun createRoute(eventId: String) = "add_memory/$eventId"
    }
}