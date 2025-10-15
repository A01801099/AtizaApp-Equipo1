package mx.aro.atizaapp_equipo1.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.aro.atizaapp_equipo1.model.ApiClient
import mx.aro.atizaapp_equipo1.model.CreateAccountRequest
import mx.aro.atizaapp_equipo1.model.CreateAccountResponse
import mx.aro.atizaapp_equipo1.model.TOKEN_WEB
import mx.aro.atizaapp_equipo1.model.Usuario

// Data class para representar el estado de la UI de autenticación
data class AuthState(
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalMessage: String? = null,
    val registrationComplete: Boolean = false
)

// Data class para representar el estado de la pantalla de la credencial
data class CredencialState(
    val isLoading: Boolean = false,
    val usuario: Usuario? = null,
    val error: String? = null
)

class AppVM: ViewModel() {

    //API-ATIZAAP-API
    private val api = ApiClient.service

    private val auth: FirebaseAuth = Firebase.auth

    private val _estaLoggeado = MutableStateFlow(auth.currentUser != null && (auth.currentUser?.isEmailVerified == true || auth.currentUser?.providerData?.any { it.providerId == "google.com" } == true))
    val estaLoggeado = _estaLoggeado.asStateFlow()

    // Nuevo StateFlow para manejar el estado de la UI de autenticación
    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    // Nuevo StateFlow para manejar el estado de la credencial del usuario
    private val _credencialState = MutableStateFlow(CredencialState())
    val credencialState = _credencialState.asStateFlow()


    private val _tieneCredencial = MutableStateFlow(false)
    val tieneCredencial : StateFlow<Boolean> = _tieneCredencial.asStateFlow()



    init {
        auth.currentUser?.reload()?.addOnCompleteListener {
            _estaLoggeado.value = auth.currentUser != null && (auth.currentUser?.isEmailVerified == true || auth.currentUser?.providerData?.any { it.providerId == "google.com" } == true)
        }
    }

    fun clearAuthState() {
        _authState.value = AuthState()
    }

    fun hacerLoginGoogle(credencial: AuthCredential) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            auth.signInWithCredential(credencial)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _estaLoggeado.value = true
                    } else {
                        _estaLoggeado.value = false
                        _authState.update { it.copy(generalMessage = "Error al iniciar sesión con Google.") }
                    }
                     _authState.update { it.copy(isLoading = false) }
                }
        }
    }

    fun hacerLoginEmailPassword(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState(emailError = if(email.isBlank()) "El correo no puede estar vacío" else null, passwordError = if(pass.isBlank()) "La contraseña no puede estar vacía" else null)
            return
        }
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, emailError = null, passwordError = null) }
            auth.signInWithEmailAndPassword(email.trim(), pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {
                            _estaLoggeado.value = true
                        } else {
                            auth.signOut()
                            _authState.update { it.copy(generalMessage = "Por favor, verifica tu correo antes de iniciar sesión.") }
                            _estaLoggeado.value = false
                        }
                    } else {
                        val exception = task.exception
                        val newState = when (exception) {
                            is FirebaseAuthInvalidUserException -> AuthState(emailError = "Correo no registrado.")
                            is FirebaseAuthInvalidCredentialsException -> AuthState(passwordError = "Contraseña incorrecta.")
                            else -> AuthState(generalMessage = "Error: ${exception?.localizedMessage}")
                        }
                        _authState.value = newState
                        _estaLoggeado.value = false
                    }
                    if(_authState.value.generalMessage == null) {
                         _authState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    fun hacerSignUp(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState(emailError = if(email.isBlank()) "El correo no puede estar vacío" else null, passwordError = if(pass.isBlank()) "La contraseña no puede estar vacía" else null)
            return
        }
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, emailError = null, passwordError = null) }
            auth.createUserWithEmailAndPassword(email.trim(), pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.user?.sendEmailVerification()
                        auth.signOut()
                        _authState.value = AuthState(registrationComplete = true, generalMessage = "¡Registro exitoso! Revisa tu correo para verificar la cuenta.")
                    } else {
                        val exception = task.exception
                        val newState = when (exception) {
                            is FirebaseAuthWeakPasswordException -> AuthState(passwordError = "La contraseña es muy débil (mín. 6 caracteres).")
                            is FirebaseAuthUserCollisionException -> AuthState(emailError = "Este correo ya está registrado.")
                            else -> AuthState(generalMessage = "Error en el registro: ${exception?.localizedMessage}")
                        }
                         _authState.value = newState
                    }
                     if(_authState.value.generalMessage == null) {
                         _authState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    fun hacerLogout(context: Context) {
        auth.signOut()
        _estaLoggeado.value = false

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(TOKEN_WEB)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso).signOut()
    }

    fun createAccount(
        curp: String,
        fechaNacimiento: String,
        entidadRegistro: String,
        onSuccess: (CreateAccountResponse) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val email = auth.currentUser?.email
                val nombre = auth.currentUser?.displayName


                if(email == null){
                    throw Exception("Email no encontrado")
                }

                if(nombre == null){
                    throw Exception("Nombre no encontrado")
                }

                val body = CreateAccountRequest(
                    curp = curp,
                    nacimiento = fechaNacimiento,
                    entidadRegistro = entidadRegistro,
                    correo = email,
                    nombre = nombre
                )

                val response = api.createAccount(body)
                onSuccess(response)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun getMe() {
        viewModelScope.launch {
            _credencialState.update { it.copy(isLoading = true, error = null) }
            try {
                val email = auth.currentUser?.email

                if (email == null) {
                    throw Exception("Usuario no autenticado.")
                }
                val response = api.getMe(email)
                _credencialState.update { it.copy(isLoading = false, usuario = response) }
            } catch (e: Exception) {
                Log.e("AppVM", "Error al obtener datos del usuario", e)
                _credencialState.update { it.copy(isLoading = false, error = "Error al obtener los datos: ${e.message}") }
            }
        }
    }
}