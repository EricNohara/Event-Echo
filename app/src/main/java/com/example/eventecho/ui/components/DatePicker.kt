package com.example.eventecho.ui.components

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePicker(
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }

    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val dateString = selectedDate.format(formatter)

    val context = LocalContext.current
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newDate = LocalDate.of(year, month + 1, dayOfMonth)
            selectedDate = newDate
            onDateSelected(newDate)
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    Button(onClick = { datePickerDialog.show() }) {
        Row (verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(dateString)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = "Calendar Icon"
            )
        }
    }
}
