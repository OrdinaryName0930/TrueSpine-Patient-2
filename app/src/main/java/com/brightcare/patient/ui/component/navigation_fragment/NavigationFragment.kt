package com.brightcare.patient.ui.component.navigation_fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.screens.*

/**
 * Navigation items for the bottom navigation bar
 */
sealed class NavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : NavigationItem(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Rounded.Home
    )

    object Chiro : NavigationItem(
        route = "chiro",
        title = "Chiro",
        selectedIcon = Icons.Rounded.MedicalServices,
        unselectedIcon = Icons.Rounded.MedicalServices
    )

    object Appointment : NavigationItem(
        route = "booking",
        title = "Booking",
        selectedIcon = Icons.Rounded.CalendarMonth,
        unselectedIcon = Icons.Rounded.CalendarMonth
    )

    object Message : NavigationItem(
        route = "message",
        title = "Message",
        selectedIcon = Icons.Rounded.Message,
        unselectedIcon = Icons.Rounded.Message
    )

    object Profile : NavigationItem(
        route = "profile",
        title = "Profile",
        selectedIcon = Icons.Rounded.Person,
        unselectedIcon = Icons.Rounded.Person
    )
}

/**
 * Main navigation fragment with bottom navigation bar
 */
@Composable
fun NavigationFragment(
    navController: NavController,
    modifier: Modifier = Modifier,
    currentRoute: String = "home",
    onNavigationItemClick: (String) -> Unit = {}
) {
    val navigationItems = listOf(
        NavigationItem.Home,
        NavigationItem.Chiro,
        NavigationItem.Appointment,
        NavigationItem.Message,
        NavigationItem.Profile
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        // Main content area with actual screens
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth() // Small padding to prevent content overlap
        ) {
            // Display actual screens based on current route
            when (currentRoute) {
                "home" -> HomeScreen(navController = navController)
                "chiro" -> ChiroScreen(navController = navController)
                "booking" -> BookingScreen(
                    navController = navController,
                    onShowChiropractorSelection = {
                        // Show chiropractor selection within booking screen
                        // No navigation needed - handled internally
                    }
                )
                "message" -> MessageScreen(navController = navController)
                "profile" -> ProfileScreen(navController = navController)
                else -> NavigationContent(currentRoute = currentRoute)
            }
        }

        // Bottom Navigation Bar
        NavigationBar(
            containerColor = White,
            contentColor = Blue500,
            tonalElevation = 40.dp,
            windowInsets = WindowInsets(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
                navigationItems.forEach { item ->
                    val isSelected = currentRoute == item.route

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            onNavigationItemClick(item.route)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Blue500,
                            selectedTextColor = Blue500,
                            unselectedIconColor = Gray500,
                            unselectedTextColor = Gray500,
                            indicatorColor = Blue50
                        ),
                         modifier = Modifier.weight(1f)
                    )
                }
            }
    }
}


/**
 * Placeholder content for each navigation item
 * Replace this with actual screen composables
 */
@Composable
private fun NavigationContent(currentRoute: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        val (title, description, icon) = when (currentRoute) {
            "home" -> Triple(
                "Home",
                "Welcome to your chiropractor app dashboard",
                Icons.Rounded.Home
            )
            "chiro" -> Triple(
                "Chiropractor",
                "Find and connect with chiropractors",
                Icons.Rounded.LocalHospital
            )
            "appointment" -> Triple(
                "Appointments",
                "Manage your appointments and schedule",
                Icons.Rounded.CalendarToday
            )
            "message" -> Triple(
                "Messages",
                "Chat with your healthcare providers",
                Icons.Rounded.Message
            )
            "profile" -> Triple(
                "Profile",
                "Manage your account and settings",
                Icons.Rounded.Person
            )
            else -> Triple(
                "Unknown",
                "Page not found",
                Icons.Rounded.Error
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Blue500,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Blue500,
                fontSize = 28.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Gray600,
                lineHeight = 24.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Blue50),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Coming Soon",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Blue700
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This feature will be available soon. Stay tuned!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray700,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Stateful navigation fragment that manages its own navigation state
 */
@Composable
fun StatefulNavigationFragment(
    navController: NavController,
    modifier: Modifier = Modifier,
    initialRoute: String = "home"
) {
    var currentRoute by remember { mutableStateOf(initialRoute) }
    
    NavigationFragment(
        navController = navController,
        modifier = modifier,
        currentRoute = currentRoute,
        onNavigationItemClick = { route ->
            currentRoute = route
            // Here you can add actual navigation logic
            // navController.navigate(route)
        }
    )
}

// Preview for Home tab
@Preview(
    showBackground = true,
    name = "Navigation Fragment - Home",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun NavigationFragmentHomePreview() {
    BrightCarePatientTheme {
        NavigationFragment(
            navController = rememberNavController(),
            currentRoute = "home"
        )
    }
}

// Preview for Chiro tab
@Preview(
    showBackground = true,
    name = "Navigation Fragment - Chiro",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun NavigationFragmentChiroPreview() {
    BrightCarePatientTheme {
        NavigationFragment(
            navController = rememberNavController(),
            currentRoute = "chiro"
        )
    }
}

// Preview for Appointment tab
@Preview(
    showBackground = true,
    name = "Navigation Fragment - Appointment",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun NavigationFragmentAppointmentPreview() {
    BrightCarePatientTheme {
        NavigationFragment(
            navController = rememberNavController(),
            currentRoute = "booking"
        )
    }
}

// Preview for Message tab
@Preview(
    showBackground = true,
    name = "Navigation Fragment - Message",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun NavigationFragmentMessagePreview() {
    BrightCarePatientTheme {
        NavigationFragment(
            navController = rememberNavController(),
            currentRoute = "message"
        )
    }
}

// Preview for Profile tab
@Preview(
    showBackground = true,
    name = "Navigation Fragment - Profile",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun NavigationFragmentProfilePreview() {
    BrightCarePatientTheme {
        NavigationFragment(
            navController = rememberNavController(),
            currentRoute = "profile"
        )
    }
}
