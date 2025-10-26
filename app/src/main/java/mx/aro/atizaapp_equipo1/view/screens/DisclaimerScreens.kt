package mx.aro.atizaapp_equipo1.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mx.aro.atizaapp_equipo1.view.components.CurvedSheet

/**
 * Muestra el aviso de privacidad que el usuario debe aceptar.
 */
@Composable
fun AvisoPrivacidadScreen(
    onAceptar: () -> Unit,
    onRegresar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
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
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 32.dp)
        ) {
            CurvedSheet(
                title = "Aviso de Privacidad",
                sheetColor = white,
                curveHeight = 64.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Center)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = purple,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "La aplicación Beneficio Joven es operada por el Gobierno Municipal de Atizapán de Zaragoza a través del Instituto de la Juventud, quien es responsable del tratamiento de los datos personales recabados.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = purple.copy(alpha = 0.2f)
                            )

                            Text(
                                text = "Uso de datos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = purple
                            )

                            Text(
                                text = "Los datos personales solicitados (nombre, CURP, correo electrónico, fecha de nacimiento, fotografía y ubicación aproximada) serán utilizados exclusivamente para verificar tu identidad dentro de la aplicación de beneficios del Instituto de la Juventud.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = purple.copy(alpha = 0.2f)
                            )

                            Text(
                                text = "Protección de datos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = purple
                            )

                            Text(
                                text = "Tus datos no serán transferidos a terceros ajenos al Gobierno Municipal sin tu consentimiento, salvo obligaciones legales.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            Text(
                                text = "Puedes ejercer tus derechos de Acceso, Rectificación, Cancelación y Oposición (ARCO) enviando una solicitud al correo:",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            Text(
                                text = "transparencia.juventud@atizapan.gob.mx",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = purple,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = purple.copy(alpha = 0.2f)
                            )

                            Text(
                                text = "Marco legal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = purple
                            )

                            Text(
                                text = "El tratamiento de la información se realiza conforme a la Ley Federal de Protección de Datos Personales en Posesión de los Sujetos Obligados y a las políticas de privacidad del Ayuntamiento.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 2.dp,
                                color = purple
                            )

                            Text(
                                text = "Al continuar, confirmas que has leído y aceptas este Aviso de Privacidad.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = purple,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón de Aceptar
                    Button(
                        onClick = onAceptar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = purple)
                    ) {
                        Text(
                            text = "Aceptar Aviso de Privacidad",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Botón de regresar en la esquina superior izquierda (dibujado al final para que quede encima)
        onRegresar?.let {
            IconButton(
                onClick = it,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 48.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Regresar",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

