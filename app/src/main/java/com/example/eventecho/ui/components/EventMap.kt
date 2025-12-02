package com.example.eventecho.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.eventecho.R
import com.example.eventecho.ui.dataclass.SearchCircle
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun EventMap(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
    events: List<EventPin> = emptyList(),
    isMyLocationEnabled: Boolean = false,
    onMarkerClick: (EventPin) -> Unit = {},
    searchCircle: SearchCircle? = null,
    onMapLoaded: () -> Unit = {},
) {
//    val context = LocalContext.current
//    val isDarkTheme = isSystemInDarkTheme()
//
//    val mapStyleOptions = remember(isDarkTheme) {
//        if (isDarkTheme) {
//            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_dark_style)
//        } else {
//            null
//        }
//    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = isMyLocationEnabled,
//            mapStyleOptions = mapStyleOptions,
        ),
        onMapLoaded = onMapLoaded
    ) {
        // Circle overlay
        searchCircle?.let { circle ->
            Circle(
                center = circle.center,
                radius = circle.radiusMeters,
                fillColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
                strokeColor = MaterialTheme.colorScheme.primary,
                strokeWidth = 2f
            )
        }

        // Event markers
        events.forEach { event ->
            Marker(
                state = MarkerState(position = event.location),
                title = event.title,
                snippet = event.snippet,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                onClick = {
                    onMarkerClick(event)
                    false
                }
            )
        }
    }
}
