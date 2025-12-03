package com.brightcare.patient.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.component.navigation_fragment.StatefulNavigationFragment
import com.brightcare.patient.ui.theme.BrightCarePatientTheme

/**
 * Main Dashboard Screen - Contains the bottom navigation and manages the main app screens
 * This is the main container for the authenticated user experience
 */
@Composable
fun MainDashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    initialRoute: String = "home"
) {
    StatefulNavigationFragment(
        navController = navController,
        modifier = modifier.fillMaxSize(),
        initialRoute = initialRoute
    )
}

@Preview(
    showBackground = true,
    name = "Main Dashboard Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun MainDashboardScreenPreview() {
    BrightCarePatientTheme {
        MainDashboardScreen(
            navController = rememberNavController()
        )
    }
}
