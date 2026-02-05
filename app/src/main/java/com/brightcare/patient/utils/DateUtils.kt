package com.brightcare.patient.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Date utilities for appointment grouping and formatting
 */
object DateUtils {
    
    /**
     * Get date title for appointment grouping
     */
    fun getDateTitle(dateString: String): String {
        return try {
            val appointmentDate = parseAppointmentDate(dateString)
            val today = Calendar.getInstance()
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            
            val appointmentCalendar = Calendar.getInstance().apply { time = appointmentDate }
            
            when {
                isSameDay(appointmentCalendar, today) -> "Today"
                isSameDay(appointmentCalendar, tomorrow) -> "Tomorrow"
                isSameDay(appointmentCalendar, yesterday) -> "Yesterday"
                else -> {
                    // Format as "December 15, 2025"
                    SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(appointmentDate)
                }
            }
        } catch (e: Exception) {
            // Fallback to original date string if parsing fails
            dateString
        }
    }
    
    /**
     * Parse appointment date from various formats
     */
    private fun parseAppointmentDate(dateString: String): Date {
        val formats = listOf(
            "yyyy-MM-dd",           // 2025-12-15
            "dd MMMM yyyy",         // 15 December 2025
            "MMM dd, yyyy",         // Dec 15, 2025
            "MMMM dd, yyyy"         // December 15, 2025
        )
        
        for (format in formats) {
            try {
                return SimpleDateFormat(format, Locale.getDefault()).parse(dateString) ?: throw Exception()
            } catch (e: Exception) {
                continue
            }
        }
        
        throw Exception("Unable to parse date: $dateString")
    }
    
    /**
     * Check if two calendars represent the same day
     */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Group appointments by date with proper titles
     */
    fun <T> groupAppointmentsByDate(
        appointments: List<T>,
        dateExtractor: (T) -> String
    ): List<DateGroup<T>> {
        val grouped = appointments.groupBy { appointment ->
            val dateString = dateExtractor(appointment)
            val dateTitle = getDateTitle(dateString)
            val sortKey = getSortKey(dateString)
            DateGroupKey(dateTitle, sortKey)
        }
        
        return grouped.map { (key, appointments) ->
            DateGroup(
                title = key.title,
                appointments = appointments,
                sortKey = key.sortKey
            )
        }.sortedBy { it.sortKey }
    }
    
    /**
     * Get sort key for proper date ordering
     */
    private fun getSortKey(dateString: String): Long {
        return try {
            parseAppointmentDate(dateString).time
        } catch (e: Exception) {
            Long.MAX_VALUE // Put unparseable dates at the end
        }
    }
}

/**
 * Data class for date group key
 */
private data class DateGroupKey(
    val title: String,
    val sortKey: Long
)

/**
 * Data class for grouped appointments by date
 */
data class DateGroup<T>(
    val title: String,
    val appointments: List<T>,
    val sortKey: Long
)
