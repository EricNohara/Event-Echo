package com.example.eventecho.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.collection.IntList
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import com.example.eventecho.ui.dataclass.Event
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.calculateStandardPaneScaffoldDirective
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.ui.platform.LocalConfiguration
import com.google.maps.android.compose.CameraPositionState

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventMapHomeScreen(
    navController: NavController,
    viewModel: EventMapViewModel
) {
    // for device detection
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val scaffoldDirective = calculateStandardPaneScaffoldDirective(adaptiveInfo)
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val useSplitLayout = isTablet && !isPortrait

    val mapHeight = when {
        isTablet && isPortrait -> 500.dp
        else -> 300.dp
    }

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

    // --- LOCATION PERMISSIONS ---
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }
    var permissionGranted by remember { mutableStateOf(false) }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
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
        } else {
            permissionGranted = true
        }
    }

    // --- FETCH GPS LOCATION ---
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

    // --- CAMERA MOVE SEARCH ---
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

    // --- FILTER STATE (UI State Only) ---
    val radiusState = remember { mutableStateOf<Int>(viewModel.radiusKm) }

    val startDateState = remember {
        mutableStateOf<LocalDate>(viewModel.selectedStartDate ?: LocalDate.now())
    }

    val endDateState = remember {
        mutableStateOf<LocalDate>(viewModel.selectedEndDate ?: LocalDate.now().plusDays(3))
    }

    val searchQuery = remember { mutableStateOf<String>("") }

    var filterMenuExpanded by remember { mutableStateOf(false) }

    val eventSourceOptions = listOf("Ticketmaster", "Users")
    val userEventOptions = listOf("Created", "Attended", "Favorites")

    var selectedSources by remember { mutableStateOf(setOf("Ticketmaster", "Users")) }
    var selectedUserFilters by remember { mutableStateOf(setOf<String>()) }

    // --- FILTER EFFECT: radius / startDate / endDate ---
    LaunchedEffect(radiusState.value, startDateState.value, endDateState.value, selectedSources, selectedUserFilters) {

        viewModel.radiusKm = radiusState.value
        viewModel.selectedStartDate = startDateState.value
        viewModel.selectedEndDate = endDateState.value

        viewModel.showTicketmasterEvents = "Ticketmaster" in selectedSources
        viewModel.showUserEvents = "Users" in selectedSources

        viewModel.showCreatedEvents = "Created" in selectedUserFilters
        viewModel.showAttendedEvents = "Attended" in selectedUserFilters
        viewModel.showFavoriteEvents = "Favorites" in selectedUserFilters

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

    // --- MAP CAMERA IDLE EVENT ---
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            viewModel.onMapCameraIdle(cameraPositionState.position.target)
        }
    }

    // --- SEARCH FILTER FOR LIST ONLY ---
    val filteredEvents by remember(events, searchQuery) {
        derivedStateOf<List<Event>> {
            val q = searchQuery.value.trim().lowercase()
            if (q.isBlank()) events
            else events.filter { it.title.lowercase().contains(q) }
        }
    }

    // --- UI ---
    Scaffold { padding ->
        if (useSplitLayout) {
            // TABLET LAYOUT
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // LEFT: MAP + SEARCH
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                ) {
                    MapSection(
                        navController = navController,
                        viewModel = viewModel,
                        cameraPositionState = cameraPositionState,
                        permissionGranted = permissionGranted,
                        fetchLocation = { fetchLocation() },
                        isLoading = isLoading,
                        onMapLoaded = { mapLoaded = true },
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                    )
                }

                // RIGHT: FILTERS + EVENT GRID
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                ) {
                    FilterRow(
                        radiusState = radiusState,
                        startDateState = startDateState,
                        endDateState = endDateState,
                        searchQuery = searchQuery,
                        filterMenuExpanded = filterMenuExpanded,
                        onExpandMenu = { filterMenuExpanded = it },
                        eventSourceOptions = eventSourceOptions,
                        userEventOptions = userEventOptions,
                        selectedSources = selectedSources,
                        onSelectedSourcesChange = { selectedSources = it },
                        selectedUserFilters = selectedUserFilters,
                        onSelectedUserFiltersChange = { selectedUserFilters = it }
                    )

                    Spacer(Modifier.height(8.dp))

                    EventGrid(
                        navController = navController,
                        events = filteredEvents
                    )
                }
            }

        } else {
            // PHONE LAYOUT
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                MapSection(
                    navController = navController,
                    viewModel = viewModel,
                    cameraPositionState = cameraPositionState,
                    permissionGranted = permissionGranted,
                    fetchLocation = { fetchLocation() },
                    isLoading = isLoading,
                    onMapLoaded = { mapLoaded = true },
                    modifier = Modifier.height(mapHeight)
                )

                FilterRow(
                    radiusState = radiusState,
                    startDateState = startDateState,
                    endDateState = endDateState,
                    searchQuery = searchQuery,
                    filterMenuExpanded = filterMenuExpanded,
                    onExpandMenu = { filterMenuExpanded = it },
                    eventSourceOptions = eventSourceOptions,
                    userEventOptions = userEventOptions,
                    selectedSources = selectedSources,
                    onSelectedSourcesChange = { selectedSources = it },
                    selectedUserFilters = selectedUserFilters,
                    onSelectedUserFiltersChange = { selectedUserFilters = it }
                )

                EventGrid(
                    navController = navController,
                    events = filteredEvents
                )
            }
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
    eventSourceOptions: List<String>,
    userEventOptions: List<String>,
    selectedSources: Set<String>,
    onSelectedSourcesChange: (Set<String>) -> Unit,
    selectedUserFilters: Set<String>,
    onSelectedUserFiltersChange: (Set<String>) -> Unit
) {

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            // --- FOCUS-AWARE SEARCH BAR ---
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()

            TextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                placeholder = { Text("Search events") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                singleLine = true,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(20),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isFocused)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
            )

            Spacer(modifier = Modifier.width(12.dp))

            // FILTER BUTTON
            Box {
                Button(
                    onClick = { onExpandMenu(true) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(58.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Filters")
                }

                DropdownMenu(
                    expanded = filterMenuExpanded,
                    onDismissRequest = { onExpandMenu(false) },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiary)
                ) {
                    Column (
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {

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

                        Spacer(Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column {
                                Text("Start Date")
                                DatePicker(
                                    initialDate = startDateState.value,
                                    onDateSelected = {
                                        startDateState.value = it
                                        if (endDateState.value.isBefore(it)) {
                                            endDateState.value = it
                                        }
                                    }
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.Start
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

                        Spacer(Modifier.height(8.dp))

                        // AI-Assisted Feature: Filter Chips for Event Source & Your Events
                        val chipColors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            labelColor = MaterialTheme.colorScheme.onTertiary,

                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        )

                        Text("Event Sources")

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            eventSourceOptions.forEach { option ->
                                FilterChip(
                                    selected = option in selectedSources,
                                    onClick = {
                                        val updated = selectedSources.toMutableSet()
                                        if (option in updated) {
                                            if (updated.size > 1) updated.remove(option)
                                        } else {
                                            updated.add(option)
                                        }
                                        onSelectedSourcesChange(updated)
                                    },
                                    label = { Text(option) },
                                    colors = chipColors,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Text("Your Events")

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            userEventOptions.forEach { option ->
                                FilterChip(
                                    selected = option in selectedUserFilters,
                                    onClick = {
                                        val updated = selectedUserFilters.toMutableSet()
                                        if (option in updated) {
                                            updated.remove(option)
                                        } else {
                                            updated.add(option)
                                        }
                                        onSelectedUserFiltersChange(updated)
                                    },
                                    label = { Text(option) },
                                    colors = chipColors,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// composable for the map section of the screen
@Composable
fun MapSection(
    navController: NavController,
    viewModel: EventMapViewModel,
    cameraPositionState: CameraPositionState,
    permissionGranted: Boolean,
    fetchLocation: () -> Unit,
    isLoading: Boolean,
    onMapLoaded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userLocation by viewModel.userLocation.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        EventMap(
            cameraPositionState = cameraPositionState,
            events = viewModel.mapPins.collectAsState().value,
            isMyLocationEnabled = permissionGranted,
            onMarkerClick = { pin ->
                navController.navigate("event_detail/${pin.id}")
            },
            onMapLoaded = onMapLoaded
        )

        Box(Modifier.align(Alignment.TopCenter)) {
            MapSearchBar(
                userLat = userLocation?.latitude ?: 0.0,
                userLng = userLocation?.longitude ?: 0.0,
                onSearch = { query ->
                    viewModel.performSearch(query, context)
                }
            )
        }

        IconButton(
            onClick = fetchLocation,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.shapes.medium
                )
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Current Location")
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
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