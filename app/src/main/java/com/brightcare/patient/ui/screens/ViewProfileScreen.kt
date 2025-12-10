package com.brightcare.patient.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.brightcare.patient.ui.component.profile.*
import com.brightcare.patient.ui.viewmodel.ViewProfileViewModel

/**
 * View Profile Screen for displaying chiropractor profile information
 * Screen para sa pagpapakita ng profile information ng chiropractor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewProfileScreen(
    navController: NavController,
    chiropractorId: String,
    modifier: Modifier = Modifier,
    viewModel: ViewProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsStateWithLifecycle()
    val tabLoadingStates by viewModel.tabLoadingStates.collectAsStateWithLifecycle()
    
    // Load chiropractor profile when screen loads
    LaunchedEffect(chiropractorId) {
        viewModel.loadChiropractorProfile(chiropractorId)
    }
    
    // Load tab data when tab changes
    LaunchedEffect(selectedTabIndex, chiropractorId) {
        if (uiState.hasData) {
            viewModel.loadTabData(chiropractorId, selectedTabIndex)
        }
    }
    
    val tabs = listOf(
        TabItem("Overview"),
        TabItem("Education"),
        TabItem("Experience"),
        TabItem("Credentials"),
        TabItem("Others")
    )
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
            .verticalScroll(scrollState)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    // Navigate back to chiro screen reliably
                    // Maaasahang pagbabalik sa chiro screen
                    println("🔙 Back button clicked in ViewProfileScreen - navigating to chiro screen")
                    
                    try {
                        // First try to pop back to chiro screen if it's in the back stack
                        // Una, subukang mag-pop back sa chiro screen kung nasa back stack
                        val popSuccess = navController.popBackStack(NavigationRoutes.CHIRO, inclusive = false)
                        
                        if (!popSuccess) {
                            // If chiro screen is not in back stack, navigate to main dashboard with chiro tab
                            // Kung walang chiro screen sa back stack, mag-navigate sa main dashboard na may chiro tab
                            navController.navigate(NavigationRoutes.MAIN_DASHBOARD + "?initialRoute=chiro") {
                                popUpTo(NavigationRoutes.MAIN_DASHBOARD) { inclusive = true }
                                launchSingleTop = true
                                restoreState = false
                            }
                            println("🔙 Navigated to main dashboard with chiro tab")
                        } else {
                            println("🔙 Successfully popped back to chiro screen")
                        }
                    } catch (e: Exception) {
                        println("🔙 Navigation error: ${e.message}")
                        
                        // Fallback: Navigate directly to chiro via main dashboard
                        // Fallback: Mag-navigate directly sa chiro via main dashboard
                        try {
                            navController.navigate(NavigationRoutes.MAIN_DASHBOARD) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                            println("🔙 Fallback navigation to main dashboard successful")
                        } catch (fallbackError: Exception) {
                            println("🔙 All navigation attempts failed: ${fallbackError.message}")
                            // Last resort: simple pop back stack
                            navController.popBackStack()
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back / Balik",
                    tint = Blue500
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Chiropractor Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Blue500
            )
        }
        
        // Handle different UI states
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue500)
                }
            }
            
            uiState.hasError -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                        text = "Error Loading Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Error
                    )
                    Text(
                        text = uiState.errorMessage ?: "Unknown error occurred",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.refreshProfile(chiropractorId) },
                        colors = ButtonDefaults.buttonColors(containerColor = Blue500)
                    ) {
                        Text("Retry")
                    }
                }
            }
            
            uiState.hasData -> {
                val chiropractor = uiState.chiropractor!!
                
                // Profile Header
                ProfileHeader(
                    chiropractor = chiropractor,
                    onBookNowClick = {
                        // Navigate to booking screen
                        // Mag-navigate sa booking screen
                        navController.navigate("book_appointment/$chiropractorId")
                    }
                )
                
                // Tab Row
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = White,
                    contentColor = Blue500,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Blue500
                        )
                    },
                    edgePadding = 16.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { 
                                viewModel.setSelectedTab(index)
                                viewModel.loadTabData(chiropractorId, index)
                            },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = tab.title,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                    
                                    // Show loading indicator for tab
                                    if (tabLoadingStates[index] == true) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 1.dp,
                                            color = if (selectedTabIndex == index) Blue500 else Gray400
                                        )
                                    }
                                }
                            },
                            selectedContentColor = Blue500,
                            unselectedContentColor = Gray600
                        )
                    }
                }
                
                // Tab Content in a scrollable container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // Show tab loading state
                    if (tabLoadingStates[selectedTabIndex] == true) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = Blue500)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Loading ${tabs[selectedTabIndex].title.lowercase()} data...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gray600
                                )
                            }
                        }
                    } else {
                        // Show tab content
                        when (selectedTabIndex) {
                            0 -> ScrollableOverviewTab(chiropractor)
                            1 -> {
                                val educationList = chiropractor.education.values.toList()
                                Log.d("ViewProfileScreen", "Education data count: ${educationList.size}")
                                ScrollableEducationTab(
                                    educationList = educationList,
                                    isLoading = tabLoadingStates[1] ?: false
                                )
                            }
                            2 -> {
                                val experienceList = chiropractor.experienceHistory.values.toList()
                                Log.d("ViewProfileScreen", "Experience data count: ${experienceList.size}")
                                ScrollableExperienceTab(
                                    experienceList = experienceList,
                                    isLoading = tabLoadingStates[2] ?: false
                                )
                            }
                            3 -> {
                                val credentialsList = chiropractor.professionalCredentials.values.toList()
                                Log.d("ViewProfileScreen", "Credentials data count: ${credentialsList.size}")
                                ScrollableCredentialsTab(
                                    credentialsList = credentialsList,
                                    isLoading = tabLoadingStates[3] ?: false
                                )
                            }
                            4 -> {
                                val othersList = chiropractor.others.values.toList()
                                Log.d("ViewProfileScreen", "Others data count: ${othersList.size}")
                                ScrollableOthersTab(
                                    othersList = othersList,
                                    isLoading = tabLoadingStates[4] ?: false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Profile header component
 * Component ng profile header
 */
