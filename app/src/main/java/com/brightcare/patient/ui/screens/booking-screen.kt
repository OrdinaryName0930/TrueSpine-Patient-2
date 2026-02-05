package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.brightcare.patient.data.model.*
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.BookingViewModel
import com.brightcare.patient.utils.DateUtils
import com.brightcare.patient.ui.component.DateHeader
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * Booking screen - Manage appointments and schedule
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun BookingScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BookingViewModel = hiltViewModel(),
    onShowChiropractorSelection: () -> Unit = { 
        // Show chiropractor selection within booking screen
        // No navigation needed - will be handled internally
    }
) {
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Past", "All")
    var showChiropractorSelection by remember { mutableStateOf(false) }
    var chiropractors by remember { mutableStateOf<List<Chiropractor>>(emptyList()) }
    var isLoadingChiropractors by remember { mutableStateOf(false) }
    var appointmentToCancel by remember { mutableStateOf<Appointment?>(null) }
    var showCancelConfirmation by remember { mutableStateOf(false) }
    var userInitiatedBooking by remember { mutableStateOf(false) }
    
    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            // Clear any existing error messages
            viewModel.clearError()
            // Load appointments
            viewModel.loadUserAppointments()
        }
    )
    
    // Filter appointments based on selected tab
    val filteredAppointments = remember(selectedTab, appointments) {
        when (selectedTab) {
            0 -> appointments.filter { 
                it.status == "pending" || 
                it.status == "approved" ||
                it.status == "booked"
            }
            1 -> appointments.filter { 
                it.status == "completed" || 
                it.status == "cancelled"
            }
            else -> appointments
        }
    }
    
    // Group appointments by date with proper titles
    val groupedAppointments = remember(filteredAppointments) {
        DateUtils.groupAppointmentsByDate(filteredAppointments) { appointment ->
            appointment.date
        }
    }
    
    // Handle refresh state - stop refreshing when data is loaded or error occurs
    LaunchedEffect(uiState.isLoading, uiState.errorMessage, appointments) {
        if (isRefreshing) {
            // Stop refreshing when loading completes (success or error)
            if (!uiState.isLoading) {
                // Add a small delay to show the refresh indicator briefly
                kotlinx.coroutines.delay(300)
                isRefreshing = false
            }
        }
    }
    
    // Handle profile validation when user tries to book
    fun handleBookAppointment() {
        userInitiatedBooking = true
        viewModel.validateProfileForBooking()
    }
    
    // Load chiropractors from Firestore
    fun loadChiropractors() {
        if (chiropractors.isNotEmpty()) return // Already loaded
        
        isLoadingChiropractors = true
        
        // Use coroutine to load chiropractors
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val querySnapshot = firestore.collection("chiropractors")
                    .get()
                    .await()
                
                val loadedChiropractors = querySnapshot.documents.mapNotNull { document ->
                    try {
                        val data = document.data ?: return@mapNotNull null
                        
                        // Build full name from firstName, middleName, lastName, suffix
                        val firstName = data["firstName"] as? String ?: ""
                        val middleName = data["middleName"] as? String ?: ""
                        val lastName = data["lastName"] as? String ?: ""
                        val suffix = data["suffix"] as? String ?: ""
                        
                        val fullName = buildString {
                            append(firstName)
                            if (middleName.isNotBlank()) append(" $middleName")
                            if (lastName.isNotBlank()) append(" $lastName")
                            if (suffix.isNotBlank()) append(" $suffix")
                        }.trim().ifBlank { data["name"] as? String ?: "Unknown Doctor" }
                        
                        // Get real rating and review count from Firestore
                        val realRating = (data["rating"] as? Double) ?: 0.0
                        val realReviewCount = (data["reviewCount"] as? Long)?.toInt() ?: 0
                        
                        Chiropractor(
                            id = document.id,
                            name = fullName,
                            email = data["email"] as? String ?: "",
                            phoneNumber = data["contactNumber"] as? String ?: "",
                            photoUrl = data["profileImageUrl"] as? String,
                            specialization = data["specialization"] as? String ?: "General Practice",
                            licenseNumber = data["prcLicenseNumber"] as? String ?: "",
                            experience = (data["yearsOfExperience"] as? Long)?.toInt() ?: 0,
                            rating = realRating, // Real rating from reviews
                            reviewCount = realReviewCount, // Real review count from reviews
                            isAvailable = true,
                            location = "Philippines",
                            bio = data["about"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        android.util.Log.w("BookingScreen", "Error parsing chiropractor: ${document.id}", e)
                        null
                    }
                }
                
                CoroutineScope(Dispatchers.Main).launch {
                    chiropractors = loadedChiropractors
                    isLoadingChiropractors = false
                }
            } catch (e: Exception) {
                android.util.Log.e("BookingScreen", "Error loading chiropractors", e)
                CoroutineScope(Dispatchers.Main).launch {
                    isLoadingChiropractors = false
                }
            }
        }
    }
    
    // Initialize and load appointments when screen loads (removed automatic profile validation)
    LaunchedEffect(Unit) {
        viewModel.loadUserAppointments()
    }
    
    // Handle successful profile validation - show chiropractor selection only if user initiated booking
    LaunchedEffect(uiState.profileValidation.isValid, userInitiatedBooking) {
        if (uiState.profileValidation.isValid && !uiState.showProfileIncompleteDialog && userInitiatedBooking) {
            // Profile is valid, dialog is not showing, and user clicked the + button
            showChiropractorSelection = true
            loadChiropractors()
            userInitiatedBooking = false // Reset the flag after showing the dialog
        }
    }
    
    // Show error messages
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Error will be displayed in the UI
        }
    }
    
    // Show success messages
    uiState.successMessage?.let { successMessage ->
        LaunchedEffect(successMessage) {
            // Success will be displayed in the UI
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        // Header
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Appointments",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Blue500,
                            fontSize = 28.sp
                        )
                    )
                    Text(
                        text = "Manage your bookings",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Gray600
                        )
                    )
                }
                
                // Book Appointment FAB
                FloatingActionButton(
                    onClick = { 
                        // Always validate profile first when user clicks to book
                        handleBookAppointment()
                    },
                    containerColor = Blue500,
                    contentColor = White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Book Appointment"
                    )
                }
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = WhiteBg,
            contentColor = Blue500,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Blue500
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Content with pull-to-refresh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            if (filteredAppointments.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Gray400
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (selectedTab) {
                            0 -> "No upcoming appointments"
                            1 -> "No past appointments"
                            else -> "No appointments yet"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray600,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Pull down to refresh your appointments",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                groupedAppointments.forEach { dateGroup ->
                    // Date header
                    item(key = "header_${dateGroup.title}") {
                        DateHeader(
                            title = dateGroup.title,
                            modifier = Modifier.padding(top = if (dateGroup == groupedAppointments.first()) 0.dp else 16.dp)
                        )
                    }
                    
                    // Appointments for this date
                    items(
                        items = dateGroup.appointments,
                        key = { appointment -> appointment.id }
                    ) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onCancelClick = {
                                // Show cancel confirmation dialog
                                appointmentToCancel = appointment
                                showCancelConfirmation = true
                            },
                            onViewDetailsClick = {
                                // Navigate to appointment details
                                navController.navigate(NavigationRoutes.appointmentDetails(appointment.id))
                            }
                        )
                    }
                }
                
                // Add bottom padding for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // Pull refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
    
    // Profile Incomplete Dialog
    if (uiState.showProfileIncompleteDialog) {
        ProfileIncompleteDialog(
            profileValidation = uiState.profileValidation,
            onDismiss = { 
                viewModel.hideProfileIncompleteDialog()
                userInitiatedBooking = false // Reset flag when dialog is dismissed
            },
            onNavigateToPersonalDetails = {
                viewModel.hideProfileIncompleteDialog()
                userInitiatedBooking = false // Reset flag when navigating away
                navController.navigate(NavigationRoutes.PERSONAL_DETAILS)
            },
            onNavigateToEmergencyContacts = {
                viewModel.hideProfileIncompleteDialog()
                userInitiatedBooking = false // Reset flag when navigating away
                navController.navigate(NavigationRoutes.EMERGENCY_CONTACTS)
            },
            onProceedToBooking = {
                // Re-validate profile before proceeding - keep userInitiatedBooking true
                viewModel.hideProfileIncompleteDialog()
                viewModel.validateProfileForBooking()
            }
        )
    }
    
    // Error Message Display
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // You can implement a snackbar here if needed
        }
    }
    
    // Success Message Display
    uiState.successMessage?.let { successMessage ->
        LaunchedEffect(successMessage) {
            // You can implement a snackbar here if needed
        }
    }
    
    // Chiropractor Selection Dialog
    if (showChiropractorSelection) {
        ChiropractorSelectionDialog(
            chiropractors = chiropractors,
            isLoading = isLoadingChiropractors,
            onDismiss = { 
                showChiropractorSelection = false
                userInitiatedBooking = false // Reset flag when dialog is dismissed
            },
            onChiropractorSelected = { chiropractor ->
                showChiropractorSelection = false
                userInitiatedBooking = false // Reset flag when chiropractor is selected
                navController.navigate("book_appointment/${chiropractor.id}")
            }
        )
    }
    
    // Cancel Confirmation Dialog
    if (showCancelConfirmation && appointmentToCancel != null) {
        CancelAppointmentDialog(
            appointment = appointmentToCancel!!,
            onDismiss = {
                showCancelConfirmation = false
                appointmentToCancel = null
            },
            onConfirm = { reason ->
                viewModel.cancelAppointment(appointmentToCancel!!.id, reason)
                showCancelConfirmation = false
                appointmentToCancel = null
            }
        )
    }
    }
}

