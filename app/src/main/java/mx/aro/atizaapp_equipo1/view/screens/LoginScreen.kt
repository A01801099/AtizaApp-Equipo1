// LoginScreen.kt
package mx.aro.atizaapp_equipo1.view.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.base.R
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import mx.aro.atizaapp_equipo1.model.apiClientService.TOKEN_WEB
import mx.aro.atizaapp_equipo1.view.components.CurvedSheet
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

@Composable
fun LoginScreen(
    appVM: AppVM,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
    onForgotPasswordClick: () -> Unit  // ← AGREGAR ESTE PARÁMETRO
) {
    val purple = Color(0xFF5B2DCC) // morado del mock
    val white = Color(0xFFFFFFFF)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val authState by appVM.authState.collectAsStateWithLifecycle()
    val isNetworkAvailable by appVM.isNetworkAvailable.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }

    // Google Sign-In launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            // Verificar conexión ANTES de procesar el resultado
            if (!isNetworkAvailable) {
                Toast.makeText(
                    context,
                    "Sin conexión a Internet. Por favor, verifica tu conexión y vuelve a intentar.",
                    Toast.LENGTH_LONG
                ).show()
                return@rememberLauncherForActivityResult
            }

            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            appVM.hacerLoginGoogle(credential)
        } catch (e: ApiException) {
            val errorMessage = when (e.statusCode) {
                com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR -> {
                    "Sin conexión a Internet. Por favor, verifica tu conexión y vuelve a intentar."
                }
                com.google.android.gms.common.api.CommonStatusCodes.CANCELED -> {
                    "Inicio de sesión cancelado."
                }
                else -> {
                    "Error en login con Google "
                }
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error en el login verifique su conexión a internet o intentelo de nuevo mas tarde", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(authState) {
        authState.generalMessage?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }
    DisposableEffect(Unit) { onDispose { appVM.clearAuthState() } }

    // Lienzo general con cabecera morada
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(purple)
    ) {
        // “Tarjeta” blanca con curva superior
        CurvedSheet(
            title = "Inicia Sesión",
            sheetColor = white,
            curveHeight = 64.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.Center)
        ) {
            // ====== Contenido del formulario ======
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                trailingIcon = {
                    val valid = remember(email) { email.contains("@") && email.contains(".") }
                    if (valid) Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF22C55E)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .focusRequester(emailFocusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            keyboardController?.show()
                        }
                    }
                    .fillMaxWidth(),
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        appVM.hacerLoginEmailPassword(email, password)
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            keyboardController?.show()
                        }
                    }
                    .fillMaxWidth(),
                isError = authState.passwordError != null,
                supportingText = { authState.passwordError?.let { Text(it) } }
            )

            // Olvidé mi contraseña (solo UI)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = {
                    onForgotPasswordClick()
                }) {
                    Text("Olvidé mi contraseña", color = purple)
                }
            }

            // Botón principal
            Button(
                onClick = { appVM.hacerLoginEmailPassword(email, password) },
                enabled = !authState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = purple)
            ) {
                Text("Inicia Sesión")
            }

            // LÍNEA divisoria
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Botón Google
            OutlinedButton(
                onClick = {
                    // Verificar conexión ANTES de iniciar el flujo de Google Sign-In
                    if (!isNetworkAvailable) {
                        Toast.makeText(
                            context,
                            "Sin conexión a Internet. Por favor, verifica tu conexión para iniciar sesión con Google.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@OutlinedButton
                    }

                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(TOKEN_WEB)
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    launcher.launch(googleSignInClient.signInIntent)
                },
                enabled = !authState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.googleg_standard_color_18),
                    contentDescription = "Google",
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Text("Continuar con Google")
            }

            // Crear cuenta (link)
            TextButton(
                onClick = {
                    appVM.clearAuthState()
                    onRegisterClick()
                },
                enabled = !authState.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Crear Cuenta", color = purple)
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

