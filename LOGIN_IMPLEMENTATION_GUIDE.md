# Patient Login Implementation Guide

## Overview / Pangkalahatang Paglalarawan

This guide explains how to implement and use the patient login system with Firebase Authentication in your BrightCare Patient app. The system supports email/password, Google, and Facebook authentication.

Ang gabay na ito ay nagpapaliwanag kung paano ipatupad at gamitin ang patient login system gamit ang Firebase Authentication sa inyong BrightCare Patient app. Sumusuporta ang system sa email/password, Google, at Facebook authentication.

## Architecture / Arkitektura

### Data Layer / Data Layer
- **Models**: `data/model/patient-login.kt` - Contains all data models for login functionality
- **Repository**: `data/repository/patient-login.kt` - Handles Firebase Auth integration and business logic
- **Dependency Injection**: `di/AppModule.kt` - Provides repository instances

### UI Layer / UI Layer
- **ViewModel**: `ui/viewmodel/PatientSignInViewModel.kt` - Manages UI state and coordinates with repository
- **Screen**: `ui/screens/PatientLoginScreenIntegrated.kt` - Complete login screen implementation
- **Components**: `ui/component/Login-Component/` - Reusable login UI components

### Navigation / Navigation
- **Routes**: `navigation/NavigationRoutes.kt` - Defines navigation routes
- **Extensions**: `navigation/NavigationExtensions.kt` - Helper functions for navigation

## Implementation Steps / Mga Hakbang sa Pagpapatupad

### 1. Firebase Setup / Firebase Setup

Ensure your Firebase project is configured with:
Siguraduhing naka-configure ang inyong Firebase project na may:

```kotlin
// In your build.gradle (Module: app)
implementation 'com.google.firebase:firebase-auth-ktx'
implementation 'com.google.firebase:firebase-firestore-ktx'
implementation 'com.facebook.android:facebook-login'
implementation 'androidx.credentials:credentials'
implementation 'androidx.credentials:credentials-play-services-auth'
implementation 'com.google.android.libraries.identity.googleid:googleid'
```

### 2. Using the Login System / Paggamit ng Login System

#### Basic Usage / Pangunahing Paggamit

```kotlin
// In your composable
@Composable
fun MyLoginScreen(navController: NavController) {
    PatientLoginScreenIntegrated(
        navController = navController
    )
}
```

#### Custom Implementation / Custom na Pagpapatupad

```kotlin
@Composable
fun CustomLoginScreen(
    navController: NavController,
    viewModel: PatientSignInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    
    // Handle login results
    LaunchedEffect(uiState.loginResult) {
        when (val result = uiState.loginResult) {
            is LoginResult.Success -> {
                // Navigate based on profile completion
                if (!result.response.isProfileComplete) {
                    navController.navigateToCompleteProfile()
                } else {
                    navController.navigateToHome()
                }
            }
            is LoginResult.Error -> {
                // Handle error
                when (result.exception) {
                    is LoginException.EmailNotVerified -> {
                        // Show email verification dialog
                    }
                    else -> {
                        // Show error toast
                    }
                }
            }
            else -> { /* Loading or null */ }
        }
    }
    
    // Your UI implementation
    Column {
        // Email input
        TextField(
            value = email,
            onValueChange = viewModel::updateEmail,
            // ... other properties
        )
        
        // Password input
        TextField(
            value = password,
            onValueChange = viewModel::updatePassword,
            // ... other properties
        )
        
        // Login button
        Button(
            onClick = { viewModel.signInWithEmailAndPassword() },
            enabled = !uiState.isLoading
        ) {
            Text("Sign In")
        }
        
        // Google login
        Button(
            onClick = { viewModel.signInWithGoogle() }
        ) {
            Text("Sign in with Google")
        }
        
        // Facebook login
        Button(
            onClick = { viewModel.signInWithFacebook() }
        ) {
            Text("Sign in with Facebook")
        }
    }
}
```

### 3. Navigation Integration / Navigation Integration

```kotlin
// In your navigation graph
composable(NavigationRoutes.LOGIN) {
    PatientLoginScreenIntegrated(navController = navController)
}

// Handle navigation after login
fun handleLoginSuccess(
    navController: NavController,
    response: LoginResponse
) {
    navController.navigateAfterLogin(
        isEmailVerified = response.isEmailVerified,
        isProfileComplete = response.isProfileComplete
    )
}
```

## Key Features / Mga Pangunahing Feature

### Email/Password Authentication / Email/Password Authentication

```kotlin
// Automatic validation
viewModel.signInWithEmailAndPassword()

// Manual validation
val (emailError, passwordError) = ValidationUtils.validateLoginForm(email, password)
```

### Social Authentication / Social Authentication

#### Google Sign-In
- Uses Credential Manager API
- Automatic token handling
- Profile information extraction

#### Facebook Sign-In
- Uses Facebook SDK
- Callback manager integration
- Profile completion check

### Error Handling / Error Handling

```kotlin
sealed class LoginException(message: String) : Exception(message) {
    object InvalidCredentials : LoginException("Invalid email or password.")
    object UserNotFound : LoginException("No user found with this email address.")
    object WrongPassword : LoginException("Incorrect password.")
    object EmailNotVerified : LoginException("Please verify your email before signing in.")
    object UserDisabled : LoginException("This user account has been disabled.")
    object TooManyRequests : LoginException("Too many failed login attempts. Please try again later.")
    object NetworkError : LoginException("Network error occurred. Please check your connection.")
    // ... more exceptions
}
```

