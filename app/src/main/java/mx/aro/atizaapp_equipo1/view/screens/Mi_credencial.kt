package mx.aro.atizaapp_equipo1.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import mx.aro.atizaapp_equipo1.view.components.CredencialContentView
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

/**
 * Muestra la pantalla principal de la credencial del usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiCredencialScreen(navController: NavHostController, appVM: AppVM) {
    LaunchedEffect(key1 = Unit) {
        appVM.getMe()
    }

    val idFormateado by appVM.idFormateado.collectAsState()
    val credencialState by appVM.credencialState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi credencial") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("codigo_qr_credencial") {
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.QrCode, contentDescription = "Código QR")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            // Banner de estado de sincronización
            credencialState.error?.let { errorMsg ->
                if (errorMsg.contains("offline", ignoreCase = true) ||
                    errorMsg.contains("Sincronizando", ignoreCase = true)
                ) {

                    val isOffline = errorMsg.contains("offline", ignoreCase = true)
                    val isSyncing = errorMsg.contains("Sincronizando", ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                when {
                                    isOffline -> Color(0xFFFF9800) // Naranja para offline
                                    isSyncing -> Color(0xFF2196F3) // Azul para sincronizando
                                    else -> Color(0xFFFFC107) // Amarillo por defecto
                                }
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.Sync,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMsg,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Contenido principal de la credencial
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    // Indicador de carga (solo si no hay datos cargados aún)
                    credencialState.isLoading && credencialState.usuario == null -> {
                        CircularProgressIndicator()
                    }
                    // Mostrar datos del usuario cuando se cargan
                    credencialState.usuario != null -> {
                        CredencialContentView(
                            usuario = credencialState.usuario!!,
                            idFormateado = idFormateado
                        )
                    }
                    // Mensaje de error crítico (sin datos disponibles)
                    credencialState.error != null && credencialState.usuario == null -> {
                        Text(
                            text = "${credencialState.error}",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
}

