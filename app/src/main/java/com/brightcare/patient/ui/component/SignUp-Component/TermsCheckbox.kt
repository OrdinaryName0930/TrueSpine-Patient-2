package com.brightcare.patient.ui.component.signup_component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brightcare.patient.ui.theme.*

@Composable
fun TermsCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit = {},
    onTermsAndPrivacyClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(top = 9.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = Blue500,
                    uncheckedColor = if (isError) MaterialTheme.colorScheme.error else Gray400,
                    checkmarkColor = White
                ),
            )

            // Create interaction source to track press state
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            
            ClickableText(
                text = buildAnnotatedString {
                    // Regular text part
                    withStyle(
                        style = SpanStyle(
                            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        append("Agree to the ")
                    }
                    
                    // Clickable Terms & Privacy Policy text with hover effect
                    withStyle(
                        style = SpanStyle(
                            color = if (isPressed) Blue500.copy(alpha = 0.7f) else Blue500,
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Terms & Conditions and Privacy Policy")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                onClick = { offset ->
                    // Check if click is on the "Terms & Conditions and Privacy Policy" part
                    val termsStart = "Agree to the ".length
                    val termsEnd = termsStart + "Terms & Conditions and Privacy Policy".length
                    
                    if (offset in termsStart until termsEnd) {
                        // Only navigate when onTermsAndPrivacyClick is provided
                        if (onTermsAndPrivacyClick != null) {
                            onTermsAndPrivacyClick()
                        } else {
                            // Fallback to legacy behavior
                            onTermsClick()
                        }
                    }
                }
            )
        }

        if (isError) {
            Text(
                text = "*You must agree to continue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 0.dp, start = 22.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TermsCheckboxPreview() {
    BrightCarePatientTheme {
        var checked by remember { mutableStateOf(false) }

        Surface(color = WhiteBg, modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Default Behavior (toggles checkbox when text clicked)",
                    style = MaterialTheme.typography.titleSmall
                )
                TermsCheckbox(
                    isChecked = checked,
                    onCheckedChange = { checked = it },
                    onTermsClick = {},
                    onPrivacyClick = {},
                    isError = false
                )

                Text(
                    text = "Navigation Behavior (only blue text is clickable)",
                    style = MaterialTheme.typography.titleSmall
                )
                var checked2 by remember { mutableStateOf(false) }
                var navigationClicked by remember { mutableStateOf(false) }
                TermsCheckbox(
                    isChecked = checked2,
                    onCheckedChange = { checked2 = it },
                    onTermsClick = {},
                    onTermsAndPrivacyClick = {
                        navigationClicked = true
                    }
                )
                if (navigationClicked) {
                    Text(
                        text = "✅ Terms link clicked! Would navigate to Terms screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Success
                    )
                }

                Text(
                    text = "Error State",
                    style = MaterialTheme.typography.titleSmall
                )
                var checked3 by remember { mutableStateOf(false) }
                TermsCheckbox(
                    isChecked = checked3,
                    onCheckedChange = { checked3 = it },
                    onTermsClick = {},
                    onPrivacyClick = {},
                    isError = true
                )
            }
        }
    }
}