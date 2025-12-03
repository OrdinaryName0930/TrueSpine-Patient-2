# Patient Signup Backend Implementation

## Overview / Pangkalahatang Paglalarawan

This document outlines the complete backend implementation for the patient signup flow in the BrightCare Patient Kotlin mobile application. The implementation integrates Firebase Authentication for email/password, Google, and Facebook sign-in with proper error handling and navigation.

Ang dokumentong ito ay naglalaman ng kumpletong backend implementation para sa patient signup flow sa BrightCare Patient Kotlin mobile application. Ang implementation ay nag-integrate ng Firebase Authentication para sa email/password, Google, at Facebook sign-in na may tamang error handling at navigation.

## Architecture / Arkitektura

The implementation follows the MVVM (Model-View-ViewModel) architecture pattern with the following layers:

### 1. Data Layer / Data Layer
- **Models** (`data/model/patient-signup.kt`): Data classes for requests, responses, and authentication states
- **Repository** (`data/repository/patient-signup.kt`): Handles Firebase Auth operations and business logic
- **Dependency Injection** (`di/AppModule.kt`): Provides Firebase Auth and Repository instances

### 2. Presentation Layer / Presentation Layer
- **ViewModel** (`ui/viewmodel/PatientSignUpViewModel.kt`): Manages UI state and coordinates with repository
- **Screen** (`ui/screens/patient-signup.kt`): Updated to integrate with ViewModel and handle navigation

## Key Features / Mga Pangunahing Feature

### ✅ Email/Password Signup
- **Validation**: Email format and password strength validation before Firebase call
- **Firebase Integration**: Creates user account with `createUserWithEmailAndPassword`
- **Email Verification**: Automatically sends verification email after successful signup
- **Navigation**: Redirects to login screen after successful signup
- **Error Handling**: Displays "email already in use" error below email field as requested

### ✅ Google Sign-In
- **Modern Implementation**: Uses Credential Manager API (recommended by Google)
- **Firebase Integration**: Authenticates with Firebase using Google ID token
- **Navigation**: Redirects to CompleteProfile screen after successful login
- **Error Handling**: Comprehensive error handling for Google authentication

### ✅ Facebook Sign-In (Placeholder)
- **Structure Ready**: Repository method created for Facebook integration
- **Future Implementation**: Ready for Facebook SDK integration when needed

### ✅ Error Handling & Validation
- **Bilingual Messages**: Error messages in both English and Tagalog
- **Field-Specific Errors**: Email errors display below email field
- **General Errors**: Other errors display in error card above form
- **Real-time Validation**: Form validation happens as user types
- **Firebase Exception Mapping**: Maps Firebase errors to user-friendly messages

### ✅ Navigation Integration
- **Conditional Navigation**: 
  - Email/password signup → Login screen
  - Social login → CompleteProfile screen
- **State Management**: Proper cleanup and state management during navigation
- **Terms Integration**: Maintains existing terms and conditions flow

## File Structure / File Structure

```
app/src/main/java/com/brightcare/patient/
├── data/
│   ├── model/
│   │   └── patient-signup.kt          # Data models and sealed classes
│   └── repository/
│       └── patient-signup.kt          # Firebase Auth repository
├── navigation/
│   ├── NavigationRoutes.kt            # Centralized route definitions
│   ├── NavigationGraph.kt             # Navigation graph with all screens
│   └── NavigationExtensions.kt        # Type-safe navigation extensions
├── ui/
│   ├── viewmodel/
│   │   └── PatientSignUpViewModel.kt  # ViewModel for signup screen
│   └── screens/
│       └── patient-signup.kt          # Updated signup screen
├── BrightCarePatientApplication.kt    # Application class
└── MainActivity.kt                    # Updated with navigation graph
```

## Implementation Details / Mga Detalye ng Implementation

### 1. Data Models

```kotlin
// Authentication result wrapper
sealed class AuthResult {
    data class Success(val response: SignUpResponse) : AuthResult()
    data class Error(val exception: AuthException) : AuthResult()
    object Loading : AuthResult()
}

// Custom exception for authentication errors
sealed class AuthException(message: String) : Exception(message) {
    object EmailAlreadyInUse : AuthException("The email address is already in use by another account.")
    object WeakPassword : AuthException("The password is too weak.")
    // ... other exceptions
}
```

### 2. Repository Pattern

The `PatientSignUpRepository` handles all Firebase Auth operations:

```kotlin
@Singleton
class PatientSignUpRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val context: Context
) {
    suspend fun signUpWithEmailAndPassword(request: SignUpRequest): AuthResult
    suspend fun signInWithGoogle(): AuthResult
    suspend fun signInWithFacebook(): AuthResult
    suspend fun sendEmailVerification()
    // ... other methods
}
```

### 3. ViewModel Integration

The `PatientSignUpViewModel` manages UI state and coordinates with the repository:

