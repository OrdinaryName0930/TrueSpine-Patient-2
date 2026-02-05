package com.brightcare.patient.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.brightcare.patient.data.model.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling booking and appointment operations
 * Repository para sa pag-handle ng booking at appointment operations
 */
@Singleton
class BookingRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val profileValidationService: ProfileValidationService,
    private val notificationRepository: NotificationRepository
) {
    
    companion object {
        private const val TAG = "BookingRepository"
        private const val COLLECTION_APPOINTMENTS = "appointment"
        private const val COLLECTION_CHIROPRACTORS = "chiropractors"  // Changed to plural to match Firestore
        private const val COLLECTION_AVAILABILITY = "availability"
    }
    
    /**
     * Book an appointment with a chiropractor
     * Mag-book ng appointment sa chiropractor
     */
    suspend fun bookAppointment(
        chiropractorId: String,
        appointmentDate: Date,
        appointmentTime: String,
        appointmentType: AppointmentType,
        symptoms: String,
        notes: String,
        isFirstVisit: Boolean,
        paymentOption: String = "",
        paymentProofUri: String = ""
    ): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in to book appointment"))
            }
            
            Log.d(TAG, "Starting appointment booking for user: ${currentUser.uid}")
            
            // Validate profile first
            val profileValidation = profileValidationService.validateProfileForBooking()
            if (!profileValidation.isValid) {
                Log.w(TAG, "Profile validation failed: ${profileValidation.missingFields}")
                return Result.failure(Exception("Profile incomplete: ${profileValidation.errorMessage}"))
            }
            
            // Generate appointment ID
            val appointmentId = UUID.randomUUID().toString()
            
            // Format date as YYYY-MM-DD
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(appointmentDate)
            
            // Convert time from "10:00 AM" to "10:00" (24-hour format)
            val formattedTime = convertTo24HourFormat(appointmentTime)
            
            // Combine message from symptoms and notes
            val message = if (symptoms.isNotBlank() && notes.isNotBlank()) {
                "$symptoms. $notes"
            } else if (symptoms.isNotBlank()) {
                symptoms
            } else if (notes.isNotBlank()) {
                notes
            } else {
                "General consultation"
            }
            
            // Fetch chiropractor information for the appointment
            val chiropractorDoc = firestore.collection(COLLECTION_CHIROPRACTORS)
                .document(chiropractorId)
                .get()
                .await()
            
            val chiropractorName = if (chiropractorDoc.exists()) {
                val data = chiropractorDoc.data!!
                buildChiropractorName(data)
            } else {
                "Unknown Doctor"
            }
            
            val chiropractorSpecialization = if (chiropractorDoc.exists()) {
                chiropractorDoc.data!!["specialization"] as? String ?: "General Practice"
            } else {
                "General Practice"
            }
            
            // Create appointment with new format including chiropractor info and payment data
            val appointment = Appointment(
                id = appointmentId,
                chiroId = chiropractorId,
                clientId = currentUser.uid,
                date = formattedDate,
                time = formattedTime,
                status = "pending",
                message = message,
                createdAt = System.currentTimeMillis() / 1000, // Unix timestamp in seconds
                whoBooked = "client",
                bookedByUid = currentUser.uid,
                paymentOption = paymentOption,
                paymentProofUri = paymentProofUri,
                lastUpdated = System.currentTimeMillis() / 1000,
                chiropractorName = chiropractorName,
                chiropractorSpecialization = chiropractorSpecialization
            )
            
            // Save appointment to Firestore with new path structure
            firestore.collection(COLLECTION_APPOINTMENTS)
                .document(appointmentId)
                .set(appointment.toMap())
                .await()
            
            // Create notification for new booking using existing NotificationRepository
            try {
                Log.d(TAG, "Creating notification for new booking...")
                val result = notificationRepository.createNotification(
                    title = "New Appointment Booked 📅",
                    message = "Your appointment with $chiropractorName on $formattedDate at $appointmentTime has been submitted and is pending approval.",
                    type = NotificationType.NEW_BOOKING,
                    appointmentId = appointmentId
                )
                result.fold(
                    onSuccess = { notificationId ->
                        Log.d(TAG, "Notification created successfully: $notificationId")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to create notification: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception creating notification: ${e.message}", e)
                // Don't fail the booking if notification fails
            }
            
            Log.d(TAG, "Appointment booked successfully: $appointmentId")
            Result.success(appointmentId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error booking appointment", e)
            Result.failure(Exception("Failed to book appointment: ${e.message}"))
        }
    }
    
    /**
     * Get user's appointments
     * Kumuha ng mga appointment ng user
     */
    suspend fun getUserAppointments(): Result<List<Appointment>> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            Log.d(TAG, "Fetching appointments for user: ${currentUser.uid}")
            
            val querySnapshot = firestore.collection(COLLECTION_APPOINTMENTS)
                .whereEqualTo("clientId", currentUser.uid)
                .get()
                .await()
            
            val appointments = querySnapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data
                    if (data != null) {
                        Appointment.fromMap(document.id, data)
                    } else null
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing appointment document: ${document.id}", e)
                    null
                }
            }.sortedByDescending { it.createdAt }
            
            // Enrich appointments with chiropractor information
            val enrichedAppointments = enrichAppointmentsWithChiropractorInfo(appointments)
            
            Log.d(TAG, "Successfully fetched and enriched ${enrichedAppointments.size} appointments")
            Result.success(enrichedAppointments)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user appointments", e)
            Result.failure(Exception("Failed to fetch appointments: ${e.message}"))
        }
    }
    
    /**
     * Get upcoming appointments
     * Kumuha ng mga upcoming appointments
     */
    suspend fun getUpcomingAppointments(): Result<List<Appointment>> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User must be logged in"))
            }
            
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val querySnapshot = firestore.collection(COLLECTION_APPOINTMENTS)
                .whereEqualTo("clientId", currentUser.uid)
                .whereIn("status", listOf("pending", "confirmed", "approved"))
                .whereGreaterThanOrEqualTo("date", today)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val appointments = querySnapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data
                    if (data != null) {
                        Appointment.fromMap(document.id, data)
                    } else null
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing appointment document: ${document.id}", e)
                    null
                }
            }
            
            // Enrich appointments with chiropractor information
            val enrichedAppointments = enrichAppointmentsWithChiropractorInfo(appointments)
            
            Log.d(TAG, "Successfully fetched and enriched ${enrichedAppointments.size} upcoming appointments")
            Result.success(enrichedAppointments)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching upcoming appointments", e)
            Result.failure(Exception("Failed to fetch upcoming appointments: ${e.message}"))
        }
    }
    
    /**
     * Cancel an appointment
     * I-cancel ang appointment
     */
    suspend fun cancelAppointment(
        appointmentId: String,
        cancellationReason: String
    ): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User must be logged in"))
            }
            
            Log.d(TAG, "Cancelling appointment: $appointmentId")
            
            val updateData = mapOf(
                "status" to "cancelled",
                "message" to "Cancelled: $cancellationReason"
            )
            
            firestore.collection(COLLECTION_APPOINTMENTS)
                .document(appointmentId)
                .update(updateData)
                .await()
            
            Log.d(TAG, "Appointment cancelled successfully: $appointmentId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling appointment", e)
            Result.failure(Exception("Failed to cancel appointment: ${e.message}"))
        }
    }
    
    /**
     * Get available time slots for a chiropractor on a specific date
     * Kumuha ng available time slots para sa chiropractor sa specific date
     */
    suspend fun getAvailableTimeSlots(
        chiropractorId: String,
        date: Date
    ): Result<List<TimeSlot>> {
        return try {
            Log.d(TAG, "Fetching available time slots for chiropractor: $chiropractorId on date: $date")
            
            // Get booked times for this chiropractor on this date
            val bookedTimesResult = getBookedTimesForDoctorAndDate(chiropractorId, date)
            val bookedTimes = bookedTimesResult.getOrElse { 
                Log.w(TAG, "Failed to get booked times, proceeding with empty set")
                emptySet()
            }
            
            // Generate default time slots (10 AM to 7 PM, 30-minute intervals)
            val timeSlots = generateDefaultTimeSlots().map { timeSlot ->
                val isBooked = bookedTimes.contains(timeSlot.time)
                val isPastTime = isTimeSlotInPast(timeSlot.time, date)
                
                timeSlot.copy(
                    isAvailable = !isBooked && !isPastTime,
                    isBooked = isBooked
                )
            }
            
            Log.d(TAG, "Generated ${timeSlots.size} time slots, ${timeSlots.count { it.isAvailable }} available")
            Result.success(timeSlots)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching available time slots", e)
            Result.failure(Exception("Failed to fetch available time slots: ${e.message}"))
        }
    }
    
    /**
     * Generate default time slots for a day
     * Mag-generate ng default time slots para sa isang araw
     */
    private fun generateDefaultTimeSlots(): List<TimeSlot> {
        val timeSlots = mutableListOf<TimeSlot>()
        var currentHour = 10 // Start at 10:00 AM
        var currentMinute = 0
        val endHour = 19 // End at 7:00 PM (19:00 in 24-hour format)
        
        while (currentHour < endHour || (currentHour == endHour && currentMinute == 0)) {
            val displayHour = if (currentHour > 12) currentHour - 12 else currentHour
            val amPm = if (currentHour < 12) "AM" else "PM"
            val minuteStr = if (currentMinute == 0) "00" else currentMinute.toString()
            val timeString = "$displayHour:$minuteStr $amPm"
            
            // Create TimeSlot with 30-minute duration
            timeSlots.add(TimeSlot(timeString, true, false, 30))
            
            // Increment by 30 minutes
            currentMinute += 30
            if (currentMinute >= 60) {
                currentMinute = 0
                currentHour++
            }
        }
        
        return timeSlots
    }
    
    /**
     * Get user's booked dates to prevent double booking
     * Kunin ang mga booked dates ng user para maiwasan ang double booking
     */
    suspend fun getUserBookedDates(): Result<Set<String>> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            Log.d(TAG, "Fetching booked dates for user: ${currentUser.uid}")
            
            val querySnapshot = firestore.collection(COLLECTION_APPOINTMENTS)
                .whereEqualTo("clientId", currentUser.uid)
                .whereIn("status", listOf("pending", "approved", "booked", "confirmed"))
                .get()
                .await()
            
            val bookedDates = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.getString("date")
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing appointment date: ${document.id}", e)
                    null
                }
            }.toSet()
            
            Log.d(TAG, "Found ${bookedDates.size} booked dates for user")
            Result.success(bookedDates)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user booked dates", e)
            Result.failure(Exception("Failed to fetch booked dates: ${e.message}"))
        }
    }
    
    /**
     * Get unique patient count for a specific chiropractor
     * Kunin ang bilang ng mga unique patients para sa specific chiropractor
     */
    suspend fun getPatientCountForChiropractor(chiropractorId: String): Result<Int> {
        return try {
            Log.d(TAG, "Fetching patient count for chiropractor: $chiropractorId")
            
            val querySnapshot = firestore.collection(COLLECTION_APPOINTMENTS)
                .whereEqualTo("chiroId", chiropractorId)
                .get()
                .await()
            
            val uniqueClientIds = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.getString("clientId")
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing clientId from appointment: ${document.id}", e)
                    null
                }
            }.toSet()
            
            val patientCount = uniqueClientIds.size
            Log.d(TAG, "Found $patientCount unique patients for chiropractor: $chiropractorId")
            Result.success(patientCount)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching patient count for chiropractor", e)
            Result.failure(Exception("Failed to fetch patient count: ${e.message}"))
        }
    }
    
    /**
     * Get booked times for a specific doctor on a specific date
     * Kunin ang mga booked times para sa specific doctor sa specific date
     */
    suspend fun getBookedTimesForDoctorAndDate(chiropractorId: String, date: Date): Result<Set<String>> {
        return try {
            Log.d(TAG, "Fetching booked times for chiropractor: $chiropractorId on date: $date")
            
            // Format date for query (YYYY-MM-DD)
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(date)
            
            val querySnapshot = firestore.collection(COLLECTION_APPOINTMENTS)
                .whereEqualTo("chiroId", chiropractorId)
                .whereEqualTo("date", formattedDate)
                .whereIn("status", listOf("pending", "approved", "booked", "confirmed"))
                .get()
                .await()
            
            val bookedTimes = querySnapshot.documents.mapNotNull { document ->
                try {
                    val time24Hour = document.getString("time")
                    // Convert from 24-hour format to 12-hour format for comparison with UI
                    time24Hour?.let { convertFrom24HourFormat(it) }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing appointment time: ${document.id}", e)
                    null
                }
            }.toSet()
            
            Log.d(TAG, "Found ${bookedTimes.size} booked times for chiropractor $chiropractorId on $formattedDate: $bookedTimes")
            Result.success(bookedTimes)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching booked times for doctor", e)
            Result.failure(Exception("Failed to fetch booked times: ${e.message}"))
        }
    }
    
    /**
     * Get chiropractor unavailability data
     * Kunin ang unavailability data ng chiropractor
     */
    suspend fun getChiropractorUnavailability(chiropractorId: String): Result<ChiropractorUnavailability> {
        return try {
            Log.d(TAG, "Fetching unavailability data for chiropractor: $chiropractorId")
            
            val document = firestore.collection("chiro_unavailable")
                .document(chiropractorId)
                .get()
                .await()
            
            if (document.exists()) {
                val data = document.data ?: emptyMap()
                val unavailability = ChiropractorUnavailability.fromMap(chiropractorId, data)
                Log.d(TAG, "Found ${unavailability.dates.size} unavailable date entries for chiropractor: $chiropractorId")
                Result.success(unavailability)
            } else {
                Log.d(TAG, "No unavailability data found for chiropractor: $chiropractorId")
                Result.success(ChiropractorUnavailability(chiropractorId = chiropractorId))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching chiropractor unavailability", e)
            Result.failure(Exception("Failed to fetch unavailability data: ${e.message}"))
        }
    }
    
    /**
     * Check if a date is available for booking
     * I-check kung available ang date para sa booking
     */
    fun isDateAvailable(unavailability: ChiropractorUnavailability?, date: String): Boolean {
        return unavailability?.isDateFullyUnavailable(date) != true
    }
    
    /**
     * Check if a time slot is available for booking
     * I-check kung available ang time slot para sa booking
     */
    fun isTimeSlotAvailable(unavailability: ChiropractorUnavailability?, date: String, time: String): Boolean {
        if (unavailability == null) return true
        
        // Convert time format from "10:00 AM" to "10:00" (24-hour format)
        val time24Hour = convertTo24HourFormat(time)
        return !unavailability.isTimeUnavailable(date, time24Hour)
    }
    
    /**
     * Convert time from 24-hour format to 12-hour format
     * I-convert ang time mula 24-hour format patungo sa 12-hour format
     */
    private fun convertFrom24HourFormat(time24Hour: String): String {
        return try {
            val parts = time24Hour.split(":")
            if (parts.size != 2) return time24Hour
            
            val hour = parts[0].toInt()
            val minute = parts[1]
            
            when {
                hour == 0 -> "12:$minute AM"
                hour < 12 -> "$hour:$minute AM"
                hour == 12 -> "12:$minute PM"
                else -> "${hour - 12}:$minute PM"
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error converting time from 24-hour format: $time24Hour", e)
            time24Hour
        }
    }
    
    /**
     * Convert time from 12-hour format to 24-hour format
     * I-convert ang time mula 12-hour format patungo sa 24-hour format
     */
    private fun convertTo24HourFormat(time12Hour: String): String {
        return try {
            val parts = time12Hour.split(" ")
            if (parts.size != 2) return time12Hour
            
            val timePart = parts[0]
            val amPm = parts[1].uppercase()
            val timeParts = timePart.split(":")
            
            if (timeParts.size != 2) return time12Hour
            
            var hour = timeParts[0].toInt()
            val minute = timeParts[1]
            
            when (amPm) {
                "AM" -> {
                    if (hour == 12) hour = 0
                }
                "PM" -> {
                    if (hour != 12) hour += 12
                }
            }
            
            String.format("%02d:%s", hour, minute)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting time format: $time12Hour", e)
            time12Hour
        }
    }
    
    /**
     * Check if a time slot is in the past for the given date
     * I-check kung nakaraan na ang time slot para sa given date
     */
    private fun isTimeSlotInPast(timeSlot: String, selectedDate: Date): Boolean {
        return try {
            val currentDate = Calendar.getInstance()
            val selectedCalendar = Calendar.getInstance().apply { time = selectedDate }
            
            // Only filter past times if the selected date is today
            if (!isSameDay(currentDate, selectedCalendar)) {
                return false
            }
            
            // Convert time slot to 24-hour format for comparison
            val time24Hour = convertTo24HourFormat(timeSlot)
            val timeParts = time24Hour.split(":")
            if (timeParts.size != 2) return false
            
            val slotHour = timeParts[0].toInt()
            val slotMinute = timeParts[1].toInt()
            
            // Create calendar for the time slot
            val slotCalendar = Calendar.getInstance().apply {
                time = selectedDate
                set(Calendar.HOUR_OF_DAY, slotHour)
                set(Calendar.MINUTE, slotMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            // Check if the slot time has passed
            val isPast = slotCalendar.before(currentDate)
            
            if (isPast) {
                Log.d(TAG, "Time slot $timeSlot is in the past for today")
            }
            
            isPast
        } catch (e: Exception) {
            Log.w(TAG, "Error checking if time slot is in past: $timeSlot", e)
            false // If error occurs, don't filter the slot
        }
    }
    
    /**
     * Check if two calendars represent the same day
     * I-check kung pareho ang araw ng dalawang calendar
     */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Enrich appointments with chiropractor information
     * I-enrich ang appointments ng chiropractor information
     */
    private suspend fun enrichAppointmentsWithChiropractorInfo(appointments: List<Appointment>): List<Appointment> {
        return try {
            Log.d(TAG, "Starting enrichment for ${appointments.size} appointments")
            
            // Debug: Log appointment structure
            appointments.forEachIndexed { index, appointment ->
                Log.d(TAG, "Appointment $index: id=${appointment.id}, chiroId='${appointment.chiroId}', chiropractorId='${appointment.chiropractorId}', existing name='${appointment.chiropractorName}'")
            }
            
            // Get unique chiropractor IDs
            val chiropractorIds = appointments.mapNotNull { 
                when {
                    it.chiroId.isNotEmpty() -> it.chiroId
                    it.chiropractorId.isNotEmpty() -> it.chiropractorId
                    else -> {
                        Log.w(TAG, "Appointment ${it.id} has no chiropractor ID")
                        null
                    }
                }
            }.distinct()
            
            Log.d(TAG, "Found ${chiropractorIds.size} unique chiropractor IDs: $chiropractorIds")
            
            if (chiropractorIds.isEmpty()) {
                Log.w(TAG, "No chiropractor IDs found in any appointments - trying to get default chiropractor")
                val defaultChiropractor = getDefaultChiropractor()
                
                return appointments.map { appointment ->
                    if (appointment.chiropractorName.isEmpty()) {
                        appointment.copy(
                            chiropractorName = defaultChiropractor?.first ?: "Doctor",
                            chiropractorSpecialization = defaultChiropractor?.second ?: "General Practice"
                        )
                    } else {
                        appointment
                    }
                }
            }
            
            Log.d(TAG, "Fetching chiropractor info for ${chiropractorIds.size} chiropractors")
            
            // Fetch chiropractor information in batches (Firestore 'in' query limit is 10)
            val chiropractorInfoMap = mutableMapOf<String, Pair<String, String>>()
            
            // Fetch chiropractor information individually (more reliable than batch queries)
            chiropractorIds.forEach { chiropractorId ->
                try {
                    Log.d(TAG, "Fetching chiropractor info for ID: $chiropractorId")
                    val chiropractorDoc = firestore.collection(COLLECTION_CHIROPRACTORS)
                        .document(chiropractorId)
                        .get()
                        .await()
                    
                    if (chiropractorDoc.exists()) {
                        val data = chiropractorDoc.data!!
                        val name = buildChiropractorName(data)
                        val specialization = data["specialization"] as? String ?: "General Practice"
                        chiropractorInfoMap[chiropractorId] = Pair(name, specialization)
                        Log.d(TAG, "Found chiropractor: $name ($specialization)")
                    } else {
                        Log.w(TAG, "Chiropractor document not found for ID: $chiropractorId")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error fetching chiropractor ID: $chiropractorId", e)
                }
            }
            
            Log.d(TAG, "Successfully fetched info for ${chiropractorInfoMap.size} chiropractors")
            
            // Enrich appointments with chiropractor info
            appointments.map { appointment ->
                val chiroId = if (appointment.chiroId.isNotEmpty()) appointment.chiroId else appointment.chiropractorId
                val chiropractorInfo = chiropractorInfoMap[chiroId]
                
                if (chiropractorInfo != null) {
                    Log.d(TAG, "Enriching appointment ${appointment.id} with chiropractor: ${chiropractorInfo.first}")
                    appointment.copy(
                        chiropractorName = chiropractorInfo.first,
                        chiropractorSpecialization = chiropractorInfo.second
                    )
                } else {
                    // Keep original appointment if chiropractor info not found, but log the issue
                    Log.w(TAG, "Chiropractor info not found for ID: $chiroId in appointment ${appointment.id}")
                    Log.d(TAG, "Available chiropractor IDs: ${chiropractorInfoMap.keys}")
                    
                    // If appointment already has chiropractor name, keep it
                    if (appointment.chiropractorName.isNotEmpty()) {
                        Log.d(TAG, "Using existing chiropractor name: ${appointment.chiropractorName}")
                        appointment
                    } else {
                        // Set default values
                        appointment.copy(
                            chiropractorName = "Unknown Doctor",
                            chiropractorSpecialization = "General Practice"
                        )
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error enriching appointments with chiropractor info", e)
            // Return original appointments if enrichment fails
            appointments
        }
    }
    
    /**
     * Build chiropractor full name from Firestore data
     * Gumawa ng full name ng chiropractor mula sa Firestore data
     */
    private fun buildChiropractorName(data: Map<String, Any>): String {
        val firstName = data["firstName"] as? String ?: ""
        val middleName = data["middleName"] as? String ?: ""
        val lastName = data["lastName"] as? String ?: ""
        val suffix = data["suffix"] as? String ?: ""
        
        return buildString {
            append(firstName)
            if (middleName.isNotBlank()) append(" $middleName")
            if (lastName.isNotBlank()) append(" $lastName")
            if (suffix.isNotBlank()) append(" $suffix")
        }.trim().ifBlank { 
            data["name"] as? String ?: "Unknown Doctor" 
        }
    }

    /**
     * Get a default chiropractor for appointments without chiropractor ID
     * Kumuha ng default chiropractor para sa appointments na walang chiropractor ID
     */
    private suspend fun getDefaultChiropractor(): Pair<String, String>? {
        return try {
            Log.d(TAG, "Fetching default chiropractor from collection")
            val chiropractorSnapshot = firestore.collection(COLLECTION_CHIROPRACTORS)
                .limit(1)
                .get()
                .await()
            
            if (chiropractorSnapshot.documents.isNotEmpty()) {
                val doc = chiropractorSnapshot.documents.first()
                val data = doc.data!!
                val name = buildChiropractorName(data)
                val specialization = data["specialization"] as? String ?: "General Practice"
                Log.d(TAG, "Using default chiropractor: $name")
                Pair(name, specialization)
            } else {
                Log.w(TAG, "No chiropractors found in collection")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching default chiropractor", e)
            null
        }
    }

    /**
     * Validate profile for booking
     * I-validate ang profile para sa booking
     */
    suspend fun validateProfileForBooking(): Result<ProfileValidationResult> {
        return try {
            val validation = profileValidationService.validateProfileForBooking()
            Result.success(validation)
        } catch (e: Exception) {
            Log.e(TAG, "Error validating profile for booking", e)
            Result.failure(Exception("Failed to validate profile: ${e.message}"))
        }
    }
}

