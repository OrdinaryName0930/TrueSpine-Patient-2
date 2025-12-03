package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.theme.*

data class ChiropractorInfo(
    val id: String,
    val name: String,
    val specialization: String,
    val experience: String,
    val rating: Float,
    val location: String,
    val isAvailable: Boolean
)

/**
 * Chiro screen - Find and connect with chiropractors
 */
@OptIn(ExperimentalMaterial3Api::class)
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
                start = 16.dp,
                top = 16.dp,
                end = 16.dp
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Find Chiropractors",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Blue500,
                        fontSize = 28.sp
                    )
                )
                Text(
                    text = "Connect with certified professionals",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray600
                    )
                )
            }
            
            IconButton(
                onClick = { /* Search functionality */ },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Blue50
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Blue500
                )
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                onClick = { /* Filter by availability */ },
                label = { Text("Available Now") },
                selected = false,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            
            FilterChip(
                onClick = { /* Filter by location */ },
                label = { Text("Near Me") },
                selected = false,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }

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

@Composable
private fun ChiropractorCard(
    chiropractor: ChiropractorInfo,
    onBookClick: () -> Unit,
    onViewProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chiropractor.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                    )
                    
                    Text(
                        text = chiropractor.specialization,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Blue500,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Orange500,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${chiropractor.rating}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray700,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Text(
                            text = " • ${chiropractor.experience}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray600
                            )
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Gray500,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = chiropractor.location,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray600
                            ),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                
                // Availability status
                Surface(
                    color = if (chiropractor.isAvailable) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (chiropractor.isAvailable) Success else Error)
                        )
                        Text(
                            text = if (chiropractor.isAvailable) "Available" else "Busy",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (chiropractor.isAvailable) Success else Error,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewProfileClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Blue500
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Blue500
                    )
                ) {
                    Text("View Profile")
                }
                
                Button(
                    onClick = onBookClick,
                    modifier = Modifier.weight(1f),
                    enabled = chiropractor.isAvailable,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue500,
                        contentColor = White,
                        disabledContainerColor = Gray300,
                        disabledContentColor = Gray500
                    )
                ) {
                    Text("Book Now")
                }
            }
        }
    }
}

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
