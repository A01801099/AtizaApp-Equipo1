package mx.aro.atizaapp_equipo1.view.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mx.aro.atizaapp_equipo1.view.components.CurvedSheet

@Composable
fun DeclaratoriaEdadScreen(
    onAceptar: () -> Unit,
    onRegresar: (() -> Unit)? = null,
    modifier: Modifier = Modifier.Companion
) {
    val purple = Color(0xFF5B2DCC)
    val white = Color(0xFFFFFFFF)
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(purple)

    ) {
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(vertical = 32.dp)
        ) {
            CurvedSheet(
                title = "Declaratoria de Edad",
                sheetColor = white,
                curveHeight = 64.dp,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Companion.Center)
            ) {
                Column(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icono de advertencia
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Advertencia",
                        tint = purple,
                        modifier = Modifier.Companion
                            .size(64.dp)
                            .align(Alignment.Companion.CenterHorizontally)
                    )

                    // Contenido de la declaratoria
                    Card(
                        modifier = Modifier.Companion.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.Companion.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "La aplicación Beneficio Joven está dirigida únicamente a personas de entre 12 y 29 años de edad, conforme a los lineamientos del Instituto Mexiquense de la Juventud.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Companion.Justify
                            )

                            HorizontalDivider(
                                modifier = Modifier.Companion.padding(vertical = 4.dp),
                                color = purple.copy(alpha = 0.2f)
                            )

                            Text(
                                text = "Al crear una cuenta en la App:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Companion.Bold,
                                color = purple
                            )

                            Column(
                                modifier = Modifier.Companion.padding(start = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.Companion.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = "• ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Companion.Bold,
                                        color = purple
                                    )
                                    Text(
                                        text = "Declaras bajo protesta de decir verdad que tienes entre 12 y 29 años.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Companion.Justify,
                                        modifier = Modifier.Companion.weight(1f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.Companion.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = "• ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Companion.Bold,
                                        color = purple
                                    )
                                    Text(
                                        text = "Aceptas que, si tu edad no se encuentra dentro de este rango, no podrás completar el registro ni acceder a los beneficios del programa.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Companion.Justify,
                                        modifier = Modifier.Companion.weight(1f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.Companion.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = "• ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Companion.Bold,
                                        color = purple
                                    )
                                    Text(
                                        text = "Si tienes menos de 12 años o más de 29, no podrás continuar con el proceso de registro.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Companion.Justify,
                                        modifier = Modifier.Companion.weight(1f)
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.Companion.padding(vertical = 8.dp),
                                thickness = 2.dp,
                                color = purple
                            )

                            Card(
                                modifier = Modifier.Companion.fillMaxWidth(),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = purple.copy(alpha = 0.1f)
                                )
                            ) {
                                Text(
                                    text = "⚠️ Al continuar, confirmas que cumples con el requisito de edad establecido.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Companion.Bold,
                                    textAlign = TextAlign.Companion.Center,
                                    color = purple,
                                    modifier = Modifier.Companion.padding(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.Companion.height(8.dp))

                    // Botón de Aceptar
                    Button(
                        onClick = onAceptar,
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = purple)
                    ) {
                        Text(
                            text = "Acepto la Declaratoria de Edad",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Companion.Bold
                        )
                    }

                    Spacer(modifier = Modifier.Companion.height(16.dp))
                }
            }
        }

        // Botón de regresar en la esquina superior izquierda (dibujado al final para que quede encima)
        onRegresar?.let {
            IconButton(
                onClick = it,
                modifier = Modifier.Companion
                    .align(Alignment.Companion.TopStart)
                    .padding(start = 16.dp, top = 56.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Regresar",
                    tint = Color.Companion.Black,
                    modifier = Modifier.Companion.size(32.dp)
                )
            }
        }
    }
}