package com.brightcare.patient.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import android.util.Log

/**
 * Extension functions for navigation to provide type-safe navigation
 * and common navigation patterns
 */

/**
 * Navigate to login screen and clear the back stack
 */
fun NavController.navigateToLogin(clearBackStack: Boolean = false) {
    val navOptions = if (clearBackStack) {
        NavOptions.Builder()
            .setPopUpTo(graph.startDestinationId, inclusive = true)
            .build()
    } else null
    
    navigate(NavigationRoutes.LOGIN, navOptions)
}

/**
 * Navigate to login screen from sign-up and clear login states
 */
fun NavController.navigateToLoginFromSignUp() {
    // Add a parameter to indicate we're coming from sign-up
    navigate("${NavigationRoutes.LOGIN}?clearStates=true")
}

/**
 * Navigate to signup screen
 */
fun NavController.navigateToSignUp() {
    navigate(NavigationRoutes.SIGNUP)
}

/**
 * Navigate to complete profile screen after successful social login
 */
fun NavController.navigateToCompleteProfile(clearBackStack: Boolean = true) {
    val navOptions = if (clearBackStack) {
        NavOptions.Builder()
            .setPopUpTo(NavigationRoutes.LOGIN, inclusive = true)
            .build()
    } else null
    
    navigate(NavigationRoutes.COMPLETE_PROFILE, navOptions)
}

/**
 * Navigate to home/dashboard after successful login
 */
fun NavController.navigateToHome(clearBackStack: Boolean = true) {
    val navOptions = if (clearBackStack) {
        NavOptions.Builder()
            .setPopUpTo(NavigationRoutes.LOGIN, inclusive = true)
            .build()
    } else null
    
    navigate(NavigationRoutes.MAIN_DASHBOARD, navOptions)
}

/**
 * Navigate to dashboard after successful login
 */
fun NavController.navigateToDashboard(clearBackStack: Boolean = true) {
    val navOptions = if (clearBackStack) {
        NavOptions.Builder()
            .setPopUpTo(NavigationRoutes.LOGIN, inclusive = true)
            .build()
    } else null
    
    navigate(NavigationRoutes.DASHBOARD, navOptions)
}

/**
 * Navigate based on login result
 */
fun NavController.navigateAfterLogin(
    isEmailVerified: Boolean,
    isProfileComplete: Boolean,
    clearBackStack: Boolean = true
) {
    Log.d("NavigationExtensions", "navigateAfterLogin called:")
    Log.d("NavigationExtensions", "  - isEmailVerified: $isEmailVerified")
    Log.d("NavigationExtensions", "  - isProfileComplete: $isProfileComplete")
    
    when {
        !isEmailVerified -> {
            Log.d("NavigationExtensions", "  - Action: Staying on login (email not verified)")
            // Stay on login screen, show email verification dialog
            // This should be handled by the UI, not navigation
        }
        !isProfileComplete -> {
            Log.d("NavigationExtensions", "  - Action: Navigating to Complete Profile")
            navigateToCompleteProfile(clearBackStack)
        }
        else -> {
            Log.d("NavigationExtensions", "  - Action: Navigating to Home")
            navigateToHome(clearBackStack)
        }
    }
}

/**
 * Navigate to terms and conditions screen while preserving checkbox state
 */
fun NavController.navigateToTermsAndConditions(preserveCheckbox: Boolean = false) {
    if (preserveCheckbox) {
        currentBackStackEntry?.savedStateHandle?.set(
            NavigationArgs.PRESERVE_CHECKBOX, 
            true
        )
    }
    navigate(NavigationRoutes.TERMS_AND_CONDITIONS)
}

/**
 * Navigate to forgot password OTP screen with email parameter
 */
fun NavController.navigateToForgotPasswordOtp(email: String) {
    navigate(NavigationRoutes.forgotPasswordOtp(email))
}

/**
 * Navigate back to previous screen safely
 */
fun NavController.navigateBack(): Boolean {
    return if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        false
    }
}

/**
 * Clear all saved state for the current back stack entry
 */
fun NavController.clearSavedState() {
    currentBackStackEntry?.savedStateHandle?.apply {
        remove<Boolean>(NavigationArgs.TERMS_AGREED)
        remove<Boolean>(NavigationArgs.PRESERVE_CHECKBOX)
        remove<Boolean>(NavigationArgs.TERMS_AGREED_COMPLETE_PROFILE)
    }
}









