package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.brightcare.patient.data.model.Appointment
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.BookingViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Appointment Details Screen
 * Screen para sa detalye ng appointment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailsScreen(
    navController: NavController,
    appointmentId: String,
    modifier: Modifier = Modifier,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Find the appointment by ID
    val appointment = appointments.find { it.id == appointmentId }
    
    // Load appointments if not already loaded
    LaunchedEffect(Unit) {
        if (appointments.isEmpty()) {
            viewModel.loadUserAppointments()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = "Appointment Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Blue500
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Blue500
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = White
            )
        )
        
        if (appointment == null) {
            // Show loading or error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Blue500)
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Appointment not found",
                            style = MaterialTheme.typography.titleMedium,
                            color = Error
                        )
                        Text(
                            text = "The appointment you're looking for could not be found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Show appointment details
            AppointmentDetailsContent(
                appointment = appointment,
                onCancelClick = {
                    viewModel.cancelAppointment(appointment.id, "Cancelled by patient")
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Appointment details content
 * Content ng appointment details
 */
@Composable
private fun AppointmentDetailsContent(
    appointment: Appointment,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Format date to "December 20, 2025" format
    val formattedDate = try {
        val date = when {
            appointment.date.contains("-") -> {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(appointment.date)
            }
            appointment.date.contains(" ") -> {
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).parse(appointment.date)
            }
            else -> {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(appointment.date)
            }
        }
        date?.let { 
            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(it)
        } ?: appointment.date
    } catch (e: Exception) {
        appointment.date
    }
    
    // Format time to ensure consistent display
    val formattedTime = try {
        when {
            appointment.time.contains("PM") || appointment.time.contains("AM") -> {
                appointment.time
            }
            appointment.time.contains(":") && appointment.time.length <= 5 -> {
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
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = appointment.status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Appointment ID: ${appointment.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
        }
        
        // Chiropractor Information
        DetailCard(
            title = "Chiropractor Information",
            icon = Icons.Default.Person
        ) {
            DetailRow(
                label = "Name",
                value = appointment.chiropractorName.ifEmpty { "Not specified" }
            )
            DetailRow(
                label = "Specialization",
                value = appointment.chiropractorSpecialization.ifEmpty { "General Practice" }
            )
        }
        
        // Appointment Information
        DetailCard(
            title = "Appointment Information",
            icon = Icons.Default.CalendarToday
        ) {
            DetailRow(
                label = "Date",
                value = formattedDate
            )
            DetailRow(
                label = "Time",
                value = formattedTime.ifEmpty { "Not specified" }
            )
            DetailRow(
                label = "Type",
                value = appointment.appointmentType.name.lowercase().replaceFirstChar { it.uppercase() }
            )
            if (appointment.location.isNotEmpty()) {
                DetailRow(
                    label = "Location",
                    value = appointment.location
                )
            }
            DetailRow(
                label = "Duration",
                value = "${appointment.duration} minutes"
            )
        }
        
        // Additional Information
        if (appointment.message.isNotEmpty() || appointment.symptoms.isNotEmpty() || appointment.notes.isNotEmpty()) {
            DetailCard(
                title = "Additional Information",
                icon = Icons.Default.Info
            ) {
                if (appointment.message.isNotEmpty()) {
                    DetailRow(
                        label = "Message",
                        value = appointment.message
                    )
                }
                if (appointment.symptoms.isNotEmpty()) {
                    DetailRow(
                        label = "Symptoms",
                        value = appointment.symptoms
                    )
                }
                if (appointment.notes.isNotEmpty()) {
                    DetailRow(
                        label = "Notes",
                        value = appointment.notes
                    )
                }
                DetailRow(
                    label = "First Visit",
                    value = if (appointment.isFirstVisit) "Yes" else "No"
                )
            }
        }
        
        // Action Buttons
        if (appointment.status == "pending" || appointment.status == "approved" || appointment.status == "booked") {
            Button(
                onClick = onCancelClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Red500,
                    contentColor = White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel Appointment")
            }
        }
        
        // Add bottom padding for better scrolling
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Detail card component
 * Component ng detail card
 */
@Composable
private fun DetailCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Blue500,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Blue500
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            content()
        }
    }
}

/**
 * Detail row component
 * Component ng detail row
 */
@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Gray600,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray800
        )
    }
}
