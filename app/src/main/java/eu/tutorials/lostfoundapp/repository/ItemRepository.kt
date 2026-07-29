package eu.tutorials.lostfoundapp.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.lostfoundapp.model.FoundItem
import eu.tutorials.lostfoundapp.model.ItemStatus
import eu.tutorials.lostfoundapp.model.LostItem
import eu.tutorials.lostfoundapp.util.uriToBase64
import kotlinx.coroutines.tasks.await

class ItemRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val LOST_ITEMS = "lost_items"
        private const val FOUND_ITEMS = "found_items"
    }

    private val currentUserId: String
        get() = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in")

    suspend fun reportLostItem(
        context: Context,
        itemName: String,
        category: String,
        description: String,
        dateLost: Long,
        locationLost: String,
        identifyingDetails: String,
        imageUri: Uri?
    ): Result<LostItem> = runCatching {
        val docRef = firestore.collection(LOST_ITEMS).document()

        // Firebase Storage bypass -> Base64 string conversion
        val imageBase64 = uriToBase64(context, imageUri) ?: ""

        val item = LostItem(
            itemId = docRef.id,
            userId = currentUserId,
            itemName = itemName.trim(),
            category = category,
            description = description.trim(),
            dateLost = dateLost,
            locationLost = locationLost.trim(),
            imageUrl = imageBase64, // Base64 string directly stored in imageUrl field
            identifyingDetails = identifyingDetails.trim(),
            status = ItemStatus.SEARCHING.value.uppercase(),
            timestamp = System.currentTimeMillis()
        )
        docRef.set(item.toMap()).await()
        item
    }

    suspend fun reportFoundItem(
        context: Context,
        itemName: String,
        category: String,
        description: String,
        dateFound: Long,
        locationFound: String,
        identifyingDetails: String,
        imageUri: Uri?
    ): Result<FoundItem> = runCatching {
        val docRef = firestore.collection(FOUND_ITEMS).document()

        // Firebase Storage bypass -> Base64 string conversion
        val imageBase64 = uriToBase64(context, imageUri) ?: ""

        val item = FoundItem(
            itemId = docRef.id,
            userId = currentUserId,
            itemName = itemName.trim(),
            category = category,
            description = description.trim(),
            dateFound = dateFound,
            locationFound = locationFound.trim(),
            imageUrl = imageBase64, // Base64 string directly stored in imageUrl field
            identifyingDetails = identifyingDetails.trim(),
            status = ItemStatus.REPORTED.value.uppercase(),
            timestamp = System.currentTimeMillis()
        )
        docRef.set(item.toMap()).await()
        item
    }
}