package com.brightcare.patient.ui.screens

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.PatientSignInViewModel
import com.brightcare.patient.ui.viewmodel.HomeViewModel
import com.brightcare.patient.ui.component.HomeComponent.HomeHeader
import com.brightcare.patient.ui.component.HomeComponent.AppointmentCard

/**
 * Home screen - main dashboard after successful login and profile completion
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    signInViewModel: PatientSignInViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    onNavigateToBooking: () -> Unit = {},
    onNavigateToTab: (String) -> Unit = {}
) {
    // Make system navigation bar white with dark icons
    SetWhiteSystemNavBar()
    
    // Get context for phone calls
    val context = LocalContext.current
    
    // State to store chiropractor ID for pending call
    var pendingCallChiropractorId by remember { mutableStateOf<String?>(null) }
    
    // Permission launcher for phone calls
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && pendingCallChiropractorId != null) {
            // Permission granted, make the call
            homeViewModel.makePhoneCall(context, pendingCallChiropractorId!!)
            pendingCallChiropractorId = null
        }
    }

    // Collect UI state
    val uiState by homeViewModel.uiState.collectAsState()
    val userFirstName by homeViewModel.userFirstName.collectAsState()
    val userProfilePictureUrl by homeViewModel.userProfilePictureUrl.collectAsState()
    val todaysAppointments by homeViewModel.todaysAppointments.collectAsState()
    val upcomingAppointments by homeViewModel.upcomingAppointments.collectAsState()
    val unreadCount by homeViewModel.unreadNotificationsCount.collectAsState()
    val isLoadingAppointments by homeViewModel.isLoadingAppointments.collectAsState()

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
    LaunchedEffect(isLoadingAppointments) {
        if (!isLoadingAppointments && isRefreshing) {
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
            // Header with user name, profile picture and notification count
            HomeHeader(
                firstName = userFirstName,
                profilePictureUrl = userProfilePictureUrl,
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
                subtitle = "Your appointments for today",
                onViewAllClick = onNavigateToBooking
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoadingAppointments) {
                LoadingCard("Loading today's appointments...")
            } else if (todaysAppointments.isEmpty()) {
                EmptyStateCard(
                    title = "No appointments today",
                    subtitle = "You have no scheduled appointments for today",
                    icon = Icons.Default.EventAvailable
                )
            } else {
                todaysAppointments.forEach { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onCardClick = {
                            // Navigate to appointment details
                            navController.navigate("appointment_details/${appointment.id}")
                        },
                        onCallClick = {
                            // Handle call functionality with permission check
                            handlePhoneCall(
                                context = context,
                                chiropractorId = appointment.chiroId,
                                callPermissionLauncher = callPermissionLauncher,
                                homeViewModel = homeViewModel,
                                setPendingCallChiropractorId = { pendingCallChiropractorId = it }
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Upcoming Schedule Section
            SectionHeader(
                title = "Upcoming Schedule",
                subtitle = "Your upcoming appointments",
                onViewAllClick = onNavigateToBooking
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoadingAppointments) {
                LoadingCard("Loading upcoming appointments...")
            } else if (upcomingAppointments.isEmpty()) {
                EmptyStateCard(
                    title = "No upcoming appointments",
                    subtitle = "You have no upcoming appointments scheduled",
                    icon = Icons.Default.Schedule
                )
            } else {
                // Swipable upcoming appointments
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(upcomingAppointments) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            modifier = Modifier.width(320.dp), // Fixed width for swipable cards
                            onCardClick = {
                                // Navigate to appointment details
                                navController.navigate("appointment_details/${appointment.id}")
                            },
                            onCallClick = {
                                // Handle call functionality with permission check
                                handlePhoneCall(
                                    context = context,
                                    chiropractorId = appointment.chiroId,
                                    callPermissionLauncher = callPermissionLauncher,
                                    homeViewModel = homeViewModel,
                                    setPendingCallChiropractorId = { pendingCallChiropractorId = it }
                                )
                            }
                        )
                    }
                }
                
                // Show appointment count if there are multiple appointments
                if (upcomingAppointments.size > 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${upcomingAppointments.size} upcoming appointments",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Gray600,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Promotional Card Section
            PromotionalCard(
                onBookNowClick = {
                    // Navigate to chiropractor tab in navigation
                    onNavigateToTab("chiro")
                }
            )
            
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
 * Handle phone call with permission check
 * Hawak ang tawag sa telepono na may permission check
 */
private fun handlePhoneCall(
    context: android.content.Context,
    chiropractorId: String,
    callPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    homeViewModel: HomeViewModel,
    setPendingCallChiropractorId: (String?) -> Unit
) {
    when (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE)) {
        PackageManager.PERMISSION_GRANTED -> {
            // Permission already granted, make the call
            homeViewModel.makePhoneCall(context, chiropractorId)
        }
        else -> {
            // Store chiropractor ID and request permission
            setPendingCallChiropractorId(chiropractorId)
            callPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
        }
    }
}

/**
 * Promotional card component for whole body chiropractic service
 * Promotional card component para sa whole body chiropractic service
 */
@Composable
private fun PromotionalCard(
    onBookNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Blue500
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(Blue500, Blue600)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Service info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Service title
                    Text(
                        text = "Whole Body Chiropractic",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Service description
                    Text(
                        text = "Complete body alignment & wellness therapy",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Price
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "₱",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "3,499",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Book now button
                    Button(
                        onClick = onBookNowClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Blue500
                        ),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = "Book Now",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Book Now",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Right side - Decorative icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Color.White.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Healing,
                            contentDescription = "Chiropractic Service",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
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
            navController = rememberNavController(),
            onNavigateToBooking = { }
        )
    }
}

