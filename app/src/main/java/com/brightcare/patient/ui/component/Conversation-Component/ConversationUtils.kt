package com.brightcare.patient.ui.component.conversationcomponent

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Utility functions for conversation components
 * Mga utility function para sa conversation components
 */

/**
 * Get icon for attachment type
 * Kunin ang icon para sa attachment type
 */
fun getAttachmentIcon(attachmentType: AttachmentType): ImageVector {
    return when (attachmentType) {
        AttachmentType.IMAGE -> Icons.Default.Image
        AttachmentType.VIDEO -> Icons.Default.VideoLibrary
        AttachmentType.AUDIO -> Icons.Default.AudioFile
        AttachmentType.DOCUMENT -> Icons.Default.Description
        AttachmentType.FILE -> Icons.Default.AttachFile
    }
}

/**
 * Get file extension from filename
 * Kunin ang file extension mula sa filename
 */
fun getFileExtension(filename: String): String {
    return filename.substringAfterLast('.', "")
}

/**
 * Check if file is image
 * I-check kung ang file ay image
 */
fun isImageFile(filename: String): Boolean {
    val extension = getFileExtension(filename).lowercase()
    return extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
}

/**
 * Check if file is video
 * I-check kung ang file ay video
 */
fun isVideoFile(filename: String): Boolean {
    val extension = getFileExtension(filename).lowercase()
    return extension in listOf("mp4", "avi", "mov", "wmv", "flv", "webm", "mkv")
}

/**
 * Check if file is audio
 * I-check kung ang file ay audio
 */
fun isAudioFile(filename: String): Boolean {
    val extension = getFileExtension(filename).lowercase()
    return extension in listOf("mp3", "wav", "aac", "ogg", "m4a", "flac")
}

/**
 * Check if file is document
 * I-check kung ang file ay document
 */
fun isDocumentFile(filename: String): Boolean {
    val extension = getFileExtension(filename).lowercase()
    return extension in listOf("pdf", "doc", "docx", "txt", "rtf", "odt")
}

/**
 * Get attachment type from filename
 * Kunin ang attachment type mula sa filename
 */
fun getAttachmentTypeFromFilename(filename: String): AttachmentType {
    return when {
        isImageFile(filename) -> AttachmentType.IMAGE
        isVideoFile(filename) -> AttachmentType.VIDEO
        isAudioFile(filename) -> AttachmentType.AUDIO
        isDocumentFile(filename) -> AttachmentType.DOCUMENT
        else -> AttachmentType.FILE
        }
    }
    
    /**
 * Format file size to human readable string
 * I-format ang file size sa human readable string
 */
fun formatFileSize(sizeBytes: Long): String {
    return when {
        sizeBytes < 1024 -> "$sizeBytes B"
        sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
        sizeBytes < 1024 * 1024 * 1024 -> "${sizeBytes / (1024 * 1024)} MB"
        else -> "${sizeBytes / (1024 * 1024 * 1024)} GB"
    }
}

/**
 * Validate message content
 * I-validate ang message content
 */
fun validateMessageContent(content: String): Boolean {
    return content.trim().isNotEmpty() && content.length <= 5000
}

/**
 * Format timestamp for message display
 * I-format ang timestamp para sa message display
 */
fun formatMessageTime(timestamp: java.util.Date): String {
    val now = System.currentTimeMillis()
    val messageTime = timestamp.time
    val diff = now - messageTime
    
    return when {
        diff < 60_000 -> "Just now" // Less than 1 minute
        diff < 3600_000 -> "${diff / 60_000}m ago" // Less than 1 hour
        diff < 86400_000 -> "${diff / 3600_000}h ago" // Less than 1 day
        diff < 604800_000 -> "${diff / 86400_000}d ago" // Less than 1 week
        else -> {
            val date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            date.format(timestamp)
        }
    }
}

/**
 * Get MIME type from file extension
 * Kunin ang MIME type mula sa file extension
 */
fun getMimeTypeFromExtension(filename: String): String {
    return when (getFileExtension(filename).lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "bmp" -> "image/bmp"
        "mp4" -> "video/mp4"
        "avi" -> "video/avi"
        "mov" -> "video/quicktime"
        "wmv" -> "video/x-ms-wmv"
        "flv" -> "video/x-flv"
        "webm" -> "video/webm"
        "mkv" -> "video/x-matroska"
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "aac" -> "audio/aac"
        "ogg" -> "audio/ogg"
        "m4a" -> "audio/mp4"
        "flac" -> "audio/flac"
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "txt" -> "text/plain"
        "rtf" -> "application/rtf"
        "odt" -> "application/vnd.oasis.opendocument.text"
        "zip" -> "application/zip"
        "rar" -> "application/x-rar-compressed"
        "7z" -> "application/x-7z-compressed"
        else -> "application/octet-stream"
    }
}