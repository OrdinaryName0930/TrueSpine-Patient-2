package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.brightcare.patient.data.model.*
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.BookingViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chiropractor booking screen with time slot selection
 * Screen para sa pag-book ng chiropractor na may time slot selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChiropractorBookingScreen(
    chiropractorId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BookingViewModel = hiltViewModel()
) {
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    
    
    // Mock chiropractor data - in real implementation, fetch from repository
    val chiropractor = remember {
        Chiropractor(
            id = chiropractorId,
            name = "Dr. Maria Santos",
            email = "dr.santos@example.com",
            phoneNumber = "+63 912 345 6789",
            specialization = "Spinal Adjustment Specialist",
            licenseNumber = "CHR12345",
            experience = 15,
            rating = 4.8,
            reviewCount = 120,
            isAvailable = true,
            location = "Makati Medical Center",
            bio = "Experienced chiropractor specializing in spinal adjustments and pain management."
        )
    }
    
    // Set selected chiropractor when screen loads
    LaunchedEffect(chiropractor) {
        viewModel.setSelectedChiropractor(chiropractor)
    }
    
    // Date selection state
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Load time slots when date is selected
    LaunchedEffect(selectedDate) {
        selectedDate?.let { date ->
            viewModel.loadAvailableTimeSlots(chiropractorId, date)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Blue500
                )
            }
            
            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "Book Appointment\nMag-book ng Appointment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Blue500
                )
                
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chiropractor Info Card
            item {
                ChiropractorInfoCard(chiropractor = chiropractor)
            }
            
            
            // Date Selection
            item {
                DateSelectionCard(
                    selectedDate = selectedDate,
                    onDateSelected = { date ->
                        selectedDate = date
                        viewModel.updateFormState(
                            uiState.formState.copy(selectedDate = date)
                        )
                    },
                    onShowDatePicker = { showDatePicker = true }
                )
            }
            
            // Time Slot Selection
            if (selectedDate != null) {
                item {
                    TimeSlotSelectionCard(
                        timeSlots = uiState.availableTimeSlots,
                        selectedTime = uiState.formState.selectedTime,
                        onTimeSelected = { time ->
                            viewModel.selectTimeSlot(time)
                        },
                        isLoading = uiState.isLoading
                    )
                }
            }
            
            // Appointment Details Form
            if (selectedDate != null && uiState.formState.selectedTime.isNotBlank()) {
                item {
                    AppointmentDetailsCard(
                        formState = uiState.formState,
                        onFormStateChanged = { newFormState ->
                            viewModel.updateFormState(newFormState)
                        }
                    )
                }
            }
            
            // Book Button
            if (selectedDate != null && uiState.formState.selectedTime.isNotBlank()) {
                item {
                    Button(
                        onClick = { 
                            viewModel.bookAppointment()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Book Appointment\nMag-book ng Appointment",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Add bottom padding
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Success Dialog
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Navigate back to booking screen after successful booking
            navController.navigate(NavigationRoutes.BOOKING) {
                popUpTo(NavigationRoutes.CHIRO) { inclusive = true }
            }
        }
    }
    
    // Error Snackbar
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
        }
    }
}

/**
 * Chiropractor info card
 * Card ng chiropractor info
 */
@Composable
private fun ChiropractorInfoCard(
    chiropractor: Chiropractor,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            AsyncImage(
                model = chiropractor.photoUrl ?: "https://via.placeholder.com/80",
                contentDescription = "Doctor Photo",
                modifier = Modifier
                    .size(80.dp)
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
                    color = Blue500
                )
                
                Text(
                    text = chiropractor.specialization,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Orange500
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${chiropractor.rating} (${chiropractor.reviewCount} reviews)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
                
                if (chiropractor.location.isNotBlank()) {
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
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = chiropractor.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }
            }
        }
    }
}

/**
 * Date selection card
 * Card para sa date selection
 */
