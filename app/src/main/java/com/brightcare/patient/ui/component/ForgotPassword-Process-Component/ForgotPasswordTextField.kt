package com.brightcare.patient.ui.component.forgotpassword_component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.unit.dp
import com.brightcare.patient.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    showPasswordToggle: Boolean = true,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val trailingIconColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> Blue500
            else -> Gray500
        }
    )
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth(),

            // Label floats within the border (Material3 behavior)
            label = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder
                    )
                }
            },

            // No leading icon (like in SignUpForm)

            // Optional Password Visibility Toggle
            trailingIcon = if (isPassword && showPasswordToggle) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Hide password" else "Show password",
                            tint = trailingIconColor
                        )
                    }
                }
            } else null,

            // Password or Normal Text Field
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation()
            else
                VisualTransformation.None,

            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),

            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() },
                onGo = { onImeAction() },
                onSearch = { onImeAction() },
                onSend = { onImeAction() }
            ),

            singleLine = singleLine,
            enabled = enabled,
            isError = isError,
            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError)
                    MaterialTheme.colorScheme.error
                else
                    Blue500,
                unfocusedBorderColor = if (isError)
                    MaterialTheme.colorScheme.error
                else
                    Gray300,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Gray50,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorContainerColor = Color.Transparent,
                focusedLabelColor = if (isError)
                    MaterialTheme.colorScheme.error
                else
                    Blue500,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Error Message Below Field
        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 3.dp, start = 10.dp)
            )
        }
    }
}
