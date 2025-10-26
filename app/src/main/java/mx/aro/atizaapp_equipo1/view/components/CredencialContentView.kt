package mx.aro.atizaapp_equipo1.view.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.aro.atizaapp_equipo1.R
import mx.aro.atizaapp_equipo1.model.data_classes.Usuario
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Muestra el contenido visual de la credencial del usuario.
 */
@Composable
fun CredencialContentView(usuario: Usuario, idFormateado: String?) {
    val fechaVencimiento = remember(usuario.nacimiento) {
        try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val nacimiento = formatoEntrada.parse(usuario.nacimiento)
            val calendar = Calendar.getInstance().apply {
                time = nacimiento!!
                add(Calendar.YEAR, 29)
            }
            formatoSalida.format(calendar.time)
        } catch (e: Exception) {
            "Fecha inválida"
        }
    }

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.Companion.height(16.dp))

        // Box para superponer el ID sobre la imagen
        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Companion.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.atras_tarjeta2),
                contentDescription = "Imagen de la credencial",
                modifier = Modifier.Companion.fillMaxWidth(),
                contentScale = ContentScale.Companion.FillWidth
            )

            idFormateado?.let { id ->
                Text(
                    text = id,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = Color.Companion.Black,
                    textAlign = TextAlign.Companion.Center,
                    modifier = Modifier.Companion.offset(y = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.Companion.height(24.dp))

        // Datos del usuario
        Text(
            text = "CURP: ${usuario.curp}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Companion.Medium
        )
        Text(
            text = "Nombre: ${usuario.nombre}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Companion.Medium
        )
        Text(
            text = "Correo: ${usuario.correo}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Companion.Medium
        )
        Text(
            text = "Estado: ${usuario.estado}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Companion.Medium
        )
        Text(
            text = "Nacimiento: ${usuario.nacimiento}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Companion.Medium
        )
        Spacer(modifier = Modifier.Companion.height(32.dp))
        Text(
            text = "Vencimiento de la tarjeta: $fechaVencimiento",
            fontSize = 18.sp,
            fontWeight = FontWeight.Companion.Medium
        )

        Spacer(modifier = Modifier.Companion.height(32.dp))
    }
}