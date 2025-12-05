package com.brightcare.patient.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.component.messagecomponent.MessageComponent
import com.brightcare.patient.ui.theme.BrightCarePatientTheme
import com.brightcare.patient.navigation.NavigationRoutes

/**
 * Message screen - Chat with healthcare providers
 * Screen para sa pakikipag-chat sa mga healthcare provider
 */
@Composable
fun MessageScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Use the MessageComponent with all functionality including search
    // Gamitin ang MessageComponent na may lahat ng functionality kasama ang search
    MessageComponent(
        navController = navController,
        modifier = modifier,
        onConversationClick = { conversationId ->
            // Navigate to conversation screen
            // Pumunta sa conversation screen
            navController.navigate(NavigationRoutes.conversation(conversationId))
        }
    )
}


@Preview(
    showBackground = true,
    name = "Message Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun MessageScreenPreview() {
    BrightCarePatientTheme {
        MessageScreen(
            navController = rememberNavController()
        )
    }
}
