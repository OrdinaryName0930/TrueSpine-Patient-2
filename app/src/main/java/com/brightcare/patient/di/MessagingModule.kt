package com.brightcare.patient.di

import com.brightcare.patient.data.repository.MessagingRepository
import com.brightcare.patient.data.repository.ChiropractorSearchRepository
import com.brightcare.patient.data.repository.ConversationRepository
import com.brightcare.patient.data.storage.FirebaseStorageHelper
import com.brightcare.patient.domain.usecase.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for messaging
 * Dependency injection module para sa messaging
 */
@Module
@InstallIn(SingletonComponent::class)
object MessagingModule {

    // Firebase instances are already provided in AppModule
    // Remove duplicate providers to avoid Dagger conflicts

    @Provides
    @Singleton
    fun provideFirebaseStorageHelper(
        storage: FirebaseStorage
    ): FirebaseStorageHelper {
        return FirebaseStorageHelper(storage)
    }

    @Provides
    @Singleton
    fun provideMessagingRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storageHelper: FirebaseStorageHelper
    ): MessagingRepository {
        return MessagingRepository(firestore, auth, storageHelper)
    }

    @Provides
    @Singleton
    fun provideChiropractorSearchRepository(
        firestore: FirebaseFirestore
    ): ChiropractorSearchRepository {
        return ChiropractorSearchRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideConversationRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storageHelper: FirebaseStorageHelper
    ): ConversationRepository {
        return ConversationRepository(firestore, auth, storageHelper)
    }

    @Provides
    @Singleton
    fun provideGetAssignedChiropractorUseCase(
        repository: MessagingRepository
    ): GetAssignedChiropractorUseCase {
        return GetAssignedChiropractorUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCreateOrFindConversationUseCase(
        repository: MessagingRepository
    ): CreateOrFindConversationUseCase {
        return CreateOrFindConversationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetConversationsUseCase(
        repository: MessagingRepository
    ): GetConversationsUseCase {
        return GetConversationsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetMessagesUseCase(
        repository: MessagingRepository
    ): GetMessagesUseCase {
        return GetMessagesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSendTextMessageUseCase(
        repository: MessagingRepository
    ): SendTextMessageUseCase {
        return SendTextMessageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSendImageMessageUseCase(
        repository: MessagingRepository
    ): SendImageMessageUseCase {
        return SendImageMessageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSendFileMessageUseCase(
        repository: MessagingRepository
    ): SendFileMessageUseCase {
        return SendFileMessageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMarkMessageAsReadUseCase(
        repository: MessagingRepository
    ): MarkMessageAsReadUseCase {
        return MarkMessageAsReadUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMarkAllMessagesAsReadUseCase(
        repository: MessagingRepository
    ): MarkAllMessagesAsReadUseCase {
        return MarkAllMessagesAsReadUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteMessageUseCase(
        repository: MessagingRepository
    ): DeleteMessageUseCase {
        return DeleteMessageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMakePhoneCallUseCase(
        repository: MessagingRepository
    ): MakePhoneCallUseCase {
        return MakePhoneCallUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideValidateMessageContentUseCase(): ValidateMessageContentUseCase {
        return ValidateMessageContentUseCase()
    }

    @Provides
    @Singleton
    fun provideFormatMessageTimestampUseCase(): FormatMessageTimestampUseCase {
        return FormatMessageTimestampUseCase()
    }

    @Provides
    @Singleton
    fun provideGetFileSizeStringUseCase(): GetFileSizeStringUseCase {
        return GetFileSizeStringUseCase()
    }

    @Provides
    @Singleton
    fun provideMarkConversationAsReadUseCase(
        repository: ConversationRepository
    ): MarkConversationAsReadUseCase {
        return MarkConversationAsReadUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMarkConversationAsReadSetToZeroUseCase(
        repository: ConversationRepository
    ): MarkConversationAsReadSetToZeroUseCase {
        return MarkConversationAsReadSetToZeroUseCase(repository)
    }

    // Chiropractor Search Use Cases
    @Provides
    @Singleton
    fun provideSearchChiropractorsUseCase(
        repository: ChiropractorSearchRepository
    ): SearchChiropractorsUseCase {
        return SearchChiropractorsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetAllActiveChiropractorsUseCase(
        repository: ChiropractorSearchRepository
    ): GetAllActiveChiropractorsUseCase {
        return GetAllActiveChiropractorsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetChiropractorsBySpecializationUseCase(
        repository: ChiropractorSearchRepository
    ): GetChiropractorsBySpecializationUseCase {
        return GetChiropractorsBySpecializationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetChiropractorByIdUseCase(
        repository: ChiropractorSearchRepository
    ): GetChiropractorByIdUseCase {
        return GetChiropractorByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetTopRatedChiropractorsUseCase(
        repository: ChiropractorSearchRepository
    ): GetTopRatedChiropractorsUseCase {
        return GetTopRatedChiropractorsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetAvailableSpecializationsUseCase(
        repository: ChiropractorSearchRepository
    ): GetAvailableSpecializationsUseCase {
        return GetAvailableSpecializationsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSearchChiropractorsFlowUseCase(
        repository: ChiropractorSearchRepository
    ): SearchChiropractorsFlowUseCase {
        return SearchChiropractorsFlowUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideChiropractorSearchUseCases(
        searchChiropractors: SearchChiropractorsUseCase,
        getAllActiveChiropractors: GetAllActiveChiropractorsUseCase,
        getChiropractorsBySpecialization: GetChiropractorsBySpecializationUseCase,
        getChiropractorById: GetChiropractorByIdUseCase,
        getTopRatedChiropractors: GetTopRatedChiropractorsUseCase,
        getAvailableSpecializations: GetAvailableSpecializationsUseCase,
        searchChiropractorsFlow: SearchChiropractorsFlowUseCase
    ): ChiropractorSearchUseCases {
        return ChiropractorSearchUseCases(
            searchChiropractors = searchChiropractors,
            getAllActiveChiropractors = getAllActiveChiropractors,
            getChiropractorsBySpecialization = getChiropractorsBySpecialization,
            getChiropractorById = getChiropractorById,
            getTopRatedChiropractors = getTopRatedChiropractors,
            getAvailableSpecializations = getAvailableSpecializations,
            searchChiropractorsFlow = searchChiropractorsFlow
        )
    }

    @Provides
    @Singleton
    fun provideImmediateSetUnreadCountToZeroUseCase(
        repository: ConversationRepository
    ): ImmediateSetUnreadCountToZeroUseCase {
        return ImmediateSetUnreadCountToZeroUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideComprehensiveMarkAsReadUseCase(
        repository: ConversationRepository
    ): ComprehensiveMarkAsReadUseCase {
        return ComprehensiveMarkAsReadUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMessagingUseCases(
        getAssignedChiropractor: GetAssignedChiropractorUseCase,
        createOrFindConversation: CreateOrFindConversationUseCase,
        getConversations: GetConversationsUseCase,
        getMessages: GetMessagesUseCase,
        sendTextMessage: SendTextMessageUseCase,
        sendImageMessage: SendImageMessageUseCase,
        sendFileMessage: SendFileMessageUseCase,
        markMessageAsRead: MarkMessageAsReadUseCase,
        markAllMessagesAsRead: MarkAllMessagesAsReadUseCase,
        markConversationAsRead: MarkConversationAsReadUseCase,
        markConversationAsReadSetToZero: MarkConversationAsReadSetToZeroUseCase,
        immediateSetUnreadCountToZero: ImmediateSetUnreadCountToZeroUseCase,
        comprehensiveMarkAsRead: ComprehensiveMarkAsReadUseCase,
        deleteMessage: DeleteMessageUseCase,
        makePhoneCall: MakePhoneCallUseCase,
        validateMessageContent: ValidateMessageContentUseCase,
        formatMessageTimestamp: FormatMessageTimestampUseCase,
        getFileSizeString: GetFileSizeStringUseCase
    ): MessagingUseCases {
        return MessagingUseCases(
            getAssignedChiropractor = getAssignedChiropractor,
            createOrFindConversation = createOrFindConversation,
            getConversations = getConversations,
            getMessages = getMessages,
            sendTextMessage = sendTextMessage,
            sendImageMessage = sendImageMessage,
            sendFileMessage = sendFileMessage,
            markMessageAsRead = markMessageAsRead,
            markAllMessagesAsRead = markAllMessagesAsRead,
            markConversationAsRead = markConversationAsRead,
            markConversationAsReadSetToZero = markConversationAsReadSetToZero,
            immediateSetUnreadCountToZero = immediateSetUnreadCountToZero,
            comprehensiveMarkAsRead = comprehensiveMarkAsRead,
            deleteMessage = deleteMessage,
            makePhoneCall = makePhoneCall,
            validateMessageContent = validateMessageContent,
            formatMessageTimestamp = formatMessageTimestamp,
            getFileSizeString = getFileSizeString
        )
    }
}
