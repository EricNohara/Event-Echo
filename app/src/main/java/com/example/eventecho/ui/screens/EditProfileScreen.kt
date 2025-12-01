package com.example.eventecho.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventecho.ui.viewmodels.EditProfileViewModel
import androidx.compose.ui.text.input.ImeAction

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) viewModel.uploadImage(uri)
        }
    )

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            EditProfileTopBar(
                onCancel = { navController.popBackStack() },
                onDone = {
                    viewModel.saveProfile()
                    navController.popBackStack()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            ProfilePhotoSection(
                profilePicUrl = uiState.profilePicUrl,
                uploading = uiState.uploadingPicture,
                onClick = { imagePicker.launch("image/*") }
            )

            Spacer(Modifier.height(24.dp))

            SimpleTextField(
                label = "Username",
                value = uiState.username,
                onValueChange = viewModel::onUsernameChange
            )

            SimpleTextField(
                label = "Bio",
                value = uiState.bio,
                onValueChange = viewModel::onBioChange,
                singleLine = false,
                maxLines = 4
            )
        }
    }
}

@Composable
fun ProfilePhotoSection(
    profilePicUrl: String?,
    uploading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color.LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            uploading ->
                CircularProgressIndicator()

            profilePicUrl != null ->
                AsyncImage(
                    model = profilePicUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.clip(CircleShape)
                )

            else ->
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(80.dp)
                )
        }
    }

    Spacer(Modifier.height(12.dp))

    Text(
        text = "Change Profile Photo",
        color = Color.Blue,
        fontSize = 14.sp,
        modifier = Modifier.clickable { onClick() }
    )
}


@Composable
fun EditProfileTopBar(onCancel: () -> Unit, onDone: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onCancel) {
            Text("Cancel", fontSize = 16.sp, color = Color.Black)
        }

        Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        TextButton(onClick = onDone) {
            Text("Done", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
        }
    }
}

@Composable
fun SimpleTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.LightGray
            ),
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            )
        )
    }
}