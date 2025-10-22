package mx.aro.atizaapp_equipo1.view.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CloudOff
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
 * Pantalla de Código QR de la Credencial
 * Modificada para usar caché local con sincronización en background
 *
 * OPTIMIZACIONES:
 * - Carga instantánea desde caché (< 20ms)
 * - Genera QR inmediatamente con datos locales
 * - Sincroniza en background si es necesario
 * - Muestra banner de estado (online/offline/sincronizando)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodigoQRCredencialScreen(
    navController: NavHostController,
    appVM: AppVM = viewModel()
) {
    // Estado de la credencial desde el ViewModel
    val credState by appVM.credencialState.collectAsState()
    val idFormateado by appVM.idFormateado.collectAsState()

    // Cargar credencial (primero intenta caché, luego sincroniza si es necesario)
    LaunchedEffect(Unit) {
        appVM.getMe()
    }

    // Obtener idUsuario si existe
    val idUsuario: Int? = credState.usuario?.id

    // Generar QR con el ID formateado (usa el del ViewModel que ya está formateado)
    val qrBitmap: Bitmap? = remember(idFormateado) {
        idFormateado?.let { generarCodigoQR(it) }
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
            modifier = Modifier.padding(innerPadding)
        ) {
            // Banner de estado de sincronización (igual que en Mi_credencial.kt)
            credState.error?.let { errorMsg ->
                if (errorMsg.contains("offline", ignoreCase = true) ||
                    errorMsg.contains("Sincronizando", ignoreCase = true)) {

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

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    // Cargando (solo si no hay datos aún)
                    credState.isLoading && credState.usuario == null -> {
                        CircularProgressIndicator()
                        Text(
                            text = "Cargando credencial...",
                            modifier = Modifier.padding(top = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Datos cargados (desde caché o servidor)
                    credState.usuario != null && idFormateado != null -> {
                        val usuario = credState.usuario!!

                        // Mostrar el código QR generado
                        qrBitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Código QR del usuario",
                                modifier = Modifier.size(300.dp)
                            )
                        } ?: run {
                            // Si por alguna razón no se pudo generar el QR
                            Text(
                                text = "Error al generar código QR",
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Credencial Digital",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Error crítico (sin datos disponibles)
                    credState.error != null && credState.usuario == null -> {

                        Text(
                            text = "No se pudo cargar la credencial",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🔧 Función para dar formato al ID del usuario:
 * Ejemplo:
 * id = 1      → 0000-0000-0000-0001
 * id = 10000  → 0000-0000-0001-0000
 */
fun formatearIdUsuario(id: Int): String {
    val idStr = id.toString().padStart(16, '0') // asegura 16 dígitos
    return idStr.chunked(4).joinToString("-")  // agrupa de 4 en 4 con guiones
}

/**
 * Función auxiliar para generar un código QR desde un texto
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