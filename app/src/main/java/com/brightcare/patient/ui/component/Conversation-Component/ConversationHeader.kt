package com.brightcare.patient.ui.component.conversationcomponent

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.brightcare.patient.ui.theme.*
import java.util.*

/**
 * Fixed header component for conversation screen
 */
@Composable
fun ConversationHeader(
    conversation: ChatConversation,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCallDialog by remember { mutableStateOf(false) }
    
    // Permission launcher for CALL_PHONE
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, make the call
            conversation.phoneNumber?.let { phoneNumber ->
                makePhoneCall(context, phoneNumber)
            }
        }
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Back Button
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back / Bumalik",
                    tint = Blue500
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar - Use profile image if available, otherwise fallback to icon
            if (!conversation.profileImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = conversation.profileImageUrl,
                    contentDescription = conversation.participantName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Gray100),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback to icon-based avatar
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = when (conversation.participantType) {
                        SenderType.DOCTOR -> Blue100
                        SenderType.ADMIN -> Orange100
                        SenderType.PATIENT -> Gray200
                        SenderType.NURSE -> Blue100
                        SenderType.SUPPORT -> Orange100
                    }
                ) {
                    Icon(
                        imageVector = when (conversation.participantType) {
                            SenderType.DOCTOR -> Icons.Default.LocalHospital
                            SenderType.ADMIN -> Icons.Default.Support
                            SenderType.PATIENT -> Icons.Default.Person
                            SenderType.NURSE -> Icons.Default.MedicalServices
                            SenderType.SUPPORT -> Icons.Default.Support
                        },
                        contentDescription = null,
                        tint = when (conversation.participantType) {
                            SenderType.DOCTOR -> Blue500
                            SenderType.ADMIN -> Orange500
                            SenderType.PATIENT -> Gray600
                            SenderType.NURSE -> Blue500
                            SenderType.SUPPORT -> Orange500
                        },
                        modifier = Modifier
                            .size(20.dp)
                            .wrapContentSize(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + Specialization
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.participantName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900
                    )
                )
                if (!conversation.specialization.isNullOrBlank()) {
                    Text(
                        text = conversation.specialization,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Gray600
                        )
                    )
                }
            }

            // Phone Call Button
            IconButton(
                onClick = { 
                    if (conversation.phoneNumber != null) {
                        showCallDialog = true
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent
                ),
                enabled = conversation.phoneNumber != null
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Phone call / Tawagan",
                    tint = if (conversation.phoneNumber != null) Blue500 else Gray400
                )
            }
        }
    }
    
    // Phone call confirmation dialog
    if (showCallDialog) {
        PhoneCallDialog(
            doctorName = conversation.participantName,
            phoneNumber = conversation.phoneNumber ?: "",
            onConfirm = {
                showCallDialog = false
                // Check permission and make call
                when (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE)) {
                    PackageManager.PERMISSION_GRANTED -> {
                        // Permission already granted, make the call
                        conversation.phoneNumber?.let { phoneNumber ->
                            makePhoneCall(context, phoneNumber)
                        }
                    }
                    else -> {
                        // Request permission
                        callPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
                    }
                }
            },
            onDismiss = {
                showCallDialog = false
            }
        )
    }
}

/**
 * Phone call confirmation dialog
 * Dialog para sa pagkumpirma ng tawag
 */
@Composable
private fun PhoneCallDialog(
    doctorName: String,
    phoneNumber: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Call Doctor / Tawagan ang Doktor",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Do you want to call $doctorName?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray700
                    )
                )
                Text(
                    text = "Gusto mo bang tawagan si $doctorName?",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Gray600
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Phone: $phoneNumber",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Blue500
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Blue500
                )
            ) {
                Text("Call / Tawagan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Gray600
                )
            ) {
                Text("Cancel / Kanselahin")
            }
        },
        containerColor = White,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Helper function to make phone call
 * Helper function para sa pagtawag
 */
private fun makePhoneCall(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error - could show a toast or log the error
        e.printStackTrace()
    }
}

@Preview(showBackground = true, name = "Conversation Header Preview")
@Composable
fun ConversationHeaderPreview() {
    BrightCarePatientTheme {
        ConversationHeader(
            conversation = ChatConversation(
                id = "1",
                participantName = "Dr. Maria Santos",
                participantType = SenderType.DOCTOR,
                lastMessage = "Good morning! How are you?",
                lastMessageTime = Date(),
                unreadCount = 0,
                isOnline = true,
                phoneNumber = "+63 912 345 6789",
                profileImageUrl = "https://example.com/profile.jpg",
                specialization = "Chiropractor"
            ),
            onBackClick = {}
        )
    }
}
