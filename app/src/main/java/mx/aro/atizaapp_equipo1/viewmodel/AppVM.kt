package mx.aro.atizaapp_equipo1.viewmodel

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.aro.atizaapp_equipo1.model.ApiClient
import mx.aro.atizaapp_equipo1.model.CreateAccountRequest
import mx.aro.atizaapp_equipo1.model.CreateAccountResponse
import mx.aro.atizaapp_equipo1.model.Negocio
import mx.aro.atizaapp_equipo1.model.NegociosApiResponse

import mx.aro.atizaapp_equipo1.model.TOKEN_WEB
import mx.aro.atizaapp_equipo1.model.Usuario
import androidx.compose.foundation.lazy.items
import androidx.room.util.copy
import mx.aro.atizaapp_equipo1.model.Oferta
import mx.aro.atizaapp_equipo1.view.screens.formatearIdUsuario
import mx.aro.atizaapp_equipo1.view.screens.formatearIdUsuario
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

// Data class para verificar SOLO la existencia de la credencial (para navegación)
data class VerificationCredencialState(
    val isLoading: Boolean = false,
    val hasCredencial: Boolean = false,
    val error: String? = null,
    val isNetworkError: Boolean = false
)

// Data class para el estado de la lista de negocios con paginación por cursor
data class NegociosState(
    val isLoadingInitial: Boolean = false, // Carga de pantalla completa la primera vez
    val isLoadingMore: Boolean = false,    // Spinner al final de la lista para paginación
    val negocios: List<Negocio> = emptyList(),
    val error: String? = null,
    val nextCursor: String? = null,        // Cursor para la siguiente página. Nulo para la primera llamada.
    val endReached: Boolean = false        // true si la API devuelve un cursor nulo
)

// Data class para el estado de recuperación de contraseña
data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val sent: Boolean = false,
    val error: String? = null
)

// Data class para el estado de creación de credencial
data class CreateCredentialState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorTitle: String? = null,
    val errorMessage: String? = null,
    val canRetry: Boolean = false  // Indica si se puede mostrar botón "Reintentar"
)

data class OfertasState(
    val isLoadingInitial: Boolean = false,
    val isLoadingMore: Boolean = false,
    val ofertas: List<Oferta> = emptyList(),
    val error: String? = null,
    val nextCursor: String? = null,
    val endReached: Boolean = false
)

