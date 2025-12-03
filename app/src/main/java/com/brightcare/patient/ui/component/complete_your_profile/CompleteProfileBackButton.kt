package com.brightcare.patient.ui.component.complete_your_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.brightcare.patient.ui.theme.*

@Composable
fun CompleteProfileBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Gray50,
    iconColor: Color = Gray700
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
