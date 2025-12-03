# Chiro Components

This folder contains reusable UI components for chiropractor-related screens in the BrightCare Patient app.

## Components

### 1. ChiropractorInfo.kt
Data class that represents chiropractor information including:
- ID, name, specialization
- Experience, rating, location
- Availability status

### 2. ChiropractorCard.kt
Reusable card component that displays:
- Chiropractor information
- Rating and experience
- Location
- Availability badge
- Action buttons (View Profile, Book Now)

### 3. ChiroHeader.kt
Header component for chiro screens featuring:
- Title and subtitle
- Search button
- Consistent styling

### 4. ChiroFilterChips.kt
Filter components for chiropractor search:
- Available Now filter
- Near Me filter
- Reusable individual filter chip

## Usage

Import these components in your screens:

```kotlin
import com.brightcare.patient.ui.component.chiro.ChiropractorInfo
import com.brightcare.patient.ui.component.chiro.ChiropractorCard
import com.brightcare.patient.ui.component.chiro.ChiroHeader
import com.brightcare.patient.ui.component.chiro.ChiroFilterChips
```

## Design Principles

- **Reusability**: All components are designed to be used across multiple screens
- **Consistency**: Follows the app's design system and theme
- **Modularity**: Each component handles a specific UI concern
- **Accessibility**: Includes proper content descriptions and semantic elements

## Language Support

Components support both English and Tagalog text as per app requirements.