package com.brightcare.patient.ui.component.signup_component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brightcare.patient.ui.theme.*
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
enum class SignUpButtonType {
    PRIMARY,
    SECONDARY,
    OUTLINE,
    TEXT,
    SOCIAL_GOOGLE,
    SOCIAL_FACEBOOK
}

@Composable
fun SignUpButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: SignUpButtonType = SignUpButtonType.PRIMARY,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.START
) {
    // Maintain original design but adapt colors for enabled/disabled states
    val (containerColor, contentColor, borderStroke) = when (type) {
        SignUpButtonType.PRIMARY -> Triple(
            if (enabled) Blue500 else Blue500.copy(alpha = 0.5f),
            if (enabled) White else White.copy(alpha = 0.6f),
            null
        )
        SignUpButtonType.SECONDARY -> Triple(
            if (enabled) Orange500 else Orange500.copy(alpha = 0.5f),
            if (enabled) White else White.copy(alpha = 0.6f),
            null
        )
        SignUpButtonType.OUTLINE -> Triple(
            Color.Transparent,
            if (enabled) Blue500 else Gray400,
            BorderStroke(1.dp, if (enabled) Blue500 else Gray300)
        )
        SignUpButtonType.TEXT -> Triple(
            Color.Transparent,
            if (enabled) Blue500 else Gray400,
            null
        )
        SignUpButtonType.SOCIAL_GOOGLE -> Triple(
            White,
            Gray700,
            BorderStroke(1.dp, Gray300)
        )
        SignUpButtonType.SOCIAL_FACEBOOK -> Triple(
            White,
            Gray700,
            BorderStroke(1.dp, Gray300)
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        border = borderStroke,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (type == SignUpButtonType.SOCIAL_GOOGLE) 1.dp else 0.dp,
            pressedElevation = if (type == SignUpButtonType.SOCIAL_GOOGLE) 2.dp else 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null && iconPosition == IconPosition.START) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = contentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                )

                if (icon != null && iconPosition == IconPosition.END) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = contentColor
                    )
                }
            }
        }
    }
}

enum class IconPosition {
    START,
    END
}

// Google Logo Vector
@Composable
fun GoogleLogo(modifier: Modifier = Modifier) {
    val googleIcon = ImageVector.Builder(
        name = "GoogleLogo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color(0xFF4285F4)),
            fillAlpha = 1.0f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(22.56f, 12.25f)
            curveToRelative(0f, -0.78f, -0.07f, -1.53f, -0.2f, -2.25f)
            horizontalLineTo(12f)
            verticalLineToRelative(4.26f)
            horizontalLineToRelative(5.92f)
            curveToRelative(-0.26f, 1.37f, -1.04f, 2.53f, -2.21f, 3.31f)
            verticalLineToRelative(2.77f)
            horizontalLineToRelative(3.57f)
            curveToRelative(2.08f, -1.92f, 3.28f, -4.74f, 3.28f, -8.09f)
            close()
        }
        path(
            fill = SolidColor(Color(0xFF34A853)),
            fillAlpha = 1.0f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12f, 23f)
            curveToRelative(2.97f, 0f, 5.46f, -0.98f, 7.28f, -2.66f)
            lineToRelative(-3.57f, -2.77f)
            curveToRelative(-0.98f, 0.66f, -2.23f, 1.06f, -3.71f, 1.06f)
            curveToRelative(-2.86f, 0f, -5.29f, -1.93f, -6.16f, -4.53f)
            horizontalLineTo(2.18f)
            verticalLineToRelative(2.84f)
            curveTo(3.99f, 20.53f, 7.7f, 23f, 12f, 23f)
            close()
        }
        path(
            fill = SolidColor(Color(0xFFFBBC05)),
            fillAlpha = 1.0f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(5.84f, 14.09f)
            curveToRelative(-0.22f, -0.66f, -0.35f, -1.36f, -0.35f, -2.09f)
            reflectiveCurveToRelative(0.13f, -1.43f, 0.35f, -2.09f)
            verticalLineTo(7.07f)
            horizontalLineTo(2.18f)
            curveTo(1.43f, 8.55f, 1f, 10.22f, 1f, 12f)
            reflectiveCurveToRelative(0.43f, 3.45f, 1.18f, 4.93f)
            lineToRelative(2.85f, -2.22f)
            lineToRelative(0.81f, -0.62f)
            close()
        }
        path(
            fill = SolidColor(Color(0xFFEA4335)),
            fillAlpha = 1.0f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12f, 5.38f)
            curveToRelative(1.62f, 0f, 3.06f, 0.56f, 4.21f, 1.64f)
            lineToRelative(3.15f, -3.15f)
            curveTo(17.45f, 2.09f, 14.97f, 1f, 12f, 1f)
            curveTo(7.7f, 1f, 3.99f, 3.47f, 2.18f, 7.07f)
            lineToRelative(3.66f, 2.84f)
            curveToRelative(0.87f, -2.6f, 3.3f, -4.53f, 6.16f, -4.53f)
            close()
        }
    }.build()

    Icon(
        imageVector = googleIcon,
        contentDescription = "Google Logo",
        modifier = modifier,
        tint = Color.Unspecified
    )
}

