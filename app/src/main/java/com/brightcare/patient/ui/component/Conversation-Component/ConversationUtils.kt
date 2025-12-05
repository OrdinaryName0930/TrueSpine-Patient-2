package com.brightcare.patient.ui.component.conversationcomponent

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoLibrary
import java.util.*

/**
 * Utility functions for conversation components and Firestore operations
 * Mga utility function para sa conversation components at Firestore operations
 */

/**
 * Get sample conversation by ID (temporary implementation)
 * Kumuha ng sample conversation gamit ang ID (temporary implementation)
 */
fun getSampleConversationById(conversationId: String): ChatConversation {
    val conversations = getSampleConversations()
    return conversations.find { it.id == conversationId } ?: conversations.first()
}

/**
 * Get sample conversations (temporary implementation)
 * Kumuha ng sample conversations (temporary implementation)
 */
fun getSampleConversations(): List<ChatConversation> {
    val calendar = Calendar.getInstance()
    return listOf(
        ChatConversation(
            id = "1",
            participantName = "Dr. Maria Santos",
            participantType = SenderType.DOCTOR,
            participantId = "doctor_maria",
            lastMessage = "Your next appointment is confirmed for tomorrow at 10 AM.",
            lastMessageTime = calendar.apply { add(Calendar.MINUTE, -30) }.time,
            unreadCount = 1,
            isOnline = true,
            conversationType = ConversationType.DIRECT
        ),
        ChatConversation(
            id = "2",
            participantName = "Dr. John Reyes",
            participantType = SenderType.DOCTOR,
            participantId = "doctor_john",
            lastMessage = "The X-ray results look good. Continue with the prescribed exercises.",
            lastMessageTime = calendar.apply { add(Calendar.HOUR, -2) }.time,
            unreadCount = 0,
            isOnline = false,
            lastSeenTime = calendar.apply { add(Calendar.MINUTE, -45) }.time,
            conversationType = ConversationType.DIRECT
        ),
        ChatConversation(
            id = "3",
            participantName = "BrightCare Support",
            participantType = SenderType.ADMIN,
            participantId = "support_team",
            lastMessage = "Thank you for your feedback. We'll improve our services based on your suggestions.",
            lastMessageTime = calendar.apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
            unreadCount = 2,
            isOnline = true,
            conversationType = ConversationType.DIRECT
        )
    )
}

/**
 * Get sample messages for a conversation (temporary implementation)
 * Kumuha ng sample messages para sa conversation (temporary implementation)
 */
