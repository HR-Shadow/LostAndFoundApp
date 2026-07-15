package eu.tutorials.lostfoundapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.lostfoundapp.model.MatchStatus
import eu.tutorials.lostfoundapp.model.MatchWithDetails
import eu.tutorials.lostfoundapp.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchesUiState(
    val isLoading: Boolean = true,
    val matches: List<MatchWithDetails> = emptyList(),
    val pendingCount: Int = 0,
    val actionInProgress: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class MatchesViewModel(
    private val matchRepository: MatchRepository = MatchRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    init {
        observeMatches()
    }

    private fun observeMatches() {
        viewModelScope.launch {
            matchRepository.observeUserMatches()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load matches"
                        )
                    }
                }
                .collect { matches ->
                    val details = matchRepository.loadMatchDetails(matches)
                    val pending = details.count { it.match.status == MatchStatus.PENDING.value }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            matches = details,
                            pendingCount = pending,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun confirmMatch(matchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgress = matchId, errorMessage = null) }
            matchRepository.confirmMatch(matchId)
                .onSuccess { updated ->
                    val message = if (updated.status == MatchStatus.CONFIRMED.value) {
                        "Match confirmed by both users!"
                    } else {
                        "Your confirmation was recorded. Waiting for the other user."
                    }
                    _uiState.update {
                        it.copy(
                            actionInProgress = null,
                            successMessage = message
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            actionInProgress = null,
                            errorMessage = error.message ?: "Failed to confirm match"
                        )
                    }
                }
        }
    }

    fun rejectMatch(matchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgress = matchId, errorMessage = null) }
            matchRepository.rejectMatch(matchId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            actionInProgress = null,
                            successMessage = "Match rejected."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            actionInProgress = null,
                            errorMessage = error.message ?: "Failed to reject match"
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
