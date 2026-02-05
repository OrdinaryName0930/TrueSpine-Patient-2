package com.brightcare.patient.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.brightcare.patient.data.model.Appointment
import com.brightcare.patient.data.repository.ReviewRepository
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.ui.component.ReviewDialog
import com.brightcare.patient.ui.component.ReviewSuccessDialog
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.BookingViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
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
                },
                onReviewSubmitted = {
                    // Refresh appointments after review
                    viewModel.loadUserAppointments()
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
    onReviewSubmitted: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Review state
    var showReviewDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isSubmittingReview by remember { mutableStateOf(false) }
    var isReviewed by remember { mutableStateOf(appointment.isReviewed) }
    var reviewError by remember { mutableStateOf<String?>(null) }
    
    // Create ReviewRepository instance
    val reviewRepository = remember {
        ReviewRepository(
            firestore = FirebaseFirestore.getInstance(),
            firebaseAuth = FirebaseAuth.getInstance()
        )
    }
    val coroutineScope = rememberCoroutineScope()
    
    // Check if appointment is reviewed on load
    LaunchedEffect(appointment.id) {
        val reviewed = reviewRepository.isAppointmentReviewed(appointment.id)
        isReviewed = reviewed || appointment.isReviewed
    }

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
    
    // Review Dialog
    if (showReviewDialog) {
        ReviewDialog(
            chiropractorName = appointment.chiropractorName.ifEmpty { "your chiropractor" },
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment, isAnonymous ->
                coroutineScope.launch {
                    isSubmittingReview = true
                    reviewError = null
                    
                    val chiropractorId = appointment.chiroId.ifEmpty { appointment.chiropractorId }
                    
                    val result = reviewRepository.submitReview(
                        appointmentId = appointment.id,
                        chiropractorId = chiropractorId,
                        rating = rating,
                        comment = comment,
                        isAnonymous = isAnonymous
                    )
                    
                    result.fold(
                        onSuccess = {
                            isSubmittingReview = false
                            showReviewDialog = false
                            isReviewed = true
                            showSuccessDialog = true
                            onReviewSubmitted()
                        },
                        onFailure = { exception ->
                            isSubmittingReview = false
                            reviewError = exception.message
                        }
                    )
                }
            },
            isSubmitting = isSubmittingReview
        )
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        ReviewSuccessDialog(
            onDismiss = { showSuccessDialog = false }
        )
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
        
        // Payment Information
        if (appointment.paymentOption.isNotEmpty()) {
            PaymentInfoCard(
                paymentOption = appointment.paymentOption,
                paymentProofUri = appointment.paymentProofUri
            )
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
        
        // Review Button for Completed Appointments
        if (appointment.status == "completed") {
            ReviewSection(
                isReviewed = isReviewed,
                reviewError = reviewError,
                onReviewClick = { showReviewDialog = true },
                onDismissError = { reviewError = null }
            )
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

/**
 * Payment Information Card
 * Card para sa payment information
 */
@Composable
private fun PaymentInfoCard(
    paymentOption: String,
    paymentProofUri: String
) {
    val context = LocalContext.current
    var showFullScreenImage by remember { mutableStateOf(false) }
    
    // Determine payment details based on option
    val paymentMethod = when (paymentOption.lowercase()) {
        "full" -> "Full Payment"
        "downpayment" -> "Downpayment"
        else -> paymentOption.replaceFirstChar { it.uppercase() }
    }
    
    val paymentAmount = when (paymentOption.lowercase()) {
        "full" -> "₱3,499.00"
        "downpayment" -> "₱699.00"
        else -> "N/A"
    }
    
    val paymentColor = when (paymentOption.lowercase()) {
        "full" -> Blue500
        "downpayment" -> Orange500
        else -> Gray600
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = paymentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Payment Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = paymentColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Payment Method and Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        color = paymentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = paymentMethod,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = paymentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Amount Paid",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = paymentAmount,
                        style = MaterialTheme.typography.titleLarge,
                        color = paymentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Payment Proof Section
            if (paymentProofUri.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider(color = Gray200, thickness = 1.dp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Payment Proof",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Payment Proof Image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { showFullScreenImage = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Gray50),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(paymentProofUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Payment Proof",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Tap to view overlay
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Blue500.copy(alpha = 0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "View Full",
                                    tint = White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tap to view",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Full screen image dialog
                if (showFullScreenImage) {
                    Dialog(onDismissRequest = { showFullScreenImage = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.8f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Gray900)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(Uri.parse(paymentProofUri))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Payment Proof Full View",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    contentScale = ContentScale.Fit
                                )
                                
                                // Close button
                                IconButton(
                                    onClick = { showFullScreenImage = false },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = White.copy(alpha = 0.9f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Gray800,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // No payment proof uploaded
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Gray100
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ImageNotSupported,
                            contentDescription = null,
                            tint = Gray500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No payment proof uploaded",
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
 * Review Section for completed appointments
 * Section ng review para sa completed appointments
 */
@Composable
private fun ReviewSection(
    isReviewed: Boolean,
    reviewError: String?,
    onReviewClick: () -> Unit,
    onDismissError: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isReviewed) Green50 else Orange50
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isReviewed) Icons.Default.CheckCircle else Icons.Default.Star,
                    contentDescription = null,
                    tint = if (isReviewed) Green600 else Orange500,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isReviewed) "Review Submitted" else "Rate Your Experience",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isReviewed) Green700 else Orange600
                    )
                    Text(
                        text = if (isReviewed) 
                            "Thank you for your feedback! / Salamat sa iyong feedback!" 
                        else 
                            "Share your experience with the chiropractor",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isReviewed) Green600 else Orange500
                    )
                }
            }
            
            // Error message if any
            reviewError?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Red50
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Red500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = Red600,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Red500,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            // Review button (only show if not reviewed)
            if (!isReviewed) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onReviewClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange500,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Write a Review",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Tagalog text
                Text(
                    text = "Magsulat ng review / I-rate ang iyong experience",
                    style = MaterialTheme.typography.bodySmall,
                    color = Orange400,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                // Show reviewed badge
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Green100
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Green600,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You have already reviewed this appointment",
                            style = MaterialTheme.typography.bodySmall,
                            color = Green700,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
