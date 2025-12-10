package com.brightcare.patient.ui.component.HomeComponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brightcare.patient.data.model.Notification
import com.brightcare.patient.data.model.NotificationType
import com.brightcare.patient.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card component for displaying notification information
 * Card component para sa pagpapakita ng notification information
 */
@Composable
fun NotificationCard(
    notification: Notification,
    onCardClick: () -> Unit = {},
    onMarkAsRead: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Gray50 else Blue50
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {
            if (!notification.isRead) {
                onMarkAsRead()
            }
            onCardClick()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left side - Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getNotificationIconBackground(notification.type)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = notification.type.displayName,
                    tint = getNotificationIconColor(notification.type),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Middle - Notification content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        color = if (notification.isRead) Gray700 else Gray900
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Message
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (notification.isRead) Gray600 else Gray700,
                        fontSize = 12.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Time
                Text(
                    text = formatNotificationTime(notification.createdAt, notification.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Gray500,
                        fontSize = 10.sp
                    )
                )
            }
            
            // Right side - Unread indicator
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Blue600)
                )
            }
        }
    }
}

/**
 * Get notification icon based on type
 * Kumuha ng notification icon base sa type
 */
private fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.NEW_BOOKING -> Icons.Default.EventAvailable
        NotificationType.APPOINTMENT_CONFIRMED -> Icons.Default.CheckCircle
        NotificationType.APPOINTMENT_CANCELLED -> Icons.Default.Cancel
        NotificationType.APPOINTMENT_REMINDER -> Icons.Default.Alarm
        NotificationType.MESSAGE_RECEIVED -> Icons.Default.Message
        NotificationType.PROFILE_UPDATE -> Icons.Default.Person
        NotificationType.GENERAL -> Icons.Default.Notifications
    }
}

/**
 * Get notification icon background color
 * Kumuha ng notification icon background color
 */
private fun getNotificationIconBackground(type: NotificationType): Color {
    return when (type) {
        NotificationType.NEW_BOOKING -> Blue100
        NotificationType.APPOINTMENT_CONFIRMED -> Green100
        NotificationType.APPOINTMENT_CANCELLED -> Red100
        NotificationType.APPOINTMENT_REMINDER -> Orange100
        NotificationType.MESSAGE_RECEIVED -> Purple100
        NotificationType.PROFILE_UPDATE -> Teal100
        NotificationType.GENERAL -> Gray100
    }
}

/**
 * Get notification icon color
 * Kumuha ng notification icon color
 */
private fun getNotificationIconColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.NEW_BOOKING -> Blue600
        NotificationType.APPOINTMENT_CONFIRMED -> Green600
        NotificationType.APPOINTMENT_CANCELLED -> Red600
        NotificationType.APPOINTMENT_REMINDER -> Orange600
        NotificationType.MESSAGE_RECEIVED -> Purple600
        NotificationType.PROFILE_UPDATE -> Teal600
        NotificationType.GENERAL -> Gray600
    }
}

/**
 * Format notification time for display
 * I-format ang notification time para sa display
 */
private fun formatNotificationTime(createdAt: String, timestamp: Long): String {
    return try {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d ago"
            else -> {
                val date = Date(timestamp)
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
            }
        }
    } catch (e: Exception) {
        createdAt
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationCardPreview() {
    BrightCarePatientTheme {
        NotificationCard(
            notification = Notification(
                id = "1",
                title = "New Appointment Booking",
                message = "Your appointment with Dr. Maria Santos has been confirmed for December 8, 2025 at 2:30 PM",
                type = NotificationType.APPOINTMENT_CONFIRMED,
                createdAt = "2025-12-08 14:30:00",
                timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                isRead = false
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationCardReadPreview() {
    BrightCarePatientTheme {
        NotificationCard(
            notification = Notification(
                id = "2",
                title = "Appointment Reminder",
                message = "Don't forget your appointment tomorrow at 10:00 AM with Dr. Juan Dela Cruz",
                type = NotificationType.APPOINTMENT_REMINDER,
                createdAt = "2025-12-07 18:00:00",
                timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
                isRead = true
            )
        )
    }
}






