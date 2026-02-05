package com.brightcare.patient.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Utility class for compressing images while maintaining readability
 * Utility class para sa pag-compress ng mga larawan habang pinapanatili ang readability
 */
object ImageCompressionUtils {
    
    private const val TAG = "ImageCompressionUtils"
    
    // Configuration for ID document compression
    // Kompigurasyon para sa compression ng ID documents
    private const val MAX_WIDTH = 1200 // Maximum width for ID documents
    private const val MAX_HEIGHT = 1200 // Maximum height for ID documents
    private const val QUALITY_HIGH = 85 // High quality for readability (85%)
    private const val QUALITY_MEDIUM = 75 // Medium quality fallback (75%)
    private const val MAX_FILE_SIZE_KB = 500 // Target max file size in KB
    
    /**
     * Compress image for ID document upload
     * Maintains high quality while reducing file size for faster upload
     * 
     * I-compress ang larawan para sa ID document upload
     * Pinapanatili ang mataas na kalidad habang binabawasan ang laki ng file para sa mas mabilis na upload
     */
    suspend fun compressIdImage(
        context: Context,
        imageUri: Uri,
        isIdDocument: Boolean = true
    ): Result<String> {
        return try {
            Log.d(TAG, "Starting image compression for URI: $imageUri")
            
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                return Result.failure(Exception("Cannot open image file"))
            }
            
            // Read original bitmap
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (originalBitmap == null) {
                return Result.failure(Exception("Cannot decode image"))
            }
            
            Log.d(TAG, "Original image size: ${originalBitmap.width}x${originalBitmap.height}")
            
            // Handle image rotation based on EXIF data
            val rotatedBitmap = handleImageRotation(context, imageUri, originalBitmap)
            
            // Calculate optimal dimensions
            val (newWidth, newHeight) = calculateOptimalDimensions(
                rotatedBitmap.width,
                rotatedBitmap.height,
                if (isIdDocument) MAX_WIDTH else 800,
                if (isIdDocument) MAX_HEIGHT else 800
            )
            
            // Resize bitmap if needed
            val resizedBitmap = if (newWidth != rotatedBitmap.width || newHeight != rotatedBitmap.height) {
                Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, true)
            } else {
                rotatedBitmap
            }
            
            Log.d(TAG, "Resized image to: ${resizedBitmap.width}x${resizedBitmap.height}")
            
            // Compress with adaptive quality
            val compressedFile = compressWithAdaptiveQuality(
                context,
                resizedBitmap,
                if (isIdDocument) QUALITY_HIGH else QUALITY_MEDIUM,
                MAX_FILE_SIZE_KB
            )
            
            // Clean up bitmaps
            if (rotatedBitmap != originalBitmap) {
                rotatedBitmap.recycle()
            }
            if (resizedBitmap != rotatedBitmap) {
                resizedBitmap.recycle()
            }
            originalBitmap.recycle()
            
            val finalSizeKB = compressedFile.length() / 1024
            Log.d(TAG, "Image compression completed. Final size: ${finalSizeKB}KB")
            
            Result.success(compressedFile.absolutePath)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image", e)
            Result.failure(Exception("Failed to compress image: ${e.message}"))
        }
    }
    
    /**
     * Handle image rotation based on EXIF data
     * Ayusin ang rotation ng larawan base sa EXIF data
     */
    private fun handleImageRotation(context: Context, imageUri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream != null) {
                val exif = ExifInterface(inputStream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                inputStream.close()
                
                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    else -> return bitmap
                }
                
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
            bitmap
        } catch (e: Exception) {
            Log.w(TAG, "Could not handle image rotation", e)
            bitmap
        }
    }
    
    /**
     * Calculate optimal dimensions while maintaining aspect ratio
     * Kalkulahin ang optimal na dimensions habang pinapanatili ang aspect ratio
     */
    private fun calculateOptimalDimensions(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Pair<Int, Int> {
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return Pair(originalWidth, originalHeight)
        }
        
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        
        return if (originalWidth > originalHeight) {
            // Landscape orientation
            val newWidth = minOf(maxWidth, originalWidth)
            val newHeight = (newWidth / aspectRatio).toInt()
            if (newHeight > maxHeight) {
                val adjustedHeight = maxHeight
                val adjustedWidth = (adjustedHeight * aspectRatio).toInt()
                Pair(adjustedWidth, adjustedHeight)
            } else {
                Pair(newWidth, newHeight)
            }
        } else {
            // Portrait orientation
            val newHeight = minOf(maxHeight, originalHeight)
            val newWidth = (newHeight * aspectRatio).toInt()
            if (newWidth > maxWidth) {
                val adjustedWidth = maxWidth
                val adjustedHeight = (adjustedWidth / aspectRatio).toInt()
                Pair(adjustedWidth, adjustedHeight)
            } else {
                Pair(newWidth, newHeight)
            }
        }
    }
    
    /**
     * Compress bitmap with adaptive quality to meet target file size
     * I-compress ang bitmap na may adaptive quality para makamit ang target file size
     */
    private fun compressWithAdaptiveQuality(
        context: Context,
        bitmap: Bitmap,
        initialQuality: Int,
        maxSizeKB: Int
    ): File {
        val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        var quality = initialQuality
        var compressedData: ByteArray
        
        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedData = outputStream.toByteArray()
            outputStream.close()
            
            val sizeKB = compressedData.size / 1024
            Log.d(TAG, "Compression attempt - Quality: $quality%, Size: ${sizeKB}KB")
            
            if (sizeKB <= maxSizeKB || quality <= 50) {
                break
            }
            
            // Reduce quality by 10% for next attempt
            quality = maxOf(50, quality - 10)
            
        } while (quality >= 50)
        
        // Write final compressed data to file
        FileOutputStream(outputFile).use { fos ->
            fos.write(compressedData)
        }
        
        val finalSizeKB = outputFile.length() / 1024
        Log.d(TAG, "Final compression - Quality: $quality%, Size: ${finalSizeKB}KB")
        
        return outputFile
    }
    
    /**
     * Get file size in KB
     * Kunin ang laki ng file sa KB
     */
    fun getFileSizeKB(file: File): Long {
        return file.length() / 1024
    }
    
    /**
     * Clean up temporary compressed files
     * Linisin ang mga temporary compressed files
     */
    fun cleanupTempFiles(context: Context) {
        try {
            val cacheDir = context.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("compressed_") && file.name.endsWith(".jpg")) {
                    if (System.currentTimeMillis() - file.lastModified() > 24 * 60 * 60 * 1000) { // 24 hours
                        file.delete()
                        Log.d(TAG, "Cleaned up old temp file: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning up temp files", e)
        }
    }
}



