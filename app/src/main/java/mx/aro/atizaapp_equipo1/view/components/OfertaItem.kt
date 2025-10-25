package mx.aro.atizaapp_equipo1.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.aro.atizaapp_equipo1.model.data_classes.Oferta
import mx.aro.atizaapp_equipo1.utils.formatFechaOfertas

@Composable
fun OfertaItem(oferta: Oferta, onClick: () -> Unit) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            // Ícono de oferta
            Box(
                modifier = Modifier.Companion
                    .size(56.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Companion.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalOffer,
                    contentDescription = "Oferta",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.Companion.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.Companion.width(16.dp))

            // Contenido de la oferta
            Column(
                modifier = Modifier.Companion.weight(1f)
            ) {
                Text(
                    text = oferta.titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Companion.Ellipsis
                )

                Spacer(modifier = Modifier.Companion.height(4.dp))

                Text(
                    text = oferta.descripcion,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Companion.Ellipsis
                )

                Spacer(modifier = Modifier.Companion.height(8.dp))

                // Fecha de vencimiento
                Row(
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Fecha",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.Companion.size(16.dp)
                    )
                    Spacer(modifier = Modifier.Companion.width(4.dp))
                    Text(
                        text = "Vencimiento hasta ${formatFechaOfertas(oferta.fechaFin ?: "")}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Precio si existe y no es 0
                if (oferta.precio.toDoubleOrNull() != null && oferta.precio.toDouble() > 0) {
                    Spacer(modifier = Modifier.Companion.height(4.dp))
                    Text(
                        text = "Precio: $${oferta.precio}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Companion.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}