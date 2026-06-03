package com.example.ecowash_client.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ecowash_client.feature.presentation.splash.SplashScreen
import com.example.ecowash_client.feature.presentation.login.LoginScreen
import com.example.ecowash_client.feature.presentation.login.LoginViewModel
import com.example.ecowash_client.feature.presentation.login.LoginViewModelFactory
import com.example.ecowash_client.feature.presentation.register.RegisterScreen
import com.example.ecowash_client.feature.presentation.register.RegisterViewModel
import com.example.ecowash_client.feature.presentation.screens.HomeScreen

@Composable
fun NavigationGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Route.Splash
    ) {
        // Pantalla Splash
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
                }
            )
        }

        // Pantalla de Login (Modificada para añadir navegación al registro)
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
                    // 👈 CONECTAMOS LA NAVEGACIÓN HACIA LA RUTA DE REGISTRO
                    navController.navigate(Route.Register)
                }
            )
        }
        // Pantalla de Registro
        composable<Route.Register> {
            val registerViewModel: RegisterViewModel = viewModel(factory = LoginViewModelFactory(context))
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.navigate(Route.Home) { popUpTo(Route.Register) { inclusive = true } }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla Principal (Home)
        composable<Route.Home> {
            HomeScreen()
        }
    }
}