package eu.tutorials.lostfoundapp.model

enum class ItemCategory(val displayName: String) {
    WALLET("Wallet"),
    PHONE("Phone"),
    DOCUMENTS("Documents"),
    JEWELRY("Jewelry"),
    BAG("Bag"),
    KEYS("Keys"),
    PET("Pet"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): ItemCategory =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }
}
