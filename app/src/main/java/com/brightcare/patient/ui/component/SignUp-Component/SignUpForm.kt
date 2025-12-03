package com.brightcare.patient.ui.component.signup_component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class SignUpFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isNameError: Boolean = false,
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false,
    val isConfirmPasswordError: Boolean = false,
    val nameErrorMessage: String = "",
    val emailErrorMessage: String = "",
    val passwordErrorMessage: String = "",
    val confirmPasswordErrorMessage: String = "",
    val hasInteractedName: Boolean = false,
    val hasInteractedEmail: Boolean = false,
    val hasInteractedPassword: Boolean = false,
    val hasInteractedConfirm: Boolean = false
)

@Composable
fun SignUpForm(
    formState: SignUpFormState,
    onFormStateChange: (SignUpFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .padding(start = 16.dp)
            .padding(end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

// Email Field with automatic lowercase + no spaces allowed
        SignUpTextField(
            value = formState.email,
            onValueChange = { input ->

                // 1. Remove all spaces
                val noSpaces = input.replace(" ", "")

                // 2. Trim remaining whitespace (just in case)
                val trimmed = noSpaces.trim()

                // 3. Convert to lowercase
                val cleanedInput = trimmed.lowercase()

                val isValid = ValidationUtils.isValidEmail(cleanedInput)

                val updatedState = formState.copy(
                    email = cleanedInput,
                    hasInteractedEmail = true,
                    isEmailError = formState.hasInteractedEmail &&
                            cleanedInput.isNotEmpty() &&
                            !isValid,
                    emailErrorMessage =
                        if (formState.hasInteractedEmail && !isValid && cleanedInput.isNotEmpty())
                            "Please enter a valid email address"
                        else ""
                )

                onFormStateChange(updatedState)
            },
            placeholder = "Email Address",
            isError = formState.isEmailError,
            errorMessage = formState.emailErrorMessage,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )


        // Password Field
        SignUpTextField(
            value = formState.password,
            onValueChange = {
                val strong = ValidationUtils.isStrongPassword(it)
                val hasNoWhitespace = ValidationUtils.hasNoWhitespace(it)
                val updatedState = formState.copy(
                    password = it,
                    hasInteractedPassword = true,
                    isPasswordError = formState.hasInteractedPassword && it.isNotEmpty() && (!strong || !hasNoWhitespace),
                    passwordErrorMessage = when {
                        formState.hasInteractedPassword && it.isNotEmpty() && !hasNoWhitespace ->
                            "Password cannot contain spaces"
                        formState.hasInteractedPassword && it.isNotEmpty() && !strong ->
                            "Password must be at least 8 characters with uppercase, lowercase, number, and special character."
                        else -> ""
                    }
                )
                onFormStateChange(updatedState)
            },
            placeholder = "Password",
            isPassword = true,
            isError = formState.isPasswordError,
            errorMessage = formState.passwordErrorMessage,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        // Confirm Password Field
        SignUpTextField(
            value = formState.confirmPassword,
            onValueChange = {
                val updatedState = formState.copy(
                    confirmPassword = it,
                    hasInteractedConfirm = true,
                    isConfirmPasswordError = formState.hasInteractedConfirm && it.isNotEmpty() && it != formState.password,
                    confirmPasswordErrorMessage = if (formState.hasInteractedConfirm && it != formState.password && it.isNotEmpty())
                        "Passwords do not match"
                    else ""
                )
                onFormStateChange(updatedState)
            },
            placeholder = "Confirm Password",
            isPassword = true,
            isError = formState.isConfirmPasswordError,
            errorMessage = formState.confirmPasswordErrorMessage,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onImeAction = { focusManager.clearFocus() }
        )
    }
}

// Validation functions (used when clicking Sign Up)
fun validateSignUpForm(formState: SignUpFormState): SignUpFormState {
    var updatedState = formState

    val emailLower = formState.email.lowercase() // ensure lowercase for validation

    // Validate email
    if (emailLower.isBlank()) {
        updatedState = updatedState.copy(
            isEmailError = true,
            emailErrorMessage = "This field is required"
        )
    } else if (!ValidationUtils.isValidEmail(emailLower)) {
        updatedState = updatedState.copy(
            isEmailError = true,
            emailErrorMessage = "Please enter a valid email address"
        )
    }

    // Validate password
    if (formState.password.isBlank()) {
        updatedState = updatedState.copy(
            isPasswordError = true,
            passwordErrorMessage = "This field is required"
        )
    } else if (!ValidationUtils.isStrongPassword(formState.password)) {
        updatedState = updatedState.copy(
            isPasswordError = true,
            passwordErrorMessage = "Password must be at least 8 characters with uppercase, lowercase, number, and special character."
        )
    }
    else if (!ValidationUtils.hasNoWhitespace(formState.password)) {
        updatedState = updatedState.copy(
            isPasswordError = true,
            passwordErrorMessage = "Password cannot contain spaces"
        )
    }

    // Validate confirm password
    if (formState.confirmPassword.isBlank()) {
        updatedState = updatedState.copy(
            isConfirmPasswordError = true,
            confirmPasswordErrorMessage = "This field is required"
        )
    } else if (formState.password != formState.confirmPassword) {
        updatedState = updatedState.copy(
            isConfirmPasswordError = true,
            confirmPasswordErrorMessage = "Passwords do not match"
        )
    }

    return updatedState
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpFormPreview() {
    var formState by remember { mutableStateOf(SignUpFormState()) }

    MaterialTheme {
        Surface {
            SignUpForm(
                formState = formState,
                onFormStateChange = { formState = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpFormValidationTestPreview() {
    var formState by remember { 
        mutableStateOf(SignUpFormState(
            email = "invalid-email",
            password = "weak",
            confirmPassword = "different",
            hasInteractedEmail = true,
            hasInteractedPassword = true,
            hasInteractedConfirm = true,
            isEmailError = true,
            isPasswordError = true,
            isConfirmPasswordError = true,
            emailErrorMessage = "Please enter a valid email address",
            passwordErrorMessage = "Password must be at least 8 characters with uppercase, lowercase, number, and special character.",
            confirmPasswordErrorMessage = "Passwords do not match"
        ))
    }

    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Validation Test - Should show error messages",
                    style = MaterialTheme.typography.titleMedium
                )
                
                SignUpForm(
                    formState = formState,
                    onFormStateChange = { formState = it }
                )
                
                Text(
                    text = "Form State Debug:",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Email Error: ${formState.isEmailError}\nPassword Error: ${formState.isPasswordError}\nConfirm Error: ${formState.isConfirmPasswordError}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
