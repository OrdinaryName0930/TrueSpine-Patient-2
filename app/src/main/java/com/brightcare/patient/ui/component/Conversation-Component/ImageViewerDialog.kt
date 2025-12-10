package com.brightcare.patient.ui.component.conversationcomponent

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.brightcare.patient.ui.theme.*

/**
 * Full screen image viewer dialog
 * Full screen image viewer dialog
 */
@Composable
fun ImageViewerDialog(
    imageUrl: String,
    imageName: String = "",
    onDismiss: () -> Unit,
    onDownload: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            // Main image
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Full screen image / Full screen na larawan",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { /* Prevent dismiss when clicking image */ },
                contentScale = ContentScale.Fit,
                error = painterResource(android.R.drawable.ic_menu_gallery),
                placeholder = painterResource(android.R.drawable.ic_menu_gallery)
            )
            
            // Top bar with close and actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close / Isara",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Share button
                    IconButton(
                        onClick = {
                            shareImage(context, imageUrl, imageName)
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share / I-share",
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Download button
                    IconButton(
                        onClick = onDownload,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download / I-download",
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Bottom info bar
            if (imageName.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = imageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Share image function
 * Function para sa pag-share ng image
 */
private fun shareImage(context: Context, imageUrl: String, imageName: String) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this image: $imageUrl")
            putExtra(Intent.EXTRA_SUBJECT, imageName.ifEmpty { "Shared Image" })
        }
        
        val chooserIntent = Intent.createChooser(shareIntent, "Share Image")
        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        // Handle error silently or show toast
        e.printStackTrace()
    }
}







