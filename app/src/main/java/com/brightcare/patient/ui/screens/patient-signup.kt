package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.data.model.AuthResult
import com.brightcare.patient.data.model.EmailVerificationState
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.navigation.navigateToCompleteProfile
import com.brightcare.patient.navigation.navigateToLogin
import com.brightcare.patient.navigation.navigateToTermsAndConditions
import com.brightcare.patient.ui.component.signup_component.*
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.PatientSignUpViewModel
import com.brightcare.patient.ui.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientSignUpScreen(
    navController: NavController,
    onBackClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    // This parameter will be set to true when user comes back from Terms screen
    autoCheckTerms: Boolean = false
) {
    val context = LocalContext.current
    val activity = context as androidx.activity.ComponentActivity
    val viewModel: PatientSignUpViewModel = viewModel { PatientSignUpViewModel(context) }
    // Collect ViewModel states
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val emailVerificationState by viewModel.emailVerificationState.collectAsStateWithLifecycle()
    
    // Toast state for showing email verification status
    val toastState = rememberToastState()
    
    // Use rememberSaveable to persist form data across navigation
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    
    // Create and manage form state properly
    var formState by remember {
        mutableStateOf(SignUpFormState(
            email = email,
            password = password,
            confirmPassword = confirmPassword
        ))
    }
    
    // Initialize form state with saved values when component loads
    LaunchedEffect(Unit) {
        formState = formState.copy(
            email = email,
            password = password,
            confirmPassword = confirmPassword
        )
    }
    
    // Update individual fields when form state changes (for persistence)
    LaunchedEffect(formState.email, formState.password, formState.confirmPassword) {
        email = formState.email
        password = formState.password
        confirmPassword = formState.confirmPassword
    }
    
    var termsAccepted by remember { mutableStateOf(autoCheckTerms) }
    var isTermsError by remember { mutableStateOf(false) }
    
    // Update terms acceptance when autoCheckTerms changes
    LaunchedEffect(autoCheckTerms) {
        if (autoCheckTerms) {
            termsAccepted = true
            isTermsError = false
        }
    }
    
    // Handle navigation based on auth state
    LaunchedEffect(authState, uiState.isSignUpSuccessful) {
        when (authState) {
            is AuthResult.Success -> {
                if (uiState.isSignUpSuccessful) {
                    // Show success toast for email verification sent
                    toastState.showInfo(
                        message = EmailVerificationState.VERIFICATION_SENT_MESSAGE,
                        duration = EmailVerificationState.SUCCESS_DURATION,
                    )
                    
                    // Wait a bit for toast to show before navigating
                    kotlinx.coroutines.delay(1000)
                    
                    // Email/password signup successful - navigate to login
                    navController.navigateToLogin(clearBackStack = true)
                } else if (uiState.isSocialLoginSuccessful) {
                    // Social login successful - navigate to complete profile
                    navController.navigateToCompleteProfile(clearBackStack = true)
                }
            }
            else -> { /* Handle other states if needed */ }
        }
    }
    
    // Handle email verification errors separately
    LaunchedEffect(emailVerificationState.error) {
        emailVerificationState.error?.let { error ->
            // Show error toast if verification failed
            toastState.showError(
                message = EmailVerificationState.VERIFICATION_FAILED_MESSAGE,
                duration = EmailVerificationState.ERROR_DURATION,
                actionLabel = EmailVerificationState.ACTION_TRY_AGAIN,
                onActionClick = {
                    // Retry sending verification email
                    viewModel.resendEmailVerification()
                }
            )
        }
    }
    
    // Clear errors when form state changes
    LaunchedEffect(formState.email, formState.password) {
        if (uiState.emailFieldError != null || uiState.errorMessage != null) {
            viewModel.clearErrors()
        }
    }

    val scrollState = rememberScrollState()

    // 🔹 Real-time validation: check if all fields are valid and terms accepted
    val isFormValid by remember(formState, termsAccepted, uiState.isLoading) {
        derivedStateOf {
            formState.email.isNotBlank() &&
                    formState.password.isNotBlank() &&
                    formState.confirmPassword.isNotBlank() &&
                    !formState.isEmailError &&
                    !formState.isPasswordError &&
                    !formState.isConfirmPasswordError &&
                    termsAccepted &&
                    !uiState.isLoading
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start
        ) {

        // Title
        Text(
            text = "Sign up",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp
            ),
            color = Blue500,
            modifier = Modifier.padding(bottom = 8.dp, top = 40.dp, start = 15.dp)
        )
        // Show general error message if present
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
        // Sign Up Form
        SignUpForm(
            formState = formState.copy(
                // Override email error with ViewModel error if present
                isEmailError = formState.isEmailError || uiState.emailFieldError != null,
                emailErrorMessage = uiState.emailFieldError ?: formState.emailErrorMessage
            ),
            onFormStateChange = { formState = it },
            modifier = Modifier
        )


        // Terms Checkbox with Navigation
        TermsCheckbox(
            isChecked = termsAccepted,
            onCheckedChange = {
                termsAccepted = it
                isTermsError = false
            },
            onTermsClick = {
                // Save current checkbox state before navigating
                navController.navigateToTermsAndConditions(preserveCheckbox = termsAccepted)
            },
            onPrivacyClick = {
                // Optional: handle privacy separately if you have another screen
                navController.navigateToTermsAndConditions(preserveCheckbox = termsAccepted)
            },
            isError = isTermsError,
            modifier = Modifier.padding(start = 10.dp, end = 12.dp, bottom = 15.dp)
        )

        // Sign Up Button with reactive enable/disable
        SignUpButton(
            text = "Sign up",
            onClick = {
                if (!isFormValid) {
                    isTermsError = !termsAccepted
                    return@SignUpButton
                }
                // Call ViewModel to handle signup
                viewModel.signUpWithEmailAndPassword(formState, termsAccepted, termsAccepted)
            },
            type = SignUpButtonType.PRIMARY,
            loading = uiState.isLoading,
            enabled = isFormValid, // button only clickable when form valid
            modifier = Modifier.padding(start = 16.dp, end = 15.dp, top = 16.dp)
        )

        // Sign In Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            TextButton(
                onClick = onSignInClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Sign in",
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

        // Social Login Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            SocialIconButton(
                provider = SocialProvider.GOOGLE,
                onClick = { viewModel.signInWithGoogle(activity) },
                modifier = Modifier.size(45.dp),
                logoSize = 20
            )
            SocialIconButton(
                provider = SocialProvider.FACEBOOK,
                onClick = { viewModel.signInWithFacebook(activity) },
                modifier = Modifier.size(45.dp),
                logoSize = 23
            )
        }

            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Toast overlay at the top of the screen
        BrightCareToast(
            toastState = toastState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp) // Add padding to avoid status bar
        )
    }
}

@Preview(
    showBackground = true,
    name = "POCO C75 - Portrait",
    widthDp = 360,
    heightDp = 740,
    showSystemUi = true
)
@Composable
fun PatientSignUpScreenPreview() {
    BrightCarePatientTheme {
        PatientSignUpScreen(
            navController = rememberNavController()
        )
    }
}
