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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.eventecho.ui.components.*
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
    var mapLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(mapLoaded) {
        if (mapLoaded) {
            val center = cameraPositionState.position.target
            viewModel.onMapCameraIdle(center)
        }
    }

    // LOCATION PERMISSIONS
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }
    var permissionGranted by remember { mutableStateOf(false) }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            permissionGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
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

    // GPS FETCH
    fun fetchLocation() {
        if (!permissionGranted) return

        fusedLocation.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val pos = LatLng(loc.latitude, loc.longitude)
                viewModel.updateUserLocation(pos)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(pos, 14f)
                return@addOnSuccessListener
            }

            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1500L
            ).setMaxUpdates(1).build()

            fusedLocation.requestLocationUpdates(
                request,
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val fresh = result.lastLocation ?: return
                        val pos = LatLng(fresh.latitude, fresh.longitude)
                        viewModel.updateUserLocation(pos)
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(pos, 14f)
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

    // SEARCH BAR CAMERA MOVE (ONLY TOP BAR)
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

    // FILTER STATE (Dropdown)
    val radiusState = remember { mutableStateOf(viewModel.radiusKm) }
    val startDateState = remember { mutableStateOf(viewModel.selectedDate) }
    val endDateState = remember { mutableStateOf(viewModel.selectedDate) }
    val searchQuery = remember { mutableStateOf("") }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    // FILTER CHANGES - ZOOM + REFRESH
    LaunchedEffect(radiusState.value, startDateState.value, endDateState.value) {

        viewModel.radiusKm = radiusState.value
        viewModel.selectedDate = startDateState.value

        val center = cameraPositionState.position.target

        if (mapLoaded) {
            val zoom = radiusToZoom(radiusState.value)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(center, zoom),
                durationMs = 500
            )
        }

        viewModel.onMapCameraIdle(center)
    }

    // MAP CAMERA IDLE
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            viewModel.onMapCameraIdle(cameraPositionState.position.target)
        }
    }

    // SEARCH BAR BELOW MAP — CLIENT FILTER ONLY
    val filteredEvents by remember(events, searchQuery.value) {
        derivedStateOf {
            val q = searchQuery.value.trim().lowercase()
            if (q.isBlank()) events
            else events.filter { it.title.lowercase().contains(q) }
        }
    }

    // UI
    Scaffold {
        Column(Modifier.fillMaxWidth()) {

            // MAP
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

                Box(Modifier.align(Alignment.TopCenter)) {
                    MapSearchBar { query ->
                        viewModel.performSearch(query, context)
                    }
                }

                IconButton(
                    onClick = { fetchLocation() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 90.dp, end = 16.dp)
                        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                ) {
                    Icon(Icons.Default.MyLocation, "Current Location")
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // FILTER ROW
            FilterRow(
                radiusState = radiusState,
                startDateState = startDateState,
                endDateState = endDateState,
                searchQuery = searchQuery,
                filterMenuExpanded = filterMenuExpanded,
                onExpandMenu = { filterMenuExpanded = it }
            )

            // EVENT GRID
            EventGrid(
                navController = navController,
                events = filteredEvents
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

        Row(verticalAlignment = Alignment.CenterVertically) {

            // SEARCH BAR (filters only list)
            TextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                placeholder = { Text("Search events") },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                singleLine = true,
                shape = RoundedCornerShape(20),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // FILTER BUTTON
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
                    onDismissRequest = { onExpandMenu(false) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.tertiary).padding(16.dp),
                ) {

                    // Radius
                    Text("Radius: ${radiusState.value} miles")

                    Slider(
                        value = radiusState.value.toFloat(),
                        onValueChange = { radiusState.value = it.toInt() },
                        valueRange = 1f..100f,
                        steps = 99,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.onTertiary,
                            activeTrackColor = MaterialTheme.colorScheme.onTertiary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.3f),
                            activeTickColor = MaterialTheme.colorScheme.onTertiary,
                            inactiveTickColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Start Date Picker
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

                    // End Date Picker
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("End Date")
                        DatePicker(
                            initialDate = endDateState.value,
                            onDateSelected = { endDateState.value = it }
                        )
                    }
                }
            }
        }
    }
}

// Convert miles radius → approximate zoom
fun radiusToZoom(radius: Int): Float {
    return when (radius) {
        in 1..2 -> 14f
        in 3..5 -> 13f
        in 6..10 -> 12f
        in 11..20 -> 11f
        in 21..40 -> 10f
        in 41..70 -> 9f
        in 71..100 -> 8f
        else -> 7f
    }
}