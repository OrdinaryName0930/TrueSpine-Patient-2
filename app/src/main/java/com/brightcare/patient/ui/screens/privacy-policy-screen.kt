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
 * Privacy Policy screen - Display privacy policy information
 * Privacy Policy screen - Ipakita ang privacy policy information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
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
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Blue500,
                            fontSize = 28.sp
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
                PrivacySection(
                    title = "Introduction",
                    content = "At BrightCare Medical Center, we are committed to protecting your privacy and ensuring the security of your personal and health information. This Privacy Policy explains how we collect, use, store, and protect your information when you use our mobile application and services."
                )
            }
            
            // Information We Collect
            item {
                PrivacySection(
                    title = "1. Information We Collect",
                    content = "We collect personal and health-related information that you provide directly to us, including:\n• Name, contact number, and email address\n• Health and medical history\n• Appointment details and visit records\n• Communication logs with healthcare staff\n• Emergency contact information\n• Insurance and billing information"
                )
            }
            
            // How We Use Information
            item {
                PrivacySection(
                    title = "2. How We Use Your Information",
                    content = "Your information helps us:\n• Manage your medical appointments and healthcare services\n• Send reminders and important health updates\n• Improve our services and patient care quality\n• Communicate regarding your treatment or schedule changes\n• Maintain accurate medical records\n• Process insurance claims and billing\n• Comply with legal and regulatory requirements"
                )
            }
            
            // Information Sharing
            item {
                PrivacySection(
                    title = "3. Information Sharing and Disclosure",
                    content = "We do not sell or share your personal information to third parties for marketing purposes. However, we may share limited data with:\n• Licensed healthcare practitioners involved in your care\n• Trusted service providers for technical support and maintenance\n• Insurance companies for claims processing\n• Legal authorities when required by law\n• Emergency contacts in case of medical emergencies"
                )
            }
            
            // Data Security
            item {
                PrivacySection(
                    title = "4. Data Security and Protection",
                    content = "BrightCare uses industry-standard security measures to protect your personal data:\n• End-to-end encryption for data transmission\n• Secure servers with regular security updates\n• Access controls and authentication protocols\n• Regular security audits and monitoring\n• HIPAA-compliant data handling procedures\n\nDespite these efforts, no system is 100% secure, and we cannot guarantee absolute security of data transmitted over the internet."
                )
            }
            
            // Data Retention
            item {
                PrivacySection(
                    title = "5. Data Retention",
                    content = "We retain your personal and medical information for as long as necessary to:\n• Provide ongoing healthcare services\n• Comply with legal and regulatory requirements\n• Maintain medical records as required by law\n• Process insurance claims and billing\n\nYou may request deletion of your data, subject to legal and medical record-keeping requirements."
                )
            }
            
            // Your Rights
            item {
                PrivacySection(
                    title = "6. Your Privacy Rights",
                    content = "You have the right to:\n• Access and review your personal data\n• Request correction of inaccurate information\n• Request deletion of your data (subject to legal requirements)\n• Withdraw consent for certain data uses\n• Receive a copy of your medical records\n• File complaints about privacy practices\n• Opt-out of non-essential communications"
                )
            }
            
            // Children's Privacy
            item {
                PrivacySection(
                    title = "7. Children's Privacy",
                    content = "Our services are not intended for children under 13 years of age. We do not knowingly collect personal information from children under 13. If you are a parent or guardian and believe your child has provided us with personal information, please contact us immediately."
                )
            }
            
            // Changes to Privacy Policy
            item {
                PrivacySection(
                    title = "8. Changes to This Privacy Policy",
                    content = "We may update this Privacy Policy from time to time to reflect changes in our practices or legal requirements. We will notify you of any material changes through the app or email. Your continued use of our services after such modifications constitutes acceptance of the updated Privacy Policy."
                )
            }
            
            // Contact Information
            item {
                PrivacySection(
                    title = "9. Contact Us",
                    content = "If you have any questions about this Privacy Policy or our privacy practices, please contact us at:\n\nEmail: privacy@brightcare.com\nPhone: +63 9123-456-789\nAddress: 123 Healthcare Ave, Medical District, Metro Manila, 1600"
                )
            }
        }
    }
}

@Composable
private fun PrivacySection(
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
    name = "Privacy Policy Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun PrivacyPolicyScreenPreview() {
    BrightCarePatientTheme {
        PrivacyPolicyScreen(
            navController = rememberNavController()
        )
    }
}
