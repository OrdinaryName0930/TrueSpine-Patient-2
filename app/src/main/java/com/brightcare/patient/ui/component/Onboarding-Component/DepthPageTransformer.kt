package com.brightcare.patient.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerScope
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import kotlin.math.abs

/**
 * Depth page transformer for smooth transitions between onboarding slides
 * Provides fade and scale effects similar to ViewPager2's DepthPageTransformer
 */
@OptIn(ExperimentalPagerApi::class)
fun Modifier.onboardingPageTransition(
    pagerScope: PagerScope,
    page: Int
): Modifier {
    return this.graphicsLayer {
        val pageOffset = pagerScope.calculateCurrentOffsetForPage(page)
        
        when {
            pageOffset < -1f -> {
                // Page is way off-screen to the left
                alpha = 0f
            }
            pageOffset <= 0f -> {
                // Page is moving to the left or is the current page
                alpha = 1f
                translationX = 0f
                scaleX = 1f
                scaleY = 1f
            }
            pageOffset <= 1f -> {
                // Page is moving to the right
                alpha = 1f - pageOffset
                
                // Move page behind
                translationX = size.width * -pageOffset
                
                // Scale down the page
                val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(pageOffset))
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            else -> {
                // Page is way off-screen to the right
                alpha = 0f
            }
        }
    }
}

private const val MIN_SCALE = 0.85f