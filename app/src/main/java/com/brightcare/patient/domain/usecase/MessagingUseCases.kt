package com.brightcare.patient.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.brightcare.patient.data.model.*
import com.brightcare.patient.data.repository.MessagingRepository
import com.brightcare.patient.data.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use cases for messaging functionality
 * Use cases para sa messaging functionality
 */

/**
 * Get assigned chiropractor use case
 * Use case para sa pagkuha ng assigned chiropractor
 */
class GetAssignedChiropractorUseCase @Inject constructor(
    private val repository: MessagingRepository
) {
    suspend operator fun invoke(): Result<Chiropractor?> {
        return repository.getAssignedChiropractor()
    }
}

/**
 * Create or find conversation use case
 * Use case para sa paggawa o paghanap ng conversation
 */
class CreateOrFindConversationUseCase @Inject constructor(
    private val repository: MessagingRepository
) {
    suspend operator fun invoke(chiropractorId: String): Result<Conversation> {
        return repository.createOrFindConversation(chiropractorId)
    }
}

/**
 * Get conversations use case
 * Use case para sa pagkuha ng mga conversation
 */
class GetConversationsUseCase @Inject constructor(
    private val repository: MessagingRepository
) {
    operator fun invoke(): Flow<List<Conversation>> {
        return repository.getConversations()
    }
}

/**
 * Get messages use case
 * Use case para sa pagkuha ng mga mensahe
 */
class GetMessagesUseCase @Inject constructor(
    private val repository: MessagingRepository
) {
    operator fun invoke(conversationId: String): Flow<List<Message>> {
        return repository.getMessages(conversationId)
    }
}

/**
 * Send text message use case
 * Use case para sa pagpadala ng text message
 */
class SendTextMessageUseCase @Inject constructor(
    private val repository: MessagingRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        receiverId: String,
        content: String,
        replyToMessageId: String? = null
    ): Result<Message> {
        // Validate input
        if (content.isBlank()) {
            return Result.failure(Exception("Message content cannot be empty"))
        }
        
        return repository.sendTextMessage(conversationId, receiverId, content, replyToMessageId)
    }
}

/**
 * Send image message use case
 * Use case para sa pagpadala ng image message
 */
class SendImageMessageUseCase @Inject constructor(
    private val repository: MessagingRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        receiverId: String,
        imageUri: Uri,
        fileName: String? = null
    ): Flow<MessageWithStatus> {
        return repository.sendImageMessage(conversationId, receiverId, imageUri, fileName)
    }
}

/**
 * Send file message use case
 * Use case para sa pagpadala ng file message
 */
class SendFileMessageUseCase @Inject constructor(
    private val repository: MessagingRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        receiverId: String,
        fileUri: Uri,
        fileName: String,
        mimeType: String
    ): Flow<MessageWithStatus> {
        // Validate file name
        if (fileName.isBlank()) {
            throw Exception("File name cannot be empty")
        }
        
        return repository.sendFileMessage(conversationId, receiverId, fileUri, fileName, mimeType)
    }
}

/**
 * Mark message as read use case
 * Use case para sa pagmarka ng mensahe bilang nabasa
 */
class MarkMessageAsReadUseCase @Inject constructor(
    private val repository: MessagingRepository
) {
    suspend operator fun invoke(conversationId: String, messageId: String): Result<Unit> {
        return repository.markMessageAsRead(conversationId, messageId)
    }
}

/**
 * Mark all messages as read use case
 * Use case para sa pagmarka ng lahat ng mensahe bilang nabasa
 */
class MarkAllMessagesAsReadUseCase @Inject constructor(
    private val repository: MessagingRepository
) {
    suspend operator fun invoke(conversationId: String): Result<Unit> {
        return repository.markAllMessagesAsRead(conversationId)
    }
}

/**
 * Delete message use case
 * Use case para sa pagtanggal ng mensahe
 */
class DeleteMessageUseCase @Inject constructor(
    private val repository: MessagingRepository
) {
    suspend operator fun invoke(conversationId: String, messageId: String): Result<Unit> {
        return repository.deleteMessage(conversationId, messageId)
    }
}

/**
 * Make phone call use case
 * Use case para sa pagtawag
 */
