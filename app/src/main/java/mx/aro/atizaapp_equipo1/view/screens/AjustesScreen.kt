package mx.aro.atizaapp_equipo1.view.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AjustesScreen(
    navController: NavHostController,
    appVM: AppVM // no default, recibe el compartido desde AppNavHost
) {
    val context = LocalContext.current

    Text("Ajustes")

    ElevatedButton(
        onClick = {
            appVM.hacerLogout(context)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Logout")
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = {
            appVM.createAccount(
                curp = "EELG050828HMCSNBA7",
                fechaNacimiento = "2005-08-28",
                entidadRegistro = "MEXICO",
                onSuccess = { res -> println("Success: $res") },
                onError = { err -> println("Error: $err") }
            )
        }
    ) {
        Text("Crear cuenta")
    }

    // Opcional debug visual:
    val esta by appVM.estaLoggeado.collectAsState()
    Text("Conectado: $esta")
}