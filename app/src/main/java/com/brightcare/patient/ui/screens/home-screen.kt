package com.brightcare.patient.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.PatientSignInViewModel

/**
 * Home screen - main dashboard after successful login and profile completion
 * Pangunahing screen pagkatapos ng matagumpay na login at profile completion
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    signInViewModel: PatientSignInViewModel = hiltViewModel()
) {
    // Make system navigation bar white with dark icons
    SetWhiteSystemNavBar()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome Icon
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Welcome",
            tint = Blue500,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Welcome Title
        Text(
            text = "Welcome to TrueSpine!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Blue500,
                fontSize = 28.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(32.dp))

        // Placeholder for future features
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Blue50),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Features Coming Soon",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Blue700
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Di pa naiintegrate yung sa main activity",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray700,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout button
        Button(
            onClick = {
                // Sign out user and clear session
                signInViewModel.signOut()
                // Navigate to login with clearStates=true to prevent auto-redirect
                navController.navigate("login?clearStates=true") {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign Out",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

/**
 * Sets the system navigation bar (Back/Home/Overview) to white with dark icons
 */
@Composable
fun SetWhiteSystemNavBar() {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.navigationBarColor = android.graphics.Color.WHITE
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightNavigationBars = true
        }
    }
}

@Preview(
    showBackground = true,
    name = "Home Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun HomeScreenPreview() {
    BrightCarePatientTheme {
        HomeScreen(
            navController = rememberNavController()
        )
    }
}
