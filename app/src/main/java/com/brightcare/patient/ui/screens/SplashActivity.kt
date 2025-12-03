package com.brightcare.patient.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import com.brightcare.patient.MainActivity
import com.brightcare.patient.R
import com.brightcare.patient.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SplashActivity - Jetpack Compose
 * Splash screen na makikita ng user habang nag-initialize ang app
 * 
 * Features:
 * - Logo animation with fade in and scale effects
 * - White background theme (WhiteBg)
 * - Logo loaded from assets/logo.png
 * - Smooth transition to MainActivity after 3 seconds
 * - Responsive design for different screen sizes
 * 
 * English/Tagalog:
 * This splash activity will be shown when the app starts while everything is initializing.
 * Ang splash activity na ito ay makikita kapag nagsisimula ang app habang nag-i-initialize ang lahat.
 */
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrightCarePatientTheme {
                SplashScreen(
                    onSplashFinished = {
                        // Navigate to MainActivity after splash
                        // MainActivity will handle onboarding vs login navigation based on user's first-time status
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

/**
 * SplashScreen Composable
 * Main splash screen content with logo and animations
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier.background(WhiteBg)
) {
    // Animation values for logo
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.3f) }
    val textAlpha = remember { Animatable(0f) }
    
    // Start animations when composable is first composed
    LaunchedEffect(Unit) {
        // Start logo animations concurrently
        val logoJob = launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
        val scaleJob = launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200)
            )
        }
        
        // Text fade in after logo animation starts
        delay(300)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        
        // Wait for total splash duration then navigate
        delay(1500) // Total splash time: 3 seconds
        onSplashFinished()
    }
    
    // Main splash screen layout
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg), // Using pure white as requested
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo from assets
            val context = LocalContext.current
            val logoBitmap = remember {
                try {
                    context.assets.open("images/logo.png").use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            
            if (logoBitmap != null) {
                Image(
                    bitmap = logoBitmap.asImageBitmap(),
                    contentDescription = "TrueSpine Logo",
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer(
                            alpha = logoAlpha.value,
                            scaleX = logoScale.value,
                            scaleY = logoScale.value
                        )
                )
            } else {
                // Fallback to vector drawable if bitmap loading fails
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "TrueSpine Logo",
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer(
                            alpha = logoAlpha.value,
                            scaleX = logoScale.value,
                            scaleY = logoScale.value
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App name text
            Text(
                text = "TrueSpine",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    letterSpacing = 0.02.sp
                ),
                color = Blue500, // Using the blue color from theme
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer(alpha = textAlpha.value)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle
            Text(
                text = "Your Spine, Our Care",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = Gray600,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer(alpha = textAlpha.value)
            )
        }
    }
}

// ============================================================================
// COMPOSE PREVIEW / PREVIEW NG COMPOSE
// ============================================================================

/**
 * Preview for Splash Screen - Light Mode
 * Preview para sa Splash Screen - Light Mode
 */
@Preview(
    name = "Splash Screen - Light Mode",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    heightDp = 800,
    widthDp = 400,
    showSystemUi = true
)
@Composable
fun PreviewSplashScreen() {
    BrightCarePatientTheme {
        SplashScreen(
            onSplashFinished = { /* Preview - no action */ }
        )
    }
}

/**
 * Preview for Splash Screen - Different Screen Size
 * Preview para sa Splash Screen - Ibang Screen Size
 */
@Preview(
    name = "Splash Screen - Tablet",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    heightDp = 1024,
    widthDp = 768
)
@Composable
fun PreviewSplashScreenTablet() {
    BrightCarePatientTheme {
        SplashScreen(
            onSplashFinished = { /* Preview - no action */ }
        )
    }
}

/**
 * Preview for Splash Screen - Landscape
 * Preview para sa Splash Screen - Landscape
 */
@Preview(
    name = "Splash Screen - Landscape",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    heightDp = 400,
    widthDp = 800
)
@Composable
fun PreviewSplashScreenLandscape() {
    BrightCarePatientTheme {
        SplashScreen(
            onSplashFinished = { /* Preview - no action */ }
        )
    }
}
