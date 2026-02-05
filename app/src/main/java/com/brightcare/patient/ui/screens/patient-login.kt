package com.brightcare.patient.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.data.model.LoginResult
import com.brightcare.patient.data.model.LoginException
import com.brightcare.patient.data.model.LoginToastMessages
import com.brightcare.patient.navigation.navigateAfterLogin
import com.brightcare.patient.navigation.navigateToSignUp
import com.brightcare.patient.ui.component.login_component.*
import com.brightcare.patient.ui.component.signup_component.ValidationUtils
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.PatientSignInViewModel
import com.brightcare.patient.ui.BrightCareToast
import com.brightcare.patient.ui.rememberToastState
import com.brightcare.patient.ui.showInfo
import com.brightcare.patient.ui.showError
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientLoginScreen(
    navController: NavController,
    viewModel: PatientSignInViewModel = hiltViewModel(),
    clearStatesOnInit: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Custom toast state
    val toastState = rememberToastState()
    
    // Collect state from ViewModel with error handling
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    val loginResult by viewModel.loginResult.collectAsState()
    
    // Note: If you experience crashes, you can temporarily use SimpleLoginFallback for debugging
    
    // State for login attempt errors (separate from validation)
    var loginAttemptError by remember { mutableStateOf<String?>(null) }
    
    // State for required field errors
    var emailRequiredError by remember { mutableStateOf(false) }
    var emailFormatError by remember { mutableStateOf(false) }
    var passwordRequiredError by remember { mutableStateOf(false) }
    
    // Create form state for existing components
    val formState = remember(email, password, loginAttemptError, emailRequiredError, emailFormatError, passwordRequiredError, uiState.credentialError) {
        LoginFormState(
            email = email,
            password = password,
            loginAttemptError = loginAttemptError,
            emailRequiredError = emailRequiredError,
            emailFormatError = emailFormatError,
            loginCredentialError = uiState.credentialError,
            passwordRequiredError = passwordRequiredError
        )
    }
    
    // Facebook Activity Result Launcher
    val facebookLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Facebook login handled by callback manager in ViewModel
        }
    }
    
    // Clear login states when coming from sign-up
    LaunchedEffect(clearStatesOnInit) {
        if (clearStatesOnInit) {
            viewModel.clearAllLoginStates()
        }
    }
    
    // Handle login results
    LaunchedEffect(loginResult) {
        when (val result = loginResult) {
            is LoginResult.Success -> {
                val response = result.response
                
                // Clear any errors
                loginAttemptError = null
                emailRequiredError = false
                emailFormatError = false
                passwordRequiredError = false
                // Note: credentialError is cleared by ViewModel
                
                // Show success toast
                val message = when (response.providerId) {
                    "google.com" -> LoginToastMessages.GOOGLE_LOGIN_SUCCESS
                    "facebook.com" -> LoginToastMessages.FACEBOOK_LOGIN_SUCCESS
                    else -> LoginToastMessages.LOGIN_SUCCESS
                }
                
                toastState.showInfo(message)
                
                // Wait for toast to be visible before navigating
                delay(1500L) // 1.5 seconds delay to show toast
                
                // Navigate based on profile completion status
                navController.navigateAfterLogin(
                    isEmailVerified = response.isEmailVerified,
                    isProfileComplete = response.isProfileComplete
                )
                
                // Clear the result after handling
                viewModel.clearLoginResult()
            }
            
            is LoginResult.Error -> {
                when (result.exception) {
                    is LoginException.EmailNotVerified -> {
                        // Show email verification dialog instead of setting form error
                        viewModel.showEmailVerificationDialog()
                    }
                    else -> {
                        // LoginException errors are handled by ViewModel and displayed in Error Message Card
                        // No toast needed for these - they show in the UI state error card
                    }
                }
                
                // Clear the result after a delay
                delay(100)
                viewModel.clearLoginResult()
            }
            
            else -> { /* Loading or null state */ }
        }
    }
    
    // Email Verification Dialog
    if (uiState.showEmailVerificationDialog) {
        EmailVerificationDialog(
            onDismiss = { viewModel.clearEmailVerificationDialog() },
            onResendClick = { 
                val throttleResult = viewModel.sendEmailVerification()
                if (throttleResult.canSend) {
                    toastState.showInfo("Verification email sent! Please check your inbox or spam.")
                    viewModel.clearEmailVerificationDialog()
                } else {
                    toastState.showError("Please wait ${throttleResult.remainingTimeFormatted} to send new verification email")
                }
            },
            viewModel = viewModel
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {

        // Title
        Text(
            text = "Sign in",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp
            ),
            color = Blue500,
            modifier = Modifier.padding(bottom = 8.dp, top = 40.dp, start = 15.dp)
        )

        // Error Message Card
        uiState.errorMessage?.let { errorMessage ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Login Form
        LoginForm(
            formState = formState,
            onFormStateChange = { newState ->
                viewModel.updateEmail(newState.email)
                viewModel.updatePassword(newState.password)
                // Clear login attempt error when user types
                if (newState.loginAttemptError == null && loginAttemptError != null) {
                    loginAttemptError = null
                }
                // Clear required field errors when user types
                if (!newState.emailRequiredError && emailRequiredError) {
                    emailRequiredError = false
                }
                if (!newState.emailFormatError && emailFormatError) {
                    emailFormatError = false
                }
                if (!newState.passwordRequiredError && passwordRequiredError) {
                    passwordRequiredError = false
                }
                // Note: loginCredentialError is cleared by ViewModel when user types
            },
            modifier = Modifier
        )

        // Forgot Password Link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    // Navigate to forgot password screen
                    navController.navigate("forgot_password_email")
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium.merge(
                        TextStyle(
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                    color = Blue500
                )
            }
        }

        // Login Button
        LoginButton(
            text = "Sign in",
            onClick = {
                // Validate required fields and email format before attempting login
                val isEmailEmpty = email.trim().isEmpty()
                val isPasswordEmpty = password.trim().isEmpty()
                val isEmailFormatInvalid = !isEmailEmpty && !ValidationUtils.isValidEmail(email.trim())
                
                if (isEmailEmpty || isPasswordEmpty || isEmailFormatInvalid) {
                    // Set validation errors
                    emailRequiredError = isEmailEmpty
                    emailFormatError = isEmailFormatInvalid
                    passwordRequiredError = isPasswordEmpty
                    // Clear any existing login attempt errors
                    loginAttemptError = null
                    viewModel.clearErrorMessage()
                } else {
                    // Clear any validation errors and proceed with login
                    emailRequiredError = false
                    emailFormatError = false
                    passwordRequiredError = false
                    viewModel.clearErrorMessage()
                    viewModel.signInWithEmailAndPassword()
                }
            },
            type = LoginButtonType.PRIMARY,
            loading = uiState.isEmailPasswordLoading,
            enabled = !uiState.isEmailPasswordLoading && email.trim().isNotEmpty() && password.trim().isNotEmpty(), // Enabled only when BOTH fields have content and not loading
            modifier = Modifier.padding(start = 16.dp, end = 15.dp, top = 24.dp)
        )

        // Sign Up Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            TextButton(
                onClick = {
                    navController.navigateToSignUp()
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Sign up",
                    style = MaterialTheme.typography.bodyMedium.merge(
                        TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                    color = Blue500
                )
            }
        }

        // Social login section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 58.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Or continue with",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Blue500
            )
        }

        // Social Login Buttons - Google centered, Facebook hidden but preserved
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Google button centered
            SocialIconButton(
                provider = SocialProvider.GOOGLE,
                onClick = {
                    // Clear any form validation errors before social login
                    emailRequiredError = false
                    emailFormatError = false
                    passwordRequiredError = false
                    loginAttemptError = null
                    viewModel.clearErrorMessage()
                    viewModel.signInWithGoogle()
                },
                modifier = Modifier.size(45.dp),
                enabled = !uiState.isGoogleLoading, // Disabled only when Google login is loading
                loading = uiState.isGoogleLoading,
                logoSize = 20
            )
            
            // Facebook button hidden but code preserved for future use (positioned outside layout flow)
            SocialIconButton(
                provider = SocialProvider.FACEBOOK,
                onClick = {
                    // Clear any form validation errors before social login
                    emailRequiredError = false
                    emailFormatError = false
                    passwordRequiredError = false
                    loginAttemptError = null
                    viewModel.clearErrorMessage()
                    viewModel.signInWithFacebook()
                },
                modifier = Modifier
                    .size(45.dp)
                    .alpha(0f) // Hide Facebook button while preserving functionality
                    .offset(x = 100.dp), // Move out of the way but keep code intact
                enabled = false, // Disable interaction while hidden
                loading = uiState.isFacebookLoading,
                logoSize = 23
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
    
    // Custom Toast Overlay
    Box(modifier = Modifier.fillMaxSize()) {
        BrightCareToast(
            toastState = toastState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Email Verification Dialog
 */
@Composable
private fun EmailVerificationDialog(
    onDismiss: () -> Unit,
    onResendClick: () -> Unit,
    viewModel: PatientSignInViewModel = hiltViewModel()
) {
    // Real-time throttle status with countdown timer
    var throttleResult by remember { mutableStateOf(viewModel.canSendEmailVerification()) }
    val canResend = throttleResult.canSend
    
    // Update throttle status every second for real-time countdown
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Update every second
            val newResult = viewModel.getEmailVerificationThrottleResult()
            throttleResult = newResult
            if (newResult.canSend) break // Stop updating when can send
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Email Verification Required",
                style = MaterialTheme.typography.headlineSmall,
                color = Blue500
            )
        },
        text = {
            Column {
                Text(
                    text = "Please verify your email address before signing in. Check your inbox or spam for the verification link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
                
                // Show throttle message if applicable
                if (!canResend) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You can request another verification email in ${throttleResult.remainingTimeFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },

        // ✅ Use only confirmButton and build custom row INSIDE it
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // ✅ LEFT – Resend Email (keeps original functionality)
                TextButton(
                    onClick = onResendClick,
                    enabled = canResend
                ) {
                    Text(
                        text = if (canResend) "Resend Email" else "Wait ${throttleResult.remainingTimeFormatted}",
                        color = if (canResend) Blue500 else Gray600,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // ✅ RIGHT – OK (keeps original functionality)
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "OK",
                        color = Gray600
                    )
                }
            }
        }
    )
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PatientLoginScreenPreview() {
    BrightCarePatientTheme {
        PatientLoginScreen(
            navController = rememberNavController(),
            clearStatesOnInit = false
        )
    }
}


@Preview(showBackground = true, name = "Email Verification Dialog")
@Composable
fun EmailVerificationDialogPreview() {
    BrightCarePatientTheme {
        // Note: Preview cannot use actual ViewModel, so we'll create a simple version
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "Email Verification Required",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Blue500
                )
            },
            text = {
                Column {
                    Text(
                        text = "Please verify your email address before signing in. Check your inbox or spam for the verification link.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {}) {
                        Text(
                            text = "Resend Email",
                            color = Blue500,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TextButton(onClick = {}) {
                        Text(
                            text = "OK",
                            color = Gray600
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PatientLoginScreenWithDataPreview() {
    BrightCarePatientTheme {
        PatientLoginScreen(
            navController = rememberNavController(),
            clearStatesOnInit = false
        )
    }
}