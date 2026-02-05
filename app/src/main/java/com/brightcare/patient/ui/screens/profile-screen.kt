package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.PatientSignInViewModel
import com.brightcare.patient.navigation.NavigationRoutes
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import android.net.Uri
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.ui.platform.LocalContext
import com.brightcare.patient.ui.viewmodel.CompleteProfileViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brightcare.patient.ui.BrightCareToast
import com.brightcare.patient.ui.rememberToastState
import com.brightcare.patient.ui.showInfo
import com.brightcare.patient.ui.showError

data class ProfileMenuItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val action: () -> Unit,
    val showArrow: Boolean = true,
    val textColor: Color? = null
)

/**
 * Profile screen - Manage account and settings
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    signInViewModel: PatientSignInViewModel = hiltViewModel(),
    authViewModel: AuthenticationViewModel = hiltViewModel(),
    profileViewModel: CompleteProfileViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val toastState = rememberToastState()
    
    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            profileViewModel.loadExistingProfile()
            authViewModel.refreshProfileData()
        }
    )
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showProfilePictureDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var tempProfilePictureUri by remember { mutableStateOf<Uri?>(null) }
    
    // Get real user data from AuthenticationViewModel
    val userName by authViewModel.currentUserName.collectAsState()
    val userEmail by authViewModel.currentUserEmail.collectAsState()
    val firstName by authViewModel.firstName.collectAsState()
    val middleName by authViewModel.middleName.collectAsState()
    val lastName by authViewModel.lastName.collectAsState()
    val suffix by authViewModel.suffix.collectAsState()
    val loginTimestamp = authViewModel.getLoginTimestamp()
    
    // Load profile data when screen is first displayed
    LaunchedEffect(Unit) {
        profileViewModel.loadExistingProfile()
    }
    
    // Handle profile picture upload success
    LaunchedEffect(profileUiState.isSuccess) {
        if (profileUiState.isSuccess) {
            toastState.showInfo("Profile picture updated successfully!")
            profileViewModel.resetSuccessState()
            // Close the confirmation dialog
            showConfirmationDialog = false
            tempProfilePictureUri = null
        }
    }
    
    // Handle profile picture upload errors
    profileUiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            toastState.showError(errorMessage)
            profileViewModel.clearError()
        }
    }
    
    // Camera and gallery launchers
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            tempProfilePictureUri = it
            showConfirmationDialog = true
        }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempImageUri?.let {
                tempProfilePictureUri = it
                showConfirmationDialog = true
            }
        }
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                // Create images directory in cache if it doesn't exist
                val imagesDir = File(context.cacheDir, "images")
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }
                
                // Create temporary file for camera
                val imageFile = File(imagesDir, "temp_profile_${System.currentTimeMillis()}.jpg")
                tempImageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    imageFile
                )
                tempImageUri?.let { cameraLauncher.launch(it) }
            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "Error creating camera file", e)
            }
        }
    }
    
    // Combine all name parts for display
    // I-combine ang lahat ng bahagi ng pangalan para sa display
    val displayName: String = remember(firstName, middleName, lastName, suffix, userName) {
        when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() -> {
                buildString {
                    append(firstName)
                    if (!middleName.isNullOrBlank()) {
                        append(" $middleName")
                    }
                    append(" $lastName")
                    if (!suffix.isNullOrBlank()) {
                        append(" $suffix")
                    }
                }
            }
            !userName.isNullOrBlank() -> userName ?: "User Name"
            else -> "User Name"
        }
    }
    
    // Refresh profile data when screen is displayed
    LaunchedEffect(Unit) {
        authViewModel.refreshProfileData()
    }
    
    // Handle refresh state - stop refreshing when data is loaded or error occurs
    LaunchedEffect(profileUiState.isLoading, profileUiState.errorMessage) {
        if (!profileUiState.isLoading) {
            isRefreshing = false
        }
    }
    
    // Format member since date
    val memberSince = remember(loginTimestamp) {
        if (loginTimestamp > 0) {
            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            "Member since ${dateFormat.format(Date(loginTimestamp))}"
        } else {
            "Member since recently"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
        // Header with user info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Blue500,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile picture
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable { showProfilePictureDialog = true },
                    shape = CircleShape,
                    color = White.copy(alpha = 0.2f)
                ) {
                    if (profileUiState.formState.profilePictureUrl.isNotBlank()) {
                        AsyncImage(
                            model = profileUiState.formState.profilePictureUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (profilePictureUri != null) {
                        AsyncImage(
                            model = profilePictureUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            tint = White,
                            modifier = Modifier
                                .size(40.dp)
                                .wrapContentSize(Alignment.Center)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = White
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = userEmail ?: "user@email.com",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = White.copy(alpha = 0.9f)
                    )
                )
                
                Text(
                    text = memberSince,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Edit profile picture button
                OutlinedButton(
                    onClick = { showProfilePictureDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile Picture")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Menu sections
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Personal Information Section
            ProfileSection(
                title = "Personal Information",
                items = listOf(
                    ProfileMenuItem(
                        title = "Personal Details",
                        subtitle = "Name, email, phone number",
                        icon = Icons.Default.Person,
                        action = { navController.navigate(NavigationRoutes.PERSONAL_DETAILS) }
                    ),
                    ProfileMenuItem(
                        title = "Emergency Contacts",
                        subtitle = "Family and emergency contacts",
                        icon = Icons.Default.ContactPhone,
                        action = { navController.navigate(NavigationRoutes.EMERGENCY_CONTACTS) }
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Terms & Privacy Policy Section
            ProfileSection(
                title = "Terms & Privacy Policy",
                items = listOf(
                    ProfileMenuItem(
                        title = "Terms & Conditions",
                        subtitle = "Read our terms of service",
                        icon = Icons.Default.Description,
                        action = { navController.navigate(NavigationRoutes.TERMS_AND_CONDITIONS) }
                    ),
                    ProfileMenuItem(
                        title = "Privacy Policy",
                        subtitle = "How we protect your data",
                        icon = Icons.Default.PrivacyTip,
                        action = { navController.navigate(NavigationRoutes.PRIVACY_POLICY) }
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Account Actions Section
            ProfileSection(
                title = "Account",
                items = listOf(
                    ProfileMenuItem(
                        title = "Change Password",
                        subtitle = "Update your account password",
                        icon = Icons.Default.Lock,
                        action = { navController.navigate(NavigationRoutes.CHANGE_PASSWORD) }
                    ),
                    ProfileMenuItem(
                        title = "Sign Out",
                        subtitle = "Sign out of your account",
                        icon = Icons.Default.ExitToApp,
                        action = { showLogoutDialog = true },
                        showArrow = false,
                        textColor = Error
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App version info
            Text(
                text = "",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Gray500
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for navigation
        }
        }
        
        // Pull refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of your account?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        // Use the logout functionality
                        authViewModel.logout()
                        signInViewModel.signOut()
                        navController.navigate(NavigationRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Error,
                        contentColor = White
                    )
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel", color = Gray500)
                }
            }
        )
    }
    
    // Profile Picture Upload Dialog
    if (showProfilePictureDialog) {
        ProfilePictureUploadDialog(
            onDismiss = { showProfilePictureDialog = false },
            onCameraClick = {
                showProfilePictureDialog = false
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onGalleryClick = {
                showProfilePictureDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }
    
    // Profile Picture Confirmation Dialog
    if (showConfirmationDialog && tempProfilePictureUri != null) {
        ProfilePictureConfirmationDialog(
            imageUri = tempProfilePictureUri!!,
            isUploading = profileUiState.isSaving,
            onDismiss = { 
                if (!profileUiState.isSaving) {
                    showConfirmationDialog = false
                    tempProfilePictureUri = null
                }
            },
            onSave = {
                tempProfilePictureUri?.let { uri ->
                    profileViewModel.uploadProfilePicture(uri.toString())
                }
            },
            onRetake = {
                if (!profileUiState.isSaving) {
                    showConfirmationDialog = false
                    tempProfilePictureUri = null
                    showProfilePictureDialog = true
                }
            }
        )
    }
    
    // Toast for messages
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        BrightCareToast(
            toastState = toastState,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ProfileSection(
    title: String,
    items: List<ProfileMenuItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Gray800
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    ProfileMenuItemRow(
                        item = item,
                        showDivider = index < items.size - 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuItemRow(
    item: ProfileMenuItem,
    showDivider: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Surface(
            onClick = item.action,
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.textColor ?: Blue500,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = item.textColor ?: Gray900
                        )
                    )
                    
                    item.subtitle?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray600
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                
                if (item.showArrow) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        if (showDivider) {
            Divider(
                color = Gray100,
                thickness = 1.dp,
                modifier = Modifier.padding(start = 56.dp)
            )
        }
    }
}

@Composable
private fun ProfilePictureUploadDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Update Profile Picture",
                color = Blue500,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose how you want to update your profile picture:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Camera Option
                OutlinedButton(
                    onClick = onCameraClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Photo")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Gallery Option
                OutlinedButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose from Gallery")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray600)
            }
        }
    )
}

@Composable
private fun ProfilePictureConfirmationDialog(
    imageUri: Uri,
    isUploading: Boolean = false,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onRetake: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Confirm Profile Picture",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Blue500
                    ),
                    textAlign = TextAlign.Start
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Preview Image
                Surface(
                    modifier = Modifier.size(200.dp),
                    shape = CircleShape,
                    color = Gray100
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Profile Picture Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Do you want to use this photo as your profile picture?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray400
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // First Row: Cancel and Retake
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = !isUploading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Gray600
                            )
                        ) {
                            Text("Cancel")
                        }
                        
                        // Retake Button
                        OutlinedButton(
                            onClick = onRetake,
                            modifier = Modifier.weight(1f),
                            enabled = !isUploading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Blue500
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retake")
                        }
                    }
                    
                    // Second Row: Save Button (Full Width)
                    Button(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUploading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue500,
                            contentColor = White
                        )
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Profile Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun ProfileScreenPreview() {
    BrightCarePatientTheme {
        ProfileScreen(
            navController = rememberNavController()
        )
    }
}



