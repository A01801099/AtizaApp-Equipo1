package mx.aro.atizaapp_equipo1.viewmodel

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
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
import mx.aro.atizaapp_equipo1.model.CredencialRepository
import mx.aro.atizaapp_equipo1.model.Negocio
import mx.aro.atizaapp_equipo1.model.NegociosApiResponse

import mx.aro.atizaapp_equipo1.model.TOKEN_WEB
import mx.aro.atizaapp_equipo1.model.Usuario
import androidx.compose.foundation.lazy.items
import androidx.room.util.copy
import mx.aro.atizaapp_equipo1.model.Oferta
import mx.aro.atizaapp_equipo1.view.screens.formatearIdUsuario
import mx.aro.atizaapp_equipo1.view.screens.formatearIdUsuario
import mx.aro.atizaapp_equipo1.utils.NetworkUtils

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

    // Repository para persistencia local de credencial
    private lateinit var credencialRepository: CredencialRepository

    // Contexto de aplicación guardado para verificaciones de red
    private lateinit var applicationContext: Context

    /**
     * Inicializar el ViewModel con el contexto de la aplicación
     * DEBE llamarse desde MainActivity.onCreate()
     */
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        credencialRepository = CredencialRepository(applicationContext)

        // PRIMERO: Verificar estado inicial de red de forma síncrona
        val initialNetworkState = NetworkUtils.isNetworkAvailable(applicationContext)
        _isNetworkAvailable.value = initialNetworkState
        Log.d("AppVM", "🔍 Estado inicial de red (sync): ${if (initialNetworkState) "Conectado" else "Desconectado"}")

        // SEGUNDO: Observar cambios en la conectividad de red
        viewModelScope.launch {
            NetworkUtils.observeNetworkConnectivity(applicationContext).collect { isConnected ->
                val previousState = _isNetworkAvailable.value
                _isNetworkAvailable.value = isConnected

                Log.d("AppVM", "📶 Cambio de red detectado: $previousState → $isConnected")

                // Si se recupera la conexión, intentar recargar usuario de Firebase
                if (!previousState && isConnected) {
                    Log.d("AppVM", "✅ Conexión recuperada - Intentando reload de Firebase")
                    reloadFirebaseUser()
                }
            }
        }

        // TERCERO: Solo hacer reload inicial si hay conexión confirmada
        if (initialNetworkState) {
            Log.d("AppVM", "🌐 Hay conexión - Programando reload de Firebase")
            reloadFirebaseUser()
        } else {
            Log.d("AppVM", "📵 Sin conexión inicial - Omitiendo reload de Firebase")
        }
    }

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

    // StateFlow para el estado de conectividad de red
    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable = _isNetworkAvailable.asStateFlow()


    init {
        // Establecer estado inicial del usuario SIN recargar de Firebase
        // El reload se hará después de initialize() cuando sepamos el estado de red
        _estaLoggeado.value = auth.currentUser != null &&
                (auth.currentUser?.isEmailVerified == true ||
                        auth.currentUser?.providerData?.any { it.providerId == "google.com" } == true)

        Log.d("AppVM", "AppVM inicializado - Usuario loggeado: ${_estaLoggeado.value}")
    }

    /**
     * Recargar estado del usuario de Firebase de forma segura
     * Solo se llama después de initialize() cuando conocemos el estado de red
     */
    private fun reloadFirebaseUser() {
        viewModelScope.launch {
            // Solo recargar si hay conexión
            if (_isNetworkAvailable.value && auth.currentUser != null) {
                try {
                    auth.currentUser?.reload()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _estaLoggeado.value = auth.currentUser != null &&
                                    (auth.currentUser?.isEmailVerified == true ||
                                            auth.currentUser?.providerData?.any { it.providerId == "google.com" } == true)
                            Log.d("AppVM", "✅ Usuario de Firebase recargado correctamente")
                        } else {
                            // Verificar si el error es por red
                            val exception = task.exception
                            if (exception is com.google.firebase.FirebaseNetworkException) {
                                Log.w("AppVM", "⚠️ Sin conexión al recargar usuario, usando estado local")
                            } else {
                                Log.w("AppVM", "⚠️ No se pudo recargar usuario: ${exception?.message}, usando estado local")
                            }
                        }
                    }?.addOnFailureListener { e ->
                        if (e is com.google.firebase.FirebaseNetworkException) {
                            Log.w("AppVM", "⚠️ Sin conexión de red al recargar usuario")
                        } else {
                            Log.e("AppVM", "❌ Error al recargar usuario: ${e.message}")
                        }
                    }
                } catch (e: com.google.firebase.FirebaseNetworkException) {
                    Log.w("AppVM", "⚠️ FirebaseNetworkException capturada: ${e.message}")
                } catch (e: Exception) {
                    Log.e("AppVM", "❌ Excepción al recargar usuario: ${e.message}")
                }
            } else {
                Log.d("AppVM", "📵 Sin conexión - Omitiendo reload de Firebase user")
            }
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

        // Limpiar caché local de credencial
        viewModelScope.launch {
            if (::credencialRepository.isInitialized) {
                credencialRepository.clearCredencial()
                Log.d("AppVM", "Caché de credencial limpiado en logout")
            }
        }

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

                // ✅ Éxito: 201 Created - Guardar en caché
                if (::credencialRepository.isInitialized) {
                    credencialRepository.saveCredencial(response.usuario)
                    Log.d("AppVM", "Credencial creada y guardada en caché")
                }

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

    /**
     * Sincronizar credencial con el servidor (online)
     * Guarda en caché si es exitoso
     */
    private suspend fun syncCredencial(): Boolean {
        return try {
            val email = auth.currentUser?.email ?: throw Exception("No autenticado")
            val usuario = api.getMe(email)

            // Guardar en caché
            if (::credencialRepository.isInitialized) {
                credencialRepository.saveCredencial(usuario)
            }

            // Actualizar estado
            _credencialState.update {
                it.copy(
                    isLoading = false,
                    usuario = usuario,
                    error = null
                )
            }

            // Actualizar ID formateado
            usuario.id.let { id ->
                _idFormateado.value = formatearIdUsuario(id)
            }

            true
        } catch (e: Exception) {
            Log.e("AppVM", "Error en sincronización de credencial", e)
            false
        }
    }

    // Función para obtener datos COMPLETOS del usuario (para pantalla Mi Credencial)
    // Modificada para priorizar caché local y sincronizar en background
    fun getMe() {
        viewModelScope.launch {
            _credencialState.update { it.copy(isLoading = true, error = null) }

            try {
                // Verificar que el repository esté inicializado
                if (!::credencialRepository.isInitialized) {
                    throw Exception("Repository no inicializado. Llamar initialize() primero.")
                }

                // 1. PRIMERO intentar cargar del caché (rápido, < 20ms)
                val cached = credencialRepository.getCredencial()

                if (cached != null) {
                    // Caché encontrado → mostrar inmediatamente
                    val ageInfo = credencialRepository.formatTimestamp(cached.timestampMs)

                    _credencialState.update {
                        it.copy(
                            isLoading = false,
                            usuario = cached.usuario,
                            error = if (credencialRepository.isCacheStale(hours = 1)) {
                                "Sincronizando..."
                            } else null
                        )
                    }

                    // Actualizar ID formateado
                    cached.usuario.id.let { id ->
                        _idFormateado.value = formatearIdUsuario(id)
                    }

                    Log.d("AppVM", "Credencial cargada desde caché: $ageInfo")
                }

                // 2. Sincronizar en background si:
                //    - No hay caché, o
                //    - El caché tiene más de 24 horas
                if (cached == null || credencialRepository.isCacheStale(hours = 24)) {
                    Log.d("AppVM", "Iniciando sincronización en background...")

                    val syncSuccess = syncCredencial()

                    if (!syncSuccess && cached == null) {
                        // Si falla sincronización Y no hay caché → mostrar error
                        _credencialState.update {
                            it.copy(
                                isLoading = false,
                                error = "No se pudo obtener la credencial. Verifica tu conexión.",
                                usuario = null
                            )
                        }
                    } else if (!syncSuccess && cached != null) {
                        // Si falla sincronización PERO hay caché → mostrar advertencia
                        _credencialState.update {
                            it.copy(
                                error = "Modo offline - ${credencialRepository.formatTimestamp(cached.timestampMs)}"
                            )
                        }
                    }
                } else {
                    // Caché reciente, no sincronizar
                    Log.d("AppVM", "Caché reciente, omitiendo sincronización")
                }

            } catch (e: Exception) {
                Log.e("AppVM", "Error al obtener datos del usuario", e)
                _credencialState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}",
                        usuario = null
                    )
                }
            }
        }
    }

    // Función para VERIFICAR EXISTENCIA de credencial (solo para navegación)
    // Optimizada para ser 100% cache-first y evitar errores de red al iniciar
    private fun checkCredencialExists() {
        viewModelScope.launch {
            _verificationState.update {
                it.copy(isLoading = true, hasCredencial = false, error = null, isNetworkError = false)
            }

            try {
                // Verificar que el repository esté inicializado
                if (!::credencialRepository.isInitialized) {
                    Log.e("AppVM", "❌ Repository no inicializado - llamar initialize() primero")
                    throw Exception("Repository no inicializado")
                }

                // 1. PRIMERO verificar caché local (instantáneo, < 20ms)
                val hasLocalCredencial = credencialRepository.hasValidCredencial()

                if (hasLocalCredencial) {
                    // ✅ Credencial válida en caché → ACCESO INMEDIATO (sin esperar API)
                    Log.d("AppVM", "✅ Credencial encontrada en caché - Acceso concedido")

                    _verificationState.update {
                        it.copy(
                            isLoading = false,
                            hasCredencial = true,
                            error = null,
                            isNetworkError = false
                        )
                    }

                    // Sincronizar en background SOLO si hay conexión (sin bloquear navegación)
                    if (_isNetworkAvailable.value) {
                        viewModelScope.launch {
                            try {
                                val email = auth.currentUser?.email ?: return@launch
                                val usuario = api.getMe(email)
                                credencialRepository.saveCredencial(usuario)
                                Log.d("AppVM", "✅ Credencial sincronizada en background")
                            } catch (e: Exception) {
                                Log.w("AppVM", "⚠️ Sincronización en background falló (continuará offline)", e)
                                // No hacer nada - el usuario ya tiene acceso
                            }
                        }
                    } else {
                        Log.d("AppVM", "📵 Sin conexión - Omitiendo sincronización background")
                    }

                } else {
                    // ⚠️ No hay caché válido → REQUERIDO verificar con servidor
                    Log.d("AppVM", "⚠️ Sin caché válido - Verificando conexión...")

                    // DOBLE VERIFICACIÓN: StateFlow + Verificación directa del sistema
                    val isNetworkAvailableFlow = _isNetworkAvailable.value
                    val isNetworkAvailableDirect = if (::applicationContext.isInitialized) {
                        NetworkUtils.isNetworkAvailable(applicationContext)
                    } else {
                        false
                    }

                    Log.d("AppVM", "   Flow dice: $isNetworkAvailableFlow")
                    Log.d("AppVM", "   Sistema dice: $isNetworkAvailableDirect")

                    // Si CUALQUIERA de las dos dice que no hay red, lanzar excepción
                    if (!isNetworkAvailableFlow && !isNetworkAvailableDirect) {
                        Log.e("AppVM", "🔴 Sin conexión detectada - Lanzando excepción de red")
                        throw java.net.UnknownHostException("Sin conexión a Internet")
                    }

                    Log.d("AppVM", "🌐 Conexión disponible - Consultando servidor...")
                    val email = auth.currentUser?.email
                        ?: throw Exception("Usuario no autenticado")

                    val response = api.getMe(email)

                    // Guardar en caché para futuros accesos offline
                    credencialRepository.saveCredencial(response)
                    Log.d("AppVM", "✅ Credencial obtenida del servidor y guardada en caché")

                    _verificationState.update {
                        it.copy(
                            isLoading = false,
                            hasCredencial = true,
                            error = null,
                            isNetworkError = false
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e("AppVM", "❌ Error al verificar credencial", e)
                Log.e("AppVM", "   Tipo: ${e.javaClass.simpleName}")
                Log.e("AppVM", "   Mensaje: ${e.message}")

                // Detectar si es error de red
                val isNetworkIssue = e is java.net.UnknownHostException ||
                        e is java.net.SocketTimeoutException ||
                        e is java.io.IOException ||
                        e is javax.net.ssl.SSLException ||
                        e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                        e.message?.contains("timeout", ignoreCase = true) == true ||
                        e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                        e.message?.contains("Sin conexión", ignoreCase = true) == true

                if (isNetworkIssue) {
                    // 🔴 Error de red + Sin caché válido = Mostrar pantalla de error
                    Log.e("AppVM", "🔴 Error de red confirmado - Mostrando NetworkErrorScreen")

                    _verificationState.update {
                        it.copy(
                            isLoading = false,
                            hasCredencial = false,
                            error = "Sin conexión. Necesitas Internet para acceder por primera vez.",
                            isNetworkError = true
                        )
                    }
                } else {
                    // ❌ Error de API (404, 401, etc.) = Credencial no existe en servidor
                    Log.e("AppVM", "❌ Error de API - Credencial no encontrada")

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
                Log.d("AppVM", "✅ Verificación completada - credencialChecked = true")
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

    /**
     * Configura el modo offline al iniciar la app sin conexión
     * Permite al usuario acceder a la app usando datos en caché
     */
    fun setOfflineMode() {
        _credencialChecked.value = true
        _verificationState.update {
            it.copy(
                isLoading = false,
                hasCredencial = true,  // Asumir que tiene credencial (beneficio de la duda)
                error = "Sin conexión. Usando datos locales.",
                isNetworkError = true
            )
        }
        Log.d("AppVM", "🔌 Modo offline activado - Usuario puede acceder con datos en caché")
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
