package mx.aro.atizaapp_equipo1.view.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import mx.aro.atizaapp_equipo1.view.screens.OfertasScreen
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

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
        !estaLoggeado -> "login"
        !credencialChecked -> "loading"  // Mostrar loading mientras verifica
        verificationState.hasCredencial -> "explorar"
        verificationState.isNetworkError -> "explorar"  // ⚠️ Sin red: permitir acceso offline
        else -> "register_credencial"  // Sin credencial: ir a registro
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
        "explorar_comercio/{id}",
        "ajustes",
        "beneficios"
    )

    // Pantallas donde se debe mostrar el banner de offline
    val showOfflineBanner = estaLoggeado && !isNetworkAvailable && currentRoute in listOf(
        "explorar",
        "mi_credencial",
        "contacto",
        "codigo_qr_credencial",
        "explorar_comercio/{id}",
        "ajustes",
        "beneficios"
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
                ExplorarComerciosScreen(appVM = appVM, navController = navController)
            }
            composable("mi_credencial") {
                MiCredencialScreen(navController = navController, appVM = appVM)
            }
            composable("contacto") {
                ContactoScreen(navController = navController)
            }
            composable("codigo_qr_credencial") {
                CodigoQRCredencialScreen(navController = navController, appVM = appVM)
            }

            composable("beneficios") {
                OfertasScreen(navController = navController, appVM = appVM)
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

/**
 * Banner de modo offline que se muestra en la parte superior de la app
 * Indica al usuario que está sin conexión y solo puede acceder a la credencial
 */
@Composable
private fun OfflineBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF607D8B)) // Naranja oscuro para mejor visibilidad
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Sin conexión",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Modo Sin Conexión",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Mostrando los datos de la ultima sincronización",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }
        }
    }
}
