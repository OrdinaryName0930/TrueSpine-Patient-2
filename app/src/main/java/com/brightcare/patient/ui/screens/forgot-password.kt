package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.brightcare.patient.ui.component.termsandconditions_and_privacypolicy.TermsBackButton
import com.brightcare.patient.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    email: String = "",
    onBackClick: () -> Unit = { navController.popBackStack() },
    onVerifyOtpClick: (String) -> Unit = { navController.navigate("change_password") }
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteBg)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                TermsBackButton(
                    onClick = onBackClick
                )
            }
            
            // Title
            Text(
                text = "OTP Verification",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = Blue500,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Description
            Text(
                text = "OTP verification is no longer used. Password reset is now handled via email link. Please use the 'Forgot Password' option on the login screen.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Gray600,
                    lineHeight = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Go to Login Button
            Button(
                onClick = { 
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue500
                )
            ) {
                Text(
                    text = "Go to Login",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = WhiteBg
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "POCO C75 - Portrait",
    widthDp = 360,
    heightDp = 740,
    showSystemUi = true
)
@Composable
fun ForgotPasswordScreenPreview() {
    BrightCarePatientTheme {
        ForgotPasswordScreen(
            navController = rememberNavController()
        )
    }
}