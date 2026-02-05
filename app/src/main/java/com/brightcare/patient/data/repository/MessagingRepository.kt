package com.brightcare.patient.data.repository

import android.net.Uri
import com.brightcare.patient.data.model.*
import com.brightcare.patient.data.storage.FirebaseStorageHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for messaging functionality
 * Repository para sa messaging functionality
 */
@Singleton
class MessagingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storageHelper: FirebaseStorageHelper
) {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val CONVERSATIONS_COLLECTION = "conversations"
        private const val MESSAGES_COLLECTION = "messages"
        private const val CHIROPRACTORS_COLLECTION = "chiropractors"
        private const val PATIENTS_COLLECTION = "patients"
    }

    /**
     * Get current user ID
     * Kunin ang current user ID
     */
    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Get assigned chiropractor for current patient
     * Kunin ang assigned chiropractor para sa current patient
     */
    suspend fun getAssignedChiropractor(): Result<Chiropractor?> {
        return try {
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            // Get patient document
            val patientDoc = firestore.collection(PATIENTS_COLLECTION)
                .document(currentUserId)
                .get()
                .await()

            val patient = patientDoc.toObject<Patient>()
            val chiropractorId = patient?.assignedChiropractorId

            if (chiropractorId != null) {
                // Get chiropractor details
                val chiropractorDoc = firestore.collection(CHIROPRACTORS_COLLECTION)
                    .document(chiropractorId)
                    .get()
                    .await()

                val chiropractor = chiropractorDoc.toObject<Chiropractor>()
                Result.success(chiropractor)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create or find conversation between patient and chiropractor
     * Gumawa o hanapin ang conversation sa pagitan ng patient at chiropractor
     */
    suspend fun createOrFindConversation(chiropractorId: String): Result<Conversation> {
        return try {
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            // Check if conversation already exists
            val existingConversations = firestore.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participantIds", currentUserId)
                .get()
                .await()

            val existingConversation = existingConversations.documents
                .mapNotNull { it.toObject<Conversation>() }
                .find { it.participantIds.contains(chiropractorId) }

            if (existingConversation != null) {
                return Result.success(existingConversation)
            }

            // Create new conversation
            val conversationId = UUID.randomUUID().toString()
            
            // Get participant details
            val patientDoc = firestore.collection(PATIENTS_COLLECTION)
                .document(currentUserId)
                .get()
                .await()
            val patient = patientDoc.toObject<Patient>()

            val chiropractorDoc = firestore.collection(CHIROPRACTORS_COLLECTION)
                .document(chiropractorId)
                .get()
                .await()
            val chiropractor = chiropractorDoc.toObject<Chiropractor>()

            val conversation = Conversation(
                id = conversationId,
                participantIds = listOf(currentUserId, chiropractorId),
                participantNames = mapOf(
                    currentUserId to (patient?.name ?: "Patient"),
                    chiropractorId to (chiropractor?.name ?: "Chiropractor")
                ),
                participantTypes = mapOf(
                    currentUserId to UserType.PATIENT,
                    chiropractorId to UserType.CHIROPRACTOR
                ),
                unreadCounts = mapOf(
                    currentUserId to 0,
                    chiropractorId to 0
                )
            )

            // Save to Firestore
            firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .set(conversation.toMap())
                .await()

            Result.success(conversation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get conversations for current user
     * Kunin ang mga conversation para sa current user
     */
    fun getConversations(): Flow<List<Conversation>> = callbackFlow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }

        val listener = firestore.collection(CONVERSATIONS_COLLECTION)
            .whereArrayContains("participantIds", currentUserId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Conversation>()?.copy(id = doc.id)
                } ?: emptyList()

                trySend(conversations)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get messages for a conversation with real-time updates
     * Kunin ang mga mensahe para sa conversation na may real-time updates
     */
    fun getMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .collection(MESSAGES_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Message>()?.copy(id = doc.id)
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Send text message
     * Magpadala ng text message
     */
    suspend fun sendTextMessage(
        conversationId: String,
        receiverId: String,
        content: String,
        replyToMessageId: String? = null
    ): Result<Message> {
        return try {
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            val messageId = UUID.randomUUID().toString()
            val message = Message(
                id = messageId,
                senderId = currentUserId,
                receiverId = receiverId,
                conversationId = conversationId,
                type = MessageType.TEXT,
                content = content,
                replyToMessageId = replyToMessageId
            )

            // Save message
            firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .document(messageId)
                .set(message.toMap())
                .await()

            // Update conversation
            updateConversationLastMessage(conversationId, message)

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send image message
     * Magpadala ng image message
     */
    suspend fun sendImageMessage(
        conversationId: String,
        receiverId: String,
        imageUri: Uri,
        fileName: String? = null
    ): Flow<MessageWithStatus> = callbackFlow {
        try {
            val currentUserId = getCurrentUserId() 
                ?: throw Exception("User not authenticated")

            val messageId = UUID.randomUUID().toString()
            val actualFileName = fileName ?: "image_${System.currentTimeMillis()}.jpg"

            // Create initial message
            val message = Message(
                id = messageId,
                senderId = currentUserId,
                receiverId = receiverId,
                conversationId = conversationId,
                type = MessageType.IMAGE,
                content = actualFileName,
                fileName = actualFileName
            )

            // Send initial status
            trySend(MessageWithStatus(message, MessageStatus.SENDING))

            // Upload image with progress
            storageHelper.uploadImage(conversationId, imageUri, actualFileName)
                .collect { progress ->
                    if (progress.isComplete && progress.downloadUrl != null) {
                        // Update message with file URL
                        val updatedMessage = message.copy(
                            fileUrl = progress.downloadUrl,
                            fileSize = 0L // You might want to get actual file size
                        )

                        // Save to Firestore
                        firestore.collection(CONVERSATIONS_COLLECTION)
                            .document(conversationId)
                            .collection(MESSAGES_COLLECTION)
                            .document(messageId)
                            .set(updatedMessage.toMap())
                            .await()

                        // Update conversation
                        updateConversationLastMessage(conversationId, updatedMessage)

                        trySend(MessageWithStatus(updatedMessage, MessageStatus.SENT))
                        close()
                    } else if (progress.error != null) {
                        trySend(MessageWithStatus(message, MessageStatus.FAILED))
                        close(Exception(progress.error))
                    } else {
                        trySend(MessageWithStatus(
                            message, 
                            MessageStatus.SENDING,
                            progress
                        ))
                    }
                }
        } catch (e: Exception) {
            val errorMessage = Message(
                senderId = getCurrentUserId() ?: "",
                receiverId = receiverId,
                conversationId = conversationId,
                type = MessageType.IMAGE,
                content = "Failed to send image"
            )
            trySend(MessageWithStatus(errorMessage, MessageStatus.FAILED))
            close(e)
        }

        awaitClose { }
    }

    /**
     * Send file message
     * Magpadala ng file message
     */
    suspend fun sendFileMessage(
        conversationId: String,
        receiverId: String,
        fileUri: Uri,
        fileName: String,
        mimeType: String
    ): Flow<MessageWithStatus> = callbackFlow {
        try {
            val currentUserId = getCurrentUserId() 
                ?: throw Exception("User not authenticated")

            val messageId = UUID.randomUUID().toString()

            // Create initial message
            val message = Message(
                id = messageId,
                senderId = currentUserId,
                receiverId = receiverId,
                conversationId = conversationId,
                type = MessageType.FILE,
                content = fileName,
                fileName = fileName,
                mimeType = mimeType
            )

            // Send initial status
            trySend(MessageWithStatus(message, MessageStatus.SENDING))

            // Upload file with progress
            storageHelper.uploadFile(conversationId, fileUri, fileName, mimeType)
                .collect { progress ->
                    if (progress.isComplete && progress.downloadUrl != null) {
                        // Update message with file URL
                        val updatedMessage = message.copy(
                            fileUrl = progress.downloadUrl,
                            fileSize = 0L // You might want to get actual file size
                        )

                        // Save to Firestore
                        firestore.collection(CONVERSATIONS_COLLECTION)
                            .document(conversationId)
                            .collection(MESSAGES_COLLECTION)
                            .document(messageId)
                            .set(updatedMessage.toMap())
                            .await()

                        // Update conversation
                        updateConversationLastMessage(conversationId, updatedMessage)

                        trySend(MessageWithStatus(updatedMessage, MessageStatus.SENT))
                        close()
                    } else if (progress.error != null) {
                        trySend(MessageWithStatus(message, MessageStatus.FAILED))
                        close(Exception(progress.error))
                    } else {
                        trySend(MessageWithStatus(
                            message, 
                            MessageStatus.SENDING,
                            progress
                        ))
                    }
                }
        } catch (e: Exception) {
            val errorMessage = Message(
                senderId = getCurrentUserId() ?: "",
                receiverId = receiverId,
                conversationId = conversationId,
                type = MessageType.FILE,
                content = "Failed to send file"
            )
            trySend(MessageWithStatus(errorMessage, MessageStatus.FAILED))
            close(e)
        }

        awaitClose { }
    }

    /**
     * Mark message as read
     * Markahan ang mensahe bilang nabasa
     */
    suspend fun markMessageAsRead(conversationId: String, messageId: String): Result<Unit> {
        return try {
            firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .document(messageId)
                .update("isRead", true)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mark all messages as read in conversation
     * Markahan ang lahat ng mensahe bilang nabasa sa conversation
     */
    suspend fun markAllMessagesAsRead(conversationId: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            // Get unread messages
            val unreadMessages = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            // Batch update
            val batch = firestore.batch()
            unreadMessages.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()

            // Update conversation unread count
            firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .update("unreadCounts.$currentUserId", 0)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete message
     * Tanggalin ang mensahe
     */
    suspend fun deleteMessage(conversationId: String, messageId: String): Result<Unit> {
        return try {
            // Get message first to check if it has files
            val messageDoc = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .document(messageId)
                .get()
                .await()

            val message = messageDoc.toObject<Message>()
            
            // Delete file from storage if exists
            message?.fileUrl?.let { fileUrl ->
                storageHelper.deleteFile(fileUrl)
            }

            // Delete message from Firestore
            firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .document(messageId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update conversation last message
     * I-update ang huling mensahe ng conversation
     */
    private suspend fun updateConversationLastMessage(conversationId: String, message: Message) {
        try {
            val updates = mapOf(
                "lastMessage" to message.content,
                "lastMessageType" to message.type.name,
                "lastMessageTimestamp" to message.timestamp,
                "lastMessageSenderId" to message.senderId,
                "updatedAt" to message.timestamp,
                "unreadCounts.${message.receiverId}" to com.google.firebase.firestore.FieldValue.increment(1)
            )

            firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .update(updates)
                .await()
        } catch (e: Exception) {
            // Log error but don't fail the message sending
            e.printStackTrace()
        }
    }

    /**
     * Get chiropractor phone number for calling
     * Kunin ang phone number ng chiropractor para sa pagtawag
     */
    suspend fun getChiropractorPhoneNumber(chiropractorId: String): Result<String> {
        return try {
            val chiropractorDoc = firestore.collection(CHIROPRACTORS_COLLECTION)
                .document(chiropractorId)
                .get()
                .await()

            // Get contactNumber from Firestore document directly
            val contactNumber = chiropractorDoc.getString("contactNumber")

            if (contactNumber != null && contactNumber.isNotEmpty()) {
                Result.success(contactNumber)
            } else {
                Result.failure(Exception("Phone number not available"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

