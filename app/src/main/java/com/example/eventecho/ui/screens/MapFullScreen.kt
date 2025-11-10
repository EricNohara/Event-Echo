package com.example.eventecho.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventecho.ui.components.EventMap
// import com.example.eventecho.ui.components.EventPin // No longer needed
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState

private val BOSTON_LOCATION_FULL = LatLng(42.3601, -71.0589)

@Composable
fun MapFullScreen(
    navController: NavController,
) {
    val fullScreenCameraState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(BOSTON_LOCATION_FULL, 12f)
    }


    Box(modifier = Modifier.fillMaxSize()) {
        EventMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = fullScreenCameraState
        )
    }
}