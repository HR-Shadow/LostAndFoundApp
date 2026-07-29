package eu.tutorials.lostfoundapp.util

import eu.tutorials.lostfoundapp.model.FoundItem
import eu.tutorials.lostfoundapp.model.LostItem
import kotlin.math.max
import kotlin.math.min

object MatchingAlgorithm {

    const val MATCH_THRESHOLD = 0.45
    private const val MAX_DATE_GAP_DAYS = 60L
    private const val MAX_LOCATION_DISTANCE_KM = 25.0

    private val STOP_WORDS = setOf(
        "a", "an", "the", "and", "or", "of", "in", "on", "at", "to", "for", "is", "it", "my", "with"
    )

    fun calculateScore(lost: LostItem, found: FoundItem): Double {
        if (lost.category != found.category) return 0.0
        if (lost.userId == found.userId) return 0.0

        var score = 0.0

        // Category match (required gate + 25%)
        score += 0.25

        // Item name keyword overlap (25%)
        score += 0.25 * textSimilarity(lost.itemName, found.itemName)

        // Description + identifying details (30%)
        val lostText = "${lost.description} ${lost.identifyingDetails}"
        val foundText = "${found.description} ${found.identifyingDetails}"
        score += 0.30 * textSimilarity(lostText, foundText)

        // Location proximity (10%)
        score += 0.10 * locationSimilarity(lost, found)

        // Date compatibility (10%)
        score += 0.10 * dateCompatibility(lost.dateLost, found.dateFound)

        return score.coerceIn(0.0, 1.0)
    }

    fun isPotentialMatch(lost: LostItem, found: FoundItem): Boolean =
        calculateScore(lost, found) >= MATCH_THRESHOLD

    private fun textSimilarity(a: String, b: String): Double {
        val wordsA = tokenize(a)
        val wordsB = tokenize(b)
        if (wordsA.isEmpty() || wordsB.isEmpty()) return 0.0

        val intersection = wordsA.intersect(wordsB).size
        val union = wordsA.union(wordsB).size
        return intersection.toDouble() / union.toDouble()
    }

    private fun tokenize(text: String): Set<String> =
        text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 && it !in STOP_WORDS }
            .toSet()

    private fun locationSimilarity(lost: LostItem, found: FoundItem): Double {
        val latA = lost.latitude
        val lonA = lost.longitude
        val latB = found.latitude
        val lonB = found.longitude

        if (latA != null && lonA != null && latB != null && lonB != null) {
            val distanceKm = haversineKm(latA, lonA, latB, lonB)
            return when {
                distanceKm <= 2.0 -> 1.0
                distanceKm <= 10.0 -> 0.75
                distanceKm <= MAX_LOCATION_DISTANCE_KM -> 0.5
                else -> 0.0
            }
        }

        return textSimilarity(lost.locationLost, found.locationFound)
    }

    private fun dateCompatibility(dateLost: Long, dateFound: Long): Double {
        if (dateLost == 0L || dateFound == 0L) return 0.5

        val daysDiff = kotlin.math.abs(dateFound - dateLost) / (1000L * 60 * 60 * 24)
        return when {
            dateFound >= dateLost && daysDiff <= MAX_DATE_GAP_DAYS -> 1.0
            daysDiff <= MAX_DATE_GAP_DAYS -> 0.7
            daysDiff <= 90 -> 0.3
            else -> 0.0
        }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadiusKm * c
    }
}
