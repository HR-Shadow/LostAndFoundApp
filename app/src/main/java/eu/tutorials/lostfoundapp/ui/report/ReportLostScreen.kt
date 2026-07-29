package eu.tutorials.lostfoundapp.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.tutorials.lostfoundapp.R
import eu.tutorials.lostfoundapp.viewmodel.ReportLostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportLostScreen(
    onNavigateBack: () -> Unit,
    onReportSuccess: () -> Unit,
    viewModel: ReportLostViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(R.string.lost_report_success)

    // SAFE LAUNCHED EFFECT: Ek baar error aane par snackbar dikhakar clear karega bina recomposition loop ke
    val errorMessage = state.errorMessage
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }

    // SAFE LAUNCHED EFFECT: Success state handle karega
    val isSuccess = state.isSuccess
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            snackbarHostState.showSnackbar(successMessage)
            viewModel.resetSuccess()
            onReportSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.report_lost),
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0F19))
                .padding(padding)
        ) {
            ReportItemForm(
                state = state,
                dateLabel = stringResource(R.string.date_lost),
                locationLabel = stringResource(R.string.location_lost),
                submitLabel = stringResource(R.string.submit_lost_report),
                // Safe lambda wrappers to prevent accidental trigger on composition
                onItemNameChange = { viewModel.updateItemName(it) },
                onCategoryChange = { viewModel.updateCategory(it) },
                onDescriptionChange = { viewModel.updateDescription(it) },
                onDateChange = { viewModel.updateEventDate(it) },
                onLocationChange = { viewModel.updateLocation(it) },
                onIdentifyingDetailsChange = { viewModel.updateIdentifyingDetails(it) },
                onImageSelected = { viewModel.updateImageUri(it) },
                onSubmit = { viewModel.submitReport() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}