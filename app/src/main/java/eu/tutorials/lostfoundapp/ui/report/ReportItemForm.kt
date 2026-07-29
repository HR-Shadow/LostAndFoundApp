package eu.tutorials.lostfoundapp.ui.report

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFF1E293B),
        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.8f),
        disabledContainerColor = Color(0xFF1E293B).copy(alpha = 0.4f),
        focusedBorderColor = Color(0xFF38BDF8),
        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
        focusedLabelColor = Color(0xFF38BDF8),
        unfocusedLabelColor = Color(0xFF94A3B8),
        focusedLeadingIconColor = Color(0xFF38BDF8),
        unfocusedLeadingIconColor = Color(0xFF94A3B8),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White
    )

    val fieldShape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding() // Keyboard height safety padding
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Item Name Field
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
            singleLine = true,
            shape = fieldShape,
            colors = textFieldColors
        )

        // Category Selection Dropdown
        CategoryDropdown(
            selectedCategory = state.category,
            onCategorySelected = { onCategoryChange(it) },
            label = stringResource(R.string.category)
        )

        // Description Field
        OutlinedTextField(
            value = state.description,
            onValueChange = { onDescriptionChange(it) },
            label = { Text(stringResource(R.string.description)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = fieldShape,
            colors = textFieldColors
        )

        // Date Picker Component
        DatePickerField(
            selectedDate = state.eventDate,
            onDateSelected = { onDateChange(it) },
            label = dateLabel
        )

        // Location Field
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
            singleLine = true,
            shape = fieldShape,
            colors = textFieldColors
        )

        Text(
            text = stringResource(R.string.location_map_hint),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(start = 4.dp)
        )

        // Identifying Details Field
        OutlinedTextField(
            value = state.identifyingDetails,
            onValueChange = { onIdentifyingDetailsChange(it) },
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
            placeholder = {
                Text(
                    text = stringResource(R.string.identifying_details_hint),
                    color = Color(0xFF64748B)
                )
            },
            shape = fieldShape,
            colors = textFieldColors
        )

        // Image Picker Card
        ImagePickerCard(
            imageUri = state.imageUri,
            onImageSelected = { onImageSelected(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Glowing Gradient Submit Button
        Button(
            onClick = { onSubmit() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !state.isSubmitting,
            shape = fieldShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (!state.isSubmitting) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF38BDF8)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF334155),
                                    Color(0xFF334155)
                                )
                            )
                        },
                        shape = fieldShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = submitLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}