package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.brightcare.patient.data.model.Chiropractor
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.ChiropractorSearchViewModel

/**
 * Chiropractor search screen
 * Screen para sa paghahanap ng chiropractor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChiropractorSearchScreen(
    navController: NavController,
    onChiropractorSelected: (Chiropractor) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChiropractorSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val topRatedChiropractors by viewModel.topRatedChiropractors.collectAsState()
    val specializations by viewModel.specializations.collectAsState()
    val selectedSpecialization by viewModel.selectedSpecialization.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        // Header with search
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title and back button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back / Bumalik",
                            tint = Blue500
                        )
                    }
                    
                    Text(
                        text = "Find Chiropractors",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Gray900
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (uiState.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Blue500,
                            strokeWidth = 2.dp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search by name or specialization...",
                            color = Gray500
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Gray500
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = viewModel::clearSearch
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Gray500
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = Gray300,
                        cursorColor = Blue500
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }
        
        // Show error if any
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = viewModel::clearError
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Specialization filters
            if (specializations.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Specializations",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Gray900
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // All specializations chip
                            item {
                                FilterChip(
                                    onClick = { viewModel.selectSpecialization(null) },
                                    label = { Text("All") },
                                    selected = selectedSpecialization == null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Blue500,
                                        selectedLabelColor = White
                                    )
                                )
                            }
                            
                            items(specializations) { specialization ->
                                FilterChip(
                                    onClick = { viewModel.selectSpecialization(specialization) },
                                    label = { Text(specialization) },
                                    selected = selectedSpecialization == specialization,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Blue500,
                                        selectedLabelColor = White
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // Top rated chiropractors (show when no search query)
            if (searchQuery.isEmpty() && topRatedChiropractors.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Top Rated Chiropractors",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Gray900
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(topRatedChiropractors) { chiropractor ->
                                TopRatedChiropractorCard(
                                    chiropractor = chiropractor,
                                    onClick = { onChiropractorSelected(chiropractor) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Search results
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "All Chiropractors" else "Search Results",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Gray900
                        )
                    )
                    
                    Text(
                        text = "${searchResults.size} found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                }
            }
            
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Blue500)
                    }
                }
            } else if (searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Gray400
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No chiropractors found",
                                style = MaterialTheme.typography.titleMedium,
                                color = Gray600
                            )
                            Text(
                                text = "Try adjusting your search criteria",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray500
                            )
                        }
                    }
                }
            } else {
                items(searchResults) { chiropractor ->
                    ChiropractorCard(
                        chiropractor = chiropractor,
                        onClick = { onChiropractorSelected(chiropractor) }
                    )
                }
            }
        }
    }
}

/**
 * Top rated chiropractor card for horizontal scroll
 * Card para sa top rated chiropractor sa horizontal scroll
 */
@Composable
private fun TopRatedChiropractorCard(
    chiropractor: Chiropractor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile image
            AsyncImage(
                model = chiropractor.photoUrl,
                contentDescription = chiropractor.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Gray100),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = chiropractor.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Gray900
            )
            
            Text(
                text = chiropractor.specialization,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFB300)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", chiropractor.rating),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray700
                )
            }
        }
    }
}

/**
 * Full chiropractor card for search results
 * Kumpletong chiropractor card para sa search results
 */
@Composable
private fun ChiropractorCard(
    chiropractor: Chiropractor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            AsyncImage(
                model = chiropractor.photoUrl,
                contentDescription = chiropractor.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Gray100),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chiropractor.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900
                    )
                )
                
                Text(
                    text = chiropractor.specialization,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Blue500
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFB300)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", chiropractor.rating),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray700
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${chiropractor.reviewCount} reviews)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
                
                if (chiropractor.experience > 0) {
                    Text(
                        text = "${chiropractor.experience} years experience",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Availability indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (chiropractor.isAvailable) Color(0xFF4CAF50) else Gray400,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (chiropractor.isAvailable) "Available" else "Busy",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (chiropractor.isAvailable) Color(0xFF4CAF50) else Gray500
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Gray400
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChiropractorSearchScreenPreview() {
    BrightCarePatientTheme {
        ChiropractorSearchScreen(
            navController = rememberNavController()
        )
    }
}







