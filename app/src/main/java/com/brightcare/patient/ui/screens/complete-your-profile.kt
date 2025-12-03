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
import com.brightcare.patient.ui.viewmodel.CompleteProfileViewModel
import com.brightcare.patient.ui.viewmodel.PatientSignInViewModel
import com.brightcare.patient.navigation.navigateToHome
import com.brightcare.patient.ui.BrightCareToast
import com.brightcare.patient.ui.rememberToastState
import com.brightcare.patient.ui.showInfo
import com.brightcare.patient.ui.showError
import androidx.compose.ui.text.font.FontStyle

data class CompleteProfileFormState(
    val firstName: String = "",
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
    
    // Error states
    val isFirstNameError: Boolean = false,
    val isLastNameError: Boolean = false,
    val isBirthDateError: Boolean = false,
    val isSexError: Boolean = false,
    val isPhoneNumberError: Boolean = false,
    val isProvinceError: Boolean = false,
    val isMunicipalityError: Boolean = false,
    val isAdditionalAddressError: Boolean = false,
    
    // Error messages
    val firstNameErrorMessage: String = "",
    val lastNameErrorMessage: String = "",
    val birthDateErrorMessage: String = "",
    val sexErrorMessage: String = "",
    val phoneNumberErrorMessage: String = "",
    val provinceErrorMessage: String = "",
    val municipalityErrorMessage: String = "",
    val additionalAddressErrorMessage: String = ""
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
    
    val addressData = rememberAddressDataOnce()
    if (addressData == null || uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    
    // Real-time validation: check if all required fields are valid
    val isFormValid by remember(uiState.formState) {
        derivedStateOf {
            uiState.formState.firstName.isNotBlank() &&
            uiState.formState.lastName.isNotBlank() &&
            uiState.formState.birthDate.isNotBlank() &&
            uiState.formState.sex.isNotBlank() &&
            uiState.formState.phoneNumber.isNotBlank() &&
            uiState.formState.province.isNotBlank() &&
            uiState.formState.municipality.isNotBlank() &&
            !uiState.formState.isFirstNameError &&
            !uiState.formState.isLastNameError &&
            !uiState.formState.isBirthDateError &&
            !uiState.formState.isSexError &&
            !uiState.formState.isPhoneNumberError &&
            !uiState.formState.isProvinceError &&
            !uiState.formState.isMunicipalityError &&
            !uiState.formState.isAdditionalAddressError
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
    
    // Show error toast
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            toastState.showError(errorMessage)
            viewModel.clearError()
        }
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
                    // Clear session and go back to login
                    signInViewModel.signOut()
                    navController.navigate("login?clearStates=true") {
                        popUpTo(0) { inclusive = true }
                    }
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
        
        // Save/Continue button (matching SignUp button style)
        CompleteProfileButton(
            text = "Save & Continue",
            onClick = {
                if (isFormValid) {
                    viewModel.saveProfile()
                }
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