// Data class para el estado de ofertas de un negocio específico
data class OfertasNegocioState(
    val isLoading: Boolean = false,
    val ofertas: List<Oferta> = emptyList(),
    val error: String? = null,
    val negocioId: Int? = null
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

    // Nuevo StateFlow para manejar el estado de la credencial del usuario (datos completos)
    private val _credencialState = MutableStateFlow(CredencialState())
    val credencialState = _credencialState.asStateFlow()

    private val _idFormateado = MutableStateFlow<String?>(null)
    val idFormateado: StateFlow<String?> = _idFormateado

    // Nuevo StateFlow para verificación de existencia de credencial (solo para navegación)
    private val _verificationState = MutableStateFlow(VerificationCredencialState())
    val verificationState = _verificationState.asStateFlow()

    // StateFlow para la lista de negocios
    private val _negociosState = MutableStateFlow(NegociosState())
    val negociosState = _negociosState.asStateFlow()

    // StateFlow para verificar si ya se comprobó la credencial
    private val _credencialChecked = MutableStateFlow(false)
    val credencialChecked = _credencialChecked.asStateFlow()

    // StateFlow para la recuperación de contraseña
    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordState())
    val forgotPasswordState = _forgotPasswordState.asStateFlow()

    // StateFlow para la creación de credencial
    private val _createCredentialState = MutableStateFlow(CreateCredentialState())
    val createCredentialState = _createCredentialState.asStateFlow()

    private val _ofertasState = MutableStateFlow(OfertasState())
    val ofertasState = _ofertasState.asStateFlow()

    // StateFlow para ofertas de un negocio específico
    private val _ofertasNegocioState = MutableStateFlow(OfertasNegocioState())
    val ofertasNegocioState = _ofertasNegocioState.asStateFlow()


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
    fun loadNextPageOfOfertas() {
        viewModelScope.launch {
            val currentState = _ofertasState.value

            if (currentState.isLoadingInitial || currentState.isLoadingMore || currentState.endReached) return@launch

            val isInitialLoad = currentState.ofertas.isEmpty()
            if (isInitialLoad) {
                _ofertasState.update { it.copy(isLoadingInitial = true, error = null) }
            } else {
                _ofertasState.update { it.copy(isLoadingMore = true, error = null) }
            }

            try {
                val response = api.getOfertas(cursor = currentState.nextCursor)
                _ofertasState.update {
                    it.copy(
                        isLoadingInitial = false,
                        isLoadingMore = false,
                        ofertas = it.ofertas + response.items,
                        nextCursor = response.nextCursor,
                        endReached = response.nextCursor == null
                    )
                }
            } catch (e: Exception) {
                _ofertasState.update {
                    it.copy(
                        isLoadingInitial = false,
                        isLoadingMore = false,
                        error = "Error al cargar ofertas: ${e.message}"
                    )
                }
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
        resetCredencialCheck()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(TOKEN_WEB)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso).signOut()
    }

    fun createAccount(
        nombre: String,
        curp: String,
        fechaNacimiento: String,
        entidadRegistro: String
    ) {
        viewModelScope.launch {
            // Resetear estado y mostrar loading
            _createCredentialState.update {
                CreateCredentialState(isLoading = true)
            }

            try {
                val email = auth.currentUser?.email

                if(email == null){
                    _createCredentialState.update {
                        it.copy(
                            isLoading = false,
                            errorTitle = "Error de Autenticación",
                            errorMessage = "No se pudo obtener el correo electrónico. Por favor, inicia sesión nuevamente."
                        )
                    }
                    return@launch
                }

                if(nombre.isBlank()){
                    _createCredentialState.update {
                        it.copy(
                            isLoading = false,
                            errorTitle = "Campo Requerido",
                            errorMessage = "El nombre completo es obligatorio."
                        )
                    }
                    return@launch
                }

                val body = CreateAccountRequest(
                    curp = curp,
                    nacimiento = fechaNacimiento,
                    entidadRegistro = entidadRegistro,
                    correo = email,
                    nombre = nombre.trim()
                )

                val response = api.createAccount(body)

                // ✅ Éxito: 201 Created
                _createCredentialState.update {
                    it.copy(
                        isLoading = false,
                        success = true,
                        errorTitle = null,
                        errorMessage = null
                    )
                }

            } catch (e: retrofit2.HttpException) {
                // Manejar errores HTTP de la API
                handleApiError(e)
            } catch (e: java.net.UnknownHostException) {
                _createCredentialState.update {
                    it.copy(
                        isLoading = false,
                        errorTitle = "Sin Conexión",
                        errorMessage = "No hay conexión a Internet. Por favor, verifica tu conexión y vuelve a intentar.",
                        canRetry = true  // Puede reintentar
                    )
                }
            } catch (e: java.net.SocketTimeoutException) {
                _createCredentialState.update {
                    it.copy(
                        isLoading = false,
                        errorTitle = "Tiempo de Espera Agotado",
                        errorMessage = "La verificación está tardando más de lo normal. Por favor, intenta nuevamente mas tarde.",
                        canRetry = true  // Puede reintentar
                    )
                }
            } catch (e: Exception) {
                _createCredentialState.update {
                    it.copy(
                        isLoading = false,
                        errorTitle = "Error Inesperado",
                        errorMessage = "Ocurrió un error inesperado. Por favor, intenta nuevamente.",
                        canRetry = false
                    )
                }
            }
        }
    }

    private fun handleApiError(exception: retrofit2.HttpException) {
        try {
            val errorBody = exception.response()?.errorBody()?.string()
            val gson = com.google.gson.Gson()
            val apiError = gson.fromJson(errorBody, mx.aro.atizaapp_equipo1.model.ApiErrorResponse::class.java)

            val statusCode = exception.code()

            when (statusCode) {
                400 -> {
                    // Bad Request - Parámetros inválidos o Email requerido
                    if (apiError.error.contains("Email requerido", ignoreCase = true)) {
                        _createCredentialState.update {
                            it.copy(
                                isLoading = false,
                                errorTitle = "Email Requerido",
                                errorMessage = "Debe proporcionar un email válido para continuar."
                            )
                        }
                    } else {
                        _createCredentialState.update {
                            it.copy(
                                isLoading = false,
                                errorTitle = "Datos Inválidos",
                                errorMessage = "Por favor, verifica que todos los campos estén correctamente llenados."
                            )
                        }
                    }
                }

                409 -> {
                    // Conflict - Duplicados
                    val message = when {
                        apiError.error.contains("CURP y correo ya registrados", ignoreCase = true) ->
                            "Tu CURP y correo electrónico ya están registrados en el sistema."
                        apiError.error.contains("CURP ya registrada", ignoreCase = true) ->
                            "Esta CURP ya está asociada a una cuenta existente."
                        apiError.error.contains("Correo ya registrado", ignoreCase = true) ->
                            "Este correo electrónico ya está asociado a una cuenta existente."
                        else ->
                            "Ya existe una cuenta con estos datos."
                    }

                    _createCredentialState.update {
                        it.copy(
                            isLoading = false,
                            errorTitle = "Registro Duplicado",
                            errorMessage = message
                        )
                    }
                }

                422 -> {
                    // Unprocessable Entity - Errores de validación con VerificaMex
                    val (title, message) = when {
                        apiError.error.contains("CURP no verificado", ignoreCase = true) ->
                            "CURP No Verificada" to "No se encontraron registros válidos para la CURP proporcionada en el sistema oficial."

                        apiError.error.contains("CURP no coincide", ignoreCase = true) ->
                            "Datos No Coinciden" to "Los datos proporcionados no coinciden con los registros oficiales de CURP. Por favor, verifica la información ingresada."

                        apiError.error.contains("Formato de fecha inválido", ignoreCase = true) ->
                            "Datos No Coinciden" to "Los datos proporcionados no coinciden con los registros oficiales de CURP. Por favor, verifica la información ingresada."

                        apiError.error.contains("Fecha de nacimiento no coincide", ignoreCase = true) ->
                            "Datos No Coinciden" to "Los datos proporcionados no coinciden con los registros oficiales de CURP. Por favor, verifica la información ingresada.\n\nSi consideras que se trata de un error, por favor contáctanos."

                        apiError.error.contains("Entidad de registro no coincide", ignoreCase = true) ->
                            "Datos No Coinciden" to "Los datos proporcionados no coinciden con los registros oficiales de CURP. Por favor, verifica la información ingresada."

                        else ->
                            "Error de Validación" to "Los datos proporcionados no pasaron la validación oficial. Por favor, verifica la información ingresada."
                    }

                    _createCredentialState.update {
                        it.copy(
                            isLoading = false,
                            errorTitle = title,
                            errorMessage = message
                        )
                    }
                }

                502 -> {
                    // Bad Gateway - Error de VerificaMex
                    _createCredentialState.update {
                        it.copy(
                            isLoading = false,
                            errorTitle = "Servicio de Verificación No Disponible",
                            errorMessage = "No se pudo verificar la CURP con el proveedor oficial. Por favor, intenta nuevamente más tarde.",
                            canRetry = true  // Puede reintentar
                        )
                    }
                }

                503 -> {
                    // Service Unavailable - BD no disponible
                    _createCredentialState.update {
                        it.copy(
                            isLoading = false,
                            errorTitle = "Servicio Temporalmente No Disponible",
                            errorMessage = "El servicio no está disponible en este momento. Por favor, intenta nuevamente en unos momentos.",
                            canRetry = true  // Puede reintentar
                        )
                    }
                }

                504 -> {
                    // Gateway Timeout - VerificaMex no encontró la CURP
                    _createCredentialState.update {
                        it.copy(
                            isLoading = false,
                            errorTitle = "CURP No Encontrada",
                            errorMessage = "No se pudo verificar la CURP con el proveedor. Por favor, verifica tus datos e intenta nuevamente."
                        )
                    }
                }

                else -> {
                    // 500 o cualquier otro error
                    _createCredentialState.update {
                        it.copy(
                            isLoading = false,
                            errorTitle = "Error del Servidor",
                            errorMessage = "Ocurrió un error en el servidor. Por favor, intenta nuevamente más tarde."
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Si falla el parseo del error
            _createCredentialState.update {
                it.copy(
                    isLoading = false,
                    errorTitle = "Error de Comunicación",
                    errorMessage = "Ocurrió un error al procesar la solicitud. Por favor, intenta nuevamente."
                )
            }
        }
    }

    fun clearCreateCredentialState() {
        _createCredentialState.value = CreateCredentialState()
    }

    // Función para obtener datos COMPLETOS del usuario (para pantalla Mi Credencial)
    fun getMe() {
        viewModelScope.launch {
            _credencialState.update { it.copy(isLoading = true, error = null) }
            try {
                val email = auth.currentUser?.email

                if (email == null) {
                    throw Exception("Usuario no autenticado.")
                }
                val response = api.getMe(email)
                _credencialState.update {
                    it.copy(
                        isLoading = false,
                        usuario = response,
                        error = null
                    )
                }

                response?.id?.let { id ->
                    _idFormateado.value = formatearIdUsuario(id)
                }

            } catch (e: Exception) {
                Log.e("AppVM", "Error al obtener datos del usuario", e)
                _credencialState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al obtener los datos: ${e.message}",
                        usuario = null
                    )
                }
            }
        }
    }

    // Función para VERIFICAR EXISTENCIA de credencial (solo para navegación)
    private fun checkCredencialExists() {
        viewModelScope.launch {
            _verificationState.update { it.copy(isLoading = true, hasCredencial = false, error = null, isNetworkError = false) }
            try {
                val email = auth.currentUser?.email

                if (email == null) {
                    throw Exception("Usuario no autenticado.")
                }

                // Llamar a la API solo para verificar existencia
                val response = api.getMe(email)

                // Éxito: La credencial existe
                _verificationState.update {
                    it.copy(
                        isLoading = false,
                        hasCredencial = true,
                        error = null,
                        isNetworkError = false
                    )
                }
            } catch (e: Exception) {
                Log.e("AppVM", "Error al verificar credencial", e)

                // Detectar si es error de red o credencial no existe
                val isNetworkIssue = e is java.net.UnknownHostException ||
                        e is java.net.SocketTimeoutException ||
                        e is java.io.IOException ||
                        e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                        e.message?.contains("timeout", ignoreCase = true) == true ||
                        e.message?.contains("Failed to connect", ignoreCase = true) == true

                if (isNetworkIssue) {
                    // Error de red: asumir que tiene credencial (beneficio de la duda)
                    _verificationState.update {
                        it.copy(
                            isLoading = false,
                            hasCredencial = true, // Permitir acceso offline
                            error = "Sin conexión. Modo offline.",
                            isNetworkError = true
                        )
                    }
                } else {
                    // Error de API (404, etc.): no tiene credencial
                    _verificationState.update {
                        it.copy(
                            isLoading = false,
                            hasCredencial = false,
                            error = "Credencial no encontrada",
                            isNetworkError = false
                        )
                    }
                }
            } finally {
                _credencialChecked.value = true
            }
        }
    }

    // Función para verificar credencial al iniciar sesión
    //mecanismo de protección que garantiza que la aplicación siempre tenga un estado definido, incluso en situaciones inesperadas donde el usuario no está autenticado cuando
    fun verificarCredencial() {
        if (auth.currentUser != null) {
            checkCredencialExists()
        } else {
            _credencialChecked.value = true
            _verificationState.update {
                it.copy(
                    isLoading = false,
                    hasCredencial = false,
                    error = null,
                    isNetworkError = false
                )
            }
        }
    }

    // Función para resetear el estado de verificación
    fun resetCredencialCheck() {
        _credencialChecked.value = false
        _verificationState.update {
            VerificationCredencialState(
                isLoading = false,
                hasCredencial = false,
                error = null,
                isNetworkError = false
            )
        }
    }
    // Obtener un negocio por ID
    fun getNegocioById(id: Int, onSuccess: (Negocio) -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                val negocio = api.getNegocioById(id)
                onSuccess(negocio)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    // Cargar ofertas de un negocio específico
    fun loadOfertasByNegocio(negocioId: Int) {
        viewModelScope.launch {
            _ofertasNegocioState.update {
                it.copy(isLoading = true, error = null, negocioId = negocioId)
            }

            try {
                val response = api.getOfertasByNegocio(negocioId = negocioId)
                _ofertasNegocioState.update {
                    it.copy(
                        isLoading = false,
                        ofertas = response.items,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _ofertasNegocioState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar ofertas: ${e.message}",
                        ofertas = emptyList(),
                        negocioId = null
                    )
                }
            }
        }
    }

    // Limpiar las ofertas del negocio cuando se sale de la pantalla
    fun clearOfertasNegocio() {
        _ofertasNegocioState.value = OfertasNegocioState()
    }
    fun loadNextPageOfNegocios() {
        viewModelScope.launch {
            val currentState = _negociosState.value
            // Evita hacer llamadas si ya se está cargando o si se llegó al final
            if (currentState.isLoadingInitial || currentState.isLoadingMore || currentState.endReached) {
                return@launch
            }

            // Determina si es la carga inicial para mostrar el indicador de carga apropiado
            val isInitialLoad = currentState.negocios.isEmpty()
            if (isInitialLoad) {
                _negociosState.update { it.copy(isLoadingInitial = true, error = null) }
            } else {
                _negociosState.update { it.copy(isLoadingMore = true, error = null) }
            }

            try {
                // NOTA: Asegúrate de que tu ApiService.getNegocios() acepte un cursor (String?)
                // y devuelva un objeto NegociosApiResponse(val negocios: List<Negocio>, val nextCursor: String?).
                val response = api.getNegocios(cursor = currentState.nextCursor)
                println("Negocios recibidos de la API: ${response.items}")

                _negociosState.update {
                    it.copy(
                        isLoadingInitial = false,
                        isLoadingMore = false,
                        // Añade los nuevos negocios a la lista existente
                        negocios = it.negocios + response.items,
                        // Actualiza el cursor para la siguiente llamada
                        nextCursor = response.nextCursor,
                        // Si el nuevo cursor es nulo, hemos llegado al final
                        endReached = response.nextCursor == null
                    )
                }
            } catch (e: Exception) {
                Log.e("AppVM", "Error al cargar negocios", e)
                _negociosState.update {
                    it.copy(
                        isLoadingInitial = false,
                        isLoadingMore = false,
                        error = "Error al cargar los negocios: ${e.message}"
                    )
                }
            }
        }
    }

    // ========== FUNCIONES DE RECUPERACIÓN DE CONTRASEÑA ==========

    // Función para actualizar el email en el estado de recuperación de contraseña
    fun onForgotPasswordEmailChange(email: String) {
        _forgotPasswordState.update { it.copy(email = email, error = null) }
    }

    // Función para enviar el correo de restablecimiento de contraseña
    fun sendPasswordResetEmail() {
        val email = _forgotPasswordState.value.email.trim()

        // Validar formato de email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _forgotPasswordState.update { it.copy(error = "Ingresa un correo válido") }
            return
        }

        _forgotPasswordState.update { it.copy(isLoading = true, error = null, sent = false) }

        // Configurar el idioma de Firebase al español
        auth.setLanguageCode("es")

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _forgotPasswordState.update { it.copy(isLoading = false, sent = true) }
            }
            .addOnFailureListener { e ->
                // Mapeo de errores seguro (no enumera usuarios existentes por seguridad)
                val code = (e as? FirebaseAuthException)?.errorCode
                val errorMessage = when (code) {
                    "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido."
                    "ERROR_OPERATION_NOT_ALLOWED" ->
                        "El inicio de sesión por correo está deshabilitado. Contacta soporte."
                    else -> "No pudimos procesar la solicitud. Inténtalo de nuevo en unos minutos."
                }
                _forgotPasswordState.update { it.copy(isLoading = false, error = errorMessage) }
            }
    }

    // Función para resetear el estado de recuperación de contraseña
    fun clearForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState()
    }
}
