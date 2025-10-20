package mx.aro.atizaapp_equipo1.view.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.aro.atizaapp_equipo1.view.screens.AjustesScreen
import mx.aro.atizaapp_equipo1.view.screens.MiCredencialScreen
import mx.aro.atizaapp_equipo1.view.screens.BottomNavigationBar
import mx.aro.atizaapp_equipo1.view.screens.CodigoQRCredencialScreen
import mx.aro.atizaapp_equipo1.view.screens.ContactoScreen
import mx.aro.atizaapp_equipo1.view.screens.DetalleComercioScreen
import mx.aro.atizaapp_equipo1.view.screens.ExplorarComerciosScreen
import mx.aro.atizaapp_equipo1.view.screens.LoginScreen
import mx.aro.atizaapp_equipo1.view.screens.RegisterScreen
import mx.aro.atizaapp_equipo1.view.screens.CreateCredentialScreen
import mx.aro.atizaapp_equipo1.view.screens.LoadingScreen
import mx.aro.atizaapp_equipo1.view.screens.ForgotPasswordScreen
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

@Composable
fun AppNavHost(appVM: AppVM) {
    val navController = rememberNavController()
    val estaLoggeado = appVM.estaLoggeado.collectAsState().value
    val verificationState = appVM.verificationState.collectAsState().value // ← Cambio aquí
    val credencialChecked = appVM.credencialChecked.collectAsState().value

    // Verificar credencial cuando el usuario está loggeado
    // Se ejecuta cuando el usuario vuelve a ingresar a la app después de cerrarla,
    // el login en Firebase persiste pero el state de hasCredencial tiene que ser consultado cada que se abre la app
    androidx.compose.runtime.LaunchedEffect(estaLoggeado, credencialChecked) {
        if (estaLoggeado && !credencialChecked) {
            appVM.verificarCredencial()
        }
    }

    // Determinar la pantalla inicial según el flujo
    // Esta lógica es REACTIVA: cuando cambian los estados, NavHost se re-renderiza
    // y ajusta la navegación automáticamente SIN necesidad de navegación programática
    val startDestination = when {
        !estaLoggeado -> "login"
        !credencialChecked -> "loading" // Mostrar pantalla de carga mientras se verifica
        verificationState.hasCredencial -> "explorar"
        else -> "register_credencial"
    }

    // Obtenemos la ruta actual para decidir si mostrar el BottomBar
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Lista de pantallas donde SÍ se muestra el BottomNavigationBar
    val showBottomBar = currentRoute in listOf(
        "explorar",
        "mi_credencial",
        "contacto",
        "codigo_qr_credencial",
        "explorar_comercio",
        "ajustes"
    )

    Scaffold(
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
            // LOADING
            composable("loading") {
                LoadingScreen(message = "Verificando credencial...")
            }

            // AUTH
            composable("login") {
                LoginScreen(
                    appVM = appVM,
                    onRegisterClick = { navController.navigate("register") },
                    onForgotPasswordClick = { navController.navigate("forgot_password") }
                )
            }
            composable("register") {
                RegisterScreen(
                    appVM = appVM,
                    onLoginClick = { navController.navigate("login") }
                )
            }
            composable("forgot_password") {
                ForgotPasswordScreen(
                    appVM = appVM,
                    onBackToLogin = {
                        appVM.clearForgotPasswordState()
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            // ONBOARDING: registro de credencial obligatorio
            composable("register_credencial") {
                CreateCredentialScreen(
                    appVM = appVM,
                    onDone = {
                        appVM.verificarCredencial()
                        navController.navigate("explorar")
                    },
                    onCancel = { navController.navigate("login") }
                )
            }
            // MAIN
            composable("explorar") {
                val verification = appVM.verificationState.collectAsState().value

                // Mostrar mensaje si está en modo offline
                if (verification.isNetworkError) {
                    androidx.compose.foundation.layout.Column {
                        // Banner de advertencia
                        androidx.compose.foundation.layout.Box(
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxWidth()
                                .background(androidx.compose.ui.graphics.Color(0xFFFF9800))
                                .padding(8.dp)
                        ) {
                            androidx.compose.material3.Text(
                                text = "⚠️ ${verification.error ?: "Sin conexión"}",
                                color = androidx.compose.ui.graphics.Color.White,
                                modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.Center)
                            )
                        }
                        ExplorarComerciosScreen(appVM = appVM, navController = navController)
                    }
                } else {
                    ExplorarComerciosScreen(appVM = appVM, navController = navController)
                }
            }
            composable("mi_credencial") {
                MiCredencialScreen(navController = navController, appVM = appVM)
            }
            composable("contacto") {
                ContactoScreen(navController = navController)
            }
            composable("codigo_qr_credencial") {
                CodigoQRCredencialScreen(navController = navController)
            }
            composable("explorar_comercio/{id}") { backStackEntry ->
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

            composable("ajustes"){
                AjustesScreen(navController = navController, appVM = appVM)
            }
        }
    }
}
