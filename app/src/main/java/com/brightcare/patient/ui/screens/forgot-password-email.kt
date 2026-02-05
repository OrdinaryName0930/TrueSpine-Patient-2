package com.brightcare.patient.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.data.model.ForgotPasswordStep
import com.brightcare.patient.data.model.ForgotPasswordResult
import com.brightcare.patient.data.model.ForgotPasswordException
import com.brightcare.patient.ui.component.forgotpassword_component.*
import com.brightcare.patient.ui.component.signup_component.ValidationUtils
import com.brightcare.patient.ui.BrightCareToast
import com.brightcare.patient.ui.rememberToastState
import com.brightcare.patient.ui.showInfo
import com.brightcare.patient.ui.showError
import com.brightcare.patient.ui.component.termsandconditions_and_privacypolicy.TermsBackButton
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.PatientForgotPasswordViewModel
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordEmailScreen(
    navController: NavController,
    onBackClick: () -> Unit = { navController.popBackStack() },
    onSendResetLinkClick: () -> Unit = { navController.navigate("login") },
    viewModel: PatientForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val toastState = rememberToastState()
    
    // Handle toast messages for reset link sent/not sent and navigation
    LaunchedEffect(uiState.sendResetLinkResult) {
        println("DEBUG: LaunchedEffect triggered with sendResetLinkResult: ${uiState.sendResetLinkResult}")
        when (val result = uiState.sendResetLinkResult) {
            is ForgotPasswordResult.Success -> {
                println("DEBUG: Showing success toast for reset link sent")
                toastState.showInfo("Password reset link sent to your email successfully! Please check your inbox or spam.")
                println("DEBUG: Toast state after showInfo - isVisible: ${toastState.isVisible.value}, toastData: ${toastState.toastData.value}")
                
                // Wait for toast to show, then navigate to login
                delay(2500) // 2.5 seconds delay to let user see the success message
                println("DEBUG: Navigating to login after success toast")
                onSendResetLinkClick()
            }
            is ForgotPasswordResult.Error -> {
                when (result.exception) {
                    is ForgotPasswordException.EmailNotFound -> {
                        println("DEBUG: EmailNotFound error - showing in Card, not toast")
                        // Don't show toast for email not found, show in Card instead
                    }
                    else -> {
                        println("DEBUG: Showing error toast for: ${result.exception.message}")
                        toastState.showError("Failed to send reset link. Please try again.")
                        println("DEBUG: Toast state after showError - isVisible: ${toastState.isVisible.value}, toastData: ${toastState.toastData.value}")
                    }
                }
            }
            else -> { 
                println("DEBUG: sendResetLinkResult is null or loading: $result")
            }
        }
    }
    
    // Frontend validation states
    var email by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }
    var emailErrorMessage by remember { mutableStateOf("") }
    var hasInteractedEmail by remember { mutableStateOf(false) }
    
    // Throttle countdown state
    var showThrottleToast by remember { mutableStateOf(false) }
    var throttleMessage by remember { mutableStateOf("") }
    
    // Real-time email validation (like in SignUpForm)
    fun validateEmailRealTime(input: String) {
        val lowercased = input.lowercase()
        val isValid = ValidationUtils.isValidEmail(lowercased)
        email = lowercased
        hasInteractedEmail = true
        isEmailError = hasInteractedEmail && lowercased.isNotEmpty() && !isValid
        emailErrorMessage = if (hasInteractedEmail && !isValid && lowercased.isNotEmpty())
            "Please enter a valid email address"
        else ""
        
        // Also update viewModel to clear backend errors when user types
        viewModel.updateEmail(lowercased)
    }
    
    // Final validation for form submission
    fun validateEmailForSubmission(): Boolean {
        return when {
            email.isBlank() -> {
                isEmailError = true
                emailErrorMessage = "This field is required"
                false
            }
            !ValidationUtils.isValidEmail(email) -> {
                isEmailError = true
                emailErrorMessage = "Please enter a valid email address"
                false
            }
            else -> {
                isEmailError = false
                emailErrorMessage = ""
                true
            }
        }
    }
    
    fun handleSendResetLink() {
        if (validateEmailForSubmission()) {
            val throttleResult = viewModel.sendResetLink(email)
            if (!throttleResult.canSend) {
                // Show countdown toast
                showThrottleToast = true
                throttleMessage = viewModel.getPasswordResetThrottleToastMessage(email)
                Log.d("ForgotPasswordEmail", "Reset link throttled: ${throttleResult.remainingTimeFormatted}")
            }
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteBg)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
        // Back Button (like in terms screen)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 3.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TermsBackButton(
                onClick = onBackClick
            )
        }
        
        // Title (like in signup screen)
        Text(
            text = "Forgot \nPassword",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp
            ),
            color = Blue500,
            modifier = Modifier.padding(bottom = 8.dp, start = 15.dp)
        )
        
        // Description
        Text(
            text = "Enter your registered email address to receive a password reset link.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Gray600,
                lineHeight = 24.sp
            ),
            modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 16.dp)
        )
        
        // Show backend error messages (like account not found) in Card
        uiState.sendResetLinkResult?.let { result ->
            println("DEBUG: sendResetLinkResult = $result")
            if (result is ForgotPasswordResult.Error && result.exception is ForgotPasswordException.EmailNotFound) {
                println("DEBUG: Showing EmailNotFound error card")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "No account found with this email address. Please check your email or create a new account.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (result is ForgotPasswordResult.Error) {
                println("DEBUG: Other error type: ${result.exception}")
            }
        } ?: run {
            println("DEBUG: sendResetLinkResult is null")
        }
        
        // Email Input Field (without icon, like in SignUpForm)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            ForgotPasswordTextField(
                value = email,
                onValueChange = { input ->
                    validateEmailRealTime(input)
                },
                placeholder = "Email Address",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                onImeAction = { handleSendResetLink() },
                isError = isEmailError,
                errorMessage = emailErrorMessage,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Send Reset Link Button
        ForgotPasswordButton(
            text = "Send Reset Link",
            onClick = { handleSendResetLink() },
            loading = uiState.isSendingResetLink,
            enabled = !uiState.isSendingResetLink && email.isNotEmpty() && !isEmailError,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Toast component - positioned at bottom center with proper padding
        BrightCareToast(
            toastState = toastState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
        
        // Real-time countdown toast for throttling - positioned at bottom
        if (showThrottleToast) {
            ThrottleCountdownToast(
                email = email,
                viewModel = viewModel,
                onDismiss = { showThrottleToast = false },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 15.dp) // Above the regular toast
            )
        }
    }
}

/**
 * Throttle Countdown Toast - Shows real-time countdown for password reset throttling
 */
@Composable
private fun ThrottleCountdownToast(
    email: String,
    viewModel: PatientForgotPasswordViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var throttleResult by remember { mutableStateOf(viewModel.getPasswordResetThrottleResult(email)) }
    
    // Update countdown every second
    LaunchedEffect(email) {
        while (!throttleResult.canSend) {
            delay(1000) // Update every second
            val newResult = viewModel.getPasswordResetThrottleResult(email)
            throttleResult = newResult
            if (newResult.canSend) {
                onDismiss() // Auto-dismiss when throttle expires
                break
            }
        }
    }
    
    // Show the toast with countdown
    if (!throttleResult.canSend) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Please wait to send new password reset link",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Time remaining: ${throttleResult.remainingTimeFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "POCO C75 - Portrait",
    widthDp = 360,
    heightDp = 740,
    showSystemUi = true)
@Composable
fun ForgotPasswordEmailScreenPreview() {
    BrightCarePatientTheme {
        ForgotPasswordEmailScreen(
            navController = rememberNavController()
        )
    }
}
