package com.example.eventecho.ui.dataclass

import com.google.android.gms.maps.model.LatLng

data class EventPin(
    val id: String,
    val title: String,
    val snippet: String,
    val location: LatLng
)