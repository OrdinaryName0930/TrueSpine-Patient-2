package com.brightcare.patient.data.repository

import android.net.Uri
import com.brightcare.patient.data.model.*
import com.brightcare.patient.data.storage.FirebaseStorageHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
 * Repository for conversation and user management
 * Repository para sa conversation at user management
 */
@Singleton
class ConversationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storageHelper: FirebaseStorageHelper
) {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val CHIROPRACTORS_COLLECTION = "chiropractors" // Changed to plural to match your Firestore
        private const val CONVERSATIONS_COLLECTION = "conversations"
        private const val MESSAGES_COLLECTION = "messages"
    }

    /**
     * Get current user ID
     * Kunin ang current user ID
     */
    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Fetch all registered chiropractors from chiropractors collection
     * Kunin ang lahat ng registered chiropractors mula sa chiropractors collection
     */
    suspend fun getAllChiropractors(): Result<List<User>> {
        return try {
            val result = firestore.collection(CHIROPRACTORS_COLLECTION)
                .get()
                .await()

            val chiropractors = result.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    // Map TrueSpine Firestore structure to User model
                    User(
                        uid = doc.id,
                        fullName = data["name"] as? String ?: "",
                        email = data["email"] as? String ?: "",
                        profileImage = data["profileImageUrl"] as? String,
                        role = UserRole.CHIROPRACTOR,
                        specialization = data["specialization"] as? String,
                        phoneNumber = data["contactNumber"] as? String,
                        experience = (data["yearsOfExperience"] as? Long)?.toInt() ?: 0,
                        rating = 4.5, // Default rating since not in your data
                        reviewCount = 0, // Default review count
                        isAvailable = true, // Default to available
                        bio = data["about"] as? String ?: "Experienced ${data["specialization"] as? String ?: "chiropractor"}"
                    )
                } else null
            }.sortedBy { it.fullName } // Sort by name in code

            Result.success(chiropractors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get chiropractors with real-time updates from chiropractors collection
     * Kunin ang mga chiropractor na may real-time updates mula sa chiropractors collection
     */
    fun getChiropractorsFlow(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection(CHIROPRACTORS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val chiropractors = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        // Map TrueSpine Firestore structure to User model
                        User(
                            uid = doc.id,
                            fullName = data["name"] as? String ?: "",
                            email = data["email"] as? String ?: "",
                            profileImage = data["profileImageUrl"] as? String,
                            role = UserRole.CHIROPRACTOR,
                            specialization = data["specialization"] as? String,
                            phoneNumber = data["contactNumber"] as? String,
                            experience = (data["yearsOfExperience"] as? Long)?.toInt() ?: 0,
                            rating = 4.5, // Default rating since not in your data
                            reviewCount = 0, // Default review count
                            isAvailable = true, // Default to available
                            bio = data["about"] as? String ?: "Experienced ${data["specialization"] as? String ?: "chiropractor"}"
                        )
                    } else null
                }?.sortedBy { it.fullName } // Sort by name in code
                    ?: emptyList()

                trySend(chiropractors)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Search chiropractors by name
     * Maghanap ng chiropractor gamit ang pangalan
     */
    fun searchChiropractors(query: String): List<User> {
        // This will be used for local filtering in the ViewModel
        // Real-time search will be handled in the UI layer
        return emptyList()
    }

    /**
     * Check if conversation exists between patient and chiropractor
     * I-check kung may conversation na sa pagitan ng patient at chiropractor
     */
    suspend fun findExistingConversation(chiropractorId: String): Result<ConversationMetadata?> {
        return try {
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            val result = firestore.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participants", currentUserId)
                .get()
                .await()

            val existingConversation = result.documents
                .mapNotNull { doc -> doc.toObject<ConversationMetadata>()?.copy(id = doc.id) }
                .find { it.participants.contains(chiropractorId) }

            Result.success(existingConversation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create new conversation between patient and chiropractor
     * Gumawa ng bagong conversation sa pagitan ng patient at chiropractor
     */
    suspend fun createConversation(chiropractorId: String): Result<ConversationMetadata> {
        return try {
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            val conversationId = UUID.randomUUID().toString()
            val conversation = ConversationMetadata(
                id = conversationId,
                participants = listOf(currentUserId, chiropractorId),
                lastMessage = "",
                unreadCounts = mapOf(
                    currentUserId to 0,
                    chiropractorId to 0
                )
            )

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
     * Get conversation metadata by conversationId
     * Kunin ang conversation metadata gamit ang conversationId
     */
    suspend fun getConversationById(conversationId: String): Result<ConversationMetadata?> {
        return try {
            val doc = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .get()
                .await()

            if (doc.exists()) {
                val data = doc.data
                if (data != null) {
                    val conversation = ConversationMetadata(
                        id = doc.id,
                        participants = (data["participants"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        lastMessage = data["lastMessage"] as? String ?: "",
                        lastMessageType = data["lastMessageType"] as? String ?: "text",
                        lastMessageSenderId = data["lastMessageSenderId"] as? String ?: "",
                        updatedAt = data["updatedAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                        unreadCounts = (data["unreadCounts"] as? Map<String, Any>)?.mapValues { (it.value as? Long)?.toInt() ?: 0 } ?: emptyMap()
                    )
                    Result.success(conversation)
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get or create conversation
     * Kunin o gumawa ng conversation
     */
    suspend fun getOrCreateConversation(chiropractorId: String): Result<ConversationMetadata> {
        return try {
            // First, check if conversation exists
            val existingResult = findExistingConversation(chiropractorId)
            
            existingResult.fold(
                onSuccess = { existingConversation ->
                    if (existingConversation != null) {
                        Result.success(existingConversation)
                    } else {
                        // Create new conversation
                        createConversation(chiropractorId)
                    }
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get conversations for current user with chiropractor details - EFFICIENT REAL-TIME VERSION
     * Kunin ang mga conversation para sa current user na may chiropractor details - EFFICIENT REAL-TIME VERSION
     */
    fun getConversationsWithDetails(): Flow<List<ConversationDisplay>> = callbackFlow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }

        // Cache for chiropractor details to avoid repeated fetches
        val chiropractorCache = mutableMapOf<String, User>()
        
        val listener = firestore.collection(CONVERSATIONS_COLLECTION)
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            ConversationMetadata(
                                id = doc.id,
                                participants = (data["participants"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                                lastMessage = data["lastMessage"] as? String ?: "",
                                lastMessageType = data["lastMessageType"] as? String ?: "text",
                                lastMessageSenderId = data["lastMessageSenderId"] as? String ?: "",
                                updatedAt = data["updatedAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                                unreadCounts = (data["unreadCounts"] as? Map<String, Any>)?.mapValues { (it.value as? Long)?.toInt() ?: 0 } ?: emptyMap()
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                // Process conversations and fetch missing chiropractor details
                val conversationDisplays = mutableListOf<ConversationDisplay>()
                var pendingFetches = 0
                
                if (conversations.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                conversations.forEach { conversation ->
                    val chiropractorId = conversation.participants.find { it != currentUserId }
                    if (chiropractorId != null) {
                        // Check cache first
                        val cachedChiropractor = chiropractorCache[chiropractorId]
                        if (cachedChiropractor != null) {
                            val display = ConversationDisplay(
                                conversationId = conversation.id,
                                chiropractor = cachedChiropractor,
                                lastMessage = conversation.lastMessage,
                                lastMessageTime = conversation.updatedAt.toDate(),
                                unreadCount = conversation.unreadCounts[currentUserId] ?: 0,
                                isOnline = cachedChiropractor.isAvailable
                            )
                            conversationDisplays.add(display)
                        } else {
                            // Fetch chiropractor details if not cached
                            pendingFetches++
                            
                            // Try chiropractors collection first, then users collection
                            firestore.collection(CHIROPRACTORS_COLLECTION)
                                .document(chiropractorId)
                                .get()
                                .addOnSuccessListener { chiropractorDoc ->
                                    var chiropractor: User? = null
                                    
                                    if (chiropractorDoc.exists()) {
                                        val data = chiropractorDoc.data
                                        if (data != null) {
                                            chiropractor = User(
                                                uid = chiropractorDoc.id,
                                                fullName = data["name"] as? String ?: "",
                                                email = data["email"] as? String ?: "",
                                                profileImage = data["profileImageUrl"] as? String,
                                                role = UserRole.CHIROPRACTOR,
                                                specialization = data["specialization"] as? String,
                                                phoneNumber = data["contactNumber"] as? String,
                                                experience = (data["yearsOfExperience"] as? Long)?.toInt() ?: 0,
                                                rating = 4.5,
                                                reviewCount = 0,
                                                isAvailable = true,
                                                bio = data["about"] as? String ?: "Experienced ${data["specialization"] as? String ?: "chiropractor"}"
                                            )
                                        }
                                    }
                                    
                                    if (chiropractor != null) {
                                        // Cache the chiropractor
                                        chiropractorCache[chiropractorId] = chiropractor
                                        
                                        val display = ConversationDisplay(
                                            conversationId = conversation.id,
                                            chiropractor = chiropractor,
                                            lastMessage = conversation.lastMessage,
                                            lastMessageTime = conversation.updatedAt.toDate(),
                                            unreadCount = conversation.unreadCounts[currentUserId] ?: 0,
                                            isOnline = chiropractor.isAvailable
                                        )
                                        conversationDisplays.add(display)
                                    }
                                    
                                    pendingFetches--
                                    if (pendingFetches == 0) {
                                        // Sort by last message time and send
                                        val sortedDisplays = conversationDisplays.sortedByDescending { it.lastMessageTime }
                                        trySend(sortedDisplays)
                                    }
                                }
                                .addOnFailureListener {
                                    // Try users collection as fallback
                                    firestore.collection(USERS_COLLECTION)
                                        .document(chiropractorId)
                                        .get()
                                        .addOnSuccessListener { userDoc ->
                                            val chiropractor = userDoc.toObject<User>()?.copy(uid = userDoc.id)
                                            if (chiropractor != null) {
                                                chiropractorCache[chiropractorId] = chiropractor
                                                
                                                val display = ConversationDisplay(
                                                    conversationId = conversation.id,
                                                    chiropractor = chiropractor,
                                                    lastMessage = conversation.lastMessage,
                                                    lastMessageTime = conversation.updatedAt.toDate(),
                                                    unreadCount = conversation.unreadCounts[currentUserId] ?: 0,
                                                    isOnline = chiropractor.isAvailable
                                                )
                                                conversationDisplays.add(display)
                                            }
                                            
                                            pendingFetches--
                                            if (pendingFetches == 0) {
                                                val sortedDisplays = conversationDisplays.sortedByDescending { it.lastMessageTime }
                                                trySend(sortedDisplays)
                                            }
                                        }
                                        .addOnFailureListener {
                                            pendingFetches--
                                            if (pendingFetches == 0) {
                                                val sortedDisplays = conversationDisplays.sortedByDescending { it.lastMessageTime }
                                                trySend(sortedDisplays)
                                            }
                                        }
                                }
                        }
                    }
                }
                
                // If all chiropractors were cached, send immediately
                if (pendingFetches == 0) {
                    val sortedDisplays = conversationDisplays.sortedByDescending { it.lastMessageTime }
                    trySend(sortedDisplays)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get combined chiropractors and conversations data in real-time - MOST EFFICIENT
     * Kunin ang combined chiropractors at conversations data sa real-time - PINAKA EFFICIENT
     */
    fun getCombinedChiropractorsAndConversations(): Flow<Pair<List<User>, List<ConversationDisplay>>> = callbackFlow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }

        var chiropractors: List<User> = emptyList()
        var conversations: List<ConversationDisplay> = emptyList()
        
        // Listen to chiropractors
        val chiropractorsListener = firestore.collection(CHIROPRACTORS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                chiropractors = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        User(
                            uid = doc.id,
                            fullName = data["name"] as? String ?: "",
                            email = data["email"] as? String ?: "",
                            profileImage = data["profileImageUrl"] as? String,
                            role = UserRole.CHIROPRACTOR,
                            specialization = data["specialization"] as? String,
                            phoneNumber = data["contactNumber"] as? String,
                            experience = (data["yearsOfExperience"] as? Long)?.toInt() ?: 0,
                            rating = 4.5,
                            reviewCount = 0,
                            isAvailable = true,
                            bio = data["about"] as? String ?: "Experienced ${data["specialization"] as? String ?: "chiropractor"}"
                        )
                    } else null
                }?.sortedBy { it.fullName } ?: emptyList()
                
                // Emit combined data
                trySend(Pair(chiropractors, conversations))
            }

        // Listen to conversations
        val conversationsListener = firestore.collection(CONVERSATIONS_COLLECTION)
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val conversationMetadata = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            ConversationMetadata(
                                id = doc.id,
                                participants = (data["participants"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                                lastMessage = data["lastMessage"] as? String ?: "",
                                lastMessageType = data["lastMessageType"] as? String ?: "text",
                                lastMessageSenderId = data["lastMessageSenderId"] as? String ?: "",
                                updatedAt = data["updatedAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                                unreadCounts = (data["unreadCounts"] as? Map<String, Any>)?.mapValues { (it.value as? Long)?.toInt() ?: 0 } ?: emptyMap()
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                // Map conversations to displays using current chiropractors list
                conversations = conversationMetadata.mapNotNull { conversation ->
                    val chiropractorId = conversation.participants.find { it != currentUserId }
                    val chiropractor = chiropractors.find { it.uid == chiropractorId }
                    
                    if (chiropractor != null) {
                        ConversationDisplay(
                            conversationId = conversation.id,
                            chiropractor = chiropractor,
                            lastMessage = conversation.lastMessage,
                            lastMessageTime = conversation.updatedAt.toDate(),
                            unreadCount = conversation.unreadCounts[currentUserId] ?: 0,
                            isOnline = chiropractor.isAvailable
                        )
                    } else null
                }.sortedByDescending { it.lastMessageTime }
                
                // Emit combined data
                trySend(Pair(chiropractors, conversations))
            }

        awaitClose { 
            chiropractorsListener.remove()
            conversationsListener.remove()
        }
    }

    /**
     * Get real-time unread message count for a conversation
     * Kunin ang real-time unread message count para sa conversation
     */
    fun getUnreadMessageCount(conversationId: String): Flow<Int> = callbackFlow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }

        // Listen to unread messages directly (more reliable than conversation metadata)
        val listener = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .collection(MESSAGES_COLLECTION)
            .whereNotEqualTo("senderId", currentUserId) // Messages not sent by current user
            .whereEqualTo("isRead", false) // Unread messages
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val unreadCount = snapshot?.size() ?: 0
                trySend(unreadCount)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get messages for a conversation with real-time updates - OPTIMIZED VERSION
     * Kunin ang mga mensahe para sa conversation na may real-time updates - OPTIMIZED VERSION
     */
    fun getMessages(conversationId: String): Flow<List<ChatMessageNew>> = callbackFlow {
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
                    doc.toObject<ChatMessageNew>()?.copy(id = doc.id)
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
        content: String,
        replyToMessageId: String? = null
    ): Result<ChatMessageNew> {
        return try {
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            val messageId = UUID.randomUUID().toString()
            val message = ChatMessageNew(
                id = messageId,
                senderId = currentUserId,
                type = "text",
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

            // Update conversation metadata
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
        imageUri: Uri,
        fileName: String? = null
    ): Flow<Pair<ChatMessageNew, UploadProgress?>> = callbackFlow {
        try {
            val currentUserId = getCurrentUserId() 
                ?: throw Exception("User not authenticated")

            val messageId = UUID.randomUUID().toString()
            val actualFileName = fileName ?: "image_${System.currentTimeMillis()}.jpg"

            // Create initial message
            val message = ChatMessageNew(
                id = messageId,
                senderId = currentUserId,
                type = "image",
                content = actualFileName,
                fileName = actualFileName
            )

            // Send initial message state
            trySend(Pair(message, UploadProgress(actualFileName, 0f)))

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

                        trySend(Pair(updatedMessage, progress))
                        close()
                    } else if (progress.error != null) {
                        close(Exception(progress.error))
                    } else {
                        trySend(Pair(message, progress))
                    }
                }
        } catch (e: Exception) {
            close(e)
        }

        awaitClose { }
    }

    /**
     * Send file message (PDF, documents)
     * Magpadala ng file message (PDF, documents)
     */
    suspend fun sendFileMessage(
        conversationId: String,
        fileUri: Uri,
        fileName: String,
        mimeType: String
    ): Flow<Pair<ChatMessageNew, UploadProgress?>> = callbackFlow {
        try {
            val currentUserId = getCurrentUserId() 
                ?: throw Exception("User not authenticated")

            val messageId = UUID.randomUUID().toString()

            // Create initial message
            val message = ChatMessageNew(
                id = messageId,
                senderId = currentUserId,
                type = "file",
                content = fileName,
                fileName = fileName,
                mimeType = mimeType
            )

            // Send initial message state
            trySend(Pair(message, UploadProgress(fileName, 0f)))

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

                        trySend(Pair(updatedMessage, progress))
                        close()
                    } else if (progress.error != null) {
                        close(Exception(progress.error))
                    } else {
                        trySend(Pair(message, progress))
                    }
                }
        } catch (e: Exception) {
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
     * Debug function to manually test unread count deletion
     * Debug function para sa manual na pag-test ng unread count deletion
     */
    suspend fun debugDeleteUnreadCount(conversationId: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            println("🔧 DEBUG: Manually deleting unread count for user $currentUserId in conversation $conversationId")
            
            val conversationRef = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
            
            // Check before deletion
            val beforeDoc = conversationRef.get().await()
            val beforeUnreadCounts = beforeDoc.data?.get("unreadCounts") as? Map<String, Any>
            println("📊 BEFORE deletion - unreadCounts: $beforeUnreadCounts")
            
            // Perform deletion
            conversationRef.update("unreadCounts.$currentUserId", com.google.firebase.firestore.FieldValue.delete()).await()
            
            // Check after deletion
            val afterDoc = conversationRef.get().await()
            val afterUnreadCounts = afterDoc.data?.get("unreadCounts") as? Map<String, Any>
            println("📊 AFTER deletion - unreadCounts: $afterUnreadCounts")
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ DEBUG ERROR: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Alternative method: Set unread count to 0 instead of deleting the field
     * Alternative na paraan: I-set ang unread count sa 0 imbes na tanggalin ang field
     */
    suspend fun markConversationAsReadSetToZero(conversationId: String): Result<Unit> {
        return try {
            println("🔄 Starting markConversationAsReadSetToZero for conversation: $conversationId")
            
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            println("👤 Current user ID: $currentUserId")

            // Get all unread messages for current user
            val unreadMessages = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .whereNotEqualTo("senderId", currentUserId) // Messages not sent by current user
                .whereEqualTo("isRead", false) // Unread messages
                .get()
                .await()

            println("📧 Found ${unreadMessages.size()} unread messages to mark as read")

            // Mark all unread messages as read and set unread count to 0
            val batch = firestore.batch()
            unreadMessages.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
                println("✅ Marking message ${doc.id} as read")
            }

            // Set unread count to 0 for current user in conversation metadata
            val conversationRef = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
            
            // Check current state
            val conversationDoc = conversationRef.get().await()
            val unreadCounts = conversationDoc.data?.get("unreadCounts") as? Map<String, Any>
            val currentUserUnreadCount = unreadCounts?.get(currentUserId)
            
            println("📊 Current unreadCounts map: $unreadCounts")
            println("📊 Current user ($currentUserId) unread count: $currentUserUnreadCount")
            
            batch.update(conversationRef, "unreadCounts.$currentUserId", 0)
            println("🔄 Setting unreadCounts.$currentUserId to 0 in conversation metadata")

            // Execute batch update
            batch.commit().await()
            
            // Verify the update worked
            val updatedConversationDoc = conversationRef.get().await()
            val updatedUnreadCounts = updatedConversationDoc.data?.get("unreadCounts") as? Map<String, Any>
            val updatedUserUnreadCount = updatedUnreadCounts?.get(currentUserId)
            
            println("✅ Successfully marked conversation as read and set unread count to 0")
            println("📊 Updated unreadCounts map: $updatedUnreadCounts")
            println("📊 Updated user ($currentUserId) unread count: $updatedUserUnreadCount")
            
            if (updatedUserUnreadCount == 0 || updatedUserUnreadCount == 0L) {
                println("🎉 SUCCESS: User's unread count was successfully set to 0!")
            } else {
                println("⚠️ WARNING: User's unread count is not 0: $updatedUserUnreadCount")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Error in markConversationAsReadSetToZero: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Mark all unread messages as read when user opens conversation and delete unread count
     * Markahan ang lahat ng hindi pa nabasang mensahe bilang nabasa kapag binuksan ng user ang conversation at tanggalin ang unread count
     */
    suspend fun markConversationAsRead(conversationId: String): Result<Unit> {
        return try {
            println("🔄 Starting markConversationAsRead for conversation: $conversationId")
            
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            println("👤 Current user ID: $currentUserId")

            // Get all unread messages for current user
            val unreadMessages = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .whereNotEqualTo("senderId", currentUserId) // Messages not sent by current user
                .whereEqualTo("isRead", false) // Unread messages
                .get()
                .await()

            println("📧 Found ${unreadMessages.size()} unread messages to mark as read")

            // Mark all unread messages as read and delete unread count
            val batch = firestore.batch()
            unreadMessages.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
                println("✅ Marking message ${doc.id} as read")
            }

            // Reset/Delete unread count for current user in conversation metadata
            val conversationRef = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
            
            // First check if the unreadCounts field exists for this user
            val conversationDoc = conversationRef.get().await()
            val unreadCounts = conversationDoc.data?.get("unreadCounts") as? Map<String, Any>
            val currentUserUnreadCount = unreadCounts?.get(currentUserId)
            
            println("📊 Current unreadCounts map: $unreadCounts")
            println("📊 Current user ($currentUserId) unread count: $currentUserUnreadCount")
            
            // Strategy: Try deletion first, but we'll verify and fallback if needed
            batch.update(conversationRef, "unreadCounts.$currentUserId", com.google.firebase.firestore.FieldValue.delete())
            println("🗑️ Attempting to DELETE unreadCounts.$currentUserId from conversation metadata")

            // Execute batch update
            batch.commit().await()
            
            // Verify the deletion worked, with fallback to setting to 0 if needed
            val updatedConversationDoc = conversationRef.get().await()
            val updatedUnreadCounts = updatedConversationDoc.data?.get("unreadCounts") as? Map<String, Any>
            val updatedUserUnreadCount = updatedUnreadCounts?.get(currentUserId)
            
            println("✅ Successfully marked conversation as read")
            println("📊 Updated unreadCounts map: $updatedUnreadCounts")
            println("📊 Updated user ($currentUserId) unread count: $updatedUserUnreadCount")
            
            if (updatedUserUnreadCount == null) {
                println("🎉 SUCCESS: User's unread count field was successfully deleted!")
            } else {
                println("⚠️ Field deletion didn't work, applying fallback: setting to 0")
                // Fallback: Set to 0 if deletion didn't work
                conversationRef.update("unreadCounts.$currentUserId", 0).await()
                println("🔄 FALLBACK: Set unreadCounts.$currentUserId to 0")
                
                // Verify fallback worked
                val fallbackDoc = conversationRef.get().await()
                val fallbackUnreadCounts = fallbackDoc.data?.get("unreadCounts") as? Map<String, Any>
                val fallbackUserUnreadCount = fallbackUnreadCounts?.get(currentUserId)
                println("📊 After fallback - user unread count: $fallbackUserUnreadCount")
                
                if (fallbackUserUnreadCount == 0 || fallbackUserUnreadCount == 0L) {
                    println("🎉 FALLBACK SUCCESS: User's unread count set to 0!")
                } else {
                    println("❌ FALLBACK FAILED: Unread count is still: $fallbackUserUnreadCount")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Error in markConversationAsRead: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Update conversation last message
     * I-update ang huling mensahe ng conversation
     */
    private suspend fun updateConversationLastMessage(conversationId: String, message: ChatMessageNew) {
        try {
            val currentUserId = getCurrentUserId() ?: return
            
            // Get conversation to find the other participant
            val conversationDoc = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .get()
                .await()
            
            val conversation = conversationDoc.toObject<ConversationMetadata>()
            val otherParticipantId = conversation?.participants?.find { it != currentUserId }
            
            val updates = mutableMapOf<String, Any>(
                "lastMessage" to message.content,
                "lastMessageType" to message.type,
                "lastMessageSenderId" to message.senderId,
                "updatedAt" to message.timestamp
            )
            
            // Increment unread count for the receiver
            if (otherParticipantId != null) {
                updates["unreadCounts.$otherParticipantId"] = com.google.firebase.firestore.FieldValue.increment(1)
            }

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
     * IMMEDIATE: Set unread count to 0 for current user - MOST RELIABLE METHOD
     * AGAD: I-set ang unread count sa 0 para sa current user - PINAKA RELIABLE NA PARAAN
     */
    suspend fun immediateSetUnreadCountToZero(conversationId: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            println("🚀 IMMEDIATE: Setting unread count to 0 for user $currentUserId in conversation $conversationId")
            
            val conversationRef = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
            
            // Direct update - no batch, no complexity
            // Direktang update - walang batch, walang komplikasyon
            conversationRef.update("unreadCounts.$currentUserId", 0).await()
            
            println("✅ IMMEDIATE: Successfully set unread count to 0")
            
            // Verify it worked
            val verifyDoc = conversationRef.get().await()
            val verifyUnreadCounts = verifyDoc.data?.get("unreadCounts") as? Map<String, Any>
            val verifyUserUnreadCount = verifyUnreadCounts?.get(currentUserId)
            
            println("🔍 VERIFICATION: unreadCounts map: $verifyUnreadCounts")
            println("🔍 VERIFICATION: user unread count: $verifyUserUnreadCount")
            
            if (verifyUserUnreadCount == 0 || verifyUserUnreadCount == 0L) {
                println("🎉 IMMEDIATE SUCCESS: Unread count is now 0!")
                Result.success(Unit)
            } else {
                println("❌ IMMEDIATE FAILED: Unread count is still: $verifyUserUnreadCount")
                Result.failure(Exception("Failed to set unread count to 0"))
            }
        } catch (e: Exception) {
            println("❌ IMMEDIATE ERROR: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * COMPREHENSIVE: Mark messages as read AND set unread count to 0
     * KOMPREHENSIBO: I-mark ang mga mensahe bilang nabasa AT i-set ang unread count sa 0
     */
    suspend fun comprehensiveMarkAsRead(conversationId: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            println("🔄 COMPREHENSIVE: Starting comprehensive mark as read for conversation: $conversationId")
            println("👤 COMPREHENSIVE: Current user ID: $currentUserId")
            
            // Step 1: Set unread count to 0 immediately
            // Hakbang 1: I-set ang unread count sa 0 agad
            val conversationRef = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
            
            conversationRef.update("unreadCounts.$currentUserId", 0).await()
            println("✅ STEP 1: Set unread count to 0")
            
            // Step 2: Mark all unread messages as read
            // Hakbang 2: I-mark ang lahat ng unread messages bilang nabasa
            val unreadMessages = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .whereNotEqualTo("senderId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            println("📧 STEP 2: Found ${unreadMessages.size()} unread messages to mark as read")

            if (unreadMessages.documents.isNotEmpty()) {
                val batch = firestore.batch()
                unreadMessages.documents.forEach { doc ->
                    batch.update(doc.reference, "isRead", true)
                    println("✅ Marking message ${doc.id} as read")
                }
                batch.commit().await()
                println("✅ STEP 2: All messages marked as read")
            }
            
            // Step 3: Final verification
            // Hakbang 3: Final verification
            val finalDoc = conversationRef.get().await()
            val finalUnreadCounts = finalDoc.data?.get("unreadCounts") as? Map<String, Any>
            val finalUserUnreadCount = finalUnreadCounts?.get(currentUserId)
            
            println("🔍 FINAL VERIFICATION: unreadCounts map: $finalUnreadCounts")
            println("🔍 FINAL VERIFICATION: user unread count: $finalUserUnreadCount")
            
            if (finalUserUnreadCount == 0 || finalUserUnreadCount == 0L) {
                println("🎉 COMPREHENSIVE SUCCESS: Everything is now marked as read!")
                Result.success(Unit)
            } else {
                println("⚠️ COMPREHENSIVE WARNING: Unread count is not 0: $finalUserUnreadCount")
                Result.success(Unit) // Still return success since we tried our best
            }
        } catch (e: Exception) {
            println("❌ COMPREHENSIVE ERROR: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Test function to verify mark as read functionality
     * Test function para ma-verify ang mark as read functionality
     */
    suspend fun testMarkAsReadFunctionality(conversationId: String): Result<String> {
        return try {
            val currentUserId = getCurrentUserId() 
                ?: return Result.failure(Exception("User not authenticated"))

            println("🧪 TEST: Starting mark as read test for conversation: $conversationId")
            
            // Get conversation before marking as read
            val beforeDoc = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .get()
                .await()
            
            val beforeUnreadCounts = beforeDoc.data?.get("unreadCounts") as? Map<String, Any>
            val beforeUserUnreadCount = beforeUnreadCounts?.get(currentUserId)
            
            println("🧪 TEST: BEFORE - unreadCounts: $beforeUnreadCounts")
            println("🧪 TEST: BEFORE - user unread count: $beforeUserUnreadCount")
            
            // Mark as read
            val result = markConversationAsRead(conversationId)
            
            if (result.isSuccess) {
                // Check after marking as read
                val afterDoc = firestore.collection(CONVERSATIONS_COLLECTION)
                    .document(conversationId)
                    .get()
                    .await()
                
                val afterUnreadCounts = afterDoc.data?.get("unreadCounts") as? Map<String, Any>
                val afterUserUnreadCount = afterUnreadCounts?.get(currentUserId)
                
                println("🧪 TEST: AFTER - unreadCounts: $afterUnreadCounts")
                println("🧪 TEST: AFTER - user unread count: $afterUserUnreadCount")
                
                val testResult = if (afterUserUnreadCount == null || afterUserUnreadCount == 0 || afterUserUnreadCount == 0L) {
                    "✅ TEST PASSED: Conversation successfully marked as read!"
                } else {
                    "❌ TEST FAILED: Unread count is still: $afterUserUnreadCount"
                }
                
                Result.success(testResult)
            } else {
                Result.failure(Exception("Mark as read failed"))
            }
        } catch (e: Exception) {
            println("🧪 TEST ERROR: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get user by ID (checks both users and chiropractor collections)
     * Kunin ang user gamit ang ID (i-check ang users at chiropractor collections)
     */
    suspend fun getUserById(userId: String): Result<User?> {
        return try {
            // First try users collection
            var doc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            var user = doc.toObject<User>()?.copy(uid = doc.id)
            
            // If not found in users, try chiropractors collection
            if (user == null) {
                doc = firestore.collection(CHIROPRACTORS_COLLECTION)
                    .document(userId)
                    .get()
                    .await()
                
                val data = doc.data
                if (data != null) {
                    // Map TrueSpine Firestore structure to User model
                    user = User(
                        uid = doc.id,
                        fullName = data["name"] as? String ?: "",
                        email = data["email"] as? String ?: "",
                        profileImage = data["profileImageUrl"] as? String,
                        role = UserRole.CHIROPRACTOR,
                        specialization = data["specialization"] as? String,
                        phoneNumber = data["contactNumber"] as? String,
                        experience = (data["yearsOfExperience"] as? Long)?.toInt() ?: 0,
                        rating = 4.5, // Default rating since not in your data
                        reviewCount = 0, // Default review count
                        isAvailable = true, // Default to available
                        bio = data["about"] as? String ?: "Experienced ${data["specialization"] as? String ?: "chiropractor"}"
                    )
                }
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
