package com.jorgeHernandez.mycloudstock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jorgeHernandez.mycloudstock.ui.auth.AuthViewModel
import com.jorgeHernandez.mycloudstock.ui.auth.LoginScreen
import com.jorgeHernandez.mycloudstock.ui.auth.RegisterScreen
import com.jorgeHernandez.mycloudstock.ui.home.HomeScreen

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val startDestination = if (isLoggedIn) "home" else "login"

    LaunchedEffect(isLoggedIn) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (isLoggedIn && currentRoute != "home") {
            navController.navigate("home") {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (!isLoggedIn && currentRoute == "home") {
            navController.navigate("login") {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                uiState = uiState,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onLoginClick = authViewModel::login,
                onNavigateToRegister = {
                    authViewModel.clearErrors()
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                uiState = uiState,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onConfirmPasswordChange = authViewModel::onConfirmPasswordChange,
                onRegisterClick = authViewModel::register,
                onNavigateToLogin = {
                    authViewModel.clearErrors()
                    navController.popBackStack()
                }
            )
        }
        composable("home") {
            HomeScreen(
                userEmail = authViewModel.userEmail,
                onLogoutClick = authViewModel::logout
            )
        }
    }
}