package com.brightcare.patient.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.screens.ChatScreen
import com.brightcare.patient.ui.theme.BrightCarePatientTheme

/**
 * Conversation Screen - Individual chat conversation using ChatScreen
 * Screen para sa individual na chat conversation gamit ang ChatScreen
 */
@Composable
fun ConversationScreen(
    conversationId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Use the ChatScreen for individual conversations
    // Gamitin ang ChatScreen para sa individual conversations
    ChatScreen(
        conversationId = conversationId,
        navController = navController,
        modifier = modifier
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







