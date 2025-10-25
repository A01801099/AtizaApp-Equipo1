package mx.aro.atizaapp_equipo1.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tarjeta blanca con una “semicurva” por encima, centrada.
 * Implementación simple con dos Surfaces: uno circular (la curva) y el cuerpo rectangular.
 */
@Composable
fun CurvedSheet(
    title: String,
    sheetColor: Color,
    curveHeight: Dp,
    modifier: Modifier = Modifier.Companion,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Cuerpo de la tarjeta
        Surface(
            color = sheetColor,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.Companion
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.Companion
                    .padding(horizontal = 20.dp)
                    .padding(top = curveHeight + 16.dp, bottom = 20.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Companion.Black
                )
                content()
            }
        }

    }
}