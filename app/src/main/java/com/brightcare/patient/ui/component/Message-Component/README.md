# Message Component / Message Component

This folder contains all the reusable components for the messaging functionality in the BrightCare Patient app.

Ang folder na ito ay naglalaman ng lahat ng reusable components para sa messaging functionality sa BrightCare Patient app.

## Components / Mga Component

### 1. MessageDataClasses.kt
Contains all data classes and enums used in the messaging system:
- `ChatMessage` - Represents a single message
- `ChatConversation` - Represents a conversation
- `SenderType` - Enum for different types of senders (DOCTOR, PATIENT, ADMIN)

Naglalaman ng lahat ng data classes at enums na ginagamit sa messaging system.

### 2. MessageUtils.kt
Utility functions for the messaging system:
- `getSampleConversations()` - Returns sample conversation data
- `filterConversationsByTab()` - Filters conversations by tab selection
- `formatConversationTime()` - Formats time for display

Mga utility functions para sa messaging system.

### 3. MessageHeader.kt
Header component with title and search functionality:
- Displays "Messages" title
- Shows subtitle "Chat with your healthcare team"
- Contains search button

Header component na may title at search functionality.

### 4. MessageTabRow.kt
Tab navigation component for filtering conversations:
- "Chats" tab - Shows all conversations
- "Doctors" tab - Shows only doctor conversations
- "Support" tab - Shows only admin/support conversations

Tab navigation component para sa pag-filter ng mga conversation.

### 5. ConversationCard.kt
Individual conversation card components:
- `ConversationCard` - Main card component
- `ConversationAvatar` - Avatar with online status
- `ConversationContent` - Message content and metadata
- `UnreadCountBadge` - Badge for unread message count

Mga individual conversation card components.

### 6. ConversationsList.kt
List component for displaying conversations:
- `ConversationsList` - Main list component
- `EmptyConversationsState` - Empty state when no conversations

List component para sa pagdisplay ng mga conversation.

### 7. MessageSearch.kt
Advanced search components with multiple features:
- `MessageSearch` - Expandable search bar with animations
- `SearchResultsSummary` - Shows search results count
- `AdvancedMessageSearch` - Search with filter chips
- Full keyboard support and animations

Advanced search components na may maraming feature.

### 8. SimpleMessageSearch.kt
Simple search components for basic usage:
- `SimpleMessageSearch` - Basic search text field
- `CompactMessageSearch` - Expandable search button
- `QuickFilterSearch` - Search with quick filter chips
- Lightweight and easy to use

Simple search components para sa basic usage.

### 9. MessageComponent.kt
Main component that combines all other components:
- Uses all individual components including search
- Manages search state and filtering
- Provides callbacks for user interactions
- Integrated search functionality

Pangunahing component na pinagsasama ang lahat ng ibang components kasama ang search.

## Usage / Paggamit

```kotlin
import com.brightcare.patient.ui.component.messagecomponent.MessageComponent

@Composable
fun MyScreen(navController: NavController) {
    MessageComponent(
        navController = navController,
        onSearchClick = { 
            // Handle search
        },
        onConversationClick = { conversationId ->
            // Handle conversation click
            navController.navigate("chat/$conversationId")
        }
    )
}
```

## Features / Mga Feature

- ✅ Modular design / Modular na design
- ✅ Reusable components / Reusable na mga components
- ✅ **Working search functionality** / **Gumaganang search functionality**
- ✅ **Advanced filtering** / **Advanced na pag-filter**
- ✅ **Real-time search results** / **Real-time na search results**
- ✅ **Multiple search UI variants** / **Maraming search UI variants**
- ✅ Bilingual documentation / Bilingual na documentation
- ✅ Material Design 3 / Material Design 3
- ✅ Preview support / Preview support
- ✅ Clean architecture / Clean na architecture
- ✅ Keyboard support / Keyboard support
- ✅ Smooth animations / Smooth na animations

## Dependencies / Mga Dependency

- Jetpack Compose
- Material Design 3
- Navigation Compose
- BrightCare Patient Theme
