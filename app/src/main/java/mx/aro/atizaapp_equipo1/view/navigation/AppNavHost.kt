package mx.aro.atizaapp_equipo1.view.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
import mx.aro.atizaapp_equipo1.view.screens.CreateCredentialScreen // <-- crea esta pantalla
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

            //Todo mostrar la pantalla de ce crearCredencial despues de estar loggeado y si y solo si se tiene credencial se pasa a ala pantallad e explorar comercios
            //usar tiene_credencial
            navController = navController,
            startDestination = if (estaLoggeado) "explorar" else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            // AUTH
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

            // ONBOARDING: registro de credencial obligatorio
            composable("register_credencial") {
                CreateCredentialScreen(
                    appVM = appVM,
                    onDone = {
                        navController.navigate("explorar")
                    },
                    onCancel = {
                        println("El usuario canceló la creación de la credencial")
                    }
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
