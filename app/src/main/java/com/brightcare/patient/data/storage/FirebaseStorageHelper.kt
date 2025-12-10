package com.brightcare.patient.data.storage

import android.net.Uri
import com.brightcare.patient.data.model.UploadProgress
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Storage helper for file uploads
 * Firebase Storage helper para sa file uploads
 */
@Singleton
class FirebaseStorageHelper @Inject constructor(
    private val storage: FirebaseStorage
) {

    companion object {
        private const val CONVERSATIONS_PATH = "conversations"
        private const val IMAGES_PATH = "images"
        private const val FILES_PATH = "files"
        private const val THUMBNAILS_PATH = "thumbnails"
    }

    /**
     * Upload image file with progress tracking
     * Upload ng image file na may progress tracking
     */
    fun uploadImage(
        conversationId: String,
        imageUri: Uri,
        fileName: String? = null
    ): Flow<UploadProgress> = callbackFlow {
        val actualFileName = fileName ?: "image_${UUID.randomUUID()}.jpg"
        val imageRef = getImageReference(conversationId, actualFileName)

        val uploadTask = imageRef.putFile(imageUri)

        // Progress listener
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat() / 100f
            trySend(UploadProgress(actualFileName, progress))
        }

        // Success listener
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                trySend(UploadProgress(
                    fileName = actualFileName,
                    progress = 1.0f,
                    isComplete = true,
                    downloadUrl = downloadUri.toString()
                ))
                close()
            }.addOnFailureListener { exception ->
                trySend(UploadProgress(
                    fileName = actualFileName,
                    progress = 0f,
                    error = exception.message
                ))
                close()
            }
        }

        // Failure listener
        uploadTask.addOnFailureListener { exception ->
            trySend(UploadProgress(
                fileName = actualFileName,
                progress = 0f,
                error = exception.message
            ))
            close()
        }

        awaitClose {
            uploadTask.cancel()
        }
    }

    /**
     * Upload file (document, PDF, etc.) with progress tracking
     * Upload ng file (document, PDF, etc.) na may progress tracking
     */
    fun uploadFile(
        conversationId: String,
        fileUri: Uri,
        fileName: String,
        mimeType: String
    ): Flow<UploadProgress> = callbackFlow {
        val fileRef = getFileReference(conversationId, fileName)

        val uploadTask = fileRef.putFile(fileUri)

        // Progress listener
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat() / 100f
            trySend(UploadProgress(fileName, progress))
        }

        // Success listener
        uploadTask.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                trySend(UploadProgress(
                    fileName = fileName,
                    progress = 1.0f,
                    isComplete = true,
                    downloadUrl = downloadUri.toString()
                ))
                close()
            }.addOnFailureListener { exception ->
                trySend(UploadProgress(
                    fileName = fileName,
                    progress = 0f,
                    error = exception.message
                ))
                close()
            }
        }

        // Failure listener
        uploadTask.addOnFailureListener { exception ->
            trySend(UploadProgress(
                fileName = fileName,
                progress = 0f,
                error = exception.message
            ))
            close()
        }

        awaitClose {
            uploadTask.cancel()
        }
    }

    /**
     * Generate thumbnail for image
     * Gumawa ng thumbnail para sa image
     */
    suspend fun generateThumbnail(
        conversationId: String,
        originalImageUrl: String,
        thumbnailUri: Uri
    ): Result<String> {
        return try {
            val thumbnailFileName = "thumb_${UUID.randomUUID()}.jpg"
            val thumbnailRef = getThumbnailReference(conversationId, thumbnailFileName)
            
            val uploadTask = thumbnailRef.putFile(thumbnailUri).await()
            val downloadUrl = thumbnailRef.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete file from storage
     * Tanggalin ang file sa storage
     */
    suspend fun deleteFile(fileUrl: String): Result<Unit> {
        return try {
            val fileRef = storage.getReferenceFromUrl(fileUrl)
            fileRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get file metadata
     * Kunin ang file metadata
     */
    suspend fun getFileMetadata(fileUrl: String): Result<Map<String, Any>> {
        return try {
            val fileRef = storage.getReferenceFromUrl(fileUrl)
            val metadata = fileRef.metadata.await()
            
            val metadataMap = mapOf(
                "name" to (metadata.name ?: ""),
                "size" to metadata.sizeBytes,
                "contentType" to (metadata.contentType ?: ""),
                "timeCreated" to (metadata.creationTimeMillis),
                "updated" to (metadata.updatedTimeMillis)
            )
            
            Result.success(metadataMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get image reference for conversation
     * Kunin ang image reference para sa conversation
     */
    private fun getImageReference(conversationId: String, fileName: String): StorageReference {
        return storage.reference
            .child(CONVERSATIONS_PATH)
            .child(conversationId)
            .child(IMAGES_PATH)
            .child(fileName)
    }

    /**
     * Get file reference for conversation
     * Kunin ang file reference para sa conversation
     */
    private fun getFileReference(conversationId: String, fileName: String): StorageReference {
        return storage.reference
            .child(CONVERSATIONS_PATH)
            .child(conversationId)
            .child(FILES_PATH)
            .child(fileName)
    }

    /**
     * Get thumbnail reference for conversation
     * Kunin ang thumbnail reference para sa conversation
     */
    private fun getThumbnailReference(conversationId: String, fileName: String): StorageReference {
        return storage.reference
            .child(CONVERSATIONS_PATH)
            .child(conversationId)
            .child(THUMBNAILS_PATH)
            .child(fileName)
    }

    /**
     * Get storage path for conversation
     * Kunin ang storage path para sa conversation
     */
    fun getConversationStoragePath(conversationId: String): String {
        return "$CONVERSATIONS_PATH/$conversationId"
    }

    /**
     * Validate file size and type
     * I-validate ang file size at type
     */
    fun validateFile(
        fileUri: Uri,
        maxSizeBytes: Long = 50 * 1024 * 1024, // 50MB default
        allowedMimeTypes: List<String> = listOf(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain", "application/zip"
        )
    ): Result<Unit> {
        return try {
            // Note: In a real implementation, you would check the file size and MIME type
            // This is a placeholder for validation logic
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

