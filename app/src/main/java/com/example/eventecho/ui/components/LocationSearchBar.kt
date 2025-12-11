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
import androidx.compose.foundation.lazy.items   // 👈 IMPORTANT!
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.eventecho.data.places.PlaceSuggestion
import com.example.eventecho.data.places.PlacesRepository
import kotlinx.coroutines.launch

// distanceMiles(...) is already defined in MapSearchBar.kt in the same package,
// so we just reuse it here.

@Composable
fun LocationSearchBar(
    label: String = "Event Location",
    userLat: Double? = null,    // optional (for miles calculation)
    userLng: Double? = null,
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    onLocationSelected: (String, Double, Double) -> Unit,
    dropdownMaxHeight: Dp = 180.dp,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val repo = remember { PlacesRepository(context) }

    var suggestions by remember { mutableStateOf(listOf<PlaceSuggestion>()) }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        // --- Search TextField (same feel as MapSearchBar) ---
        OutlinedTextField(
            value = locationName,
            onValueChange = { newValue ->
                onLocationNameChange(newValue)

                scope.launch {
                    suggestions =
                        if (newValue.isBlank()) emptyList()
                        else repo.autocomplete(newValue)
                }
            },
            label = { Text(label) },
            singleLine = true,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = if (isFocused)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                suggestions = emptyList()
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            enabled = enabled
        )

        // ---- Suggestions Dropdown (same style as MapSearchBar) ----
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

                        val milesText =
                            if (userLat != null && userLng != null)
                                String.format(
                                    "%.1f miles away",
                                    distanceMiles(userLat, userLng, suggestion.lat, suggestion.lng)
                                )
                            else null

                        Column(modifier = Modifier.fillMaxWidth()) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onLocationNameChange(suggestion.description)
                                        onLocationSelected(
                                            suggestion.description,
                                            suggestion.lat,
                                            suggestion.lng
                                        )
                                        suggestions = emptyList()
                                        focusManager.clearFocus()
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 12.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        suggestion.description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    milesText?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
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
