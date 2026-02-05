# Conversation Component

## Overview / Pangkalahatang-ideya

The Conversation Component is a reusable UI component for individual chat conversations in the BrightCare Patient app. It provides a complete chat interface with message bubbles, attachment support, and Firestore integration.

Ang Conversation Component ay isang reusable UI component para sa individual na chat conversations sa BrightCare Patient app. Nagbibigay ito ng kumpletong chat interface na may message bubbles, attachment support, at Firestore integration.

## Components / Mga Component

### 1. ConversationComponent.kt
- Main conversation component with Firestore integration
- Manages message state and auto-scrolling
- Handles message sending and receiving
- Pangunahing conversation component na may Firestore integration
- Nag-manage ng message state at auto-scrolling
- Nag-handle ng message sending at receiving

### 2. ConversationHeader.kt
- Fixed header with back button, participant info, and action buttons
- Shows online/offline status
- Includes video call, voice call, and more options buttons
- Fixed header na may back button, participant info, at action buttons
- Nagpapakita ng online/offline status
- May video call, voice call, at more options buttons

### 3. MessageBubble.kt
- Individual message bubble with attachment support
- Supports images, files, documents, and other attachments
- Different styling for sent/received messages
- Individual na message bubble na may attachment support
- Sumusuporta sa images, files, documents, at iba pang attachments
- Iba-ibang styling para sa sent/received messages

### 4. MessageInputArea.kt
- Message input with attachment and image features
- Expandable attachment options (gallery, camera, document, file)
- Send button with visual feedback
- Message input na may attachment at image features
- Expandable attachment options (gallery, camera, document, file)
- Send button na may visual feedback

### 5. ConversationDataClasses.kt
- Enhanced data classes with attachment support
- Message, conversation, and attachment models
- Support for reactions, mentions, and message status
- Enhanced data classes na may attachment support
- Message, conversation, at attachment models
- Support para sa reactions, mentions, at message status

### 6. ConversationUtils.kt
- Utility functions and Firestore operations
- Sample data generators
- File formatting and type detection utilities
- Mga utility function at Firestore operations
- Sample data generators
- File formatting at type detection utilities

## Features / Mga Feature

### ✅ Implemented / Na-implement na
- [x] Fixed conversation header with participant info
- [x] Message bubbles with attachment support
- [x] Message input area with attachment options
- [x] Auto-scroll to latest messages (bottom)
- [x] Image and file attachment display
- [x] Message status indicators (sent, delivered, read)
- [x] Online/offline status display
- [x] Expandable attachment options
- [x] Enhanced data models with attachment support
- [x] Firestore integration structure (ready for implementation)

### 🚧 Planned / Plano pa
- [ ] Actual Firestore integration
- [ ] Image picker implementation
- [ ] File picker implementation
- [ ] Camera capture functionality
- [ ] Message reactions
- [ ] Message replies
- [ ] Typing indicators
- [ ] Message search within conversation
- [ ] Message editing and deletion
- [ ] Voice message recording

## Usage / Paggamit

```kotlin
@Composable
fun ConversationScreen(
    conversationId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    ConversationComponent(
        conversationId = conversationId,
        navController = navController,
        modifier = modifier,
        onBackClick = { navController.popBackStack() }
    )
}
```

## File Structure / File Structure

```
Conversation-Component/
├── ConversationComponent.kt      # Main component
├── ConversationHeader.kt         # Fixed header
├── MessageBubble.kt             # Message bubbles with attachments
├── MessageInputArea.kt          # Input area with attachment options
├── ConversationDataClasses.kt   # Enhanced data models
├── ConversationUtils.kt         # Utilities and Firestore operations
└── README.md                    # This file
```

## Key Features Implemented / Mga Pangunahing Feature na Na-implement

### 🎯 **Fixed Header**
- Always visible conversation header
- Back navigation button
- Participant information and online status
- Video call, voice call, and more options buttons

### 💬 **Message System**
- Messages start from bottom (latest messages)
- Auto-scroll to newest messages
- Different bubble styles for sent/received messages
- Message status indicators (sent, delivered, read)
- Timestamp display

### 📎 **Attachment Support**
- Image attachments with preview
- File attachments with type icons
- Expandable attachment options menu
- Gallery, camera, document, and file options
- File size formatting and type detection

### 🔄 **Firestore Integration Ready**
- Complete data models for Firestore
- Send message functionality structure
- File upload progress tracking
- Message status tracking
- Error handling structure

## Integration Notes / Mga Integration Notes

1. **Firestore Integration**: Currently uses sample data. Uncomment Firestore code in ConversationUtils.kt for production use.
2. **Attachment Handling**: Image and file pickers need to be implemented based on your file handling strategy.
3. **Navigation**: Uses NavController for back navigation and can be extended for other navigation needs.
4. **State Management**: Uses local state management. Can be enhanced with ViewModel for complex state handling.

1. **Firestore Integration**: Gumagamit pa ng sample data. I-uncomment ang Firestore code sa ConversationUtils.kt para sa production use.
2. **Attachment Handling**: Kailangan pa i-implement ang image at file pickers base sa inyong file handling strategy.
3. **Navigation**: Gumagamit ng NavController para sa back navigation at pwedeng i-extend para sa iba pang navigation needs.
4. **State Management**: Gumagamit ng local state management. Pwedeng i-enhance gamit ang ViewModel para sa complex state handling.

## Message Flow / Message Flow

1. **User types message** → MessageInputArea captures input
2. **User clicks send** → ConversationComponent handles sending
3. **Message sent to Firestore** → ConversationUtils manages Firestore operations
4. **Message appears in chat** → MessageBubble displays the message
5. **Auto-scroll to bottom** → Latest messages always visible

The conversation component provides a **complete messenger-like experience** with attachment support and Firestore integration ready for healthcare communication!























