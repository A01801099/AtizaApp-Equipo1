package mx.aro.atizaapp_equipo1.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.aro.atizaapp_equipo1.view.screens.ExplorarComerciosScreen
import mx.aro.atizaapp_equipo1.view.screens.LoginScreen
import mx.aro.atizaapp_equipo1.view.screens.RegisterScreen
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

@Composable
fun AppNavHost(appVM: AppVM) {
    val navController = rememberNavController()
    val estaLoggeado = appVM.estaLoggeado.collectAsState().value

    NavHost(
        navController = navController,
        startDestination = if (estaLoggeado) "explorar" else "login"
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
            ExplorarComerciosScreen(appVM = appVM)
        }
    }
}
