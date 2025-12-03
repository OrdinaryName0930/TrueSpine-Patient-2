package com.brightcare.patient.ui.component.chiro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brightcare.patient.ui.theme.BrightCarePatientTheme

/**
 * Filter chips for chiropractor search
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChiroFilterChips(
    onAvailableNowClick: () -> Unit,
    onNearMeClick: () -> Unit,
    isAvailableNowSelected: Boolean = false,
    isNearMeSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            onClick = onAvailableNowClick,
            label = { Text("Available Now") },
            selected = isAvailableNowSelected,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
        
        FilterChip(
            onClick = onNearMeClick,
            label = { Text("Near Me") },
            selected = isNearMeSelected,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
    }
}

/**
 * Individual filter chip component for reusability
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChiroFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FilterChip(
        onClick = onClick,
        label = { Text(label) },
        selected = isSelected,
        leadingIcon = leadingIcon,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun ChiroFilterChipsPreview() {
    BrightCarePatientTheme {
        var availableSelected by remember { mutableStateOf(false) }
        var nearMeSelected by remember { mutableStateOf(false) }
        
        ChiroFilterChips(
            onAvailableNowClick = { availableSelected = !availableSelected },
            onNearMeClick = { nearMeSelected = !nearMeSelected },
            isAvailableNowSelected = availableSelected,
            isNearMeSelected = nearMeSelected
        )
    }
}