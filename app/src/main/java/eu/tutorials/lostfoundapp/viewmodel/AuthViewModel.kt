package eu.tutorials.lostfoundapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.lostfoundapp.model.User
import eu.tutorials.lostfoundapp.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authStateFlow().collect { firebaseUser ->
                if (firebaseUser == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            currentUser = null
                        )
                    }
                } else {
                    loadUserProfile(firebaseUser.uid)
                }
            }
        }
    }

    private fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.getUserProfile(userId)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = user,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = User(userId = userId),
                            errorMessage = error.message
                        )
                    }
                }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password are required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            authRepository.signIn(email.trim(), password)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isAuthenticated = true,
                            currentUser = user,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "Sign in failed"
                        )
                    }
                }
        }
    }

    fun signUp(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        when {
            name.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Name is required") }
                return
            }
            email.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Email is required") }
                return
            }
            phone.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Phone number is required") }
                return
            }
            password.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
                return
            }
            password != confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Passwords do not match") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            authRepository.signUp(name, email, phone, password)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isAuthenticated = true,
                            currentUser = user,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "Sign up failed"
                        )
                    }
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.update {
            AuthUiState(isLoading = false, isAuthenticated = false)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
