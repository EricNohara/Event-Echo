package com.example.eventecho.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventecho.ui.dataclass.Memory

@Composable
fun MemoryCard(
    memory: Memory,
    hasUpvoted: Boolean,
    onToggleUpvote: () -> Unit,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {

            AsyncImage(
                model = memory.imageUrl,
                contentDescription = "Memory Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.height(8.dp))

            Text(text = memory.description)
            Text(text = "Posted by: ${memory.userId}")
            Text(text = "Upvotes: ${memory.upvoteCount}")

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onToggleUpvote,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (hasUpvoted) "Remove Upvote 👎" else "Upvote 👍")
            }
        }
    }
}
