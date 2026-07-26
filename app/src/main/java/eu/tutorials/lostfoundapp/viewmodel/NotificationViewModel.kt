package eu.tutorials.lostfoundapp.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.lostfoundapp.model.FoundItem
import eu.tutorials.lostfoundapp.model.LostItem
import eu.tutorials.lostfoundapp.model.MatchRequest
import eu.tutorials.lostfoundapp.model.MatchWithDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val _notifications = MutableStateFlow<List<MatchWithDetails>>(emptyList())
    val notifications: StateFlow<List<MatchWithDetails>> = _notifications.asStateFlow()

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
                                doc.toObject(MatchRequest::class.java)?.copy(matchId = doc.id)
                            }

                            val detailedList = mutableListOf<MatchWithDetails>()
                            for (match in matchList) {
                                val lostItem = fetchLostItem(match.lostItemId)
                                val foundItem = fetchFoundItem(match.foundItemId)
                                val isLostOwner = match.lostUserId == currentUserId

                                detailedList.add(
                                    MatchWithDetails(
                                        match = match,
                                        lostItem = lostItem,
                                        foundItem = foundItem,
                                        isLostOwner = isLostOwner
                                    )
                                )
                            }
                            _notifications.value = detailedList
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

    private suspend fun fetchLostItem(itemId: String): LostItem? {
        if (itemId.isBlank()) return null
        return try {
            val doc = FirebaseFirestore.getInstance().collection("lost_items").document(itemId).get().await()
            doc.toObject(LostItem::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchFoundItem(itemId: String): FoundItem? {
        if (itemId.isBlank()) return null
        return try {
            val doc = FirebaseFirestore.getInstance().collection("found_items").document(itemId).get().await()
            doc.toObject(FoundItem::class.java)
        } catch (e: Exception) {
            null
        }
    }
}