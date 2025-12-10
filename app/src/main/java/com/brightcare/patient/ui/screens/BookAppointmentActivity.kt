package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.brightcare.patient.utils.UnavailabilityTestUtils
import com.brightcare.patient.data.model.*
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.BookingViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class for day information
 * Data class para sa impormasyon ng araw
 */
data class DayInfo(
    val dayNumber: Int,
    val dayName: String,
    val fullDate: Date,
    val isAvailable: Boolean = true
)

/**
 * Book Appointment Activity - Separate screen for booking appointments
 * Activity para sa pag-book ng appointments - Hiwalay na screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentActivity(
    chiropractorId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BookingViewModel = hiltViewModel()
) {
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // State for chiropractor data
    var chiropractor by remember { mutableStateOf<Chiropractor?>(null) }
    var isLoadingChiropractor by remember { mutableStateOf(true) }
    var chiropractorError by remember { mutableStateOf<String?>(null) }
    var chiropractorUnavailability by remember { mutableStateOf<ChiropractorUnavailability?>(null) }
    
    // Fetch chiropractor data from Firestore
    LaunchedEffect(chiropractorId) {
        if (chiropractorId.isNotEmpty()) {
            try {
                isLoadingChiropractor = true
                chiropractorError = null
                
                // Fetch from Firestore chiropractors collection (using TrueSpine structure)
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val document = firestore.collection("chiropractors")
                    .document(chiropractorId)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val data = document.data!!
                    
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
                    
                    chiropractor = Chiropractor(
                        id = document.id,
                        name = fullName,
                        email = data["email"] as? String ?: "",
                        phoneNumber = data["contactNumber"] as? String ?: "",
                        photoUrl = data["profileImageUrl"] as? String,
                        specialization = data["specialization"] as? String ?: "General Practice",
                        licenseNumber = data["prcLicenseNumber"] as? String ?: "",
                        experience = (data["yearsOfExperience"] as? Long)?.toInt() ?: 0,
                        rating = 4.8, // Default rating as it's not in the structure
                        reviewCount = 150, // Default review count
                        isAvailable = true, // Default to available
                        location = "Philippines", // Default location
                        bio = data["about"] as? String ?: "",
                        serviceHours = data["serviceHours"] as? String // Get service hours from Firestore
                    )
                } else {
                    chiropractorError = "Chiropractor not found"
                }
            } catch (e: Exception) {
                chiropractorError = "Failed to load chiropractor information: ${e.message}"
                android.util.Log.e("BookAppointmentActivity", "Error fetching chiropractor", e)
            } finally {
                isLoadingChiropractor = false
            }
        }
    }
    
    // Load chiropractor unavailability data
    LaunchedEffect(chiropractorId) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val document = firestore.collection("chiro_unavailable")
                .document(chiropractorId)
                .get()
                .await()
            
            if (document.exists()) {
                val data = document.data ?: emptyMap()
                android.util.Log.d("BookAppointmentActivity", "Raw unavailability data: $data")
                
                chiropractorUnavailability = ChiropractorUnavailability.fromMap(chiropractorId, data)
                android.util.Log.d("BookAppointmentActivity", "Loaded unavailability data with ${chiropractorUnavailability?.dates?.size ?: 0} entries for chiropractor: $chiropractorId")
                
                // Debug: Print all unavailable dates
                chiropractorUnavailability?.dates?.forEach { unavailableDate ->
                    android.util.Log.d("BookAppointmentActivity", "Unavailable date: ${unavailableDate.date}, fullDay: ${unavailableDate.fullDay}, times: ${unavailableDate.times}")
                }
                
                // Run test to verify parsing
                UnavailabilityTestUtils.testUnavailabilityParsing()
            } else {
                chiropractorUnavailability = ChiropractorUnavailability(chiropractorId = chiropractorId)
                android.util.Log.d("BookAppointmentActivity", "No unavailability data found for chiropractor: $chiropractorId")
            }
        } catch (e: Exception) {
            android.util.Log.e("BookAppointmentActivity", "Error loading unavailability data", e)
            chiropractorUnavailability = ChiropractorUnavailability(chiropractorId = chiropractorId)
        }
    }
    
    // Get current date for validation
    val currentCalendar = Calendar.getInstance()
    val currentYear = currentCalendar.get(Calendar.YEAR)
    
    // Date selection state - structured approach
    var selectedYear by remember { mutableStateOf<Int?>(currentYear) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var selectedTime by remember { mutableStateOf("") }
    var appointmentMessage by remember { mutableStateOf("")}
    val currentMonth = currentCalendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
    val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
    
    // Available years (current year and next year only)
    val availableYears = listOf(currentYear, currentYear + 1)
    
    // Available months (don't show past months for current year)
    val availableMonths = remember(selectedYear) {
        if (selectedYear == currentYear) {
            (currentMonth..12).toList()
        } else {
            (1..12).toList()
        }
    }
    
    // Available days with day names based on selected year and month
    val availableDays = remember(selectedYear, selectedMonth, chiropractorUnavailability) {
        if (selectedYear != null && selectedMonth != null) {
            val calendar = Calendar.getInstance()
            calendar.set(selectedYear!!, selectedMonth!! - 1, 1) // Calendar.MONTH is 0-based
            val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            // For current year and month, don't show past days
            val startDay = if (selectedYear == currentYear && selectedMonth == currentMonth) {
                currentDay
            } else {
                1
            }
            
            (startDay..maxDays).map { day ->
                calendar.set(Calendar.DAY_OF_MONTH, day)
                val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                val isAvailable = chiropractorUnavailability?.isDateFullyUnavailable(dateString) != true
                
                // Debug logging for date availability
                android.util.Log.d("BookAppointmentActivity", "Checking date: $dateString, isFullyUnavailable: ${chiropractorUnavailability?.isDateFullyUnavailable(dateString)}, isAvailable: $isAvailable")
                
                DayInfo(
                    dayNumber = day,
                    dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time),
                    fullDate = calendar.time.clone() as Date,
                    isAvailable = isAvailable
                )
            }
        } else {
            emptyList()
        }
    }
    
    // Available time slots based on chiropractor's schedule and unavailability
    val availableTimeSlots = remember(chiropractor, selectedYear, selectedMonth, selectedDay, chiropractorUnavailability) {
        val baseTimeSlots = chiropractor?.let { chiro ->
            parseChiropractorSchedule(chiro.serviceHours)
        } ?: generateDefault30MinuteTimeSlots()
        
        // Convert to TimeSlot objects with availability status
        val unavailability = chiropractorUnavailability
        if (selectedYear != null && selectedMonth != null && selectedDay != null && unavailability != null) {
            val calendar = Calendar.getInstance()
            calendar.set(selectedYear!!, selectedMonth!! - 1, selectedDay!!)
            val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            // If the whole day is unavailable, return all slots as unavailable
            if (unavailability.isDateFullyUnavailable(dateString)) {
                baseTimeSlots.map { timeSlot ->
                    TimeSlot(
                        time = timeSlot,
                        isAvailable = false,
                        isBooked = false,
                        duration = 30
                    )
                }
            } else {
                // Mark specific time slots as unavailable
                baseTimeSlots.map { timeSlot ->
                    val time24Hour = convertTo24HourFormat(timeSlot)
                    val isTimeUnavailable = unavailability.isTimeUnavailable(dateString, time24Hour)
                    TimeSlot(
                        time = timeSlot,
                        isAvailable = !isTimeUnavailable,
                        isBooked = false,
                        duration = 30
                    )
                }
            }
        } else {
            // Convert to TimeSlot objects (all available by default)
            baseTimeSlots.map { timeSlot ->
                TimeSlot(
                    time = timeSlot,
                    isAvailable = true,
                    isBooked = false,
                    duration = 30
                )
            }
        }
    }
    
    // Create final date when all components are selected
    val finalSelectedDate = remember(selectedYear, selectedMonth, selectedDay) {
        if (selectedYear != null && selectedMonth != null && selectedDay != null) {
            Calendar.getInstance().apply {
                set(selectedYear!!, selectedMonth!! - 1, selectedDay!!) // Calendar.MONTH is 0-based
            }.time
        } else {
            null
        }
    }
    
    // Set selected chiropractor when data is loaded
    LaunchedEffect(chiropractor) {
        chiropractor?.let { chiro ->
            viewModel.setSelectedChiropractor(chiro)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
            .verticalScroll(rememberScrollState())
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
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Book Appointment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Blue500
            )
        }
        
        // Loading or Error State
        if (isLoadingChiropractor) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue500)
                }
            }
        } else if (chiropractorError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Red500,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = chiropractorError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Text("Go Back")
                    }
                }
            }
        } else if (chiropractor != null) {
            // Doctor Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Doctor Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Image
                        Box {
                            AsyncImage(
                                model = chiropractor!!.photoUrl ?: "https://via.placeholder.com/80",
                                contentDescription = "Doctor Photo",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Gray200),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Verified Badge
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp),
                                shape = CircleShape,
                                color = Blue500
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Verified",
                                    tint = White,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(16.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Doctor Details
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = chiropractor!!.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Gray900
                            )
                            
                            Text(
                                text = chiropractor!!.specialization,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            
                            if (chiropractor!!.location.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Blue500
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = chiropractor!!.location,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gray600
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Statistics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatisticItem(
                            icon = Icons.Default.People,
                            value = "7,500+",
                            label = "Patients",
                            iconColor = Blue500
                        )
                        
                        StatisticItem(
                            icon = Icons.Default.School,
                            value = "${chiropractor!!.experience}+",
                            label = "Years Exp.",
                            iconColor = Blue500
                        )
                        
                        StatisticItem(
                            icon = Icons.Default.Star,
                            value = if (chiropractor!!.rating > 0) "${chiropractor!!.rating}" else "N/A",
                            label = "Rating",
                            iconColor = Orange500
                        )
                        
                        StatisticItem(
                            icon = Icons.Default.RateReview,
                            value = "${chiropractor!!.reviewCount}",
                            label = "Review",
                            iconColor = Blue500
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Book Appointment Section
        Text(
            text = "BOOK APPOINTMENT",
            style = MaterialTheme.typography.labelMedium,
            color = Gray600,
            modifier = Modifier.padding(horizontal = 16.dp),
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Year Selection
        Text(
            text = "Year / Taon",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Gray900,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableYears) { year ->
                YearChip(
                    year = year,
                    isSelected = selectedYear == year,
                    onClick = { 
                        selectedYear = year
                        // Reset month and day when year changes
                        selectedMonth = null
                        selectedDay = null
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Month Selection (only show if year is selected)
        if (selectedYear != null) {
            Text(
                text = "Month / Buwan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray900,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(availableMonths) { month ->
                    MonthChip(
                        month = month,
                        isSelected = selectedMonth == month,
                        onClick = { 
                            selectedMonth = month
                            // Reset day when month changes
                            selectedDay = null
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Day Selection (only show if year and month are selected)
        if (selectedYear != null && selectedMonth != null) {
            Text(
                text = "Day / Araw",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray900,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(availableDays) { dayInfo ->
                    DayChip(
                        dayInfo = dayInfo,
                        isSelected = selectedDay == dayInfo.dayNumber,
                        onClick = { selectedDay = dayInfo.dayNumber }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Time Selection
        Text(
            text = "Time",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Gray900,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableTimeSlots) { timeSlot ->
                TimeChip(
                    time = timeSlot.time,
                    isSelected = selectedTime == timeSlot.time,
                    isAvailable = timeSlot.isAvailable,
                    onClick = { 
                        if (timeSlot.isAvailable) {
                            selectedTime = timeSlot.time 
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Custom Schedule Option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Want a custom schedule?",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700
            )
            
            TextButton(
                onClick = { /* Handle custom schedule */ }
            ) {
                Text(
                    text = "Request Schedule",
                    color = Blue500,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Optional Message Section
        Text(
            text = "Message (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Gray900,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = appointmentMessage,
            onValueChange = { appointmentMessage = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { 
                Text(
                    text = "Add any specific concerns or requests...",
                    color = Gray500
                ) 
            },
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue500,
                unfocusedBorderColor = Gray300,
                focusedLabelColor = Blue500
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Debug info for troubleshooting
        if (chiropractor != null) {
            android.util.Log.d("BookAppointmentActivity", "Button state - finalSelectedDate: $finalSelectedDate, selectedTime: '$selectedTime', isSaving: ${uiState.isSaving}")
        }
        
        // Make Appointment Button (only show if chiropractor data is loaded)
        if (chiropractor != null && !isLoadingChiropractor && chiropractorError == null) {
            Button(
                onClick = {
                    if (finalSelectedDate != null && selectedTime.isNotEmpty()) {
                        // Handle appointment booking
                        viewModel.updateFormState(
                            uiState.formState.copy(
                                selectedChiropractorId = chiropractorId,
                                selectedDate = finalSelectedDate,
                                selectedTime = selectedTime,
                                symptoms = if (appointmentMessage.isNotBlank()) appointmentMessage else "General consultation",
                                notes = appointmentMessage
                            )
                        )
                        viewModel.bookAppointment()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue500,
                    disabledContainerColor = Gray300
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = finalSelectedDate != null && selectedTime.isNotEmpty() && !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White
                    )
                } else {
                    Text(
                        text = "Make Appointment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
    
    // Success Dialog
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Navigate back after successful booking
            navController.popBackStack()
        }
    }
    
    // Error Handling
    uiState.errorMessage?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Booking Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}

/**
 * Statistic item component
 * Component para sa statistic item
 */
@Composable
private fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .padding(12.dp)
                    .size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Blue500
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Gray600
        )
    }
}

/**
 * Date chip component
 * Component para sa date chip
 */
@Composable
private fun DateChip(
    date: Date,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance().apply { time = date }
    val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
    val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)
    val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(date)
    
    val isToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time == Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
    
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .width(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Blue500 else if (isToday) Blue50 else Gray100
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            Text(
                text = if (isToday) "Today" else dayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) White else if (isToday) Blue500 else Gray600,
                fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
            )
            
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) White else if (isToday) Blue500 else Gray900
            )
            
            if (!isToday) {
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) White else Gray600
                )
            }
        }
    }
}

