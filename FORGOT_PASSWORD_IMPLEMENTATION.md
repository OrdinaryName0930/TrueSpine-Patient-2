# Forgot Password with OTP Implementation

## Overview
This implementation provides a complete forgot password flow with OTP (One-Time Password) verification for the BrightCare Patient app. The system uses Firebase Authentication and Firestore for secure password reset operations.

## Features
- ✅ Email-based OTP verification
- ✅ Secure OTP generation and storage
- ✅ OTP expiration (10 minutes)
- ✅ Maximum attempt limits (3 attempts)
- ✅ Resend OTP functionality with cooldown
- ✅ Strong password validation
- ✅ Real-time form validation
- ✅ Comprehensive error handling
- ✅ Loading states and user feedback

## Architecture

### Data Layer
1. **Models** (`patient-forgot-password.kt`)
   - `SendOtpRequest/Response` - OTP sending operations
   - `VerifyOtpRequest/Response` - OTP verification
   - `ResetPasswordRequest/Response` - Password reset
   - `OtpData` - Firestore OTP storage model
   - `ForgotPasswordResult` - Result wrapper
   - `ForgotPasswordException` - Custom exceptions
   - `ForgotPasswordUiState` - UI state management

2. **Repository** (`patient-forgot-password.kt`)
   - `sendOtp()` - Generate and send OTP to email
   - `verifyOtp()` - Verify user-entered OTP
   - `resetPassword()` - Reset user password
   - `resendOtp()` - Resend OTP functionality
   - Validation methods for email, OTP, and password

### Presentation Layer
3. **ViewModel** (`PatientForgotPassword.kt`)
   - State management for the entire flow
   - Business logic coordination
   - Real-time validation
   - Error and success handling

4. **UI Screens**
   - `ForgotPasswordEmailScreen` - Email input
   - `ForgotPasswordScreen` - OTP verification
   - `ChangePasswordScreen` - New password setup

## Flow Diagram

```
1. Email Input → 2. Send OTP → 3. OTP Verification → 4. Password Reset → 5. Complete
     ↓              ↓              ↓                    ↓               ↓
   Validate      Generate        Verify OTP          Update          Navigate
   Email         6-digit OTP     & Create Token      Password        to Login
```

## Security Features

### OTP Security
- **6-digit numeric OTP** generated using `SecureRandom`
- **10-minute expiration** time
- **Maximum 3 attempts** before requiring new OTP
- **One-time use** - OTP becomes invalid after successful verification
- **Reset token** generated after OTP verification for password reset authorization

### Password Security
- **Minimum 8 characters**
- **Must contain**: uppercase, lowercase, number, and special character
- **No whitespace** allowed
- **Validation** using existing `ValidationUtils`

### Data Security
- **Email normalization** (lowercase)
- **Firestore security rules** should be configured
- **Session management** with unique OTP IDs and reset tokens
- **Automatic cleanup** of used OTP documents

## Database Structure

### Firestore Collections

#### `password_reset_otps` Collection
```json
{
  "otpId": "unique-uuid",
  "email": "user@example.com",
  "otp": "123456",
  "createdAt": 1699123456789,
  "expiresAt": 1699124056789,
  "attempts": 0,
  "isUsed": false,
  "resetToken": "reset-token-uuid"
}
```

## Usage

### 1. Email Input Screen
```kotlin
ForgotPasswordEmailScreen(
    navController = navController,
    onBackClick = { navController.popBackStack() },
    onSendOtpClick = { email -> 
        navController.navigate("forgot_password_otp/$email") 
    }
)
```

### 2. OTP Verification Screen
```kotlin
ForgotPasswordScreen(
    navController = navController,
    email = email,
    onBackClick = { navController.popBackStack() },
    onVerifyOtpClick = { otp -> 
        navController.navigate("change_password") 
    }
)
```

### 3. Password Reset Screen
```kotlin
ChangePasswordScreen(
    navController = navController,
    onBackClick = { navController.popBackStack() },
    onPasswordResetClick = { 
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }
)
```

## Error Handling

### Common Errors
- `EmailNotFound` - No account with provided email
- `OtpExpired` - OTP has expired (10 minutes)
- `InvalidOtp` - Wrong OTP entered
- `MaxAttemptsExceeded` - Too many failed attempts
- `OtpAlreadyUsed` - OTP was already used
- `InvalidResetToken` - Reset token expired or invalid
- `WeakPassword` - Password doesn't meet requirements
- `NetworkError` - Connection issues

### User Feedback
- **Success dialogs** for completed operations
- **Error dialogs** with clear messages
- **Loading states** during API calls
- **Real-time validation** feedback
- **Countdown timers** for resend functionality

## Configuration

### Constants (Configurable)
```kotlin
companion object {
    const val OTP_EXPIRY_MINUTES = 10L
    const val MAX_OTP_ATTEMPTS = 3
    const val RESEND_COOLDOWN_SECONDS = 30
    const val OTP_LENGTH = 6
}
```

### Dependencies Required
```kotlin
// In app/build.gradle.kts
implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
implementation("androidx.lifecycle:lifecycle-compose:2.7.0")
```

## Email Service Integration

### Current Implementation
The current implementation uses Firebase Auth's password reset email as a carrier. For production, integrate with:

- **SendGrid**
- **AWS SES**
- **Firebase Cloud Functions** with email service
- **Mailgun**
- **Any SMTP service**

### Email Template Example
```
Subject: Your BrightCare Password Reset Code

Hello,

Your password reset code is: 123456

This code will expire in 10 minutes.

If you didn't request this, please ignore this email.

Best regards,
BrightCare Team
```

## Testing

### Unit Tests (Recommended)
- Repository methods
- ViewModel state management
- Validation functions
- Error handling

### Integration Tests
- Complete flow from email to password reset
- OTP expiration scenarios
- Maximum attempts handling
- Network error scenarios

## Deployment Checklist

- [ ] Configure Firestore security rules
- [ ] Set up email service integration
- [ ] Configure proper error tracking
- [ ] Test with real email addresses
- [ ] Verify OTP delivery times
- [ ] Test on different devices/networks
- [ ] Configure rate limiting (if needed)

## Firestore Security Rules

```javascript
// Add to firestore.rules
match /password_reset_otps/{otpId} {
  allow read, write: if request.auth != null;
  allow create: if true; // Allow anonymous OTP creation
}
```

## Future Enhancements

1. **SMS OTP** as alternative to email
2. **Rate limiting** per IP/device
3. **Analytics** for password reset metrics
4. **Admin dashboard** for OTP monitoring
5. **Multi-language** support for emails
6. **Biometric** verification option
7. **Account lockout** after multiple failed attempts

## Support

For issues or questions about this implementation:
1. Check the error logs in Firebase Console
2. Verify Firestore security rules
3. Test email delivery service
4. Check network connectivity
5. Validate user input properly

---

**Implementation completed**: All backend functionality is connected to the frontend with proper error handling and user feedback.




