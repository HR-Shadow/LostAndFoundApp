package eu.tutorials.lostfoundapp.ui.matches

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.lostfoundapp.R
import eu.tutorials.lostfoundapp.model.MatchStatus
import eu.tutorials.lostfoundapp.model.MatchWithDetails
import eu.tutorials.lostfoundapp.util.rememberBase64ImageBitmap
import kotlin.math.roundToInt

@Composable
fun MatchCard(
    matchDetails: MatchWithDetails,
    isActionInProgress: Boolean,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onOpenChat: () -> Unit = {},
    onHide: () -> Unit = {},
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val match = matchDetails.match
    val scorePercent = (match.matchScore * 100).roundToInt()
    val status = MatchStatus.fromString(match.status)
    val userAlreadyConfirmed = if (matchDetails.isLostOwner) {
        match.lostUserConfirmed
    } else {
        match.foundUserConfirmed
    }

    // Outer Card Dark Glass Gradient
    val outerCardGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1B4B).copy(alpha = 0.70f),
            Color(0xFF0F172A).copy(alpha = 0.75f)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .border(
                width = 1.dp,
                color = Color(0xFF818CF8).copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .background(outerCardGradient)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Score Tag & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF312E81).copy(alpha = 0.6f))
                        .border(0.5.dp, Color(0xFFA5B4FC).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = "🎯 ", fontSize = 13.sp)
                    Text(
                        text = stringResource(R.string.match_score, scorePercent),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC7D2FE)
                    )
                }

                MatchStatusChip(status = status)
            }

            // USER PROFILE HEADER (Shows Opposite User's Name)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (matchDetails.isLostOwner) "Found by User" else "Lost by User",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = matchDetails.otherUserName.ifBlank { "Unknown User" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Lost Item Summary
            matchDetails.lostItem?.let { lost ->
                ItemSummaryCard(
                    title = stringResource(R.string.lost_item_label),
                    itemName = lost.itemName,
                    category = lost.category,
                    description = lost.description,
                    location = lost.locationLost,
                    identifyingDetails = lost.identifyingDetails,
                    imageUrl = lost.imageUrl,
                    onImageClick = onImageClick
                )
            }

            // Found Item Summary
            matchDetails.foundItem?.let { found ->
                ItemSummaryCard(
                    title = stringResource(R.string.found_item_label),
                    itemName = found.itemName,
                    category = found.category,
                    description = found.description,
                    location = found.locationFound,
                    identifyingDetails = found.identifyingDetails,
                    imageUrl = found.imageUrl,
                    onImageClick = onImageClick
                )
            }

            // --- PHONE NUMBER SECURITY / PRIVACY CHECK ---
            if (status == MatchStatus.CONFIRMED) {
                // UNLOCKED: Phone Number Shown + Direct Dial Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF065F46).copy(alpha = 0.5f))
                        .border(1.dp, Color(0xFF34D399).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Contact Number",
                                fontSize = 10.sp,
                                color = Color(0xFFA7F3D0)
                            )
                            Text(
                                text = matchDetails.otherUserPhoneNumber.ifBlank { "Not provided" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (matchDetails.otherUserPhoneNumber.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${matchDetails.otherUserPhoneNumber}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981))
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call User",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                // LOCKED: Security Privacy Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF334155).copy(alpha = 0.4f))
                        .border(0.5.dp, Color(0xFF64748B).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFFFACC15),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Phone Number: •••••••••• (Confirms required)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            // Interactive Actions Section
            when (status) {
                MatchStatus.PENDING -> {
                    if (userAlreadyConfirmed) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF3B0764).copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = Color(0xFFE9D5FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.waiting_other_user),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF3E8FF)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onConfirm,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                enabled = !isActionInProgress,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4F46E5)
                                )
                            ) {
                                if (isActionInProgress) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.ThumbUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (matchDetails.isLostOwner) {
                                            stringResource(R.string.this_is_mine)
                                        } else {
                                            stringResource(R.string.this_matches)
                                        },
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                enabled = !isActionInProgress,
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFF818CF8), Color(0xFFC084FC))
                                    )
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFE0E7FF)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.not_a_match),
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE0E7FF)
                                )
                            }
                        }
                    }
                }

                MatchStatus.CONFIRMED -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF065F46).copy(alpha = 0.45f))
                                .border(0.5.dp, Color(0xFF34D399).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF6EE7B7),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.match_confirmed_message),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFA7F3D0)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onOpenChat,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3B82F6)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.chat_now),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = onHide,
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFFF87171), Color(0xFFFB7185))
                                    )
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFFFCA5A5)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Delete",
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFCA5A5)
                                )
                            }
                        }
                    }
                }

                MatchStatus.REJECTED -> {
                    Text(
                        text = stringResource(R.string.match_rejected_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFCA5A5),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF881337).copy(alpha = 0.45f))
                            .padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchStatusChip(status: MatchStatus) {
    val (label, color, containerColor) = when (status) {
        MatchStatus.PENDING -> Triple(
            stringResource(R.string.status_pending),
            Color(0xFFFDE047),
            Color(0xFF713F12).copy(alpha = 0.5f)
        )
        MatchStatus.CONFIRMED -> Triple(
            stringResource(R.string.status_confirmed),
            Color(0xFF6EE7B7),
            Color(0xFF064E3B).copy(alpha = 0.6f)
        )
        MatchStatus.REJECTED -> Triple(
            stringResource(R.string.status_rejected),
            Color(0xFFFCA5A5),
            Color(0xFF881337).copy(alpha = 0.6f)
        )
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = color,
            containerColor = containerColor
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = color.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(50)
    )
}

@Composable
fun ItemSummaryCard(
    title: String,
    itemName: String,
    category: String,
    description: String,
    location: String,
    identifyingDetails: String,
    imageUrl: String?,
    onImageClick: (String) -> Unit = {}
) {
    val imageBitmap = rememberBase64ImageBitmap(imageUrl ?: "")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.6f))
            .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF818CF8)
        )

        Text(
            text = itemName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        if (description.isNotBlank()) {
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFFCBD5E1)
            )
        }

        if (location.isNotBlank()) {
            Text(
                text = "📍 $location",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        if (imageBitmap != null && !imageUrl.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF020617))
                    .clickable { onImageClick(imageUrl) }
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = itemName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tap to enlarge",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}