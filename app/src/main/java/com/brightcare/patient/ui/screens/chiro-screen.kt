package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.component.chirocomponents.ChiropractorInfo
import com.brightcare.patient.ui.component.chirocomponents.ChiropractorCard
import com.brightcare.patient.ui.component.chirocomponents.ChiroHeader
import com.brightcare.patient.ui.theme.*

/**
 * Chiro screen - Find and connect with chiropractors
 */
@Composable
fun ChiroScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Sample data for chiropractors
    val chiropractors = remember {
        listOf(
            ChiropractorInfo(
                id = "1",
                name = "Dr. Maria Santos",
                specialization = "Spinal Adjustment",
                experience = "8 years",
                rating = 4.8f,
                location = "Makati City",
                isAvailable = true
            ),
            ChiropractorInfo(
                id = "2",
                name = "Dr. John Reyes",
                specialization = "Sports Injury",
                experience = "12 years",
                rating = 4.9f,
                location = "Quezon City",
                isAvailable = false
            ),
            ChiropractorInfo(
                id = "3",
                name = "Dr. Ana Cruz",
                specialization = "Pediatric Chiropractic",
                experience = "6 years",
                rating = 4.7f,
                location = "Manila",
                isAvailable = true
            ),
            ChiropractorInfo(
                id = "4",
                name = "Dr. Michael Garcia",
                specialization = "Pain Management",
                experience = "15 years",
                rating = 4.9f,
                location = "Pasig City",
                isAvailable = true
            )
        )
    }

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
            subtitle = "Connect with certified professionals",
            onSearchClick = { /* Search functionality */ }
        )

        // Chiropractors list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chiropractors) { chiropractor ->
                ChiropractorCard(
                    chiropractor = chiropractor,
                    onBookClick = {
                        // Navigate to booking screen with chiropractor info
                        navController.navigate("booking/${chiropractor.id}")
                    },
                    onViewProfileClick = {
                        // Navigate to chiropractor profile
                        navController.navigate("chiro_profile/${chiropractor.id}")
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
        ChiroScreen(
            navController = rememberNavController()
        )
    }
}
