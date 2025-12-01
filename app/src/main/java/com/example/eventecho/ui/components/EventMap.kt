package com.example.eventecho.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.eventecho.ui.dataclass.SearchCircle
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
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
) {
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = isMyLocationEnabled)
    ) {

        searchCircle?.let { circle ->
            Circle(
                center = circle.center,
                radius = circle.radiusMeters,
                fillColor = Color(0x204285F4),
                strokeColor = Color.Blue,
                strokeWidth = 2f
            )
        }

        events.forEach { event ->
            Marker(
                state = MarkerState(position = event.location),
                title = event.title,
                snippet = event.snippet,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                onClick = {
                    onMarkerClick(event)
                    false
                }
            )
        }
    }
}