fun getSampleMessages(conversationId: String): List<ChatMessage> {
    val calendar = Calendar.getInstance()
    
    return when (conversationId) {
        "1" -> listOf(
            ChatMessage(
                id = "msg1",
                senderId = "doctor_maria",
                senderName = "Dr. Maria Santos",
                senderType = SenderType.DOCTOR,
                message = "Good morning! How are you feeling today?",
                timestamp = calendar.apply { add(Calendar.HOUR, -2) }.time,
                isRead = true
            ),
            ChatMessage(
                id = "msg2",
                senderId = "current_user",
                senderName = "You",
                senderType = SenderType.PATIENT,
                message = "Hi Doctor! I'm feeling much better, thank you. The medication is working well.",
                timestamp = calendar.apply { add(Calendar.MINUTE, 5) }.time,
                isRead = true
            ),
            ChatMessage(
                id = "msg3",
                senderId = "current_user",
                senderName = "You",
                senderType = SenderType.PATIENT,
                message = "Here's the X-ray you requested",
                timestamp = calendar.apply { add(Calendar.MINUTE, 10) }.time,
                isRead = true,
                attachments = listOf(
                    MessageAttachment(
                        id = "att1",
                        name = "xray_chest_2024.jpg",
                        url = "https://example.com/xray.jpg",
                        type = AttachmentType.IMAGE,
                        size = 1024000,
                        mimeType = "image/jpeg"
                    )
                )
            ),
            ChatMessage(
                id = "msg4",
                senderId = "doctor_maria",
                senderName = "Dr. Maria Santos",
                senderType = SenderType.DOCTOR,
                message = "Perfect! The X-ray looks clear. Your next appointment is confirmed for tomorrow at 10 AM. Please arrive 15 minutes early.",
                timestamp = calendar.apply { add(Calendar.MINUTE, 2) }.time,
                isRead = false
            )
        )
        "2" -> listOf(
            ChatMessage(
                id = "msg5",
                senderId = "doctor_john",
                senderName = "Dr. John Reyes",
                senderType = SenderType.DOCTOR,
                message = "Hello! I've reviewed your X-ray results.",
                timestamp = calendar.apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
                isRead = true
            ),
            ChatMessage(
                id = "msg6",
                senderId = "doctor_john",
                senderName = "Dr. John Reyes",
                senderType = SenderType.DOCTOR,
                message = "The X-ray results look good. Continue with the prescribed exercises.",
                timestamp = calendar.apply { add(Calendar.MINUTE, 1) }.time,
                isRead = true,
                attachments = listOf(
                    MessageAttachment(
                        id = "att2",
                        name = "exercise_plan.pdf",
                        url = "https://example.com/exercise_plan.pdf",
                        type = AttachmentType.FILE,
                        size = 512000,
                        mimeType = "application/pdf"
                    )
                )
            ),
            ChatMessage(
                id = "msg7",
                senderId = "current_user",
                senderName = "You",
                senderType = SenderType.PATIENT,
                message = "Thank you doctor! Should I continue with the same routine?",
                timestamp = calendar.apply { add(Calendar.MINUTE, 30) }.time,
                isRead = true
            )
        )
        "3" -> listOf(
            ChatMessage(
                id = "msg8",
                senderId = "support_team",
                senderName = "BrightCare Support",
                senderType = SenderType.ADMIN,
                message = "Hello! Thank you for contacting BrightCare support. How can we help you today?",
                timestamp = calendar.apply { add(Calendar.DAY_OF_MONTH, -3) }.time,
                isRead = true
            ),
            ChatMessage(
                id = "msg9",
                senderId = "current_user",
                senderName = "You",
                senderType = SenderType.PATIENT,
                message = "Hi! I have some feedback about the app's user interface.",
                timestamp = calendar.apply { add(Calendar.MINUTE, 10) }.time,
                isRead = true
            ),
            ChatMessage(
                id = "msg10",
                senderId = "support_team",
                senderName = "BrightCare Support",
                senderType = SenderType.ADMIN,
                message = "Thank you for your feedback. We'll improve our services based on your suggestions.",
                timestamp = calendar.apply { add(Calendar.MINUTE, 5) }.time,
                isRead = false
            )
        )
        else -> listOf(
            ChatMessage(
                id = "msg11",
                senderId = "doctor_ana",
                senderName = "Dr. Ana Cruz",
                senderType = SenderType.DOCTOR,
                message = "Please follow the home care instructions I sent you.",
                timestamp = calendar.apply { add(Calendar.WEEK_OF_YEAR, -1) }.time,
                isRead = true
            )
        )
    }
}

/**
 * Firestore operations class (placeholder for future implementation)
 * Firestore operations class (placeholder para sa future implementation)
 */
class ConversationFirestoreManager {
    
