package mx.aro.atizaapp_equipo1.view.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    navController: NavController,
    appVM: AppVM
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 🌓 Leer modo oscuro guardado al iniciar
    var darkModeEnabled by rememberSaveable { mutableStateOf(ThemePrefs.isDarkMode(context)) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ajustes") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .fillMaxSize()
        ) {
            // ======================
            // 🧭 SECCIÓN: Apariencia
            // ======================
            Text(
                text = "Apariencia",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )

            AjusteItemSwitch(
                title = "Modo oscuro",
                checked = darkModeEnabled,
                onCheckedChange = { enabled ->
                    darkModeEnabled = enabled
                    ThemePrefs.setDarkMode(context, enabled)

                    // 🔄 Reiniciar la actividad para aplicar el nuevo tema
                    val activity = context as Activity
                    activity.recreate()
                }
            )

            Spacer(Modifier.height(24.dp))

            // ======================
            // 🔓 SECCIÓN: Sesión
            // ======================
            Text(
                text = "Sesión",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )

            // ✅ BOTÓN DE LOGOUT
            ElevatedButton(
                onClick = { appVM.hacerLogout(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ======================
// COMPONENTES REUTILIZABLES
// ======================

@Composable
fun AjusteItemSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}