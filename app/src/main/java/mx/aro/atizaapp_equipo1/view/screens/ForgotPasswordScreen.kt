package mx.aro.atizaapp_equipo1.view.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mx.aro.atizaapp_equipo1.view.components.CurvedSheet
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

/**
 * Muestra el formulario para solicitar el restablecimiento de contraseña.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    appVM: AppVM,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val purple = Color(0xFF5B2DCC)
    val white = Color(0xFFFFFFFF)

    val forgotPasswordState by appVM.forgotPasswordState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(purple)
    ) {
        CurvedSheet(
            title = "Recuperar Contraseña",
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
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = forgotPasswordState.email,
                    onValueChange = { appVM.onForgotPasswordEmailChange(it) },
                    label = { Text("Correo electrónico") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    singleLine = true,
                    enabled = !forgotPasswordState.isLoading,
                    isError = forgotPasswordState.error != null,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ejemplo@correo.com") }
                )

                val errorMessage = forgotPasswordState.error
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (forgotPasswordState.sent) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "✓ Si existe una cuenta asociada, te enviamos un correo para restablecer tu contraseña. Revisa tu bandeja de entrada o spam.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { appVM.sendPasswordResetEmail() },
                    enabled = !forgotPasswordState.isLoading &&
                            Patterns.EMAIL_ADDRESS.matcher(forgotPasswordState.email.trim())
                                .matches(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = purple)
                ) {
                    if (forgotPasswordState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = white
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Enviar correo de restablecimiento")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onBackToLogin,
                    enabled = !forgotPasswordState.isLoading
                ) {
                    Text(
                        "Volver a iniciar sesión",
                        color = purple
                    )
                }
            }
        }
    }
}
