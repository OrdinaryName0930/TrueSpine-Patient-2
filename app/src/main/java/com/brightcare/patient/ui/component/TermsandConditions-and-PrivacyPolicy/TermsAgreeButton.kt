package com.brightcare.patient.ui.component.termsandconditions_and_privacypolicy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brightcare.patient.ui.theme.*

@Composable
fun TermsAgreeButton(
    text: String = "I Agree",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val containerColor = if (enabled) Blue500 else Blue500.copy(alpha = 0.5f)
    val contentColor = if (enabled) White else White.copy(alpha = 0.6f)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TermsAgreeButtonPreview() {
    BrightCarePatientTheme {
        Surface(
            color = WhiteBg,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Default Agree Button",
                    style = MaterialTheme.typography.titleMedium
                )
                TermsAgreeButton(
                    onClick = { /* Preview action */ }
                )
                
                Text(
                    text = "Loading State",
                    style = MaterialTheme.typography.titleMedium
                )
                TermsAgreeButton(
                    onClick = { /* Preview action */ },
                    loading = true
                )
                
                Text(
                    text = "Disabled State",
                    style = MaterialTheme.typography.titleMedium
                )
                TermsAgreeButton(
                    onClick = { /* Preview action */ },
                    enabled = false
                )
                
                Text(
                    text = "Custom Text",
                    style = MaterialTheme.typography.titleMedium
                )
                TermsAgreeButton(
                    text = "Sumang-ayon",
                    onClick = { /* Preview action */ }
                )
            }
        }
    }
}
