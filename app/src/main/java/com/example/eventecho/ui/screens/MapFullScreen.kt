package com.example.eventecho.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.eventecho.ui.components.EventMap
import com.example.eventecho.ui.components.MapSearchBar
import com.example.eventecho.ui.viewmodels.EventMapViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState

private val BOSTON_LOCATION_FULL = LatLng(42.3601, -71.0589)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapFullScreen(
    navController: NavController,
    viewModel: EventMapViewModel
) {
    val context = LocalContext.current

    val mapPins by viewModel.mapPins.collectAsState()
    val cameraMove by viewModel.cameraMoveEvent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(BOSTON_LOCATION_FULL, 12f)
    }

    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }
    var permissionGranted by remember { mutableStateOf(false) }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            permissionGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }

    // Ask GPS permission
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else permissionGranted = true
    }

    // GPS fetch logic
    val fetchLocation: () -> Unit = fetchLocation@{
        if (!permissionGranted) return@fetchLocation

        fusedLocation.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val pos = LatLng(loc.latitude, loc.longitude)
                viewModel.updateUserLocation(pos)
                cameraState.position = CameraPosition.fromLatLngZoom(pos, 14f)
                return@addOnSuccessListener
            }

            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1500L
            ).setMaxUpdates(1).build()

            fusedLocation.requestLocationUpdates(
                request,
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val fresh = result.lastLocation ?: return
                        val pos = LatLng(fresh.latitude, fresh.longitude)
                        viewModel.updateUserLocation(pos)
                        cameraState.position =
                            CameraPosition.fromLatLngZoom(pos, 14f)
                        fusedLocation.removeLocationUpdates(this)
                    }
                },
                Looper.getMainLooper()
            )
        }
    }

    // Move map on search
    LaunchedEffect(cameraMove) {
        cameraMove?.let {
            cameraState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 14f),
                durationMs = 900
            )
            viewModel.onCameraMoved()
        }
    }

    // React to map idle events
    LaunchedEffect(cameraState.isMoving) {
        if (!cameraState.isMoving) {
            viewModel.onMapCameraIdle(cameraState.position.target)
        }
    }

    // MAIN UI
    Box(Modifier.fillMaxSize()) {
        // MAP
        EventMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            events = mapPins,
            isMyLocationEnabled = permissionGranted,
            onMarkerClick = { pin ->
                navController.navigate("event_detail/${pin.id}")
            }
        )

        // SEARCH BAR
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            MapSearchBar { query ->
                viewModel.performSearch(query, context)
            }
        }

        // GPS BUTTON
        IconButton(
            onClick = { fetchLocation() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 16.dp)
                .background(Color.White, MaterialTheme.shapes.medium)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Current Location")
        }

        // LOADING OVERLAY
        if (isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}
