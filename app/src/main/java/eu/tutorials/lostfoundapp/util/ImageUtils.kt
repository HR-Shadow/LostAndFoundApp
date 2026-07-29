package eu.tutorials.lostfoundapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

/**
 * Converts image Uri to a compressed Base64 String to save directly in Firestore.
 */
fun uriToBase64(context: Context, uri: Uri?): String? {
    if (uri == null) return null
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null

        val maxDimension = 600
        val width = originalBitmap.width
        val height = originalBitmap.height
        val ratio = width.toFloat() / height.toFloat()

        val targetWidth: Int
        val targetHeight: Int

        if (width > height) {
            targetWidth = maxDimension
            targetHeight = (maxDimension / ratio).toInt()
        } else {
            targetHeight = maxDimension
            targetWidth = (maxDimension * ratio).toInt()
        }

        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        val imageBytes = outputStream.toByteArray()

        Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Decodes Base64 string into ImageBitmap for Compose Image component.
 */
@Composable
fun rememberBase64ImageBitmap(base64String: String): ImageBitmap? {
    return remember(base64String) {
        if (base64String.isBlank()) null
        else {
            try {
                val cleanBase64 = if (base64String.contains(",")) {
                    base64String.substringAfter(",")
                } else base64String

                val decodedBytes = Base64.decode(cleanBase64, Base64.NO_WRAP)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                bitmap?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}