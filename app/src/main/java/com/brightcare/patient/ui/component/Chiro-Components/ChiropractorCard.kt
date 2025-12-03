package com.brightcare.patient.ui.component.chiro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brightcare.patient.ui.theme.*

/**
 * Reusable chiropractor card component
 * Displays chiropractor information with action buttons
 */
@Composable
fun ChiropractorCard(
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
                    }
                }
                
                // Availability status
                ChiropractorAvailabilityBadge(isAvailable = chiropractor.isAvailable)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            ChiropractorCardActions(
                isAvailable = chiropractor.isAvailable,
                onBookClick = onBookClick,
                onViewProfileClick = onViewProfileClick
            )
        }
    }
}

/**
 * Availability badge component for chiropractor card
 */
@Composable
private fun ChiropractorAvailabilityBadge(
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isAvailable) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isAvailable) Success else Error)
            )
            Text(
                text = if (isAvailable) "Available" else "Busy",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isAvailable) Success else Error,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * Action buttons for chiropractor card
 */
@Composable
private fun ChiropractorCardActions(
    isAvailable: Boolean,
    onBookClick: () -> Unit,
    onViewProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onViewProfileClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Blue500
            ),
            border = BorderStroke(
                width = 1.dp,
                color = Blue500
            )
        ) {
            Text("View Profile")
        }
        
        Button(
            onClick = onBookClick,
            modifier = Modifier.weight(1f),
            enabled = isAvailable,
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

@Preview(showBackground = true)
@Composable
fun ChiropractorCardPreview() {
    BrightCarePatientTheme {
        ChiropractorCard(
            chiropractor = ChiropractorInfo(
                id = "1",
                name = "Dr. Maria Santos",
                specialization = "Spinal Adjustment",
                experience = "8 years",
                rating = 4.8f,
                location = "Makati City",
                isAvailable = true
            ),
            onBookClick = { },
            onViewProfileClick = { }
        )
    }
}