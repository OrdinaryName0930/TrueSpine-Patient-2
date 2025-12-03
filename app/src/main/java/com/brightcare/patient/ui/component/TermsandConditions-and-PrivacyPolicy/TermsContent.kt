package com.brightcare.patient.ui.component.termsandconditions_and_privacypolicy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brightcare.patient.ui.theme.*

@Composable
fun TermsContent(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = 5.dp,
            bottom = 100.dp // Extra space for the floating button
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Terms & Conditions Section
        item {
            Text(
                text = "Terms & Conditions",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Gray900,
                    fontSize = 24.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Last updated: October 29, 2024",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gray600,
                    fontSize = 12.sp
                ),
                modifier = Modifier.fillMaxWidth()
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }


        item {
            TermsSection(
                title = "1. Introduction",
                content = "Welcome to TrueSpine Chiropractic Appointment System. These Terms and Conditions govern your use of our chiropractic scheduling and patient management application operated by TrueSpine Wellness Center.\n\nBy using our Service, you agree to follow and be bound by these Terms. If you do not agree, please discontinue use of the app immediately."
            )
        }

        // Acceptance of Terms
        item {
            TermsSection(
                title = "2. Acceptance of Terms",
                content = "By creating an account and using TrueSpine, you acknowledge that you have read, understood, and agreed to these Terms and Conditions, as well as our Privacy Policy. Continued use of the Service constitutes acceptance of any updates to these Terms."
            )
        }

        // Use of Service
        item {
            TermsSection(
                title = "3. Use of Service",
                content = "Our Service enables you to:\n• Schedule chiropractic appointments\n• View and manage your visit history\n• Receive reminders and notifications\n• Communicate with TrueSpine staff\n\nYou agree to use this Service only for lawful, personal, and non-commercial purposes."
            )
        }

        // User Accounts
        item {
            TermsSection(
                title = "4. User Accounts",
                content = "When registering for TrueSpine, you must provide accurate, complete, and updated information. You are responsible for maintaining the confidentiality of your login credentials and for all activities that occur under your account."
            )
        }

        // Health Information Disclaimer
        item {
            TermsSection(
                title = "5. Medical Information",
                content = "Information provided through TrueSpine is for scheduling and educational purposes only. It does not replace professional medical or chiropractic advice. Always consult a licensed chiropractor or healthcare provider regarding any concerns about your condition or treatment."
            )
        }

        // Cancellation & Rescheduling
        item {
            TermsSection(
                title = "6. Appointment Cancellation and Rescheduling",
                content = "You may cancel or reschedule appointments through the app based on clinic policy. Repeated last-minute cancellations may affect your future scheduling privileges. Please notify TrueSpine at least 24 hours before your appointment whenever possible."
            )
        }

        // Limitation of Liability
        item {
            TermsSection(
                title = "7. Limitation of Liability",
                content = "TrueSpine and its operators are not responsible for damages, losses, or delays resulting from your use or inability to use the app, system errors, or third-party service interruptions."
            )
        }

        // Privacy Policy Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Gray900,
                    fontSize = 24.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Last updated: October 29, 2024",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gray600,
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }

        item {
            TermsSection(
                title = "1. Information We Collect",
                content = "We collect personal and health-related information that you provide directly to us, including:\n• Name, contact number, and email address\n• Health and chiropractic history\n• Appointment details and visit records\n• Communication logs with clinic staff"
            )
        }

        // How We Use Information
        item {
            TermsSection(
                title = "2. How We Use Your Information",
                content = "Your information helps us:\n• Manage your chiropractic appointments\n• Send reminders and updates\n• Improve our services and patient care\n• Communicate regarding your treatment or schedule changes\n• Maintain accurate patient records"
            )
        }

        // Information Sharing
        item {
            TermsSection(
                title = "3. Information Sharing",
                content = "We do not sell or share your personal information to third parties. However, we may share limited data with licensed practitioners or trusted service providers as needed for treatment, billing, or technical support, in compliance with applicable laws."
            )
        }

        // Data Security
        item {
            TermsSection(
                title = "4. Data Security",
                content = "TrueSpine uses appropriate security measures to protect your personal data against unauthorized access or misuse. Despite these efforts, we cannot guarantee absolute security of data transmitted over the internet."
            )
        }

        // Your Rights
        item {
            TermsSection(
                title = "5. Your Rights",
                content = "You have the right to:\n• Access and update your personal data\n•Request correction or deletion of inaccurate information\n• Withdraw consent for data use (subject to legal or medical record-keeping requirements)\n• Receive a copy of your records upon request"
            )
        }

        // Contact Information
        item {
            TermsSection(
                title = "6. Contact Us",
                content = "If you have any questions about these Terms and Conditions or Privacy Policy, please contact us at:\n\nEmail: support@TrueSpine.com\nPhone: +63 9123-456-789\nAddress: 123 Pag-ibig Ave, Barangay Kilig, Eme City, 1600"
            )
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TermsContentPreview() {
    BrightCarePatientTheme {
        Surface(
            color = White,
            modifier = Modifier.fillMaxSize()
        ) {
            TermsContent()
        }
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
fun TermsContentCompactPreview() {
    BrightCarePatientTheme {
        Surface(
            color = White,
            modifier = Modifier.fillMaxSize()
        ) {
            TermsContent()
        }
    }
}
