package eu.tutorials.lostfoundapp.model

enum class NotificationType {
    MATCH,
    MESSAGE
}

data class NotificationModel(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val matchedImageUrl: String? = null,
    val timestamp: String,
    val type: NotificationType,
    val isRead: Boolean = false
)
