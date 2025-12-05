package com.brightcare.patient.ui.component.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.ui.screens.AuthenticationViewModel
import com.brightcare.patient.ui.theme.*

/**
 * Logout button component that can be used in any screen
 * Logout button component na pwedeng gamitin sa kahit anong screen
 */
@Composable
fun LogoutButton(
    navController: NavController,
    authViewModel: AuthenticationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    showConfirmDialog: Boolean = true,
    buttonText: String = "Logout",
    buttonStyle: LogoutButtonStyle = LogoutButtonStyle.FILLED
) {
    var showDialog by remember { mutableStateOf(false) }
    
    // Logout confirmation dialog
    if (showDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                authViewModel.logout()
                showDialog = false
                // Navigate to login screen
                navController.navigate(NavigationRoutes.LOGIN) {
                    // Clear the entire back stack
                    popUpTo(0) { inclusive = true }
                }
            },
            onDismiss = {
                showDialog = false
            }
        )
    }
    
    when (buttonStyle) {
        LogoutButtonStyle.FILLED -> {
            Button(
                onClick = {
                    if (showConfirmDialog) {
                        showDialog = true
                    } else {
                        authViewModel.logout()
                        navController.navigate(NavigationRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = modifier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Error,
                    contentColor = White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        LogoutButtonStyle.OUTLINED -> {
            OutlinedButton(
                onClick = {
                    if (showConfirmDialog) {
                        showDialog = true
                    } else {
                        authViewModel.logout()
                        navController.navigate(NavigationRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = modifier,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Error
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Error)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        LogoutButtonStyle.TEXT -> {
            TextButton(
                onClick = {
                    if (showConfirmDialog) {
                        showDialog = true
                    } else {
                        authViewModel.logout()
                        navController.navigate(NavigationRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = modifier,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        LogoutButtonStyle.ICON_ONLY -> {
            IconButton(
                onClick = {
                    if (showConfirmDialog) {
                        showDialog = true
                    } else {
                        authViewModel.logout()
                        navController.navigate(NavigationRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = modifier,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout"
                )
            }
        }
    }
}

/**
 * Logout confirmation dialog
 * Logout confirmation dialog
 */
@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Logout Confirmation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to logout? You will need to sign in again to access your account.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Error,
                    contentColor = White
                )
            ) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = White,
        titleContentColor = Gray900,
        textContentColor = Gray700
    )
}

/**
 * Logout button styles
 * Mga logout button style
 */
enum class LogoutButtonStyle {
    FILLED,      // Solid red button
    OUTLINED,    // Red border button
    TEXT,        // Text-only button
    ICON_ONLY    // Icon-only button
}

/**
 * Compact logout button for use in app bars or toolbars
 * Compact logout button para sa app bars o toolbars
 */
@Composable
fun CompactLogoutButton(
    navController: NavController,
    authViewModel: AuthenticationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    LogoutButton(
        navController = navController,
        authViewModel = authViewModel,
        modifier = modifier,
        buttonStyle = LogoutButtonStyle.ICON_ONLY,
        showConfirmDialog = true
    )
}

/**
 * Preview for LogoutButton
 */
@Preview(showBackground = true)
@Composable
fun LogoutButtonPreview() {
    BrightCarePatientTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LogoutButton(
                navController = rememberNavController(),
                buttonStyle = LogoutButtonStyle.FILLED
            )
            
            LogoutButton(
                navController = rememberNavController(),
                buttonStyle = LogoutButtonStyle.OUTLINED
            )
            
            LogoutButton(
                navController = rememberNavController(),
                buttonStyle = LogoutButtonStyle.TEXT
            )
            
            LogoutButton(
                navController = rememberNavController(),
                buttonStyle = LogoutButtonStyle.ICON_ONLY
            )
        }
    }
}
