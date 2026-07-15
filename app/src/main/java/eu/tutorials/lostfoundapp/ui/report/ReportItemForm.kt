package eu.tutorials.lostfoundapp.ui.report

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.tutorials.lostfoundapp.R
import eu.tutorials.lostfoundapp.model.ItemCategory
import eu.tutorials.lostfoundapp.ui.components.CategoryDropdown
import eu.tutorials.lostfoundapp.ui.components.DatePickerField
import eu.tutorials.lostfoundapp.ui.components.ImagePickerCard
import eu.tutorials.lostfoundapp.viewmodel.ReportItemUiState
import java.time.LocalDate

@Composable
fun ReportItemForm(
    state: ReportItemUiState,
    dateLabel: String,
    locationLabel: String,
    submitLabel: String,
    onItemNameChange: (String) -> Unit,
    onCategoryChange: (ItemCategory) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onLocationChange: (String) -> Unit,
    onIdentifyingDetailsChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding() // Keyboard height safety padding
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // FIXED: Replaced custom AuthTextField with highly stable OutlinedTextField
        OutlinedTextField(
            value = state.itemName,
            onValueChange = { onItemNameChange(it) },
            label = { Text(stringResource(R.string.item_name)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        CategoryDropdown(
            selectedCategory = state.category,
            onCategorySelected = { onCategoryChange(it) },
            label = stringResource(R.string.category)
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = { onDescriptionChange(it) }, // Explicit safe lambda
            label = { Text(stringResource(R.string.description)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        DatePickerField(
            selectedDate = state.eventDate,
            onDateSelected = { onDateChange(it) },
            label = dateLabel
        )

        // FIXED: Replaced with standard OutlinedTextField for consistent focus tree
        OutlinedTextField(
            value = state.location,
            onValueChange = { onLocationChange(it) },
            label = { Text(locationLabel) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(
            text = stringResource(R.string.location_map_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = state.identifyingDetails,
            onValueChange = { onIdentifyingDetailsChange(it) }, // Explicit safe lambda
            label = { Text(stringResource(R.string.identifying_details)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            placeholder = { Text(stringResource(R.string.identifying_details_hint)) }
        )

        ImagePickerCard(
            imageUri = state.imageUri,
            onImageSelected = { onImageSelected(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onSubmit() }, // Explicit safe lambda trigger
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !state.isSubmitting
        ) {
            Text(
                text = if (state.isSubmitting) stringResource(R.string.submitting) else submitLabel,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}