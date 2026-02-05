package com.brightcare.patient.data.model

import android.util.Log
import com.google.firebase.Timestamp
import java.util.Date

/**
 * Appointment data model for booking system
 * Model ng appointment para sa booking system
 */
data class Appointment(
    val id: String = "",
    val chiroId: String = "", // Changed from chiropractorId
    val clientId: String = "", // Changed from patientId
    val date: String = "", // Format: "2025-12-22" (YYYY-MM-DD)
    val time: String = "", // Format: "11:00" (24-hour format)
    val status: String = "pending", // pending | approved | booked | cancelled | completed
    val message: String = "", // Optional message from client/chiro
    val createdAt: Long = System.currentTimeMillis() / 1000, // Unix timestamp in seconds
    val whoBooked: String = "client", // client | chiro | admin
    val bookedByUid: String = "", // UID of who booked the appointment
    val paymentOption: String = "", // "full" or "downpayment"
    val paymentProofUri: String = "", // URI of uploaded payment proof
    val lastUpdated: Long = 0, // Unix timestamp in seconds
    val isReviewed: Boolean = false, // Track if appointment has been reviewed
    val reviewId: String = "", // ID of the review if reviewed
    
    // Legacy fields for backward compatibility (can be removed later)
    @Deprecated("Use chiroId instead")
    val chiropractorId: String = "",
    @Deprecated("Use clientId instead") 
    val patientId: String = "",
    val chiropractorName: String = "",
    val chiropractorSpecialization: String = "",
    val appointmentDate: Timestamp? = null,
    val appointmentTime: String = "",
    val duration: Int = 30,
    val appointmentType: AppointmentType = AppointmentType.CONSULTATION,
    val location: String = "",
    val notes: String = "",
    val symptoms: String = "",
    val isFirstVisit: Boolean = true,
    val updatedAt: Timestamp? = null,
    val cancelledAt: Timestamp? = null,
    val cancelledBy: String? = null,
    val cancellationReason: String? = null
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        chiroId = "",
        clientId = "",
        date = "",
        time = "",
        status = "pending",
        message = "",
        createdAt = System.currentTimeMillis() / 1000,
        whoBooked = "client",
        bookedByUid = "",
        paymentOption = "",
        paymentProofUri = "",
        lastUpdated = 0,
        isReviewed = false,
        reviewId = ""
    )

    /**
     * Convert to map for Firestore storage
     * I-convert sa map para sa Firestore storage
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "chiroId" to chiroId,
            "clientId" to clientId,
            "date" to date,
            "time" to time,
            "status" to status,
            "message" to message,
            "createdAt" to createdAt,
            "whoBooked" to whoBooked,
            "bookedByUid" to bookedByUid,
            "paymentOption" to paymentOption,
            "paymentProofUri" to paymentProofUri,
            "lastUpdated" to lastUpdated,
            "isReviewed" to isReviewed,
            "reviewId" to reviewId
        )
    }

    companion object {
        /**
         * Create from Firestore document data
         * Gumawa mula sa Firestore document data
         */
        fun fromMap(id: String, data: Map<String, Any>): Appointment {
            return Appointment(
                id = id,
                chiroId = data["chiroId"] as? String ?: "",
                clientId = data["clientId"] as? String ?: "",
                date = data["date"] as? String ?: "",
                time = data["time"] as? String ?: "",
                status = data["status"] as? String ?: "pending",
                message = data["message"] as? String ?: "",
                createdAt = (data["createdAt"] as? Long) ?: (System.currentTimeMillis() / 1000),
                whoBooked = data["whoBooked"] as? String ?: "client",
                bookedByUid = data["bookedByUid"] as? String ?: "",
                paymentOption = data["paymentOption"] as? String ?: "",
                paymentProofUri = data["paymentProofUri"] as? String ?: "",
                lastUpdated = (data["lastUpdated"] as? Long) ?: 0,
                isReviewed = data["isReviewed"] as? Boolean ?: false,
                reviewId = data["reviewId"] as? String ?: "",
                
                // Legacy fields for backward compatibility
                patientId = data["patientId"] as? String ?: data["clientId"] as? String ?: "",
                chiropractorId = data["chiropractorId"] as? String ?: data["chiroId"] as? String ?: "",
                chiropractorName = data["chiropractorName"] as? String ?: "",
                chiropractorSpecialization = data["chiropractorSpecialization"] as? String ?: "",
                appointmentDate = data["appointmentDate"] as? Timestamp,
                appointmentTime = data["appointmentTime"] as? String ?: data["time"] as? String ?: "",
                duration = (data["duration"] as? Long)?.toInt() ?: 30,
                appointmentType = AppointmentType.fromString(data["appointmentType"] as? String ?: "consultation"),
                location = data["location"] as? String ?: "",
                notes = data["notes"] as? String ?: data["message"] as? String ?: "",
                symptoms = data["symptoms"] as? String ?: "",
                isFirstVisit = data["isFirstVisit"] as? Boolean ?: true,
                updatedAt = data["updatedAt"] as? Timestamp,
                cancelledAt = data["cancelledAt"] as? Timestamp,
                cancelledBy = data["cancelledBy"] as? String,
                cancellationReason = data["cancellationReason"] as? String
            )
        }
    }
}

