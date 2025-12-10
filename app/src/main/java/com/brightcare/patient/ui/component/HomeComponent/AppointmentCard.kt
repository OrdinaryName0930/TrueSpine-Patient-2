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
 * Card component para sa pagpapakita ng appointment information
 */
@Composable
fun AppointmentCard(
    appointment: Appointment,
    onCardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (appointment.status) {
                "confirmed", "approved" -> Green50
                "pending" -> Blue50
                "cancelled" -> Red50
                else -> Gray50
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onCardClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Specialization
                if (appointment.chiropractorSpecialization.isNotEmpty()) {
                    Text(
                        text = appointment.chiropractorSpecialization,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Gray600,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Date and time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = Blue600,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDateTime(appointment.date, appointment.time),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Gray700,
                            fontSize = 11.sp
                        )
                    )
                }
            }
            
            // Right side - Status and type
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Status badge
                StatusBadge(status = appointment.status)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Appointment type icon
                AppointmentTypeIcon(type = appointment.appointmentType)
            }
        }
    }
}

/**
 * Status badge component
 * Status badge component
 */
@Composable
private fun StatusBadge(status: String) {
    val (backgroundColor, textColor, text) = when (status.lowercase()) {
        "confirmed", "approved" -> Triple(Green100, Green800, "Confirmed")
        "pending" -> Triple(Blue100, Blue800, "Pending")
        "cancelled" -> Triple(Red100, Red800, "Cancelled")
        "completed" -> Triple(Gray100, Gray800, "Completed")
        else -> Triple(Gray100, Gray800, status.capitalize())
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp
            )
        )
    }
}

/**
 * Appointment type icon
 * Appointment type icon
 */
@Composable
private fun AppointmentTypeIcon(type: AppointmentType) {
    val (icon, tint) = when (type) {
        AppointmentType.CONSULTATION -> Icons.Default.Chat to Blue600
        AppointmentType.TREATMENT -> Icons.Default.Healing to Green600
        AppointmentType.FOLLOW_UP -> Icons.Default.Refresh to Orange600
        AppointmentType.THERAPY -> Icons.Default.FitnessCenter to Purple600
        AppointmentType.ADJUSTMENT -> Icons.Default.Build to Teal600
        AppointmentType.ASSESSMENT -> Icons.Default.Assessment to Indigo600
    }
    
    Icon(
        imageVector = icon,
        contentDescription = type.displayName,
        tint = tint,
        modifier = Modifier.size(16.dp)
    )
}

/**
 * Format date and time for display
 * I-format ang date at time para sa display
 */
private fun formatDateTime(date: String, time: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val displayTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        
        val parsedDate = dateFormat.parse(date)
        val parsedTime = timeFormat.parse(time)
        
        val formattedDate = if (parsedDate != null) {
            displayDateFormat.format(parsedDate)
        } else date
        
        val formattedTime = if (parsedTime != null) {
            displayTimeFormat.format(parsedTime)
        } else time
        
        "$formattedDate at $formattedTime"
    } catch (e: Exception) {
        "$date at $time"
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
                date = "2025-12-08",
                time = "14:30",
                status = "confirmed",
                chiropractorName = "Dr. Maria Santos",
                chiropractorSpecialization = "Spinal Adjustment Specialist",
                appointmentType = AppointmentType.CONSULTATION
            )
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
                date = "2025-12-09",
                time = "10:00",
                status = "pending",
                chiropractorName = "Dr. Juan Dela Cruz",
                chiropractorSpecialization = "Physical Therapy",
                appointmentType = AppointmentType.TREATMENT
            )
        )
    }
}






