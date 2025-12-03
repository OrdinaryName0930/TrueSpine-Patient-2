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
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("preserve_checkbox", false)
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

/**
 * Enhanced PatientSignUpScreen with Terms & Conditions integration
 *
 * This enhanced version integrates the Terms & Conditions screen
 * with the SignUp screen using navigation.
 *
 * Key features:
 * - Navigates to Terms & Conditions screen when terms text is clicked
 * - Automatically checks the terms checkbox when user agrees from Terms screen
 * - Maintains all existing SignUp functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedPatientSignUpScreen(
    navController: NavController,
    onBackClick: () -> Unit = {},
    onSignUpClick: (SignUpFormState) -> Unit = {},
    onGoogleSignUpClick: () -> Unit = {},
    onFacebookSignUpClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    // This parameter will be set to true when user comes back from Terms screen
    autoCheckTerms: Boolean = false
) {
    // Use rememberSaveable to persist form data across navigation
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    // Create and manage form state properly
    var formState by remember {
        mutableStateOf(SignUpFormState(
            email = email,
            password = password,
            confirmPassword = confirmPassword
        ))
    }

    // Initialize form state with saved values when component loads
    LaunchedEffect(Unit) {
        formState = formState.copy(
            email = email,
            password = password,
            confirmPassword = confirmPassword
        )
    }

    // Update individual fields when form state changes (for persistence)
    LaunchedEffect(formState.email, formState.password, formState.confirmPassword) {
        email = formState.email
        password = formState.password
        confirmPassword = formState.confirmPassword
    }

    var termsAccepted by remember { mutableStateOf(autoCheckTerms) }
    var isTermsError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Update terms acceptance when autoCheckTerms changes
    LaunchedEffect(autoCheckTerms) {
        if (autoCheckTerms) {
            termsAccepted = true
            isTermsError = false
        }
    }

    // Real-time validation: check if all fields are valid and terms accepted
    val isFormValid by remember(formState, termsAccepted) {
        derivedStateOf {
            formState.email.isNotBlank() &&
                    formState.password.isNotBlank() &&
                    formState.confirmPassword.isNotBlank() &&
                    !formState.isEmailError &&
                    !formState.isPasswordError &&
                    !formState.isConfirmPasswordError &&
                    termsAccepted
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {

        // Title
        Text(
            text = "Sign up",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp
            ),
            color = Blue500,
            modifier = Modifier.padding(bottom = 8.dp, top = 40.dp, start = 15.dp)
        )

        // Sign Up Form
        SignUpForm(
            formState = formState,
            onFormStateChange = { formState = it },
            modifier = Modifier
        )

        // Terms Checkbox with Navigation
        TermsCheckbox(
            isChecked = termsAccepted,
            onCheckedChange = {
                termsAccepted = it
                isTermsError = false
            },
            onTermsClick = {
                // Save current checkbox state before navigating
                navController.currentBackStackEntry?.savedStateHandle?.set("preserve_checkbox", termsAccepted)
                navController.navigate("terms_and_conditions")
            },
            onTermsAndPrivacyClick = {
                // Save current checkbox state before navigating
                navController.currentBackStackEntry?.savedStateHandle?.set("preserve_checkbox", termsAccepted)
                navController.navigate("terms_and_conditions")
            },
            isError = isTermsError,
            modifier = Modifier.padding(start = 10.dp, end = 12.dp, bottom = 15.dp)
        )

        // Sign Up Button with reactive enable/disable
        SignUpButton(
            text = "Sign up",
            onClick = {
                if (!isFormValid) {
                    isTermsError = !termsAccepted
                    return@SignUpButton
                }
                isLoading = true
                onSignUpClick(formState)
            },
            type = SignUpButtonType.PRIMARY,
            loading = isLoading,
            enabled = isFormValid,
            modifier = Modifier.padding(start = 16.dp, end = 15.dp, top = 16.dp)
        )

        // Sign In Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            TextButton(
                onClick = onSignInClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Sign in",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Blue500
                )
            }
        }

        // Social login section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 58.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Or continue with",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Blue500
            )
        }

        // Social Login Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            SocialIconButton(
                provider = SocialProvider.GOOGLE,
                onClick = onGoogleSignUpClick,
                modifier = Modifier.size(45.dp),
                logoSize = 20
            )
            SocialIconButton(
                provider = SocialProvider.FACEBOOK,
                onClick = onFacebookSignUpClick,
                modifier = Modifier.size(45.dp),
                logoSize = 23
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
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

/**
 * Navigation Integration Guide
 *
 * Add this to your navigation graph:
 *
 * ```kotlin
 * // In your NavHost
 * composable("signup") { backStackEntry ->
 *     val autoCheckTerms = backStackEntry.savedStateHandle.get<Boolean>("terms_agreed") ?: false
 *
 *     EnhancedPatientSignUpScreen(
 *         navController = navController,
 *         autoCheckTerms = autoCheckTerms,
 *         onSignUpClick = { formState ->
 *             // Handle sign up
 *         }
 *     )
 * }
 *
 * composable("terms_and_conditions") {
 *     TermsAndConditionsScreen(
 *         navController = navController,
 *         onAgreeClicked = {
 *             // Set the flag that terms were agreed to
 *             navController.previousBackStackEntry?.savedStateHandle?.set("terms_agreed", true)
 *         }
 *     )
 * }
 *
 * // Behavior:
 * // - When user clicks back button: checkbox remains unchecked (autoCheckTerms = false)
 * // - When user clicks "I Agree": checkbox gets checked (autoCheckTerms = true)
 * ```
 */