/**
 * Appointment type enumeration
 * Enumeration ng uri ng appointment
 */
enum class AppointmentType(val displayName: String) {
    CONSULTATION("Consultation"),
    TREATMENT("Treatment"),
    FOLLOW_UP("Follow Up"),
    THERAPY("Therapy"),
    ADJUSTMENT("Adjustment"),
    ASSESSMENT("Assessment");

    companion object {
        fun fromString(value: String): AppointmentType {
            return values().find { it.name.lowercase() == value.lowercase() } ?: CONSULTATION
        }
    }
}

/**
 * Appointment status enumeration
 * Enumeration ng status ng appointment
 */
enum class AppointmentStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW,
    RESCHEDULED;

    companion object {
        fun fromString(value: String): AppointmentStatus {
            return values().find { it.name.lowercase() == value.lowercase() } ?: PENDING
        }
    }
}

/**
 * Time slot data model for booking
 * Model ng time slot para sa booking
 */
data class TimeSlot(
    val time: String, // Format: "10:00 AM"
    val isAvailable: Boolean = true,
    val isBooked: Boolean = false,
    val duration: Int = 60 // Duration in minutes
)

/**
 * Available dates and time slots for a chiropractor
 * Available dates at time slots para sa chiropractor
 */
data class ChiropractorAvailability(
    val chiropractorId: String = "",
    val date: Date = Date(),
    val timeSlots: List<TimeSlot> = emptyList(),
    val isAvailable: Boolean = true
)

/**
 * Booking request form state
 * Form state para sa booking request
 */
data class BookingFormState(
    val selectedChiropractorId: String = "",
    val selectedDate: Date? = null,
    val selectedTime: String = "",
    val appointmentType: AppointmentType = AppointmentType.CONSULTATION,
    val symptoms: String = "",
    val notes: String = "",
    val isFirstVisit: Boolean = true,
    val paymentOption: String = "downpayment", // "full" or "downpayment"
    val paymentProofUri: String = "", // URI of uploaded payment proof image
    
    // Validation states
    val isDateError: Boolean = false,
    val isTimeError: Boolean = false,
    val isSymptomsError: Boolean = false,
    
    // Error messages
    val dateErrorMessage: String = "",
    val timeErrorMessage: String = "",
    val symptomsErrorMessage: String = ""
)

/**
 * Profile validation result
 * Result ng profile validation
 */
data class ProfileValidationResult(
    val isValid: Boolean = false,
    val hasPersonalDetails: Boolean = false,
    val hasEmergencyContact: Boolean = false,
    val missingFields: List<String> = emptyList(),
    val errorMessage: String? = null
)

/**
 * Chiropractor unavailable date data model
 * Model ng unavailable date para sa chiropractor
 */
