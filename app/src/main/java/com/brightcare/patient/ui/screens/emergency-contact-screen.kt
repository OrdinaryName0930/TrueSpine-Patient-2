package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brightcare.patient.data.model.EmergencyContact
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.EmergencyContactViewModel
import com.brightcare.patient.ui.component.complete_your_profile.CompleteProfileTextField
import com.brightcare.patient.ui.component.complete_your_profile.CompleteProfileDropdown
import com.brightcare.patient.ui.component.complete_your_profile.rememberAddressData
import com.brightcare.patient.ui.BrightCareToast
import com.brightcare.patient.ui.rememberToastState
import com.brightcare.patient.ui.showInfo
import com.brightcare.patient.ui.showError
import com.brightcare.patient.navigation.NavigationRoutes
import androidx.activity.compose.BackHandler
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

/**
 * Extension function to format display names
 */
private fun String.toDisplayName(): String =
    lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

/**
 * Emergency Contact screen - Manage emergency contact information
 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun EmergencyContactScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: EmergencyContactViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val toastState = rememberToastState()
    
    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadEmergencyContacts()
        }
    )
    
    // Handle system back button
    BackHandler {
        navController.navigate("${NavigationRoutes.MAIN_DASHBOARD}?initialRoute=profile") {
            popUpTo(NavigationRoutes.MAIN_DASHBOARD) { inclusive = false }
        }
    }
    
    // Handle refresh state - stop refreshing when data is loaded or error occurs
    LaunchedEffect(uiState.isLoading, uiState.errorMessage) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }
    
    // Handle success/error messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            toastState.showInfo(message)
            viewModel.clearSuccess()
        }
    }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            toastState.showError(message)
            viewModel.clearError()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp
                    )
                    .verticalScroll(scrollState)
            ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                onClick = { 
                    navController.navigate("${NavigationRoutes.MAIN_DASHBOARD}?initialRoute=profile") {
                        popUpTo(NavigationRoutes.MAIN_DASHBOARD) { inclusive = false }
                    }
                }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Gray600
                    )
                }
                
                Column {
                    Text(
                        text = "Emergency Contact",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Blue500,
                            fontSize = 28.sp
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content Area
            when {
                uiState.isLoading -> {
                    // Loading indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Blue500)
                    }
                }
                uiState.errorMessage != null -> {
                    // Error state - still show add button functionality
                    ErrorStateCard(
                        errorMessage = uiState.errorMessage!!,
                        onRetry = { viewModel.loadEmergencyContacts() }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Add Contact Button below error card (if less than 3 contacts)
                    if (uiState.emergencyContacts.size < 3) {
                        AddContactButton(
                            onClick = { viewModel.showAddContactDialog() }
                        )
                    }
                }
                uiState.emergencyContacts.isEmpty() -> {
                    // Empty state
                    EmptyStateCard()
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Add Contact Button below empty state card (if less than 3 contacts)
                    if (uiState.emergencyContacts.size < 3) {
                        AddContactButton(
                            onClick = { viewModel.showAddContactDialog() }
                        )
                    }
                }
                else -> {
                    // Emergency Contacts List
                    EmergencyContactsList(
                        contacts = uiState.emergencyContacts,
                        onEditClick = { contact -> viewModel.showEditContactDialog(contact) },
                        onDeleteClick = { contact -> viewModel.showDeleteConfirmation(contact) },
                        onSetPrimaryClick = { contactId -> viewModel.setPrimaryContact(contactId) },
                        isDeleting = uiState.isDeleting
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Add Contact Button below contacts list (if less than 3 contacts)
                    if (uiState.emergencyContacts.size < 3) {
                        AddContactButton(
                            onClick = { viewModel.showAddContactDialog() }
                        )
                    }
                }
            }
            
                Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for navigation
            }
            
            // Pull refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        
        // Add/Edit Contact Dialog
        if (uiState.showAddContactDialog) {
            AddEditContactDialog(
                isEditing = uiState.editingContact != null,
                formState = uiState.formState,
                viewModel = viewModel,
                onSave = { viewModel.saveEmergencyContact() },
                onDismiss = { viewModel.hideContactDialog() },
                isSaving = uiState.isSaving,
                isFormValid = viewModel.isFormValid()
            )
        }
        
        // Delete Confirmation Dialog
        if (uiState.showDeleteConfirmDialog && uiState.contactToDelete != null) {
            DeleteConfirmationDialog(
                contactName = uiState.contactToDelete!!.fullName,
                onConfirm = { viewModel.confirmDeleteEmergencyContact() },
                onDismiss = { viewModel.hideDeleteConfirmation() },
                isDeleting = uiState.isDeleting
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
}

@Composable
private fun ErrorStateCard(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Red50),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(15.dp),
                color = Red100
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Red500,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Error Loading Contacts",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Red700
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue500,
                    contentColor = White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = Blue50
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContactPhone,
                        contentDescription = null,
                        tint = Blue500,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "No Emergency Contacts",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Add up to 3 emergency contacts who can be reached in case of emergency.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gray600,
                    lineHeight = 20.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmergencyContactsList(
    contacts: List<EmergencyContact>,
    onEditClick: (EmergencyContact) -> Unit,
    onDeleteClick: (EmergencyContact) -> Unit,
    onSetPrimaryClick: (String) -> Unit,
    isDeleting: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Blue50),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Blue500,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "You can add up to 3 emergency contacts. Tap the star to set a primary contact.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Blue700,
                        lineHeight = 16.sp
                    )
                )
            }
        }
        
        // Contacts list
        contacts.forEach { contact ->
            EmergencyContactCard(
                contact = contact,
                onEditClick = { onEditClick(contact) },
                onDeleteClick = { onDeleteClick(contact) },
                onSetPrimaryClick = { onSetPrimaryClick(contact.id) },
                isDeleting = isDeleting
            )
        }
    }
}

@Composable
private fun EmergencyContactCard(
    contact: EmergencyContact,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSetPrimaryClick: () -> Unit,
    isDeleting: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (contact.isPrimary) Blue50 else White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left side - Name and relationship (avatar removed)
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = contact.fullName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Gray900,
                                    fontSize = 18.sp
                                ),
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                softWrap = true
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (contact.isPrimary) Blue100 else Gray100
                        ) {
                            Text(
                                text = contact.relationship,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (contact.isPrimary) Blue700 else Gray700,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Visible
                            )
                        }
                    }
                }

                // Right side - Minimized Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Primary toggle button (minimized)
                    Surface(
                        onClick = onSetPrimaryClick,
                        enabled = !contact.isPrimary,
                        shape = RoundedCornerShape(8.dp),
                        color = if (contact.isPrimary) Orange100 else Gray100,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (contact.isPrimary) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (contact.isPrimary) "Primary Contact" else "Set as Primary",
                                tint = if (contact.isPrimary) Orange600 else Gray500,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Edit button (minimized)
                    Surface(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(8.dp),
                        color = Blue100,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Contact",
                                tint = Blue600,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Delete button (minimized)
                    Surface(
                        onClick = onDeleteClick,
                        enabled = !isDeleting,
                        shape = RoundedCornerShape(8.dp),
                        color = Red100,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = Red600,
                                    strokeWidth = 1.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Contact",
                                    tint = Red600,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Primary Contact Badge
            if (contact.isPrimary) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Blue500,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Primary Emergency Contact",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Contact Information Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (contact.isPrimary) White else Gray50
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Phone number (always present)
                    EnhancedContactDetailRow(
                        icon = Icons.Default.Phone,
                        label = "Phone Number",
                        value = contact.phoneNumber,
                        iconColor = Green600,
                        iconBackgroundColor = Green100
                    )

                    // Email (if present)
                    if (contact.email.isNotBlank()) {
                        EnhancedContactDetailRow(
                            icon = Icons.Default.Email,
                            label = "Email Address",
                            value = contact.email,
                            iconColor = Blue600,
                            iconBackgroundColor = Blue100
                        )
                    }

                    // Address (if present)
                    if (contact.fullAddress.isNotBlank()) {
                        EnhancedContactDetailRow(
                            icon = Icons.Default.LocationOn,
                            label = "Address",
                            value = contact.fullAddress,
                            iconColor = Orange600,
                            iconBackgroundColor = Orange100,
                            isMultiLine = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gray500,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Gray600,
                fontWeight = FontWeight.Medium
            )
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Gray800
            )
        )
    }
}

@Composable
private fun EnhancedContactDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    iconBackgroundColor: Color,
    isMultiLine: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = if (isMultiLine) Alignment.Top else Alignment.CenterVertically
    ) {
        // Enhanced icon with background
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = iconBackgroundColor
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Gray600,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gray900,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                maxLines = if (isMultiLine) 3 else 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditContactDialog(
    isEditing: Boolean,
    formState: com.brightcare.patient.data.model.EmergencyContactFormState,
    viewModel: EmergencyContactViewModel,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    isSaving: Boolean,
    isFormValid: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditing) "Edit Emergency Contact" else "Add Emergency Contact",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Blue500
                            )
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Gray600
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Form fields in scrollable column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Name Section
                    Text(
                        text = "Name Information",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // First Name
                    CompleteProfileTextField(
                        value = formState.firstName,
                        onValueChange = { value ->
                            viewModel.updateFirstName(value)
                        },
                        placeholder = "*First Name",
                        isError = formState.isFirstNameError,
                        errorMessage = formState.firstNameErrorMessage
                    )
                    
                    // Middle Name
                    CompleteProfileTextField(
                        value = formState.middleName,
                        onValueChange = { value ->
                            viewModel.updateMiddleName(value)
                        },
                        placeholder = "Middle Name (Optional)",
                        isError = formState.isMiddleNameError,
                        errorMessage = formState.middleNameErrorMessage
                    )
                    
                    // Last Name
                    CompleteProfileTextField(
                        value = formState.lastName,
                        onValueChange = { value ->
                            viewModel.updateLastName(value)
                        },
                        placeholder = "*Last Name",
                        isError = formState.isLastNameError,
                        errorMessage = formState.lastNameErrorMessage
                    )
                    
                    // Suffix
                    CompleteProfileDropdown(
                        value = formState.suffix,
                        onValueChange = { value ->
                            viewModel.updateSuffix(value)
                        },
                        placeholder = "Suffix (Optional)",
                        options = listOf("Jr.", "Sr.", "II", "III", "IV", "V")
                    )
                    
                    CompleteProfileDropdown(
                        value = formState.relationship,
                        onValueChange = { value ->
                            viewModel.updateRelationship(value)
                        },
                        placeholder = "*Relationship",
                        options = EmergencyContact.relationshipOptions,
                        isError = formState.isRelationshipError,
                        errorMessage = formState.relationshipErrorMessage
                    )
                    
                    // Custom relationship field (only shown when "Other" is selected)
                    if (formState.relationship == "Other") {
                        CompleteProfileTextField(
                            value = formState.customRelationship,
                            onValueChange = { value ->
                                viewModel.updateCustomRelationship(value)
                            },
                            placeholder = "*Specify Relationship",
                            isError = formState.isCustomRelationshipError,
                            errorMessage = formState.customRelationshipErrorMessage
                        )
                    }
                    
                    CompleteProfileTextField(
                        value = formState.phoneNumber,
                        onValueChange = { value ->
                            viewModel.updatePhoneNumber(value)
                        },
                        placeholder = "*Phone Number (09XXXXXXXXX)",
                        keyboardType = KeyboardType.Phone,
                        isError = formState.isPhoneNumberError,
                        errorMessage = formState.phoneNumberErrorMessage
                    )
                    
                    CompleteProfileTextField(
                        value = formState.email,
                        onValueChange = { value ->
                            viewModel.updateEmail(value)
                        },
                        placeholder = "Email Address (Optional)",
                        keyboardType = KeyboardType.Email,
                        isError = formState.isEmailError,
                        errorMessage = formState.emailErrorMessage
                    )
                    
                    // Address Section
                    Text(
                        text = "Address Information",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Load address data for dropdowns
                    val addressData = rememberAddressData()
                    
                    // Province list
                    val provinces = addressData.provinces.map { it.toDisplayName() }
                    val selectedProvinceKey = addressData.provinces.find { it.toDisplayName() == formState.province } ?: ""
                    
                    // Municipality list for selected province
                    val municipalities = if (selectedProvinceKey.isNotEmpty()) {
                        addressData.municipalities[selectedProvinceKey]?.map { it.toDisplayName() } ?: emptyList()
                    } else emptyList()
                    val selectedMunicipalityKey = addressData.municipalities[selectedProvinceKey]?.find { it.toDisplayName() == formState.municipality } ?: ""
                    
                    // Barangay list for selected province+municipality
                    val barangays = if (selectedProvinceKey.isNotEmpty() && selectedMunicipalityKey.isNotEmpty()) {
                        addressData.barangays["$selectedProvinceKey-$selectedMunicipalityKey"]?.map { it.toDisplayName() } ?: emptyList()
                    } else emptyList()
                    
                    // Country field (read-only, default Philippines)
                    CompleteProfileTextField(
                        value = "Philippines",
                        onValueChange = { }, // No-op since it's read-only
                        placeholder = "Country",
                        enabled = false
                    )
                    
                    // Province dropdown
                    CompleteProfileDropdown(
                        value = formState.province,
                        onValueChange = { value ->
                            viewModel.updateProvince(value)
                        },
                        placeholder = "Select Province (Optional)",
                        options = provinces,
                        isError = formState.isProvinceError,
                        errorMessage = formState.provinceErrorMessage
                    )
                    
                    // Municipality dropdown
                    CompleteProfileDropdown(
                        value = formState.municipality,
                        onValueChange = { value ->
                            viewModel.updateMunicipality(value)
                        },
                        placeholder = "Select Municipality (Optional)",
                        options = municipalities,
                        isError = formState.isMunicipalityError,
                        errorMessage = formState.municipalityErrorMessage
                    )
                    
                    // Barangay dropdown
                    CompleteProfileDropdown(
                        value = formState.barangay,
                        onValueChange = { value ->
                            viewModel.updateBarangay(value)
                        },
                        placeholder = "Select Barangay (Optional)",
                        options = barangays,
                        isError = formState.isBarangayError,
                        errorMessage = formState.barangayErrorMessage
                    )
                    
                    // Additional Address
                    CompleteProfileTextField(
                        value = formState.additionalAddress,
                        onValueChange = { value ->
                            viewModel.updateAdditionalAddress(value)
                        },
                        placeholder = "Additional Address (Optional)",
                        singleLine = false,
                        isError = formState.isAdditionalAddressError,
                        errorMessage = formState.additionalAddressErrorMessage
                    )
                    
                    // Primary contact checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = formState.isPrimary,
                            onCheckedChange = { checked ->
                                viewModel.updatePrimaryStatus(checked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Blue500,
                                uncheckedColor = Gray400
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(2.dp))
                        
                        Column {
                            Text(
                                text = "Set as Primary Contact",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Gray800,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = Gray600)
                    }
                    
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving && isFormValid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue500,
                            contentColor = White,
                            disabledContainerColor = Gray300,
                            disabledContentColor = Gray600
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (isEditing) "Update" else "Add")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteConfirmationDialog(
    contactName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDeleting: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Contact",
                style = MaterialTheme.typography.headlineSmall,
                color = Blue500
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete this emergency contact?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "\"$contactName\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Gray700
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Red700
                )
            }
        },
                confirmButton = {
                    TextButton(
                        onClick = onConfirm,
                        enabled = !isDeleting,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Error
                        )
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Error,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isDeleting) "Deleting..." else "Delete")
                    }
                },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Gray600
                )
            ) {
                Text("Cancel")
            }
        },
        tonalElevation = 8.dp,
        titleContentColor = Gray900,
        textContentColor = Gray700
    )
}

@Composable
private fun AddContactButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Blue500,
            contentColor = White
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Emergency Contact",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Preview(
    showBackground = true,
    name = "Emergency Contact Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun EmergencyContactScreenPreview() {
    BrightCarePatientTheme {
        EmergencyContactScreen(
            navController = rememberNavController()
        )
    }
}
