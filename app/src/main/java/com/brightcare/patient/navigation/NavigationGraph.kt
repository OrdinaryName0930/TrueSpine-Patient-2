package com.brightcare.patient.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.brightcare.patient.ui.screens.*
import com.brightcare.patient.utils.OnboardingPreferences
import android.util.Log

/**
 * Navigation graph for the BrightCare Patient app
 * Handles all screen navigation and transitions
 */
@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String = NavigationRoutes.LOGIN,
    onFinishActivity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Onboarding Screen
        composable(NavigationRoutes.ONBOARDING) {
            val context = LocalContext.current
            OnboardingScreen(
                onComplete = {
                    // Mark onboarding as seen
                    // Markahan ang onboarding bilang nakita na
                    OnboardingPreferences.setOnboardingSeen(context)
                    
                    // Navigate directly to login screen (skip permissions)
                    // Direktang pumunta sa login screen (laktawan ang permissions)
                    navController.navigate(NavigationRoutes.LOGIN) {
                        popUpTo(NavigationRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        
        // Login Screen
        composable(
            "${NavigationRoutes.LOGIN}?clearStates={clearStates}",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) { backStackEntry ->
            val clearStates = backStackEntry.arguments?.getString("clearStates")?.toBoolean() ?: false
            PatientLoginScreen(
                navController = navController,
                clearStatesOnInit = clearStates
            )
        }
        
        // Signup Screen
        composable(
            NavigationRoutes.SIGNUP,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) { backStackEntry ->
            val autoCheckTerms = backStackEntry.savedStateHandle
                .get<Boolean>(NavigationArgs.TERMS_AGREED) ?: false
            val preserveCheckboxState = backStackEntry.savedStateHandle
                .get<Boolean>(NavigationArgs.PRESERVE_CHECKBOX) ?: false
            
            PatientSignUpScreen(
                navController = navController,
                autoCheckTerms = autoCheckTerms || preserveCheckboxState,
                onBackClick = onFinishActivity,
                onSignInClick = {
                    navController.navigateToLoginFromSignUp()
                }
            )
        }
        
        // Terms and Conditions Screen
        composable(NavigationRoutes.TERMS_AND_CONDITIONS) {
            TermsConditionsScreen(
                navController = navController
            )
        }
        
        // Privacy Policy Screen
        composable(NavigationRoutes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(
                navController = navController
            )
        }
        
        // Forgot Password Email Screen
        composable(
            NavigationRoutes.FORGOT_PASSWORD_EMAIL,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) {
            ForgotPasswordEmailScreen(
                navController = navController
            )
        }
        
        // Forgot Password OTP Screen
        composable(
            NavigationRoutes.FORGOT_PASSWORD_OTP,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString(NavigationArgs.EMAIL) ?: ""
            ForgotPasswordScreen(
                navController = navController,
                email = email,
                onBackClick = {
                    Log.d("NavigationGraph", "OTP back button clicked, calling popBackStack()")
                    val result = navController.popBackStack()
                    Log.d("NavigationGraph", "popBackStack() result: $result")
                }
            )
        }
        
        // Change Password Screen
        composable(
            NavigationRoutes.CHANGE_PASSWORD,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) {
            ChangePasswordScreen(
                navController = navController
            )
        }
        
        // Complete Profile Screen
        composable(
            NavigationRoutes.COMPLETE_PROFILE,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) {
            CompleteYourProfileScreen(
                navController = navController,
                onBackClick = {
                    navController.popBackStack()
                }
                // Note: onSaveClick is now handled internally by the ViewModel
                // The screen will automatically navigate to home on success
            )
        }
        
        // Home Screen (legacy - redirects to main dashboard)
        composable(
            NavigationRoutes.HOME,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) {
            // Redirect to main dashboard
            LaunchedEffect(Unit) {
                navController.navigate(NavigationRoutes.MAIN_DASHBOARD) {
                    popUpTo(NavigationRoutes.HOME) { inclusive = true }
                }
            }
        }
        
        // Main Dashboard Screen with Navigation Fragment
        composable(
            "${NavigationRoutes.MAIN_DASHBOARD}?initialRoute={initialRoute}",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) { backStackEntry ->
            val initialRoute = backStackEntry.arguments?.getString("initialRoute") ?: "home"
            MainDashboardScreen(
                navController = navController,
                initialRoute = initialRoute
            )
        }
        
        // Personal Details Screen
        composable(
            NavigationRoutes.PERSONAL_DETAILS,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) {
            PersonalDetailsScreen(
                navController = navController
            )
        }
        
        // Emergency Contacts Screen
        composable(
            NavigationRoutes.EMERGENCY_CONTACTS,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) {
            EmergencyContactScreen(
                navController = navController
            )
        }
        
        // Chiropractor Booking Screen
        composable(
            NavigationRoutes.CHIROPRACTOR_BOOKING,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) { backStackEntry ->
            val chiropractorId = backStackEntry.arguments?.getString(NavigationArgs.CHIROPRACTOR_ID) ?: ""
            ChiropractorBookingScreen(
                chiropractorId = chiropractorId,
                navController = navController
            )
        }
        
        // Book Appointment Activity Screen
        composable(
            NavigationRoutes.BOOK_APPOINTMENT,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) { backStackEntry ->
            val chiropractorId = backStackEntry.arguments?.getString(NavigationArgs.CHIROPRACTOR_ID) ?: ""
            BookAppointmentActivity(
                chiropractorId = chiropractorId,
                navController = navController
            )
        }
        
        // Payment Activity Screen
        composable(
            NavigationRoutes.PAYMENT,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) { backStackEntry ->
            val chiropractorId = backStackEntry.arguments?.getString(NavigationArgs.CHIROPRACTOR_ID) ?: ""
            val date = backStackEntry.arguments?.getString(NavigationArgs.DATE) ?: ""
            val time = java.net.URLDecoder.decode(backStackEntry.arguments?.getString(NavigationArgs.TIME) ?: "", "UTF-8")
            val paymentOption = backStackEntry.arguments?.getString(NavigationArgs.PAYMENT_OPTION) ?: "downpayment"
            val message = java.net.URLDecoder.decode(backStackEntry.arguments?.getString(NavigationArgs.MESSAGE) ?: "", "UTF-8")
            
            PaymentActivity(
                navController = navController,
                chiropractorId = chiropractorId,
                date = date,
                time = time,
                paymentOption = paymentOption,
                message = message
            )
        }
        
        // Conversation Screen
        composable(
            NavigationRoutes.CONVERSATION,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString(NavigationArgs.CONVERSATION_ID) ?: ""
            ConversationScreen(
                conversationId = conversationId,
                navController = navController
            )
        }
        
        // View Profile Screen
        composable(
            NavigationRoutes.VIEW_PROFILE,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) { backStackEntry ->
            val chiropractorId = backStackEntry.arguments?.getString(NavigationArgs.CHIROPRACTOR_ID) ?: ""
            ViewProfileScreen(
                chiropractorId = chiropractorId,
                navController = navController
            )
        }
        
        // Appointment Details Screen
        composable(
            NavigationRoutes.APPOINTMENT_DETAILS,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString(NavigationArgs.APPOINTMENT_ID) ?: ""
            AppointmentDetailsScreen(
                appointmentId = appointmentId,
                navController = navController
            )
        }
        
        // Notifications Screen
        composable(
            NavigationRoutes.NOTIFICATIONS,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(600)
                )
            }
        ) {
            NotificationScreen(
                navController = navController
            )
        }
    }
}





