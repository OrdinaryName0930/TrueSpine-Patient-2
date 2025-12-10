package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.component.HomeComponent.NotificationCard
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.HomeViewModel

/**
 * Notification screen showing all user notifications
 * Screen ng notification na nagpapakita ng lahat ng notification ng user
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    // Collect state
    val notifications by homeViewModel.notifications.collectAsState()
    val isLoading by homeViewModel.isLoadingNotifications.collectAsState()
    val unreadCount by homeViewModel.unreadNotificationsCount.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Gray900
                        )
                    )
                    if (unreadCount > 0) {
                        Text(
                            text = "$unreadCount unread notifications",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray600,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Gray700
                    )
                }
            },
            actions = {
                if (unreadCount > 0) {
                    TextButton(
                        onClick = { homeViewModel.markAllNotificationsAsRead() }
                    ) {
                        Text(
                            text = "Mark all read",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Blue600
                            )
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = WhiteBg,
                titleContentColor = Gray900
            )
        )

        // Content
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Blue600,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading notifications...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Gray600
                        )
                    )
                }
            }
        } else if (notifications.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "No notifications",
                        tint = Gray400,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No notifications yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Gray700
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You'll see notifications about your appointments and messages here.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Gray500
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Notifications list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onCardClick = {
                            // Handle notification click based on type
                            when (notification.type.value) {
                                "new_booking", "appointment_confirmed", "appointment_cancelled", "appointment_reminder" -> {
                                    // Navigate to appointments/booking screen
                                    navController.navigate("booking")
                                }
                                "message_received" -> {
                                    // Navigate to messages screen
                                    navController.navigate("message")
                                }
                                "profile_update" -> {
                                    // Navigate to profile screen
                                    navController.navigate("profile")
                                }
                                else -> {
                                    // General notification - no specific action
                                }
                            }
                        },
                        onMarkAsRead = {
                            homeViewModel.markNotificationAsRead(notification.id)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    BrightCarePatientTheme {
        NotificationScreen(
            navController = rememberNavController()
        )
    }
}






