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
    const val PRIVACY_POLICY = "privacy_policy"
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
    const val NOTIFICATIONS = "notifications"
    const val PERSONAL_DETAILS = "personal_details"
    const val EMERGENCY_CONTACTS = "emergency_contacts"
    
    // Booking routes
    const val CHIROPRACTOR_BOOKING = "chiropractor_booking/{chiropractorId}"
    const val BOOK_APPOINTMENT = "book_appointment/{chiropractorId}"
    const val PAYMENT = "payment/{chiropractorId}/{date}/{time}/{paymentOption}?message={message}"
    const val APPOINTMENT_DETAILS = "appointment_details/{appointmentId}"
    
    // Conversation/Chat routes
    const val CONVERSATION = "conversation/{conversationId}"
    
    // Profile routes
    const val VIEW_PROFILE = "view_profile/{chiropractorId}"
    
    // Helper functions for parameterized routes
    fun forgotPasswordOtp(email: String) = "forgot_password_otp/$email"
    fun conversation(conversationId: String) = "conversation/$conversationId"
    fun chiropractorBooking(chiropractorId: String) = "chiropractor_booking/$chiropractorId"
    fun bookAppointment(chiropractorId: String) = "book_appointment/$chiropractorId"
    fun viewProfile(chiropractorId: String) = "view_profile/$chiropractorId"
    fun appointmentDetails(appointmentId: String) = "appointment_details/$appointmentId"
    
    // Payment route helper - pass all appointment data
    fun payment(
        chiropractorId: String,
        date: String,
        time: String,
        paymentOption: String,
        message: String = ""
    ): String {
        val encodedTime = java.net.URLEncoder.encode(time, "UTF-8")
        val encodedMessage = java.net.URLEncoder.encode(message, "UTF-8")
        return "payment/$chiropractorId/$date/$encodedTime/$paymentOption?message=$encodedMessage"
    }
}

/**
 * Navigation arguments keys
 */
object NavigationArgs {
    const val EMAIL = "email"
    const val TERMS_AGREED = "terms_agreed"
    const val PRESERVE_CHECKBOX = "preserve_checkbox"
    const val TERMS_AGREED_COMPLETE_PROFILE = "terms_agreed_complete_profile"
    const val CONVERSATION_ID = "conversationId"
    const val CHIROPRACTOR_ID = "chiropractorId"
    const val APPOINTMENT_ID = "appointmentId"
    
    // Payment arguments
    const val DATE = "date"
    const val TIME = "time"
    const val PAYMENT_OPTION = "paymentOption"
    const val MESSAGE = "message"
}









