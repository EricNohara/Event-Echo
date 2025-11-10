package com.example.eventecho.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun MemoryWallScreen(navController: NavController, eventId: String) {
    Button(onClick = {
        navController.popBackStack()
    }) {
        Text("Back")
    }
}