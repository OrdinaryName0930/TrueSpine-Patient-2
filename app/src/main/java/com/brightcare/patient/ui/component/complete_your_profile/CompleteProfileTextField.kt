package com.brightcare.patient.ui.component.complete_your_profile

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.brightcare.patient.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    enabled: Boolean = true,
    singleLine: Boolean = true,
    readOnly: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),

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
            readOnly = readOnly,
            isError = isError,
            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.error
                else Blue500,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error
                else Gray300,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledBorderColor = Gray300,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorContainerColor = Color.Transparent,
                focusedLabelColor = if (isError) MaterialTheme.colorScheme.error
                else Blue500,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

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

/**
 * Dedicated BirthDateTextField composable with DatePickerDialog and manual input.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDateTextField(
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String = "",
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val calendar = remember { Calendar.getInstance() }

    // Parse existing date to set initial date picker values
    val initialDate = remember(birthDate) {
        if (birthDate.isNotBlank()) {
            try {
                val sdf = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
                sdf.parse(birthDate)?.let { date ->
                    val cal = Calendar.getInstance()
                    cal.time = date
                    Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val datePickerDialog = remember(initialDate) {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                // Double-check the selected date is valid before accepting it
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                
                val today = Calendar.getInstance()
                val minDate = Calendar.getInstance().apply { add(Calendar.YEAR, -122) }
                val maxDate = Calendar.getInstance().apply { add(Calendar.YEAR, -3) } // Minimum 3 years old
                
                // Only accept the date if it's within valid range (at least 3 years old)
                if (selectedCalendar.after(minDate) && selectedCalendar.before(maxDate)) {
                    val formatted = String.format("%02d/%02d/%04d", month + 1, dayOfMonth, year)
                    onBirthDateChange(formatted)
                }
            },
            // Show current date if no birthdate is set, otherwise show the existing date
            initialDate?.first ?: calendar.get(Calendar.YEAR),
            initialDate?.second ?: calendar.get(Calendar.MONTH),
            initialDate?.third ?: calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Set max date to 3 years ago (minimum 3 years old)
            val maxCalendar = Calendar.getInstance()
            maxCalendar.add(Calendar.YEAR, -3)
            datePicker.maxDate = maxCalendar.timeInMillis
            
            // Set min date to 122 years ago (prevent ages over 122)
            val minCalendar = Calendar.getInstance()
            minCalendar.add(Calendar.YEAR, -122)
            datePicker.minDate = minCalendar.timeInMillis
        }
    }

    // Show date picker when field gets focus (like HTML date input)
    LaunchedEffect(isFocused) {
        if (isFocused) {
            datePickerDialog.show()
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = birthDate,
            onValueChange = { }, // Read-only like HTML date input
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { datePickerDialog.show() },
            label = { Text("*Birthdate") },
            placeholder = { Text("Select date") },
            singleLine = true,
            readOnly = true, // Make it read-only like HTML date input
            interactionSource = interactionSource,
            isError = isError,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date",
                    tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { datePickerDialog.show() }
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else Blue500,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else Gray300,
                focusedLabelColor = if (isError) MaterialTheme.colorScheme.error else Blue500,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = if (isError) MaterialTheme.colorScheme.error else Gray300,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

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
