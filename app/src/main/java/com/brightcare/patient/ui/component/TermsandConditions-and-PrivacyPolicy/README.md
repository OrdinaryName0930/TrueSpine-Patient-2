# Terms & Conditions and Privacy Policy UI Components

This directory contains all the reusable components for the Terms & Conditions and Privacy Policy screen in the BrightCare Patient App.

## 📁 Components Overview

### 1. `TermsBackButton.kt`
- **Purpose**: Consistent back button for the Terms screen
- **Design**: Matches the SignUp back button style
- **Features**: Circular background, arrow icon, customizable colors

### 2. `TermsAgreeButton.kt`
- **Purpose**: "I Agree" button at the bottom of the Terms screen
- **Design**: Matches the SignUp button style and colors
- **Features**: Loading state, consistent styling with primary buttons

### 3. `TermsContent.kt`
- **Purpose**: Scrollable content displaying Terms & Conditions and Privacy Policy
- **Features**: 
  - Comprehensive legal text
  - Proper typography and spacing
  - Scrollable LazyColumn layout
  - Professional formatting

### 4. `EnhancedTermsCheckbox.kt`
- **Purpose**: Enhanced version of the original TermsCheckbox with navigation
- **Features**: 
  - Clickable terms text that navigates to Terms screen
  - Consistent styling with original checkbox
  - Error state handling

## 🎨 Design Consistency

All components follow the established design system:
- **Colors**: Uses the same Blue4280EF primary color, Gray tones, and White background
- **Typography**: Consistent with Material 3 typography scale
- **Spacing**: 24dp horizontal padding, 16dp vertical spacing
- **Shapes**: 12dp rounded corners for buttons
- **Elevation**: Minimal elevation following Material 3 principles

## 🚀 Usage Examples

### Basic Terms Screen Implementation

```kotlin
@Composable
fun MyTermsScreen(navController: NavController) {
    TermsAndConditionsScreen(
        navController = navController,
        onAgreeClicked = {
            // Handle agreement logic
            navController.popBackStack()
        }
    )
}
```

### Integration with SignUp Screen

```kotlin
@Composable
fun MySignUpScreen(navController: NavController) {
    // ... other signup logic ...
    
    EnhancedTermsCheckbox(
        isChecked = termsAccepted,
        onCheckedChange = { termsAccepted = it },
        onTermsAndPrivacyClick = {
            navController.navigate("terms_and_conditions")
        }
    )
}
```

### Navigation Setup

```kotlin
// In your NavHost
composable("signup") { backStackEntry ->
    val autoCheckTerms = backStackEntry.savedStateHandle.get<Boolean>("terms_agreed") ?: false
    
    EnhancedPatientSignUpScreen(
        navController = navController,
        autoCheckTerms = autoCheckTerms
    )
}

composable("terms_and_conditions") {
    TermsAndConditionsScreen(
        navController = navController,
        onAgreeClicked = {
            navController.previousBackStackEntry?.savedStateHandle?.set("terms_agreed", true)
        }
    )
}
```

## 🔧 Customization Options

### TermsBackButton
```kotlin
TermsBackButton(
    onClick = { /* navigation logic */ },
    backgroundColor = Gray50, // Customizable
    iconColor = Gray700      // Customizable
)
```

### TermsAgreeButton
```kotlin
TermsAgreeButton(
    text = "I Agree",        // Customizable text
    onClick = { /* logic */ },
    enabled = true,          // Enable/disable state
    loading = false          // Loading state
)
```

### TermsContent
```kotlin
TermsContent(
    listState = rememberLazyListState(), // For scroll control
    modifier = Modifier.fillMaxSize()    // Layout customization
)
```

## 📱 Features

### ✅ Implemented Features
- **Sticky Top Bar**: Back button remains visible during scrolling
- **Scrollable Content**: Smooth scrolling through terms and privacy policy
- **Floating Action Button**: "I Agree" button floats at the bottom
- **Navigation Integration**: Seamless navigation back to SignUp screen
- **Auto-checkbox**: Automatically checks terms checkbox when user agrees
- **Loading States**: Visual feedback during interactions
- **Error Handling**: Proper error states for form validation
- **Responsive Design**: Works on different screen sizes
- **Material 3 Design**: Follows latest Material Design principles

### 🎯 Key Benefits
- **Consistent UX**: Matches existing SignUp screen design
- **Accessibility**: Proper content descriptions and touch targets
- **Performance**: Efficient LazyColumn for large content
- **Maintainable**: Modular component structure
- **Extensible**: Easy to customize and extend

## 🌐 Internationalization Support

The components are ready for internationalization:
- All text strings can be externalized to string resources
- RTL layout support through Compose's automatic handling
- Proper text scaling for accessibility

## 📋 Content Structure

The Terms & Conditions and Privacy Policy include:

### Terms & Conditions
1. Introduction
2. Acceptance of Terms
3. Use of Service
4. User Accounts
5. Medical Information

### Privacy Policy
1. Information We Collect
2. How We Use Your Information
3. Information Sharing
4. Data Security
5. Your Rights
6. Contact Information

## 🔄 State Management

The components use proper Compose state management:
- `remember` for local component state
- `LaunchedEffect` for side effects
- `derivedStateOf` for computed values
- Proper state hoisting for parent-child communication

## 🎨 Theming

All components respect the app's theme:
- Uses theme colors from `Color.kt`
- Follows typography scale from `Type.kt`
- Consistent with Material 3 theming approach

---

**Note**: This implementation follows Jetpack Compose best practices and Material 3 design guidelines. The components are production-ready and can be easily integrated into the existing BrightCare Patient App architecture.

