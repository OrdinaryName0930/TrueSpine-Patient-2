package com.brightcare.patient.ui.component.messagecomponent

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
 * Header component for messages screen
 * Header component para sa messages screen
 */
@Composable
fun MessageHeader(
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Messages",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Blue500,
                        fontSize = 28.sp
                    )
                )
                Text(
                    text = "Chat with your healthcare team",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray600
                    )
                )
            }
        }
    }
}

/**
 * Preview for MessageHeader
 */
@Preview(showBackground = true)
@Composable
fun MessageHeaderPreview() {
    BrightCarePatientTheme {
        MessageHeader(
            onSearchClick = { /* Preview action */ }
        )
    }
}
