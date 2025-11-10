package com.example.eventecho.ui.components

import com.google.android.gms.maps.model.LatLng

data class EventPin(
    val id: String,
    val location: LatLng,
    val title: String,
    val snippet: String
)