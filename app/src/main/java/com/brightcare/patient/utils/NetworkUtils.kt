package com.brightcare.patient.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Network utility class for handling connectivity checks and retry logic
 * Optimized for low/slow internet connections
 */
object NetworkUtils {

    private const val TAG = "NetworkUtils"
    
    /**
     * Timeout configurations for different network conditions
     */
    object Timeouts {
        // Extended timeouts for slow connections
        const val SOCIAL_LOGIN_TIMEOUT_MS = 60_000L // 60 seconds for social login
        const val CREDENTIAL_MANAGER_TIMEOUT_MS = 45_000L // 45 seconds for credential manager
        const val FIREBASE_AUTH_TIMEOUT_MS = 30_000L // 30 seconds for Firebase auth
        const val FIRESTORE_WRITE_TIMEOUT_MS = 20_000L // 20 seconds for Firestore writes
        const val FIRESTORE_READ_TIMEOUT_MS = 15_000L // 15 seconds for Firestore reads
        const val NETWORK_CHECK_TIMEOUT_MS = 10_000L // 10 seconds for network checks
        
        // Minimum timeouts (for quick failure on no network)
        const val MIN_TIMEOUT_MS = 5_000L
    }
    
    /**
     * Retry configurations
     */
    object RetryConfig {
        const val MAX_RETRIES = 3
        const val INITIAL_DELAY_MS = 1_000L // 1 second initial delay
        const val MAX_DELAY_MS = 8_000L // 8 seconds max delay
        const val BACKOFF_MULTIPLIER = 2.0
    }
    
    /**
     * Network quality levels
     */
    enum class NetworkQuality {
        NONE,
        POOR,
        MODERATE,
        GOOD,
        EXCELLENT
    }

