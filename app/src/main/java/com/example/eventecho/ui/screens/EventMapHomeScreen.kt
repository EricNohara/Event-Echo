package com.example.eventecho.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.eventecho.ui.components.EventMap
import com.example.eventecho.ui.components.EventPin
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState

private val BOSTON_LOCATION = LatLng(42.3601, -71.0589)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventMapHomeScreen(
    navController: NavController,
    //  how we'll navigate later
//    onNavigateToFullScreen: () -> Unit = {},
//    onNavigateToEventDetails: (String) -> Unit = {}
) {
    val bostonCameraState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(BOSTON_LOCATION, 12f)
    }

    val bostonEvents by remember {
        mutableStateOf(
            listOf(
                EventPin(
                    id = "1",
                    location = LatLng(42.3584, -71.0598), // Boston Common
                    title = "Event at Boston Common",
                    snippet = "Click to see 12 echos"
                ),
                EventPin(
                    id = "2",
                    location = LatLng(42.3656, -71.0542), // Faneuil Hall
                    title = "Marketplace Gathering",
                    snippet = "Click to see 8 echos"
                ),
                EventPin(
                    id = "3",
                    location = LatLng(42.3467, -71.0972), // Fenway Park
                    title = "Game Day",
                    snippet = "Click to see 45 echos"
                )
            )
        )
    }

    Scaffold(
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            EventMap(
                cameraPositionState = bostonCameraState,
                events = bostonEvents,
                onMarkerClick = { eventPin ->
                    Log.d("HomeScreen", "Marker clicked in HomeScreen: ${eventPin.title}")

                    // onNavigateToEventDetails(eventPin.id)
                }
            )
        }
    }
}