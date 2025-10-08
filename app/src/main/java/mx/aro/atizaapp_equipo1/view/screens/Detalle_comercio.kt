package mx.aro.atizaapp_equipo1.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng
import mx.aro.atizaapp_equipo1.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleComercioScreen(navController: NavHostController) {
    // 📍 Ubicación de ejemplo (puedes cambiar las coordenadas)
    val juventudLocation = LatLng(19.5570659, -99.2422345) // Atizapán de Zaragoza
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(juventudLocation, 15f)
    }

    val scrollState = rememberScrollState()

    // 🏪 Datos de ejemplo del comercio
    val nombreComercio = "Cafetería Central"
    val descripcionComercio =
        "Un lugar acogedor para disfrutar de café artesanal, postres caseros y un ambiente tranquilo para conversar o trabajar."

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
        ) {
            // 🖼️ Imagen superior del comercio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fondo_beneficio_joven), // Reemplaza con tu imagen real
                    contentDescription = "Imagen del comercio",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // 🔙 Botón de retroceso
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .background(Color(0x66000000), shape = MaterialTheme.shapes.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Volver a explorar",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🏪 Nombre del comercio
            Text(
                text = nombreComercio,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 📝 Descripción del comercio
            Text(
                text = descripcionComercio,
                fontSize = 16.sp,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🗺️ Sección de mapa
            Text(
                text = "¿Cómo llegar?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = juventudLocation),
                        title = "Cafetería Central",
                        snippet = "Atizapán de Zaragoza"
                    )
                }
            }

            Text(
                text = "¿Contáctanos?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Teléfono: 55-16-68-17-48",
                fontSize = 16.sp,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Dirección: Avenida del parque SN, " +
                        " Jardines de Atizapán, Atizapán de Zaragoza",
                fontSize = 16.sp,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}