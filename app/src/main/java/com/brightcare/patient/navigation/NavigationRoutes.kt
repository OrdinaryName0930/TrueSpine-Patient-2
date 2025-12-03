package com.brightcare.patient.navigation

/**
 * Navigation routes for the BrightCare Patient app
 * Centralized route definitions for type-safe navigation
 */
object NavigationRoutes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val TERMS_AND_CONDITIONS = "terms_and_conditions"
    const val FORGOT_PASSWORD_EMAIL = "forgot_password_email"
    const val FORGOT_PASSWORD_OTP = "forgot_password_otp/{email}"
    const val CHANGE_PASSWORD = "change_password"
    const val COMPLETE_PROFILE = "complete_profile"
    const val HOME = "home"
    const val DASHBOARD = "dashboard"
    
    // Main app navigation routes
    const val MAIN_DASHBOARD = "main_dashboard"
    const val CHIRO = "chiro"
    const val BOOKING = "booking"
    const val MESSAGE = "message"
    const val PROFILE = "profile"
    
    // Helper functions for parameterized routes
    fun forgotPasswordOtp(email: String) = "forgot_password_otp/$email"
}

/**
 * Navigation arguments keys
 */
object NavigationArgs {
    const val EMAIL = "email"
    const val TERMS_AGREED = "terms_agreed"
    const val PRESERVE_CHECKBOX = "preserve_checkbox"
}









