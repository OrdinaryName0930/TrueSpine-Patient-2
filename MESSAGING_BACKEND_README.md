# BrightCare Patient Messaging Backend Implementation

## Overview / Pangkalahatang-ideya

This document provides a complete MVVM + Repository backend implementation for the patient ↔ chiropractor messaging system using Firebase Firestore, Firebase Storage, and Firebase Authentication.

Ang dokumentong ito ay nagbibigay ng kumpletong MVVM + Repository backend implementation para sa patient ↔ chiropractor messaging system gamit ang Firebase Firestore, Firebase Storage, at Firebase Authentication.

## 🏗️ Architecture / Arkitektura

```
app/src/main/java/com/brightcare/patient/
├── data/
│   ├── model/
│   │   └── MessageModels.kt              # Data models for messaging
│   ├── repository/
│   │   └── MessagingRepository.kt        # Repository layer with Firestore integration
│   └── storage/
│       └── FirebaseStorageHelper.kt      # Firebase Storage helper for file uploads
├── domain/
│   └── usecase/
│       └── MessagingUseCases.kt          # Business logic use cases
├── ui/
│   ├── viewmodel/
│   │   ├── MessagingViewModel.kt         # ViewModel for individual conversations
│   │   └── ConversationsViewModel.kt    # ViewModel for conversations list
│   ├── integration/
│   │   └── MessagingIntegration.kt       # Integration helpers for existing UI
│   └── example/
│       └── MessagingUsageExample.kt      # Usage examples and integration guide
└── di/
    └── MessagingModule.kt                # Dependency injection module
```

## 📊 Data Models

### Core Models

1. **Message** - Individual message with attachments support
2. **Conversation** - Chat conversation between patient and chiropractor
3. **Chiropractor** - Chiropractor profile with contact information
4. **Patient** - Patient profile with assigned chiropractor
5. **MessageType** - Enum for different message types (TEXT, IMAGE, FILE, etc.)
6. **UserType** - Enum for user roles (PATIENT, CHIROPRACTOR, etc.)

### Key Features

- **File Attachments**: Support for images, documents, and files
- **Real-time Updates**: Live message synchronization using Firestore listeners
- **Upload Progress**: Track file upload progress with StateFlow
- **Message Status**: Delivery and read status tracking
- **Phone Integration**: Direct calling functionality

## 🔥 Firebase Structure

### Firestore Collections

```
users/{uid}                                    # User profiles
patients/{patientId}                           # Patient documents
chiropractors/{chiropractorId}                 # Chiropractor documents
conversations/{conversationId}                 # Conversation metadata
conversations/{conversationId}/messages/{messageId}  # Messages subcollection
```

### Firebase Storage Structure

```
conversations/{conversationId}/
├── images/
│   ├── image_123456789.jpg
│   └── camera_987654321.jpg
├── files/
│   ├── document.pdf
│   └── report.docx
└── thumbnails/
    ├── thumb_123456789.jpg
    └── thumb_987654321.jpg
```

## 🚀 Implementation Guide

### 1. Repository Layer

The `MessagingRepository` provides:

- **getAssignedChiropractor()**: Retrieve assigned chiropractor for current patient
- **createOrFindConversation()**: Create or find existing conversation
- **getMessages()**: Real-time message listener using StateFlow
- **sendTextMessage()**: Send text messages
- **sendImageMessage()**: Send images with upload progress
- **sendFileMessage()**: Send documents with upload progress
- **markMessageAsRead()**: Update message read status
- **getChiropractorPhoneNumber()**: Get phone number for calling

### 2. Use Cases

Business logic is encapsulated in use cases:

- **GetAssignedChiropractorUseCase**: Get patient's assigned chiropractor
- **SendTextMessageUseCase**: Send text messages with validation
- **SendImageMessageUseCase**: Handle image uploads
- **SendFileMessageUseCase**: Handle file uploads
- **MakePhoneCallUseCase**: Handle phone call functionality
- **ValidateMessageContentUseCase**: Validate message content
- **FormatMessageTimestampUseCase**: Format timestamps for display

### 3. ViewModels

#### MessagingViewModel
- Manages individual conversation state
- Handles message sending and receiving
- Tracks upload progress
- Manages phone call functionality

#### ConversationsViewModel
- Manages conversations list
- Tracks unread counts
- Handles conversation refresh

### 4. Integration with Existing UI

Use the `MessagingIntegrationProvider` to connect with your existing Compose UI:

```kotlin
@Composable
fun YourMessagingScreen() {
    MessagingIntegrationProvider { state ->
        // Your existing UI components
        ConversationHeader(
            conversation = state.conversation?.toChatConversation(),
            onBackClick = { /* handle back */ }
        )
        
        MessageInputArea(
            messageText = state.messageText,
            onMessageTextChange = state.onMessageTextChange,
            onSendMessage = state.onSendMessage,
            onImageClick = { uri -> state.onSendImage(uri, null) },
            onCameraClick = { uri -> state.onSendImage(uri, null) },
            onDocumentClick = { uri, name, type -> 
                state.onSendFile(uri, name, type) 
            }
        )
    }
}
```

