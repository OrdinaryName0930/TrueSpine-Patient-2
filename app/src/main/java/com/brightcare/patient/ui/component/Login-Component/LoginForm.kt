package com.brightcare.patient.ui.component.login_component

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
import com.brightcare.patient.ui.component.signup_component.ValidationUtils

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val isLoginError: Boolean = false,
    val loginErrorMessage: String = "",
    val loginAttemptError: String? = null, // New field for login attempt errors
    val emailRequiredError: Boolean = false, // Error for empty email field
    val emailFormatError: Boolean = false, // Error for invalid email format
    val loginCredentialError: String? = null, // Error for invalid credentials (UserNotFound, InvalidCredential)
    val passwordRequiredError: Boolean = false // Error for empty password field
)

@Composable
fun LoginForm(
    formState: LoginFormState,
    onFormStateChange: (LoginFormState) -> Unit,
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

        // Email Field with automatic lowercase
        LoginTextField(
            value = formState.email,
            onValueChange = { input ->

                // 1. Remove all spaces
                val noSpaces = input.replace(" ", "")

                // 2. Trim any accidental whitespace
                val trimmed = noSpaces.trim()

                // 3. Convert to lowercase
                val cleanedInput = trimmed.lowercase()

                val updatedState = formState.copy(
                    email = cleanedInput,
                    loginAttemptError = null,       // Clear login attempt error when typing
                    emailRequiredError = false,     // Clear required error when typing
                    emailFormatError = false,       // Clear format error when typing
                    loginCredentialError = null     // Clear credential error when typing
                )

                onFormStateChange(updatedState)
            },
            placeholder = "Email Address",
            isError = formState.emailRequiredError ||
                    formState.emailFormatError ||
                    formState.loginCredentialError != null ||
                    formState.loginAttemptError != null,
            errorMessage = when {
                formState.emailRequiredError -> "This field is required"
                formState.emailFormatError -> "Please enter a valid email address"
                formState.loginCredentialError != null -> formState.loginCredentialError
                formState.loginAttemptError != null -> formState.loginAttemptError
                else -> ""
            },
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )


        // Password Field
        LoginTextField(
            value = formState.password,
            onValueChange = { input ->
                val updatedState = formState.copy(
                    password = input,
                    loginAttemptError = null, // Clear login attempt error when user types
                    passwordRequiredError = false, // Clear required error when user types
                    loginCredentialError = null // Clear credential error when user types
                )
                onFormStateChange(updatedState)
            },
            placeholder = "Password",
            isPassword = true,
            isError = formState.passwordRequiredError || formState.loginCredentialError != null || formState.loginAttemptError != null, // Show error border for required error, credential error, or login attempt error
            errorMessage = if (formState.passwordRequiredError) "This field is required" else "", // Show required error message on password field
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onImeAction = { focusManager.clearFocus() }
        )
    }
}

// Function to set login error (called when login fails)
fun setLoginError(formState: LoginFormState, errorMessage: String): LoginFormState {
    return formState.copy(
        loginAttemptError = errorMessage
    )
}

// Basic validation (just check if fields are not empty)
fun isLoginFormValid(formState: LoginFormState): Boolean {
    return formState.email.isNotBlank() && formState.password.isNotBlank()
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginFormPreview() {
    var formState by remember { mutableStateOf(LoginFormState()) }

    MaterialTheme {
        Surface {
            LoginForm(
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
fun LoginFormErrorTestPreview() {
    var formState by remember { 
        mutableStateOf(LoginFormState(
            email = "",
            password = "",
            emailRequiredError = true,
            emailFormatError = false,
            loginCredentialError = null,
            passwordRequiredError = true
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
                    text = "Login Error Test - Should show error message",
                    style = MaterialTheme.typography.titleMedium
                )
                
                LoginForm(
                    formState = formState,
                    onFormStateChange = { formState = it }
                )
                
                Text(
                    text = "Form State Debug:",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Email Required: ${formState.emailRequiredError}\nPassword Required: ${formState.passwordRequiredError}\nLogin Attempt Error: ${formState.loginAttemptError}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
