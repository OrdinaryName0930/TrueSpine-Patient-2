package com.brightcare.patient.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.R
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.BookingViewModel
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Payment Activity - Screen for payment processing
 * Receives appointment data via navigation arguments
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentActivity(
    navController: NavController,
    chiropractorId: String = "",
    date: String = "",
    time: String = "",
    paymentOption: String = "downpayment",
    message: String = "",
    modifier: Modifier = Modifier,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // State for image upload
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var firebaseImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    
    // Coroutine scope for Firebase operations
    val coroutineScope = rememberCoroutineScope()
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedImageBitmap = bitmap
                uploadError = null
                
                // Upload image to Firebase Storage
                if (bitmap != null) {
                    isUploadingImage = true
                    coroutineScope.launch {
                        val downloadUrl = uploadImageToFirebaseStorage(context, bitmap)
                        isUploadingImage = false
                        if (downloadUrl != null) {
                            firebaseImageUrl = downloadUrl
                            android.util.Log.d("PaymentActivity", "Image uploaded to Firebase: $downloadUrl")
                            Toast.makeText(context, "Receipt uploaded successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            uploadError = "Failed to upload image. Please try again."
                            Toast.makeText(context, "Failed to upload receipt", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PaymentActivity", "Error loading image", e)
                uploadError = "Error loading image: ${e.message}"
            }
        }
    }
    
    // Load chiropractor data and log received parameters
    LaunchedEffect(Unit) {
        android.util.Log.d("PaymentActivity", "Received parameters:")
        android.util.Log.d("PaymentActivity", "ChiropractorId: $chiropractorId")
        android.util.Log.d("PaymentActivity", "Date: $date")
        android.util.Log.d("PaymentActivity", "Time: $time")
        android.util.Log.d("PaymentActivity", "Payment Option: $paymentOption")
        android.util.Log.d("PaymentActivity", "Message: $message")
        
        // Load chiropractor data for validation and display
        if (chiropractorId.isNotBlank()) {
            viewModel.loadChiropractorById(chiropractorId)
        }
    }
    
    // Use passed parameters directly
    val paymentAmount = if (paymentOption == "full") "₱3,499.00" else "₱699.00"
    val paymentType = if (paymentOption == "full") "Full Payment" else "Downpayment"
    
    // Format date for display (date comes as yyyy-MM-dd)
    val formattedDate = try {
        if (date.isNotEmpty()) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            parsedDate?.let { outputFormat.format(it) } ?: "No date selected"
        } else {
            "No date selected"
        }
    } catch (e: Exception) {
        date.ifEmpty { "No date selected" }
    }
    
    val formattedTime = time.ifEmpty { "No time selected" }
    
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
                text = "Payment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Blue500
            )
        }
        
        // Appointment Summary Card
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Appointment Summary",
                        tint = Blue500,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Appointment Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Blue500
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date and Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gray600,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray900,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gray600,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray900,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider(color = Gray200, thickness = 1.dp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Payment Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Payment Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gray600,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = paymentType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray900,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Text(
                        text = paymentAmount,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (paymentOption == "full") Blue500 else Orange500,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // QR Code Payment Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code Payment",
                        tint = Blue500,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Scan to Pay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Blue500
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // QR Code Image based on payment option
                val qrImageResource = if (paymentOption == "full") {
                    R.drawable.full_payment // Full-payment.jpg
                } else {
                    R.drawable.downpayment // Downpayment.jpg
                }
                
                Card(
                    modifier = Modifier.size(280.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = qrImageResource),
                        contentDescription = "QR Code for $paymentType",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Download QR Code Button
                Button(
                    onClick = {
                        saveQrCodeToGallery(context, qrImageResource, paymentType)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue500
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download QR Code",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Payment Instructions
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Blue50
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Payment Instructions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Blue700
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "1. Scan the QR code using your mobile banking app\n" +
                                    "2. Send the exact amount: $paymentAmount\n" +
                                    "3. Take a screenshot of your payment receipt\n" +
                                    "4. Upload the receipt below to complete booking",
                            style = MaterialTheme.typography.bodySmall,
                            color = Blue600,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Upload Proof of Payment Card
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload Proof",
                        tint = Orange500,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Upload Proof of Payment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Orange500
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Upload Area
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedImageBitmap != null) Gray50 else Orange50
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (selectedImageBitmap != null) Green500 else Orange300
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageBitmap != null) {
                            // Show uploaded image
                            Image(
                                bitmap = selectedImageBitmap!!.asImageBitmap(),
                                contentDescription = "Uploaded Receipt",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Status overlay - shows uploading or uploaded
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = when {
                                    isUploadingImage -> Orange500
                                    firebaseImageUrl != null -> Green500
                                    uploadError != null -> Red500
                                    else -> Orange500
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isUploadingImage) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Uploading...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else if (firebaseImageUrl != null) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = "Uploaded",
                                            tint = White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Uploaded",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else if (uploadError != null) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Error",
                                            tint = White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Failed",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            // Upload placeholder
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Upload",
                                    tint = Orange400,
                                    modifier = Modifier.size(48.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = "Tap to upload receipt",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Orange600,
                                    fontWeight = FontWeight.SemiBold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "JPG, PNG, or screenshot",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Orange400
                                )
                            }
                        }
                    }
                }
                
                if (selectedImageBitmap != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Change image button
                    TextButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Image",
                            tint = Orange500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Change Image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Orange500,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Upload Guidelines
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Gray50
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Upload Guidelines",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Gray700
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "• Make sure the receipt shows the payment amount\n" +
                                    "• Image should be clear and readable\n" +
                                    "• Include transaction reference number if visible",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Book Now Button (only enabled after image uploaded to Firebase)
        Button(
            onClick = {
                if (firebaseImageUrl != null) {
                    // Parse the date string back to Date object
                    val appointmentDate = try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        inputFormat.parse(date)
                    } catch (e: Exception) {
                        null
                    }
                    
                    android.util.Log.d("PaymentActivity", "Booking with Firebase payment proof: $firebaseImageUrl")
                    
                    // Update form state with all appointment data and Firebase Storage URL
                    viewModel.updateFormState(
                        uiState.formState.copy(
                            selectedChiropractorId = chiropractorId,
                            selectedDate = appointmentDate,
                            selectedTime = time,
                            paymentOption = paymentOption,
                            symptoms = message.ifEmpty { "General consultation" },
                            notes = message,
                            paymentProofUri = firebaseImageUrl!!
                        )
                    )
                    // Proceed with booking
                    viewModel.bookAppointment()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = when {
                    isUploadingImage -> Orange500
                    firebaseImageUrl != null -> Green500
                    else -> Gray300
                },
                disabledContainerColor = Gray300
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = firebaseImageUrl != null && !uiState.isSaving && !isUploadingImage
        ) {
            if (uiState.isSaving || isUploadingImage) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isUploadingImage) "Uploading Receipt..." else "Booking...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (firebaseImageUrl != null) Icons.Default.Check else Icons.Default.Lock,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (firebaseImageUrl != null) 
                            "Complete Booking" 
                        else 
                            "Upload Receipt",
                        style = MaterialTheme.typography.bodyLarge,
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
            // Navigate back to home or appointments after successful booking
            navController.popBackStack()
            navController.popBackStack() // Pop twice to go back to main screen
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
 * Upload image to Firebase Storage
 * I-upload ang image sa Firebase Storage
 * Returns the download URL of the uploaded image
 */
private suspend fun uploadImageToFirebaseStorage(context: Context, bitmap: Bitmap): String? {
    return withContext(Dispatchers.IO) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                android.util.Log.e("PaymentActivity", "No authenticated user found")
                return@withContext null
            }
            
            val storage = FirebaseStorage.getInstance()
            
            // Create unique filename with user ID and timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val uniqueId = UUID.randomUUID().toString().take(8)
            val filename = "payment_proof_${timestamp}_$uniqueId.jpg"
            
            // Create storage reference: payment_proofs/{userId}/{filename}
            val storageRef = storage.reference
                .child("payment_proofs")
                .child(currentUser.uid)
                .child(filename)
            
            // Convert bitmap to byte array
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val imageData = baos.toByteArray()
            
            android.util.Log.d("PaymentActivity", "Uploading image to Firebase: ${storageRef.path}")
            
            // Upload image
            val uploadTask = storageRef.putBytes(imageData).await()
            
            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await()
            
            android.util.Log.d("PaymentActivity", "Image uploaded successfully: $downloadUrl")
            downloadUrl.toString()
            
        } catch (e: Exception) {
            android.util.Log.e("PaymentActivity", "Error uploading image to Firebase Storage", e)
            null
        }
    }
}

