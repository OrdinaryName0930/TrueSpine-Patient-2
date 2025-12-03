package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class AppointmentInfo(
    val id: String,
    val doctorName: String,
    val specialization: String,
    val date: Date,
    val time: String,
    val status: AppointmentStatus,
    val location: String,
    val type: String
)

enum class AppointmentStatus {
    UPCOMING, COMPLETED, CANCELLED, PENDING
}

/**
 * Booking screen - Manage appointments and schedule
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Upcoming", "Past", "All")
    
    // Sample appointment data
    val appointments = remember {
        val calendar = Calendar.getInstance()
        listOf(
            AppointmentInfo(
                id = "1",
                doctorName = "Dr. Maria Santos",
                specialization = "Spinal Adjustment",
                date = calendar.apply { add(Calendar.DAY_OF_MONTH, 2) }.time,
                time = "10:00 AM",
                status = AppointmentStatus.UPCOMING,
                location = "Makati Clinic",
                type = "Consultation"
            ),
            AppointmentInfo(
                id = "2",
                doctorName = "Dr. John Reyes",
                specialization = "Sports Injury",
                date = calendar.apply { add(Calendar.DAY_OF_MONTH, -5) }.time,
                time = "2:00 PM",
                status = AppointmentStatus.COMPLETED,
                location = "QC Medical Center",
                type = "Treatment"
            ),
            AppointmentInfo(
                id = "3",
                doctorName = "Dr. Ana Cruz",
                specialization = "Pediatric Chiropractic",
                date = calendar.apply { add(Calendar.DAY_OF_MONTH, 5) }.time,
                time = "9:30 AM",
                status = AppointmentStatus.PENDING,
                location = "Manila Health Hub",
                type = "Follow-up"
            ),
            AppointmentInfo(
                id = "4",
                doctorName = "Dr. Michael Garcia",
                specialization = "Pain Management",
                date = calendar.apply { add(Calendar.DAY_OF_MONTH, -10) }.time,
                time = "3:30 PM",
                status = AppointmentStatus.CANCELLED,
                location = "Pasig Wellness Center",
                type = "Therapy"
            )
        )
    }

    val filteredAppointments = remember(selectedTab, appointments) {
        when (selectedTab) {
            0 -> appointments.filter { 
                it.status == AppointmentStatus.UPCOMING || it.status == AppointmentStatus.PENDING 
            }
            1 -> appointments.filter { 
                it.status == AppointmentStatus.COMPLETED || it.status == AppointmentStatus.CANCELLED 
            }
            else -> appointments
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
                
                FloatingActionButton(
                    onClick = { 
                        // Navigate to book new appointment
                        navController.navigate("chiro")
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
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    selectedContentColor = Blue500,
                    unselectedContentColor = Gray500
                )
            }
        }

        // Appointments List
        if (filteredAppointments.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = "No appointments",
                    tint = Gray400,
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "No appointments found",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Gray600
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Book your first appointment with a chiropractor",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray500
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { navController.navigate("chiro") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue500,
                        contentColor = White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Book Appointment")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAppointments) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onRescheduleClick = {
                            // Handle reschedule
                        },
                        onCancelClick = {
                            // Handle cancel
                        },
                        onViewDetailsClick = {
                            // Navigate to appointment details
                            navController.navigate("appointment_details/${appointment.id}")
                        }
                    )
                }
                
                // Add bottom padding for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: AppointmentInfo,
    onRescheduleClick: () -> Unit,
    onCancelClick: () -> Unit,
    onViewDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.doctorName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                    )
                    
                    Text(
                        text = appointment.specialization,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Blue500,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                
                // Status badge
                Surface(
                    color = when (appointment.status) {
                        AppointmentStatus.UPCOMING -> Blue50
                        AppointmentStatus.COMPLETED -> Success.copy(alpha = 0.1f)
                        AppointmentStatus.CANCELLED -> Error.copy(alpha = 0.1f)
                        AppointmentStatus.PENDING -> Warning.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = when (appointment.status) {
                            AppointmentStatus.UPCOMING -> "Upcoming"
                            AppointmentStatus.COMPLETED -> "Completed"
                            AppointmentStatus.CANCELLED -> "Cancelled"
                            AppointmentStatus.PENDING -> "Pending"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = when (appointment.status) {
                                AppointmentStatus.UPCOMING -> Blue500
                                AppointmentStatus.COMPLETED -> Success
                                AppointmentStatus.CANCELLED -> Error
                                AppointmentStatus.PENDING -> Warning
                            },
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Appointment details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = Gray500,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = dateFormat.format(appointment.date),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray700,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = Gray500,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = appointment.time,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray700,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Gray500,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = appointment.location,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray700,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "Type",
                            tint = Gray500,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = appointment.type,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray700,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
            
            // Action buttons (only for upcoming appointments)
            if (appointment.status == AppointmentStatus.UPCOMING || appointment.status == AppointmentStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewDetailsClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Blue500
                        )
                    ) {
                        Text("Details")
                    }
                    
                    OutlinedButton(
                        onClick = onRescheduleClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Orange500
                        )
                    ) {
                        Text("Reschedule")
                    }
                    
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Error
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(
                    onClick = onViewDetailsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "View Details",
                        color = Blue500,
                        fontWeight = FontWeight.Medium
                    )
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
        BookingScreen(
            navController = rememberNavController()
        )
    }
}
