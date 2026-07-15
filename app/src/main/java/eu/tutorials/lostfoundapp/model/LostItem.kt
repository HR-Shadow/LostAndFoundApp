package eu.tutorials.lostfoundapp.model

data class LostItem(
    val itemId: String = "",
    val userId: String = "",
    val itemName: String = "",
    val category: String = ItemCategory.OTHER.name,
    val description: String = "",
    val dateLost: Long = 0L,
    val locationLost: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrl: String = "",
    val identifyingDetails: String = "",
    val status: String = ItemStatus.SEARCHING.value,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "itemId" to itemId,
        "userId" to userId,
        "itemName" to itemName,
        "category" to category,
        "description" to description,
        "dateLost" to dateLost,
        "locationLost" to locationLost,
        "latitude" to latitude,
        "longitude" to longitude,
        "imageUrl" to imageUrl,
        "identifyingDetails" to identifyingDetails,
        "status" to status,
        "timestamp" to timestamp
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): LostItem = LostItem(
            itemId = map["itemId"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            itemName = map["itemName"] as? String ?: "",
            category = map["category"] as? String ?: ItemCategory.OTHER.name,
            description = map["description"] as? String ?: "",
            dateLost = (map["dateLost"] as? Number)?.toLong() ?: 0L,
            locationLost = map["locationLost"] as? String ?: "",
            latitude = (map["latitude"] as? Number)?.toDouble(),
            longitude = (map["longitude"] as? Number)?.toDouble(),
            imageUrl = map["imageUrl"] as? String ?: "",
            identifyingDetails = map["identifyingDetails"] as? String ?: "",
            status = map["status"] as? String ?: ItemStatus.SEARCHING.value,
            timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
        )
    }
}
