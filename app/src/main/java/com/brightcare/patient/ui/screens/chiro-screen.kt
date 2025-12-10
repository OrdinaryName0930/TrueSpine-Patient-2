package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.ui.component.chirocomponents.ChiropractorInfo
import com.brightcare.patient.ui.component.chirocomponents.ChiropractorCard
import com.brightcare.patient.ui.component.chirocomponents.ChiroHeader
import com.brightcare.patient.ui.component.messagecomponent.SimpleMessageSearch
import com.brightcare.patient.ui.viewmodel.ChiropractorViewModel
import com.brightcare.patient.ui.theme.*

/**
 * Chiro screen - Find and connect with chiropractors
 * Now fetches data from Firestore via ViewModel
 */
@Composable
fun ChiroScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ChiropractorViewModel = hiltViewModel()
) {
    // Collect state from ViewModel
    val chiropractors by viewModel.chiropractors.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val showAvailableOnly by viewModel.showAvailableOnly.collectAsStateWithLifecycle()
    val showNearMeOnly by viewModel.showNearMeOnly.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
            .padding(
                start = 12.dp,
                top = 16.dp,
                end = 12.dp
            )
    ) {
        // Header
        ChiroHeader(
            title = "Find Chiropractors",
            subtitle = "Meet our licensed chiropractors",
            onSearchClick = { 
                // TODO: Implement search functionality
                // For now, just refresh the data
                viewModel.refresh()
            }
        )
        
        // Search bar
        SimpleMessageSearch(
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            placeholder = "Search chiropractors...",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Content based on loading state
        when {
            isLoading -> {
                // Loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Blue500
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading chiropractors...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Gray600
                            )
                        )
                    }
                }
            }
            
            errorMessage != null -> {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading chiropractors",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Error,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = errorMessage ?: "Unknown error occurred",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Gray600
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Blue500
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            chiropractors.isEmpty() -> {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No chiropractors found" else "No chiropractors available",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Gray600,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try adjusting your search terms" else "Please try again later or adjust your filters",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Gray500
                            )
                        )
                    }
                }
            }
            
            else -> {
                // Chiropractors list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chiropractors) { chiropractor ->
                        ChiropractorCard(
                            chiropractor = chiropractor,
                            onBookClick = {
                                // Navigate to book appointment activity
                                navController.navigate("book_appointment/${chiropractor.id}")
                            },
                            onViewProfileClick = {
                                // Navigate to chiropractor profile
                                navController.navigate(NavigationRoutes.viewProfile(chiropractor.id))
                            }
                        )
                    }
                    
                    // Add some bottom padding
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

// ChiropractorCard component has been moved to Chiro-Components folder for reusability

@Preview(
    showBackground = true,
    name = "Chiro Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun ChiroScreenPreview() {
    BrightCarePatientTheme {
        // Preview with sample data since we can't use ViewModel in preview
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteBg)
                .padding(
                    start = 12.dp,
                    top = 16.dp,
                    end = 12.dp
                )
        ) {
            ChiroHeader(
                title = "Find Chiropractors",
                subtitle = "Meet our licensed chiropractors",
                onSearchClick = { }
            )
            
            // Search bar preview
            SimpleMessageSearch(
                searchQuery = "",
                onSearchQueryChange = { },
                placeholder = "Search chiropractors...",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Sample preview data
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ChiropractorCard(
                        chiropractor = ChiropractorInfo(
                            id = "1",
                            name = "Dr. Joshua Miller",
                            specialization = "Pediatric & Family Care",
                            experience = "8 years",
                            rating = 4.8f,
                            location = "Makati City",
                            isAvailable = true,
                            yearsOfExperience = 8,
                            reviewCount = 45,
                            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/truespine-e8576.firebasestorage.app/o/profile_images%2FGHkvU5c8c4SZHqK63HwJ18TDvEZ2%2F1765126305341.jpg?alt=media&token=2c69eae4-1f8d-4bf7-bc87-74fdbde77507"
                        ),
                        onBookClick = { },
                        onViewProfileClick = { }
                    )
                }
            }
        }
    }
}
