package com.example.eventecho.ui.screens

import android.app.Application
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventecho.R
import com.example.eventecho.ui.viewmodels.CreateEventViewModel
import com.example.eventecho.ui.viewmodels.CreateEventViewModelFactory
import com.example.eventecho.data.firebase.EventRepository
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import com.example.eventecho.ui.components.DatePicker
import com.example.eventecho.ui.components.ImageSelectorCard
import com.example.eventecho.ui.components.LimitedTextField
import com.example.eventecho.ui.components.LocationSearchBar
import com.example.eventecho.ui.components.SimpleTextField
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateEventScreen(
    navController: NavController,
    repo: EventRepository
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application

    val viewModel: CreateEventViewModel = viewModel(
        factory = CreateEventViewModelFactory(repo, app)
    )

    val ui by viewModel.ui.collectAsState()

    val usingCurrentLocation: Boolean = false

    // CAMERA SUPPORT
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

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

    // GALLERY PICKER
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) viewModel.onImageSelected(uri)
    }

    val canCreate = ui.title.isNotBlank() &&
            ui.locationLat != null &&
            ui.locationLng != null

    Scaffold(
        bottomBar = {
            CreateEventBottomBar(
                isLoading = ui.isLoading,
                canCreate = canCreate,
                onCancel = { navController.popBackStack() },
                onCreate = {
                    viewModel.createEvent(
                        onSuccess = { id -> navController.navigate("event_detail/$id") },
                        onError = { e -> println("Error creating event: $e") }
                    )
                }
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
            ImageSelectorCard(
                imageUri = ui.imageUri,
                isLoading = ui.isLoading,
                onGalleryClick = { pickImageLauncher.launch("image/*") },
                onCameraClick = {
                    val newUri = createImageUri()
                    cameraImageUri = newUri
                    takePictureLauncher.launch(newUri)
                },
            )

            Spacer(Modifier.height(16.dp))

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
                        }
                    }
                )

                Text("Use Current Location")
            }

            LocationSearchBar(
                locationName = ui.locationName,
                onLocationNameChange = {
                    if (!ui.usingCurrentLocation) viewModel.onLocationNameChange(it)
                },
                userLat = ui.userLat,
                userLng = ui.userLng,
                onLocationSelected = { name, lat, lng ->
                    if (!ui.usingCurrentLocation)
                        viewModel.onLocationSelected(name, lat, lng)
                },
                enabled = !ui.usingCurrentLocation
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title field takes most of the width
                SimpleTextField(
                    value = ui.title,
                    onValueChange = viewModel::onTitleChange,
                    label = "Event Title",
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(12.dp))

                // Date picker takes minimal width
                DatePicker(
                    initialDate = ui.date,
                    onDateSelected = viewModel::onDateChange
                )
            }

            Spacer(Modifier.height(12.dp))

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

@Composable
fun EventImagePicker(
    imageUri: Uri?,
    isLoading: Boolean,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {

        // Big circle (tap = gallery)
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onGalleryClick() },
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()

                imageUri != null -> AsyncImage(
                    model = imageUri,
                    contentDescription = "Event Image",
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )

                else -> Image(
                    painter = painterResource(R.drawable.default_image),
                    contentDescription = "Default Event Image",
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            }
        }

        // Camera button
        Box(
            modifier = Modifier
                .size(42.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onCameraClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = "Camera")
        }
    }

    Spacer(Modifier.height(12.dp))

    Text(
        "Tap image to choose from gallery, or use camera",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}


@Composable
fun EventInputField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(4.dp))

        TextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(12.dp)),
            minLines = minLines,
            maxLines = maxLines,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Spacer(Modifier.height(18.dp))
    }
}

@Composable
fun CreateEventBottomBar(
    isLoading: Boolean,
    canCreate: Boolean,
    onCancel: () -> Unit,
    onCreate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }

            Spacer(Modifier.width(16.dp))

            Button(
                onClick = onCreate,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                enabled = !isLoading && canCreate
            ) {
                Text(if (isLoading) "Creating..." else "Create")
            }
        }
    }
}
