package com.example.eventecho.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(

    // Global default body text uses base font
    bodyLarge = TextStyle(
        fontFamily = EventEchoBaseFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = EventEchoBaseFont,
        fontSize = 14.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = EventEchoTitleFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),

    headlineLarge = TextStyle(
        fontFamily = EventEchoTitleFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),

    titleLarge = TextStyle(
        fontFamily = EventEchoTitleFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    )
)
