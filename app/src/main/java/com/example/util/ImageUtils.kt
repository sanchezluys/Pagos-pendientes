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

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than requested dimensions.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun saveUriToAppStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val receiptsDir = File(context.filesDir, "receipts").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(receiptsDir, "receipt_${System.currentTimeMillis()}.jpg")

            val maxDimension = 1920

            // Step 1: Decode dimensions only (memory-safe with inJustDecodeBounds)
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            // Step 2: Decode with calculated inSampleSize to prevent OOM
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(boundsOptions, maxDimension, maxDimension)
                inJustDecodeBounds = false
            }

            var decodedBitmap: Bitmap? = null
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                decodedBitmap = BitmapFactory.decodeStream(input, null, decodeOptions)
            }

            if (decodedBitmap != null) {
                val bitmap = decodedBitmap!!
                FileOutputStream(destinationFile).use { out ->
                    // Final scale down if still exceeds max dimension after inSampleSize
                    val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                        val newW = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                        val newH = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                        Bitmap.createScaledBitmap(bitmap, newW, newH, true)
                    } else {
                        bitmap
                    }

                    scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)

                    if (scaled != bitmap) {
                        scaled.recycle()
                    }
                    bitmap.recycle()
                }
            } else {
                // Fallback direct stream copy
                context.contentResolver.openInputStream(sourceUri)?.use { rawInput ->
                    FileOutputStream(destinationFile).use { out ->
                        rawInput.copyTo(out)
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
