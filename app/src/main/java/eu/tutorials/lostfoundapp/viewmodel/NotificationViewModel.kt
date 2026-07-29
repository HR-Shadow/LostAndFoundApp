package eu.tutorials.lostfoundapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.lostfoundapp.model.FoundItem
import eu.tutorials.lostfoundapp.model.LostItem
import eu.tutorials.lostfoundapp.model.MatchRequest
import eu.tutorials.lostfoundapp.model.MatchWithDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationViewModel : ViewModel() {

    private val _notifications = MutableStateFlow<List<MatchWithDetails>>(emptyList())
    val notifications: StateFlow<List<MatchWithDetails>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _isLoading.value = true

        FirebaseFirestore.getInstance().collection("match_requests")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isLoading.value = false
                    _errorMessage.value = error.message
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    viewModelScope.launch {
                        try {
                            val matchList = snapshot.documents.mapNotNull { doc ->
                                doc.data?.let { MatchRequest.fromMap(it) }?.copy(matchId = doc.id)
                            }.filter { match ->
                                !match.hiddenFor.contains(currentUserId) &&
                                        !match.status.equals("REJECTED", ignoreCase = true)
                            }

                            val detailedList = mutableListOf<MatchWithDetails>()
                            var unread = 0

                            for (match in matchList) {
                                val lostItem = fetchLostItem(match.lostItemId)
                                val foundItem = fetchFoundItem(match.foundItemId)
                                val isLostOwner = match.lostUserId == currentUserId

                                val otherUserId = if (isLostOwner) match.foundUserId else match.lostUserId
                                val (userName, userPhone) = fetchUserDetails(otherUserId)

                                val isConfirmed = match.status.equals("CONFIRMED", ignoreCase = true)
                                val userAlreadyConfirmed = if (isLostOwner) match.lostUserConfirmed else match.foundUserConfirmed

                                if (!isConfirmed && !userAlreadyConfirmed) {
                                    unread++
                                }

                                detailedList.add(
                                    MatchWithDetails(
                                        match = match,
                                        lostItem = lostItem,
                                        foundItem = foundItem,
                                        isLostOwner = isLostOwner,
                                        otherUserName = userName,
                                        otherUserPhoneNumber = userPhone
                                    )
                                )
                            }

                            _notifications.value = detailedList
                            _unreadCount.value = unread
                            _isLoading.value = false
                        } catch (e: Exception) {
                            _isLoading.value = false
                            _errorMessage.value = e.localizedMessage
                        }
                    }
                } else {
                    _isLoading.value = false
                }
            }
    }

    fun markNotificationsAsSeen() {
        _unreadCount.value = 0
    }

    fun confirmMatchRequest(matchId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection("match_requests").document(matchId)
                val snapshot = docRef.get().await()
                val match = snapshot.data?.let { MatchRequest.fromMap(it) } ?: return@launch

                val isLostOwner = match.lostUserId == currentUserId
                val updates = mutableMapOf<String, Any>()

                if (isLostOwner) {
                    updates["lostUserConfirmed"] = true
                    if (match.foundUserConfirmed) updates["status"] = "CONFIRMED"
                } else {
                    updates["foundUserConfirmed"] = true
                    if (match.lostUserConfirmed) updates["status"] = "CONFIRMED"
                }

                docRef.update(updates).await()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    // Direct Silent Rejection (Removes item from list instantly & updates Firestore)
    fun rejectMatchRequest(matchId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Instant local removal for zero-lag UI feedback
        _notifications.value = _notifications.value.filter { it.match.matchId != matchId }

        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val updates = mapOf<String, Any>(
                    "status" to "REJECTED",
                    "hiddenFor" to FieldValue.arrayUnion(currentUserId)
                )
                db.collection("match_requests").document(matchId).update(updates).await()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun hideMatch(matchId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Instant local removal for zero-lag UI feedback
        _notifications.value = _notifications.value.filter { it.match.matchId != matchId }

        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance().collection("match_requests")
                    .document(matchId)
                    .update("hiddenFor", FieldValue.arrayUnion(currentUserId))
                    .await()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    private suspend fun fetchLostItem(itemId: String): LostItem? {
        if (itemId.isBlank()) return null
        return try {
            val doc = FirebaseFirestore.getInstance().collection("lost_items").document(itemId).get().await()
            doc.data?.let { LostItem.fromMap(it) }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchFoundItem(itemId: String): FoundItem? {
        if (itemId.isBlank()) return null
        return try {
            val doc = FirebaseFirestore.getInstance().collection("found_items").document(itemId).get().await()
            doc.data?.let { FoundItem.fromMap(it) }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchUserDetails(userId: String): Pair<String, String> {
        if (userId.isBlank()) return Pair("User", "")
        return try {
            val doc = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
            val data = doc.data
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
}