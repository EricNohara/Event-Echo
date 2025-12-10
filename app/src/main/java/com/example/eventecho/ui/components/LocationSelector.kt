package com.example.eventecho.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun LocationSelector(
    label: String = "Event Location",
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    onLocationSelected: (String, Double, Double) -> Unit
) {
    val context = LocalContext.current
    val repo = remember { com.example.eventecho.data.places.PlacesRepository(context) }

    var suggestions by remember { mutableStateOf(listOf<com.example.eventecho.data.places.PlaceSuggestion>()) }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(locationName) {
        if (locationName.isNotBlank()) {
            suggestions = repo.autocomplete(locationName)
        } else {
            suggestions = emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = locationName,
            onValueChange = {
                onLocationNameChange(it)
                isFocused = true
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done)
        )

        if (isFocused && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {

                Column {
                    suggestions.forEach { suggestion ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLocationSelected(
                                        suggestion.description,
                                        suggestion.lat,
                                        suggestion.lng
                                    )
                                    isFocused = false
                                    suggestions = emptyList()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(suggestion.description)
                        }
                    }
                }
            }
        }
    }
}
