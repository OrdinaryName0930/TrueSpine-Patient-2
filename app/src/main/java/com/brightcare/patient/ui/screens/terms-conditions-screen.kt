package com.brightcare.patient.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.ui.theme.*

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhiteBg)
                    .padding(16.dp),
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
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Blue500,
                        fontSize = 28.sp
                    )
                )
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
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text(
                    text = "Last updated: October 29, 2024",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray600,
                        fontSize = 12.sp
                    )
                )
            }

            item {
                TermsSection(
                    title = "1. Introduction",
                    content = "Welcome to the TrueSpine Patient App. These Terms and Conditions govern your use of our chiropractic appointment scheduling and patient management application operated by TrueSpine Wellness Center.\n\nBy using our Service, you agree to follow and be bound by these Terms. If you do not agree, please discontinue use of the app immediately."
                )
            }

            item {
                TermsSection(
                    title = "2. Acceptance of Terms",
                    content = "By creating an account and using TrueSpine, you acknowledge that you have read, understood, and agreed to these Terms and Conditions, as well as our Privacy Policy. Continued use of the Service constitutes acceptance of any updates to these Terms."
                )
            }

            item {
                TermsSection(
                    title = "3. Use of Service",
                    content = "Our Service enables you to:\n• Schedule chiropractic appointments\n• View and manage your visit history\n• Receive reminders and notifications\n• Communicate with clinic staff\n\nYou agree to use this Service only for lawful, personal, and non-commercial purposes."
                )
            }

            item {
                TermsSection(
                    title = "4. User Accounts",
                    content = "When registering for TrueSpine, you must provide accurate, complete, and updated information. You are responsible for maintaining the confidentiality of your login credentials and for all activities that occur under your account."
                )
            }

            item {
                TermsSection(
                    title = "5. Medical Information",
                    content = "Information provided through TrueSpine is for appointment scheduling and educational purposes only. It does not replace professional medical or chiropractic advice. Always consult a licensed practitioner regarding your condition or treatment."
                )
            }

            item {
                TermsSection(
                    title = "6. Appointment Cancellation and Rescheduling",
                    content = "You may cancel or reschedule appointments through the app according to clinic policy. Repeated last-minute cancellations may affect your future scheduling privileges. Please notify TrueSpine at least 24 hours before your appointment whenever possible."
                )
            }

            item {
                TermsSection(
                    title = "7. Limitation of Liability",
                    content = "TrueSpine and its operators are not responsible for damages, losses, or delays resulting from your use or inability to use the app, system errors, or third-party service interruptions."
                )
            }

            item {
                TermsSection(
                    title = "8. Changes to Terms",
                    content = "We reserve the right to modify these Terms at any time. We will notify users of material changes through the app or email. Continued use of the Service constitutes acceptance of the updated Terms."
                )
            }

            item {
                TermsSection(
                    title = "9. Contact Us",
                    content = "If you have any questions about these Terms and Conditions, please contact us at:\n\nEmail: support@truespine.com\nPhone: +63 9123-456-789\nAddress: 123 Wellness Ave, Medical District, Metro Manila, 1600"
                )
            }
        }
    }
}

@Composable
private fun TermsSection(
    title: String,
    content: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
