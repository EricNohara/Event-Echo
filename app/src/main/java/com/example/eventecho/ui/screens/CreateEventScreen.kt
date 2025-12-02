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
import com.example.eventecho.ui.components.DatePicker
import com.example.eventecho.ui.viewmodels.CreateEventViewModel
import com.example.eventecho.ui.viewmodels.CreateEventViewModelFactory
import com.example.eventecho.data.firebase.EventRepository

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

    // Image picker launcher
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) viewModel.onImageSelected(uri)
    }

    Scaffold(
        topBar = {
            CreateEventTopBar(onBack = { navController.popBackStack() })
        },
        bottomBar = {
            CreateEventBottomBar(
                isLoading = ui.isLoading,
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            EventImagePicker(
                imageUri = ui.imageUri,
                isLoading = ui.isLoading,
                onClick = { pickImageLauncher.launch("image/*") }
            )

            Spacer(Modifier.height(28.dp))

            EventInputField(
                label = "Event Title",
                value = ui.title,
                onChange = viewModel::onTitleChange
            )

            EventInputField(
                label = "Description",
                value = ui.description,
                onChange = viewModel::onDescriptionChange,
                minLines = 4,
                maxLines = 8
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Event Date",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            DatePicker(
                initialDate = ui.date,
                onDateSelected = viewModel::onDateChange
            )

            Spacer(Modifier.height(90.dp)) // leave space above bottom buttons
        }
    }
}

@Composable
fun CreateEventTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            "Create Event",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun EventImagePicker(imageUri: Uri?, isLoading: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {

        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onClick() },
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

        Box(
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = "Add Image")
        }
    }

    Spacer(Modifier.height(12.dp))

    Text("Tap image to select an event photo", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Creating..." else "Create")
            }
        }
    }
}
