package com.brightcare.patient.ui.component.chirocomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brightcare.patient.ui.theme.*

/**
 * Reusable chiropractor card component
 * Displays chiropractor information with action buttons
 * Layout matches the provided design reference
 */
@Composable
fun ChiropractorCard(
    chiropractor: ChiropractorInfo,
    onBookClick: () -> Unit,
    onViewProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Professional Doctor Badge and Heart (excluded as requested)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Professional Doctor Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = Blue50,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Professional Doctor",
                            tint = Blue500,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Licensed Chiropractor",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Blue500,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                // Heart icon excluded as requested
            }
            
            // Main Content Row: Image and Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Doctor Image (Left side)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gray100),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder for doctor image - using initials
                    Text(
                        text = chiropractor.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Blue500
                        )
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Doctor Info (Right side)
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Doctor Name
                    Text(
                        text = chiropractor.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Gray900,
                            fontSize = 18.sp
                        )
                    )
                    
                    // Specialization (replacing "Dentist" with specialization)
                    Text(
                        text = "${chiropractor.experience} of experience",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Gray600,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Rating and Experience Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stars
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < chiropractor.rating.toInt()) Orange500 else Gray300,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Rating number
                        Text(
                            text = "${chiropractor.rating}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Gray900
                            ),
                            modifier = Modifier.padding(start = 6.dp)
                        )

                        // Divider |
                        Text(
                            text = " | ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Gray600
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Reviews count (from Firestore data)
                        Text(
                            text = "${chiropractor.reviewCount} Reviews",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Gray400
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            ChiropractorCardActions(
                isAvailable = chiropractor.isAvailable,
                onBookClick = onBookClick,
                onViewProfileClick = onViewProfileClick
            )
        }
    }
}

@Composable
private fun ChiropractorCardActions(
    isAvailable: Boolean,
    onBookClick: () -> Unit,
    onViewProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Left: View Profile (Outlined)
        OutlinedButton(
            onClick = onViewProfileClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Blue500,
                containerColor = Color.Transparent
            ),
            border = BorderStroke(
                width = 1.dp,
                color = Blue500
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(
                text = "View Profile",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        // Right: Make Appointment (Primary)
        Button(
            onClick = onBookClick,
            modifier = Modifier.weight(1f),
            enabled = isAvailable,
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue500,
                contentColor = White,
                disabledContainerColor = Gray300,
                disabledContentColor = Gray500
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(
                text = if (isAvailable) "Book Now" else "Unavailable",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
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
                specialization = "Licensed Chiropractor",
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