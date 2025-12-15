package com.example.eventecho

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.content.pm.ActivityInfo
import android.content.res.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.google.firebase.FirebaseApp.initializeApp(this)

        val isTablet =
            resources.configuration.screenLayout and
                    Configuration.SCREENLAYOUT_SIZE_MASK >=
                    Configuration.SCREENLAYOUT_SIZE_LARGE

        if (!isTablet) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        setContent {
            EventEchoApp()
        }
    }
}