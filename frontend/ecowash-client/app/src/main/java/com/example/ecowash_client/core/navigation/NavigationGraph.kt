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

        // Pantalla de Login
        composable<Route.Login> {
            // Instanciamos el ViewModel usando nuestra Factory moderna de Compose
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(context)
            )

            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla Principal (Home)
        composable<Route.Home> {
            HomeScreen()
        }
    }
}