package com.example.eventecho.ui.screens

import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.eventecho.ui.viewmodels.EditProfileViewModel
import com.example.eventecho.ui.components.SimpleTextField
import com.example.eventecho.ui.components.LimitedTextField
import com.example.eventecho.ui.components.ImageSelectorCard
import java.io.File
import androidx.core.content.FileProvider
import com.example.eventecho.ui.viewmodels.ProfileViewModel
import com.example.eventecho.ui.viewmodels.UserViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = viewModel(),
    profileViewModel: ProfileViewModel,
    userViewModel: UserViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- IMAGE PICKERS ---
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    // Helper function for creating temp image URI from file
    fun createTempImageUri(): Uri {
        val imageFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "profile_${System.currentTimeMillis()}.jpg"
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraUri != null) {
            viewModel.uploadImage(cameraUri!!)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) viewModel.uploadImage(uri)
    }

    // Loading overlay
    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        bottomBar = {
            BottomActionBar(
                onCancel = { navController.popBackStack() },
                onSave = {
                    viewModel.saveProfile()
                    profileViewModel.refreshUser()
                    userViewModel.refreshUser()
                    navController.popBackStack()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // Select Image from Gallery or Camera
            ImageSelectorCard(
                imageUri = uiState.profilePicUrl?.let { Uri.parse(it) },
                isLoading = uiState.uploadingPicture,
                onGalleryClick = { galleryLauncher.launch("image/*") },
                onCameraClick = {
                    val newUri = createTempImageUri()
                    cameraUri = newUri
                    cameraLauncher.launch(newUri)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
            )

            Spacer(Modifier.height(28.dp))

            // Display Name
            SimpleTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChange,
                label = "Display Name"
            )

            Spacer(Modifier.height(16.dp))

            // Bio
            LimitedTextField(
                value = uiState.bio,
                onValueChange = viewModel::onBioChange,
                label = "Bio",
                maxChars = 300,
                minLines = 6,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun BottomActionBar(onCancel: () -> Unit, onSave: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Cancel")
            }

            Spacer(Modifier.width(16.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save")
            }
        }
    }
}
