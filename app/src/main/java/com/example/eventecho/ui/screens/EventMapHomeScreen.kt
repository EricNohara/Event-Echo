package com.example.eventecho.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.eventecho.ui.components.DatePicker
import com.example.eventecho.ui.components.DrawerContent
import com.example.eventecho.ui.components.DropdownInput
import com.example.eventecho.ui.components.EventList
import com.example.eventecho.ui.components.EventGrid
import com.example.eventecho.ui.components.TopBar
import com.example.eventecho.ui.navigation.Routes
import com.example.eventecho.ui.viewmodels.EventMapViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventMapHomeScreen(navController: NavController, viewModel: EventMapViewModel) {
    val events by viewModel.events.collectAsState()

    // state vars for storing input
    val radiusState = remember { mutableStateOf(viewModel.radius) }
    val startDateState = remember { mutableStateOf(LocalDate.now()) }
    val endDateState = remember { mutableStateOf(LocalDate.now()) }
    val searchQuery = remember { mutableStateOf("") }

    // fetch new events based off filters when they are changed
    LaunchedEffect(radiusState.value, startDateState.value) {
        viewModel.radius = radiusState.value
        viewModel.startDateTime = startDateState.value.atStartOfDay(java.time.ZoneOffset.UTC)
            .format(java.time.format.DateTimeFormatter.ISO_INSTANT)
        viewModel.fetchEvents()
    }

    // Screen content
    Box(
        modifier = Modifier.fillMaxWidth().fillMaxHeight().background(MaterialTheme.colorScheme.background)
    ) {
        Column {
            var filterMenuExpanded by remember { mutableStateOf(false) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {

                // Search Bar
                TextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    placeholder = { Text("Search events…") },
                    modifier = Modifier.weight(0.5f),
                    singleLine = true,
                    shape = RoundedCornerShape(20),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )

                )

                Spacer(modifier = Modifier.width(12.dp))

                Box {
                    // Filter Button
                    Box(
                        modifier = Modifier
                            .shadow(6.dp, RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp)),
                    ) {
                        IconButton(
                            onClick = { filterMenuExpanded = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Filter Dropdown
                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {

                        // Radius
                        Text(
                            "Radius: ${radiusState.value}",
                            modifier = Modifier.padding(12.dp)
                        )

                        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                            Slider(
                                value = radiusState.value.toFloat(),
                                onValueChange = { radiusState.value = it.toInt().toString() },
                                valueRange = 1f..500f,
                                steps = 49,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.onSurface,
                                    activeTrackColor = MaterialTheme.colorScheme.onSurface,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    activeTickColor = MaterialTheme.colorScheme.onSurface,
                                    inactiveTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Start Date
                        Text("Start Date", modifier = Modifier.padding(horizontal = 12.dp))
                        DatePicker(
                            initialDate = startDateState.value,
                            onDateSelected = {
                                startDateState.value = it
                                // Bound endDate to >= startDate
                                if (endDateState.value.isBefore(startDateState.value)) {
                                    endDateState.value = startDateState.value
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // End Date
                        Text("End Date", modifier = Modifier.padding(horizontal = 12.dp))
                        // key used to make sure endDate is recomposed if updated
                        key(endDateState.value) {
                            DatePicker(
                                initialDate = endDateState.value,
                                onDateSelected = { endDateState.value = it }
                            )
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(4.dp))

            // Display the event grid
            EventGrid(navController, events)
        }
    }
}