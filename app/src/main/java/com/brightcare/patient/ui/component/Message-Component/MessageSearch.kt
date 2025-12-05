package com.brightcare.patient.ui.component.messagecomponent

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brightcare.patient.ui.theme.*

/**
 * Search component for messages with expandable search bar
 * Search component para sa mga mensahe na may expandable search bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageSearch(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    onClearSearch: () -> Unit = { onSearchQueryChange("") },
    modifier: Modifier = Modifier,
    placeholder: String = "Search conversations..."
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-focus when search becomes active
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    AnimatedVisibility(
        visible = isSearchActive,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Icon
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Blue500,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Search TextField
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = Gray500,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = Gray300,
                        cursorColor = Blue500,
                        focusedTextColor = Gray900,
                        unfocusedTextColor = Gray700
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                        }
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Clear/Close buttons
                Row {
                    // Clear search button (only show when there's text)
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = onClearSearch,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = Gray500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    // Close search button
                    IconButton(
                        onClick = {
                            onSearchActiveChange(false)
                            onClearSearch()
                            keyboardController?.hide()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close search",
                            tint = Gray600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Search results summary component
 * Component para sa summary ng search results
 */
@Composable
fun SearchResultsSummary(
    searchQuery: String,
    resultCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    if (searchQuery.isNotEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Blue50
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Search results",
                    tint = Blue500,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = if (resultCount > 0) {
                        "Found $resultCount of $totalCount conversations for \"$searchQuery\""
                    } else {
                        "No conversations found for \"$searchQuery\""
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Blue700,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

/**
 * Enhanced search functionality with filters
 * Enhanced search functionality na may mga filter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedMessageSearch(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    selectedSenderType: SenderType?,
    onSenderTypeChange: (SenderType?) -> Unit,
    onClearSearch: () -> Unit = { 
        onSearchQueryChange("")
        onSenderTypeChange(null)
    },
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    AnimatedVisibility(
        visible = isSearchActive,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Search bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Blue500,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Search conversations...",
                                color = Gray500,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            unfocusedBorderColor = Gray300,
                            cursorColor = Blue500
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            onSearchActiveChange(false)
                            onClearSearch()
                            keyboardController?.hide()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close search",
                            tint = Gray600
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Filter:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Gray600,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    
                    // All filter chip
                    FilterChip(
                        onClick = { onSenderTypeChange(null) },
                        label = { Text("All") },
                        selected = selectedSenderType == null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue500,
                            selectedLabelColor = White
                        )
                    )
                    
                    // Doctors filter chip
                    FilterChip(
                        onClick = { onSenderTypeChange(SenderType.DOCTOR) },
                        label = { Text("Doctors") },
                        selected = selectedSenderType == SenderType.DOCTOR,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue500,
                            selectedLabelColor = White
                        )
                    )
                    
                    // Support filter chip
                    FilterChip(
                        onClick = { onSenderTypeChange(SenderType.ADMIN) },
                        label = { Text("Support") },
                        selected = selectedSenderType == SenderType.ADMIN,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue500,
                            selectedLabelColor = White
                        )
                    )
                }
                
                // Clear all button
                if (searchQuery.isNotEmpty() || selectedSenderType != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(
                        onClick = onClearSearch,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Blue500
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear all",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear all filters")
                    }
                }
            }
        }
    }
}

/**
 * Preview for MessageSearch
 */
@Preview(showBackground = true)
@Composable
fun MessageSearchPreview() {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(true) }
    
    BrightCarePatientTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhiteBg)
                .padding(16.dp)
        ) {
            MessageSearch(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                isSearchActive = isSearchActive,
                onSearchActiveChange = { isSearchActive = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SearchResultsSummary(
                searchQuery = "Dr. Maria",
                resultCount = 2,
                totalCount = 4
            )
        }
    }
}

/**
 * Preview for AdvancedMessageSearch
 */
@Preview(showBackground = true)
@Composable
fun AdvancedMessageSearchPreview() {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(true) }
    var selectedSenderType by remember { mutableStateOf<SenderType?>(null) }
    
    BrightCarePatientTheme {
        AdvancedMessageSearch(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            isSearchActive = isSearchActive,
            onSearchActiveChange = { isSearchActive = it },
            selectedSenderType = selectedSenderType,
            onSenderTypeChange = { selectedSenderType = it }
        )
    }
}
