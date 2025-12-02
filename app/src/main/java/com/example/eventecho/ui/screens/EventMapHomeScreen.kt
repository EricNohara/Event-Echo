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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
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
import com.example.eventecho.ui.components.DatePicker
import com.example.eventecho.ui.viewmodels.EventMapViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import java.time.LocalDate

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
    val isLoading by viewModel.isLoading.collectAsState()

    val cameraPositionState = rememberCameraPositionState()

    // Track when Google Maps is ready to accept CameraUpdateFactory calls
    var mapLoaded by remember { mutableStateOf(false) }

    // ----------------------------------------------------
    // LOCATION PERMISSIONS
    // ----------------------------------------------------

    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }
    var permissionGranted by remember { mutableStateOf(false) }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            permissionGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }

    // Request permissions once
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

    // ----------------------------------------------------
    // GPS FETCH
    // ----------------------------------------------------

    val fetchLocation = fun() {
        if (!permissionGranted) return

        fusedLocation.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val pos = LatLng(loc.latitude, loc.longitude)
                viewModel.updateUserLocation(pos)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(pos, 14f)
                return@addOnSuccessListener
            }

            // fallback request
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

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) fetchLocation()
    }

    // ----------------------------------------------------
    // SEARCH CAMERA MOVEMENT
    // ----------------------------------------------------

    LaunchedEffect(cameraMove) {
        cameraMove?.let {
            if (mapLoaded) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(it, 14f),
                    durationMs = 900
                )
            }
            viewModel.onCameraMoved()
        }
    }

    // ----------------------------------------------------
    // FILTER STATE
    // ----------------------------------------------------

    val radiusState = remember { mutableStateOf(viewModel.radiusKm) }
    val startDateState = remember { mutableStateOf(viewModel.selectedDate) }
    val endDateState = remember { mutableStateOf(viewModel.selectedDate) }
    val searchQuery = remember { mutableStateOf("") }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    // ----------------------------------------------------
    // WHEN FILTERS CHANGE → UPDATE VIEWMODEL + ADJUST MAP
    // ----------------------------------------------------

    LaunchedEffect(radiusState.value, startDateState.value, endDateState.value) {

        viewModel.radiusKm = radiusState.value
        viewModel.selectedDate = startDateState.value

        val center = cameraPositionState.position.target

        if (mapLoaded) {
            val zoom = radiusKmToZoom(radiusState.value)

            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(center, zoom),
                durationMs = 600
            )
        }

        // refresh events
        viewModel.onMapCameraIdle(center)
    }

    // ----------------------------------------------------
    // MAP CAMERA IDLE → REFRESH EVENTS
    // ----------------------------------------------------

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val center = cameraPositionState.position.target
            viewModel.onMapCameraIdle(center)
        }
    }

    // ----------------------------------------------------
    // UI LAYOUT
    // ----------------------------------------------------

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            // =============================
            // MAP
            // =============================
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
                    },
                    onMapLoaded = { mapLoaded = true }
                )

                // Search Bar
                Box(Modifier.align(Alignment.TopCenter)) {
                    MapSearchBar { query ->
                        viewModel.performSearch(query, context)
                    }
                }

                // GPS
                IconButton(
                    onClick = fetchLocation,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 90.dp, end = 16.dp)
                        .background(Color.White, MaterialTheme.shapes.medium)
                ) {
                    Icon(Icons.Default.MyLocation, "Current Location")
                }

                // Loading
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }

            // =============================
            // FILTER ROW
            // =============================
            FilterRow(
                radiusState = radiusState,
                startDateState = startDateState,
                endDateState = endDateState,
                searchQuery = searchQuery,
                filterMenuExpanded = filterMenuExpanded,
                onExpandMenu = { filterMenuExpanded = it }
            )

            // =============================
            // EVENT LIST
            // =============================
            EventList(
                navController = navController,
                events = events
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FilterRow(
    radiusState: MutableState<Int>,
    startDateState: MutableState<LocalDate>,
    endDateState: MutableState<LocalDate>,
    searchQuery: MutableState<String>,
    filterMenuExpanded: Boolean,
    onExpandMenu: (Boolean) -> Unit,
) {

    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {

        // Row with search + filter button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            TextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                placeholder = { Text("Search events…") },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                singleLine = true,
                shape = RoundedCornerShape(20),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Dropdown anchor
            Box {
                Button(
                    onClick = { onExpandMenu(true) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Filters")
                }

                DropdownMenu(
                    expanded = filterMenuExpanded,
                    onDismissRequest = { onExpandMenu(false) }
                ) {

                    Text("Radius: ${radiusState.value} miles", Modifier.padding(12.dp))

                    Slider(
                        value = radiusState.value.toFloat(),
                        onValueChange = { radiusState.value = it.toInt() },
                        valueRange = 1f..100f,
                        steps = 99
                    )

                    Spacer(Modifier.height(12.dp))

                    // Centered date pickers
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Start Date")
                        DatePicker(
                            initialDate = startDateState.value,
                            onDateSelected = {
                                startDateState.value = it
                                if (endDateState.value.isBefore(it)) endDateState.value = it
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("End Date")
                        DatePicker(
                            initialDate = endDateState.value,
                            onDateSelected = {
                                endDateState.value = it
                            }
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Convert radius miles → map zoom level
// ----------------------------------------------------
fun radiusKmToZoom(radius: Int): Float {
    return when {
        radius <= 2 -> 15f
        radius <= 5 -> 13f
        radius <= 10 -> 12f
        radius <= 25 -> 11f
        radius <= 50 -> 10f
        radius <= 75 -> 9f
        else -> 8f
    }
}
