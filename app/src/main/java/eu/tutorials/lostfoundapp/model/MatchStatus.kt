package eu.tutorials.lostfoundapp.model

enum class MatchStatus(val value: String) {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    REJECTED("rejected");

    companion object {
        fun fromString(value: String): MatchStatus =
            entries.find { it.value.equals(value, ignoreCase = true) } ?: PENDING
    }
}
