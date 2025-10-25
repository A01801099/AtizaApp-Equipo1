package mx.aro.atizaapp_equipo1.view.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.aro.atizaapp_equipo1.view.screens.AjustesScreen
import mx.aro.atizaapp_equipo1.view.screens.MiCredencialScreen
import mx.aro.atizaapp_equipo1.view.components.BottomNavigationBar
import mx.aro.atizaapp_equipo1.view.screens.CodigoQRCredencialScreen
import mx.aro.atizaapp_equipo1.view.screens.ContactoScreen
import mx.aro.atizaapp_equipo1.view.screens.DetalleComercioScreen
import mx.aro.atizaapp_equipo1.view.screens.ExplorarComerciosScreen
import mx.aro.atizaapp_equipo1.view.screens.LoginScreen
import mx.aro.atizaapp_equipo1.view.screens.RegisterScreen
import mx.aro.atizaapp_equipo1.view.screens.CreateCredentialScreen
import mx.aro.atizaapp_equipo1.view.screens.LoadingScreen
import mx.aro.atizaapp_equipo1.view.screens.ForgotPasswordScreen
import mx.aro.atizaapp_equipo1.view.screens.OfertasScreen
import mx.aro.atizaapp_equipo1.viewmodel.AppVM
import mx.aro.atizaapp_equipo1.view.components.OfflineBanner

@Composable
fun AppNavHost(appVM: AppVM) {
    val navController = rememberNavController()
    val estaLoggeado = appVM.estaLoggeado.collectAsState().value
    val verificationState = appVM.verificationState.collectAsState().value
    val credencialChecked = appVM.credencialChecked.collectAsState().value
    val isNetworkAvailable = appVM.isNetworkAvailable.collectAsState().value

    // ✅ Verificación automática de credencial al inicio (solo con conexión)
    // Solo se ejecuta si hay red disponible para evitar errores innecesarios
    LaunchedEffect(estaLoggeado, credencialChecked, isNetworkAvailable) {
        if (estaLoggeado && !credencialChecked) {
            if (isNetworkAvailable) {
                // ✅ Hay red: verificar credencial con el backend
                appVM.verificarCredencial()
            } else {
                // ⚠️ Sin red: asumir que tiene credencial (modo offline)
                // Esto permite usar la app offline sin bloquear al usuario
                appVM.setOfflineMode()
            }
        }
    }

    // Determinar la pantalla inicial según el flujo
    // Esta lógica es REACTIVA: cuando cambian los estados, NavHost se re-renderiza
    // y ajusta la navegación automáticamente SIN necesidad de navegación programática
    val startDestination = when {
        !estaLoggeado -> Routes.LOGIN
        !credencialChecked -> Routes.LOADING  // Mostrar loading mientras verifica
        verificationState.hasCredencial -> Routes.EXPLORAR
        verificationState.isNetworkError -> Routes.EXPLORAR  // ⚠️ Sin red: permitir acceso offline
        else -> Routes.REGISTER_CREDENCIAL  // Sin credencial: ir a registro
    }

    // Obtenemos la ruta actual para decidir si mostrar el BottomBar
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Lista de pantallas donde SÍ se muestra el BottomNavigationBar
    val showBottomBar = currentRoute in listOf(
        Routes.EXPLORAR,
        Routes.MI_CREDENCIAL,
        Routes.CONTACTO,
        Routes.CODIGO_QR_CREDENCIAL,
        Routes.EXPLORAR_COMERCIO,
        Routes.AJUSTES,
        Routes.BENEFICIOS
    )

    // Pantallas donde se debe mostrar el banner de offline
    val showOfflineBanner = estaLoggeado && !isNetworkAvailable && currentRoute in listOf(
        Routes.EXPLORAR,
        Routes.MI_CREDENCIAL,
        Routes.CONTACTO,
        Routes.CODIGO_QR_CREDENCIAL,
        Routes.EXPLORAR_COMERCIO,
        Routes.AJUSTES,
        Routes.BENEFICIOS
    )

    Scaffold(
        topBar = {
            // Banner de modo offline global
            if (showOfflineBanner) {
                OfflineBanner()
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // LOADING (Ya no se usa, pero se deja por si se necesita en el futuro)
            composable(Routes.LOADING) {
                LoadingScreen(message = "Verificando credencial...")
            }

            // AUTH
            composable(Routes.LOGIN) {
                LoginScreen(
                    appVM = appVM,
                    onRegisterClick = { navController.navigate(Routes.REGISTER) },
                    onForgotPasswordClick = { navController.navigate(Routes.FORGOT_PASSWORD) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    appVM = appVM,
                    onLoginClick = { navController.navigate(Routes.LOGIN) }
                )
            }
            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    appVM = appVM,
                    onBackToLogin = {
                        appVM.clearForgotPasswordState()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            // ONBOARDING: registro de credencial obligatorio
            composable(Routes.REGISTER_CREDENCIAL) {
                CreateCredentialScreen(
                    appVM = appVM,
                    onDone = {
                        appVM.verificarCredencial()
                        navController.navigate(Routes.EXPLORAR)
                    },
                    onCancel = { navController.navigate(Routes.LOGIN) }
                )
            }
            // MAIN
            composable(Routes.EXPLORAR) {
                ExplorarComerciosScreen(appVM = appVM, navController = navController)
            }
            composable(Routes.MI_CREDENCIAL) {
                MiCredencialScreen(navController = navController, appVM = appVM)
            }
            composable(Routes.CONTACTO) {
                ContactoScreen(navController = navController)
            }
            composable(Routes.CODIGO_QR_CREDENCIAL) {
                CodigoQRCredencialScreen(navController = navController, appVM = appVM)
            }

            composable(Routes.BENEFICIOS) {
                OfertasScreen(navController = navController, appVM = appVM)
            }
            composable(Routes.EXPLORAR_COMERCIO) { backStackEntry ->
                val negocioId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                if (negocioId != null) {
                    DetalleComercioScreen(
                        navController = navController,
                        negocioId = negocioId,
                        appVM = appVM
                    )
                } else {
                    // Manejo opcional si el id no es válido
                    Text("Negocio no encontrado")
                }
            }

            composable(Routes.AJUSTES){
                AjustesScreen(navController = navController, appVM = appVM)
            }
        }
    }
}