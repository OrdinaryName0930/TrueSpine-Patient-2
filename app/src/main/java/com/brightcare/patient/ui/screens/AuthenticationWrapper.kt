package com.brightcare.patient.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.brightcare.patient.navigation.NavigationGraph
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.utils.AuthenticationManager
import com.brightcare.patient.utils.OnboardingPreferences
import javax.inject.Inject

/**
 * Authentication wrapper that determines the start destination based on login state
 * Authentication wrapper na nagtutukoy ng start destination base sa login state
 */
@Composable
fun AuthenticationWrapper(
    navController: NavHostController,
    authenticationManager: AuthenticationManager,
    onFinishActivity: () -> Unit = {},
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val context = LocalContext.current
    val isLoggedIn by authenticationManager.isLoggedIn.collectAsState()
    
    // Determine start destination based on authentication and onboarding state
    val startDestination = remember(isLoggedIn) {
        when {
            // Check if onboarding has been seen
            !OnboardingPreferences.hasSeenOnboarding(context) -> NavigationRoutes.ONBOARDING
            // If logged in, go to main dashboard
            isLoggedIn -> NavigationRoutes.MAIN_DASHBOARD
            // Otherwise, go to login
            else -> NavigationRoutes.LOGIN
        }
    }
    
    // Refresh auth state when the composable is first created
    LaunchedEffect(Unit) {
        authenticationManager.refreshAuthState()
    }
    
    NavigationGraph(
        navController = navController,
        startDestination = startDestination,
        onFinishActivity = onFinishActivity,
        modifier = modifier
    )
}

/**
 * ViewModel for managing authentication state across the app
 * ViewModel para sa pag-manage ng authentication state sa buong app
 */
@dagger.hilt.android.lifecycle.HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager
) : androidx.lifecycle.ViewModel() {
    
    val isLoggedIn = authenticationManager.isLoggedIn
    val currentUserId = authenticationManager.currentUserId
    val currentUserEmail = authenticationManager.currentUserEmail
    val currentUserName = authenticationManager.currentUserName
    
    /**
     * Logout function accessible from any screen
     * Logout function na pwedeng ma-access mula sa kahit anong screen
     */
    fun logout() {
        authenticationManager.clearLoginState()
    }
    
    /**
     * Check if session is still valid
     * Tingnan kung valid pa ang session
     */
    fun isSessionValid(): Boolean {
        return authenticationManager.isSessionValid()
    }
    
    /**
     * Get current user info
     * Kumuha ng current user info
     */
    fun getCurrentUserInfo(): Triple<String?, String?, String?> {
        return Triple(
            authenticationManager.currentUserId.value,
            authenticationManager.currentUserEmail.value,
            authenticationManager.currentUserName.value
        )
    }
}
