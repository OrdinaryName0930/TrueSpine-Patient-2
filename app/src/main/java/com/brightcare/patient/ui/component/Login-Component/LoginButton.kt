package com.brightcare.patient.ui.component.login_component

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

enum class LoginButtonType {
    PRIMARY,
    SECONDARY,
    OUTLINE,
    TEXT
}

@Composable
fun LoginButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: LoginButtonType = LoginButtonType.PRIMARY,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.START
) {
    // Maintain original design but adapt colors for enabled/disabled states
    val (containerColor, contentColor, borderStroke) = when (type) {
        LoginButtonType.PRIMARY -> Triple(
            if (enabled) Blue500 else Blue500.copy(alpha = 0.5f),
            if (enabled) White else White.copy(alpha = 0.6f),
            null
        )
        LoginButtonType.SECONDARY -> Triple(
            if (enabled) Orange500 else Orange500.copy(alpha = 0.5f),
            if (enabled) White else White.copy(alpha = 0.6f),
            null
        )
        LoginButtonType.OUTLINE -> Triple(
            Color.Transparent,
            if (enabled) Blue500 else Gray400,
            BorderStroke(1.dp, if (enabled) Blue500 else Gray300)
        )
        LoginButtonType.TEXT -> Triple(
            Color.Transparent,
            if (enabled) Blue500 else Gray400,
            null
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
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
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
































