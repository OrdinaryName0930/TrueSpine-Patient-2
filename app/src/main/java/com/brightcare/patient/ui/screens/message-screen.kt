package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.component.conversationcomponent.ConversationComponent
import com.brightcare.patient.ui.component.messagecomponent.SimpleMessageSearch
import com.brightcare.patient.ui.viewmodel.ConversationListViewModel
import com.brightcare.patient.ui.viewmodel.ChiropractorDisplayItem
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.ui.component.chirocomponents.*

/**
 * Message screen - Chat with healthcare providers
 * Screen para sa pakikipag-chat sa mga healthcare provider
 * Following ChiroScreen structure with integrated search functionality
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MessageScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ConversationListViewModel = hiltViewModel()
) {
    // Collect state from ViewModel (similar to ChiroScreen pattern)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val displayChiropractors by viewModel.getDisplayChiropractors().collectAsStateWithLifecycle()

    // Pull to refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.refreshData()
        }
    )

    // Handle refresh completion
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 12.dp,
                    top = 16.dp,
                    end = 12.dp
                )
        ) {
        // Header (following ChiroScreen pattern)
        ChiroHeader(
            title = "Messages",
            subtitle = "Chat with chiropractors",
            onSearchClick = { 
                // No refresh functionality - just focus on search
                // Pull to refresh is now the main refresh method
            }
        )

        // Search bar (maintaining SimpleMessageSearch.kt)
        SimpleMessageSearch(
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            placeholder = "Search chiropractors...",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Content based on loading state (following ChiroScreen pattern)
        when {
            uiState.isLoading -> {
                // Loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Blue500
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading chiropractors...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Gray600
                            )
                        )
                    }
                }
            }
            
            uiState.error != null -> {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading chiropractors",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Error,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = uiState.error ?: "Unknown error occurred",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Gray600
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refreshData() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Blue500
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            displayChiropractors.isEmpty() -> {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No chiropractors found" else "No chiropractors available",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Gray600,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try adjusting your search" else "Please try again later",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Gray500
                            )
                        )
                    }
                }
            }
            
            else -> {
                // Chiropractors list using ConversationComponent
                // This maintains the existing functionality while following ChiroScreen structure
                ConversationComponent(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        }

        // Pull refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = White,
            contentColor = Blue500
        )
    }
}


@Preview(
    showBackground = true,
    name = "Message Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun MessageScreenPreview() {
    BrightCarePatientTheme {
        // Preview with sample UI structure since we can't use ViewModel in preview
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteBg)
                .padding(
                    start = 12.dp,
                    top = 16.dp,
                    end = 12.dp
                )
        ) {
            ChiroHeader(
                title = "Messages",
                subtitle = "Chat with chiropractors",
                onSearchClick = { }
            )
            
            SimpleMessageSearch(
                searchQuery = "",
                onSearchQueryChange = { },
                placeholder = "Search chiropractors...",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Sample preview - in real app, ConversationComponent will show here
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chiropractors will appear here",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray600
                    )
                )
            }
        }
    }
}
