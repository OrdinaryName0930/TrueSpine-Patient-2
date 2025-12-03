package com.brightcare.patient.ui.component.signup_component

import android.util.Patterns
import java.util.Calendar
import java.util.Date

/**
 * Utility object for form validation functions
 */
object ValidationUtils {

    /**
     * Validates if the email format is correct (strict version)
     * Requirements:
     * - Starts with a lowercase letter or number
     * - Allows letters, numbers, dots, underscores, %, +, -
     * - Domain must start and end with letter/number, can have hyphens
     * - TLD must be at least 2 letters
     * - Also checks Android's built-in Patterns.EMAIL_ADDRESS
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false

        // Convert email to lowercase for consistency
        val lowercasedEmail = email.lowercase()

        // Strict email regex
        val strictEmailRegex = Regex(
            "^[a-z0-9][a-z0-9._%+-]+@[a-z0-9][a-z0-9-]*[a-z0-9]\\.[a-z]{2,}$"
        )

        // Check if it matches the regex and Android's standard pattern
        if (!strictEmailRegex.matches(lowercasedEmail) || !Patterns.EMAIL_ADDRESS.matcher(lowercasedEmail).matches()) {
            return false
        }

        // Ensure local part (before @) contains at least one letter
        val localPart = lowercasedEmail.substringBefore("@")
        if (!localPart.any { it.isLetter() }) return false

        return true
    }

    /**
     * Validates if the password meets strength requirements
     * Requirements: At least 8 characters, contains letters, numbers, and special characters
     */
    fun isStrongPassword(password: String): Boolean {
        if (password.length < 8) return false

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar
    }
    /**
     * Validates if the password have spaces
     * Requirements: Password should not have spaces
     */

    fun hasNoWhitespace(password: String): Boolean {
        return password.none { it.isWhitespace() }
    }

    /**
     * Remove any non-digit characters
     * Check if it starts with 09 and has exactly 11 digits
     */

    fun isValidPhoneNumber(phone: String): Boolean {
        val cleanPhone = phone.replace(Regex("[^\\d]"), "")
        return cleanPhone.matches(Regex("^09\\d{9}\$"))
    }

    /**
     * Validates if the first name is valid.
     * Requirements:
     * - At least 2 characters long
     * - Only alphabetic characters and spaces are allowed
     */

    fun isValidName(firstName: String): Boolean {
        val trimmedName = firstName.trim()
        return trimmedName.length >= 2 && trimmedName.matches(Regex("^[A-Za-z ]+$"))
    }


    fun isValidBirthday(birthDate: Date): Boolean {
        val today = Calendar.getInstance()
        val birthCal = Calendar.getInstance()
        birthCal.time = birthDate
        
        // Birth date cannot be today or in the future
        if (!birthCal.before(today)) return false
        
        // Calculate accurate age considering month and day
        var age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
        
        // Adjust age if birthday hasn't occurred this year yet
        if (today.get(Calendar.MONTH) < birthCal.get(Calendar.MONTH) ||
            (today.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH) && 
             today.get(Calendar.DAY_OF_MONTH) < birthCal.get(Calendar.DAY_OF_MONTH))) {
            age--
        }
        
        // Age must be between 0 and 122 years (reasonable human lifespan)
        if (age < 0 || age > 122) return false
        
        return true
    }

    /**
     * Validates if the birthday is a valid date of birth.
     * Rules:
     * - Cannot be today
     * - Cannot be in the future
     * - Cannot be more than 122 years old
     */

    fun isValidAdditionalAddress(address: String): Boolean {
        val trimmedAddress = address.trim()

        // Optional: valid if empty
        if (trimmedAddress.isEmpty()) return true

        // Check minimum length
        if (trimmedAddress.length < 3) return false

        // Check allowed characters including ñ and Ñ
        val pattern = Regex("^[a-zA-ZñÑ0-9 ,.#'\\-/]+$")

        return pattern.matches(trimmedAddress)
    }

    /**
     * Validates the "Additional Address" input.
     * Returns:
     * - null if valid (no error)
     * - error message string if invalid
     *
     * Rules:
     * - Optional field (only validated if not blank)
     * - Must be at least 3 characters long
     * - Allows only letters, numbers, spaces, and basic punctuation (,.#'-/)
     */
    //Unused Validations
    /**
     * Gets password strength level (0-4)
     * 0: Very weak, 1: Weak, 2: Fair, 3: Good, 4: Strong
     */
    fun getPasswordStrength(password: String): Int {
        var strength = 0

        if (password.length >= 8) strength++
        if (password.any { it.isLowerCase() }) strength++
        if (password.any { it.isUpperCase() }) strength++
        if (password.any { it.isDigit() }) strength++
        if (password.any { !it.isLetterOrDigit() }) strength++

        return minOf(strength, 4)
    }

    /**
     * Gets password strength description
     */
    fun getPasswordStrengthText(strength: Int): String {
        return when (strength) {
            0, 1 -> "Very weak"
            2 -> "Weak"
            3 -> "Fair"
            4 -> "Strong"
            else -> "Unknown"
        }
    }
}
