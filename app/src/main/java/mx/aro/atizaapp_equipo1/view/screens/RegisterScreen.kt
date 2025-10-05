// RegisterScreen.kt
package mx.aro.atizaapp_equipo1.view.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

@Composable
fun RegisterScreen(
    appVM: AppVM,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val purple = Color(0xFF5B2DCC)
    val white = Color(0xFFFFFFFF)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val authState by appVM.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState.registrationComplete) {
            Toast.makeText(context, authState.generalMessage, Toast.LENGTH_LONG).show()
            appVM.clearAuthState()
            onLoginClick()
        }
        authState.generalMessage?.let {
            if (!authState.registrationComplete) Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
    DisposableEffect(Unit) { onDispose { appVM.clearAuthState() } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(purple)
    ) {
        CurvedSheet(
            title = "Crear Cuenta",
            sheetColor = white,
            curveHeight = 64.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.Center)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = authState.emailError != null,
                supportingText = { authState.emailError?.let { Text(it) } }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = authState.passwordError != null,
                supportingText = { authState.passwordError?.let { Text(it) } }
            )

            Button(
                onClick = { appVM.hacerSignUp(email, password) },
                enabled = !authState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = purple)
            ) {
                Text("Registrarse")
            }

            TextButton(
                onClick = {
                    appVM.clearAuthState()
                    onLoginClick()
                },
                enabled = !authState.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = purple)
            }

            if (authState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.CenterHorizontally),
                    color = purple
                )
            }
        }
    }
}
