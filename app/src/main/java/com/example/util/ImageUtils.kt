package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.core.content.FileProvider
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import coil.size.Scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    fun createTempPictureUri(context: Context): Uri {
        val storageDir: File = context.cacheDir
        val tempFile = File.createTempFile(
            "temp_receipt_",
            ".jpg",
            storageDir
        )
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, tempFile)
    }

    /**
     * Saves and optimizes an image URI using Coil image loading library for
     * memory management, automatic downsampling and caching.
     */
    suspend fun saveUriToAppStorage(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val receiptsDir = File(context.filesDir, "receipts").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(receiptsDir, "receipt_${System.currentTimeMillis()}.jpg")

            // Use Coil's ImageLoader to safely decode, downsample, and handle bitmap memory
            val maxDimension = 1920
            val imageLoader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(sourceUri)
                .size(maxDimension, maxDimension)
                .scale(Scale.FIT)
                .precision(Precision.INEXACT)
                .allowHardware(false) // Requires software bitmap for FileOutputStream compression
                .build()

            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    FileOutputStream(destinationFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    return@withContext destinationFile.absolutePath
                }
            }

            // Fallback: direct stream copy if Coil doesn't yield a bitmap
            context.contentResolver.openInputStream(sourceUri)?.use { rawInput ->
                FileOutputStream(destinationFile).use { out ->
                    rawInput.copyTo(out)
                }
            }

            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Synchronous bridge for situations where a coroutine scope is not available.
     */
    fun saveUriToAppStorageSync(context: Context, sourceUri: Uri): String? {
        return runBlocking(Dispatchers.IO) {
            saveUriToAppStorage(context, sourceUri)
        }
    }
}
