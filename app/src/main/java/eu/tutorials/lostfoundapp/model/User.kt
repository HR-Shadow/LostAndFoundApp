package eu.tutorials.lostfoundapp.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImageUrl: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "name" to name,
        "email" to email,
        "phone" to phone,
        "profileImageUrl" to profileImageUrl
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): User = User(
            userId = map["userId"] as? String ?: "",
            name = map["name"] as? String ?: "",
            email = map["email"] as? String ?: "",
            phone = map["phone"] as? String ?: "",
            profileImageUrl = map["profileImageUrl"] as? String ?: ""
        )
    }
}
