package eu.tutorials.lostfoundapp.ui.matches

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.tutorials.lostfoundapp.R
import eu.tutorials.lostfoundapp.model.MatchStatus
import eu.tutorials.lostfoundapp.ui.components.Lottie3DBackground
import eu.tutorials.lostfoundapp.util.rememberBase64ImageBitmap
import eu.tutorials.lostfoundapp.viewmodel.MatchesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (matchId: String) -> Unit,
    viewModel: MatchesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // State for full-screen image zoom modal
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }

    // States for Delete/Reject Confirmation Dialog
    var matchToDeleteId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Outer Box: 3D Lottie Background Setup
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Dynamic 3D Lottie Background
        Lottie3DBackground()

        // 2. Dark Overlay Fade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        // 3. Screen Main Content
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.possible_matches),
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
                        containerColor = Color.Transparent
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            // Filter out REJECTED matches entirely so they vanish immediately
            val activeMatches = remember(state.matches) {
                state.matches.filter { it.match.status != MatchStatus.REJECTED.value }
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(44.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                activeMatches.isEmpty() -> {
                    EmptyMatchesState(modifier = Modifier.padding(padding))
                }

                else -> {
                    val pendingMatches = activeMatches.filter {
                        it.match.status == MatchStatus.PENDING.value
                    }
                    val otherMatches = activeMatches.filter {
                        it.match.status != MatchStatus.PENDING.value
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (pendingMatches.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.pending_matches_header),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(pendingMatches, key = { it.match.matchId }) { match ->
                                MatchCard(
                                    matchDetails = match,
                                    isActionInProgress = state.actionInProgress == match.match.matchId,
                                    onConfirm = { viewModel.confirmMatch(match.match.matchId) },
                                    onReject = {
                                        // Trigger Dialog instead of direct reject
                                        matchToDeleteId = match.match.matchId
                                        showDeleteDialog = true
                                    },
                                    onImageClick = { base64 ->
                                        selectedImageBase64 = base64
                                    }
                                )
                            }
                        }

                        if (otherMatches.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.past_matches_header),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            items(otherMatches, key = { it.match.matchId }) { match ->
                                MatchCard(
                                    matchDetails = match,
                                    isActionInProgress = state.actionInProgress == match.match.matchId,
                                    onConfirm = {},
                                    onReject = {},
                                    onOpenChat = {
                                        onNavigateToChat(match.match.matchId)
                                    },
                                    onHide = {
                                        // Trigger Dialog instead of direct hide
                                        matchToDeleteId = match.match.matchId
                                        showDeleteDialog = true
                                    },
                                    onImageClick = { base64 ->
                                        selectedImageBase64 = base64
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full Screen Image Zoom Dialog
        selectedImageBase64?.let { base64Image ->
            FullScreenImageModal(
                imageBase64 = base64Image,
                onDismiss = { selectedImageBase64 = null }
            )
        }

        // Confirmation Dialog for Reject / Hide / Delete actions
        if (showDeleteDialog && matchToDeleteId != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    matchToDeleteId = null
                },
                title = { Text(text = "Confirm Removal") },
                text = { Text(text = "Are you sure you want to remove or reject this match? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            matchToDeleteId?.let { id ->
                                // Call rejection/hiding based on current state logic safely
                                viewModel.rejectMatch(id)
                            }
                            showDeleteDialog = false
                            matchToDeleteId = null
                        }
                    ) {
                        Text("Confirm", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            matchToDeleteId = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun EmptyMatchesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = stringResource(R.string.no_matches_yet),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(R.string.no_matches_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Dialog component to display a high-resolution enlarged image on click
 */
@Composable
fun FullScreenImageModal(
    imageBase64: String,
    onDismiss: () -> Unit
) {
    val imageBitmap = rememberBase64ImageBitmap(imageBase64)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Full Screen Matched Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.82f)
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Top Right Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 20.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}