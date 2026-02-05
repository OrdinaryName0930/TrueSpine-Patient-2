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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brightcare.patient.ui.component.complete_your_profile.*
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.component.termsandconditions_and_privacypolicy.TermsBackButton
import com.brightcare.patient.ui.component.signup_component.TermsCheckbox
import com.brightcare.patient.ui.viewmodel.CompleteProfileViewModel
import com.brightcare.patient.ui.viewmodel.PatientSignInViewModel
import com.brightcare.patient.navigation.navigateToHome
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.ui.BrightCareToast
import com.brightcare.patient.ui.rememberToastState
import com.brightcare.patient.ui.showInfo
import com.brightcare.patient.ui.showError
import androidx.compose.ui.text.font.FontStyle
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

data class CompleteProfileFormState(
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val suffix: String = "",
    val birthDate: String = "",
    val sex: String = "",
    val phoneNumber: String = "",
    val country: String = "Philippines",
    val province: String = "",
    val municipality: String = "",
    val barangay: String = "",
    val additionalAddress: String = "",
    
    // Profile Picture
    val profilePictureUri: String = "", // Local URI for new profile picture
    val profilePictureUrl: String = "", // Firebase Storage URL after upload
    
    // ID Upload fields
    val idFrontImageUri: String = "",
    val idBackImageUri: String = "",
    val idFrontImageUrl: String = "", // Firebase Storage URL after upload
    val idBackImageUrl: String = "", // Firebase Storage URL after upload
    
    // Terms and Privacy Policy (moved from signup)
    val agreedToTerms: Boolean = false,
    val agreedToPrivacy: Boolean = false,
    
    // Error states
    val isFirstNameError: Boolean = false,
    val isMiddleNameError: Boolean = false,
    val isLastNameError: Boolean = false,
    val isBirthDateError: Boolean = false,
    val isSexError: Boolean = false,
    val isPhoneNumberError: Boolean = false,
    val isProvinceError: Boolean = false,
    val isMunicipalityError: Boolean = false,
    val isAdditionalAddressError: Boolean = false,
    val isTermsError: Boolean = false,
    val isIdFrontError: Boolean = false,
    val isIdBackError: Boolean = false,
    
    // Error messages
    val firstNameErrorMessage: String = "",
    val middleNameErrorMessage: String = "",
    val lastNameErrorMessage: String = "",
    val birthDateErrorMessage: String = "",
    val sexErrorMessage: String = "",
    val phoneNumberErrorMessage: String = "",
    val provinceErrorMessage: String = "",
    val municipalityErrorMessage: String = "",
    val additionalAddressErrorMessage: String = "",
    val idFrontErrorMessage: String = "",
    val idBackErrorMessage: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteYourProfileScreen(
    navController: NavController,
    onBackClick: () -> Unit = {},
    onSaveClick: (CompleteProfileFormState) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CompleteProfileViewModel = hiltViewModel(),
    signInViewModel: PatientSignInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val toastState = rememberToastState()
    
    // State for sign out confirmation dialog
    var showSignOutDialog by remember { mutableStateOf(false) }
    
    val addressData = rememberAddressDataOnce()
    if (addressData == null || uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    
    // Real-time validation: check if all required fields are valid including terms/privacy and ID uploads
    val isFormValid by remember(uiState.formState) {
        derivedStateOf {
            uiState.formState.firstName.isNotBlank() &&
            uiState.formState.lastName.isNotBlank() &&
            uiState.formState.birthDate.isNotBlank() &&
            uiState.formState.sex.isNotBlank() &&
            uiState.formState.phoneNumber.isNotBlank() &&
            uiState.formState.province.isNotBlank() &&
            uiState.formState.municipality.isNotBlank() &&
            uiState.formState.idFrontImageUri.isNotBlank() &&
            uiState.formState.idBackImageUri.isNotBlank() &&
            uiState.formState.agreedToTerms &&
            uiState.formState.agreedToPrivacy &&
            !uiState.formState.isFirstNameError &&
            !uiState.formState.isMiddleNameError &&
            !uiState.formState.isLastNameError &&
            !uiState.formState.isBirthDateError &&
            !uiState.formState.isSexError &&
            !uiState.formState.isPhoneNumberError &&
            !uiState.formState.isProvinceError &&
            !uiState.formState.isMunicipalityError &&
            !uiState.formState.isAdditionalAddressError &&
            !uiState.formState.isIdFrontError &&
            !uiState.formState.isIdBackError &&
            !uiState.formState.isTermsError
        }
    }
    
    // Handle success with toast and navigation
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            toastState.showInfo(
                message = "Profile completed successfully!",
                duration = 3000L
            )
            // Wait for toast to show, then navigate
            kotlinx.coroutines.delay(1500L)
            viewModel.resetSuccessState()
            navController.navigateToHome()
        }
    }
    
    // Show error toast and handle authentication errors
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            toastState.showError(errorMessage)
            
            // If it's an authentication error, log it for debugging
            if (errorMessage.contains("must be logged in", ignoreCase = true) ||
                errorMessage.contains("authentication", ignoreCase = true) ||
                errorMessage.contains("session expired", ignoreCase = true)) {
                
                // Log authentication error for debugging
                android.util.Log.w("CompleteProfileScreen", "Authentication error detected: $errorMessage")
                // User will need to manually retry or re-authenticate
            }
            
            viewModel.clearError()
        }
    }
    
    // Listen for terms agreement from Terms and Conditions screen
    LaunchedEffect(navController.currentBackStackEntry?.savedStateHandle) {
        val termsAgreed = navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("terms_agreed_complete_profile") ?: false
        
        if (termsAgreed) {
            viewModel.updateFormState { currentState ->
                currentState.copy(
                    agreedToTerms = true,
                    agreedToPrivacy = true,
                    isTermsError = false
                )
            }
            // Clear the saved state
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("terms_agreed_complete_profile")
        }
    }
    
    // Handle system back button
    BackHandler {
        showSignOutDialog = true
    }
    
    // Date picker is now handled internally by BirthDateTextField
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        // Back button (similar to SignUp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 3.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TermsBackButton(
                onClick = {
                    showSignOutDialog = true
                }
            )
        }
        
        // Title (matching SignUp style)
        Text(
            text = "Complete your Profile",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Blue500
            ),
            modifier = Modifier.padding(bottom = 8.dp, start = 15.dp)
        )
        
        // Subtitle (matching SignUp style)
        Text(
            text = "Please fill in your profile information to complete your profile setup.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Gray600,
                lineHeight = 24.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp, start = 15.dp, end = 15.dp)
        )

        Text(
            text = "* indicates a required field",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Gray600,
                fontStyle = FontStyle.Italic
            ),
            modifier = Modifier.padding(bottom = 16.dp, start = 15.dp, end = 15.dp)
        )
        
        // Form fields
        CompleteProfileForm(
            formState = uiState.formState,
            onFormStateChange = { updater -> viewModel.updateFormState(updater) },
            onPhoneNumberValidation = { phoneNumber -> viewModel.validatePhoneNumber(phoneNumber) },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Error message display
        uiState.errorMessage?.let { errorMessage ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
        
        // Terms and Privacy Policy Checkbox (moved from signup)
        TermsCheckbox(
            isChecked = uiState.formState.agreedToTerms && uiState.formState.agreedToPrivacy,
            onCheckedChange = { isChecked ->
                viewModel.updateFormState { currentState ->
                    currentState.copy(
                        agreedToTerms = isChecked,
                        agreedToPrivacy = isChecked,
                        isTermsError = false
                    )
                }
            },
            onTermsClick = {
                // Navigate to Terms and Conditions
                navController.navigate(NavigationRoutes.TERMS_AND_CONDITIONS)
            },
            onPrivacyClick = {
                // Navigate to Privacy Policy (same screen for now)
                navController.navigate(NavigationRoutes.TERMS_AND_CONDITIONS)
            },
            isError = uiState.formState.isTermsError,
            modifier = Modifier.padding(start = 10.dp, end = 12.dp, bottom = 15.dp, top = 8.dp)
        )
        
        // Save/Continue button (matching SignUp button style)
        CompleteProfileButton(
            text = if (uiState.isSaving) "Compressing & Uploading..." else "Save & Continue",
            onClick = {
                if (!isFormValid) {
                    // Show validation errors for missing required fields
                    viewModel.updateFormState { currentState ->
                        currentState.copy(
                            isTermsError = !currentState.agreedToTerms || !currentState.agreedToPrivacy,
                            isIdFrontError = currentState.idFrontImageUri.isBlank(),
                            isIdBackError = currentState.idBackImageUri.isBlank(),
                            idFrontErrorMessage = if (currentState.idFrontImageUri.isBlank()) 
                                "Front ID image is required" else "",
                            idBackErrorMessage = if (currentState.idBackImageUri.isBlank()) 
                                "Back ID image is required" else ""
                        )
                    }
                    return@CompleteProfileButton
                }
                viewModel.saveProfile()
            },
            enabled = isFormValid && !uiState.isSaving,
            loading = uiState.isSaving,
            modifier = Modifier.padding(start = 16.dp, end = 15.dp, top = 16.dp)
        )
        
        // Add bottom padding
        Spacer(modifier = Modifier.height(24.dp))
    }
    
    // Toast for success and error messages
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        BrightCareToast(
            toastState = toastState,
            modifier = Modifier.padding(16.dp)
        )
    }
    
    // Sign out confirmation dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = "Incomplete Profile",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Blue500
                    )
                )
            },
            text = {
                Text(
                    text = "Your profile is not yet complete. Are you sure you want to sign out?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray700
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        // Clear session and go back to login
                        signInViewModel.signOut()
                        navController.navigate("login?clearStates=true") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Sign Out",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Blue500
                    )
                ) {
                    Text(
                        text = "Continue Profile",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            tonalElevation = 8.dp
        )
    }
}

@Preview(
    showBackground = true,
    name = "POCO C75 - Portrait",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun CompleteYourProfileScreenPreview() {
    BrightCarePatientTheme {
        CompleteYourProfileScreen(
            navController = rememberNavController()
        )
    }
}