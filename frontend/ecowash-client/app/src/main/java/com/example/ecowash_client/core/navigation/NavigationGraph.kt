package com.example.ecowash_client.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.net.URLEncoder
import java.net.URLDecoder
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ecowash_client.core.components.EcoBottomNavBar
import com.example.ecowash_client.core.components.BottomNavItem
import com.example.ecowash_client.feature.auth.data.datasource.AuthLocalDataSource
import com.example.ecowash_client.feature.presentation.splash.SplashScreen
import com.example.ecowash_client.feature.presentation.login.LoginScreen
import com.example.ecowash_client.feature.presentation.login.LoginViewModel
import com.example.ecowash_client.feature.presentation.login.LoginViewModelFactory
import com.example.ecowash_client.feature.presentation.register.RegisterScreen
import com.example.ecowash_client.feature.presentation.register.RegisterViewModel
import com.example.ecowash_client.feature.presentation.screens.HomeScreen
import com.example.ecowash_client.feature.presentation.screens.LogoutDialog
import com.example.ecowash_client.feature.precios.presentation.PreciosScreen
import com.example.ecowash_client.feature.presentation.pedidos.CrearPedidoScreen
import com.example.ecowash_client.feature.presentation.pedidos.MisPedidosScreen
import com.example.ecowash_client.feature.presentation.pedidos.PedidoEnCursoScreen
import kotlinx.coroutines.launch

private val bottomBarScreens = listOf("home", "precios", "pedidos")

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarScreens

    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                scope.launch {
                    AuthLocalDataSource(context).clearAuth()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                EcoBottomNavBar(
                    currentRoute = currentRoute,
                    onItemClick = { item ->
                        if (item.route == currentRoute) return@EcoBottomNavBar
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onShowLogout = { showLogoutDialog = true }
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onShowLogout: () -> Unit
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(
                onNavigationToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigationToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                context = context
            )
        }

        composable("login") {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(context)
            )
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            val registerViewModel: RegisterViewModel = viewModel(
                factory = LoginViewModelFactory(context)
            )
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToPrecios = {
                    navController.navigate("precios") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToMisPedidos = {
                    navController.navigate("pedidos") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("precios") {
            PreciosScreen(
                onSeleccionarPrecio = { precioId, precio, nombreServicio, nombreCategoria ->
                    val encId = URLEncoder.encode(precioId, "UTF-8")
                    val encServicio = URLEncoder.encode(nombreServicio, "UTF-8")
                    val encCategoria = URLEncoder.encode(nombreCategoria, "UTF-8")
                    navController.navigate("crear_pedido/$encId/$precio/$encServicio/$encCategoria")
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            "crear_pedido/{precioServicioId}/{precio}/{nombreServicio}/{nombreCategoria}",
            arguments = listOf(
                navArgument("precioServicioId") { type = NavType.StringType },
                navArgument("precio") { type = NavType.StringType },
                navArgument("nombreServicio") { type = NavType.StringType },
                navArgument("nombreCategoria") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val decodedId = URLDecoder.decode(backStackEntry.arguments?.getString("precioServicioId") ?: "", "UTF-8")
            val decodedServicio = URLDecoder.decode(backStackEntry.arguments?.getString("nombreServicio") ?: "", "UTF-8")
            val decodedCategoria = URLDecoder.decode(backStackEntry.arguments?.getString("nombreCategoria") ?: "", "UTF-8")
            CrearPedidoScreen(
                precioServicioId = decodedId,
                precio = backStackEntry.arguments?.getString("precio")?.toDoubleOrNull() ?: 0.0,
                nombreServicio = decodedServicio,
                nombreCategoria = decodedCategoria,
                onPedidoCreado = { pedidoId ->
                    navController.navigate("pedido_en_curso/$pedidoId") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            "pedido_en_curso/{pedidoId}",
            arguments = listOf(
                navArgument("pedidoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: ""
            PedidoEnCursoScreen(
                pedidoId = pedidoId,
                onVolver = { navController.popBackStack() }
            )
        }

        composable("pedidos") {
            MisPedidosScreen(
                onVolver = { navController.popBackStack() },
                onLogout = { onShowLogout() }
            )
        }
    }
}
