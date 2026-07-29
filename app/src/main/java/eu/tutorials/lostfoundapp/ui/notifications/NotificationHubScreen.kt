package eu.tutorials.lostfoundapp.ui.notifications

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import eu.tutorials.lostfoundapp.R
import eu.tutorials.lostfoundapp.model.MatchWithDetails
import eu.tutorials.lostfoundapp.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHubScreen(
    viewModel: NotificationViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onViewMatch: (matchId: String) -> Unit = {}
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    NotificationHubContent(
        notifications = notifications,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onNavigateBack = onNavigateBack,
        onViewMatch = onViewMatch
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationHubContent(
    notifications: List<MatchWithDetails>,
    isLoading: Boolean,
    errorMessage: String?,
    onNavigateBack: () -> Unit,
    onViewMatch: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Activity & Matches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A).copy(alpha = 0.85f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- 1. BACKGROUND IMAGE RESOURCE ---
            Image(
                painter = painterResource(id = R.drawable.notification),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // --- 2. DARK OVERLAY FOR CONTRAST ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0B0F19).copy(alpha = 0.78f))
            )

            // --- 3. MAIN CONTENT STATES ---
            when {
                isLoading -> {
                    NotificationShimmerList()
                }
                errorMessage != null -> {
                    NotificationErrorState(
                        errorMessage = errorMessage
                    )
                }
                notifications.isEmpty() -> {
                    NotificationEmptyState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = notifications,
                            key = { it.match.matchId }
                        ) { matchDetails ->
                            MatchNotificationCard(
                                matchDetails = matchDetails,
                                onViewMatch = { onViewMatch(matchDetails.match.matchId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchNotificationCard(
    matchDetails: MatchWithDetails,
    onViewMatch: () -> Unit
) {
    val status = matchDetails.match.status
    val isConfirmed = status.equals("CONFIRMED", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF6366F1).copy(alpha = 0.3f),
                spotColor = Color(0xFF6366F1).copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.85f))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF6366F1).copy(alpha = 0.6f),
                        Color(0xFF38BDF8).copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Potential Item Match Found!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                // Status Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isConfirmed) Color(0xFF10B981).copy(alpha = 0.2f)
                            else Color(0xFFF59E0B).copy(alpha = 0.2f)
                        )
                        .border(
                            1.dp,
                            if (isConfirmed) Color(0xFF10B981) else Color(0xFFF59E0B),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConfirmed) Color(0xFF34D399) else Color(0xFFFBBF24)
                    )
                }
            }

            OverlappingMatchImages(
                imageUrl = matchDetails.lostItem?.imageUrl,
                matchedImageUrl = matchDetails.foundItem?.imageUrl
            )

            if (isConfirmed) {
                Button(
                    onClick = onViewMatch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1),
                                        Color(0xFF38BDF8)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Open Chat",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "⏳ Waiting for both users to confirm the match...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun OverlappingMatchImages(
    imageUrl: String?,
    matchedImageUrl: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        NotificationAvatar(
            imageUrl = imageUrl,
            size = 72.dp,
            modifier = Modifier.offset(x = 0.dp),
            borderColor = Color(0xFF6366F1)
        )
        NotificationAvatar(
            imageUrl = matchedImageUrl,
            size = 72.dp,
            modifier = Modifier.offset(x = 52.dp),
            borderColor = Color(0xFF38BDF8)
        )
    }
}

@Composable
private fun NotificationAvatar(
    imageUrl: String?,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(2.5.dp, borderColor, CircleShape)
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun NotificationShimmerList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) {
            ShimmerNotificationCard()
        }
    }
}

@Composable
private fun ShimmerNotificationCard() {
    val shimmerColors = listOf(
        Color(0xFF1E293B).copy(alpha = 0.6f),
        Color(0xFF334155).copy(alpha = 0.4f),
        Color(0xFF1E293B).copy(alpha = 0.6f)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 300f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
private fun NotificationEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B).copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF38BDF8)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "All caught up!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "New match activity will appear here in real time.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NotificationErrorState(
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = Color(0xFFEF4444)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error Loading Notifications",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEF4444)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCBD5E1),
            textAlign = TextAlign.Center
        )
    }
}