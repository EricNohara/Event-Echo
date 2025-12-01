package com.example.eventecho.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.CameraPositionState
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
    onMarkerClick: (EventPin) -> Unit = {}
) {
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = isMyLocationEnabled)
    ) {
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