/**
 * Time chip component
 * Component para sa time chip
 */
@Composable
private fun TimeChip(
    time: String,
    isSelected: Boolean,
    isAvailable: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(enabled = isAvailable) { 
                if (isAvailable) onClick() 
            },
        shape = RoundedCornerShape(12.dp),
        color = when {
            !isAvailable -> Gray200
            isSelected -> Blue500
            else -> Gray100
        },
        border = if (isSelected || !isAvailable) null else androidx.compose.foundation.BorderStroke(1.dp, Gray300)
    ) {
        Text(
            text = time,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                !isAvailable -> Gray400
                isSelected -> White
                else -> Gray700
            }
        )
    }
}

/**
 * Year Selection Chip
 * Chip para sa pagpili ng taon
 */
@Composable
private fun YearChip(
    year: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(50.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Blue500 else White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) White else Gray900,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Month Selection Chip
 * Chip para sa pagpili ng buwan
 */
@Composable
private fun MonthChip(
    month: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    
    Card(
        modifier = Modifier
            .width(70.dp)
            .height(50.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Blue500 else White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = monthNames[month - 1],
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) White else Gray900,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Day Selection Chip with day name
 * Chip para sa pagpili ng araw na may pangalan ng araw
 */
@Composable
private fun DayChip(
    dayInfo: DayInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isToday = remember(dayInfo.fullDate) {
        val today = Calendar.getInstance()
        val dayCalendar = Calendar.getInstance().apply { time = dayInfo.fullDate }
        
        today.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR) &&
        today.get(Calendar.DAY_OF_YEAR) == dayCalendar.get(Calendar.DAY_OF_YEAR)
    }
    
    Card(
        modifier = Modifier
            .width(70.dp)
            .height(80.dp)
            .clickable(enabled = dayInfo.isAvailable) { 
                if (dayInfo.isAvailable) onClick() 
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                !dayInfo.isAvailable -> Gray200
                isSelected -> Blue500
                isToday -> Blue100
                else -> White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day name (Mon, Tue, etc.)
            Text(
                text = if (isToday) "Today" else dayInfo.dayName,
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    !dayInfo.isAvailable -> Gray400
                    isSelected -> White
                    isToday -> Blue700
                    else -> Gray600
                },
                fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Day number
            Text(
                text = dayInfo.dayNumber.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    !dayInfo.isAvailable -> Gray400
                    isSelected -> White
                    isToday -> Blue700
                    else -> Gray900
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Parse chiropractor's service hours to generate time slots
 * Parse ng service hours ng chiropractor para gumawa ng time slots
 */
private fun parseChiropractorSchedule(serviceHours: String?): List<String> {
    if (serviceHours.isNullOrBlank()) {
        // Default time slots if no service hours specified (10:00 AM to 7:00 PM, 30-minute intervals)
        return generateDefault30MinuteTimeSlots()
    }
    
    return try {
        // Parse service hours like "Monday - Friday 10:00 am to 7:00 pm"
        val timePattern = Regex("""(\d{1,2}):(\d{2})\s*(am|pm).*?(\d{1,2}):(\d{2})\s*(am|pm)""", RegexOption.IGNORE_CASE)
        val match = timePattern.find(serviceHours)
        
        if (match != null) {
            val startHour = match.groupValues[1].toInt()
            val startMinute = match.groupValues[2].toInt()
            val startAmPm = match.groupValues[3].lowercase()
            val endHour = match.groupValues[4].toInt()
            val endMinute = match.groupValues[5].toInt()
            val endAmPm = match.groupValues[6].lowercase()
            
            // Convert to 24-hour format
            val start24Hour = if (startAmPm == "pm" && startHour != 12) startHour + 12 else if (startAmPm == "am" && startHour == 12) 0 else startHour
            val end24Hour = if (endAmPm == "pm" && endHour != 12) endHour + 12 else if (endAmPm == "am" && endHour == 12) 0 else endHour
            
            // Enforce business hours: 10:00 AM (10) to 7:00 PM (19)
            val restrictedStart = maxOf(start24Hour, 10) // Not earlier than 10:00 AM
            val restrictedEnd = minOf(end24Hour, 19) // Not later than 7:00 PM
            
            // Generate 30-minute slots between restricted start and end time
            val timeSlots = mutableListOf<String>()
            var currentHour = restrictedStart
            var currentMinute = 0
            
            while (currentHour < restrictedEnd || (currentHour == restrictedEnd && currentMinute == 0)) {
                val displayHour = if (currentHour == 0) 12 else if (currentHour > 12) currentHour - 12 else currentHour
                val amPm = if (currentHour < 12) "AM" else "PM"
                val minuteStr = if (currentMinute == 0) "00" else currentMinute.toString()
                timeSlots.add("$displayHour:$minuteStr $amPm")
                
                // Increment by 30 minutes
                currentMinute += 30
                if (currentMinute >= 60) {
                    currentMinute = 0
                    currentHour++
                }
            }
            
            return timeSlots
        } else {
            // If parsing fails, return default slots (10:00 AM to 7:00 PM, 30-minute intervals)
            generateDefault30MinuteTimeSlots()
        }
    } catch (e: Exception) {
        // If any error occurs, return default slots (10:00 AM to 7:00 PM, 30-minute intervals)
        generateDefault30MinuteTimeSlots()
    }
}

/**
 * Convert time from 12-hour format to 24-hour format
 * I-convert ang time mula 12-hour format patungo sa 24-hour format
 */
private fun convertTo24HourFormat(time12Hour: String): String {
    return try {
        val parts = time12Hour.split(" ")
        if (parts.size != 2) return time12Hour
        
        val timePart = parts[0]
        val amPm = parts[1].uppercase()
        val timeParts = timePart.split(":")
        
        if (timeParts.size != 2) return time12Hour
        
        var hour = timeParts[0].toInt()
        val minute = timeParts[1]
        
        when (amPm) {
            "AM" -> {
                if (hour == 12) hour = 0
            }
            "PM" -> {
                if (hour != 12) hour += 12
            }
        }
        
        String.format("%02d:%s", hour, minute)
    } catch (e: Exception) {
        android.util.Log.e("BookAppointmentActivity", "Error converting time format: $time12Hour", e)
        time12Hour
    }
}

/**
 * Generate default 30-minute time slots from 10:00 AM to 7:00 PM
 * Mag-generate ng default 30-minute time slots mula 10:00 AM hanggang 7:00 PM
 */
private fun generateDefault30MinuteTimeSlots(): List<String> {
    val timeSlots = mutableListOf<String>()
    var currentHour = 10 // Start at 10:00 AM
    var currentMinute = 0
    val endHour = 19 // End at 7:00 PM (19:00 in 24-hour format)
    
    while (currentHour < endHour || (currentHour == endHour && currentMinute == 0)) {
        val displayHour = if (currentHour > 12) currentHour - 12 else currentHour
        val amPm = if (currentHour < 12) "AM" else "PM"
        val minuteStr = if (currentMinute == 0) "00" else currentMinute.toString()
        timeSlots.add("$displayHour:$minuteStr $amPm")
        
        // Increment by 30 minutes
        currentMinute += 30
        if (currentMinute >= 60) {
            currentMinute = 0
            currentHour++
        }
    }
    
    return timeSlots
}

@Preview(showBackground = true)
@Composable
fun BookAppointmentActivityPreview() {
    BrightCarePatientTheme {
        BookAppointmentActivity(
            chiropractorId = "test123",
            navController = rememberNavController()
        )
    }
}
