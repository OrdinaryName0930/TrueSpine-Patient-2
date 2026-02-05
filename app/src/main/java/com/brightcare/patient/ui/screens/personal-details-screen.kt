package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.max
import kotlin.math.min
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.CompleteProfileViewModel
import com.brightcare.patient.ui.component.complete_your_profile.CompleteProfileTextField
import com.brightcare.patient.ui.component.complete_your_profile.BirthDateTextField
import com.brightcare.patient.ui.component.complete_your_profile.CompleteProfileDropdown
import com.brightcare.patient.ui.component.complete_your_profile.rememberAddressDataOnce
import com.brightcare.patient.ui.component.complete_your_profile.IdUploadComponent
import com.brightcare.patient.ui.BrightCareToast
import com.brightcare.patient.ui.rememberToastState
import com.brightcare.patient.ui.showInfo
import com.brightcare.patient.ui.showError
import com.brightcare.patient.ui.screens.CompleteProfileFormState
import com.brightcare.patient.ui.screens.AuthenticationViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar
import com.brightcare.patient.ui.component.signup_component.ValidationUtils
import com.brightcare.patient.navigation.NavigationRoutes
import androidx.activity.compose.BackHandler

data class PersonalInfoItem(
    val label: String,
    val value: String,
    val icon: ImageVector
)

enum class PersonalDetailFieldType {
    FIRST_NAME,
    LAST_NAME,
    SUFFIX,
    BIRTH_DATE,
    SEX,
    PHONE_NUMBER,
    PROVINCE,
    MUNICIPALITY,
    BARANGAY,
    ADDITIONAL_ADDRESS
}

/**
 * Format birth date from "yyyy-MM-dd" to "MMMM dd, yyyy" format
 * Mag-format ng birth date mula sa "yyyy-MM-dd" patungo sa "MMMM dd, yyyy" format
 */
private fun formatBirthDate(birthDate: String): String {
    if (birthDate.isBlank()) return "Not specified"
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(birthDate)
        date?.let { outputFormat.format(it) } ?: "Not specified"
    } catch (e: Exception) {
        // If parsing fails, try to handle other possible formats
        try {
            val inputFormat2 = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val date = inputFormat2.parse(birthDate)
            date?.let { outputFormat.format(it) } ?: "Not specified"
        } catch (e2: Exception) {
            birthDate // Return original if all parsing fails
        }
    }
}

/**
 * Combine address fields into complete address format
 * Pagsamahin ang mga address field sa kumpletong address format
 */
private fun formatCompleteAddress(
    additionalAddress: String,
    barangay: String,
    municipality: String,
    province: String,
    country: String = "Philippines"
): String {
    val addressParts = mutableListOf<String>()
    
    if (additionalAddress.isNotBlank()) {
        addressParts.add(additionalAddress)
    }
    if (barangay.isNotBlank()) {
        addressParts.add(barangay)
    }
    if (municipality.isNotBlank()) {
        addressParts.add(municipality)
    }
    if (province.isNotBlank()) {
        addressParts.add(province)
    }
    if (country.isNotBlank()) {
        addressParts.add(country)
    }
    
    return if (addressParts.isNotEmpty()) {
        addressParts.joinToString(", ")
    } else {
        "Not specified"
    }
}

/**
 * Calculate age from birth date
 * Kalkulahin ang edad mula sa birth date
 */
private fun calculateAge(birthDate: String): String {
    if (birthDate.isBlank()) return "Not specified"
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(birthDate)
        
        date?.let {
            val today = Calendar.getInstance()
            val birthCal = Calendar.getInstance()
            birthCal.time = it
            
            var age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            
            // Adjust age if birthday hasn't occurred this year yet
            if (today.get(Calendar.MONTH) < birthCal.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH) && 
                 today.get(Calendar.DAY_OF_MONTH) < birthCal.get(Calendar.DAY_OF_MONTH))) {
                age--
            }
            
            "$age years old"
        } ?: "Not specified"
    } catch (e: Exception) {
        // Try alternative format
        try {
            val inputFormat2 = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val date = inputFormat2.parse(birthDate)
            
            date?.let {
                val today = Calendar.getInstance()
                val birthCal = Calendar.getInstance()
                birthCal.time = it
                
                var age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
                
                if (today.get(Calendar.MONTH) < birthCal.get(Calendar.MONTH) ||
                    (today.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH) && 
                     today.get(Calendar.DAY_OF_MONTH) < birthCal.get(Calendar.DAY_OF_MONTH))) {
                    age--
                }
                
                "$age years old"
            } ?: "Not specified"
        } catch (e2: Exception) {
            "Not specified"
        }
    }
}

