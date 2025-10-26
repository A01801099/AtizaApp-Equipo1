package mx.aro.atizaapp_equipo1.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import mx.aro.atizaapp_equipo1.R
import mx.aro.atizaapp_equipo1.model.data_classes.Negocio
import mx.aro.atizaapp_equipo1.utils.convertGoogleDriveUrl

// -------------------- ITEM DE CADA NEGOCIO --------------------
/**
 * Muestra un elemento de la lista de negocios.
 */
@Composable
fun NegocioItem(negocio: Negocio, navController: NavHostController) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .clickable { navController.navigate("explorar_comercio/${negocio.id}") },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.Companion.padding(8.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            AsyncImage(
                model = convertGoogleDriveUrl(negocio.imagen),
                contentDescription = negocio.nombre,
                modifier = Modifier.Companion.size(80.dp),
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_foreground)
            )
            Column(modifier = Modifier.Companion.padding(start = 8.dp)) {
                Text(negocio.nombre, fontSize = 18.sp, fontWeight = FontWeight.Companion.Bold)
                Text("Tipo: ${negocio.tipo}", fontSize = 14.sp)
                Text("Ubicación: ${negocio.ubicacion}", fontSize = 12.sp)
            }
        }
    }
}