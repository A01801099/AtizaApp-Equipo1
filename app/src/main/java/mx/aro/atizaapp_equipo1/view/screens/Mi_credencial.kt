package mx.aro.atizaapp_equipo1.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiCredencialScreen(navController: NavHostController) {
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
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            // Aquí puedes agregar tu imagen de credencial
            // Ejemplo temporal:
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Aquí irá la imagen de tu credencial",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Número de Tarjeta: 111111111.-11-11-1-1",
                fontSize = 16.sp
            )
            Text(
                text = "Propietario: Arturo Rosas Osorio",
                fontSize = 16.sp
            )
            Text(
                text = "Fecha de vencimiento: 12/9999",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Imagen del propietario",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Aquí puedes poner una imagen genérica
            Text(text = "Imagen genérica", color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* No hace nada por ahora */ },
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Cambiar imagen")
            }
        }
    }
}
