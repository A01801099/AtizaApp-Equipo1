package mx.aro.atizaapp_equipo1.view.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

@Composable
fun RegisterScreen(
    appVM: AppVM,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by appVM.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = authState) {
        if (authState.registrationComplete) {
            Toast.makeText(context, authState.generalMessage, Toast.LENGTH_LONG).show()
            appVM.clearAuthState()
            onLoginClick()
        }
        authState.generalMessage?.let { message ->
             if(!authState.registrationComplete) Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    // Limpia el estado cuando la pantalla ya no está en composición
    DisposableEffect(Unit) {
        onDispose {
            appVM.clearAuthState()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Crear Nueva Cuenta",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = authState.emailError != null,
                    supportingText = {
                        authState.emailError?.let { Text(it) }
                    }
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = authState.passwordError != null,
                    supportingText = {
                        authState.passwordError?.let { Text(it) }
                    }
                )

                Button(
                    onClick = {
                        appVM.hacerSignUp(email, password)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !authState.isLoading
                ) {
                    Text("REGISTRARSE", modifier = Modifier.padding(8.dp))
                }

                TextButton(
                    onClick = {
                        appVM.clearAuthState() // Limpia errores antes de navegar
                        onLoginClick()
                    },
                    enabled = !authState.isLoading
                ) {
                    Text("¿Ya tienes cuenta? Inicia sesión")
                }

                if (authState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}