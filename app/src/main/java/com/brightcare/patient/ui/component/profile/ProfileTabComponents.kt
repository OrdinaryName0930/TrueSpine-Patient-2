package com.brightcare.patient.ui.component.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.brightcare.patient.data.model.*
import com.brightcare.patient.ui.theme.*

/**
 * Education tab component
 * Component ng education tab
 */
@Composable
fun EducationTab(educationList: List<EducationItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (educationList.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.School,
                    title = "No Education Information",
                    message = "No educational background information available."
                )
            }
        } else {
            items(educationList) { education ->
                EducationCard(education = education)
            }
        }
    }
}

/**
 * Experience tab component
 * Component ng experience tab
 */
@Composable
fun ExperienceTab(experienceList: List<ExperienceItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (experienceList.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Work,
                    title = "No Experience Information",
                    message = "No work experience information available."
                )
            }
        } else {
            items(experienceList) { experience ->
                ExperienceCard(experience = experience)
            }
        }
    }
}

/**
 * Professional Credentials tab component
 * Component ng professional credentials tab
 */
@Composable
fun CredentialsTab(credentialsList: List<ProfessionalCredentialItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (credentialsList.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Badge,
                    title = "No Credentials Information",
                    message = "No professional credentials information available."
                )
            }
        } else {
            items(credentialsList) { credential ->
                CredentialCard(credential = credential)
            }
        }
    }
}

/**
 * Others tab component
 * Component ng others tab
 */
@Composable
fun OthersTab(othersList: List<OtherItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (othersList.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Star,
                    title = "No Additional Information",
                    message = "No additional achievements or information available."
                )
            }
        } else {
            items(othersList) { other ->
                OtherCard(other = other)
            }
        }
    }
}

/**
 * Education card component
 * Component ng education card
 */
@Composable
fun EducationCard(education: EducationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(bottom = 16.dp, start = 12.dp, end = 12.dp, top = 16.dp)
        ) {
            // Header with icon and degree
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Blue500,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = education.degree.ifEmpty { "Degree" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Blue500
                )
                
                if (education.current) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Green100,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Current",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Green700,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Institution
            Text(
                text = education.institution.ifEmpty { "Institution not specified" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            
            // Duration
            val duration = if (education.current) {
                "${education.startDate} - Present"
            } else {
                "${education.startDate} - ${education.endDate}"
            }
            
            Text(
                text = duration,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            
            // Description
            if (education.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = education.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
            }
        }
    }
}

/**
 * Experience card component
 * Component ng experience card
 */
@Composable
fun ExperienceCard(experience: ExperienceItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(bottom = 16.dp, start = 12.dp, end = 12.dp, top = 16.dp)
        ) {
            // Header with icon and position
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = Blue500,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = experience.position.ifEmpty { "Position" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Blue500
                )
                
                if (experience.current) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Green100,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Current",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Green700,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Organization
            Text(
                text = experience.organization.ifEmpty { "Organization not specified" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            
            // Duration
            val duration = if (experience.current) {
                "${experience.startDate} - Present"
            } else {
                "${experience.startDate} - ${experience.endDate}"
            }
            
            Text(
                text = duration,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            
            // Description
            if (experience.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = experience.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
            }
        }
    }
}

/**
 * Credential card component
 * Component ng credential card
 */
@Composable
fun CredentialCard(credential: ProfessionalCredentialItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(bottom = 16.dp, start = 12.dp, end = 12.dp, top = 16.dp)
        ) {
            // Header with icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = Blue500,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = credential.title.ifEmpty { "Credential" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Blue500
                    )
                    
                    // Type badge
                    if (credential.type.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Blue50,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = credential.type,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Blue700,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Year
                if (credential.year.isNotEmpty()) {
                    Text(
                        text = credential.year,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Institution
            Text(
                text = credential.institution.ifEmpty { "Institution not specified" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            
            // Description
            if (credential.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = credential.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
            }
            
            // Image if available
            credential.imageUrl?.let { imageUrl ->
                if (imageUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Credential Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Gray200),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

/**
 * Other card component
 * Component ng other card
 */
@Composable
fun OtherCard(other: OtherItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(bottom = 16.dp, start = 12.dp, end = 12.dp, top = 16.dp)
        ) {
            // Header with icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Blue500,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = other.title.ifEmpty { "Achievement" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Blue500
                    )
                    
                    // Category badge
                    if (other.category.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Orange50,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = other.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Orange700,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Date
                if (other.date.isNotEmpty()) {
                    Text(
                        text = other.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Description
            if (other.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = other.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
            }
        }
    }
}

/**
 * Empty state card component
 * Component ng empty state card
 */
@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Gray50),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 20.dp, end = 20.dp, top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray600,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                textAlign = TextAlign.Center
            )
        }
    }
}
