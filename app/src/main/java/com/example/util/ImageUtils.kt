package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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

    fun saveUriToAppStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val receiptsDir = File(context.filesDir, "receipts").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(receiptsDir, "receipt_${System.currentTimeMillis()}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                // Compress slightly if too large to save space while keeping legibility
                val bitmap = BitmapFactory.decodeStream(input)
                if (bitmap != null) {
                    FileOutputStream(destinationFile).use { out ->
                        // Scale down if massive (e.g. over 1920px)
                        val maxDim = 1920
                        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                            val newW = if (ratio > 1) maxDim else (maxDim * ratio).toInt()
                            val newH = if (ratio > 1) (maxDim / ratio).toInt() else maxDim
                            Bitmap.createScaledBitmap(bitmap, newW, newH, true)
                        } else {
                            bitmap
                        }
                        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                } else {
                    // Fallback copy stream directly
                    context.contentResolver.openInputStream(sourceUri)?.use { rawInput ->
                        FileOutputStream(destinationFile).use { out ->
                            rawInput.copyTo(out)
                        }
                    }
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
