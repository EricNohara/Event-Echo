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
import androidx.compose.material.icons.filled.Add
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
import com.example.eventecho.ui.components.EventList
import com.example.eventecho.ui.components.MapSearchBar
import com.example.eventecho.ui.navigation.Routes
import com.example.eventecho.ui.viewmodels.EventMapViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventMapHomeScreen(
    navController: NavController,
    viewModel: EventMapViewModel
) {
    val context = LocalContext.current

    val events by viewModel.events.collectAsState()
    val mapPins by viewModel.mapPins.collectAsState()
    val cameraMove by viewModel.cameraMoveEvent.collectAsState()

    val cameraPositionState = rememberCameraPositionState()

    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }
    var permissionGranted by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            permissionGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }

    // Ask permission once
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
        } else {
            permissionGranted = true
        }
    }

    // GPS fetch
    val fetchLocation: () -> Unit = fetchLocation@{
        if (!permissionGranted) return@fetchLocation

        // Try last known first
        fusedLocation.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val pos = LatLng(loc.latitude, loc.longitude)
                viewModel.updateUserLocation(pos)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(pos, 14f)
                return@addOnSuccessListener
            }

            // Request new location
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
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(pos, 14f)
                        fusedLocation.removeLocationUpdates(this)
                    }
                },
                Looper.getMainLooper()
            )
        }
    }

    // Run GPS fetch once on launch
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) fetchLocation()
    }

    // Camera move from a search
    LaunchedEffect(cameraMove) {
        cameraMove?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 14f),
                durationMs = 900
            )
            viewModel.onCameraMoved()
        }
    }

    // Detect when map stops
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            viewModel.onMapCameraIdle(cameraPositionState.position.target)
        }
    }

    Scaffold { padding ->
        Column {
            // ===== MAP SECTION =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                EventMap(
                    cameraPositionState = cameraPositionState,
                    events = mapPins,
                    isMyLocationEnabled = permissionGranted,
                    onMarkerClick = { pin ->
                        navController.navigate("event_detail/${pin.id}")
                    }
                )

                // Search bar above map
                Box(Modifier.align(Alignment.TopCenter)) {
                    MapSearchBar { query ->
                        viewModel.performSearch(query, context)
                    }
                }

                // GPS button
                IconButton(
                    onClick = { fetchLocation() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 90.dp, end = 16.dp)
                        .background(Color.White, MaterialTheme.shapes.medium)
                ) {
                    Icon(Icons.Default.MyLocation, "Current Location")
                }

                // === LOADING OVERLAY ===
                if (isLoading) {
                    Box(
                        modifier = Modifier
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

            // ===== EVENT LIST =====
            EventList(
                navController = navController,
                events = events // <-- now these are Firestore events
            )
        }
    }
}
