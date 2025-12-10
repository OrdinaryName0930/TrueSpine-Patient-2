package com.brightcare.patient.ui.component.complete_your_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brightcare.patient.ui.component.signup_component.ValidationUtils
import com.brightcare.patient.ui.screens.CompleteProfileFormState
import com.brightcare.patient.ui.theme.*

fun String.toDisplayName(): String =
    lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

@Composable
fun CompleteProfileForm(
    formState: CompleteProfileFormState,
    onFormStateChange: ((CompleteProfileFormState) -> CompleteProfileFormState) -> Unit,
    onPhoneNumberValidation: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val addressData = rememberAddressData()

    // Province list
    val provinces = addressData.provinces.map { it.toDisplayName() }
    val selectedProvinceKey = addressData.provinces.find { it.toDisplayName() == formState.province } ?: ""

    // Municipality list for selected province
    val municipalities = if (selectedProvinceKey.isNotEmpty()) {
        addressData.municipalities[selectedProvinceKey]?.map { it.toDisplayName() } ?: emptyList()
    } else emptyList()
    val selectedMunicipalityKey = addressData.municipalities[selectedProvinceKey]?.find { it.toDisplayName() == formState.municipality } ?: ""

    // Barangay list for selected province+municipality
    val barangays = if (selectedProvinceKey.isNotEmpty() && selectedMunicipalityKey.isNotEmpty()) {
        addressData.barangays["$selectedProvinceKey-$selectedMunicipalityKey"]?.map { it.toDisplayName() } ?: emptyList()
    } else emptyList()

    // Remove automatic reset logic to prevent stale state issues
    // Let dropdowns handle invalid states gracefully by showing empty when options don't contain current value

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Personal Information",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Gray900
            ),
            modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
        )
        // First Name
        CompleteProfileTextField(
            value = formState.firstName,
            onValueChange = { value ->
                // Allow letters and spaces
                val filtered = value.filter { it.isLetter() || it == ' ' }

                // Collapse multiple spaces in the middle
                val collapsed = filtered.replace(Regex("\\s{2,}"), " ")

                // Keep trailing space while typing; capitalize each word
                val cleanValue = collapsed.toDisplayName()

                // Trim only for validation
                val isValid = ValidationUtils.isValidName(cleanValue.trim())

                onFormStateChange { old ->
                    old.copy(
                        firstName = cleanValue,
                        isFirstNameError = cleanValue.trim().isNotBlank() && !isValid,
                        firstNameErrorMessage = if (cleanValue.trim().isNotBlank() && !isValid)
                            "First name must be at least 2 characters and contain only letters" else ""
                    )
                }
            },
            placeholder = "*First Name",
            isError = formState.isFirstNameError,
            errorMessage = formState.firstNameErrorMessage,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )



        // Last Name + Suffix
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CompleteProfileTextField(
                value = formState.lastName,
                onValueChange = { value ->
                    // Allow letters and spaces
                    val filtered = value.filter { it.isLetter() || it == ' ' }

                    // Collapse multiple spaces in the middle
                    val collapsed = filtered.replace(Regex("\\s{2,}"), " ")

                    // Keep trailing space while typing; capitalize each word
                    val cleanValue = collapsed.toDisplayName()

                    // Trim only for validation
                    val isValid = ValidationUtils.isValidName(cleanValue.trim())

                    onFormStateChange { old ->
                        old.copy(
                            lastName = cleanValue,
                            isLastNameError = cleanValue.trim().isNotBlank() && !isValid,
                            lastNameErrorMessage = if (cleanValue.trim().isNotBlank() && !isValid)
                                "Last name must be at least 2 characters and contain only letters" else ""
                        )
                    }
                },
                placeholder = "*Last Name",
                isError = formState.isLastNameError,
                errorMessage = formState.lastNameErrorMessage,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                modifier = Modifier.weight(3f)
            )


            CompleteProfileDropdown(
                value = formState.suffix,
                onValueChange = { value -> onFormStateChange { old -> old.copy(suffix = if (value == "None") "" else value) } },
                options = listOf("None", "Jr.", "Sr.", "III", "IV", "V", "VI"),
                placeholder = "Suffix",
                modifier = Modifier.weight(2f)
            )
        }

        // Birthdate + Sex
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BirthDateTextField(
                birthDate = formState.birthDate,
                onBirthDateChange = { input ->
                    // Validate input manually
                    val isValid = try {
                        val sdf = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
                        sdf.isLenient = false
                        val date = sdf.parse(input)
                        date != null && ValidationUtils.isValidBirthday(date)
                    } catch (e: Exception) {
                        false
                    }

                    onFormStateChange { old ->
                        old.copy(
                            birthDate = input,
                            isBirthDateError = input.isNotBlank() && !isValid,
                            birthDateErrorMessage = if (input.isNotBlank() && !isValid)
                                "Invalid birthdate. Ensure it's a past date and realistic age." else ""
                        )
                    }
                },
                isError = formState.isBirthDateError,
                errorMessage = formState.birthDateErrorMessage,
                modifier = Modifier.weight(3f),
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )
            CompleteProfileDropdown(
                value = formState.sex,
                onValueChange = { value -> onFormStateChange { old -> old.copy(sex = value, isSexError = false, sexErrorMessage = "") } },
                options = listOf("Male", "Female"),
                placeholder = "*Sex",
                isError = formState.isSexError,
                errorMessage = formState.sexErrorMessage,
                modifier = Modifier.weight(2f)
            )
        }

        // Phone Number
        CompleteProfileTextField(
            value = formState.phoneNumber,
            onValueChange = { value ->
                var cleanValue = ""

                value.forEachIndexed { index, char ->
                    if (!char.isDigit()) return@forEachIndexed

                    when (index) {
                        0 -> if (char == '0') cleanValue += char
                        1 -> if (char == '9') cleanValue += char
                        in 2..10 -> cleanValue += char
                    }
                }

                // Limit to 11 digits
                if (cleanValue.length > 11) cleanValue = cleanValue.take(11)

                // Update the form state first
                onFormStateChange { old ->
                    old.copy(phoneNumber = cleanValue)
                }
                
                // Then trigger validation (including duplicate check)
                onPhoneNumberValidation?.invoke(cleanValue)
            },
            placeholder = "*Phone Number (09XXXXXXXXX)",
            isError = formState.isPhoneNumberError,
            errorMessage = formState.phoneNumberErrorMessage,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )


        // Address Section
        Text(
            text = "Address Information",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Gray900
            ),
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
        )

        // Country + Province
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CompleteProfileTextField(
                value = formState.country,
                onValueChange = {},
                placeholder = "Country",
                enabled = false,
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            CompleteProfileDropdown(
                value = formState.province,
                onValueChange = { value ->
                    onFormStateChange { old ->
                        old.copy(
                            province = value,
                            municipality = "",
                            barangay = "",
                            isProvinceError = false,
                            provinceErrorMessage = ""
                        )
                    }
                },
                options = provinces,
                placeholder = "*Province",
                isError = formState.isProvinceError,
                errorMessage = formState.provinceErrorMessage,
                modifier = Modifier.weight(1f)
            )
        }

        // Municipality + Barangay
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CompleteProfileDropdown(
                value = formState.municipality,
                onValueChange = { value ->
                    val display = value.toDisplayName()
                    onFormStateChange { old -> old.copy(municipality = display, barangay = "", isMunicipalityError = false, municipalityErrorMessage = "") }
                },
                options = municipalities,
                placeholder = "*Municipality/City",
                isError = formState.isMunicipalityError,
                errorMessage = formState.municipalityErrorMessage,
                enabled = formState.province.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
            CompleteProfileDropdown(
                value = formState.barangay,
                onValueChange = { value ->
                    val display = value.toDisplayName()
                    onFormStateChange { old -> old.copy(barangay = display) }
                },
                options = barangays,
                placeholder = "Barangay",
                enabled = formState.municipality.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
        }

        // Additional Address
        CompleteProfileTextField(
            value = formState.additionalAddress,
            onValueChange = { value ->
                // Allow letters, numbers, ñ/Ñ, spaces, and punctuation
                val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZñÑ0123456789 ,.#'-/"
                val filtered = value.filter { it in allowedChars }

                // Collapse multiple spaces in the middle
                val collapsed = filtered.replace(Regex("\\s{2,}"), " ")

                // Do NOT trim trailing spaces while typing
                val cleanValue = collapsed

                val isValid = ValidationUtils.isValidAdditionalAddress(cleanValue.trim()) // trim only for validation
                onFormStateChange { old ->
                    old.copy(
                        additionalAddress = cleanValue,
                        isAdditionalAddressError = cleanValue.trim().isNotBlank() && !isValid,
                        additionalAddressErrorMessage = if (cleanValue.trim().isNotBlank() && !isValid)
                            "Additional address must be at least 3 characters long and may only contain letters, numbers, spaces, and basic punctuation (,.#'-/)." else ""
                    )
                }
            },
            placeholder = "Additional Address",
            isError = formState.isAdditionalAddressError,
            errorMessage = formState.additionalAddressErrorMessage,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
            onImeAction = { focusManager.clearFocus() }
        )

        // ID Upload Section
        IdUploadComponent(
            frontImageUri = formState.idFrontImageUri,
            backImageUri = formState.idBackImageUri,
            onFrontImageSelected = { uri ->
                onFormStateChange { old ->
                    old.copy(
                        idFrontImageUri = uri,
                        isIdFrontError = false,
                        idFrontErrorMessage = ""
                    )
                }
            },
            onBackImageSelected = { uri ->
                onFormStateChange { old ->
                    old.copy(
                        idBackImageUri = uri,
                        isIdBackError = false,
                        idBackErrorMessage = ""
                    )
                }
            },
            isFrontError = formState.isIdFrontError,
            isBackError = formState.isIdBackError,
            frontErrorMessage = formState.idFrontErrorMessage,
            backErrorMessage = formState.idBackErrorMessage,
            modifier = Modifier.padding(top = 16.dp)
        )

    }
}
