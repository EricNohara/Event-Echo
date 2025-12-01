package com.example.eventecho.ui.screens

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventecho.ui.components.DatePicker
import com.example.eventecho.ui.viewmodels.CreateEventViewModel
import com.example.eventecho.data.firebase.EventRepository
import java.time.LocalDate
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import com.example.eventecho.ui.viewmodels.CreateEventViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateEventScreen(
    navController: NavController,
    repo: EventRepository
) {
    val app = LocalContext.current.applicationContext as Application

    val viewModel: CreateEventViewModel = viewModel(
        factory = CreateEventViewModelFactory(repo, app)
    )

    val ui by viewModel.ui.collectAsState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) viewModel.onImageSelected(uri)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        TextField(
            value = ui.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Event Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        TextField(
            value = ui.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        DatePicker(
            initialDate = ui.date,
            onDateSelected = viewModel::onDateChange
        )

        Spacer(Modifier.height(12.dp))

        Button(onClick = { pickImageLauncher.launch("image/*") }) {
            Text(if (ui.imageUri == null) "Select Image" else "Image Selected")
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.createEvent(
                    onSuccess = { id ->
                        navController.navigate("event_detail/$id")
                    },
                    onError = { e -> println("Error creating event: $e") }
                )
            },
            enabled = !ui.isLoading
        ) {
            Text(if (ui.isLoading) "Creating..." else "Create Event")
        }

        Spacer(Modifier.height(20.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}