/**
 * Profile Incomplete Dialog
 */
@Composable
private fun ProfileIncompleteDialog(
    profileValidation: ProfileValidationResult,
    onDismiss: () -> Unit,
    onNavigateToPersonalDetails: () -> Unit,
    onNavigateToEmergencyContacts: () -> Unit,
    onProceedToBooking: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Orange500,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Complete Your Profile",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                Text(
                    text = profileValidation.errorMessage ?: "Please complete your profile to book appointments.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show what's missing
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!profileValidation.hasPersonalDetails) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Red500,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Personal Details Required",
                                style = MaterialTheme.typography.bodySmall,
                                color = Red500
                            )
                        }
                    }
                    
                    if (!profileValidation.hasEmergencyContact) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = null,
                                tint = Red500,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Emergency Contact Required",
                                style = MaterialTheme.typography.bodySmall,
                                color = Red500
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (profileValidation.isValid) {
                TextButton(
                    onClick = onProceedToBooking
                ) {
                    Text("Continue Booking")
                }
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!profileValidation.hasPersonalDetails) {
                    TextButton(
                        onClick = onNavigateToPersonalDetails
                    ) {
                        Text("Add Personal Details")
                    }
                }
                
                if (!profileValidation.hasEmergencyContact) {
                    TextButton(
                        onClick = onNavigateToEmergencyContacts
                    ) {
                        Text("Add Emergency Contact")
                    }
                }
                
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

