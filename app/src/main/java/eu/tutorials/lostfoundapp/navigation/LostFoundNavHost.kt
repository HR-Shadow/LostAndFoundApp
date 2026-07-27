package eu.tutorials.lostfoundapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import eu.tutorials.lostfoundapp.ui.aiassistant.AiAssistantScreen
import eu.tutorials.lostfoundapp.ui.auth.LoginScreen
import eu.tutorials.lostfoundapp.ui.auth.SignUpScreen
import eu.tutorials.lostfoundapp.ui.components.LoadingScreen
import eu.tutorials.lostfoundapp.ui.home.HomeScreen
import eu.tutorials.lostfoundapp.ui.matches.MatchesScreen
import eu.tutorials.lostfoundapp.ui.notifications.NotificationHubScreen
import eu.tutorials.lostfoundapp.ui.report.ReportFoundScreen
import eu.tutorials.lostfoundapp.ui.report.ReportLostScreen
import eu.tutorials.lostfoundapp.ui.screens.ChatScreen
import eu.tutorials.lostfoundapp.viewmodel.AuthViewModel

@Composable
fun LostFoundNavHost(
    authViewModel: AuthViewModel = viewModel()
) {
    val authNavController = rememberNavController()
    val mainNavController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    when {
        authState.isLoading -> {
            LoadingScreen()
        }
        authState.isAuthenticated -> {
            NavHost(
                navController = mainNavController,
                startDestination = NavRoutes.HOME
            ) {
                composable(NavRoutes.HOME) {
                    HomeScreen(
                        user = authState.currentUser,
                        onReportLost = { mainNavController.navigate(NavRoutes.REPORT_LOST) },
                        onReportFound = { mainNavController.navigate(NavRoutes.REPORT_FOUND) },
                        onViewMatches = { mainNavController.navigate(NavRoutes.MATCHES) },
                        onViewNotifications = { mainNavController.navigate(NavRoutes.NOTIFICATIONS) },
                        onOpenAiAssistant = { mainNavController.navigate(NavRoutes.AI_ASSISTANT) },
                        onSignOut = authViewModel::signOut
                    )
                }
                composable(NavRoutes.MATCHES) {
                    MatchesScreen(
                        onNavigateBack = { mainNavController.popBackStack() },
                        onNavigateToChat = { matchId ->
                            mainNavController.navigate(NavRoutes.chatRoute(matchId))
                        }
                    )
                }
                composable(
                    route = NavRoutes.CHAT,
                    arguments = listOf(
                        navArgument("matchId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val matchId = backStackEntry.arguments?.getString("matchId") ?: return@composable
                    val currentUserId = authState.currentUser?.userId ?: return@composable
                    ChatScreen(
                        matchId = matchId,
                        currentUserId = currentUserId,
                        onNavigateBack = { mainNavController.popBackStack() }
                    )
                }
                composable(NavRoutes.REPORT_LOST) {
                    ReportLostScreen(
                        onNavigateBack = { mainNavController.popBackStack() },
                        onReportSuccess = { mainNavController.popBackStack() }
                    )
                }
                composable(NavRoutes.REPORT_FOUND) {
                    ReportFoundScreen(
                        onNavigateBack = { mainNavController.popBackStack() },
                        onReportSuccess = { mainNavController.popBackStack() }
                    )
                }
                composable(NavRoutes.NOTIFICATIONS) {
                    NotificationHubScreen(
                        onViewMatch = { matchId ->
                            mainNavController.navigate(NavRoutes.chatRoute(matchId))
                        }
                    )
                }
                composable(NavRoutes.AI_ASSISTANT) {
                    AiAssistantScreen(
                        onNavigateBack = { mainNavController.popBackStack() }
                    )
                }
            }
        }
        else -> {
            NavHost(
                navController = authNavController,
                startDestination = NavRoutes.LOGIN
            ) {
                composable(NavRoutes.LOGIN) {
                    LoginScreen(
                        isLoading = authState.isSubmitting,
                        errorMessage = authState.errorMessage,
                        onLogin = authViewModel::signIn,
                        onNavigateToSignUp = {
                            authNavController.navigate(NavRoutes.SIGN_UP)
                        },
                        onClearError = authViewModel::clearError
                    )
                }
                composable(NavRoutes.SIGN_UP) {
                    SignUpScreen(
                        isLoading = authState.isSubmitting,
                        errorMessage = authState.errorMessage,
                        onSignUp = authViewModel::signUp,
                        onNavigateBack = { authNavController.popBackStack() },
                        onClearError = authViewModel::clearError
                    )
                }
            }
        }
    }
}