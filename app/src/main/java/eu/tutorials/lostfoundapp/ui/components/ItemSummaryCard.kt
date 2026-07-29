package eu.tutorials.lostfoundapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.tutorials.lostfoundapp.model.ItemCategory

@Composable
fun ItemSummaryCard(
    title: String,
    itemName: String,
    category: String,
    description: String,
    location: String,
    identifyingDetails: String,
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    // Crash Prevention & Category parsing
    val categoryDisplayName = remember(category) {
        try {
            ItemCategory.fromString(category).displayName
        } catch (e: Exception) {
            category.ifBlank { "Other" }
        }
    }

    // Translucent Dark Blueish / Purple Glassmorphism Gradient
    val glassGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2C2250).copy(alpha = 0.55f), // Translucent Dark Purple
            Color(0xFF1E284A).copy(alpha = 0.55f)  // Translucent Dark Blue
        )
    )

    // Subdued Light Color for Labels
    val headerColor = Color(0xFFA5B4FC) // Light Indigo accent for titles

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFF6366F1).copy(alpha = 0.25f), // Soft glowing border
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // Gradient handle karega background
        )
    ) {
        Column(
            modifier = Modifier
                .background(glassGradient)
                .padding(14.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = headerColor,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = remember(imageUrl) { imageUrl },
                        contentDescription = itemName,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = itemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = categoryDisplayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    if (location.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📍 $location",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF93C5FD), // Soft Light Blue
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (identifyingDetails.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🔍 $identifyingDetails",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}