    /**
     * Send message to Firestore
     * Magpadala ng message sa Firestore
     */
    suspend fun sendMessage(
        conversationId: String,
        message: ChatMessage
    ): Result<ChatMessage> {
        return try {
            // TODO: Implement Firestore message sending
            /*
            val db = FirebaseFirestore.getInstance()
            val messageData = hashMapOf(
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "senderType" to message.senderType.name,
                "message" to message.message,
                "timestamp" to message.timestamp,
                "isRead" to message.isRead,
                "attachments" to message.attachments.map { attachment ->
                    hashMapOf(
                        "id" to attachment.id,
                        "name" to attachment.name,
                        "url" to attachment.url,
                        "type" to attachment.type.name,
                        "size" to attachment.size,
                        "mimeType" to attachment.mimeType
                    )
                }
            )
            
            db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .document(message.id)
                .set(messageData)
                .await()
            */
            
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload attachment to Firebase Storage
     * Mag-upload ng attachment sa Firebase Storage
     */
    suspend fun uploadAttachment(
        conversationId: String,
        attachmentData: ByteArray,
        fileName: String,
        mimeType: String,
        onProgress: (Float) -> Unit = {}
    ): Result<MessageAttachment> {
        return try {
            // TODO: Implement Firebase Storage upload
            /*
            val storageRef = FirebaseStorage.getInstance().reference
            val attachmentRef = storageRef.child("conversations/$conversationId/attachments/$fileName")
            
            val uploadTask = attachmentRef.putBytes(attachmentData)
            
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
                onProgress(progress / 100f)
            }
            
            uploadTask.await()
            val downloadUrl = attachmentRef.downloadUrl.await()
            */
            
            // Simulate successful upload
            val attachment = MessageAttachment(
                id = "att_${System.currentTimeMillis()}",
                name = fileName,
                url = "https://example.com/$fileName",
                type = when {
                    mimeType.startsWith("image/") -> AttachmentType.IMAGE
                    mimeType.startsWith("video/") -> AttachmentType.VIDEO
                    mimeType.startsWith("audio/") -> AttachmentType.AUDIO
                    else -> AttachmentType.FILE
                },
                size = attachmentData.size.toLong(),
                mimeType = mimeType
            )
            
            Result.success(attachment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Load messages from Firestore
     * Mag-load ng messages mula sa Firestore
     */
    suspend fun loadMessages(
        conversationId: String,
        limit: Int = 50,
        lastMessageId: String? = null
    ): Result<List<ChatMessage>> {
        return try {
            // TODO: Implement Firestore message loading
            /*
            val db = FirebaseFirestore.getInstance()
            var query = db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(limit.toLong())
            
            if (lastMessageId != null) {
                val lastDoc = db.collection("conversations")
                    .document(conversationId)
                    .collection("messages")
                    .document(lastMessageId)
                    .get()
                    .await()
                query = query.startAfter(lastDoc)
            }
            
            val snapshot = query.get().await()
            val messages = snapshot.documents.mapNotNull { doc ->
                // Convert Firestore document to ChatMessage
                // ...
            }
            */
            
            // Return sample data for now
            Result.success(getSampleMessages(conversationId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark messages as read
     * I-mark ang mga messages bilang nabasa
     */
    suspend fun markMessagesAsRead(
        conversationId: String,
        messageIds: List<String>
    ): Result<Unit> {
        return try {
            // TODO: Implement Firestore read status update
            /*
            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()
            
            messageIds.forEach { messageId ->
                val messageRef = db.collection("conversations")
                    .document(conversationId)
                    .collection("messages")
                    .document(messageId)
                batch.update(messageRef, "isRead", true)
            }
            
            batch.commit().await()
            */
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Utility functions for message formatting
 * Mga utility function para sa message formatting
 */

/**
 * Format timestamp to readable format
 * I-format ang timestamp sa readable format
 */
fun formatMessageTime(timestamp: Date): String {
    val now = Date()
    val diff = now.time - timestamp.time
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d ago"
        else -> java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp)
    }
}

/**
 * Format file size to human readable format
 * I-format ang file size sa human readable format
 */
fun formatFileSize(sizeInBytes: Long): String {
    if (sizeInBytes < 1024) return "${sizeInBytes}B"
    if (sizeInBytes < 1024 * 1024) return "${"%.1f".format(sizeInBytes / 1024.0)}KB"
    if (sizeInBytes < 1024 * 1024 * 1024) return "${"%.1f".format(sizeInBytes / (1024.0 * 1024.0))}MB"
    return "${"%.1f".format(sizeInBytes / (1024.0 * 1024.0 * 1024.0))}GB"
}

/**
 * Get file type icon based on mime type
 * Kumuha ng file type icon base sa mime type
 */
fun getFileTypeIcon(mimeType: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        mimeType.startsWith("image/") -> Icons.Filled.Image
        mimeType.startsWith("video/") -> Icons.Filled.VideoLibrary
        mimeType.startsWith("audio/") -> Icons.Filled.AudioFile
        mimeType == "application/pdf" -> Icons.Filled.PictureAsPdf
        mimeType.contains("document") || mimeType.contains("word") -> Icons.Filled.Description
        mimeType.contains("spreadsheet") || mimeType.contains("excel") -> Icons.Filled.Assessment
        else -> Icons.Filled.AttachFile
    }
}