package eu.tutorials.lostfoundapp.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import eu.tutorials.lostfoundapp.model.FoundItem
import eu.tutorials.lostfoundapp.model.ItemStatus
import eu.tutorials.lostfoundapp.model.LostItem
import eu.tutorials.lostfoundapp.model.MatchRequest
import eu.tutorials.lostfoundapp.model.MatchStatus
import eu.tutorials.lostfoundapp.model.MatchWithDetails
import eu.tutorials.lostfoundapp.util.MatchingAlgorithm
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MatchRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val LOST_ITEMS = "lost_items"
        private const val FOUND_ITEMS = "found_items"
        private const val MATCH_REQUESTS = "match_requests"
        private const val USERS = "users"
    }

    private val currentUserId: String
        get() = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in")

    suspend fun runMatchingForLostItem(lostItem: LostItem): Result<Int> = runCatching {
        val candidates = firestore.collection(FOUND_ITEMS)
            .whereEqualTo("category", lostItem.category)
            .whereEqualTo("status", ItemStatus.REPORTED.value.uppercase())
            .get()
            .await()
            .documents
            .mapNotNull { doc -> doc.data?.let { FoundItem.fromMap(it) } }
            .filter { it.userId != lostItem.userId }

        var created = 0
        for (found in candidates) {
            if (MatchingAlgorithm.isPotentialMatch(lostItem, found)) {
                val score = MatchingAlgorithm.calculateScore(lostItem, found)
                if (createMatchIfNotExists(lostItem, found, score)) created++
            }
        }
        created
    }

    suspend fun runMatchingForFoundItem(foundItem: FoundItem): Result<Int> = runCatching {
        val candidates = firestore.collection(LOST_ITEMS)
            .whereEqualTo("category", foundItem.category)
            .whereEqualTo("status", ItemStatus.SEARCHING.value.uppercase())
            .get()
            .await()
            .documents
            .mapNotNull { doc -> doc.data?.let { LostItem.fromMap(it) } }
            .filter { it.userId != foundItem.userId }

        var created = 0
        for (lost in candidates) {
            if (MatchingAlgorithm.isPotentialMatch(lost, foundItem)) {
                val score = MatchingAlgorithm.calculateScore(lost, foundItem)
                if (createMatchIfNotExists(lost, foundItem, score)) created++
            }
        }
        created
    }

    private suspend fun createMatchIfNotExists(
        lost: LostItem,
        found: FoundItem,
        score: Double
    ): Boolean {
        val existing = firestore.collection(MATCH_REQUESTS)
            .whereEqualTo("lostItemId", lost.itemId)
            .whereEqualTo("foundItemId", found.itemId)
            .get()
            .await()

        if (!existing.isEmpty) return false

        val docRef = firestore.collection(MATCH_REQUESTS).document()
        val participantsList = listOf(lost.userId, found.userId)

        val docData = hashMapOf(
            "matchId" to docRef.id,
            "lostItemId" to lost.itemId,
            "foundItemId" to found.itemId,
            "lostUserId" to lost.userId,
            "foundUserId" to found.userId,
            "status" to MatchStatus.PENDING.value,
            "matchScore" to score,
            "timestamp" to System.currentTimeMillis(),
            "participants" to participantsList,
            "lostUserConfirmed" to false,
            "foundUserConfirmed" to false
        )

        docRef.set(docData).await()
        return true
    }

    fun observeUserMatches(): Flow<List<MatchRequest>> = callbackFlow {
        val listener = firestore.collection(MATCH_REQUESTS)
            .whereArrayContains("participants", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val matches = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.data?.let { MatchRequest.fromMap(it) }
                    }
                    ?.filter {
                        !it.hiddenFor.contains(currentUserId) && it.status != MatchStatus.REJECTED.value
                    }
                    ?: emptyList()

                trySend(matches)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun loadMatchDetails(matches: List<MatchRequest>): List<MatchWithDetails> {
        return matches.map { match ->
            val lostItem = getLostItem(match.lostItemId)
            val foundItem = getFoundItem(match.foundItemId)
            val isLostOwner = match.lostUserId == currentUserId

            val otherUserId = if (isLostOwner) match.foundUserId else match.lostUserId
            val (userName, userPhone) = getUserDetails(otherUserId)

            MatchWithDetails(
                match = match,
                lostItem = lostItem,
                foundItem = foundItem,
                isLostOwner = isLostOwner,
                otherUserName = userName,
                otherUserPhoneNumber = userPhone
            )
        }
    }

    suspend fun confirmMatch(matchId: String): Result<MatchRequest> = runCatching {
        val docRef = firestore.collection(MATCH_REQUESTS).document(matchId)
        val snapshot = docRef.get().await()
        val match = MatchRequest.fromMap(snapshot.data ?: throw IllegalStateException("Match not found"))

        require(match.lostUserId == currentUserId || match.foundUserId == currentUserId) {
            "Not authorized"
        }
        require(match.status == MatchStatus.PENDING.value) {
            "Match is no longer pending"
        }

        val isLostOwner = match.lostUserId == currentUserId
        val updates = mutableMapOf<String, Any>(
            if (isLostOwner) "lostUserConfirmed" to true else "foundUserConfirmed" to true
        )

        val lostConfirmed = if (isLostOwner) true else match.lostUserConfirmed
        val foundConfirmed = if (!isLostOwner) true else match.foundUserConfirmed

        if (lostConfirmed && foundConfirmed) {
            updates["status"] = MatchStatus.CONFIRMED.value
            updateItemStatus(match.lostItemId, isLost = true, status = ItemStatus.MATCHED.value.uppercase())
            updateItemStatus(match.foundItemId, isLost = false, status = ItemStatus.MATCHED.value.uppercase())
        }

        docRef.update(updates).await()
        val updated = match.copy(
            lostUserConfirmed = lostConfirmed,
            foundUserConfirmed = foundConfirmed,
            status = if (lostConfirmed && foundConfirmed) {
                MatchStatus.CONFIRMED.value
            } else {
                MatchStatus.PENDING.value
            }
        )
        updated
    }

    suspend fun rejectMatch(matchId: String): Result<Unit> = runCatching {
        val docRef = firestore.collection(MATCH_REQUESTS).document(matchId)
        val snapshot = docRef.get().await()
        val match = MatchRequest.fromMap(snapshot.data ?: throw IllegalStateException("Match not found"))

        require(match.lostUserId == currentUserId || match.foundUserId == currentUserId) {
            "Not authorized"
        }
        require(match.status == MatchStatus.PENDING.value) {
            "Match is no longer pending"
        }

        // Updates status to REJECTED and auto-hides for current user simultaneously
        val updates = mapOf<String, Any>(
            "status" to MatchStatus.REJECTED.value,
            "hiddenFor" to FieldValue.arrayUnion(currentUserId)
        )
        docRef.update(updates).await()
    }

    private suspend fun getLostItem(itemId: String): LostItem? {
        if (itemId.isBlank()) return null
        val snapshot = firestore.collection(LOST_ITEMS).document(itemId).get().await()
        return snapshot.data?.let { LostItem.fromMap(it) }
    }

    private suspend fun getFoundItem(itemId: String): FoundItem? {
        if (itemId.isBlank()) return null
        val snapshot = firestore.collection(FOUND_ITEMS).document(itemId).get().await()
        return snapshot.data?.let { FoundItem.fromMap(it) }
    }

    private suspend fun getUserDetails(userId: String): Pair<String, String> {
        if (userId.isBlank()) return Pair("User", "")
        return try {
            val snapshot = firestore.collection(USERS).document(userId).get().await()
            val data = snapshot.data
            val name = data?.get("name") as? String
                ?: data?.get("userName") as? String
                ?: "User"
            val phone = data?.get("phoneNumber") as? String
                ?: data?.get("phone") as? String
                ?: ""
            Pair(name, phone)
        } catch (e: Exception) {
            Pair("User", "")
        }
    }

    private suspend fun updateItemStatus(itemId: String, isLost: Boolean, status: String) {
        val collection = if (isLost) LOST_ITEMS else FOUND_ITEMS
        firestore.collection(collection).document(itemId)
            .update("status", status)
            .await()
    }

    suspend fun hideMatchForCurrentUser(matchId: String): Result<Unit> = runCatching {
        val docRef = firestore.collection(MATCH_REQUESTS).document(matchId)
        val snapshot = docRef.get().await()
        val match = MatchRequest.fromMap(
            snapshot.data ?: throw IllegalStateException("Match not found")
        )

        require(
            match.lostUserId == currentUserId || match.foundUserId == currentUserId
        ) {
            "Not authorized"
        }

        docRef.update(
            "hiddenFor",
            FieldValue.arrayUnion(currentUserId)
        ).await()
    }
}