# Forgot Password Process Components

This directory contains all the reusable components for the Forgot Password feature in the BrightCare Patient App.

## 📁 Components Overview

### 1. `ForgotPasswordTextField.kt`
- **Purpose**: Consistent text field for the forgot password flow
- **Design**: Matches the SignUp text field style with Material 3 design (no leading icons)
- **Features**: 
  - Real-time validation support using ValidationUtils
  - Password visibility toggle
  - Interactive error state handling
  - Focus management
  - Keyboard action handling

### 2. `ForgotPasswordButton.kt`
- **Purpose**: "Send OTP", "Verify OTP", and "Confirm Reset" buttons
- **Design**: Matches the SignUp button style and colors
- **Features**: 
  - Loading state with spinner
  - Multiple button types (Primary, Secondary, Outline, Text)
  - Icon support
  - Consistent styling with existing buttons

### 3. `ForgotPasswordBackButton.kt`
- **Purpose**: Consistent back button for all forgot password screens
- **Design**: Matches the SignUp back button style
- **Features**: 
  - Circular background
  - Arrow icon
  - Customizable colors
  - Proper touch target size

### 4. `OTPInputField.kt`
- **Purpose**: Six individual OTP input boxes for verification
- **Features**: 
  - Auto-focus management between fields
  - Numeric input only
  - Visual feedback for focused/filled states
  - Error state handling
  - Backspace navigation
  - Material 3 design principles

## 🎨 Design Consistency

All components follow the established design system:
- **Colors**: Uses Blue500 primary color, Gray tones, and WhiteBg background
- **Typography**: Consistent with Material 3 typography scale
- **Spacing**: 24dp horizontal padding, consistent vertical spacing
- **Shapes**: 12dp rounded corners for buttons and input fields
- **Elevation**: Minimal elevation following Material 3 principles

## 🚀 Usage Examples

### Basic Text Field Implementation

```kotlin
ForgotPasswordTextField(
    value = email,
    onValueChange = { input ->
        validateEmailRealTime(input)
    },
    placeholder = "Email Address",
    keyboardType = KeyboardType.Email,
    isError = isEmailError,
    errorMessage = "Please enter a valid email address"
)
```

### OTP Input Field

```kotlin
OTPInputField(
    otpValue = otpValue,
    onOtpChange = { newOtp -> otpValue = newOtp },
    isError = isOtpError,
    modifier = Modifier.fillMaxWidth()
)
```

### Button Implementation

```kotlin
ForgotPasswordButton(
    text = "Send OTP",
    onClick = { handleSendOtp() },
    loading = isLoading,
    enabled = !isLoading,
    type = ForgotPasswordButtonType.PRIMARY
)
```

## 📱 Screen Flow

The forgot password feature consists of three main screens:

### 1. `forgot-password-email.kt`
- Email input screen (no top app bar, matches signup design)
- Real-time email validation using ValidationUtils
- "Send OTP" functionality
- Navigation to OTP verification

### 2. `forgot-password.kt` 
- OTP verification screen (no top app bar, matches signup design)
- 6-digit OTP input with individual boxes
- Resend OTP with countdown timer
- OTP validation and verification

### 3. `change-password.kt`
- Password reset screen (no top app bar, matches signup design)
- New password and confirm password fields (no icons)
- Real-time password validation using ValidationUtils
- Interactive error handling like signup form

## 🔄 Navigation Flow

```
Login Screen → Forgot Password Email → OTP Verification → Change Password → Login Screen
```

## 🎯 Key Features

### Email Screen
- Real-time email format validation using ValidationUtils
- Interactive error states (like signup form)
- Loading states
- No top app bar (matches patient-signup.kt design)
- Back button design matches termsandcondition-and-privacypolicy.kt

### OTP Screen
- 6-digit OTP input with individual boxes
- Auto-focus management
- Resend OTP with 30-second countdown
- OTP validation (demo uses "123456" as valid OTP)
- Error messaging
- No top app bar (matches patient-signup.kt design)

### Change Password Screen
- Real-time password validation using ValidationUtils
- Interactive error states (like signup form)
- Confirm password matching with real-time validation
- No password requirements card (removed as requested)
- Same validation rules as signup
- No input field icons (matches SignUpForm.kt design)

## 🔐 Security Features

- Real-time email format validation using ValidationUtils
- Strong password requirements (same as signup):
  - At least 8 characters
  - Uppercase and lowercase letters
  - Numbers and special characters
  - No whitespace allowed
- Interactive validation with immediate feedback
- OTP simulation (6-digit numeric)
- Form validation on all inputs using existing ValidationUtils

## 🎨 UI/UX Features

- **Consistent Design**: Matches patient-signup.kt design exactly
- **No Top App Bar**: Uses same layout structure as signup screen
- **Back Button**: Uses TermsBackButton from termsandcondition-and-privacypolicy.kt
- **Material 3**: Full Material 3 design system compliance
- **Interactive Validation**: Real-time error feedback like signup form
- **No Icons**: Input fields without leading icons (matches SignUpForm.kt)
- **Loading States**: Visual feedback during operations
- **Error Handling**: Interactive error messages using ValidationUtils
- **Focus Management**: Proper keyboard navigation
- **Animation**: Smooth transitions between screens

## 🔧 Customization

### Colors
All colors are defined in `Color.kt` and can be easily customized:
- Primary: Blue500 (#4280EF)
- Background: WhiteBg (#F5F5F5)
- Error: Error (#EF4444)
- Gray scale for text and borders

### Validation
Validation logic uses the existing `ValidationUtils.kt` from the signup component, ensuring consistency across the app.

### Navigation
Navigation is handled through Jetpack Navigation Compose with smooth slide animations.

## 📋 Integration Notes

- Uses existing validation utilities from signup components
- Follows the same theming and color scheme
- Compatible with existing navigation structure
- Maintains consistency with Material 3 design principles
- Ready for backend integration (currently uses simulated API calls)

## 🔄 Backend Integration

To integrate with your backend:

1. **Email Screen**: Replace the simulated API call in `handleSendOtp()` with your actual OTP sending endpoint
2. **OTP Screen**: Replace the hardcoded OTP validation with your backend verification endpoint
3. **Change Password**: Replace the simulated password reset with your actual password update endpoint

## 🎨 Preview Support

All screens include `@Preview` composables for easy development and testing in Android Studio.

---

**Note**: This implementation follows Jetpack Compose best practices and Material 3 design guidelines. The components are production-ready and can be easily integrated into the existing BrightCare Patient App architecture.