@Composable
private fun ProfileHeader(
    chiropractor: ChiropractorProfileModel,
    onBookNowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(bottom = 20.dp, start = 16.dp, end = 16.dp, top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            AsyncImage(
                model = chiropractor.profileImageUrl.ifEmpty { "https://via.placeholder.com/120" },
                contentDescription = "Doctor Photo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Gray200),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name
            Text(
                text = chiropractor.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Blue500,
                textAlign = TextAlign.Center
            )
            
            // Service Hours
            if (chiropractor.serviceHours.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(
                            color = Blue50,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = chiropractor.serviceHours,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Blue500,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Years of Experience
            Text(
                text = "${chiropractor.yearsOfExperience} years of experience",
                style = MaterialTheme.typography.bodyLarge,
                color = Gray600,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            
            // PITAHC Accreditation Number
            if (chiropractor.pitahcAccreditationNumber.isNotEmpty()) {
                Text(
                    text = "PITAHC: ${chiropractor.pitahcAccreditationNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            
            // Started Practice
            Text(
                text = "Started practice in ${chiropractor.startYear}",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Book Now Button
            Button(
                onClick = onBookNowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue500,
                    contentColor = White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Book Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Info chip component
 * Component ng info chip
 */
@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        color = Blue50,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Blue500,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = Blue500,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Overview tab content
 * Content ng overview tab
 */
@Composable
private fun OverviewTab(chiropractor: ChiropractorProfileModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InfoCard(
                title = "About"
            ) {
                Text(
                    text = chiropractor.about.ifEmpty { "No information available" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
            }
        }
        
        item {
            InfoCard(
                title = "Contact Information"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContactItem(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = chiropractor.email
                    )
                    ContactItem(
                        icon = Icons.Default.Phone,
                        label = "Phone",
                        value = chiropractor.contactNumber
                    )
                    if (chiropractor.serviceHours.isNotEmpty()) {
                        ContactItem(
                            icon = Icons.Default.Schedule,
                            label = "Service Hours",
                            value = chiropractor.serviceHours
                        )
                    }
                }
            }
        }
        
        
        // Add bottom padding for scrolling
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Scrollable Overview tab content for use within scrollable parent
 * Scrollable na Overview tab content para sa scrollable parent
 */
@Composable
private fun ScrollableOverviewTab(chiropractor: ChiropractorProfileModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoCard(
            title = "About"
        ) {
            Text(
                text = chiropractor.about.ifEmpty { "No information available" },
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700
            )
        }
        
        InfoCard(
            title = "Contact Information"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ContactItem(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = chiropractor.email
                )
                ContactItem(
                    icon = Icons.Default.Phone,
                    label = "Phone",
                    value = chiropractor.contactNumber
                )
                if (chiropractor.serviceHours.isNotEmpty()) {
                    ContactItem(
                        icon = Icons.Default.Schedule,
                        label = "Service Hours",
                        value = chiropractor.serviceHours
                    )
                }
            }
        }
        
        // Add bottom padding for scrolling
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Scrollable Education tab content
 */
@Composable
private fun ScrollableEducationTab(
    educationList: List<EducationItem>,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading && educationList.isEmpty()) {
            // Show loading state for empty data
            repeat(2) {
                LoadingCard()
                if (it < 1) Spacer(modifier = Modifier.height(12.dp))
            }
        } else if (educationList.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.School,
                title = "No Education Information",
                message = "No educational background information available for this chiropractor."
            )
        } else {
            educationList.forEach { education ->
                EducationCard(education = education)
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Scrollable Experience tab content
 */
@Composable
private fun ScrollableExperienceTab(
    experienceList: List<ExperienceItem>,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading && experienceList.isEmpty()) {
            // Show loading state for empty data
            repeat(2) {
                LoadingCard()
                if (it < 1) Spacer(modifier = Modifier.height(12.dp))
            }
        } else if (experienceList.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Work,
                title = "No Experience Information",
                message = "No work experience information available for this chiropractor."
            )
        } else {
            experienceList.forEach { experience ->
                ExperienceCard(experience = experience)
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Scrollable Credentials tab content
 */
@Composable
private fun ScrollableCredentialsTab(
    credentialsList: List<ProfessionalCredentialItem>,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading && credentialsList.isEmpty()) {
            // Show loading state for empty data
            repeat(2) {
                LoadingCard()
                if (it < 1) Spacer(modifier = Modifier.height(12.dp))
            }
        } else if (credentialsList.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Badge,
                title = "No Credentials Information",
                message = "No professional credentials information available for this chiropractor."
            )
        } else {
            credentialsList.forEach { credential ->
                CredentialCard(credential = credential)
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Scrollable Others tab content
 */
@Composable
private fun ScrollableOthersTab(
    othersList: List<OtherItem>,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading && othersList.isEmpty()) {
            // Show loading state for empty data
            repeat(2) {
                LoadingCard()
                if (it < 1) Spacer(modifier = Modifier.height(12.dp))
            }
        } else if (othersList.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Star,
                title = "No Additional Information",
                message = "No additional achievements or information available for this chiropractor."
            )
        } else {
            othersList.forEach { other ->
                OtherCard(other = other)
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Contact item component
 * Component ng contact item
 */
@Composable
private fun ContactItem(
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
            tint = Blue500,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value.ifEmpty { "Not provided" },
                style = MaterialTheme.typography.bodyMedium,
                color = Gray800
            )
        }
    }
}

/**
 * Generic info card component
 * Generic na component ng info card
 */
@Composable
private fun InfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(bottom = 16.dp, start = 12.dp, end = 12.dp, top = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Blue500,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Tab item data class
 * Data class ng tab item
 */
private data class TabItem(
    val title: String
)

/**
 * Mock data function - replace with actual repository call
 * Mock data function - palitan ng tunay na repository call
 */
private fun getMockChiropractorProfile(): ChiropractorProfileModel {
    return ChiropractorProfileModel(
        role = "Doctor",
        name = "Dr. Vanessa Co Cruz",
        firstName = "Vanessa",
        lastName = "Cruz",
        middleName = "Co",
        suffix = "",
        specialization = "Spinal Adjustment Specialist",
        prcLicenseNumber = "1263738362828272",
        contactNumber = "09081234567",
        about = "Experienced chiropractor specializing in spinal adjustments and pain management with over 14 years of practice.",
        yearsOfExperience = 14,
        startYear = 2011,
        profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/truespine-e8576.firebasestorage.app/o/profile_images%2FoCxcARqFoRXmYOJ2ipm7xCZXf9p1%2F1765169538809.jpg?alt=media&token=48d29c2c-8fae-4201-ab40-032ff5e895a0",
        pitahcAccreditationNumber = "Cch 0040761",
        email = "dr.vanessacruz10@gmail.com",
        serviceHours = "Monday - Friday 10:00 am to 7:00 pm",
        education = mapOf(
            "eABxIxx7YRIQSTNSKdOu" to EducationItem(
                id = "eABxIxx7YRIQSTNSKdOu",
                institution = "Harvard University",
                degree = "Doctor of Chiropractic",
                description = "Comprehensive chiropractic education with focus on spinal health",
                startDate = "Dec 2005",
                endDate = "Dec 2010",
                current = false
            )
        ),
        experienceHistory = mapOf(
            "iNwYQAfhzuGZb5dLL9uM" to ExperienceItem(
                id = "iNwYQAfhzuGZb5dLL9uM",
                organization = "ChiroPrank",
                position = "Chiropractor",
                description = "Provided comprehensive chiropractic care and treatment",
                startDate = "Dec 2011",
                endDate = "Apr 2015",
                current = false
            )
        ),
        professionalCredentials = mapOf(
            "ga3mEDORhpiVv2xtcljN" to ProfessionalCredentialItem(
                id = "ga3mEDORhpiVv2xtcljN",
                title = "Best Palagutok Award",
                institution = "Chiro Organization",
                description = "Recognition for excellence in chiropractic practice",
                year = "2016",
                type = "Certificate",
                imageUrl = null
            )
        ),
        others = mapOf(
            "bk3fRM8v5nutfBPwoCt5" to OtherItem(
                id = "bk3fRM8v5nutfBPwoCt5",
                title = "Community Health Initiative",
                category = "Community Service",
                description = "Led community health awareness programs",
                date = "Dec 7, 2008"
            )
        )
    )
}

/**
 * Loading card component for tab content
 * Loading card component para sa tab content
 */
@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray50),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(bottom = 16.dp, start = 12.dp, end = 12.dp, top = 16.dp)
        ) {
            // Simulate loading content with placeholders
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .background(Gray200, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
                    .background(Gray200, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .background(Gray200, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ViewProfileScreenPreview() {
    BrightCarePatientTheme {
        ViewProfileScreen(
            navController = rememberNavController(),
            chiropractorId = "sample_id"
        )
    }
}
