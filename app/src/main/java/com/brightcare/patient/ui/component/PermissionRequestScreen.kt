package com.brightcare.patient.ui.component

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brightcare.patient.ui.theme.*

/**
 * Permission Request Screen
 * Asks for necessary permissions after onboarding
 * Humingi ng mga kinakailangang permission pagkatapos ng onboarding
 */
@Composable
fun PermissionRequestScreen(
    onPermissionsGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Track permission states
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    var storagePermissionGranted by remember { mutableStateOf(false) }
    
    // Permission launchers
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraPermissionGranted = isGranted
        checkAllPermissions(cameraPermissionGranted, storagePermissionGranted, onPermissionsGranted)
    }
    
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        storagePermissionGranted = isGranted
        checkAllPermissions(cameraPermissionGranted, storagePermissionGranted, onPermissionsGranted)
    }
    
    // Multiple permissions launcher (for requesting all at once)
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        cameraPermissionGranted = permissions[Manifest.permission.CAMERA] ?: false
        storagePermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        
        // For Android 13+, also check READ_MEDIA_IMAGES
        val mediaImagesGranted = permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: true
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            storagePermissionGranted = mediaImagesGranted
        }
        
        checkAllPermissions(cameraPermissionGranted, storagePermissionGranted, onPermissionsGranted)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App logo or icon placeholder
        Card(
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Blue500)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        // Title
        Text(
            text = "App Permissions",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Gray900
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Subtitle
        Text(
            text = "To provide you with the best experience, BrightCare needs access to your camera and photos for ID verification.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Gray600,
                lineHeight = 24.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Permission items
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PermissionItem(
                icon = Icons.Default.CameraAlt,
                title = "Camera Access",
                description = "Take photos of your ID for verification",
                isGranted = cameraPermissionGranted
            )
            
            PermissionItem(
                icon = Icons.Default.PhotoLibrary,
                title = "Photo Access",
                description = "Select ID photos from your gallery",
                isGranted = storagePermissionGranted
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Grant permissions button
        Button(
            onClick = {
                val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                } else {
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
                multiplePermissionsLauncher.launch(permissions)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue500,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Grant Permissions",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            )
        }
        
        // Skip button
        TextButton(
            onClick = onPermissionsGranted,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "Skip for now",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gray500,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        
        // Info text
        Text(
            text = "You can always grant these permissions later in your device settings.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Gray400,
                lineHeight = 18.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Green50 else Gray50
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isGranted) Green200 else Gray200
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Card(
                modifier = Modifier.size(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isGranted) Green500 else Blue500
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray600,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Status indicator
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Granted",
                    tint = Green500,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun checkAllPermissions(
    cameraGranted: Boolean,
    storageGranted: Boolean,
    onComplete: () -> Unit
) {
    if (cameraGranted && storageGranted) {
        onComplete()
    }
}












