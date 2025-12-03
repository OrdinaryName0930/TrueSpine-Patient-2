package com.brightcare.patient.ui.component

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.brightcare.patient.ui.theme.Blue500
import com.brightcare.patient.ui.theme.BrightCarePatientTheme
import com.brightcare.patient.ui.theme.Gray200
import com.brightcare.patient.ui.theme.Gray300
import com.brightcare.patient.ui.theme.Gray400
import com.brightcare.patient.ui.theme.Gray700
import com.brightcare.patient.ui.theme.WhiteBg

/**
 * Data class representing an onboarding slide
 * Images are loaded from assets folder
 */
data class OnboardingSlide(
    val title: String,
    val content: String,
    val imagePath: String  // Path in assets folder (e.g., "images/s1.jpg")
)

/**
 * Individual onboarding slide composable with fade-in animation
 * Loads images from assets folder
 */
@Composable
fun OnboardingSlideContent(
    slide: OnboardingSlide,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Load image from assets with improved error handling
    val bitmap = remember(slide.imagePath) {
        try {
            Log.d("OnboardingImage", "Attempting to load image: ${slide.imagePath}")
            context.assets.open(slide.imagePath).use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    Log.d("OnboardingImage", "Successfully loaded image: ${slide.imagePath}")
                } else {
                    Log.e("OnboardingImage", "Failed to decode bitmap for: ${slide.imagePath}")
                }
                bitmap
            }
        } catch (e: Exception) {
            Log.e("OnboardingImage", "Error loading image ${slide.imagePath}: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    // Fade-in animation when slide appears
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(slide) {
        isVisible = false
        kotlinx.coroutines.delay(100)
        isVisible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        
        // Image with animation - loaded from assets
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = slide.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()         // ← allows it to grow
                    .aspectRatio(1.5f)
                    .alpha(alpha)
                    .scale(scale)
            )
        } else {
            // Placeholder when image fails to load
            Box(
                modifier = Modifier
                    .fillMaxWidth()         // ← allows it to grow
                    .aspectRatio(1.5f)
                    .alpha(alpha)
                    .scale(scale)
                    .background(Gray200, shape = MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = slide.title,
                    modifier = Modifier
                        .fillMaxWidth()         // ← allows it to grow
                        .aspectRatio(1.5f),
                    tint = Gray400
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title
        Text(
            text = slide.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Gray700
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(alpha)
                .fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content
        Text(
            text = slide.content,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                color = Gray700,
                lineHeight = 24.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(alpha)
                .fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.weight(0.5f))
    }
}

/**
 * Progressive dot indicators for onboarding
 */
@Composable
fun OnboardingDotIndicators(
    totalDots: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val isActive = index == currentPage
            
            val width by animateDpAsState(
                targetValue = if (isActive) 8.dp else 8.dp,
                animationSpec = tween(durationMillis = 300),
                label = "width"
            )
            
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .background(
                        color = if (isActive) Blue500 else Gray300,
                        shape = CircleShape
                    )
            )
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS / MGA PREVIEW NG COMPOSE
// ============================================================================

/**
 * Preview for Onboarding Slide Content - First Slide
 * Preview para sa Onboarding Slide - Unang Slide
 */
@Preview(
    name = "Onboarding Slide 1",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    heightDp = 800,
    widthDp = 400
)
@Composable
fun PreviewOnboardingSlide1() {
    BrightCarePatientTheme {
        OnboardingSlideContent(
            slide = OnboardingSlide(
                title = "Your Spine, Our Care",
                content = "Welcome to BrightCare, where your health and overall wellness are our top priorities.",
                imagePath = "images/s1.png"
            )
        )
    }
}

/**
 * Preview for Onboarding Slide Content - Second Slide
 * Preview para sa Onboarding Slide - Pangalawang Slide
 */
@Preview(
    name = "Onboarding Slide 2",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    heightDp = 800,
    widthDp = 400
)
@Composable
fun PreviewOnboardingSlide2() {
    BrightCarePatientTheme {
        OnboardingSlideContent(
            slide = OnboardingSlide(
                title = "Book Your Session",
                content = "Easily schedule, manage, and track your appointments anytime — helping you stay healthy and pain-free.",
                imagePath = "images/s2.png"
            )
        )
    }
}

/**
 * Preview for Onboarding Slide Content - Third Slide
 * Preview para sa Onboarding Slide - Pangatlong Slide
 */
@Preview(
    name = "Onboarding Slide 3",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    heightDp = 800,
    widthDp = 400
)
@Composable
fun PreviewOnboardingSlide3() {
    BrightCarePatientTheme {
        OnboardingSlideContent(
            slide = OnboardingSlide(
                title = "Feel Better, Move Better",
                content = "Every visit begins with a personalized assessment to understand your body's needs and restore your natural balance.",
                imagePath = "images/s3.png"
            )
        )
    }
}

/**
 * Preview for Dot Indicators - First Page Active
 * Preview para sa Dot Indicators - Unang Pahina Active
 */
@Preview(
    name = "Dot Indicators - Page 0",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun PreviewDotIndicatorsPage0() {
    BrightCarePatientTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingDotIndicators(
                totalDots = 3,
                currentPage = 0
            )
        }
    }
}

/**
 * Preview for Dot Indicators - Second Page Active
 * Preview para sa Dot Indicators - Pangalawang Pahina Active
 */
@Preview(
    name = "Dot Indicators - Page 1",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun PreviewDotIndicatorsPage1() {
    BrightCarePatientTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingDotIndicators(
                totalDots = 3,
                currentPage = 1
            )
        }
    }
}

/**
 * Preview for Dot Indicators - Third Page Active
 * Preview para sa Dot Indicators - Pangatlong Pahina Active
 */
@Preview(
    name = "Dot Indicators - Page 2",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun PreviewDotIndicatorsPage2() {
    BrightCarePatientTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingDotIndicators(
                totalDots = 3,
                currentPage = 2
            )
        }
    }
}