@Composable
private fun DateSelectionCard(
    selectedDate: Date?,
    onDateSelected: (Date) -> Unit,
    onShowDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Date\nPumili ng Petsa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Blue500
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onShowDatePicker,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedDate?.let { dateFormat.format(it) } ?: "Choose a date",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Time slot selection card
 * Card para sa time slot selection
 */
@Composable
private fun TimeSlotSelectionCard(
    timeSlots: List<TimeSlot>,
    selectedTime: String,
    onTimeSelected: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Available Time Slots\nMga Available na Time Slots",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Blue500
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue500)
                }
            } else if (timeSlots.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No available time slots for this date\nWalang available na time slots para sa petsang ito",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(timeSlots) { timeSlot ->
                        TimeSlotChip(
                            timeSlot = timeSlot,
                            isSelected = selectedTime == timeSlot.time,
                            onClick = { onTimeSelected(timeSlot.time) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Time slot chip
 * Chip para sa time slot
 */
@Composable
private fun TimeSlotChip(
    timeSlot: TimeSlot,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        !timeSlot.isAvailable -> Gray200
        isSelected -> Blue500
        else -> White
    }
    
    val textColor = when {
        !timeSlot.isAvailable -> Gray500
        isSelected -> White
        else -> Blue500
    }
    
    Surface(
        modifier = modifier
            .clickable(enabled = timeSlot.isAvailable) { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        border = if (!isSelected && timeSlot.isAvailable) {
            androidx.compose.foundation.BorderStroke(1.dp, Blue500)
        } else null
    ) {
        Text(
            text = timeSlot.time,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Appointment details card
 * Card para sa appointment details
 */
@Composable
private fun AppointmentDetailsCard(
    formState: BookingFormState,
    onFormStateChanged: (BookingFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Appointment Details\nMga Detalye ng Appointment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Blue500
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Appointment Type
            Text(
                text = "Appointment Type",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray700
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppointmentType.values()) { type ->
                    FilterChip(
                        selected = formState.appointmentType == type,
                        onClick = {
                            onFormStateChanged(formState.copy(appointmentType = type))
                        },
                        label = {
                            Text(
                                text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Symptoms
            OutlinedTextField(
                value = formState.symptoms,
                onValueChange = { symptoms ->
                    onFormStateChanged(
                        formState.copy(
                            symptoms = symptoms,
                            isSymptomsError = false,
                            symptomsErrorMessage = ""
                        )
                    )
                },
                label = { Text("Describe your symptoms*") },
                placeholder = { Text("Please describe your symptoms or concerns...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                isError = formState.isSymptomsError,
                supportingText = if (formState.isSymptomsError) {
                    { Text(formState.symptomsErrorMessage) }
                } else null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Notes (Optional)
            OutlinedTextField(
                value = formState.notes,
                onValueChange = { notes ->
                    onFormStateChanged(formState.copy(notes = notes))
                },
                label = { Text("Additional Notes (Optional)") },
                placeholder = { Text("Any additional information...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // First Visit Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = formState.isFirstVisit,
                    onCheckedChange = { isFirstVisit ->
                        onFormStateChanged(formState.copy(isFirstVisit = isFirstVisit))
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "This is my first visit to this chiropractor\nIto ang aking unang pagbisita sa chiropractor na ito",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
            }
        }
    }
}

/**
 * Simple date picker dialog
 * Simple na date picker dialog
 */
@Composable
private fun DatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    // For simplicity, showing next 7 days
    val availableDates = remember {
        val calendar = Calendar.getInstance()
        (1..7).map { dayOffset ->
            calendar.add(Calendar.DAY_OF_MONTH, if (dayOffset == 1) 1 else 1)
            calendar.time.clone() as Date
        }
    }
    
    val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = {
            LazyColumn {
                items(availableDates) { date ->
                    TextButton(
                        onClick = { onDateSelected(date) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dateFormat.format(date),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun ChiropractorBookingScreenPreview() {
    BrightCarePatientTheme {
        ChiropractorBookingScreen(
            chiropractorId = "test123",
            navController = rememberNavController()
        )
    }
}

