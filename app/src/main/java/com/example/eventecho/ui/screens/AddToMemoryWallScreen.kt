package com.example.eventecho.ui.screens

import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventecho.data.firebase.MemoryRepository
import com.example.eventecho.data.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import com.example.eventecho.ui.components.LimitedTextField
import com.example.eventecho.ui.components.ImageSelectorCard
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToMemoryWallScreen(
    navController: NavController,
    eventId: String
) {
    // Memory & User repositories
    val memoryRepo = remember {
        MemoryRepository(
            db = FirebaseFirestore.getInstance(),
            storage = FirebaseStorage.getInstance()
        )
    }

    val userRepo = remember { UserRepository() }

    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val maxChars = 400

    /** Create a temporary file and return its URI */
    fun createImageUri(): Uri {
        val imageFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "memory_${System.currentTimeMillis()}.jpg"
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    /** Camera launcher */
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            imageUri = cameraImageUri
        }
    }

    /** Gallery launcher */
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
        }
    }

    Scaffold(
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = {
                        val userId = FirebaseAuth.getInstance().currentUser!!.uid
                        isUploading = true
                        errorMessage = null

                        coroutineScope.launch {
                            try {
                                val url = memoryRepo.uploadMemoryImage(
                                    eventId, userId, imageUri!!
                                )

                                memoryRepo.createMemory(
                                    eventId = eventId,
                                    userId = userId,
                                    description = description,
                                    imageUrl = url
                                )

                                userRepo.addEventToAttended(eventId)
                                navController.popBackStack()

                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Upload failed."
                            } finally {
                                isUploading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !isUploading &&
                            imageUri != null &&
                            description.isNotBlank() &&
                            description.length <= maxChars
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Upload Memory")
                    }
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            // IMAGE SELECTOR WITH CAMERA SUPPORT
            ImageSelectorCard(
                imageUri = imageUri,
                isLoading = isUploading,
                onGalleryClick = { imagePickerLauncher.launch("image/*") },
                onCameraClick = {
                    val newUri = createImageUri()
                    cameraImageUri = newUri
                    takePictureLauncher.launch(newUri)
                },
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // DESCRIPTION WITH CHAR LIMIT
            LimitedTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                maxChars = maxChars,
                minLines = 10,
                modifier = Modifier.fillMaxWidth()
            )

            // ERROR MESSAGE
            errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
