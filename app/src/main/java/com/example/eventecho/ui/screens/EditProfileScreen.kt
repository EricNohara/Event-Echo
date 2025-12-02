package com.example.eventecho.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventecho.ui.viewmodels.EditProfileViewModel
import androidx.compose.ui.text.input.ImeAction
import com.example.eventecho.R

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) viewModel.uploadImage(uri)
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            SimpleTopBar()
        },
        bottomBar = {
            BottomActionBar(
                onCancel = { navController.popBackStack() },
                onSave = {
                    viewModel.saveProfile()
                    navController.popBackStack()
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            AvatarSection(
                profilePicUrl = uiState.profilePicUrl,
                uploading = uiState.uploadingPicture,
                onClick = { picker.launch("image/*") }
            )

            Spacer(Modifier.height(28.dp))

            ProfileInput(
                label = "Display Name",
                value = uiState.username,
                onChange = viewModel::onUsernameChange
            )

            ProfileInput(
                label = "Bio",
                value = uiState.bio,
                onChange = viewModel::onBioChange,
                maxLines = 8,
                minLines = 6
            )

            Spacer(Modifier.height(90.dp)) // leave room above bottom buttons
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar() {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Edit Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun AvatarSection(profilePicUrl: String?, uploading: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            when {
                uploading -> CircularProgressIndicator()

                profilePicUrl != null -> AsyncImage(
                    model = profilePicUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )

                else -> Image(
                    painter = painterResource(R.drawable.default_avatar),
                    contentDescription = "Default Avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = "Edit Photo")
        }
    }

    Spacer(Modifier.height(12.dp))

    Text("Click the camera icon to change avatar", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun ProfileInput(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    maxLines: Int = 1,
    minLines: Int = 1
) {
    Box(
        modifier = Modifier
            .fillMaxWidth() // take full width so it's not centered by parent
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            TextField(
                value = value,
                onValueChange = onChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                maxLines = maxLines,
                minLines = minLines,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(Modifier.height(18.dp))
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }

            Spacer(Modifier.width(16.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        }
    }
}
