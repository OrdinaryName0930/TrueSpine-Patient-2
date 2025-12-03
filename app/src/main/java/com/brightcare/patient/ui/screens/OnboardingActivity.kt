package com.brightcare.patient.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.brightcare.patient.ui.component.OnboardingDotIndicators
import com.brightcare.patient.ui.component.OnboardingSlide
import com.brightcare.patient.ui.component.OnboardingSlideContent
import com.brightcare.patient.ui.theme.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

/**
 * Onboarding Screen - Jetpack Compose
 * Maayos na onboarding screen na may smooth animations at magandang design
 * 
 * Features:
 * - 3 onboarding slides with images
 * - Smooth page transitions with fade and scale effects
 * - Progressive dot indicators
 * - Skip, Next, Back, and Get Started buttons
 * - Matches BrightCare design theme
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Define onboarding slides - images loaded from assets folder
    val slides = remember {
        listOf(
            OnboardingSlide(
                title = "Your Spine, Our Care",
                content = "Welcome to BrightCare, where your health and overall wellness are our top priorities.",
                imagePath = "images/s1.png"  // Loaded from assets
            ),
            OnboardingSlide(
                title = "Book Your Session",
                content = "Easily schedule, manage, and track your appointments anytime — helping you stay healthy and pain-free.",
                imagePath = "images/s2.png"  // Loaded from assets
            ),
            OnboardingSlide(
                title = "Feel Better, Move Better",
                content = "Every visit begins with a personalized assessment to understand your body's needs and restore your natural balance.",
                imagePath = "images/s3.png"  // Loaded from assets
            )
        )
    }

    val pagerState = rememberPagerState(initialPage = 0)
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        // HorizontalPager for slides
        HorizontalPager(
            count = slides.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingSlideContent(
                slide = slides[page],
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top section with Skip button
        AnimatedVisibility(
            visible = currentPage < 2,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 24.dp)
        ) {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                }
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Gray400
                    )
                )
            }
        }

        // Bottom section with dots and buttons on same line
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dot indicators
            OnboardingDotIndicators(
                totalDots = slides.size,
                currentPage = currentPage,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Button row - Back and Next/Get Started on same line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) { // Back button (left side) - Only visible on page 2 (last slide)

                // Next or Get Started button (right side, adjusts width based on page)
                if (currentPage != 2) {
                    // Next Button - Full width on page 0, minimized on page 1 (when back button visible)
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(currentPage + 1)
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue500,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Next",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                } else {
                    // Get Started Button - Minimized width to accommodate back button on page 2
                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .weight(1f) // Takes remaining space after back button
                            .height(56.dp)
                            .padding(start = 8.dp, end = 8.dp), // Space for back button on left
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue500,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    } // End main Box
}

// ============================================================================
// COMPOSE PREVIEW / PREVIEW NG COMPOSE
// ============================================================================

/**
 * Preview for Full Onboarding Screen
 * Preview para sa Buong Onboarding Screen
 * 
 * Note: Preview shows the first page of onboarding
 * Tandaan: Ang preview ay nagpapakita ng unang pahina ng onboarding
 */
@OptIn(ExperimentalPagerApi::class)
@Preview(
    name = "Onboarding Screen - Full",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    heightDp = 800,
    widthDp = 400,
    showSystemUi = true
)
@Composable
fun PreviewOnboardingScreen() {
    BrightCarePatientTheme {
        OnboardingScreen(
            onComplete = { /* Preview - no action */ }
        )
    }
}

/**
 * Preview for Onboarding Screen - Dark Background Test
 * Preview para sa Onboarding Screen - Test ng Madilim na Background
 */
@OptIn(ExperimentalPagerApi::class)
@Preview(
    name = "Onboarding Screen - Night Mode Test",
    showBackground = true,
    backgroundColor = 0xFF1A1A1A,
    heightDp = 800,
    widthDp = 400
)
@Composable
fun PreviewOnboardingScreenDark() {
    BrightCarePatientTheme {
        OnboardingScreen(
            onComplete = { /* Preview - no action */ }
        )
    }
}

/**
 * Preview for Onboarding Screen - Landscape Orientation
 * Preview para sa Onboarding Screen - Landscape Orientation
 */
@OptIn(ExperimentalPagerApi::class)
@Preview(
    name = "Onboarding Screen - Landscape",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    heightDp = 400,
    widthDp = 800
)
@Composable
fun PreviewOnboardingScreenLandscape() {
    BrightCarePatientTheme {
        OnboardingScreen(
            onComplete = { /* Preview - no action */ }
        )
    }
}