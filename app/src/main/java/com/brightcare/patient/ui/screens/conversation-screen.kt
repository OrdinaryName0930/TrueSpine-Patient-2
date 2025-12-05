package com.brightcare.patient.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.component.conversationcomponent.ConversationComponent
import com.brightcare.patient.ui.theme.BrightCarePatientTheme

/**
 * Conversation Screen - Individual chat conversation using ConversationComponent
 * Screen para sa individual na chat conversation gamit ang ConversationComponent
 */
@Composable
fun ConversationScreen(
    conversationId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Use the new ConversationComponent with all functionality
    // Gamitin ang bagong ConversationComponent na may lahat ng functionality
    ConversationComponent(
        conversationId = conversationId,
        navController = navController,
        modifier = modifier,
        onBackClick = { navController.popBackStack() }
    )
}


/**
 * Preview for ConversationScreen
 */
@Preview(
    showBackground = true,
    name = "Conversation Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun ConversationScreenPreview() {
    BrightCarePatientTheme {
        ConversationScreen(
            conversationId = "1",
            navController = rememberNavController()
        )
    }
}
