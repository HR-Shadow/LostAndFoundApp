package eu.tutorials.lostfoundapp.model

data class MatchRequest(
    val matchId: String = "",
    val lostItemId: String = "",
    val foundItemId: String = "",
    val lostUserId: String = "",
    val foundUserId: String = "",
    val status: String = MatchStatus.PENDING.value,
    val lostUserConfirmed: Boolean = false,
    val foundUserConfirmed: Boolean = false,
    val matchScore: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val participants: List<String> = emptyList(),

    // NEW FIELD
    val hiddenFor: List<String> = emptyList()
) {

    fun toMap(): Map<String, Any?> = mapOf(
        "matchId" to matchId,
        "lostItemId" to lostItemId,
        "foundItemId" to foundItemId,
        "lostUserId" to lostUserId,
        "foundUserId" to foundUserId,
        "status" to status,
        "lostUserConfirmed" to lostUserConfirmed,
        "foundUserConfirmed" to foundUserConfirmed,
        "matchScore" to matchScore,
        "timestamp" to timestamp,
        "participants" to listOf(lostUserId, foundUserId),

        // NEW FIELD
        "hiddenFor" to hiddenFor
    )

    companion object {

        fun fromMap(map: Map<String, Any?>): MatchRequest {

            return MatchRequest(

                matchId = map["matchId"] as? String ?: "",

                lostItemId = map["lostItemId"] as? String ?: "",

                foundItemId = map["foundItemId"] as? String ?: "",

                lostUserId = map["lostUserId"] as? String ?: "",

                foundUserId = map["foundUserId"] as? String ?: "",

                status = map["status"] as? String
                    ?: MatchStatus.PENDING.value,

                lostUserConfirmed =
                    map["lostUserConfirmed"] as? Boolean ?: false,

                foundUserConfirmed =
                    map["foundUserConfirmed"] as? Boolean ?: false,

                matchScore =
                    (map["matchScore"] as? Number)?.toDouble() ?: 0.0,

                timestamp =
                    (map["timestamp"] as? Number)?.toLong() ?: 0L,

                participants =
                    (map["participants"] as? List<*>)
                        ?.filterIsInstance<String>()
                        ?: emptyList(),

                // NEW FIELD
                hiddenFor =
                    (map["hiddenFor"] as? List<*>)
                        ?.filterIsInstance<String>()
                        ?: emptyList()
            )
        }
    }
}

data class MatchWithDetails(
    val match: MatchRequest,
    val lostItem: LostItem?,
    val foundItem: FoundItem?,
    val isLostOwner: Boolean
)