### Profile Completion Check / Profile Completion Check

The system automatically checks if the user's profile is complete after login:

```kotlin
data class ProfileCompletionStatus(
    val isComplete: Boolean,
    val missingFields: List<String> = emptyList()
) {
    companion object {
        val REQUIRED_FIELDS = listOf(
            "firstName",
            "lastName", 
            "phoneNumber",
            "dateOfBirth"
        )
    }
}
```

## Toast Messages / Toast Messages

The system provides predefined toast messages:

```kotlin
object LoginToastMessages {
    const val LOGIN_SUCCESS = "Welcome back!"
    const val EMAIL_VERIFICATION_REQUIRED = "Please verify your email before signing in. Check your inbox for the verification link."
    const val PROFILE_INCOMPLETE = "Please complete your profile to continue."
    const val GOOGLE_LOGIN_SUCCESS = "Successfully signed in with Google"
    const val FACEBOOK_LOGIN_SUCCESS = "Successfully signed in with Facebook"
    // ... more messages
}
```

## Validation / Validation

### Email Validation / Email Validation
```kotlin
ValidationUtils.isValidLoginEmail(email) // Less strict than signup
ValidationUtils.isValidEmail(email) // Strict validation
```

### Password Validation / Password Validation
```kotlin
ValidationUtils.isValidLoginPassword(password) // Minimum 6 characters, no spaces
ValidationUtils.isStrongPassword(password) // Strong password requirements
```

### Form Validation / Form Validation
```kotlin
val (emailError, passwordError) = ValidationUtils.validateLoginForm(email, password)
val isValid = ValidationUtils.isValidLoginForm(email, password)
```

## Security Considerations / Mga Konsiderasyon sa Security

1. **Email Verification**: Users must verify their email before accessing the app
2. **Profile Completion**: Incomplete profiles are redirected to complete profile screen
3. **Error Handling**: Specific error messages for different failure scenarios
4. **Rate Limiting**: Firebase handles rate limiting for failed attempts
5. **Secure Storage**: Firebase handles secure token storage

## Testing / Testing

### Unit Testing / Unit Testing
```kotlin
@Test
fun `test email password login success`() {
    // Test implementation
}

@Test
fun `test login validation`() {
    val (emailError, passwordError) = ValidationUtils.validateLoginForm("", "")
    assertNotNull(emailError)
    assertNotNull(passwordError)
}
```

### Integration Testing / Integration Testing
```kotlin
@Test
fun `test complete login flow`() {
    // Test complete flow from UI to repository
}
```

## Troubleshooting / Troubleshooting

### Common Issues / Mga Karaniwang Issue

1. **Google Sign-In Not Working**
   - Check Google Web Client ID in `PatientLoginRepository`
   - Ensure SHA-1 fingerprint is added to Firebase console
   - Verify Google Sign-In is enabled in Firebase Auth

2. **Facebook Sign-In Not Working**
   - Check Facebook App ID configuration
   - Ensure Facebook Login is enabled in Firebase Auth
   - Verify Facebook app is in live mode

3. **Email Verification Not Working**
   - Check Firebase Auth settings
   - Ensure email templates are configured
   - Verify SMTP settings

4. **Navigation Issues**
   - Check route definitions in `NavigationRoutes`
   - Ensure proper navigation graph setup
   - Verify navigation extensions usage

## Best Practices / Mga Best Practice

1. **State Management**: Use StateFlow for reactive UI updates
2. **Error Handling**: Provide user-friendly error messages
3. **Loading States**: Show loading indicators during authentication
4. **Validation**: Validate inputs before sending to Firebase
5. **Navigation**: Clear back stack after successful login
6. **Security**: Never store passwords locally
7. **User Experience**: Provide clear feedback for all actions

## Dependencies / Mga Dependency

Make sure these dependencies are added to your `build.gradle`:

```kotlin
// Firebase
implementation 'com.google.firebase:firebase-auth-ktx:22.3.0'
implementation 'com.google.firebase:firebase-firestore-ktx:24.9.1'

// Google Sign-In
implementation 'androidx.credentials:credentials:1.2.2'
implementation 'androidx.credentials:credentials-play-services-auth:1.2.2'
implementation 'com.google.android.libraries.identity.googleid:googleid:1.1.0'

// Facebook Login
implementation 'com.facebook.android:facebook-login:16.2.0'

// Hilt for Dependency Injection
implementation 'com.google.dagger:hilt-android:2.48'
kapt 'com.google.dagger:hilt-compiler:2.48'
implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'

// Compose
implementation 'androidx.compose.ui:ui:1.5.4'
implementation 'androidx.compose.material3:material3:1.1.2'
implementation 'androidx.navigation:navigation-compose:2.7.5'
```

## Support / Support

For issues or questions about the login implementation:
Para sa mga issue o tanong tungkol sa login implementation:

1. Check the logs for detailed error messages
2. Verify Firebase configuration
3. Test with different user accounts
4. Check network connectivity
5. Review Firebase Auth console for user status

---

**Note**: This implementation follows Firebase Auth best practices and provides a secure, user-friendly login experience for the BrightCare Patient app.

**Tandaan**: Ang implementation na ito ay sumusunod sa Firebase Auth best practices at nagbibigay ng secure, user-friendly na login experience para sa BrightCare Patient app.

