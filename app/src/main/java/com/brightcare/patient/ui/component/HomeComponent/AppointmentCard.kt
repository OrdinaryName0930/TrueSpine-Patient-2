package com.brightcare.patient.ui.component.HomeComponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brightcare.patient.data.model.Appointment
import com.brightcare.patient.data.model.AppointmentType
import com.brightcare.patient.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card component for displaying appointment information
 */
@Composable
fun AppointmentCard(
    appointment: Appointment,
    onCardClick: () -> Unit = {},
    onCallClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Blue500
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onCardClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Appointment info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Chiropractor name
                Text(
                    text = appointment.chiropractorName.ifEmpty { "Dr. ${appointment.chiroId}" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Specialization or appointment type
                Text(
                    text = if (appointment.chiropractorSpecialization.isNotEmpty()) {
                        appointment.chiropractorSpecialization
                    } else {
                        appointment.appointmentType.displayName
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Date row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDate(appointment.date),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Time row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTime(appointment.time),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                }
            }
            
            // Right side - Status and call button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status badge
                StatusBadge(status = appointment.status)
                
                // Call button
                IconButton(
                    onClick = onCallClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Status badge component
 */
@Composable
private fun StatusBadge(status: String) {
    val (backgroundColor, textColor, text) = when (status.lowercase()) {
        "confirmed", "approved" -> Triple(Green500, Color.White, "Confirmed")
        "pending" -> Triple(Orange500, Color.White, "Pending")
        "cancelled" -> Triple(Red500, Color.White, "Cancelled")
        "completed" -> Triple(Gray600, Color.White, "Completed")
        else -> Triple(Gray600, Color.White, status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
        )
    }
}


/**
 * Format date for display
 */
private fun formatDate(date: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        
        val parsedDate = dateFormat.parse(date)
        if (parsedDate != null) {
            displayDateFormat.format(parsedDate)
        } else date
    } catch (e: Exception) {
        date
    }
}

/**
 * Format time for display
 */
private fun formatTime(time: String): String {
    return try {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val displayTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        
        val parsedTime = timeFormat.parse(time)
        if (parsedTime != null) {
            displayTimeFormat.format(parsedTime)
        } else time
    } catch (e: Exception) {
        time
    }
}

@Preview(showBackground = true)
@Composable
fun AppointmentCardPreview() {
    BrightCarePatientTheme {
        AppointmentCard(
            appointment = Appointment(
                id = "1",
                chiroId = "chiro1",
                clientId = "client1",
                date = "2025-12-15",
                time = "14:30",
                status = "confirmed",
                chiropractorName = "Dr. Alana Rueter",
                chiropractorSpecialization = "Dentist Consultation",
                appointmentType = AppointmentType.CONSULTATION
            ),
            onCardClick = { },
            onCallClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppointmentCardPendingPreview() {
    BrightCarePatientTheme {
        AppointmentCard(
            appointment = Appointment(
                id = "2",
                chiroId = "chiro2",
                clientId = "client1",
                date = "2025-12-16",
                time = "09:00",
                status = "pending",
                chiropractorName = "Dr. Maria Santos",
                chiropractorSpecialization = "Physical Therapy",
                appointmentType = AppointmentType.TREATMENT
            ),
            onCardClick = { },
            onCallClick = { }
        )
    }
}













