package com.brightcare.patient.ui.component.emergency_contact

import java.util.regex.Pattern

/**
 * Validation utilities for Emergency Contact forms
 * Mga validation utilities para sa Emergency Contact forms
 */
object EmergencyContactValidation {
    
    /**
     * Validate full name with specific rules (consistent with complete profile):
     * - Allow letters and spaces
     * - Allow dots (.) for titles like "Dr."
     * - No numbers or other special characters
     * - No multiple consecutive spaces
     * - At least 2 characters
     */
    fun validateFullName(name: String): ValidationResult {
        val trimmedName = name.trim()
        
        return when {
            trimmedName.isBlank() -> ValidationResult(false, "Full name is required")
            trimmedName.length < 2 -> ValidationResult(false, "Full name must be at least 2 characters")
            trimmedName.length > 50 -> ValidationResult(false, "Full name must not exceed 50 characters")
            !isValidNamePattern(trimmedName) -> ValidationResult(false, "Full name can only contain letters, spaces, and dots")
            hasMultipleConsecutiveSpaces(trimmedName) -> ValidationResult(false, "Full name cannot have multiple consecutive spaces")
            containsOnlyDotsAndSpaces(trimmedName) -> ValidationResult(false, "Full name must contain at least one letter")
            else -> ValidationResult(true, "")
        }
    }
    
    /**
     * Format full name by removing extra spaces and capitalizing properly
     * Allow spaces between words but prevent multiple consecutive spaces
     */
    fun formatFullName(name: String): String {
        if (name.isBlank()) return name
        
        // Remove numbers and invalid special characters, keep letters, spaces, and dots
        val cleanName = name.filter { char ->
            char.isLetter() || char == ' ' || char == '.'
        }
        
        // Replace multiple consecutive spaces with single space
        val singleSpaced = cleanName.replace(Regex("\\s{2,}"), " ")
        
        // Simple capitalization without complex splitting to avoid crashes
        // Capitalize first letter of each word while preserving spaces
        var result = ""
        var capitalizeNext = true
        
        for (char in singleSpaced) {
            when {
                char == ' ' -> {
                    result += char
                    capitalizeNext = true
                }
                char.isLetter() -> {
                    result += if (capitalizeNext) {
                        capitalizeNext = false
                        char.uppercaseChar()
                    } else {
                        char.lowercaseChar()
                    }
                }
                else -> {
                    result += char
                    capitalizeNext = false
                }
            }
        }
        
        return result
    }
    
    /**
     * Validate relationship field
     */
    fun validateRelationship(relationship: String, customRelationship: String = ""): ValidationResult {
        return when {
            relationship.isBlank() -> ValidationResult(false, "Relationship is required")
            relationship == "Other" && customRelationship.trim().isBlank() -> 
                ValidationResult(false, "Please specify the relationship")
            relationship == "Other" && customRelationship.trim().length < 2 -> 
                ValidationResult(false, "Custom relationship must be at least 2 characters")
            relationship == "Other" && customRelationship.trim().length > 30 -> 
                ValidationResult(false, "Custom relationship must not exceed 30 characters")
            relationship == "Other" && !isValidCustomRelationship(customRelationship.trim()) -> 
                ValidationResult(false, "Custom relationship can contain letters and spaces")
            else -> ValidationResult(true, "")
        }
    }
    
    /**
     * Format custom relationship
     */
    fun formatCustomRelationship(relationship: String): String {
        if (relationship.isBlank()) return relationship
        
        // Allow only letters and spaces
        val cleanRelationship = relationship.filter { it.isLetter() || it == ' ' }
        
        // Replace multiple spaces with single space
        val singleSpaced = cleanRelationship.replace(Regex("\\s{2,}"), " ")
        
        // Simple capitalization without complex splitting to avoid crashes
        // Capitalize first letter of each word while preserving spaces
        var result = ""
        var capitalizeNext = true
        
        for (char in singleSpaced) {
            when {
                char == ' ' -> {
                    result += char
                    capitalizeNext = true
                }
                char.isLetter() -> {
                    result += if (capitalizeNext) {
                        capitalizeNext = false
                        char.uppercaseChar()
                    } else {
                        char.lowercaseChar()
                    }
                }
                else -> {
                    result += char
                    capitalizeNext = false
                }
            }
        }
        
        return result
    }
    