data class UnavailableDate(
    val id: String = "",
    val date: String = "", // Format: "2025-12-10"
    val fullDay: Boolean = false,
    val times: List<String> = emptyList() // Format: ["10:00", "14:00"] (24-hour format)
) {
    companion object {
        /**
         * Create from Firestore document data
         * Gumawa mula sa Firestore document data
         */
        fun fromMap(id: String, data: Map<String, Any>): UnavailableDate {
            val date = data["date"] as? String ?: ""
            val fullDay = data["fullDay"] as? Boolean ?: false
            val times = (data["times"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            
            android.util.Log.d("UnavailableDate", "Creating UnavailableDate - ID: $id, Date: $date, FullDay: $fullDay, Times: $times")
            
            return UnavailableDate(
                id = id,
                date = date,
                fullDay = fullDay,
                times = times
            )
        }
    }
}

/**
 * Chiropractor unavailability data model
 * Model ng unavailability para sa chiropractor
 */
data class ChiropractorUnavailability(
    val chiropractorId: String = "",
    val dates: List<UnavailableDate> = emptyList()
) {
    companion object {
        /**
         * Create from Firestore document data
         * Gumawa mula sa Firestore document data
         */
        fun fromMap(chiropractorId: String, data: Map<String, Any>): ChiropractorUnavailability {
            android.util.Log.d("ChiropractorUnavailability", "Parsing data for chiropractor: $chiropractorId")
            android.util.Log.d("ChiropractorUnavailability", "Raw data keys: ${data.keys}")
            
            val datesData = data["dates"] as? Map<String, Any> ?: emptyMap()
            android.util.Log.d("ChiropractorUnavailability", "Dates data: $datesData")
            
            val unavailableDates = datesData.map { (id, dateData) ->
                android.util.Log.d("ChiropractorUnavailability", "Processing date entry - ID: $id, Data: $dateData")
                UnavailableDate.fromMap(id, dateData as Map<String, Any>)
            }
            
            android.util.Log.d("ChiropractorUnavailability", "Parsed ${unavailableDates.size} unavailable dates")
            unavailableDates.forEach { date ->
                android.util.Log.d("ChiropractorUnavailability", "Unavailable date: ${date.date}, fullDay: ${date.fullDay}")
            }
            
            return ChiropractorUnavailability(
                chiropractorId = chiropractorId,
                dates = unavailableDates
            )
        }
    }
    
    /**
     * Check if a specific date is fully unavailable
     * I-check kung fully unavailable ang specific date
     */
    fun isDateFullyUnavailable(date: String): Boolean {
        val result = dates.any { it.date == date && it.fullDay }
        android.util.Log.d("ChiropractorUnavailability", "Checking if date $date is fully unavailable: $result")
        android.util.Log.d("ChiropractorUnavailability", "Available dates: ${dates.map { "${it.date} (fullDay: ${it.fullDay})" }}")
        return result
    }
    
    /**
     * Get unavailable times for a specific date
     * Kunin ang unavailable times para sa specific date
     */
    fun getUnavailableTimesForDate(date: String): List<String> {
        return dates.filter { it.date == date && !it.fullDay }
            .flatMap { it.times }
    }
    
    /**
     * Check if a specific time is unavailable on a date
     * I-check kung unavailable ang specific time sa isang date
     */
    fun isTimeUnavailable(date: String, time: String): Boolean {
        // Check if the whole day is unavailable
        if (isDateFullyUnavailable(date)) {
            return true
        }
        
        // Check if the specific time is unavailable
        val unavailableTimes = getUnavailableTimesForDate(date)
        return unavailableTimes.contains(time)
    }
}

/**
 * Booking UI state
 * UI state para sa booking
 */
data class BookingUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isValidatingProfile: Boolean = false,
    val profileValidation: ProfileValidationResult = ProfileValidationResult(),
    val selectedChiropractor: Chiropractor? = null,
    val availableTimeSlots: List<TimeSlot> = emptyList(),
    val chiropractorUnavailability: ChiropractorUnavailability? = null,
    val formState: BookingFormState = BookingFormState(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showProfileIncompleteDialog: Boolean = false
)

/**
 * Review data model for chiropractor reviews
 * Model ng review para sa chiropractor
 */
data class Review(
    val id: String = "",
    val appointmentId: String = "",
    val chiropractorId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val rating: Int = 0, // 1-5 stars
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isAnonymous: Boolean = false
) {
    /**
     * Convert to map for Firestore storage
     * I-convert sa map para sa Firestore storage
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "appointmentId" to appointmentId,
            "chiropractorId" to chiropractorId,
            "clientId" to clientId,
            "clientName" to clientName,
            "rating" to rating,
            "comment" to comment,
            "createdAt" to createdAt,
            "isAnonymous" to isAnonymous
        )
    }

    companion object {
        /**
         * Create from Firestore document data
         * Gumawa mula sa Firestore document data
         */
        fun fromMap(id: String, data: Map<String, Any>): Review {
            return Review(
                id = id,
                appointmentId = data["appointmentId"] as? String ?: "",
                chiropractorId = data["chiropractorId"] as? String ?: "",
                clientId = data["clientId"] as? String ?: "",
                clientName = data["clientName"] as? String ?: "",
                rating = (data["rating"] as? Long)?.toInt() ?: 0,
                comment = data["comment"] as? String ?: "",
                createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                isAnonymous = data["isAnonymous"] as? Boolean ?: false
            )
        }
    }
}