class MakePhoneCallUseCase @Inject constructor(
    private val repository: MessagingRepository
) {
    suspend operator fun invoke(
        context: Context,
        chiropractorId: String
    ): Result<Unit> {
        return try {
            val phoneResult = repository.getChiropractorPhoneNumber(chiropractorId)
            
            phoneResult.fold(
                onSuccess = { phoneNumber ->
                    val intent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                    }
                    context.startActivity(intent)
                    Result.success(Unit)
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
     * Get phone number for display in dialog
     * Kunin ang phone number para sa display sa dialog
     */
    suspend fun getPhoneNumber(chiropractorId: String): Result<String> {
        return repository.getChiropractorPhoneNumber(chiropractorId)
    }
}

/**
 * Validate message content use case
 * Use case para sa pag-validate ng message content
 */
class ValidateMessageContentUseCase @Inject constructor() {
    operator fun invoke(content: String): Result<String> {
        return when {
            content.isBlank() -> Result.failure(Exception("Message cannot be empty"))
            content.length > 5000 -> Result.failure(Exception("Message too long (max 5000 characters)"))
            else -> Result.success(content.trim())
        }
    }
}

/**
 * Format message timestamp use case
 * Use case para sa pag-format ng message timestamp
 */
class FormatMessageTimestampUseCase @Inject constructor() {
    operator fun invoke(timestamp: com.google.firebase.Timestamp): String {
        val now = System.currentTimeMillis()
        val messageTime = timestamp.toDate().time
        val diff = now - messageTime
        
        return when {
            diff < 60_000 -> "Just now" // Less than 1 minute
            diff < 3600_000 -> "${diff / 60_000}m ago" // Less than 1 hour
            diff < 86400_000 -> "${diff / 3600_000}h ago" // Less than 1 day
            diff < 604800_000 -> "${diff / 86400_000}d ago" // Less than 1 week
            else -> {
                val date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                date.format(timestamp.toDate())
            }
        }
    }
}

/**
 * Get file size string use case
 * Use case para sa pagkuha ng file size string
 */
class GetFileSizeStringUseCase @Inject constructor() {
    operator fun invoke(sizeBytes: Long): String {
        return when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            sizeBytes < 1024 * 1024 * 1024 -> "${sizeBytes / (1024 * 1024)} MB"
            else -> "${sizeBytes / (1024 * 1024 * 1024)} GB"
        }
    }
}

/**
 * Mark conversation as read use case (using ConversationRepository)
 * Use case para sa pagmarka ng conversation bilang nabasa (gamit ang ConversationRepository)
 */
class MarkConversationAsReadUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    suspend operator fun invoke(conversationId: String): Result<Unit> {
        return repository.markConversationAsRead(conversationId)
    }
}

/**
 * Alternative: Mark conversation as read by setting unread count to 0
 * Alternative: Markahan ang conversation bilang nabasa sa pamamagitan ng pag-set ng unread count sa 0
 */
class MarkConversationAsReadSetToZeroUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    suspend operator fun invoke(conversationId: String): Result<Unit> {
        return repository.markConversationAsReadSetToZero(conversationId)
    }
}

/**
 * IMMEDIATE: Set unread count to 0 immediately - Most reliable
 * AGAD: I-set ang unread count sa 0 agad - Pinaka reliable
 */
class ImmediateSetUnreadCountToZeroUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    suspend operator fun invoke(conversationId: String): Result<Unit> {
        return repository.immediateSetUnreadCountToZero(conversationId)
    }
}

/**
 * COMPREHENSIVE: Mark messages as read AND set unread count to 0
 * KOMPREHENSIBO: I-mark ang mga mensahe bilang nabasa AT i-set ang unread count sa 0
 */
class ComprehensiveMarkAsReadUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    suspend operator fun invoke(conversationId: String): Result<Unit> {
        return repository.comprehensiveMarkAsRead(conversationId)
    }
}

/**
 * Combined messaging use cases for easier injection
 * Pinagsama na messaging use cases para sa mas madaling injection
 */
data class MessagingUseCases(
    val getAssignedChiropractor: GetAssignedChiropractorUseCase,
    val createOrFindConversation: CreateOrFindConversationUseCase,
    val getConversations: GetConversationsUseCase,
    val getMessages: GetMessagesUseCase,
    val sendTextMessage: SendTextMessageUseCase,
    val sendImageMessage: SendImageMessageUseCase,
    val sendFileMessage: SendFileMessageUseCase,
    val markMessageAsRead: MarkMessageAsReadUseCase,
    val markAllMessagesAsRead: MarkAllMessagesAsReadUseCase,
    val markConversationAsRead: MarkConversationAsReadUseCase,
    val markConversationAsReadSetToZero: MarkConversationAsReadSetToZeroUseCase, // Alternative approach
    val immediateSetUnreadCountToZero: ImmediateSetUnreadCountToZeroUseCase, // Most reliable
    val comprehensiveMarkAsRead: ComprehensiveMarkAsReadUseCase, // Complete solution
    val deleteMessage: DeleteMessageUseCase,
    val makePhoneCall: MakePhoneCallUseCase,
    val validateMessageContent: ValidateMessageContentUseCase,
    val formatMessageTimestamp: FormatMessageTimestampUseCase,
    val getFileSizeString: GetFileSizeStringUseCase
)

