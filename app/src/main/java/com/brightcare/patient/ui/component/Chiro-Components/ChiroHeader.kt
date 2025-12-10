package com.brightcare.patient.ui.component.chiro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brightcare.patient.ui.theme.*

/**
 * Reusable header component for chiro-related screens
 */
@Composable
fun ChiroHeader(
    title: String,
    subtitle: String,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Blue500,
                    fontSize = 28.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gray600
                )
            )
        }


    }
}

@Preview(showBackground = true)
@Composable
fun ChiroHeaderPreview() {
    BrightCarePatientTheme {
        ChiroHeader(
            title = "Find Chiropractors",
            subtitle = "Connect with certified professionals",
            onSearchClick = { }
        )
    }
}