// Facebook Logo Vector
@Composable
fun FacebookLogo(modifier: Modifier = Modifier) {
    val facebookIcon = ImageVector.Builder(
        name = "FacebookLogo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color(0xFF1877F2)),
            fillAlpha = 1.0f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(24f, 12.073f)
            curveToRelative(0f, -6.627f, -5.373f, -12f, -12f, -12f)
            reflectiveCurveToRelative(-12f, 5.373f, -12f, 12f)
            curveToRelative(0f, 5.99f, 4.388f, 10.954f, 10.125f, 11.854f)
            verticalLineToRelative(-8.385f)
            horizontalLineTo(7.078f)
            verticalLineToRelative(-3.47f)
            horizontalLineToRelative(3.047f)
            verticalLineTo(9.43f)
            curveToRelative(0f, -3.007f, 1.792f, -4.669f, 4.533f, -4.669f)
            curveToRelative(1.312f, 0f, 2.686f, 0.235f, 2.686f, 0.235f)
            verticalLineToRelative(2.953f)
            horizontalLineTo(15.83f)
            curveToRelative(-1.491f, 0f, -1.956f, 0.925f, -1.956f, 1.874f)
            verticalLineToRelative(2.25f)
            horizontalLineToRelative(3.328f)
            lineToRelative(-0.532f, 3.47f)
            horizontalLineToRelative(-2.796f)
            verticalLineToRelative(8.385f)
            curveTo(19.612f, 23.027f, 24f, 18.062f, 24f, 12.073f)
            close()
        }
    }.build()

    Icon(
        imageVector = facebookIcon,
        contentDescription = "Facebook Logo",
        modifier = modifier,
        tint = Color.Unspecified
    )
}

enum class SocialProvider {
    GOOGLE,
    FACEBOOK
}

// ✅ New: Logo-only Social Buttons (Google & Facebook)
@Composable
fun SocialIconButton(
    provider: SocialProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    logoSize: Int = 32 // Fixed logo size in dp
) {
    val (containerColor, contentColor, borderStroke) = when (provider) {
        SocialProvider.GOOGLE -> Triple(
            White,
            Gray700,
            BorderStroke(1.dp, Gray300)
        )
        SocialProvider.FACEBOOK -> Triple(
            White,
            Gray700,
            BorderStroke(1.dp, Gray300)
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Gray200,
            disabledContentColor = Gray400
        ),
        border = borderStroke,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(5.dp) // Remove default padding to allow full logo size
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            // Simple centered logo with fixed size
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Ensure logo size is reasonable (not larger than button would allow)
                val actualLogoSize = logoSize.coerceAtMost(48).dp

                when (provider) {
                    SocialProvider.GOOGLE -> GoogleLogo(
                        modifier = Modifier.size(actualLogoSize)
                    )
                    SocialProvider.FACEBOOK -> FacebookLogo(
                        modifier = Modifier.size(actualLogoSize)
                    )
                }
            }
        }
    }
}