package eu.tutorials.lostfoundapp.ui.aiassistant.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class AiMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiAssistantUiState(
    val messages: List<AiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AiAssistantViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    private val generativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3.6-flash")
    }

    init {
        addWelcomeMessage()
    }

    private fun addWelcomeMessage() {
        val welcomeMessage = AiMessage(
            text = "Hello! I'm your AI Assistant for the Lost & Found app. I can help you with:\n\n• How to report lost items\n• How to report found items\n• Understanding matches\n• General app usage\n\nWhat would you like to know?",
            isFromUser = false
        )
        _uiState.value = _uiState.value.copy(
            messages = listOf(welcomeMessage)
        )
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val userAiMessage = AiMessage(
            text = userMessage,
            isFromUser = true
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userAiMessage,
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                val prompt = """
                    You are a helpful AI assistant for a Lost & Found mobile app. The app helps users:
                    - Report lost items with details like category, description, location, date
                    - Report found items with similar details
                    - Get matched with potential owners/finders of items
                    - Chat with matched users
                    - View notifications for matches
                    
                    Keep your responses concise, friendly, and helpful. Focus on app-related questions.
                    If asked about something unrelated to the app, politely redirect to app-related topics.
                    
                    User question: $userMessage
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text ?: "I apologize, but I couldn't generate a response. Please try again."

                val aiMessage = AiMessage(
                    text = responseText,
                    isFromUser = false
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to get AI response: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}