/**
 * Format name with proper capitalization and spacing
 * Mag-format ng pangalan na may tamang capitalization at spacing
 */
private fun formatName(name: String): String {
    if (name.isBlank()) return name
    
    // Allow letters and spaces only
    val filtered = name.filter { it.isLetter() || it == ' ' }
    
    // Collapse multiple spaces
    val collapsed = filtered.replace(Regex("\\s{2,}"), " ")
    
    // Capitalize each word
    return collapsed.lowercase().split(" ").joinToString(" ") { 
        it.replaceFirstChar { c -> c.uppercase() } 
    }
}

/**
 * Format phone number with proper validation
 * Mag-format ng phone number na may tamang validation
 */
private fun formatPhoneNumber(phone: String): String {
    var cleanValue = ""
    
    phone.forEachIndexed { index, char ->
        if (!char.isDigit()) return@forEachIndexed
        
        when (index) {
            0 -> if (char == '0') cleanValue += char
            1 -> if (char == '9') cleanValue += char
            in 2..10 -> cleanValue += char
        }
    }
    
    // Limit to 11 digits
    if (cleanValue.length > 11) cleanValue = cleanValue.take(11)
    
    return cleanValue
}

/**
 * Format additional address with proper validation
 * Mag-format ng additional address na may tamang validation
 */
private fun formatAdditionalAddress(address: String): String {
    if (address.isBlank()) return address
    
    // Allow letters, numbers, ñ/Ñ, spaces, and punctuation
    val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZñÑ0123456789 ,.#'-/"
    val filtered = address.filter { it in allowedChars }
    
    // Collapse multiple spaces
    val collapsed = filtered.replace(Regex("\\s{2,}"), " ")
    
    return collapsed
}

/**
 * Format address field names to display format (capitalize each word)
 * Mag-format ng address field names sa display format (capitalize each word)
 */
private fun String.toDisplayName(): String =
    lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

