package mx.aro.atizaapp_equipo1.view.screens

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
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

    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var fontScale by rememberSaveable { mutableStateOf(1f) }
    var accessibleMode by rememberSaveable { mutableStateOf(false) }

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

            AjusteSlider(
                title = "Tamaño de texto",
                value = fontScale,
                onValueChange = { fontScale = it }
            )

            // ======================
            // 🔔 SECCIÓN: Notificaciones
            // ======================
            Text(
                text = "Notificaciones",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )

            AjusteItemSwitch(
                title = "Recibir notificaciones",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )

            // ======================
            // ♿ SECCIÓN: Accesibilidad
            // ======================
            Text(
                text = "Accesibilidad",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )

            AjusteItemSwitch(
                title = "Modo accesible (botones grandes)",
                checked = accessibleMode,
                onCheckedChange = { accessibleMode = it }
            )

            // ======================
            // ⚙️ SECCIÓN: Sistema
            // ======================
            Text(
                text = "Sistema",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )

            AjusteButton(
                title = "Limpiar caché",
                onClick = {
                    Toast.makeText(context, "Caché limpiada", Toast.LENGTH_SHORT).show()
                }
            )

            // ✅ BOTÓN DE LOGOUT CORREGIDO
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

@Composable
fun AjusteSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(title)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.8f..1.5f,
            steps = 4
        )
        Text("Vista previa", fontSize = (16 * value).sp)
    }
}

@Composable
fun AjusteButton(
    title: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(title)
    }
}