package com.brightcare.patient.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.brightcare.patient.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Custom Toast Component for BrightCare Patient App
 * Provides consistent toast notifications across the application
 * 
 * Features:
 * - Success, Error, Warning, and Info toast types
 * - Smooth slide-in/slide-out animations
 * - Auto-dismiss functionality
 * - Themed design matching BrightCare colors
 */

/**
 * Toast types with corresponding colors and icons
 */
enum class ToastType(
    val backgroundColor: Color,
    val textColor: Color,
    val icon: ImageVector,
    val iconColor: Color
) {
    SUCCESS(
        backgroundColor = Success,
        textColor = White,
        icon = Icons.Default.CheckCircle,
        iconColor = White
    ),
    ERROR(
        backgroundColor = Error,
        textColor = White,
        icon = Icons.Default.Error,
        iconColor = White
    ),
    WARNING(
        backgroundColor = Warning,
        textColor = White,
        icon = Icons.Default.Warning,
        iconColor = White
    ),
    INFO(
        backgroundColor = Blue500,
        textColor = White,
        icon = Icons.Default.Info,
        iconColor = White
    )
}

/**
 * Toast data class containing message and type
 */
data class ToastData(
    val message: String,
    val type: ToastType = ToastType.INFO,
    val duration: Long = 4000L, // 4 seconds default
    val actionLabel: String? = null,
    val onActionClick: (() -> Unit)? = null
)

/**
 * Toast state management
 */
class ToastState {
    private val _toastData = mutableStateOf<ToastData?>(null)
    val toastData: State<ToastData?> = _toastData
    
    private val _isVisible = mutableStateOf(false)
    val isVisible: State<Boolean> = _isVisible
    
    fun showToast(toastData: ToastData) {
        _toastData.value = toastData
        _isVisible.value = true
    }
    
    fun hideToast() {
        _isVisible.value = false
    }
    
    fun clearToast() {
        _toastData.value = null
        _isVisible.value = false
    }
}

/**
 * Remember toast state across recompositions
 */
@Composable
fun rememberToastState(): ToastState {
    return remember { ToastState() }
}

/**
 * Main Toast Composable
 * Displays animated toast notifications
 */
@Composable
fun BrightCareToast(
    toastState: ToastState,
    modifier: Modifier = Modifier
) {
    val toastData by toastState.toastData
    val isVisible by toastState.isVisible
    val density = LocalDensity.current
    
    // Debug logging
    LaunchedEffect(toastData, isVisible) {
        println("DEBUG BrightCareToast: toastData=$toastData, isVisible=$isVisible")
    }
    
    // Auto-dismiss toast after duration
    LaunchedEffect(toastData) {
        toastData?.let { data ->
            delay(data.duration)
            toastState.hideToast()
            delay(300) // Wait for exit animation
            toastState.clearToast()
        }
    }
    
    // Animation for slide in/out from top
    val shouldShow = isVisible && toastData != null
    println("DEBUG AnimatedVisibility: shouldShow=$shouldShow, isVisible=$isVisible, toastData=$toastData")
    
    AnimatedVisibility(
        visible = shouldShow,
        enter = slideInVertically(
            animationSpec = tween(300, easing = EaseOutCubic),
            initialOffsetY = { with(density) { 100.dp.roundToPx() } } // positive = from bottom
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            animationSpec = tween(300, easing = EaseInCubic),
            targetOffsetY = { with(density) { 100.dp.roundToPx() } } // slide down to bottom
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
            .fillMaxWidth()
            .zIndex(1000f)
    ) {
        toastData?.let { data ->
            ToastContent(
                toastData = data,
                onDismiss = { toastState.hideToast() }
            )
        }
    }
}

/**
 * Toast content with icon, message, and optional action
 */
@Composable
private fun ToastContent(
    toastData: ToastData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Gray900.copy(alpha = 0.1f),
                spotColor = Gray900.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = toastData.type.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Toast icon
            Icon(
                imageVector = toastData.type.icon,
                contentDescription = null,
                tint = toastData.type.iconColor,
                modifier = Modifier.size(24.dp)
            )
            
            // Toast message
            Text(
                text = toastData.message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = toastData.type.textColor,
                modifier = Modifier.weight(1f)
            )
            
            // Optional action button
            toastData.actionLabel?.let { actionLabel ->
                TextButton(
                    onClick = {
                        toastData.onActionClick?.invoke()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = toastData.type.textColor
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
            
            // Dismiss button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss toast",
                    tint = toastData.type.textColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Convenience functions for showing different types of toasts
 */

/**
 * Show success toast
 */
fun ToastState.showSuccess(
    message: String,
    duration: Long = 5000L,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    showToast(
        ToastData(
            message = message,
            type = ToastType.SUCCESS,
            duration = duration,
            actionLabel = actionLabel,
            onActionClick = onActionClick
        )
    )
}

/**
 * Show error toast
 */
fun ToastState.showError(
    message: String,
    duration: Long = 6000L, // Longer duration for errors
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    showToast(
        ToastData(
            message = message,
            type = ToastType.ERROR,
            duration = duration,
            actionLabel = actionLabel,
            onActionClick = onActionClick
        )
    )
}

/**
 * Show warning toast
 */
fun ToastState.showWarning(
    message: String,
    duration: Long = 4500L,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    showToast(
        ToastData(
            message = message,
            type = ToastType.WARNING,
            duration = duration,
            actionLabel = actionLabel,
            onActionClick = onActionClick
        )
    )
}

/**
 * Show info toast
 */
fun ToastState.showInfo(
    message: String,
    duration: Long = 5000L,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    showToast(
        ToastData(
            message = message,
            type = ToastType.INFO,
            duration = duration,
            actionLabel = actionLabel,
            onActionClick = onActionClick
        )
    )
}


// Preview Composables
@Preview(showBackground = true)
@Composable
fun ToastSuccessPreview() {
    BrightCarePatientTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ToastContent(
                toastData = ToastData(
                    message = "Verification email sent! Please check your inbox.",
                    type = ToastType.SUCCESS
                ),
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ToastErrorPreview() {
    BrightCarePatientTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ToastContent(
                toastData = ToastData(
                    message = "Failed to send verification email. Please try again.",
                    type = ToastType.ERROR,
                    actionLabel = "Retry"
                ),
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ToastInfoPreview() {
    BrightCarePatientTheme {
        Column(
            modifier = Modifier
                .background(WhiteBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ToastContent(
                toastData = ToastData(
                    message = "Please check your email and click the verification link.",
                    type = ToastType.INFO
                ),
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ToastWarningPreview() {
    BrightCarePatientTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ToastContent(
                toastData = ToastData(
                    message = "Your session will expire in 5 minutes.",
                    type = ToastType.WARNING,
                    actionLabel = "Extend"
                ),
                onDismiss = {}
            )
        }
    }
}
 