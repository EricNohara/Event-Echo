package com.example.eventecho.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventecho.data.firebase.MemoryRepository
import com.example.eventecho.data.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToMemoryWallScreen(
    navController: NavController,
    eventId: String
) {
    // Repositories (remember outside callbacks)
    val memoryRepo = remember {
        MemoryRepository(
            db = FirebaseFirestore.getInstance(),
            storage = FirebaseStorage.getInstance()
        )
    }

    val userRepo = remember { UserRepository() }

    // UI State
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Coroutine scope
    val coroutineScope = rememberCoroutineScope()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Memory") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("< Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {

            //------------------------------------------------------
            // IMAGE PREVIEW
            //------------------------------------------------------
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            //------------------------------------------------------
            // PICK IMAGE BUTTON
            //------------------------------------------------------
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Image")
            }

            Spacer(Modifier.height(16.dp))

            //------------------------------------------------------
            // DESCRIPTION INPUT
            //------------------------------------------------------
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Memory Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            //------------------------------------------------------
            // ERROR MESSAGE
            //------------------------------------------------------
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            //------------------------------------------------------
            // UPLOAD BUTTON
            //------------------------------------------------------
            Button(
                onClick = {
                    if (description.isBlank() || imageUri == null) {
                        errorMessage = "Please select an image and enter a description."
                        return@Button
                    }

                    val userId = FirebaseAuth.getInstance().currentUser!!.uid

                    isUploading = true
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            //--------------------------------------------------
                            // 1. Upload image to Firebase Storage
                            //--------------------------------------------------
                            val imageUrl = memoryRepo.uploadMemoryImage(
                                eventId = eventId,
                                userId = userId,
                                imageUri = imageUri!!
                            )

                            //--------------------------------------------------
                            // 2. Create memory document in Firestore
                            //--------------------------------------------------
                            memoryRepo.createMemory(
                                eventId = eventId,
                                userId = userId,
                                description = description,
                                imageUrl = imageUrl
                            )

                            //--------------------------------------------------
                            // 3. Add this event to the user's attended list
                            //--------------------------------------------------
                            userRepo.addEventToAttended(eventId)

                            //--------------------------------------------------
                            // 4. Navigate back to Memory Wall
                            //--------------------------------------------------
                            navController.popBackStack()

                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to upload memory."
                        } finally {
                            isUploading = false
                        }
                    }
                },
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth()
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
}
