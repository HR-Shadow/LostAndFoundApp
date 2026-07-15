package eu.tutorials.lostfoundapp.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "messageId" to messageId,
        "senderId" to senderId,
        "text" to text,
        "timestamp" to timestamp
    )

    companion object {
        fun fromDocument(
            documentId: String,
            data: Map<String, Any?>
        ): ChatMessage = ChatMessage(
            messageId = data["messageId"] as? String ?: documentId,
            senderId = data["senderId"] as? String ?: "",
            text = data["text"] as? String ?: "",
            timestamp = data["timestamp"] as? Timestamp
        )
    }
}
