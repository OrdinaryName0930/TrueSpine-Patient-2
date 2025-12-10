package com.brightcare.patient.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.component.signup_component.*
import com.brightcare.patient.ui.component.termsandconditions_and_privacypolicy.TermsAgreeButton
import com.brightcare.patient.ui.component.termsandconditions_and_privacypolicy.TermsBackButton
import com.brightcare.patient.ui.component.termsandconditions_and_privacypolicy.TermsContent
import com.brightcare.patient.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(
    navController: NavController,
    onAgreeClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var isLoading by remember { mutableStateOf(false) }

    // Handle system back button
    BackHandler {
        val wasManuallyChecked =
            navController.previousBackStackEntry?.savedStateHandle?.get<Boolean>("preserve_checkbox") ?: false
        if (!wasManuallyChecked) {
            navController.previousBackStackEntry?.savedStateHandle?.set("terms_agreed", false)
        }
        navController.popBackStack()
    }

    // Detect if scrolled to bottom
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.lastOrNull()
                val lastItemIndex = layoutInfo.totalItemsCount - 1
                lastVisibleItem?.index == lastItemIndex
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentSize(Alignment.Center) // 👈 centers text vertically
                    ) {
                        Text(
                            text = "Terms & Privacy Policy",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Blue500,
                                fontSize = 18.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 12.dp)
                            .wrapContentSize(Alignment.Center) // 👈 centers icon vertically
                    ) {
                        TermsBackButton(
                            onClick = {
                                val wasManuallyChecked = navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.get<Boolean>("preserve_checkbox") ?: false
                                if (!wasManuallyChecked) {
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("terms_agreed", false)
                                }
                                navController.popBackStack()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WhiteBg,
                    titleContentColor = Gray900,
                    navigationIconContentColor = Gray700
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhiteBg)
                    .height(60.dp), // 👈 slightly taller AppBar (you can reduce to 56.dp if preferred)
                windowInsets = WindowInsets(0) // 👈 removes default system top padding
            )
        },
        containerColor = WhiteBg,
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable content
            TermsContent(
                listState = listState,
                modifier = Modifier.fillMaxSize()
            )

            // Scroll indicator
            if (!isAtBottom) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(color = White.copy(alpha = 0.9f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Scroll down to read all terms and conditions",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // "I Agree" button
            if (isAtBottom) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(color = WhiteBg.copy(alpha = 0.90f))
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    TermsAgreeButton(
                        text = "I Agree",
                        onClick = {
                            isLoading = true
                            
                            // Check if we came from Complete Profile
                            val previousRoute = navController.previousBackStackEntry?.destination?.route
                            if (previousRoute == "complete_profile") {
                                // Set terms agreed for Complete Profile
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("terms_agreed_complete_profile", true)
                            } else {
                                // Original signup flow
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("preserve_checkbox", false)
                            }
                            
                            onAgreeClicked()
                            navController.popBackStack()
                        },
                        loading = isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// MARK: - Preview Composables

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TermsAndConditionsScreenPreview() {
    BrightCarePatientTheme {
        TermsAndConditionsScreen(
            navController = rememberNavController(),
            onAgreeClicked = {
                // Preview action
            }
        )
    }
}