/**
 * Appointment card component
 */
@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onCancelClick: () -> Unit,
    onViewDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Format date to "December 20, 2025" format
    val formattedDate = try {
        // Parse the appointment date (could be in various formats)
        val date = when {
            appointment.date.contains("-") -> {
                // Handle YYYY-MM-DD format
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(appointment.date)
            }
            appointment.date.contains(" ") -> {
                // Handle "21 December 2025" format from TrueSpine4.json
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).parse(appointment.date)
            }
            else -> {
                // Try to parse as is
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(appointment.date)
            }
        }
        
        // Format to desired format: "December 20, 2025"
        date?.let { 
            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(it)
        } ?: appointment.date
    } catch (e: Exception) {
        // If parsing fails, use original date
        appointment.date
    }
    
    // Format time to ensure consistent display
    val formattedTime = try {
        // Handle different time formats
        when {
            appointment.time.contains("PM") || appointment.time.contains("AM") -> {
                // Already in 12-hour format, use as is
                appointment.time
            }
            appointment.time.contains(":") && appointment.time.length <= 5 -> {
                // 24-hour format (HH:mm), convert to 12-hour
                val time24 = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(appointment.time)
                time24?.let {
                    SimpleDateFormat("h:mm a", Locale.getDefault()).format(it)
                } ?: appointment.time
            }
            else -> appointment.time
        }
    } catch (e: Exception) {
        appointment.time
    }
    
    val statusColor = when (appointment.status) {
        "pending" -> Orange500
        "approved" -> Blue500
        "booked" -> Blue500
        "completed" -> Green500
        "cancelled" -> Red500
        else -> Gray500
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with doctor name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.chiropractorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Blue500
                    )
                    Text(
                        text = appointment.chiropractorSpecialization,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = appointment.status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date and time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Gray600
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$formattedDate at ${appointment.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
            }
            
            if (appointment.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Gray600
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = appointment.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                }
            }
            
            if (appointment.appointmentType != AppointmentType.CONSULTATION) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Gray600
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = appointment.appointmentType.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                }
            }
            
            // Action buttons
            if (appointment.status == "pending" || appointment.status == "approved" || appointment.status == "booked") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewDetailsClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Details")
                    }
                    
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Red500
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onViewDetailsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Details")
                }
            }
        }
    }
}

