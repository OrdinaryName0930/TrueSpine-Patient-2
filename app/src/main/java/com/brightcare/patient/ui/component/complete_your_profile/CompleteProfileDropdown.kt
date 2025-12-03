package com.brightcare.patient.ui.component.complete_your_profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import com.brightcare.patient.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { 
                if (enabled && !readOnly) {
                    expanded = !expanded 
                }
            }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { },
                readOnly = true,
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .menuAnchor()
                    .clickable(enabled = enabled && !readOnly) {
                        expanded = !expanded
                    },

                // Label floats within the border (Material3 behavior) - matching SignUp style
                label = {
                    if (placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            maxLines = 1
                        )
                    }
                },

                placeholder = {
                    if (placeholder.isNotEmpty() && value.isEmpty() && !isFocused) {
                        Text(text = placeholder, maxLines = 1)
                    }
                },

                leadingIcon = leadingIcon?.let { icon ->
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isError) MaterialTheme.colorScheme.error
                            else Gray500
                        )
                    }
                },

                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error
                        else if (expanded) Blue500
                        else Gray500
                    )
                },

                enabled = enabled,
                isError = isError,
                shape = RoundedCornerShape(12.dp),
                singleLine = true,

                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isError) MaterialTheme.colorScheme.error
                    else Blue500,
                    unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error
                    else Gray300,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Gray50,
                    disabledBorderColor = Gray200,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorContainerColor = Color.Transparent,
                    focusedLabelColor = if (isError) MaterialTheme.colorScheme.error
                    else Blue500,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Blue50)
            ) {
                options.forEach { option ->
                    val isSelected = option == value
                    DropdownMenuItem(
                        text = { Text(option, color = Black) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                        modifier = Modifier
                            .background(if (isSelected) Gray100 else Blue50)
                    )
                }
            }
        }
        
        // Error message below field
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

@Preview(showBackground = true)
@Composable
fun CompleteProfileDropdownPreview() {
    BrightCarePatientTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CompleteProfileDropdown(
                value = "",
                onValueChange = {},
                options = listOf("", "Jr.", "Sr.", "III", "IV", "V"),
                placeholder = "Suffix",
                isError = false
            )

            CompleteProfileDropdown(
                value = "Male",
                onValueChange = {},
                options = listOf("Male", "Female"),
                placeholder = "Sex",
                isError = false
            )

            CompleteProfileDropdown(
                value = "",
                onValueChange = {},
                options = listOf("Option 1", "Option 2", "Option 3"),
                placeholder = "With Error",
                isError = true,
                errorMessage = "This field is required"
            )
        }
    }
}
