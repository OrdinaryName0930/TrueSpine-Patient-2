package com.brightcare.patient.ui.component.HomeComponent

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brightcare.patient.ui.theme.*

/**
 * Home header component with welcome message and notification icon
 * Header component na may welcome message at notification icon
 * Based on ChiroHeader design pattern
 */
@Composable
fun HomeHeader(
    firstName: String?,
    unreadCount: Int = 0,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Welcome message - following ChiroHeader pattern
        Column {
            Text(
                text = "Welcome ${firstName ?: "User"}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Blue500,
                    fontSize = 28.sp
                )
            )
            Text(
                text = "Good to see you back!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gray600
                )
            )
        }
        
        // Notification icon button
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(
                        containerColor = Red500,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Blue600,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeHeaderPreview() {
    BrightCarePatientTheme {
        HomeHeader(
            firstName = "Juan",
            unreadCount = 3,
            onNotificationClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeHeaderLongNamePreview() {
    BrightCarePatientTheme {
        HomeHeader(
            firstName = "Maria Cristina",
            onNotificationClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeHeaderNoNamePreview() {
    BrightCarePatientTheme {
        HomeHeader(
            firstName = null,
            onNotificationClick = { }
        )
    }
}
