package com.brightcare.patient.data.model

import com.google.firebase.Timestamp
import java.util.Date

/**
 * Notification data model for the app
 * Modelo ng notification para sa app
 */
data class Notification(
    val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientEmail: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val appointmentId: String? = null,
    val service: String? = null,
    val date: String = "",
    val time: String = "",
    val createdAt: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val clientPhone: String? = null
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        clientId = "",
        clientName = "",
        clientEmail = "",
        title = "",
        message = "",
        type = NotificationType.GENERAL,
        appointmentId = null,
        service = null,
        date = "",
        time = "",
        createdAt = "",
        timestamp = System.currentTimeMillis(),
        isRead = false,
        clientPhone = null
    )

    /**
     * Convert to map for Firestore
     * I-convert sa map para sa Firestore
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "clientId" to clientId,
            "clientName" to clientName,
            "clientEmail" to clientEmail,
            "title" to title,
            "message" to message,
            "type" to type.value,
            "appointmentId" to (appointmentId ?: ""),
            "service" to (service ?: ""),
            "date" to date,
            "time" to time,
            "createdAt" to createdAt,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "clientPhone" to (clientPhone ?: "")
        )
    }

    companion object {
        /**
         * Create from Firestore document data
         * Gumawa mula sa Firestore document data
         */
        fun fromMap(id: String, data: Map<String, Any>): Notification {
            return Notification(
                id = id,
                clientId = data["clientId"] as? String ?: "",
                clientName = data["clientName"] as? String ?: "",
                clientEmail = data["clientEmail"] as? String ?: "",
                title = data["title"] as? String ?: "",
                message = data["message"] as? String ?: "",
                type = NotificationType.fromString(data["type"] as? String ?: "general"),
                appointmentId = data["appointmentId"] as? String,
                service = data["service"] as? String,
                date = data["date"] as? String ?: "",
                time = data["time"] as? String ?: "",
                createdAt = data["createdAt"] as? String ?: "",
                timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis(),
                isRead = data["isRead"] as? Boolean ?: false,
                clientPhone = data["clientPhone"] as? String
            )
        }
    }
}

/**
 * Notification types
 * Mga uri ng notification
 */
enum class NotificationType(val value: String, val displayName: String) {
    GENERAL("general", "General"),
    NEW_BOOKING("new_booking", "New Booking"),
    APPOINTMENT_CONFIRMED("appointment_confirmed", "Appointment Confirmed"),
    APPOINTMENT_CANCELLED("appointment_cancelled", "Appointment Cancelled"),
    APPOINTMENT_REMINDER("appointment_reminder", "Appointment Reminder"),
    MESSAGE_RECEIVED("message_received", "New Message"),
    PROFILE_UPDATE("profile_update", "Profile Update");

    companion object {
        fun fromString(value: String): NotificationType {
            return values().find { it.value == value } ?: GENERAL
        }
    }
}

/**
 * UI state for notifications
 * UI state para sa notifications
 */
data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val unreadCount: Int = 0
)






