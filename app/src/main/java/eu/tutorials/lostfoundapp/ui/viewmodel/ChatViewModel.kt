package eu.tutorials.lostfoundapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import eu.tutorials.lostfoundapp.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel(
    private val matchId: String,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val messages: StateFlow<List<ChatMessage>> = _uiState
        .map { it.messages }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var messagesListener: ListenerRegistration? = null

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    init {
        startListening()
    }

    private fun messagesCollection() =
        firestore.collection(MATCH_REQUESTS).document(matchId).collection(MESSAGES)

    private fun startListening() {
        if (matchId.isBlank()) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "Invalid chat session")
            }
            return
        }

        messagesListener?.remove()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        messagesListener = messagesCollection()
            .orderBy(TIMESTAMP_FIELD, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load messages"
                        )
                    }
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { document ->
                    document.data?.let { data ->
                        ChatMessage.fromDocument(document.id, data)
                    }
                }.orEmpty()

                _uiState.update {
                    it.copy(
                        messages = messages,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val senderId = currentUserId
        if (senderId == null) {
            _uiState.update { it.copy(errorMessage = "You must be signed in to send messages") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            try {
                val messageRef = messagesCollection().document()
                val message = mapOf(
                    MESSAGE_ID_FIELD to messageRef.id,
                    SENDER_ID_FIELD to senderId,
                    TEXT_FIELD to trimmed,
                    TIMESTAMP_FIELD to FieldValue.serverTimestamp()
                )
                messageRef.set(message).await()
                _uiState.update { it.copy(isSending = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = e.message ?: "Failed to send message"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        messagesListener?.remove()
        messagesListener = null
        super.onCleared()
    }

    companion object {
        private const val MATCH_REQUESTS = "match_requests"
        private const val MESSAGES = "messages"
        private const val MESSAGE_ID_FIELD = "messageId"
        private const val SENDER_ID_FIELD = "senderId"
        private const val TEXT_FIELD = "text"
        private const val TIMESTAMP_FIELD = "timestamp"

        fun provideFactory(matchId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                        return ChatViewModel(matchId = matchId) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }
}