    /**
     * Check if device has any network connectivity
     * @param context Application context
     * @return true if connected to network
     */
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network availability", e)
            // Return true to allow attempt even if we can't determine connectivity
            true
        }
    }

    /**
     * Check if network connectivity is validated (actually working internet)
     * @param context Application context
     * @return true if internet is actually reachable
     */
    fun hasValidatedInternet(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking validated internet", e)
            true // Allow attempt even if check fails
        }
    }

    /**
     * Estimate network quality based on available capabilities
     * Used to adjust timeouts dynamically
     * @param context Application context
     * @return NetworkQuality enum value
     */
    fun getNetworkQuality(context: Context): NetworkQuality {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return NetworkQuality.NONE
            
            val network = connectivityManager.activeNetwork ?: return NetworkQuality.NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkQuality.NONE
            
            if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                return NetworkQuality.NONE
            }
            
            // Check bandwidth (in Kbps)
            val downstreamBandwidth = capabilities.linkDownstreamBandwidthKbps
            val upstreamBandwidth = capabilities.linkUpstreamBandwidthKbps
            
            when {
                downstreamBandwidth >= 10_000 && upstreamBandwidth >= 5_000 -> NetworkQuality.EXCELLENT
                downstreamBandwidth >= 5_000 && upstreamBandwidth >= 2_000 -> NetworkQuality.GOOD
                downstreamBandwidth >= 1_000 && upstreamBandwidth >= 500 -> NetworkQuality.MODERATE
                downstreamBandwidth > 0 || upstreamBandwidth > 0 -> NetworkQuality.POOR
                else -> {
                    // Can't determine bandwidth, check if wifi or cellular
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkQuality.MODERATE
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkQuality.MODERATE
                        else -> NetworkQuality.POOR
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting network quality", e)
            NetworkQuality.MODERATE // Default to moderate to allow attempts
        }
    }

    /**
     * Get adjusted timeout based on network quality
     * @param baseTimeoutMs Base timeout in milliseconds
     * @param context Application context
     * @return Adjusted timeout based on network conditions
     */
    fun getAdjustedTimeout(baseTimeoutMs: Long, context: Context): Long {
        return when (getNetworkQuality(context)) {
            NetworkQuality.NONE -> baseTimeoutMs // Use base timeout, will fail on network error
            NetworkQuality.POOR -> (baseTimeoutMs * 2.5).toLong() // 2.5x for poor connection
            NetworkQuality.MODERATE -> (baseTimeoutMs * 1.5).toLong() // 1.5x for moderate
            NetworkQuality.GOOD -> baseTimeoutMs // Normal timeout
            NetworkQuality.EXCELLENT -> (baseTimeoutMs * 0.8).toLong() // Slightly less for fast connections
        }.coerceAtLeast(Timeouts.MIN_TIMEOUT_MS)
    }

    /**
     * Execute a suspending block with timeout and retry logic
     * Optimized for slow/unreliable connections
     * 
     * @param context Application context for network checks
     * @param baseTimeoutMs Base timeout per attempt
     * @param maxRetries Maximum number of retry attempts
     * @param operationName Name of operation for logging
     * @param shouldRetry Lambda to determine if error should trigger retry
     * @param block The suspending block to execute
     * @return Result of the block or throws exception
     */
    suspend fun <T> executeWithRetry(
        context: Context,
        baseTimeoutMs: Long = Timeouts.SOCIAL_LOGIN_TIMEOUT_MS,
        maxRetries: Int = RetryConfig.MAX_RETRIES,
        operationName: String = "Operation",
        shouldRetry: (Throwable) -> Boolean = { isRetryableError(it) },
        block: suspend () -> T
    ): T {
        var currentDelay = RetryConfig.INITIAL_DELAY_MS
        var lastException: Throwable? = null
        
        // Check network before starting
        if (!isNetworkAvailable(context)) {
            throw NetworkNotAvailableException("No network connection available. Please check your internet.")
        }
        
        val adjustedTimeout = getAdjustedTimeout(baseTimeoutMs, context)
        Log.d(TAG, "$operationName: Using timeout of ${adjustedTimeout}ms (network: ${getNetworkQuality(context)})")
        
        repeat(maxRetries) { attempt ->
            try {
                Log.d(TAG, "$operationName: Attempt ${attempt + 1} of $maxRetries")
                
                return withTimeout(adjustedTimeout) {
                    block()
                }
            } catch (e: CancellationException) {
                // Don't retry on cancellation
                throw e
            } catch (e: Throwable) {
                lastException = e
                
                Log.w(TAG, "$operationName: Attempt ${attempt + 1} failed: ${e.message}")
                
                if (attempt == maxRetries - 1) {
                    // Last attempt failed
                    Log.e(TAG, "$operationName: All $maxRetries attempts failed")
                    throw e
                }
                
                if (!shouldRetry(e)) {
                    // Error is not retryable
                    Log.d(TAG, "$operationName: Error is not retryable")
                    throw e
                }
                
                // Check network before retry
                if (!isNetworkAvailable(context)) {
                    throw NetworkNotAvailableException("Network connection lost during $operationName. Please check your internet.")
                }
                
                Log.d(TAG, "$operationName: Waiting ${currentDelay}ms before retry")
                delay(currentDelay)
                
                // Exponential backoff
                currentDelay = (currentDelay * RetryConfig.BACKOFF_MULTIPLIER)
                    .toLong()
                    .coerceAtMost(RetryConfig.MAX_DELAY_MS)
            }
        }
        
        throw lastException ?: TimeoutException("$operationName timed out after $maxRetries attempts")
    }

    /**
     * Execute a block with timeout but no retry (for user-interactive operations like credential picker)
     * Returns null on timeout instead of throwing
     */
    suspend fun <T> executeWithTimeoutOrNull(
        context: Context,
        baseTimeoutMs: Long = Timeouts.CREDENTIAL_MANAGER_TIMEOUT_MS,
        operationName: String = "Operation",
        block: suspend () -> T
    ): T? {
        val adjustedTimeout = getAdjustedTimeout(baseTimeoutMs, context)
        Log.d(TAG, "$operationName: Using timeout of ${adjustedTimeout}ms")
        
        return withTimeoutOrNull(adjustedTimeout) {
            block()
        }
    }

    /**
     * Determine if an error is retryable
     */
    fun isRetryableError(throwable: Throwable): Boolean {
        return when (throwable) {
            is SocketTimeoutException -> true
            is UnknownHostException -> true
            is IOException -> {
                val message = throwable.message?.lowercase() ?: ""
                message.contains("timeout") ||
                        message.contains("network") ||
                        message.contains("connection") ||
                        message.contains("socket") ||
                        message.contains("unreachable")
            }
            is kotlinx.coroutines.TimeoutCancellationException -> true
            else -> {
                val message = throwable.message?.lowercase() ?: ""
                message.contains("timeout") ||
                        message.contains("network") ||
                        message.contains("failed to connect") ||
                        message.contains("unable to resolve host")
            }
        }
    }

    /**
     * Check if error is a timeout error
     */
    fun isTimeoutError(throwable: Throwable): Boolean {
        return throwable is SocketTimeoutException ||
                throwable is kotlinx.coroutines.TimeoutCancellationException ||
                throwable is TimeoutException ||
                (throwable.message?.lowercase()?.contains("timeout") == true)
    }

    /**
     * Check if error is a network unavailable error
     */
    fun isNetworkUnavailableError(throwable: Throwable): Boolean {
        return throwable is NetworkNotAvailableException ||
                throwable is UnknownHostException ||
                (throwable.message?.lowercase()?.contains("no network") == true) ||
                (throwable.message?.lowercase()?.contains("unable to resolve") == true)
    }
    
    /**
     * Get user-friendly error message for network-related errors
     */
    fun getNetworkErrorMessage(throwable: Throwable): String {
        return when {
            throwable is NetworkNotAvailableException -> {
                "No internet connection. Please check your network and try again."
            }
            isTimeoutError(throwable) -> {
                "Connection timed out. Your internet may be slow. Please try again."
            }
            isNetworkUnavailableError(throwable) -> {
                "Unable to connect. Please check your internet connection."
            }
            isRetryableError(throwable) -> {
                "Network error occurred. Please try again."
            }
            else -> {
                throwable.message ?: "An unexpected error occurred."
            }
        }
    }
}

/**
 * Custom exception for network not available
 */
class NetworkNotAvailableException(message: String) : Exception(message)

/**
 * Custom exception for timeout
 */
class TimeoutException(message: String) : Exception(message)











