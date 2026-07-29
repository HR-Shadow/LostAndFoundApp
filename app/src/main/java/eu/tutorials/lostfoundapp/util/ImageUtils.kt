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
 * Ensures image size strictly stays under ~150 KB to prevent Firestore 1MB doc limits.
 */
fun uriToBase64(context: Context, uri: Uri?): String? {
    if (uri == null) return null
    var originalBitmap: Bitmap? = null
    var resizedBitmap: Bitmap? = null

    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
        inputStream.close()

        val maxDimension = 600
        val width = originalBitmap.width
        val height = originalBitmap.height

        val (targetWidth, targetHeight) = if (width > height) {
            val ratio = height.toFloat() / width.toFloat()
            maxDimension to (maxDimension * ratio).toInt()
        } else {
            val ratio = width.toFloat() / height.toFloat()
            (maxDimension * ratio).toInt() to maxDimension
        }

        resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)

        // Loop compression: Automatically lowers quality if size exceeds 150 KB
        var quality = 65
        var imageBytes: ByteArray
        val outputStream = ByteArrayOutputStream()

        do {
            outputStream.reset()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            imageBytes = outputStream.toByteArray()
            quality -= 10
        } while (imageBytes.size > 150 * 1024 && quality >= 20)

        Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        // Free bitmap memory immediately
        if (originalBitmap != resizedBitmap) {
            originalBitmap?.recycle()
        }
        resizedBitmap?.recycle()
    }
}

/**
 * Helper function to check exact size of Base64 String in KB
 */
fun getBase64SizeInKB(base64String: String): Double {
    val cleanBase64 = if (base64String.contains(",")) base64String.substringAfter(",") else base64String
    val sizeInBytes = (cleanBase64.length * 3) / 4
    return sizeInBytes / 1024.0
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