/**
 * Save QR code image to device gallery
 * I-save ang QR code image sa gallery ng device
 */
private fun saveQrCodeToGallery(context: Context, drawableResId: Int, paymentType: String) {
    try {
        // Get bitmap from drawable resource
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableResId)
        
        // Generate filename
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "BrightCare_QR_${paymentType.replace(" ", "_")}_$timestamp.jpg"
        
        // Save to gallery using MediaStore (works on all Android versions)
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            
            // For Android Q and above, use relative path
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BrightCare")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        
        uri?.let { imageUri ->
            resolver.openOutputStream(imageUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            
            // Mark as complete for Android Q and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            
            Toast.makeText(context, "QR Code saved to gallery!", Toast.LENGTH_SHORT).show()
            android.util.Log.d("PaymentActivity", "QR Code saved successfully: $filename")
        } ?: run {
            Toast.makeText(context, "Failed to save QR Code", Toast.LENGTH_SHORT).show()
            android.util.Log.e("PaymentActivity", "Failed to create media store entry")
        }
        
    } catch (e: Exception) {
        android.util.Log.e("PaymentActivity", "Error saving QR code", e)
        Toast.makeText(context, "Error saving QR Code: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentActivityPreview() {
    BrightCarePatientTheme {
        PaymentActivity(
            navController = rememberNavController()
        )
    }
}
