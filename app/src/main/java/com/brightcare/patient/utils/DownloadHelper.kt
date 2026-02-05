package com.brightcare.patient.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.brightcare.patient.ui.component.conversationcomponent.MessageAttachment
import java.io.File

/**
 * Helper class for downloading files and images
 * Helper class para sa pag-download ng mga file at image
 */
object DownloadHelper {
    
    /**
     * Download file using Android DownloadManager
     * I-download ang file gamit ang Android DownloadManager
     */
    fun downloadFile(
        context: Context,
        attachment: MessageAttachment,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            
            val request = DownloadManager.Request(Uri.parse(attachment.url)).apply {
                setTitle(attachment.name)
                setDescription("Downloading ${attachment.name}")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                
                // Set destination
                val fileName = sanitizeFileName(attachment.name)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                
                // Set MIME type if available
                if (attachment.mimeType.isNotEmpty()) {
                    setMimeType(attachment.mimeType)
                }
                
                // Allow download over mobile and WiFi
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setAllowedOverRoaming(false)
            }
            
            val downloadId = downloadManager.enqueue(request)
            
            Toast.makeText(
                context,
                "Download started: ${attachment.name}",
                Toast.LENGTH_SHORT
            ).show()
            
            onSuccess()
            
        } catch (e: Exception) {
            val errorMessage = "Failed to download file: ${e.message}"
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            onError(errorMessage)
        }
    }
    
    /**
     * Open file with appropriate app
     * Buksan ang file gamit ang appropriate na app
     */
    fun openFile(
        context: Context,
        attachment: MessageAttachment,
        onError: (String) -> Unit = {}
    ) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(attachment.url)
                
                // Set MIME type if available
                if (attachment.mimeType.isNotEmpty()) {
                    setDataAndType(Uri.parse(attachment.url), attachment.mimeType)
                } else {
                    // Try to determine MIME type from file extension
                    val mimeType = getMimeTypeFromUrl(attachment.url) ?: "application/octet-stream"
                    setDataAndType(Uri.parse(attachment.url), mimeType)
                }
                
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // No app can handle this file type, try to open in browser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(attachment.url))
                context.startActivity(browserIntent)
            }
            
        } catch (e: Exception) {
            val errorMessage = "Cannot open file: ${e.message}"
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            onError(errorMessage)
        }
    }
    
    /**
     * Share file URL
     * I-share ang file URL
     */
    fun shareFile(
        context: Context,
        attachment: MessageAttachment
    ) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, attachment.url)
                putExtra(Intent.EXTRA_SUBJECT, attachment.name)
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, "Share ${attachment.name}")
            context.startActivity(chooserIntent)
            
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to share file: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Sanitize filename for download
     * I-sanitize ang filename para sa download
     */
    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
    
    /**
     * Get MIME type from URL
     * Kunin ang MIME type mula sa URL
     */
    private fun getMimeTypeFromUrl(url: String): String? {
        return try {
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if file is downloadable
     * I-check kung ang file ay downloadable
     */
    fun isDownloadable(attachment: MessageAttachment): Boolean {
        return attachment.url.isNotEmpty() && 
               (attachment.url.startsWith("http://") || attachment.url.startsWith("https://"))
    }
    
    /**
     * Get file size string
     * Kunin ang file size string
     */
    fun getFileSizeString(sizeBytes: Long): String {
        return when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            sizeBytes < 1024 * 1024 * 1024 -> "${sizeBytes / (1024 * 1024)} MB"
            else -> "${sizeBytes / (1024 * 1024 * 1024)} GB"
        }
    }
}














