package mx.aro.atizaapp_equipo1.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    navController: NavHostController,
    appVM: AppVM
) {
    val context = LocalContext.current
    val esta by appVM.estaLoggeado.collectAsState()

    // 🔹 Modo oscuro persistente dentro del ViewModel o LocalPreferences (simulación aquí)
    var darkMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ajustes") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================
            // 🧍 Sección: Cuenta
            // ==========================
            Text(
                text = "Cuenta",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Text("Conectado: $esta")
            Spacer(modifier = Modifier.height(8.dp))

            ElevatedButton(
                onClick = { appVM.hacerLogout(context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================
            // ⚙️ Sección: Preferencias
            // ==========================
            Text(
                text = "Preferencias",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )



            Spacer(modifier = Modifier.height(16.dp))

            // ==========================
            // 🧪 Sección: Desarrollo / Pruebas
            // ==========================
            Text(
                text = "Desarrollo / Pruebas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

    Button(
        onClick = {
            appVM.createAccount(
                curp = "EELG050828HMCSNBA7",
                fechaNacimiento = "2005-08-28",
                entidadRegistro = "MEXICO",
                nombre = "Gabriel Esperilla Leon"
            )
        }
    ) {
        Text("Crear cuenta")
    }

            Spacer(modifier = Modifier.height(32.dp))

            // ==========================
            // ℹ️ Sección: Información
            // ==========================
            Text(
                text = "Versión de la app: 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}