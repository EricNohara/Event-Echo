package com.example.eventecho.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.eventecho.ui.dataclass.Event
import com.example.eventecho.R

@Composable
fun EventList(
    navController: NavController,
    events: List<Event>
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        items(events) { event ->
            EventCard(event = event) {
                navController.navigate("event_detail/${event.id}")
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {

        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // PICK IMAGE OR FALLBACK
            val imageModel: Any =
                if (event.imageUrl.isNullOrBlank()) {
                    R.drawable.default_image   // <-- your fallback drawable
                } else {
                    event.imageUrl
                }

            AsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .aspectRatio(1f)
                    .padding(end = 12.dp)
                    .clip(RoundedCornerShape(10.dp)),
                fallback = painterResource(R.drawable.default_image),
                error = painterResource(R.drawable.default_image),
                placeholder = painterResource(R.drawable.default_image)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.location,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = event.date,
                    color = Color.Gray
                )
            }
        }
    }
}
