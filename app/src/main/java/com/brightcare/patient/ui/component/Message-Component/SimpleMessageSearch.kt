package com.brightcare.patient.ui.component.messagecomponent

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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brightcare.patient.ui.theme.*

/**
 * Simple search bar component for messages
 * Simple search bar component para sa mga mensahe
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleMessageSearch(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search conversations...",
    enabled: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = Gray500,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (enabled) Blue500 else Gray400,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchQueryChange("") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = Gray500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue500,
            unfocusedBorderColor = Gray300,
            cursorColor = Blue500,
            focusedTextColor = Gray900,
            unfocusedTextColor = Gray700,
            disabledBorderColor = Gray200,
            disabledTextColor = Gray400
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
            }
        ),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Compact search bar with icon button trigger
 * Compact search bar na may icon button trigger
 */
@Composable
fun CompactMessageSearch(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isExpanded) {
            SimpleMessageSearch(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = {
                    onExpandedChange(false)
                    onSearchQueryChange("")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close search",
                    tint = Gray600
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(
                onClick = { onExpandedChange(true) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Blue50
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Open search",
                    tint = Blue500
                )
            }
        }
    }
}

/**
 * Search with quick filters
 * Search na may mga quick filter
 */
@Composable
fun QuickFilterSearch(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: SenderType?,
    onFilterChange: (SenderType?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Search bar
        SimpleMessageSearch(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange
        )
    }
}

/**
 * Preview for SimpleMessageSearch
 */
@Preview(showBackground = true)
@Composable
fun SimpleMessageSearchPreview() {
    var searchQuery by remember { mutableStateOf("") }
    
    BrightCarePatientTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SimpleMessageSearch(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )
            
            SimpleMessageSearch(
                searchQuery = "Dr. Maria",
                onSearchQueryChange = { },
                enabled = false
            )
        }
    }
}

/**
 * Preview for CompactMessageSearch
 */
@Preview(showBackground = true)
@Composable
fun CompactMessageSearchPreview() {
    var searchQuery by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    
    BrightCarePatientTheme {
        CompactMessageSearch(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            isExpanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview for QuickFilterSearch
 */
@Preview(showBackground = true)
@Composable
fun QuickFilterSearchPreview() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<SenderType?>(null) }
    
    BrightCarePatientTheme {
        QuickFilterSearch(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedFilter = selectedFilter,
            onFilterChange = { selectedFilter = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}






















