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

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sign In", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation() // 🔥 hides password
        )

        Spacer(Modifier.height(20.dp))

        Button(onClick = {
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
        }) {
            Text("Sign In")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = {
            navController.navigate(Routes.SignUp.route)
        }) {
            Text("Don't have an account? Sign Up")
        }

        error?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
