package com.example.eventecho.ui.dataclass

import com.google.android.gms.maps.model.LatLng

data class SearchCircle(
    val center: LatLng,
    val radiusMeters: Double
)