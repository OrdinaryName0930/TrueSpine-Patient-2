package com.brightcare.patient.ui.component.forgotpassword_component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brightcare.patient.ui.theme.*

@Composable
fun OTPInputField(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    otpLength: Int = 6,
    isError: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        repeat(otpLength) { index ->
            val char = otpValue.getOrNull(index)?.toString() ?: ""
            
            OTPDigitBox(
                value = char,
                onValueChange = { newValue ->
                    if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                        val newOtp = otpValue.toMutableList()
                        
                        // Ensure the list is long enough
                        while (newOtp.size <= index) {
                            newOtp.add(' ')
                        }
                        
                        if (newValue.isEmpty()) {
                            // Handle backspace
                            if (index < newOtp.size) {
                                newOtp[index] = ' '
                            }
                            // Move focus to previous field if current is empty
                            if (index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        } else {
                            // Handle digit input
                            newOtp[index] = newValue[0]
                            // Move focus to next field
                            if (index < otpLength - 1) {
                                focusRequesters[index + 1].requestFocus()
                            } else {
                                focusManager.clearFocus()
                            }
                        }
                        
                        // Convert back to string, removing trailing spaces
                        val result = newOtp.joinToString("").trimEnd()
                        onOtpChange(result)
                    }
                },
                focusRequester = focusRequesters[index],
                isError = isError
            )
        }
    }
}

@Composable
private fun OTPDigitBox(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = when {
        isError -> Error
        isFocused -> Blue500
        value.isNotEmpty() -> Blue500
        else -> Gray300
    }

    val backgroundColor = when {
        isError -> Error.copy(alpha = 0.05f)
        isFocused -> Blue50
        else -> Color.Transparent
    }

    val borderWidth = if (isError || isFocused) 2.dp else 1.dp
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            ),
            onValueChange = { textFieldValue ->
                onValueChange(textFieldValue.text)
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            textStyle = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = if (isError) Error else Gray900
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (value.isEmpty() && !isFocused) {
                        Text(
                            text = "0",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = Gray300
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

















