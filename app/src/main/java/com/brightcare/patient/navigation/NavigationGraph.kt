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
                    
                    // Navigate to login
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
            TermsAndConditionsScreen(
                navController = navController,
                onAgreeClicked = {
                    // Set the flag that terms were agreed to
                    navController.previousBackStackEntry?.savedStateHandle
                        ?.set(NavigationArgs.TERMS_AGREED, true)
                }
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
            NavigationRoutes.MAIN_DASHBOARD,
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
            MainDashboardScreen(
                navController = navController
            )
        }
    }
}