```kotlin
@HiltViewModel
class PatientSignUpViewModel @Inject constructor(
    private val signUpRepository: PatientSignUpRepository
) : ViewModel() {
    val authState: StateFlow<AuthResult> = signUpRepository.authState
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()
    
    fun signUpWithEmailAndPassword(formState: SignUpFormState)
    fun signInWithGoogle()
    fun signInWithFacebook()
    // ... other methods
}
```

### 4. Error Handling Strategy

**Email Already in Use Error:**
- Detected by Firebase error code `ERROR_EMAIL_ALREADY_IN_USE`
- Mapped to `AuthException.EmailAlreadyInUse`
- Displayed below email field in both English and Tagalog
- Form state updated to show email field error

**Other Errors:**
- Network errors, weak password, invalid email, etc.
- Displayed in error card above the form
- Bilingual error messages for better user experience

### 5. Navigation Flow

```
Email/Password Signup Success → Login Screen
Google/Facebook Login Success → CompleteProfile Screen
Any Error → Stay on Signup Screen with Error Message
```

### 6. Navigation Folder Implementation

**NavigationRoutes.kt**: Centralized route definitions
```kotlin
object NavigationRoutes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val COMPLETE_PROFILE = "complete_profile"
    // ... other routes
}
```

**NavigationGraph.kt**: Complete navigation setup with animations
```kotlin
@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String = NavigationRoutes.LOGIN
) {
    NavHost(navController = navController, startDestination = startDestination) {
        // All screen composables with transitions
    }
}
```

**NavigationExtensions.kt**: Type-safe navigation functions
```kotlin
fun NavController.navigateToLogin(clearBackStack: Boolean = false)
fun NavController.navigateToCompleteProfile(clearBackStack: Boolean = true)
// ... other extension functions
```

## Setup Instructions / Mga Tagubilin sa Setup

### 1. Dependencies Added / Mga Dependencies na Naidagdag

```kotlin
// Dependency Injection
implementation("com.google.dagger:hilt-android:2.48")
kapt("com.google.dagger:hilt-compiler:2.48")
implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

// Coroutines and Flow
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
```

### 2. Google Sign-In Configuration

**Important**: Update the `GOOGLE_WEB_CLIENT_ID` in `PatientSignUpRepository.kt`:

```kotlin
companion object {
    private const val GOOGLE_WEB_CLIENT_ID = "YOUR_ACTUAL_GOOGLE_WEB_CLIENT_ID"
}
```

**To get your Google Web Client ID:**
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project
3. Navigate to APIs & Services > Credentials
4. Find your OAuth 2.0 client ID for Web application
5. Copy the Client ID and replace `YOUR_ACTUAL_GOOGLE_WEB_CLIENT_ID`

### 3. Facebook Login (Optional)

To add Facebook Login later:
1. Uncomment the Facebook dependency in `build.gradle.kts`
2. Follow Facebook SDK setup instructions
3. Implement the Facebook login logic in the repository

### 4. Firebase Configuration

Ensure your `google-services.json` is properly configured with:
- Authentication enabled
- Email/Password provider enabled
- Google Sign-In provider enabled (with correct client ID)

## Usage Example / Halimbawa ng Paggamit

The signup screen now automatically handles all backend operations:

```kotlin
// In MainActivity.kt - the screen is already integrated
PatientSignUpScreen(
    navController = navController,
    autoCheckTerms = autoCheckTerms || preserveCheckboxState,
    onBackClick = { finish() },
    onSignInClick = { navController.navigate("login") }
)
```

The ViewModel automatically:
- Validates form input
- Calls Firebase Auth
- Handles errors and displays appropriate messages
- Navigates to correct screen based on signup type
- Sends email verification for email/password signups

## Error Messages / Mga Error Messages

All error messages are provided in both English and Tagalog:

- **Email Already in Use**: "Ang email address na ito ay ginagamit na ng ibang account. / This email address is already in use by another account."
- **Weak Password**: "Ang password ay masyadong mahina. / The password is too weak."
- **Network Error**: "Network error. Pakisuri ang inyong internet connection. / Network error. Please check your internet connection."

## Testing / Pagsusulit

To test the implementation:

1. **Email/Password Signup**:
   - Try with valid email and strong password
   - Try with existing email (should show error below email field)
   - Try with weak password (should show error message)

2. **Google Sign-In**:
   - Ensure Google Web Client ID is configured
   - Test with valid Google account
   - Should navigate to CompleteProfile screen

3. **Validation**:
   - Test email format validation
   - Test password strength validation
   - Test terms acceptance requirement

## Next Steps / Mga Susunod na Hakbang

1. **Configure Google Web Client ID** in the repository
2. **Test the implementation** with actual Firebase project
3. **Add Facebook Login** if needed (SDK integration required)
4. **Customize error messages** if different wording is preferred
5. **Add analytics tracking** for signup events if needed

## Notes / Mga Tala

- The implementation maintains backward compatibility with existing UI components
- All existing validation logic is preserved and enhanced
- The bilingual error messages follow the user rule of responding in English and Tagalog
- The navigation flow matches the requirements (email signup → login, social login → complete profile)
- Email verification is automatically sent after successful email/password signup
