package com.brightcare.patient.ui.component.conversationcomponent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.brightcare.patient.ui.component.messagecomponent.*
import com.brightcare.patient.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Individual message bubble with attachment support
 * Individual na message bubble na may attachment support
 */
@Composable
fun MessageBubble(
    message: ChatMessage,
    isFromCurrentUser: Boolean,
    modifier: Modifier = Modifier,
    onImageClick: (String) -> Unit = {},
    onAttachmentClick: (MessageAttachment) -> Unit = {},
    onDownloadClick: (MessageAttachment) -> Unit = {}
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromCurrentUser) Blue500 else White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Message text (if not empty)
                if (message.message.isNotEmpty()) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isFromCurrentUser) White else Gray900
                        )
                    )
                    
                    // Add spacing if there are attachments
                    if (message.attachments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // Attachments
                message.attachments.forEach { attachment ->
                    when (attachment.type) {
                        AttachmentType.IMAGE -> {
                            ImageAttachment(
                                attachment = attachment,
                                isFromCurrentUser = isFromCurrentUser,
                                onImageClick = onImageClick,
                                onDownloadClick = onDownloadClick
                            )
                        }
                        AttachmentType.FILE -> {
                            FileAttachment(
                                attachment = attachment,
                                isFromCurrentUser = isFromCurrentUser,
                                onAttachmentClick = onAttachmentClick,
                                onDownloadClick = onDownloadClick
                            )
                        }
                        AttachmentType.VIDEO -> {
                            FileAttachment(
                                attachment = attachment,
                                isFromCurrentUser = isFromCurrentUser,
                                onAttachmentClick = onAttachmentClick,
                                onDownloadClick = onDownloadClick
                            )
                        }
                        AttachmentType.AUDIO -> {
                            FileAttachment(
                                attachment = attachment,
                                isFromCurrentUser = isFromCurrentUser,
                                onAttachmentClick = onAttachmentClick,
                                onDownloadClick = onDownloadClick
                            )
                        }
                        AttachmentType.DOCUMENT -> {
                            FileAttachment(
                                attachment = attachment,
                                isFromCurrentUser = isFromCurrentUser,
                                onAttachmentClick = onAttachmentClick,
                                onDownloadClick = onDownloadClick
                            )
                        }
                    }
                    
                    // Add spacing between attachments
                    if (attachment != message.attachments.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Time and status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormat.format(message.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isFromCurrentUser) White.copy(alpha = 0.8f) else Gray500
                        )
                    )
                    
                    if (isFromCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                            contentDescription = if (message.isRead) "Read / Nabasa" else "Delivered / Naipadala",
                            tint = if (message.isRead) Success else White.copy(alpha = 0.8f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Image attachment component
 * Image attachment component
 */
@Composable
private fun ImageAttachment(
    attachment: MessageAttachment,
    isFromCurrentUser: Boolean,
    onImageClick: (String) -> Unit,
    onDownloadClick: (MessageAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isFromCurrentUser) White.copy(alpha = 0.1f) else Gray50
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Image with AsyncImage from Coil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { onImageClick(attachment.url) }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(attachment.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Image / Larawan",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(android.R.drawable.ic_menu_gallery),
                    placeholder = painterResource(android.R.drawable.ic_menu_gallery)
                )
                
                // View/Download overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // View button
                        IconButton(
                            onClick = { onImageClick(attachment.url) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "View / Tingnan",
                                tint = White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Download button
                        IconButton(
                            onClick = { onDownloadClick(attachment) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download / I-download",
                                tint = White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Image name and size
            if (attachment.name.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = attachment.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isFromCurrentUser) White.copy(alpha = 0.9f) else Gray700,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (attachment.size > 0) {
                        Text(
                            text = formatFileSize(attachment.size),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isFromCurrentUser) White.copy(alpha = 0.7f) else Gray500,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * File attachment component
 * File attachment component
 */
@Composable
private fun FileAttachment(
    attachment: MessageAttachment,
    isFromCurrentUser: Boolean,
    onAttachmentClick: (MessageAttachment) -> Unit,
    onDownloadClick: (MessageAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isFromCurrentUser) White.copy(alpha = 0.1f) else Gray50
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File icon
            Icon(
                imageVector = when {
                    attachment.name.endsWith(".pdf", ignoreCase = true) -> Icons.Default.PictureAsPdf
                    attachment.name.endsWith(".doc", ignoreCase = true) || 
                    attachment.name.endsWith(".docx", ignoreCase = true) -> Icons.Default.Description
                    attachment.mimeType.startsWith("image/") -> Icons.Default.Image
                    else -> Icons.Default.AttachFile
                },
                contentDescription = "File / File",
                tint = if (isFromCurrentUser) White.copy(alpha = 0.8f) else Blue500,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // File info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isFromCurrentUser) White else Gray900,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 2
                )
                
                if (attachment.size > 0) {
                    Text(
                        text = formatFileSize(attachment.size),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isFromCurrentUser) White.copy(alpha = 0.8f) else Gray600,
                            fontSize = 11.sp
                        )
                    )
                }
                
                // MIME type info
                if (attachment.mimeType.isNotEmpty()) {
                    Text(
                        text = attachment.mimeType,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isFromCurrentUser) White.copy(alpha = 0.6f) else Gray500,
                            fontSize = 10.sp
                        )
                    )
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View/Open button
                IconButton(
                    onClick = { onAttachmentClick(attachment) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isFromCurrentUser) White.copy(alpha = 0.2f) else Blue100
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open / Buksan",
                        tint = if (isFromCurrentUser) White else Blue500,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Download button
                IconButton(
                    onClick = { onDownloadClick(attachment) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isFromCurrentUser) White.copy(alpha = 0.2f) else Green100
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download / I-download",
                        tint = if (isFromCurrentUser) White else Green500,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}


/**
 * Preview for MessageBubble
 */
@Preview(
    showBackground = true,
    name = "Message Bubble Preview"
)
@Composable
fun MessageBubblePreview() {
    BrightCarePatientTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Text message from doctor
            MessageBubble(
                message = ChatMessage(
                    id = "1",
                    senderId = "doctor1",
                    senderName = "Dr. Maria Santos",
                    senderType = SenderType.DOCTOR,
                    message = "Good morning! How are you feeling today?",
                    timestamp = Date(),
                    isRead = true,
                    attachments = emptyList()
                ),
                isFromCurrentUser = false
            )
            
            // Text message from patient
            MessageBubble(
                message = ChatMessage(
                    id = "2",
                    senderId = "patient1",
                    senderName = "You",
                    senderType = SenderType.PATIENT,
                    message = "Hi Doctor! I'm feeling much better, thank you.",
                    timestamp = Date(),
                    isRead = false,
                    attachments = emptyList()
                ),
                isFromCurrentUser = true
            )
            
            // Message with image attachment
            MessageBubble(
                message = ChatMessage(
                    id = "3",
                    senderId = "patient1",
                    senderName = "You",
                    senderType = SenderType.PATIENT,
                    message = "Here's the X-ray you requested",
                    timestamp = Date(),
                    isRead = false,
                    attachments = listOf(
                        MessageAttachment(
                            id = "att1",
                            name = "xray_result.jpg",
                            url = "https://example.com/xray.jpg",
                            type = AttachmentType.IMAGE,
                            size = 1024000
                        )
                    )
                ),
                isFromCurrentUser = true
            )
        }
    }
}