/**
 * Personal Details screen - View and edit user profile information
 * Personal Details screen - Tingnan at i-edit ang user profile information
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun PersonalDetailsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CompleteProfileViewModel = hiltViewModel(),
    authViewModel: AuthenticationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val toastState = rememberToastState()
    
    // Handle system back button
    BackHandler {
        navController.navigate("${NavigationRoutes.MAIN_DASHBOARD}?initialRoute=profile") {
            popUpTo(NavigationRoutes.MAIN_DASHBOARD) { inclusive = false }
        }
    }
    
    // Get user email from authentication
    val userEmail by authViewModel.currentUserEmail.collectAsState()
    
    var showEditModal by remember { mutableStateOf(false) }
    var showEditIdModal by remember { mutableStateOf(false) }
    var showImageViewer by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }
    var selectedImageTitle by remember { mutableStateOf("") }
    
    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadExistingProfile()
        }
    )
    
    // Load profile data when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadExistingProfile()
    }
    
    // Handle refresh state - stop refreshing when data is loaded or error occurs
    LaunchedEffect(uiState.isLoading, uiState.errorMessage) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }
    
    // Handle success/error messages
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            toastState.showInfo("Profile updated successfully!")
            viewModel.resetSuccessState()
            showEditModal = false
            showEditIdModal = false
            // Reload profile data to show updated information
            viewModel.loadExistingProfile()
        }
    }
    
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            toastState.showError(errorMessage)
            viewModel.clearError()
        }
    }
    
    if (uiState.isLoading && !isRefreshing) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Blue500)
        }
        return
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
                    .padding(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp
                    )
                    .verticalScroll(scrollState)
            ) {
        // Header (similar to ChiroHeader)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
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
                        text = "Personal Details",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Blue500,
                            fontSize = 28.sp
                        )
                    )
                }
            }

            IconButton(
                onClick = { showEditModal = true },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Blue50
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = Blue500
                )
            }
        }
        
        
        // Profile Information Cards
        PersonalInfoSection(
            title = "Basic Information",
            items = listOf(
                PersonalInfoItem("Full Name", "${uiState.formState.firstName}${if (uiState.formState.middleName.isNotBlank()) " ${uiState.formState.middleName}" else ""} ${uiState.formState.lastName}${if (uiState.formState.suffix.isNotBlank()) " ${uiState.formState.suffix}" else ""}", Icons.Default.Person),
                PersonalInfoItem("Birth Date", formatBirthDate(uiState.formState.birthDate), Icons.Default.DateRange),
                PersonalInfoItem("Age", calculateAge(uiState.formState.birthDate), Icons.Default.Cake),
                PersonalInfoItem("Sex", uiState.formState.sex.ifBlank { "Not specified" }, Icons.Default.Wc)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PersonalInfoSection(
            title = "Contact Information",
            items = listOf(
                PersonalInfoItem("Email Address", userEmail ?: "Not specified", Icons.Default.Email),
                PersonalInfoItem("Phone Number", uiState.formState.phoneNumber.ifBlank { "Not specified" }, Icons.Default.Phone)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PersonalInfoSection(
            title = "Address Information",
            items = listOf(
                PersonalInfoItem(
                    "Country", 
                    uiState.formState.country.ifBlank { "Philippines" }, 
                    Icons.Default.Public
                ),
                PersonalInfoItem(
                    "Complete Address", 
                    formatCompleteAddress(
                        additionalAddress = uiState.formState.additionalAddress,
                        barangay = uiState.formState.barangay,
                        municipality = uiState.formState.municipality,
                        province = uiState.formState.province
                    ), 
                    Icons.Default.LocationOn
                )
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // ID Documents Section
        IdDocumentsSection(
            idFrontUrl = uiState.formState.idFrontImageUrl,
            idBackUrl = uiState.formState.idBackImageUrl,
            onEditClick = { showEditIdModal = true },
            onViewFrontImage = { url ->
                selectedImageUrl = url
                selectedImageTitle = "ID Front"
                showImageViewer = true
            },
            onViewBackImage = { url ->
                selectedImageUrl = url
                selectedImageTitle = "ID Back"
                showImageViewer = true
            }
        )
        
                Spacer(modifier = Modifier.height(32.dp)) // Bottom padding
            }
            
            // Pull refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    
    
    // Edit Modal
    if (showEditModal) {
        EditProfileModal(
            currentFormState = uiState.formState,
            onDismiss = { showEditModal = false },
            onSave = { updatedFormState ->
                viewModel.updateFormState { updatedFormState }
                viewModel.updateProfile()
            },
            isLoading = uiState.isSaving
        )
    }
    
    // Edit ID Documents Modal
    if (showEditIdModal) {
        EditIdDocumentsModal(
            currentFormState = uiState.formState,
            onDismiss = { showEditIdModal = false },
            onSave = { updatedFormState ->
                viewModel.updateFormState { updatedFormState }
                viewModel.updateProfile()
            },
            isLoading = uiState.isSaving
        )
    }
    
    // Image Viewer Dialog
    if (showImageViewer && selectedImageUrl.isNotBlank()) {
        ImageViewerDialog(
            imageUrl = selectedImageUrl,
            imageTitle = selectedImageTitle,
            onDismiss = { 
                showImageViewer = false
                selectedImageUrl = ""
                selectedImageTitle = ""
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
private fun PersonalInfoSection(
    title: String,
    items: List<PersonalInfoItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Gray800
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    PersonalInfoRow(
                        item = item,
                        showDivider = index < items.size - 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalInfoRow(
    item: PersonalInfoItem,
    showDivider: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = Blue50
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = Blue500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Gray600,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Gray900,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        if (showDivider) {
            HorizontalDivider(
                color = Gray100,
                thickness = 1.dp,
                modifier = Modifier.padding(start = 76.dp, end = 20.dp)
            )
        }
    }
}


@Composable
private fun IdDocumentsSection(
    idFrontUrl: String,
    idBackUrl: String,
    onEditClick: () -> Unit,
    onViewFrontImage: (String) -> Unit = {},
    onViewBackImage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ID Documents",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )
            )
            
            IconButton(
                onClick = onEditClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Blue50
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit ID Documents",
                    tint = Blue500,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // ID Front
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Blue50
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Blue500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ID Front",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray600,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        
                        Text(
                            text = if (idFrontUrl.isNotBlank()) "Uploaded" else "Not uploaded",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (idFrontUrl.isNotBlank()) Blue500 else Gray500,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    
                    if (idFrontUrl.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .size(100.dp, 70.dp)
                                .clickable { onViewFrontImage(idFrontUrl) },
                            shape = RoundedCornerShape(8.dp),
                            color = Gray100
                        ) {
                            Box {
                                AsyncImage(
                                    model = idFrontUrl,
                                    contentDescription = "ID Front",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                                // Overlay to indicate clickable
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp),
                                    shape = CircleShape,
                                    color = Blue500.copy(alpha = 0.8f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "View Image",
                                        tint = White,
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider(
                    color = Gray100,
                    thickness = 1.dp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ID Back
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Blue50
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Blue500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ID Back",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray600,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        
                        Text(
                            text = if (idBackUrl.isNotBlank()) "Uploaded" else "Not uploaded",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (idBackUrl.isNotBlank()) Blue500 else Gray500,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    
                    if (idBackUrl.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .size(100.dp, 70.dp)
                                .clickable { onViewBackImage(idBackUrl) },
                            shape = RoundedCornerShape(8.dp),
                            color = Gray100
                        ) {
                            Box {
                                AsyncImage(
                                    model = idBackUrl,
                                    contentDescription = "ID Back",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                                // Overlay to indicate clickable
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp),
                                    shape = CircleShape,
                                    color = Blue500.copy(alpha = 0.8f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "View Image",
                                        tint = White,
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileModal(
    currentFormState: CompleteProfileFormState,
    onDismiss: () -> Unit,
    onSave: (CompleteProfileFormState) -> Unit,
    isLoading: Boolean
) {
    var editedFormState by remember { mutableStateOf(currentFormState) }
    
    // Validation states
    var firstNameError by remember { mutableStateOf("") }
    var middleNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf("") }
    var birthDateError by remember { mutableStateOf("") }
    var sexError by remember { mutableStateOf("") }
    var provinceError by remember { mutableStateOf("") }
    var municipalityError by remember { mutableStateOf("") }
    var additionalAddressError by remember { mutableStateOf("") }
    
    // Load address data for dropdowns
    val addressData = rememberAddressDataOnce()
    
    // Validation functions
    val validateFirstName = {
        when {
            editedFormState.firstName.trim().isBlank() -> {
                firstNameError = "First name is required"
                false
            }
            !ValidationUtils.isValidName(editedFormState.firstName.trim()) -> {
                firstNameError = "First name must be at least 2 characters and contain only letters"
                false
            }
            else -> {
                firstNameError = ""
                true
            }
        }
    }
    
    val validateMiddleName = {
        // Middle name is optional, only validate if not blank
        if (editedFormState.middleName.trim().isNotBlank()) {
            if (!ValidationUtils.isValidName(editedFormState.middleName.trim())) {
                middleNameError = "Middle name must be at least 2 characters and contain only letters"
                false
            } else {
                middleNameError = ""
                true
            }
        } else {
            middleNameError = ""
            true
        }
    }
    
    val validateLastName = {
        when {
            editedFormState.lastName.trim().isBlank() -> {
                lastNameError = "Last name is required"
                false
            }
            !ValidationUtils.isValidName(editedFormState.lastName.trim()) -> {
                lastNameError = "Last name must be at least 2 characters and contain only letters"
                false
            }
            else -> {
                lastNameError = ""
                true
            }
        }
    }
    
    val validatePhoneNumber = {
        when {
            editedFormState.phoneNumber.isBlank() -> {
                phoneNumberError = "Phone number is required"
                false
            }
            !ValidationUtils.isValidPhoneNumber(editedFormState.phoneNumber) -> {
                phoneNumberError = "Phone number must start with 09 and have 11 digits"
                false
            }
            else -> {
                phoneNumberError = ""
                true
            }
        }
    }
    
    val validateAdditionalAddress = {
        if (editedFormState.additionalAddress.trim().isNotBlank()) {
            if (!ValidationUtils.isValidAdditionalAddress(editedFormState.additionalAddress.trim())) {
                additionalAddressError = "Additional address must be at least 3 characters long and may only contain letters, numbers, spaces, and basic punctuation (,.#'-/)."
                false
            } else {
                additionalAddressError = ""
                true
            }
        } else {
            additionalAddressError = ""
            true
        }
    }
    
    val validateRequiredFields = {
        val isFirstNameValid = validateFirstName()
        val isMiddleNameValid = validateMiddleName()
        val isLastNameValid = validateLastName()
        val isPhoneValid = validatePhoneNumber()
        val isAdditionalAddressValid = validateAdditionalAddress()
        
        birthDateError = if (editedFormState.birthDate.isBlank()) "Birth date is required" else ""
        sexError = if (editedFormState.sex.isBlank()) "Sex is required" else ""
        provinceError = if (editedFormState.province.isBlank()) "Province is required" else ""
        municipalityError = if (editedFormState.municipality.isBlank()) "Municipality is required" else ""
        
        isFirstNameValid && isMiddleNameValid && isLastNameValid && isPhoneValid && isAdditionalAddressValid &&
        editedFormState.birthDate.isNotBlank() && editedFormState.sex.isNotBlank() &&
        editedFormState.province.isNotBlank() && editedFormState.municipality.isNotBlank()
    }
    
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
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Blue500
                        )
                    )
                    
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
                    // Basic Information Section
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Gray800
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    CompleteProfileTextField(
                        value = editedFormState.firstName,
                        onValueChange = { value ->
                            val formattedValue = formatName(value)
                            editedFormState = editedFormState.copy(firstName = formattedValue)
                            validateFirstName()
                        },
                        placeholder = "*First Name",
                        isError = firstNameError.isNotEmpty(),
                        errorMessage = firstNameError
                    )
                    
                    CompleteProfileTextField(
                        value = editedFormState.middleName,
                        onValueChange = { value ->
                            val formattedValue = formatName(value)
                            editedFormState = editedFormState.copy(middleName = formattedValue)
                            validateMiddleName()
                        },
                        placeholder = "Middle Name (Optional)",
                        isError = middleNameError.isNotEmpty(),
                        errorMessage = middleNameError
                    )
                    
                    CompleteProfileTextField(
                        value = editedFormState.lastName,
                        onValueChange = { value ->
                            val formattedValue = formatName(value)
                            editedFormState = editedFormState.copy(lastName = formattedValue)
                            validateLastName()
                        },
                        placeholder = "*Last Name",
                        isError = lastNameError.isNotEmpty(),
                        errorMessage = lastNameError
                    )
                    
                    CompleteProfileDropdown(
                        value = editedFormState.suffix,
                        onValueChange = { value -> 
                            editedFormState = editedFormState.copy(suffix = if (value == "None") "" else value)
                        },
                        placeholder = "Suffix",
                        options = listOf("None", "Jr.", "Sr.", "III", "IV", "V", "VI")
                    )
                    
                    BirthDateTextField(
                        birthDate = editedFormState.birthDate,
                        onBirthDateChange = { 
                            editedFormState = editedFormState.copy(birthDate = it)
                            birthDateError = ""
                        },
                        isError = birthDateError.isNotEmpty(),
                        errorMessage = birthDateError
                    )
                    
                    CompleteProfileDropdown(
                        value = editedFormState.sex,
                        onValueChange = { 
                            editedFormState = editedFormState.copy(sex = it)
                            sexError = ""
                        },
                        placeholder = "*Select Sex",
                        options = listOf("Male", "Female"),
                        isError = sexError.isNotEmpty(),
                        errorMessage = sexError
                    )
                    
                    // Contact Information Section
                    Text(
                        text = "Contact Information",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Gray800
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    CompleteProfileTextField(
                        value = editedFormState.phoneNumber,
                        onValueChange = { value ->
                            val formattedValue = formatPhoneNumber(value)
                            editedFormState = editedFormState.copy(phoneNumber = formattedValue)
                            validatePhoneNumber()
                        },
                        placeholder = "*Phone Number (09XXXXXXXXX)",
                        keyboardType = KeyboardType.Phone,
                        isError = phoneNumberError.isNotEmpty(),
                        errorMessage = phoneNumberError
                    )
                    
                    // Address Information Section
                    Text(
                        text = "Address Information",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Gray800
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Country field (read-only, default Philippines)
                    CompleteProfileTextField(
                        value = "Philippines",
                        onValueChange = { }, // No-op since it's read-only
                        placeholder = "Country",
                        enabled = false
                    )
                    
                    addressData?.let { data ->
                        // Province dropdown with formatted options
                        val provinces = data.provinces.map { it.toDisplayName() }
                        val selectedProvinceKey = data.provinces.find { it.toDisplayName() == editedFormState.province } ?: ""
                        
                        CompleteProfileDropdown(
                            value = editedFormState.province,
                            onValueChange = { value ->
                                editedFormState = editedFormState.copy(
                                    province = value,
                                    municipality = "", // Reset dependent fields
                                    barangay = ""
                                )
                                provinceError = ""
                            },
                            placeholder = "*Select Province",
                            options = provinces,
                            isError = provinceError.isNotEmpty(),
                            errorMessage = provinceError
                        )
                        
                        // Municipality dropdown with formatted options
                        val municipalities = if (selectedProvinceKey.isNotEmpty()) {
                            data.municipalities[selectedProvinceKey]?.map { it.toDisplayName() } ?: emptyList()
                        } else emptyList()
                        val selectedMunicipalityKey = data.municipalities[selectedProvinceKey]?.find { it.toDisplayName() == editedFormState.municipality } ?: ""
                        
                        CompleteProfileDropdown(
                            value = editedFormState.municipality,
                            onValueChange = { value ->
                                editedFormState = editedFormState.copy(
                                    municipality = value,
                                    barangay = "" // Reset dependent field
                                )
                                municipalityError = ""
                            },
                            placeholder = "*Select Municipality",
                            options = municipalities,
                            isError = municipalityError.isNotEmpty(),
                            errorMessage = municipalityError,
                            enabled = editedFormState.province.isNotEmpty()
                        )
                        
                        // Barangay dropdown with formatted options
                        val barangays = if (selectedProvinceKey.isNotEmpty() && selectedMunicipalityKey.isNotEmpty()) {
                            data.barangays["$selectedProvinceKey-$selectedMunicipalityKey"]?.map { it.toDisplayName() } ?: emptyList()
                        } else emptyList()
                        
                        CompleteProfileDropdown(
                            value = editedFormState.barangay,
                            onValueChange = { value ->
                                editedFormState = editedFormState.copy(barangay = value)
                            },
                            placeholder = "Select Barangay",
                            options = barangays,
                            enabled = editedFormState.municipality.isNotEmpty()
                        )
                    }
                    
                    CompleteProfileTextField(
                        value = editedFormState.additionalAddress,
                        onValueChange = { value ->
                            val formattedValue = formatAdditionalAddress(value)
                            editedFormState = editedFormState.copy(additionalAddress = formattedValue)
                            validateAdditionalAddress()
                        },
                        placeholder = "Additional Address",
                        singleLine = false,
                        isError = additionalAddressError.isNotEmpty(),
                        errorMessage = additionalAddressError
                    )
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
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = Gray600)
                    }
                    
                    Button(
                        onClick = {
                            if (validateRequiredFields()) {
                                onSave(editedFormState)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && validateRequiredFields(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue500,
                            contentColor = White,
                            disabledContainerColor = Gray300,
                            disabledContentColor = Gray600
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Edit")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditIdDocumentsModal(
    currentFormState: CompleteProfileFormState,
    onDismiss: () -> Unit,
    onSave: (CompleteProfileFormState) -> Unit,
    isLoading: Boolean
) {
    var editedFormState by remember { mutableStateOf(currentFormState) }
    
    // Validation states
    var idFrontError by remember { mutableStateOf("") }
    var idBackError by remember { mutableStateOf("") }
    
    // Validation functions
    val validateIdDocuments = {
        idFrontError = if (editedFormState.idFrontImageUri.isBlank()) "Front ID image is required" else ""
        idBackError = if (editedFormState.idBackImageUri.isBlank()) "Back ID image is required" else ""
        
        editedFormState.idFrontImageUri.isNotBlank() && editedFormState.idBackImageUri.isNotBlank()
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
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
                    Text(
                        text = "Edit ID Documents",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Blue500
                        )
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Gray600
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ID Upload Component
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IdUploadComponent(
                        frontImageUri = editedFormState.idFrontImageUri,
                        backImageUri = editedFormState.idBackImageUri,
                        onFrontImageSelected = { uri ->
                            editedFormState = editedFormState.copy(idFrontImageUri = uri)
                            idFrontError = ""
                        },
                        onBackImageSelected = { uri ->
                            editedFormState = editedFormState.copy(idBackImageUri = uri)
                            idBackError = ""
                        },
                        isFrontError = idFrontError.isNotEmpty(),
                        isBackError = idBackError.isNotEmpty(),
                        frontErrorMessage = idFrontError,
                        backErrorMessage = idBackError
                    )
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
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = Gray600)
                    }
                    
                    Button(
                        onClick = {
                            if (validateIdDocuments()) {
                                onSave(editedFormState)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && validateIdDocuments(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue500,
                            contentColor = White,
                            disabledContainerColor = Gray300,
                            disabledContentColor = Gray600
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Edit")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageViewerDialog(
    imageUrl: String,
    imageTitle: String,
    onDismiss: () -> Unit
) {
    // State for zoom and pan
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    // Maximum zoom level (3x like Facebook Messenger)
    val maxScale = 3f
    val minScale = 1f
    
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
        
        // Only allow panning when zoomed in
        val newOffset = if (newScale > minScale) {
            // Limit pan to reasonable bounds when zoomed
            val maxOffsetX = (newScale - 1f) * 200f
            val maxOffsetY = (newScale - 1f) * 200f
            
            Offset(
                x = (offset.x + offsetChange.x).coerceIn(-maxOffsetX, maxOffsetX),
                y = (offset.y + offsetChange.y).coerceIn(-maxOffsetY, maxOffsetY)
            )
        } else {
            // Reset offset when zoomed out to original size
            Offset.Zero
        }
        
        scale = newScale
        offset = newOffset
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = imageTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Gray800
                        )
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Gray100
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Gray800
                        )
                    }
                }
                
                // Image container with zoom functionality
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = imageTitle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 600.dp)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                                .transformable(state = transformableState)
                                .pointerInput(Unit) {
                                    // Double tap to zoom in/out like Facebook Messenger
                                    detectTapGestures(
                                        onDoubleTap = { tapOffset ->
                                            if (scale > minScale) {
                                                // If zoomed in, zoom out to original size
                                                scale = minScale
                                                offset = Offset.Zero
                                            } else {
                                                // If at original size, zoom in to 2x at tap location
                                                scale = 2f
                                                // Calculate offset to center on tap location
                                                val centerX = size.width / 2f
                                                val centerY = size.height / 2f
                                                offset = Offset(
                                                    x = (centerX - tapOffset.x) * 0.5f,
                                                    y = (centerY - tapOffset.y) * 0.5f
                                                )
                                            }
                                        }
                                    )
                                },
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Personal Details Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun PersonalDetailsScreenPreview() {
    BrightCarePatientTheme {
        PersonalDetailsScreen(
            navController = rememberNavController()
        )
    }
}
