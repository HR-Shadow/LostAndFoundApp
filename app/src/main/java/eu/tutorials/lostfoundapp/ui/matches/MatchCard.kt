package eu.tutorials.lostfoundapp.ui.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.tutorials.lostfoundapp.R
import eu.tutorials.lostfoundapp.model.MatchStatus
import eu.tutorials.lostfoundapp.model.MatchWithDetails
import eu.tutorials.lostfoundapp.ui.components.ItemSummaryCard
import kotlin.math.roundToInt

@Composable
fun MatchCard(
    matchDetails: MatchWithDetails,
    isActionInProgress: Boolean,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onOpenChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val match = matchDetails.match
    val scorePercent = (match.matchScore * 100).roundToInt()
    val status = MatchStatus.fromString(match.status)
    val userAlreadyConfirmed = if (matchDetails.isLostOwner) {
        match.lostUserConfirmed
    } else {
        match.foundUserConfirmed
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.match_score, scorePercent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                MatchStatusChip(status = status)
            }

            matchDetails.lostItem?.let { lost ->
                ItemSummaryCard(
                    title = stringResource(R.string.lost_item_label),
                    itemName = lost.itemName,
                    category = lost.category,
                    description = lost.description,
                    location = lost.locationLost,
                    identifyingDetails = lost.identifyingDetails,
                    imageUrl = lost.imageUrl
                )
            }

            matchDetails.foundItem?.let { found ->
                ItemSummaryCard(
                    title = stringResource(R.string.found_item_label),
                    itemName = found.itemName,
                    category = found.category,
                    description = found.description,
                    location = found.locationFound,
                    identifyingDetails = found.identifyingDetails,
                    imageUrl = found.imageUrl
                )
            }

            when (status) {
                MatchStatus.PENDING -> {
                    if (userAlreadyConfirmed) {
                        Text(
                            text = stringResource(R.string.waiting_other_user),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onConfirm,
                                modifier = Modifier.weight(1f),
                                enabled = !isActionInProgress
                            ) {
                                if (isActionInProgress) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .height(20.dp)
                                            .width(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        if (matchDetails.isLostOwner) {
                                            stringResource(R.string.this_is_mine)
                                        } else {
                                            stringResource(R.string.this_matches)
                                        }
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.weight(1f),
                                enabled = !isActionInProgress
                            ) {
                                Text(stringResource(R.string.not_a_match))
                            }
                        }
                    }
                }
                MatchStatus.CONFIRMED -> {
                    Text(
                        text = stringResource(R.string.match_confirmed_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = onOpenChat,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.chat_now),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                MatchStatus.REJECTED -> {
                    Text(
                        text = stringResource(R.string.match_rejected_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchStatusChip(status: MatchStatus) {
    val (label, color) = when (status) {
        MatchStatus.PENDING -> stringResource(R.string.status_pending) to MaterialTheme.colorScheme.tertiary
        MatchStatus.CONFIRMED -> stringResource(R.string.status_confirmed) to MaterialTheme.colorScheme.primary
        MatchStatus.REJECTED -> stringResource(R.string.status_rejected) to MaterialTheme.colorScheme.error
    }
    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = color
        )
    )
}
