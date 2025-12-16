package com.example.eventecho.ui.screens

import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.eventecho.data.firebase.EventRepository
import com.example.eventecho.ui.components.ImageSelectorCard
import com.example.eventecho.ui.components.LimitedTextField
import com.example.eventecho.ui.components.SimpleTextField
import java.io.File
import androidx.core.content.FileProvider
import com.example.eventecho.ui.viewmodels.EditEventViewModel
import com.example.eventecho.ui.viewmodels.EditEventViewModelFactory
import com.example.eventecho.ui.components.DatePicker
import com.example.eventecho.ui.components.LocationSearchBar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditEventScreen(
    navController: NavController,
    repo: EventRepository,
    eventId: String
) {
    // App, viewModel, & UI
    val context = LocalContext.current
    val app = context.applicationContext as Application

    val viewModel: EditEventViewModel = viewModel(
        factory = EditEventViewModelFactory(repo, eventId, app)
    )

    val ui by viewModel.ui.collectAsState()

    // CAMERA SUPPORT
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Helper function for creating an image URI from file
    fun createImageUri(): Uri {
        val imageFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "event_${System.currentTimeMillis()}.jpg"
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            viewModel.onImageSelected(cameraImageUri!!)
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) viewModel.onImageSelected(uri)
    }

    val canSave =
        ui.title.isNotBlank() &&
                ui.locationLat != null &&
                ui.locationLng != null

    Scaffold(
        bottomBar = {
            CreateEventBottomBar(
                isLoading = ui.isLoading,
                canCreate = canSave,
                onCancel = { navController.popBackStack() },
                onCreate = {
                    viewModel.saveChanges {
                        navController.popBackStack()
                    }
                },
                label = "Update",
                labelLoading = "Updating..."
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Select Image from Gallery or Camera
            ImageSelectorCard(
                imageUri = ui.imageUri,
                existingImageUrl = ui.existingImageUrl,
                isLoading = ui.isLoading,
                onGalleryClick = { pickImageLauncher.launch("image/*") },
                onCameraClick = {
                    val newUri = createImageUri()
                    cameraImageUri = newUri
                    takePictureLauncher.launch(newUri)
                }
            )

            Spacer(Modifier.height(16.dp))

            // Use Current Location checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = ui.usingCurrentLocation,
                    onCheckedChange = { checked ->
                        if (checked) {
                            viewModel.enableCurrentLocation()
                        } else {
                            viewModel.disableCurrentLocation()
                            viewModel.clearManualLocation()
                        }
                    }
                )
                Text("Use Current Location")
            }

            // Location Search Bar
            LocationSearchBar(
                locationName = ui.locationName,
                onLocationNameChange = {
                    if (!ui.usingCurrentLocation)
                        viewModel.onLocationNameChange(it)
                },
                userLat = ui.locationLat,
                userLng = ui.locationLng,
                onLocationSelected = { name, lat, lng ->
                    if (!ui.usingCurrentLocation)
                        viewModel.onLocationSelected(name, lat, lng)
                },
                enabled = !ui.usingCurrentLocation
            )

            Spacer(Modifier.height(12.dp))

            // Event Title & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleTextField(
                    value = ui.title,
                    onValueChange = viewModel::onTitleChange,
                    label = "Event Title",
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(12.dp))

                DatePicker(
                    initialDate = ui.date,
                    onDateSelected = viewModel::onDateChange
                )
            }

            Spacer(Modifier.height(12.dp))

            // Event Description
            LimitedTextField(
                value = ui.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Description",
                maxChars = 300,
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}