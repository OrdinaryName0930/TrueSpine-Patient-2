package com.brightcare.patient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.screens.AuthenticationWrapper
import com.brightcare.patient.ui.theme.BrightCarePatientTheme
import com.brightcare.patient.ui.theme.WhiteBg
import com.brightcare.patient.utils.AuthenticationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Alternative MainActivity without edge-to-edge display
 * Use this version if the current MainActivity causes overlapping issues
 */
@AndroidEntryPoint
class MainActivity_Alternative : ComponentActivity() {
    
    @Inject
    lateinit var authenticationManager: AuthenticationManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Standard window setup (no edge-to-edge)
        // This prevents content from overlapping with system bars
        
        setContent {
            BrightCarePatientTheme {
                // Set system bar colors to match WhiteBg
                SideEffect {
                    val window = this@MainActivity_Alternative.window
                    window.statusBarColor = WhiteBg.toArgb()
                    window.navigationBarColor = WhiteBg.toArgb()
                    
                    // Set status bar icons to dark (since background is light)
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = true
                        isAppearanceLightNavigationBars = true
                    }
                }
                
                val navController = rememberNavController()

                // Simple Scaffold without edge-to-edge complications
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AuthenticationWrapper(
                        navController = navController,
                        authenticationManager = authenticationManager,
                        onFinishActivity = { finish() }
                    )
                }
            }
        }
    }
}
