package com.jorgeHernandez.mycloudstock.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jorgeHernandez.mycloudstock.ui.auth.AuthViewModel
import com.jorgeHernandez.mycloudstock.ui.auth.LoginScreen
import com.jorgeHernandez.mycloudstock.ui.auth.RegisterScreen
import com.jorgeHernandez.mycloudstock.ui.products.ProductEditScreen
import com.jorgeHernandez.mycloudstock.ui.products.ProductEditViewModel
import com.jorgeHernandez.mycloudstock.ui.products.ProductListScreen
import com.jorgeHernandez.mycloudstock.ui.products.ProductListViewModel

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
        if (isLoggedIn && (currentRoute == "login" || currentRoute == "register")) {
            navController.navigate("home") {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (!isLoggedIn && currentRoute != null && currentRoute != "login" && currentRoute != "register") {
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
        composable("home") { entry ->
            val context = LocalContext.current
            val prefs = remember {
                context.getSharedPreferences("cloudstock_prefs", Context.MODE_PRIVATE)
            }
            val vm: ProductListViewModel = viewModel {
                ProductListViewModel(prefs = prefs)
            }

            val deletedName by entry.savedStateHandle
                .getStateFlow<String?>("deleted_product_name", null)
                .collectAsState()

            ProductListScreen(
                viewModel = vm,
                snackbarMessage = deletedName?.let { "Deleted \"$it\"" },
                onSnackbarShown = { entry.savedStateHandle["deleted_product_name"] = null },
                onAddClick = { navController.navigate("edit") },
                onEditClick = { id -> navController.navigate("edit?id=$id") },
                onLogoutClick = authViewModel::logout
            )
        }
        composable(
            route = "edit?id={id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString("id")
            val vm: ProductEditViewModel = viewModel(key = id ?: "new") {
                ProductEditViewModel(productId = id)
            }

            ProductEditScreen(
                viewModel = vm,
                onDone = { navController.popBackStack() },
                onDeleted = { deletedName ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("deleted_product_name", deletedName)
                    navController.popBackStack()
                }
            )
        }
    }
}