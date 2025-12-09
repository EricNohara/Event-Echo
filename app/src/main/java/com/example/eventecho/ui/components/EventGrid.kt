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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.eventecho.R
import com.example.eventecho.ui.dataclass.Event
import com.example.eventecho.utils.timeAgo
import com.example.eventecho.utils.timeAgoOrAhead
import com.example.eventecho.ui.theme.EventEchoTitleFont


@Composable
fun EventGrid(
    navController: NavController,
    events: List<Event>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
    ) {
        items(events.chunked(2)) { rowEvents ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left card
                EventCardSmall(
                    event = rowEvents[0],
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        navController.navigate("event_detail/${rowEvents[0].id}")
                    }
                )

                // Right card (if exists)
                if (rowEvents.size > 1) {
                    EventCardSmall(
                        event = rowEvents[1],
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
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
    event: Event,
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
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Image
            val imageModel: Any =
                if (event.imageUrl.isNullOrBlank()) {
                    R.drawable.default_image
                } else {
                    event.imageUrl
                }

            AsyncImage(
                model = imageModel,
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Title
            Text(
                text = event.title,
                fontWeight = FontWeight.Bold,
                fontFamily = EventEchoTitleFont,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp, top = 4.dp)
            )

            // Date
            Text(
                text = timeAgoOrAhead(event.date),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

// non scrollable version for profile page
@Composable
fun EventGridNonScrollable(
    navController: NavController,
    events: List<Event>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        events.chunked(2).forEach { rowEvents ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                EventCardSmall(
                    event = rowEvents[0],
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        navController.navigate("event_detail/${rowEvents[0].id}")
                    }
                )

                if (rowEvents.size > 1) {
                    EventCardSmall(
                        event = rowEvents[1],
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
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
