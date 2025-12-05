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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onAttachmentClick: (MessageAttachment) -> Unit = {}
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
                                onImageClick = onImageClick
                            )
                        }
                        AttachmentType.FILE -> {
                            FileAttachment(
                                attachment = attachment,
                                isFromCurrentUser = isFromCurrentUser,
                                onAttachmentClick = onAttachmentClick
                            )
                        }
                        AttachmentType.VIDEO -> {
                            FileAttachment(
                                attachment = attachment,
                                isFromCurrentUser = isFromCurrentUser,
                                onAttachmentClick = onAttachmentClick
                            )
                        }
                        AttachmentType.AUDIO -> {
                            FileAttachment(
                                attachment = attachment,
                                isFromCurrentUser = isFromCurrentUser,
                                onAttachmentClick = onAttachmentClick
                            )
                        }
                        AttachmentType.DOCUMENT -> {
                            FileAttachment(
                                attachment = attachment,
                                isFromCurrentUser = isFromCurrentUser,
                                onAttachmentClick = onAttachmentClick
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onImageClick(attachment.url) },
        colors = CardDefaults.cardColors(
            containerColor = if (isFromCurrentUser) White.copy(alpha = 0.1f) else Gray50
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Image placeholder (in real implementation, use AsyncImage from Coil)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        if (isFromCurrentUser) White.copy(alpha = 0.2f) else Gray100,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Image / Larawan",
                    tint = if (isFromCurrentUser) White.copy(alpha = 0.7f) else Gray500,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Image name
            if (attachment.name.isNotEmpty()) {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isFromCurrentUser) White.copy(alpha = 0.9f) else Gray700,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(8.dp)
                )
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAttachmentClick(attachment) },
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
                    else -> Icons.Default.AttachFile
                },
                contentDescription = "File / File",
                tint = if (isFromCurrentUser) White.copy(alpha = 0.8f) else Blue500,
                modifier = Modifier.size(24.dp)
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
                    maxLines = 1
                )
                
                if (attachment.size > 0) {
                    Text(
                        text = com.brightcare.patient.ui.component.conversationcomponent.formatFileSize(attachment.size),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isFromCurrentUser) White.copy(alpha = 0.8f) else Gray600,
                            fontSize = 11.sp
                        )
                    )
                }
            }
            
            // Download icon
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download / I-download",
                tint = if (isFromCurrentUser) White.copy(alpha = 0.8f) else Gray600,
                modifier = Modifier.size(16.dp)
            )
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