/**
 * Chiropractor Selection Dialog
 */
@Composable
private fun ChiropractorSelectionDialog(
    chiropractors: List<Chiropractor>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onChiropractorSelected: (Chiropractor) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Chiropractor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Blue500
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Gray600
                        )
                    }
                }
                
                HorizontalDivider(color = Gray200)
                
                // Content
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Blue500)
                    }
                } else if (chiropractors.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Gray400
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No chiropractors available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chiropractors) { chiropractor ->
                            ChiropractorSelectionCard(
                                chiropractor = chiropractor,
                                onClick = { onChiropractorSelected(chiropractor) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chiropractor Selection Card
 */
@Composable
private fun ChiropractorSelectionCard(
    chiropractor: Chiropractor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            AsyncImage(
                model = chiropractor.photoUrl ?: "https://via.placeholder.com/60",
                contentDescription = "Doctor Photo",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Gray200),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Doctor Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chiropractor.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                
                Text(
                    text = chiropractor.specialization,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Orange500
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${chiropractor.rating} • ${chiropractor.experience} years exp.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
            }
            
            // Arrow Icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Gray400
            )
        }
    }
}

/**
 * Cancel Appointment Dialog
 */
@Composable
private fun CancelAppointmentDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var cancellationReason by remember { mutableStateOf("") }
    val predefinedReasons = listOf(
        "Schedule conflict",
        "Personal emergency",
        "Feeling unwell",
        "Found another provider",
        "No longer needed",
        "Other"
    )
    var selectedReason by remember { mutableStateOf("") }
    var showCustomReason by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cancel Appointment",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Red500
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Gray600
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Appointment info
                Text(
                    text = "Are you sure you want to cancel this appointment?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = Gray50,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = appointment.chiropractorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Blue500
                        )
                        Text(
                            text = "${appointment.date} at ${appointment.time}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cancellation reason
                Text(
                    text = "Reason for cancellation:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Predefined reasons
                predefinedReasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedReason = reason
                                showCustomReason = reason == "Other"
                                if (reason != "Other") {
                                    cancellationReason = reason
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = {
                                selectedReason = reason
                                showCustomReason = reason == "Other"
                                if (reason != "Other") {
                                    cancellationReason = reason
                                }
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Red500
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray700
                        )
                    }
                }
                
                // Custom reason input
                if (showCustomReason) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancellationReason,
                        onValueChange = { cancellationReason = it },
                        label = { Text("Please specify") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Red500,
                            focusedLabelColor = Red500
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Keep Appointment")
                    }
                    
                    Button(
                        onClick = {
                            val finalReason = if (showCustomReason) {
                                cancellationReason.ifBlank { "Other" }
                            } else {
                                selectedReason.ifBlank { "No reason provided" }
                            }
                            onConfirm(finalReason)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red500,
                            contentColor = White
                        ),
                        enabled = selectedReason.isNotEmpty() && (!showCustomReason || cancellationReason.isNotBlank())
                    ) {
                        Text("Cancel Appointment")
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Booking Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun BookingScreenPreview() {
    BrightCarePatientTheme {
        BookingScreen(navController = rememberNavController())
    }
}