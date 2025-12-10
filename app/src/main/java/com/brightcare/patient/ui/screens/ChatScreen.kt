package com.brightcare.patient.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.brightcare.patient.ui.component.conversationcomponent.MessageInputArea
import com.brightcare.patient.ui.component.conversationcomponent.MessageBubble
import com.brightcare.patient.ui.component.conversationcomponent.ConversationHeader
import com.brightcare.patient.ui.component.conversationcomponent.ChatConversation
import com.brightcare.patient.ui.component.conversationcomponent.SenderType
import com.brightcare.patient.ui.component.conversationcomponent.ImageViewerDialog
import com.brightcare.patient.ui.component.conversationcomponent.MessageAttachment
import com.brightcare.patient.utils.DownloadHelper
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.ChatViewModel
import com.brightcare.patient.navigation.NavigationRoutes
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Chat screen for individual conversation with chiropractor
 * Chat screen para sa individual conversation sa chiropractor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    // Check if this is a new conversation (starts with "new_")
    // I-check kung ito ay bagong conversation (nagsisimula sa "new_")
    val isNewConversation = conversationId.startsWith("new_")
    val chiropractorId = if (isNewConversation) {
        conversationId.removePrefix("new_")
    } else {
        null
    }
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val chiropractor by viewModel.chiropractor.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Image picker states
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Image viewer states
    var showImageViewer by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }
    var selectedImageName by remember { mutableStateOf("") }

    // Create temporary file for camera
    val createImageFile = remember {
        {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = File(context.cacheDir, "images")
            storageDir.mkdirs()
            File.createTempFile(imageFileName, ".jpg", storageDir)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            if (isNewConversation && chiropractorId != null) {
                // Create new conversation and send first image message
                viewModel.sendFirstImageMessage(chiropractorId, it, fileName)
            } else {
                viewModel.sendImageMessage(it, fileName)
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraImageUri?.let { uri ->
                val fileName = "camera_${System.currentTimeMillis()}.jpg"
                if (isNewConversation && chiropractorId != null) {
                    // Create new conversation and send first image message
                    viewModel.sendFirstImageMessage(chiropractorId, uri, fileName)
                } else {
                    viewModel.sendImageMessage(uri, fileName)
                }
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, launch camera
            try {
                val imageFile = createImageFile()
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    imageFile
                )
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                println("📷 Error launching camera after permission: ${e.message}")
            }
        } else {
            println("📷 Camera permission denied")
        }
    }

    // Document picker launcher
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "document_${System.currentTimeMillis()}"
            val mimeType = context.contentResolver.getType(it) ?: "application/octet-stream"
            if (isNewConversation && chiropractorId != null) {
                // Create new conversation and send first file message
                viewModel.sendFirstFileMessage(chiropractorId, it, fileName, mimeType)
            } else {
                viewModel.sendFileMessage(it, fileName, mimeType)
            }
        }
    }

    // Load conversation when screen opens
    LaunchedEffect(conversationId) {
        if (isNewConversation && chiropractorId != null) {
            // Load chiropractor info for new conversation
            // I-load ang chiropractor info para sa bagong conversation
            viewModel.loadChiropractorForNewConversation(chiropractorId)
        } else {
            // Load existing conversation
            // I-load ang existing conversation
            println("🔄 ChatScreen: Loading conversation and marking as read: $conversationId")
            viewModel.loadConversation(conversationId)
        }
    }

    // Additional safety: Mark conversation as read when screen becomes visible
    // Karagdagang safety: I-mark ang conversation bilang nabasa kapag naging visible ang screen
    LaunchedEffect(conversationId, messages.isNotEmpty()) {
        if (!isNewConversation && messages.isNotEmpty()) {
            println("🔄 ChatScreen: Additional mark as read for safety: $conversationId")
            viewModel.forceMarkAsReadSetToZero()
        }
    }

    // Auto-scroll to bottom when new message is added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        // Use ConversationHeader component - Always show header
        ConversationHeader(
            conversation = ChatConversation(
                id = conversationId,
                participantName = chiropractor?.fullName ?: "Loading...",
                participantType = SenderType.DOCTOR,
                lastMessage = "", // Not needed for header
                lastMessageTime = Date(), // Not needed for header
                unreadCount = 0, // Not needed for header
                isOnline = chiropractor?.isAvailable ?: false,
                profileImageUrl = chiropractor?.profileImage,
                phoneNumber = chiropractor?.phoneNumber,
                specialization = chiropractor?.specialization
            ),
            onBackClick = { 
                // Navigate reliably back to message screen - always works regardless of navigation stack
                // Maaasahang pagbabalik sa message screen - laging gumagana kahit anong navigation stack
                println("🔙 Back button clicked in ChatScreen - navigating to message screen")
                
                try {
                    // Clear all chat screens from back stack and go to message screen
                    // I-clear ang lahat ng chat screen sa back stack at pumunta sa message screen
                    navController.navigate(NavigationRoutes.MAIN_DASHBOARD + "?initialRoute=message") {
                        // Clear everything up to and including main dashboard, then recreate it
                        // I-clear ang lahat hanggang main dashboard, tapos gumawa ulit
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                        restoreState = false // Don't restore state to ensure fresh message screen
                    }
                    println("🔙 Successfully navigated to message screen")
                } catch (e: Exception) {
                    println("🔙 Navigation error: ${e.message}")
                    // Robust fallback: Force navigate to main dashboard
                    // Matatag na fallback: Pilitin ang pagpunta sa main dashboard
                    try {
                        // Use the most basic navigation possible
                        // Gamitin ang pinaka-basic na navigation
                        navController.navigate(NavigationRoutes.MAIN_DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                        println("🔙 Fallback navigation to main dashboard successful")
                    } catch (fallbackError: Exception) {
                        println("🔙 All navigation attempts failed: ${fallbackError.message}")
                        // Last resort: Try to pop back stack
                        // Huling paraan: Subukang mag-pop back stack
                        try {
                            val popResult = navController.popBackStack()
                            println("🔙 PopBackStack result: $popResult")
                        } catch (popError: Exception) {
                            println("🔙 PopBackStack also failed: ${popError.message}")
                        }
                    }
                }
            }
        )

        // Show error if any
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = viewModel::clearError
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }

        if (uiState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Blue500)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading conversation...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                }
            }
        } else {
            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = false
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Start your conversation with ${chiropractor?.fullName ?: "your chiropractor"}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Gray600
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Send a message to begin",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gray500
                                )
                            }
                        }
                    }
                } else {
                    items(messages) { message ->
                        MessageBubble(
                            message = viewModel.convertToUiMessage(message),
                            isFromCurrentUser = viewModel.isCurrentUser(message.senderId),
                            onImageClick = { imageUrl ->
                                selectedImageUrl = imageUrl
                                selectedImageName = message.fileName ?: "Image"
                                showImageViewer = true
                            },
                            onAttachmentClick = { attachment ->
                                // Open file with appropriate app
                                DownloadHelper.openFile(
                                    context = context,
                                    attachment = attachment,
                                    onError = { error ->
                                        println("❌ Error opening file: $error")
                                    }
                                )
                            },
                            onDownloadClick = { attachment ->
                                // Download file
                                DownloadHelper.downloadFile(
                                    context = context,
                                    attachment = attachment,
                                    onSuccess = {
                                        println("✅ Download started: ${attachment.name}")
                                    },
                                    onError = { error ->
                                        println("❌ Download error: $error")
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // Show upload progress
            uploadProgress.forEach { (messageId, progress) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Uploading: ${progress.fileName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        LinearProgressIndicator(
                            progress = { progress.progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (progress.error != null) {
                            Text(
                                text = "Error: ${progress.error}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // Message input area
        MessageInputArea(
            messageText = messageText,
            onMessageTextChange = viewModel::updateMessageText,
            onSendMessage = {
                if (isNewConversation && chiropractorId != null) {
                    // Create conversation and send first message
                    // Gumawa ng conversation at magpadala ng unang mensahe
                    viewModel.sendFirstMessage(chiropractorId, messageText)
                } else {
                    // Send message to existing conversation
                    // Magpadala ng mensahe sa existing conversation
                    viewModel.sendTextMessage()
                }
                keyboardController?.hide()
            },
            onImageClick = {
                // Launch gallery picker
                // I-launch ang gallery picker
                println("📷 Gallery picker launched")
                galleryLauncher.launch("image/*")
            },
            onCameraClick = {
                // Check camera permission first
                // I-check muna ang camera permission
                println("📷 Camera button clicked")
                val hasCameraPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                
                if (hasCameraPermission) {
                    // Permission already granted, launch camera directly
                    try {
                        val imageFile = createImageFile()
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            imageFile
                        )
                        cameraImageUri = uri
                        cameraLauncher.launch(uri)
                    } catch (e: Exception) {
                        println("📷 Error launching camera: ${e.message}")
                    }
                } else {
                    // Request camera permission
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onDocumentClick = {
                // Launch document picker (PDF, DOC, DOCX)
                // I-launch ang document picker (PDF, DOC, DOCX)
                println("📄 Document picker launched")
                documentLauncher.launch("*/*") // Accept all file types, but we'll filter in the picker
            }
        )
        
        // Image viewer dialog
        if (showImageViewer) {
            ImageViewerDialog(
                imageUrl = selectedImageUrl,
                imageName = selectedImageName,
                onDismiss = {
                    showImageViewer = false
                    selectedImageUrl = ""
                    selectedImageName = ""
                },
                onDownload = {
                    // Download the image
                    val attachment = MessageAttachment(
                        id = "temp_download",
                        name = selectedImageName,
                        url = selectedImageUrl,
                        type = com.brightcare.patient.ui.component.conversationcomponent.AttachmentType.IMAGE,
                        mimeType = "image/jpeg"
                    )
                    DownloadHelper.downloadFile(
                        context = context,
                        attachment = attachment,
                        onSuccess = {
                            println("✅ Image download started: $selectedImageName")
                        },
                        onError = { error ->
                            println("❌ Image download error: $error")
                        }
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    BrightCarePatientTheme {
        ChatScreen(
            conversationId = "sample_conversation",
            navController = rememberNavController()
        )
    }
}
