package com.example.ecowash_washer.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ecowash_washer.feature.auth.presentation.splash.SplashScreen
import com.example.ecowash_washer.feature.auth.presentation.login.LoginScreen
import com.example.ecowash_washer.feature.auth.presentation.login.LoginViewModel
import com.example.ecowash_washer.feature.auth.presentation.login.LoginViewModelFactory
import com.example.ecowash_washer.feature.auth.presentation.register.RegisterScreen
import com.example.ecowash_washer.feature.auth.presentation.register.RegisterViewModel
import com.example.ecowash_washer.feature.home.presentation.WasherHomeScreen

@Composable
fun NavigationGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Route.Splash
    ) {
        composable<Route.Splash> {
            SplashScreen(
                onNavigationToLogin = {
                    navController.navigate(Route.Login) {
                        popUpTo(Route.Splash) { inclusive = true }
                    }
                },
                onNavigationToHome = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Splash) { inclusive = true }
                    }
                },
                context = context
            )
        }

        composable<Route.Login> {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(context)
            )

            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Route.Register)
                }
            )
        }

        composable<Route.Register> {
            val registerViewModel: RegisterViewModel = viewModel(factory = LoginViewModelFactory(context))
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Register) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.Home> {
            WasherHomeScreen(
                onLogout = {
                    navController.navigate(Route.Login) {
                        popUpTo(Route.Home) { inclusive = true }
                    }
                }
            )
        }
    }
}
