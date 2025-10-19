package mx.aro.atizaapp_equipo1.view.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

/**
 * Muestra el QR usando el id del usuario obtenido desde el backend (getMe).
 * Si el usuario no está cargado llama a appVM.getMe() una vez.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodigoQRCredencialScreen(
    navController: NavHostController,
    appVM: AppVM = viewModel()
) {
    // Estado de la credencial desde el ViewModel
    val credState by appVM.credencialState.collectAsState()

    // Si no tenemos usuario, pedimos los datos una vez
    LaunchedEffect(Unit) {
        if (credState.usuario == null) {
            try {
                appVM.getMe()
            } catch (e: Exception) {
                // getMe ya maneja errores internamente, aun así mostramos toast por seguridad
                Toast.makeText(navController.context, "Error al obtener la credencial", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Obtener idUsuario si existe
    val idUsuario: Int? = credState.usuario?.id

    // Generar QR cuando idUsuario cambie (si es null no genera)
    val qrBitmap: Bitmap? = remember(idUsuario) {
        idUsuario?.let { generarCodigoQR(it.toString()) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi credencial") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("mi_credencial") {
                            popUpTo("mi_credencial") { inclusive = false }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.CardMembership,
                            contentDescription = "Volver a credencial"
                        )
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
            verticalArrangement = Arrangement.Center
        ) {
            // Si aún no hay usuario, mostrar loader
            if (idUsuario == null) {
                CircularProgressIndicator()
                Text(
                    text = "Cargando credencial...",
                    modifier = Modifier.padding(top = 12.dp),
                    textAlign = TextAlign.Center
                )
                return@Column
            }

            // Mostrar el código QR generado
            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Código QR del usuario",
                    modifier = Modifier.size(200.dp)
                )
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(24.dp))

            // Mostrar el texto del id debajo
            Text(
                text = "ID de usuario: $idUsuario",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Función auxiliar para generar un código QR desde un texto
 * (misma implementación que tenías, adaptada aquí)
 */
fun generarCodigoQR(texto: String, size: Int = 512): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            texto,
            BarcodeFormat.QR_CODE,
            size,
            size
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }

        Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}