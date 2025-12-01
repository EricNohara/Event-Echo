package com.example.eventecho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.example.eventecho.ui.navigation.Routes
import com.example.eventecho.data.firebase.UserRepository

@Composable
fun SignUpScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userRepo = remember { UserRepository() }

    val user = auth.currentUser

    // Redirect if already signed in
    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate(Routes.EventMapHome.route) {
                popUpTo(Routes.SignUp.route) { inclusive = true }
            }
        }
    }

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create an Account", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(20.dp))

        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(20.dp))

        // Create Account button
        Button(onClick = {
            // Validation before Firebase
            when {
                username.isBlank() -> error = "Username cannot be empty"
                email.isBlank() -> error = "Email cannot be empty"
                password.isBlank() -> error = "Password cannot be empty"
                password.length < 6 -> error = "Password must be at least 6 characters"
                else -> {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser!!.uid
                                error = null

                                // Create Firestore user profile
                                userRepo.createUser(
                                    uid = uid,
                                    email = email,
                                    username = username
                                )

                                // Navigate to the home screen
                                navController.navigate(Routes.EventMapHome.route) {
                                    popUpTo(Routes.SignUp.route) { inclusive = true }
                                }
                            } else {
                                error = task.exception?.message
                            }
                        }
                }
            }
        }) {
            Text("Create Account")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = {
            navController.navigate(Routes.SignIn.route)
        }) {
            Text("Already have an account? Sign In")
        }

        error?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
