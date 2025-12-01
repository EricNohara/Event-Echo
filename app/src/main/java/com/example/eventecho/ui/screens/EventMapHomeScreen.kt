package com.example.eventecho.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.eventecho.ui.components.EventList
import com.example.eventecho.ui.components.EventMap
import com.example.eventecho.ui.components.MapSearchBar
import com.example.eventecho.ui.navigation.Routes
import com.example.eventecho.ui.viewmodels.EventMapViewModel
import com.google.android.gms.location.LocationServices
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
    val cameraMoveEvent by viewModel.cameraMoveEvent.collectAsState() // signal to move map to location after search

    // map and gps
    val cameraPositionState = rememberCameraPositionState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isLocationPermissionGranted by remember { mutableStateOf(false) }
    var didInitialCameraCenter by rememberSaveable { mutableStateOf(false) } //so it stays in the searched city when going back a screen


    LaunchedEffect(cameraMoveEvent) {
        cameraMoveEvent?.let { target ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, 12f), 1000)
            viewModel.onCameraMoved() // Reset so we don't move again after config change
        }
    }

    //Fetch event for current area once map stops moving
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            viewModel.onMapCameraIdle(cameraPositionState.position.target)
        }
    }

    val fetchLocation = remember {
        { forceCameraMove: Boolean ->
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionGranted = true
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {

                        val latLng = LatLng(location.latitude, location.longitude)
                        viewModel.updateUserLocation(latLng)

                        if (forceCameraMove) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 12f)
                        }
                    }
                }
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            isLocationPermissionGranted = true
            fetchLocation(true)
        }
    }

    // 1st Load
    LaunchedEffect(Unit) {
        if (!didInitialCameraCenter) {
            didInitialCameraCenter = true

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                isLocationPermissionGranted = true
                fetchLocation(true)
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.CreateEvent.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Create Event")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                EventMap(
                    modifier = Modifier.fillMaxSize(),
                    events = mapPins,
                    cameraPositionState = cameraPositionState,
                    isMyLocationEnabled = isLocationPermissionGranted,
                    onMarkerClick = { pin ->
                        navController.navigate("event_detail/${pin.id}")
                    }
                )

                Box(modifier = Modifier.align(Alignment.TopCenter)) {
                    MapSearchBar(
                        onSearch = { query ->
                            viewModel.performSearch(query, context)
                        }
                    )
                }

                IconButton(
                    onClick = { fetchLocation(true) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 100.dp, end = 16.dp)
                        .background(Color.LightGray, MaterialTheme.shapes.large)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Current Location")
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                EventList(navController, events)
            }
        }
    }
}

