package com.brightcare.patient.ui.component.complete_your_profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.utils.ImageCompressionUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * ID Upload Component for Complete Profile
 * Allows users to upload front and back of their ID using camera or gallery
 * Nagbibigay-daan sa mga user na mag-upload ng harap at likod ng kanilang ID gamit ang camera o gallery
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdUploadComponent(
    frontImageUri: String,
    backImageUri: String,
    onFrontImageSelected: (String) -> Unit,
    onBackImageSelected: (String) -> Unit,
    isFrontError: Boolean = false,
    isBackError: Boolean = false,
    frontErrorMessage: String = "",
    backErrorMessage: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Clean up old temporary files on component initialization
    // Linisin ang mga lumang temporary files sa component initialization
    LaunchedEffect(Unit) {
        ImageCompressionUtils.cleanupTempFiles(context)
    }
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission result will be handled by the camera launcher
    }
    
    // State for tracking which image is being selected (front or back)
    var isSelectingFront by remember { mutableStateOf(true) }
    
    // State for temporary camera URI
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    // Create temporary file for camera capture
    val createImageFile = remember {
        {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "ID_${timeStamp}_"
            val storageDir = File(context.cacheDir, "images")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            File.createTempFile(imageFileName, ".jpg", storageDir)
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            // Image captured successfully, now set the URI
            if (isSelectingFront) {
                onFrontImageSelected(tempCameraUri.toString())
            } else {
                onBackImageSelected(tempCameraUri.toString())
            }
        }
        tempCameraUri = null // Clear temp URI
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            if (isSelectingFront) {
                onFrontImageSelected(selectedUri.toString())
            } else {
                onBackImageSelected(selectedUri.toString())
            }
        }
    }
    
    // Function to launch camera
    val launchCamera = { isFront: Boolean ->
        isSelectingFront = isFront
        
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasCameraPermission) {
            try {
                val photoFile = createImageFile()
                val photoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    photoFile
                )
                
                // Store the temp URI for use after camera capture
                tempCameraUri = photoUri
                
                // Launch camera with the URI
                cameraLauncher.launch(photoUri)
            } catch (e: Exception) {
                // Handle error creating file
                e.printStackTrace()
                tempCameraUri = null
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Function to launch gallery
    val launchGallery = { isFront: Boolean ->
        isSelectingFront = isFront
        galleryLauncher.launch("image/*")
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ID Verification",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Gray900
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
        
        Text(
            text = "Please upload clear photos of the front and back of your valid ID for verification purposes. Images will be automatically optimized for faster upload while maintaining quality.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Gray600,
                lineHeight = 20.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        // Front ID Upload
        IdUploadCard(
            title = "*Front of ID",
            imageUri = frontImageUri,
            isError = isFrontError,
            errorMessage = frontErrorMessage,
            onCameraClick = { launchCamera(true) },
            onGalleryClick = { launchGallery(true) },
            onDeleteClick = { onFrontImageSelected("") }
        )
        
        // Back ID Upload
        IdUploadCard(
            title = "*Back of ID",
            imageUri = backImageUri,
            isError = isBackError,
            errorMessage = backErrorMessage,
            onCameraClick = { launchCamera(false) },
            onGalleryClick = { launchGallery(false) },
            onDeleteClick = { onBackImageSelected("") }
        )
    }
}

@Composable
private fun IdUploadCard(
    title: String,
    imageUri: String,
    isError: Boolean,
    errorMessage: String,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = if (isError) MaterialTheme.colorScheme.error else Gray700
            ),
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (isError) MaterialTheme.colorScheme.error else Gray300
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (imageUri.isNotEmpty()) Color.Transparent else Gray50
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri.isNotEmpty()) {
                    // Show uploaded image
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Delete button
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(20.dp)
                                )
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete image",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    // Show upload options
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add image",
                            tint = Gray400,
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Text(
                            text = "Upload Image",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray600,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Camera button
                            OutlinedButton(
                                onClick = onCameraClick,
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Blue500
                                ),
                                border = BorderStroke(1.dp, Blue500)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Camera",
                                    fontSize = 12.sp
                                )
                            }
                            
                            // Gallery button
                            OutlinedButton(
                                onClick = onGalleryClick,
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Blue500
                                ),
                                border = BorderStroke(1.dp, Blue500)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Gallery",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Gallery",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Error message
        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
