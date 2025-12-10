package com.brightcare.patient.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.PatientSignInViewModel
import com.brightcare.patient.ui.viewmodel.HomeViewModel
import com.brightcare.patient.ui.component.HomeComponent.HomeHeader
import com.brightcare.patient.ui.component.HomeComponent.AppointmentCard
import com.brightcare.patient.ui.component.HomeComponent.NotificationCard

/**
 * Home screen - main dashboard after successful login and profile completion
 * Pangunahing screen pagkatapos ng matagumpay na login at profile completion
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    signInViewModel: PatientSignInViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    // Make system navigation bar white with dark icons
    SetWhiteSystemNavBar()

    // Collect UI state
    val uiState by homeViewModel.uiState.collectAsState()
    val userFirstName by homeViewModel.userFirstName.collectAsState()
    val todaysAppointments by homeViewModel.todaysAppointments.collectAsState()
    val upcomingAppointments by homeViewModel.upcomingAppointments.collectAsState()
    val notifications by homeViewModel.notifications.collectAsState()
    val unreadCount by homeViewModel.unreadNotificationsCount.collectAsState()
    val isLoadingAppointments by homeViewModel.isLoadingAppointments.collectAsState()
    val isLoadingNotifications by homeViewModel.isLoadingNotifications.collectAsState()

    // Pull to refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            homeViewModel.refreshData()
        }
    )

    // Handle refresh completion
    LaunchedEffect(isLoadingAppointments, isLoadingNotifications) {
        if (!isLoadingAppointments && !isLoadingNotifications && isRefreshing) {
            isRefreshing = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            // Header with user name and notification count
            HomeHeader(
                firstName = userFirstName,
                unreadCount = unreadCount,
                onNotificationClick = {
                    navController.navigate("notifications")
                }
            )
            
            // Main content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
            // Today's Schedule Section
            SectionHeader(
                title = "Today's Schedule",
                subtitle = "Mga appointment ngayong araw",
                onViewAllClick = { 
                    navController.navigate("booking")
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoadingAppointments) {
                LoadingCard("Loading today's appointments...")
            } else if (todaysAppointments.isEmpty()) {
                EmptyStateCard(
                    title = "No appointments today",
                    subtitle = "Walang appointment ngayong araw",
                    icon = Icons.Default.EventAvailable
                )
            } else {
                todaysAppointments.forEach { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onCardClick = {
                            // Navigate to appointment details
                            navController.navigate("booking")
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Upcoming Schedule Section
            SectionHeader(
                title = "Upcoming Schedule",
                subtitle = "Mga susunod na appointment",
                onViewAllClick = { 
                    navController.navigate("booking")
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoadingAppointments) {
                LoadingCard("Loading upcoming appointments...")
            } else if (upcomingAppointments.isEmpty()) {
                EmptyStateCard(
                    title = "No upcoming appointments",
                    subtitle = "Walang susunod na appointment",
                    icon = Icons.Default.Schedule
                )
            } else {
                upcomingAppointments.take(3).forEach { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onCardClick = {
                            // Navigate to appointment details
                            navController.navigate("booking")
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                if (upcomingAppointments.size > 3) {
                    TextButton(
                        onClick = { navController.navigate("booking") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View ${upcomingAppointments.size - 3} more appointments")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recent Notifications Section
            SectionHeader(
                title = "Recent Notifications",
                subtitle = "Mga kamakailang notification",
                onViewAllClick = { 
                    navController.navigate("booking")
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoadingNotifications) {
                LoadingCard("Loading notifications...")
            } else if (notifications.isEmpty()) {
                EmptyStateCard(
                    title = "No notifications",
                    subtitle = "Walang notification",
                    icon = Icons.Default.Notifications
                )
            } else {
                notifications.forEach { notification ->
                    NotificationCard(
                        notification = notification,
                        onCardClick = {
                            // Handle notification click
                        },
                        onMarkAsRead = {
                            homeViewModel.markNotificationAsRead(notification.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        // Pull refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = White,
            contentColor = Blue500
        )
    }
}

/**
 * Section header component
 * Section header component
 */
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    onViewAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Gray600,
                    fontSize = 11.sp
                )
            )
        }
        
        TextButton(onClick = onViewAllClick) {
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Blue600
                )
            )
        }
    }
}

/**
 * Loading card component
 * Loading card component
 */
@Composable
private fun LoadingCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray50),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Blue600
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gray600
                )
            )
        }
    }
}

/**
 * Empty state card component
 * Empty state card component
 */
@Composable
private fun EmptyStateCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray50),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Gray400,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = Gray700
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Gray500,
                    fontSize = 11.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Sets the system navigation bar (Back/Home/Overview) to white with dark icons
 */
@Composable
fun SetWhiteSystemNavBar() {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.navigationBarColor = android.graphics.Color.WHITE
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightNavigationBars = true
        }
    }
}

@Preview(
    showBackground = true,
    name = "Home Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun HomeScreenPreview() {
    BrightCarePatientTheme {
        HomeScreen(
            navController = rememberNavController()
        )
    }
}
