package com.example.eventecho.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState


@Composable
fun EventMap(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
    events: List<EventPin> = emptyList(),
    onMarkerClick: (EventPin) -> Unit = {}
) {
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
    ) {
        events.forEach { event ->
            Marker(
                state = MarkerState(position = event.location),
                title = event.title,
                snippet = event.snippet,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                onClick = {
                    Log.d("EventMap", "Clicked on marker: ${event.title}")
                    onMarkerClick(event)
                    // Return false to allow the default behavior (show info window)
                    false
                }
            )
        }
    }
}