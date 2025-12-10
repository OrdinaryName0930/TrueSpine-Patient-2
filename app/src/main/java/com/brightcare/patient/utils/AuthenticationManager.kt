package com.brightcare.patient.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

/**
 * AuthenticationManager - Handles persistent login state
 * Manages user authentication state across app sessions
 * 
 * AuthenticationManager - Nag-handle ng persistent login state
 * Nag-manage ng user authentication state sa lahat ng app sessions
 */
@Singleton
class AuthenticationManager(
    private val context: Context
) {
    companion object {
        private const val TAG = "AuthenticationManager"
        private const val PREFS_NAME = "brightcare_auth_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
    }
    
    // Firebase Auth instance
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // SharedPreferences for storing authentication data
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Authentication state flow
    private val _isLoggedIn = MutableStateFlow(checkLoginState())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // User data flows
    private val _currentUserId = MutableStateFlow(getCurrentUserId())
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _currentUserEmail = MutableStateFlow(getCurrentUserEmail())
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentUserName = MutableStateFlow(getCurrentUserName())
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()

    /**
     * Check if user is currently logged in
     * Tingnan kung naka-login ang user
     */
    private fun checkLoginState(): Boolean {
        val localLoginState = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        val firebaseUser = firebaseAuth.currentUser
        
        // Both local state and Firebase Auth must be valid
        val isActuallyLoggedIn = localLoginState && firebaseUser != null
        
        // If there's a mismatch, clear local state to stay in sync
        if (localLoginState && firebaseUser == null) {
            Log.w(TAG, "Local login state is true but Firebase user is null. Clearing local state.")
            clearLoginState()
            return false
        }
        
        Log.d(TAG, "Login state check - Local: $localLoginState, Firebase: ${firebaseUser != null}, Result: $isActuallyLoggedIn")
        return isActuallyLoggedIn
    }

    /**
     * Save login state after successful authentication
     * I-save ang login state pagkatapos ng successful authentication
     */
    fun saveLoginState(
        userId: String,
        userEmail: String,
        userName: String,
        accessToken: String? = null,
        refreshToken: String? = null
    ) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, userEmail)
            putString(KEY_USER_NAME, userName)
            putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            
            accessToken?.let { putString(KEY_ACCESS_TOKEN, it) }
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            
            apply()
        }

        // Update state flows
        _isLoggedIn.value = true
        _currentUserId.value = userId
        _currentUserEmail.value = userEmail
        _currentUserName.value = userName
    }

    /**
     * Clear login state (logout)
     * I-clear ang login state (logout)
     */
    fun clearLoginState() {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_LOGIN_TIMESTAMP)
            apply()
        }

        // Update state flows
        _isLoggedIn.value = false
        _currentUserId.value = null
        _currentUserEmail.value = null
        _currentUserName.value = null
    }

    /**
     * Get current user ID
     * Kumuha ng current user ID
     */
    private fun getCurrentUserId(): String? {
        return if (checkLoginState()) {
            sharedPreferences.getString(KEY_USER_ID, null)
        } else null
    }

    /**
     * Get current user email
     * Kumuha ng current user email
     */
    private fun getCurrentUserEmail(): String? {
        return if (checkLoginState()) {
            sharedPreferences.getString(KEY_USER_EMAIL, null)
        } else null
    }

    /**
     * Get current user name
     * Kumuha ng current user name
     */
    private fun getCurrentUserName(): String? {
        return if (checkLoginState()) {
            sharedPreferences.getString(KEY_USER_NAME, null)
        } else null
    }

    /**
     * Get access token
     * Kumuha ng access token
     */
    fun getAccessToken(): String? {
        return if (checkLoginState()) {
            sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        } else null
    }

    /**
     * Get refresh token
     * Kumuha ng refresh token
     */
    fun getRefreshToken(): String? {
        return if (checkLoginState()) {
            sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
        } else null
    }

    /**
     * Get login timestamp
     * Kumuha ng login timestamp
     */
    fun getLoginTimestamp(): Long {
        return sharedPreferences.getLong(KEY_LOGIN_TIMESTAMP, 0L)
    }

    /**
     * Update user information
     * I-update ang user information
     */
    fun updateUserInfo(
        userName: String? = null,
        userEmail: String? = null
    ) {
        if (!checkLoginState()) return

        sharedPreferences.edit().apply {
            userName?.let { 
                putString(KEY_USER_NAME, it)
                _currentUserName.value = it
            }
            userEmail?.let { 
                putString(KEY_USER_EMAIL, it)
                _currentUserEmail.value = it
            }
            apply()
        }
    }

    /**
     * Check if login session is still valid (optional expiry check)
     * Tingnan kung valid pa ang login session
     */
    fun isSessionValid(maxSessionDurationMs: Long = 30L * 24 * 60 * 60 * 1000): Boolean {
        if (!checkLoginState()) return false
        
        val loginTimestamp = getLoginTimestamp()
        val currentTime = System.currentTimeMillis()
        
        return (currentTime - loginTimestamp) < maxSessionDurationMs
    }

    /**
     * Refresh authentication state (call when app starts)
     * I-refresh ang authentication state
     */
    fun refreshAuthState() {
        Log.d(TAG, "Refreshing authentication state...")
        val isLoggedIn = checkLoginState()
        _isLoggedIn.value = isLoggedIn
        
        if (isLoggedIn) {
            val userId = getCurrentUserId()
            val userEmail = getCurrentUserEmail()
            val userName = getCurrentUserName()
            
            _currentUserId.value = userId
            _currentUserEmail.value = userEmail
            _currentUserName.value = userName
            
            Log.d(TAG, "User is logged in - ID: $userId, Email: $userEmail")
        } else {
            _currentUserId.value = null
            _currentUserEmail.value = null
            _currentUserName.value = null
            Log.d(TAG, "User is not logged in")
        }
    }
    
    /**
     * Sync with Firebase Auth state on app startup
     * I-sync ang Firebase Auth state sa app startup
     */
    fun syncWithFirebaseAuth() {
        Log.d(TAG, "Syncing with Firebase Auth state...")
        val firebaseUser = firebaseAuth.currentUser
        val localLoginState = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        
        if (firebaseUser != null && !localLoginState) {
            // Firebase user exists but local state is false - restore local state
            Log.d(TAG, "Firebase user exists, restoring local login state")
            saveLoginState(
                userId = firebaseUser.uid,
                userEmail = firebaseUser.email ?: "",
                userName = firebaseUser.displayName ?: ""
            )
        } else if (firebaseUser == null && localLoginState) {
            // Local state says logged in but Firebase user is null - clear local state
            Log.d(TAG, "Firebase user is null, clearing local login state")
            clearLoginState()
        }
        
        // Refresh state after sync
        refreshAuthState()
    }
}

/**
 * User data class for convenience
 * User data class para sa convenience
 */
data class UserInfo(
    val userId: String,
    val email: String,
    val name: String,
    val loginTimestamp: Long
)
