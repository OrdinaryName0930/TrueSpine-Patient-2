package com.brightcare.patient.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.brightcare.patient.navigation.NavigationGraph
import com.brightcare.patient.navigation.NavigationRoutes
import com.brightcare.patient.utils.AuthenticationManager
import com.brightcare.patient.utils.OnboardingPreferences
import com.brightcare.patient.data.repository.CompleteProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import android.util.Log
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel

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
    val authViewModel: AuthenticationViewModel = hiltViewModel()
    
    // State for profile completion check
    var isProfileComplete by remember { mutableStateOf<Boolean?>(null) }
    var isCheckingProfile by remember { mutableStateOf(false) }
    
    // Check profile completion when user is logged in
    LaunchedEffect(isLoggedIn) {
        Log.d("AuthenticationWrapper", "LaunchedEffect triggered - isLoggedIn: $isLoggedIn, isProfileComplete: $isProfileComplete, isCheckingProfile: $isCheckingProfile")
        
        if (isLoggedIn && isProfileComplete == null && !isCheckingProfile) {
            isCheckingProfile = true
            Log.d("AuthenticationWrapper", "User is logged in, checking profile completion...")
            
            authViewModel.checkProfileCompletion { completed ->
                isProfileComplete = completed
                isCheckingProfile = false
                Log.d("AuthenticationWrapper", "Profile completion check result: $completed")
            }
        } else if (!isLoggedIn) {
            // Reset profile completion state when user logs out
            Log.d("AuthenticationWrapper", "User is not logged in, resetting profile completion state")
            isProfileComplete = null
        }
    }
    
    // Navigate to appropriate screen when profile completion status changes
    LaunchedEffect(isProfileComplete) {
        if (isLoggedIn && isProfileComplete == true && navController.currentDestination?.route == NavigationRoutes.COMPLETE_PROFILE) {
            Log.d("AuthenticationWrapper", "Profile completed, navigating to home...")
            navController.navigate(NavigationRoutes.MAIN_DASHBOARD) {
                popUpTo(NavigationRoutes.COMPLETE_PROFILE) { inclusive = true }
            }
        }
    }
    
    // Determine start destination based on authentication, profile completion, onboarding, and permission state
    val startDestination = remember(isLoggedIn, isProfileComplete) {
        Log.d("AuthenticationWrapper", "Determining start destination - isLoggedIn: $isLoggedIn, isProfileComplete: $isProfileComplete")
        
        when {
            // Check if onboarding has been seen
            !OnboardingPreferences.hasSeenOnboarding(context) -> {
                Log.d("AuthenticationWrapper", "Start destination: ONBOARDING (onboarding not seen)")
                NavigationRoutes.ONBOARDING
            }
            // Check if permissions have been requested (only if onboarding is done)
            OnboardingPreferences.hasSeenOnboarding(context) && 
            !OnboardingPreferences.hasRequestedPermissions(context) -> {
                Log.d("AuthenticationWrapper", "Start destination: PERMISSIONS (permissions not requested)")
                NavigationRoutes.PERMISSIONS
            }
            // If logged in, check profile completion
            isLoggedIn -> {
                when (isProfileComplete) {
                    true -> {
                        Log.d("AuthenticationWrapper", "Start destination: MAIN_DASHBOARD (logged in, profile complete)")
                        NavigationRoutes.MAIN_DASHBOARD
                    }
                    false -> {
                        Log.d("AuthenticationWrapper", "Start destination: COMPLETE_PROFILE (logged in, profile incomplete)")
                        NavigationRoutes.COMPLETE_PROFILE
                    }
                    null -> {
                        Log.d("AuthenticationWrapper", "Start destination: LOGIN (checking profile completion...)")
                        // Still checking profile completion, show login for now
                        NavigationRoutes.LOGIN
                    }
                }
            }
            // Otherwise, go to login
            else -> {
                Log.d("AuthenticationWrapper", "Start destination: LOGIN (not logged in)")
                NavigationRoutes.LOGIN
            }
        }
    }
    
    // Sync with Firebase Auth and refresh auth state when the composable is first created
    LaunchedEffect(Unit) {
        authenticationManager.syncWithFirebaseAuth()
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
    private val authenticationManager: AuthenticationManager,
    private val completeProfileRepository: CompleteProfileRepository
) : androidx.lifecycle.ViewModel() {
    
    val isLoggedIn = authenticationManager.isLoggedIn
    val currentUserId = authenticationManager.currentUserId
    val currentUserEmail = authenticationManager.currentUserEmail
    val currentUserName = authenticationManager.currentUserName
    
    // Profile data state flows
    private val _firstName = MutableStateFlow<String?>(null)
    val firstName: StateFlow<String?> = _firstName.asStateFlow()
    
    private val _lastName = MutableStateFlow<String?>(null)
    val lastName: StateFlow<String?> = _lastName.asStateFlow()
    
    // Change password state
    private val _changePasswordResult = MutableStateFlow<Result<Unit>?>(null)
    val changePasswordResult: StateFlow<Result<Unit>?> = _changePasswordResult.asStateFlow()
    
    companion object {
        private const val TAG = "AuthenticationViewModel"
    }
    
    init {
        // Load profile data when ViewModel is created
        loadProfileData()
    }
    
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
    
    /**
     * Get login timestamp
     * Kumuha ng login timestamp
     */
    fun getLoginTimestamp(): Long {
        return authenticationManager.getLoginTimestamp()
    }
    
    /**
     * Load profile data from Firestore
     * I-load ang profile data mula sa Firestore
     */
    private fun loadProfileData() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading profile data...")
                val result = completeProfileRepository.getProfileData()
                
                result.onSuccess { profileData ->
                    if (profileData != null) {
                        _firstName.value = profileData.firstName
                        _lastName.value = profileData.lastName
                        Log.d(TAG, "Profile data loaded: ${profileData.firstName} ${profileData.lastName}")
                    } else {
                        Log.d(TAG, "No profile data found")
                        _firstName.value = null
                        _lastName.value = null
                    }
                }.onFailure { exception ->
                    Log.e(TAG, "Failed to load profile data: ${exception.message}")
                    _firstName.value = null
                    _lastName.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile data", e)
                _firstName.value = null
                _lastName.value = null
            }
        }
    }
    
    /**
     * Refresh profile data
     * I-refresh ang profile data
     */
    fun refreshProfileData() {
        loadProfileData()
    }
    
    /**
     * Change user password
     * Palitan ang password ng user
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Attempting to change password...")
                val user = FirebaseAuth.getInstance().currentUser
                
                if (user?.email == null) {
                    _changePasswordResult.value = Result.failure(Exception("User not authenticated"))
                    return@launch
                }
                
                // Re-authenticate user with current password
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                
                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        // Update password
                        user.updatePassword(newPassword)
                            .addOnSuccessListener {
                                Log.d(TAG, "Password changed successfully")
                                _changePasswordResult.value = Result.success(Unit)
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Failed to update password: ${exception.message}")
                                _changePasswordResult.value = Result.failure(exception)
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to re-authenticate: ${exception.message}")
                        _changePasswordResult.value = Result.failure(Exception("Current password is incorrect"))
                    }
                    
            } catch (e: Exception) {
                Log.e(TAG, "Error changing password", e)
                _changePasswordResult.value = Result.failure(e)
            }
        }
    }
    
    /**
     * Clear change password result
     * I-clear ang change password result
     */
    fun clearChangePasswordResult() {
        _changePasswordResult.value = null
    }
    
    /**
     * Check if user profile is completed
     * Tingnan kung tapos na ang user profile
     */
    fun checkProfileCompletion(callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Checking profile completion...")
                val result = completeProfileRepository.isProfileCompleted()
                
                result.onSuccess { completed ->
                    Log.d(TAG, "Profile completion check result: $completed")
                    callback(completed)
                }.onFailure { exception ->
                    Log.e(TAG, "Failed to check profile completion: ${exception.message}")
                    // On error, assume profile is incomplete to be safe
                    callback(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking profile completion", e)
                callback(false)
            }
        }
    }
}