## 🔐 Security Rules

### Firestore Security Rules

The provided security rules ensure:

- Users can only access their own data
- Conversation participants can read/write messages
- Message senders can delete their own messages
- Proper authentication is required for all operations

### Firebase Storage Security Rules

- Only conversation participants can upload/download files
- Files are organized by conversation ID
- Proper authentication is required

## 📱 Phone Call Integration

### Features

1. **Permission Dialog**: Shows confirmation before making calls
2. **Automatic Permission Request**: Requests CALL_PHONE permission if needed
3. **Direct Dialing**: Opens phone app and dials automatically
4. **Error Handling**: Proper error handling for failed calls

### Usage

```kotlin
// In your Compose UI
val context = LocalContext.current
MessagingIntegrationProvider { state ->
    Button(
        onClick = { state.onMakePhoneCall(context) }
    ) {
        Text("Call Doctor")
    }
}
```

## 🔧 Setup Instructions

### 1. Add Dependencies

Ensure your `build.gradle` includes:

```kotlin
implementation 'com.google.firebase:firebase-firestore-ktx'
implementation 'com.google.firebase:firebase-storage-ktx'
implementation 'com.google.firebase:firebase-auth-ktx'
implementation 'com.google.dagger:hilt-android'
implementation 'androidx.hilt:hilt-navigation-compose'
```

### 2. Add Permissions

Add to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

### 3. Initialize Firebase

Ensure Firebase is properly initialized in your Application class.

### 4. Setup Dependency Injection

The `MessagingModule` provides all necessary dependencies. Make sure it's included in your Hilt modules.

### 5. Deploy Security Rules

Deploy the provided Firestore and Storage security rules to your Firebase project.

## 🎯 Key Features Implemented

### ✅ Completed Features

1. **Retrieve Assigned Chiropractor** - Get chiropractor details for current patient
2. **Create/Find Conversations** - Automatic conversation creation between patient and chiropractor
3. **Real-time Messaging** - Live message updates using Firestore listeners
4. **Text Messages** - Send and receive text messages
5. **Image Messages** - Send images from gallery or camera with upload progress
6. **File Messages** - Send documents (PDF, Word, etc.) with upload progress
7. **Phone Call Integration** - Direct calling with permission handling
8. **Message Status** - Read/delivered status tracking
9. **Security Rules** - Comprehensive Firestore and Storage security
10. **MVVM Architecture** - Clean architecture with separation of concerns

### 📋 Message Data Fields

Each message includes:
- `senderId` - ID of message sender
- `receiverId` - ID of message receiver
- `type` - Message type (text/image/file)
- `content` - Text content or file name
- `fileUrl` - Firebase Storage URL for files
- `timestamp` - Message timestamp
- `isRead` - Read status
- `isDelivered` - Delivery status

### 🔄 Real-time Updates

Messages are synchronized in real-time using Firestore listeners wrapped in StateFlow for reactive UI updates.

### 📤 File Upload Process

1. User selects file/image
2. Upload starts with progress tracking
3. File is uploaded to Firebase Storage
4. Message is created with file URL
5. Conversation is updated with latest message

### 📞 Call Button Logic

1. User clicks call button
2. Permission dialog is shown
3. If confirmed, CALL_PHONE permission is checked
4. If granted, phone app opens with chiropractor's number
5. If not granted, permission is requested

## 🔮 Future WebRTC Integration

The current implementation provides a foundation for future WebRTC integration:

```kotlin
// Placeholder for future WebRTC implementation
class WebRTCCallManager {
    fun initiateVideoCall(chiropractorId: String) {
        // WebRTC video call implementation
    }
    
    fun initiateVoiceCall(chiropractorId: String) {
        // WebRTC voice call implementation
    }
}
```

## 🐛 Error Handling

The implementation includes comprehensive error handling:

- Network connectivity issues
- File upload failures
- Permission denied scenarios
- Authentication errors
- Firestore security rule violations

## 📈 Performance Considerations

- **Pagination**: Messages can be paginated for large conversations
- **Caching**: Repository layer can implement local caching
- **Compression**: Images can be compressed before upload
- **Thumbnails**: Automatic thumbnail generation for images

## 🔍 Testing

The modular architecture makes testing straightforward:

- **Unit Tests**: Test use cases and ViewModels
- **Integration Tests**: Test repository layer
- **UI Tests**: Test Compose components

## 📞 Support

For questions or issues with the implementation:

1. Check the example usage in `MessagingUsageExample.kt`
2. Review the integration helpers in `MessagingIntegration.kt`
3. Ensure all dependencies are properly configured
4. Verify Firebase project setup and security rules

## 🎉 Conclusion

This implementation provides a complete, production-ready messaging backend that integrates seamlessly with your existing Compose UI. The modular architecture ensures maintainability and extensibility for future features.

Ang implementation na ito ay nagbibigay ng kumpletong, production-ready messaging backend na seamlessly na nag-integrate sa inyong existing Compose UI. Ang modular architecture ay nagsisiguro ng maintainability at extensibility para sa future features.







