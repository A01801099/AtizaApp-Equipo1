package mx.aro.atizaapp_equipo1.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import mx.aro.atizaapp_equipo1.R
import mx.aro.atizaapp_equipo1.model.Usuario
import mx.aro.atizaapp_equipo1.viewmodel.AppVM
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiCredencialScreen(navController: NavHostController, appVM: AppVM) {
    // 1. Llama a getMe() una sola vez cuando la pantalla aparece
    LaunchedEffect(key1 = Unit) {
        appVM.getMe()
    }

    // 2. Observa el idFormateado y el estado de la credencial desde el ViewModel
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                // Indicador de carga
                credencialState.isLoading -> {
                    CircularProgressIndicator()
                }
                // Mensaje de error
                credencialState.error != null -> {
                    Text(
                        text = "Error: ${credencialState.error}",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                // Mostrar datos del usuario cuando se cargan
                credencialState.usuario != null -> {
                    CredencialContentView(
                        usuario = credencialState.usuario!!,
                        idFormateado = idFormateado
                    )
                }
            }
        }
    }
}

@Composable
private fun CredencialContentView(usuario: Usuario, idFormateado: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 🖼️ Box para superponer el ID sobre la imagen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Imagen de la credencial
            Image(
                painter = painterResource(id = R.drawable.atras_tarjeta2),
                contentDescription = "Imagen de la credencial",
                modifier = Modifier.fillMaxWidth()
            )

            // Texto del ID formateado centrado sobre la imagen
            idFormateado?.let { id ->
                Text(
                    text = id,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(y = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Datos del usuario
        Text(text = "CURP: ${usuario.curp}", fontSize = 16.sp)
        Text(text = "Nombre: ${usuario.nombre}", fontSize = 16.sp)
        Text(text = "Correo: ${usuario.correo}", fontSize = 16.sp)
        Text(text = "Estado: ${usuario.estado}", fontSize = 16.sp)
        Text(text = "Nacimiento: ${usuario.nacimiento}", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(32.dp))
    }
}