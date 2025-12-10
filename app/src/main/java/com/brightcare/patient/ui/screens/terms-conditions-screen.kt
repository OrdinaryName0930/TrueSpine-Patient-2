package com.brightcare.patient.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.navigation.NavigationRoutes

/**
 * Terms & Conditions screen - Display terms of service
 * Terms & Conditions screen - Ipakita ang terms of service
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsConditionsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Handle system back button
    BackHandler {
        navController.navigate("${NavigationRoutes.MAIN_DASHBOARD}?initialRoute=profile") {
            popUpTo(NavigationRoutes.MAIN_DASHBOARD) { inclusive = false }
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhiteBg)
                    .padding(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 10.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            navController.navigate("${NavigationRoutes.MAIN_DASHBOARD}?initialRoute=profile") {
                                popUpTo(NavigationRoutes.MAIN_DASHBOARD) { inclusive = false }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Gray600
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Terms & Conditions",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Blue500
                        )
                    )
                }
            }
        },
        containerColor = WhiteBg,
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = listState,
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 5.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Last Updated
            item {
                Text(
                    text = "Last updated: October 29, 2024",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray600,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }
            
            // Introduction
            item {
                TermsSection(
                    title = "1. Introduction",
                    content = "Welcome to BrightCare Patient App. These Terms and Conditions govern your use of our healthcare appointment scheduling and patient management application operated by BrightCare Medical Center.\n\nBy using our Service, you agree to follow and be bound by these Terms. If you do not agree, please discontinue use of the app immediately."
                )
            }
            
            // Acceptance of Terms
            item {
                TermsSection(
                    title = "2. Acceptance of Terms",
                    content = "By creating an account and using BrightCare, you acknowledge that you have read, understood, and agreed to these Terms and Conditions, as well as our Privacy Policy. Continued use of the Service constitutes acceptance of any updates to these Terms."
                )
            }
            
            // Use of Service
            item {
                TermsSection(
                    title = "3. Use of Service",
                    content = "Our Service enables you to:\n• Schedule medical appointments\n• View and manage your visit history\n• Receive reminders and notifications\n• Communicate with healthcare staff\n\nYou agree to use this Service only for lawful, personal, and non-commercial purposes."
                )
            }
            
            // User Accounts
            item {
                TermsSection(
                    title = "4. User Accounts",
                    content = "When registering for BrightCare, you must provide accurate, complete, and updated information. You are responsible for maintaining the confidentiality of your login credentials and for all activities that occur under your account."
                )
            }
            
            // Medical Information Disclaimer
            item {
                TermsSection(
                    title = "5. Medical Information",
                    content = "Information provided through BrightCare is for scheduling and educational purposes only. It does not replace professional medical advice. Always consult a licensed healthcare provider regarding any concerns about your condition or treatment."
                )
            }
            
            // Appointment Policies
            item {
                TermsSection(
                    title = "6. Appointment Cancellation and Rescheduling",
                    content = "You may cancel or reschedule appointments through the app based on clinic policy. Repeated last-minute cancellations may affect your future scheduling privileges. Please notify BrightCare at least 24 hours before your appointment whenever possible."
                )
            }
            
            // Limitation of Liability
            item {
                TermsSection(
                    title = "7. Limitation of Liability",
                    content = "BrightCare and its operators are not responsible for damages, losses, or delays resulting from your use or inability to use the app, system errors, or third-party service interruptions."
                )
            }
            
            // Changes to Terms
            item {
                TermsSection(
                    title = "8. Changes to Terms",
                    content = "We reserve the right to modify these Terms at any time. We will notify users of any material changes through the app or email. Your continued use of the Service after such modifications constitutes acceptance of the updated Terms."
                )
            }
            
            // Contact Information
            item {
                TermsSection(
                    title = "9. Contact Us",
                    content = "If you have any questions about these Terms and Conditions, please contact us at:\n\nEmail: support@brightcare.com\nPhone: +63 9123-456-789\nAddress: 123 Healthcare Ave, Medical District, Metro Manila, 1600"
                )
            }
        }
    }
}

@Composable
private fun TermsSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Gray900,
                fontSize = 16.sp
            )
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Gray700,
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            textAlign = TextAlign.Justify
        )
    }
}

@Preview(
    showBackground = true,
    name = "Terms & Conditions Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun TermsConditionsScreenPreview() {
    BrightCarePatientTheme {
        TermsConditionsScreen(
            navController = rememberNavController()
        )
    }
}
