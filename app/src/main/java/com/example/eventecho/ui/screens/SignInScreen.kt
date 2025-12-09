package com.example.eventecho.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.example.eventecho.ui.navigation.Routes
import com.example.eventecho.ui.components.SimpleTextField
import com.example.eventecho.ui.theme.DarkPrimary


@Composable
fun SignInScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate(Routes.EventMapHome.route) {
                popUpTo(Routes.SignIn.route) { inclusive = true }
            }
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // --- FULL SCREEN ROOT ---
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // BACKGROUND IMAGE WITH BLUR + DARK OVERLAY
        AsyncImage(
            model = "https://cdn.pixabay.com/photo/2016/11/23/15/48/audience-1853662_1280.jpg",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(2.dp)
                .graphicsLayer { alpha = 0.85f }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
        )

        // MAIN CONTENT
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkPrimary
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "Welcome back to EventEcho",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Sign in now",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.LightGray
                    )

                    error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(48.dp))

                    SimpleTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email"
                    )

                    Spacer(Modifier.height(12.dp))

                    SimpleTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            when {
                                email.isBlank() -> error = "Email cannot be empty"
                                password.isBlank() -> error = "Password cannot be empty"
                                else -> {
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                error = null
                                                navController.navigate(Routes.EventMapHome.route) {
                                                    popUpTo(Routes.SignIn.route) { inclusive = true }
                                                }
                                            } else {
                                                error = task.exception?.message
                                            }
                                        }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(
                        onClick = { navController.navigate(Routes.SignUp.route) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("Don't have an account? Sign Up")
                    }
                }
            }
        }
    }
}
