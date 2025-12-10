package com.example.eventecho.ui.components

import com.example.eventecho.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun CreatorInfoSection(
    source: String,
    creatorUsername: String?,
    creatorProfileUrl: String?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {

        if (source == "user") {

            // Fix: handle empty URL properly
            val avatarModel = if (creatorProfileUrl.isNullOrBlank())
                R.drawable.default_avatar
            else
                creatorProfileUrl

            AsyncImage(
                model = avatarModel,
                contentDescription = "Creator profile",
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(10.dp))

            Column {
                Text(
                    "Created by",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    creatorUsername ?: "Unknown user",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

        } else {

            // Ticketmaster image with rounded corners
            AsyncImage(
                model = R.drawable.ticketmaster_logo,
                contentDescription = "Ticketmaster Logo",
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.width(10.dp))

            Column {
                Text(
                    "Created by",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Ticketmaster",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
