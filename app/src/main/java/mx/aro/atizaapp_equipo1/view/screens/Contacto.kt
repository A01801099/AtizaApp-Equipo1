package mx.aro.atizaapp_equipo1.view.screens
import mx.aro.atizaapp_equipo1.R
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactoScreen(navController: NavHostController) {

    // 📍 Coordenadas de la Dirección de Juventud en Atizapán
    val juventudLocation = LatLng(19.5570659, -99.2422345)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(juventudLocation, 15f)
    }

    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Contáctanos") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_atizapan),
                    contentDescription = "Logo Atizapán",
                    modifier = Modifier.size(150.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.logo_direccion),
                    contentDescription = "Logo Dirección",
                    modifier = Modifier.size(100.dp)
                )
            }

            Text("Si tienes dudas o comentarios, contáctanos en:", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))


            Spacer(modifier = Modifier.height(24.dp))

            // 🗺️ Mapa interactivo con marcador
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = juventudLocation),
                        title = "Dirección de Juventud",
                        snippet = "Atizapán de Zaragoza"
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Teléfono: 55-16-68-17-48", fontSize = 16.sp)
            Text(
                "Dirección: Avenida del parque SN, " +
                        " Jardines de Atizapán, Atizapán de Zaragoza",
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
        }
    }
}
