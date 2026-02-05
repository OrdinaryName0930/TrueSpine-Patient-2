package com.brightcare.patient.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.brightcare.patient.ui.BrightCareToast
import com.brightcare.patient.ui.rememberToastState
import com.brightcare.patient.ui.showInfo
import com.brightcare.patient.ui.showError
import com.brightcare.patient.ui.component.signup_component.ValidationUtils
import com.brightcare.patient.navigation.NavigationRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    onBackClick: () -> Unit = {
        navController.navigate("${NavigationRoutes.MAIN_DASHBOARD}?initialRoute=profile") {
            popUpTo(NavigationRoutes.MAIN_DASHBOARD) { inclusive = false }
        }
    },
    authViewModel: AuthenticationViewModel = hiltViewModel()
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val toastState = rememberToastState()

    BackHandler {
        navController.navigate("${NavigationRoutes.MAIN_DASHBOARD}?initialRoute=profile") {
            popUpTo(NavigationRoutes.MAIN_DASHBOARD) { inclusive = false }
        }
    }

    // UI interaction states
    var hasInteractedCurrent by remember { mutableStateOf(false) }
    var hasInteractedNew by remember { mutableStateOf(false) }
    var hasInteractedConfirm by remember { mutableStateOf(false) }

    // 🔵 Remove current password validation entirely
    val isCurrentPasswordValid = true   // always valid

    // Normal validation for new + confirm password
    val isNewPasswordValid = ValidationUtils.isStrongPassword(newPassword)
            && ValidationUtils.hasNoWhitespace(newPassword)

    val isConfirmPasswordValid = confirmPassword == newPassword && confirmPassword.isNotEmpty()

    val isFormValid = isCurrentPasswordValid && isNewPasswordValid && isConfirmPasswordValid

    val passwordStrength = ValidationUtils.getPasswordStrength(newPassword)
    val passwordStrengthText = ValidationUtils.getPasswordStrengthText(passwordStrength)

    val changePasswordResult by authViewModel.changePasswordResult.collectAsState()

    LaunchedEffect(changePasswordResult) {
        changePasswordResult?.let { result ->
            isLoading = false
            if (result.isSuccess) {
                toastState.showInfo("Password changed successfully!")
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                kotlinx.coroutines.delay(1500)
                navController.popBackStack()
            } else {
                toastState.showError(result.exceptionOrNull()?.message ?: "Failed to change password")
            }
            authViewModel.clearChangePasswordResult()
        }
    }

    Box(
        modifier = Modifier
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Gray600
                    )
                }

                Text(
                    text = "Change Password",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Blue500,
                        fontSize = 28.sp
                    )
                )
            }

            Text(
                text = "Enter your current password and choose a new secure password.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Gray600),
                modifier = Modifier.padding(bottom = 16.dp, top = 16.dp, start = 16.dp, end = 16.dp)
            )

            // Form fields with signup-style padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(start = 16.dp)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                
                // -------------------------------
                // CURRENT PASSWORD (NO VALIDATION)
                // -------------------------------
                val currentPasswordInteractionSource = remember { MutableInteractionSource() }
                val isCurrentPasswordFocused by currentPasswordInteractionSource.collectIsFocusedAsState()
                
                OutlinedTextField(
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    hasInteractedCurrent = true
                },
                label = { Text("Current Password") },
                visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    // Match eye icon color to border color
                    val tint = if (isCurrentPasswordFocused) Blue500 else Gray300
                    IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                        Icon(
                            imageVector = if (currentPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = tint
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue500,
                    unfocusedBorderColor = Gray300
                ),
                isError = false,
                supportingText = null,
                interactionSource = currentPasswordInteractionSource
            )

            // -------------------------------
            // NEW PASSWORD
            // -------------------------------
            val newPasswordInteractionSource = remember { MutableInteractionSource() }
            val isNewPasswordFocused by newPasswordInteractionSource.collectIsFocusedAsState()
            val isNewPasswordError = hasInteractedNew && newPassword.isNotEmpty() && !isNewPasswordValid
            
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    hasInteractedNew = true
                },
                label = { Text("New Password") },
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    // Match eye icon color to border color
                    val tint = when {
                        isNewPasswordError -> Error
                        isNewPasswordFocused -> Blue500
                        else -> Gray300
                    }
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            imageVector = if (newPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = tint
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue500,
                    unfocusedBorderColor = Gray300
                ),
                isError = isNewPasswordError,
                supportingText =
                    if (isNewPasswordError) {
                        {
                            Text(
                                text = when {
                                    !ValidationUtils.hasNoWhitespace(newPassword) ->
                                        "Password cannot contain spaces"
                                    !ValidationUtils.isStrongPassword(newPassword) ->
                                        "Password must be at least 8 characters with uppercase, lowercase, number, and special character."
                                    else -> "Password must meet required complexity."
                                },
                                color = Error
                            )
                        }
                    } else null,
                interactionSource = newPasswordInteractionSource
            )

            // -------------------------------
            // CONFIRM PASSWORD
            // -------------------------------
            val confirmPasswordInteractionSource = remember { MutableInteractionSource() }
            val isConfirmPasswordFocused by confirmPasswordInteractionSource.collectIsFocusedAsState()
            val isConfirmPasswordError = hasInteractedConfirm && confirmPassword.isNotEmpty() && !isConfirmPasswordValid
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    hasInteractedConfirm = true
                },
                label = { Text("Confirm New Password") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    // Match eye icon color to border color
                    val tint = when {
                        isConfirmPasswordError -> Error
                        isConfirmPasswordFocused -> Blue500
                        else -> Gray300
                    }
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = tint
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue500,
                    unfocusedBorderColor = Gray300
                ),
                isError = isConfirmPasswordError,
                supportingText =
                    if (isConfirmPasswordError) {
                        { Text("Passwords do not match", color = Error) }
                    } else null,
                interactionSource = confirmPasswordInteractionSource
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        isLoading = true
                        authViewModel.changePassword(currentPassword, newPassword)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp),
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue500,
                    disabledContainerColor = Gray300
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Change Password",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = White
                    )
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }

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
    widthDp = 360,
    heightDp = 740,
    showSystemUi = true
)
@Composable
fun ChangePasswordScreenPreview() {
    BrightCarePatientTheme {
        ChangePasswordScreen(rememberNavController())
    }
}
