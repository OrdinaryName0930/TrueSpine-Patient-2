package com.brightcare.patient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.navigation.NavigationGraph
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.ui.theme.BrightCarePatientTheme
import com.brightcare.patient.ui.theme.WhiteBg
import com.brightcare.patient.utils.OnboardingPreferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            BrightCarePatientTheme {
                // Set system bar colors to match WhiteBg
                SideEffect {
                    val window = this@MainActivity.window
                    window.statusBarColor = WhiteBg.toArgb()
                    window.navigationBarColor = WhiteBg.toArgb()
                    
                    // Set status bar icons to dark (since background is light)
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = true
                        isAppearanceLightNavigationBars = true
                    }
                }
                
                val navController = rememberNavController()
                
                // Determine start destination based on onboarding status
                // Tukuyin ang start destination base sa onboarding status
                val startDestination = if (OnboardingPreferences.hasSeenOnboarding(this@MainActivity)) {
                    NavigationRoutes.LOGIN  // User has seen onboarding, go to login
                } else {
                    NavigationRoutes.ONBOARDING  // First time user, show onboarding
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavigationGraph(
                        navController = navController,
                        startDestination = startDestination,
                        onFinishActivity = { finish() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}


