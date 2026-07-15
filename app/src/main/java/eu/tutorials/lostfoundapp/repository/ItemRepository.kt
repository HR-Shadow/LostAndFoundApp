package eu.tutorials.lostfoundapp.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import eu.tutorials.lostfoundapp.model.FoundItem
import eu.tutorials.lostfoundapp.model.ItemStatus
import eu.tutorials.lostfoundapp.model.LostItem
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ItemRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    companion object {
        private const val LOST_ITEMS = "lost_items"
        private const val FOUND_ITEMS = "found_items"
        private const val ITEM_IMAGES = "item_images"
    }

    private val currentUserId: String
        get() = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in")

    suspend fun uploadImage(context: Context, imageUri: Uri, folder: String): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child("$ITEM_IMAGES/$folder/$currentUserId/$fileName")
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            storageRef.putStream(inputStream).await()
        } ?: throw IllegalStateException("Unable to read image")
        return storageRef.downloadUrl.await().toString()
    }

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
        var imageUrl = ""
        if (imageUri != null) {
            imageUrl = uploadImage(context, imageUri, "lost")
        }
        val item = LostItem(
            itemId = docRef.id,
            userId = currentUserId,
            itemName = itemName.trim(),
            category = category,
            description = description.trim(),
            dateLost = dateLost,
            locationLost = locationLost.trim(),
            imageUrl = imageUrl,
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
        var imageUrl = ""
        if (imageUri != null) {
            imageUrl = uploadImage(context, imageUri, "found")
        }
        val item = FoundItem(
            itemId = docRef.id,
            userId = currentUserId,
            itemName = itemName.trim(),
            category = category,
            description = description.trim(),
            dateFound = dateFound,
            locationFound = locationFound.trim(),
            imageUrl = imageUrl,
            identifyingDetails = identifyingDetails.trim(),
            status = ItemStatus.REPORTED.value.uppercase(),
            timestamp = System.currentTimeMillis()
        )
        docRef.set(item.toMap()).await()
        item
    }
}
