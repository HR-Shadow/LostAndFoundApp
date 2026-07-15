package eu.tutorials.lostfoundapp.model

enum class ItemStatus(val value: String) {
    SEARCHING("searching"),
    REPORTED("reported"),
    MATCHED("matched"),
    RESOLVED("resolved");

    companion object {
        fun fromString(value: String): ItemStatus =
            entries.find { it.value.equals(value, ignoreCase = true) } ?: SEARCHING
    }
}
