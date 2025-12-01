package com.example.eventecho.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.eventecho.ui.components.DatePicker
import com.example.eventecho.ui.components.DrawerContent
import com.example.eventecho.ui.components.DropdownInput
import com.example.eventecho.ui.components.EventList
import com.example.eventecho.ui.components.TopBar
import com.example.eventecho.ui.navigation.Routes
import com.example.eventecho.ui.viewmodels.EventMapViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventMapHomeScreen(
    navController: NavController,
    viewModel: EventMapViewModel
) {
    val events by viewModel.events.collectAsState()

    // Local UI state for filters
    val radiusState = remember { mutableStateOf(viewModel.radius) }
    val dateState = remember { mutableStateOf(viewModel.selectedDate) }

    val radiusOptions = listOf(1, 10, 25, 50)

    // 🔄 Whenever radius or date changes → refresh all events
    LaunchedEffect(radiusState.value, dateState.value) {
        viewModel.radius = radiusState.value
        viewModel.selectedDate = dateState.value
        viewModel.refreshEvents()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            // ---------- FILTER ROW ----------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                // Radius Dropdown
                DropdownInput(
                    selectedValue = radiusState.value.toString(),
                    label = "Radius (mi)",
                    options = radiusOptions.map { it.toString() },
                    onValueSelected = { newValue ->
                        radiusState.value = newValue.toInt()
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Date Picker
                DatePicker(
                    initialDate = dateState.value,
                    onDateSelected = { date ->
                        dateState.value = date
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            // ---------- EVENT LIST ----------
            EventList(navController = navController, events = events)
        }

        // ---------- FAB: ADD EVENT ----------
        FloatingActionButton(
            onClick = { navController.navigate(Routes.CreateEvent.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Event")
        }
    }
}