package eu.tutorials.lostfoundapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.lostfoundapp.model.ItemCategory
import eu.tutorials.lostfoundapp.repository.ItemRepository
import eu.tutorials.lostfoundapp.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class ReportItemUiState(
    val itemName: String = "",
    val category: ItemCategory = ItemCategory.OTHER,
    val description: String = "",
    val eventDate: LocalDate = LocalDate.now(),
    val location: String = "",
    val identifyingDetails: String = "",
    val imageUri: Uri? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

abstract class BaseReportViewModel(
    application: Application,
    protected val itemRepository: ItemRepository = ItemRepository()
) : AndroidViewModel(application) {

    protected abstract val _uiState: MutableStateFlow<ReportItemUiState>
    abstract val uiState: StateFlow<ReportItemUiState>

    fun updateItemName(value: String) = updateState { copy(itemName = value) }
    fun updateCategory(value: ItemCategory) = updateState { copy(category = value) }
    fun updateDescription(value: String) = updateState { copy(description = value) }
    fun updateEventDate(value: LocalDate) = updateState { copy(eventDate = value) }
    fun updateLocation(value: String) = updateState { copy(location = value) }
    fun updateIdentifyingDetails(value: String) = updateState { copy(identifyingDetails = value) }
    fun updateImageUri(uri: Uri?) = updateState { copy(imageUri = uri) }

    fun clearError() = updateState { copy(errorMessage = null) }

    fun resetSuccess() = updateState { copy(isSuccess = false) }

    protected fun updateState(transform: ReportItemUiState.() -> ReportItemUiState) {
        _uiState.update(transform)
    }

    protected fun validateForm(): String? {
        val state = _uiState.value
        return when {
            state.itemName.isBlank() -> "Item name is required"
            state.description.isBlank() -> "Description is required"
            state.location.isBlank() -> "Location is required"
            state.eventDate.isAfter(LocalDate.now()) -> "Date cannot be in the future"
            else -> null
        }
    }

    protected fun eventDateToMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

class ReportLostViewModel @JvmOverloads constructor(
    application: Application,
    itemRepository: ItemRepository = ItemRepository(),
    private val matchRepository: MatchRepository = MatchRepository()
) : BaseReportViewModel(application, itemRepository) {

    override val _uiState = MutableStateFlow(ReportItemUiState())
    override val uiState: StateFlow<ReportItemUiState> = _uiState.asStateFlow()

    fun submitReport() {
        val validationError = validateForm()
        if (validationError != null) {
            updateState { copy(errorMessage = validationError) }
            return
        }
        val state = _uiState.value
        viewModelScope.launch {
            updateState { copy(isSubmitting = true, errorMessage = null) }
            itemRepository.reportLostItem(
                context = getApplication(),
                itemName = state.itemName,
                category = state.category.name,
                description = state.description,
                dateLost = eventDateToMillis(state.eventDate),
                locationLost = state.location,
                identifyingDetails = state.identifyingDetails,
                imageUri = state.imageUri
            ).onSuccess { item ->
                matchRepository.runMatchingForLostItem(item)
                updateState {
                    ReportItemUiState(isSuccess = true)
                }
            }.onFailure { error ->
                updateState {
                    copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "Failed to report lost item"
                    )
                }
            }
        }
    }
}

class ReportFoundViewModel @JvmOverloads constructor(
    application: Application,
    itemRepository: ItemRepository = ItemRepository(),
    private val matchRepository: MatchRepository = MatchRepository()
) : BaseReportViewModel(application, itemRepository) {

    override val _uiState = MutableStateFlow(ReportItemUiState())
    override val uiState: StateFlow<ReportItemUiState> = _uiState.asStateFlow()

    fun submitReport() {
        val validationError = validateForm()
        if (validationError != null) {
            updateState { copy(errorMessage = validationError) }
            return
        }
        val state = _uiState.value
        viewModelScope.launch {
            updateState { copy(isSubmitting = true, errorMessage = null) }
            itemRepository.reportFoundItem(
                context = getApplication(),
                itemName = state.itemName,
                category = state.category.name,
                description = state.description,
                dateFound = eventDateToMillis(state.eventDate),
                locationFound = state.location,
                identifyingDetails = state.identifyingDetails,
                imageUri = state.imageUri
            ).onSuccess { item ->
                matchRepository.runMatchingForFoundItem(item)
                updateState {
                    ReportItemUiState(isSuccess = true)
                }
            }.onFailure { error ->
                updateState {
                    copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "Failed to report found item"
                    )
                }
            }
        }
    }
}