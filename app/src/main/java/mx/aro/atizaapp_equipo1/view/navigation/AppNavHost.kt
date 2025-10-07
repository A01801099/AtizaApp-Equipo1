package mx.aro.atizaapp_equipo1.view.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.aro.atizaapp_equipo1.view.screens.BottomNavigationBar
import mx.aro.atizaapp_equipo1.view.screens.CodigoQRCredencialScreen
import mx.aro.atizaapp_equipo1.view.screens.ContactoScreen
import mx.aro.atizaapp_equipo1.view.screens.ExplorarComerciosScreen
import mx.aro.atizaapp_equipo1.view.screens.LoginScreen
import mx.aro.atizaapp_equipo1.view.screens.MiCredencialScreen
import mx.aro.atizaapp_equipo1.view.screens.RegisterScreen
import mx.aro.atizaapp_equipo1.viewmodel.AppVM
@Composable
fun AppNavHost(appVM: AppVM) {
    val navController = rememberNavController()
    val estaLoggeado = appVM.estaLoggeado.collectAsState().value

    // Obtenemos la ruta actual para decidir si mostrar el BottomBar
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Lista de pantallas donde SÍ se muestra el BottomNavigationBar
    val showBottomBar = currentRoute in listOf(
        "explorar",
        "mi_credencial",
        "contacto",
        "codigo_qr_credencial"
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
            startDestination = if (estaLoggeado) "explorar" else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    appVM = appVM,
                    onRegisterClick = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    appVM = appVM,
                    onLoginClick = { navController.navigate("login") }
                )
            }
            composable("explorar") {
                ExplorarComerciosScreen(appVM = appVM, navController = navController)
            }
            composable("mi_credencial") {
                MiCredencialScreen(navController = navController)
            }
            composable("contacto") {
                ContactoScreen(navController = navController)
            }
            composable("codigo_qr_credencial") {
                CodigoQRCredencialScreen(navController = navController)
            }
        }
    }
}
