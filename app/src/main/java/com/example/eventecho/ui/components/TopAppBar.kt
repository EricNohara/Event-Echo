package com.example.eventecho.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.example.eventecho.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                "EventEcho",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Profile") },
                    onClick = {
                        expanded = false
                        navController.navigate(Routes.EditProfile.route)
                    }
                )

                DropdownMenuItem(
                    text = { Text("Sign Out") },
                    onClick = {
                        expanded = false
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Routes.SignIn.route) {
                            popUpTo(0)
                        }
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}