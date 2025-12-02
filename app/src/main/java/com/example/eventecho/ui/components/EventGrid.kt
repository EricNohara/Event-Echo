package com.example.eventecho.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.eventecho.data.api.ticketmaster.TicketmasterEvent

@Composable
fun EventGrid(
    navController: NavController,
    events: List<TicketmasterEvent>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        items(events.chunked(2)) { rowEvents ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left card
                EventCardSmall(
                    event = rowEvents[0],
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = {
                        navController.navigate("event_detail/${rowEvents[0].id}")
                    }
                )

                // Right card
                if (rowEvents.size > 1) {
                    EventCardSmall(
                        event = rowEvents[1],
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = {
                            navController.navigate("event_detail/${rowEvents[1].id}")
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun EventCardSmall(
    event: TicketmasterEvent,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Event name
            Column {
                Text(
                    text = event.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(Modifier.height(6.dp))
                // Venue
                event._embedded?.venues?.firstOrNull()?.let { venue ->
                    Text(
                        text = venue.name ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            // Bottom-aligned date row
            Text(
                text = formatDate(event.dates.start.localDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

fun formatDate(input: String): String {
    return try {
        // Attempts to convert YYYY-MM-DD to MM/DD/YYYY
        val parts = input.split("-")
        if (parts.size != 3) return input

        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, month - 1) // Calendar months are 0-based
            set(java.util.Calendar.DAY_OF_MONTH, day)
        }

        val formatter = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.US)
        formatter.format(calendar.time)

    } catch (e: Exception) {
        input // fallback
    }
}