    /**
     * Validate phone number (Philippine format)
     */
    fun validatePhoneNumber(phone: String): ValidationResult {
        val cleanPhone = phone.trim()
        
        return when {
            cleanPhone.isBlank() -> ValidationResult(false, "Phone number is required")
            !cleanPhone.matches(Regex("^09\\d{9}$")) -> 
                ValidationResult(false, "Phone number must be in format 09XXXXXXXXX (11 digits)")
            else -> ValidationResult(true, "")
        }
    }
    
    /**
     * Format phone number to ensure proper format
     */
    fun formatPhoneNumber(phone: String): String {
        // Keep only digits
        val digitsOnly = phone.filter { it.isDigit() }
        
        if (digitsOnly.isEmpty()) return ""
        
        // Strict validation: only allow "09" as first two digits
        var result = ""
        
        for (i in digitsOnly.indices) {
            val digit = digitsOnly[i]
            when (i) {
                0 -> {
                    // First digit must be "0"
                    if (digit == '0') {
                        result += digit
                    } else {
                        // If first digit is not "0", reject the input
                        break
                    }
                }
                1 -> {
                    // Second digit must be "9"
                    if (digit == '9') {
                        result += digit
                    } else {
                        // If second digit is not "9", reject the input
                        break
                    }
                }
                in 2..10 -> {
                    // Remaining 9 digits can be any number (0-9)
                    result += digit
                }
                else -> {
                    // Limit to 11 digits total
                    break
                }
            }
        }
        
        return result
    }
    
    /**
     * Validate email address (optional but must be valid if provided)
     */
    fun validateEmail(email: String): ValidationResult {
        val trimmedEmail = email.trim()
        
        return when {
            trimmedEmail.isBlank() -> ValidationResult(true, "") // Email is optional
            trimmedEmail.length > 100 -> ValidationResult(false, "Email address is too long")
            !isValidEmailFormat(trimmedEmail) -> ValidationResult(false, "Please enter a valid email address")
            else -> ValidationResult(true, "")
        }
    }
    
    /**
     * Validate address (optional but must be valid if provided)
     */
    fun validateAddress(address: String): ValidationResult {
        val trimmedAddress = address.trim()
        
        return when {
            trimmedAddress.isBlank() -> ValidationResult(true, "") // Address is optional
            trimmedAddress.length < 5 -> ValidationResult(false, "Address must be at least 5 characters if provided")
            trimmedAddress.length > 200 -> ValidationResult(false, "Address must not exceed 200 characters")
            !isValidAddressFormat(trimmedAddress) -> 
                ValidationResult(false, "Address can contain letters, numbers, spaces, and basic punctuation (,.#'-/)")
            else -> ValidationResult(true, "")
        }
    }
    
    /**
     * Format address by cleaning invalid characters
     */
    fun formatAddress(address: String): String {
        if (address.isBlank()) return address
        
        // Allow letters, numbers, ñ/Ñ, spaces, and basic punctuation
        val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZñÑ0123456789 ,.#'-/"
        val filtered = address.filter { it in allowedChars }
        
        // Replace multiple spaces with single space but preserve leading/trailing spaces during typing
        return filtered.replace(Regex("\\s{2,}"), " ")
    }
    
    // Private helper functions
    private fun isValidNamePattern(text: String): Boolean {
        // Allow letters (A-Z, a-z), spaces, and dots only - consistent with complete profile
        return text.matches(Regex("^[A-Za-z .]+$"))
    }
    
    private fun hasMultipleConsecutiveSpaces(text: String): Boolean {
        return text.contains(Regex("\\s{2,}"))
    }
    
    private fun containsOnlyDotsAndSpaces(text: String): Boolean {
        return text.all { it == '.' || it == ' ' }
    }
    
    private fun isValidCustomRelationship(relationship: String): Boolean {
        // Allow only letters and spaces
        return relationship.all { it.isLetter() || it == ' ' } && 
               relationship.any { it.isLetter() } // Must contain at least one letter
    }
    
    private fun isValidEmailFormat(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }
    
    private fun isValidAddressFormat(address: String): Boolean {
        // Allow letters, numbers, ñ/Ñ, spaces, and basic punctuation
        val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZñÑ0123456789 ,.#'-/"
        return address.all { it in allowedChars }
    }
}

/**
 * Data class for validation results
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)
