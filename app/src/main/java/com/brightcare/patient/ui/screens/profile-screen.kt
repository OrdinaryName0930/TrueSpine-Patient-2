package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    signInViewModel: PatientSignInViewModel = hiltViewModel(),
    authViewModel: AuthenticationViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Sample user data - replace with actual user data from ViewModel
    val userName = "John Doe"
    val userEmail = "john.doe@email.com"
    val userPhone = "+63 912 345 6789"
    val memberSince = "Member since Jan 2024"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
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
                // Profile picture placeholder
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = White.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        tint = White,
                        modifier = Modifier
                            .size(40.dp)
                            .wrapContentSize(Alignment.Center)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                )
                
                Text(
                    text = userEmail,
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
                
                // Edit profile button
                OutlinedButton(
                    onClick = { 
                        navController.navigate("edit_profile")
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
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
                        action = { navController.navigate("personal_details") }
                    ),
                    ProfileMenuItem(
                        title = "Medical History",
                        subtitle = "Health records and conditions",
                        icon = Icons.Default.MedicalServices,
                        action = { navController.navigate("medical_history") }
                    ),
                    ProfileMenuItem(
                        title = "Emergency Contacts",
                        subtitle = "Family and emergency contacts",
                        icon = Icons.Default.ContactPhone,
                        action = { navController.navigate("emergency_contacts") }
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Preferences Section
            ProfileSection(
                title = "Preferences",
                items = listOf(
                    ProfileMenuItem(
                        title = "Notifications",
                        subtitle = "Manage your notification settings",
                        icon = Icons.Default.Notifications,
                        action = { navController.navigate("notifications_settings") }
                    ),
                    ProfileMenuItem(
                        title = "Privacy Settings",
                        subtitle = "Control your privacy preferences",
                        icon = Icons.Default.Security,
                        action = { navController.navigate("privacy_settings") }
                    ),
                    ProfileMenuItem(
                        title = "Language",
                        subtitle = "English",
                        icon = Icons.Default.Language,
                        action = { navController.navigate("language_settings") }
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Support Section
            ProfileSection(
                title = "Support & Legal",
                items = listOf(
                    ProfileMenuItem(
                        title = "Help Center",
                        subtitle = "FAQs and support articles",
                        icon = Icons.Default.Help,
                        action = { navController.navigate("help_center") }
                    ),
                    ProfileMenuItem(
                        title = "Contact Support",
                        subtitle = "Get help from our team",
                        icon = Icons.Default.Support,
                        action = { navController.navigate("contact_support") }
                    ),
                    ProfileMenuItem(
                        title = "Terms & Conditions",
                        subtitle = "Read our terms of service",
                        icon = Icons.Default.Description,
                        action = { navController.navigate("terms_conditions") }
                    ),
                    ProfileMenuItem(
                        title = "Privacy Policy",
                        subtitle = "How we protect your data",
                        icon = Icons.Default.PrivacyTip,
                        action = { navController.navigate("privacy_policy") }
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
                        action = { navController.navigate("change_password") }
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
                text = "BrightCare Patient v1.0.0",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Gray500
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for navigation
        }
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
                        // Use the new logout functionality
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
                    Text("Cancel", color = Gray600)
                }
            }
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
