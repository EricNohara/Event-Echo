package com.example.eventecho.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import com.example.eventecho.data.places.PlaceSuggestion
import com.example.eventecho.data.places.PlacesRepository

@Composable
fun LocationSelector(
    label: String = "Event Location",
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    onLocationSelected: (String, Double, Double) -> Unit,
    dropdownMaxHeight: Dp = 150.dp
) {
    val context = LocalContext.current
    val repo = remember { PlacesRepository(context) }

    var suggestions by remember { mutableStateOf(emptyList<PlaceSuggestion>()) }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Correct focus tracking for dropdown visibility
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(modifier = Modifier.fillMaxWidth()) {

        // --- TEXT FIELD ---
        SimpleTextField(
            value = locationName,
            onValueChange = { newValue ->
                onLocationNameChange(newValue)

                scope.launch {
                    suggestions =
                        if (newValue.isBlank()) emptyList()
                        else repo.autocomplete(newValue)
                }
            },
            label = label,
            modifier = Modifier.fillMaxWidth(),
            interactionSource = interactionSource
        )

        // --- DROPDOWN ---
        if (isFocused && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(20f),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = dropdownMaxHeight)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    items(suggestions) { suggestion ->

                        Column(modifier = Modifier.fillMaxWidth()) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .clickable {
                                        onLocationNameChange(suggestion.description)
                                        onLocationSelected(
                                            suggestion.description,
                                            suggestion.lat,
                                            suggestion.lng
                                        )
                                        suggestions = emptyList()
                                        focusManager.clearFocus()
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 12.dp)
                                )

                                Text(
                                    suggestion.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
