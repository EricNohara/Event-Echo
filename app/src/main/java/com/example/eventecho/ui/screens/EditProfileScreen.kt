package com.example.eventecho.ui.screens

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.eventecho.ui.viewmodels.EditProfileViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), //lets you scroll if keyboard opens
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            ProfilePhotoSection()
            Spacer(modifier = Modifier.height(24.dp))

            SimpleTextField(
                label = "Name",
                value = uiState.name,
                onValueChange = viewModel::onNameChange
            )

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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
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
fun ProfilePhotoSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Change Profile Photo",
            color = Color.Blue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
//            modifier = Modifier.clickable() // TODO:  allow image upload
        )
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