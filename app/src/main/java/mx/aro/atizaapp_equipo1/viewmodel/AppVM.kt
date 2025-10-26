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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.aro.atizaapp_equipo1.model.apiClientService.ApiClient
import mx.aro.atizaapp_equipo1.model.data_classes.ApiErrorResponse
import mx.aro.atizaapp_equipo1.model.data_classes.AuthState
import mx.aro.atizaapp_equipo1.model.data_classes.CreateAccountRequest
import mx.aro.atizaapp_equipo1.model.data_classes.CreateCredentialState
import mx.aro.atizaapp_equipo1.model.apiClientService.CredencialRepository
import mx.aro.atizaapp_equipo1.model.data_classes.CredencialState
import mx.aro.atizaapp_equipo1.model.data_classes.ForgotPasswordState
import mx.aro.atizaapp_equipo1.model.data_classes.Negocio

import mx.aro.atizaapp_equipo1.model.apiClientService.TOKEN_WEB
import mx.aro.atizaapp_equipo1.model.repository.NegociosRepository
import mx.aro.atizaapp_equipo1.model.data_classes.NegociosState
import mx.aro.atizaapp_equipo1.model.data_classes.Oferta
import mx.aro.atizaapp_equipo1.model.data_classes.OfertasNegocioState
import mx.aro.atizaapp_equipo1.model.repository.OfertasRepository
import mx.aro.atizaapp_equipo1.model.data_classes.OfertasState
import mx.aro.atizaapp_equipo1.model.data_classes.VerificationCredencialState
import mx.aro.atizaapp_equipo1.utils.formatUserId
import mx.aro.atizaapp_equipo1.utils.NetworkUtils

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
        negociosRepository = NegociosRepository(applicationContext)
        ofertasRepository = OfertasRepository(applicationContext)
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

    private lateinit var negociosRepository: NegociosRepository

    /**
     * ViewModel principal encargado de inicializar el estado de autenticación del usuario
     * y configurar los repositorios necesarios para la app.
     *
     * Esta sección establece el valor inicial de [_estaLoggeado] sin recargar los datos desde Firebase.
     * El proceso de recarga se realiza posteriormente en `initialize()` cuando se confirma la conexión de red.
     */
    private lateinit var ofertasRepository: OfertasRepository

    init {
        // Establece el estado inicial del usuario SIN recargar desde Firebase.
        // El reload se ejecutará en initialize(), una vez conocido el estado de red.
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
                            Log.w("AppVM", "️ Sin conexión de red al recargar usuario")
                        } else {
                            Log.e("AppVM", " Error al recargar usuario: ${e.message}")
                        }
                    }
                } catch (e: com.google.firebase.FirebaseNetworkException) {
                    Log.w("AppVM", "️ FirebaseNetworkException capturada: ${e.message}")
                } catch (e: Exception) {
                    Log.e("AppVM", " Excepción al recargar usuario: ${e.message}")
                }
            } else {
                Log.d("AppVM", " Sin conexión - Omitiendo reload de Firebase user")
            }
        }
    }

    /**
     * Restablece el estado de autenticación a su valor inicial.
     *
     * Esta función limpia cualquier mensaje, estado de carga o error previo
     * en el objeto [_authState], devolviéndolo a su forma predeterminada (`AuthState()`).
     */
    fun clearAuthState() {
        _authState.value = AuthState()
    }

    /**
     * Inicia sesión con una cuenta de Google utilizando las credenciales proporcionadas.
     *
     * Durante el proceso, la función actualiza el estado de autenticación para reflejar
     * el progreso (por ejemplo, `isLoading = true`) y los resultados del intento de inicio de sesión.
     *
     * @param credencial Objeto [AuthCredential] obtenido tras la autenticación de Google.
     *
     * Si el inicio de sesión es exitoso:
     * - Se actualiza [_estaLoggeado] a `true`.
     *
     * Si falla:
     * - Se establece [_estaLoggeado] en `false`.
     * - Se muestra un mensaje de error en [_authState.generalMessage].
     */
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

    /**
     * Carga **todas las ofertas disponibles** desde la API o, en caso de no tener conexión,
     * desde la caché local, y actualiza el estado global de ofertas.
     *
     * Esta función no implementa paginación: descarga todas las páginas de resultados
     * en una sola llamada secuencial (bucle) hasta que no haya más datos.
     *
     * Comportamiento:
     * - Evita llamadas duplicadas si ya hay datos cargados o si una carga inicial está en curso.
     * - Si no hay conexión y existe un repositorio inicializado, se cargan las ofertas en caché.
     * - Si hay conexión, descarga todas las páginas desde la API, las guarda en caché y
     *   actualiza el estado con la lista completa.
     *
     * @throws Exception Si ocurre un error al comunicarse con la API o al guardar en caché.
     *
     * Estados modificados:
     * - `_ofertasState`: Actualiza indicadores de carga, lista de ofertas, cursor y errores.
     * - `ofertasRepository`: Guarda los resultados localmente si está inicializado.
     *
     * Logs:
     * - Muestra información de progreso y errores mediante `Log.d` y `Log.e`.
     */
    fun loadAllOfertas() {
        viewModelScope.launch {
            val currentState = _ofertasState.value

            // Evitar cargas duplicadas
            if (currentState.isLoadingInitial || currentState.ofertas.isNotEmpty()) return@launch

            _ofertasState.update { it.copy(isLoadingInitial = true, error = null) }

            // Revisar conectividad
            val online = _isNetworkAvailable.value

            if (!online && ::ofertasRepository.isInitialized) {
                // 📥 Sin internet: cargar desde caché
                val cached = ofertasRepository.getOfertas()
                if (!cached.isNullOrEmpty()) {
                    _ofertasState.update {
                        it.copy(
                            isLoadingInitial = false,
                            ofertas = cached,
                            nextCursor = null,
                            endReached = true,
                            error = "Modo offline: mostrando ofertas en caché"
                        )
                    }
                    return@launch
                }
            }

            // 🌐 Con internet: cargar TODAS las páginas
            try {
                val allOfertas = mutableListOf<Oferta>()
                var nextCursor: String? = null
                var endReached = false

                // Cargar todas las páginas en un loop
                while (!endReached) {
                    val response = api.getOfertas(cursor = nextCursor)
                    allOfertas.addAll(response.items)

                    nextCursor = response.nextCursor
                    endReached = nextCursor == null

                    Log.d("AppVM", "📥 Cargando ofertas: ${allOfertas.size} acumuladas...")
                }

                // Guardar en caché TODAS las ofertas
                if (::ofertasRepository.isInitialized) {
                    ofertasRepository.saveOfertas(allOfertas)
                }
                Log.d("AppVM", "✅ Carga completa: ${allOfertas.size} ofertas")

                _ofertasState.update {
                    it.copy(
                        isLoadingInitial = false,
                        ofertas = allOfertas,
                        nextCursor = null,
                        endReached = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("AppVM", "❌ Error al cargar ofertas: ${e.message}")
                _ofertasState.update {
                    it.copy(
                        isLoadingInitial = false,
                        error = "Error al cargar ofertas: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Limpia todas las ofertas almacenadas en memoria y en caché local.
     *
     * Esta función reinicia el estado de [_ofertasState] a su valor inicial (`OfertasState()`)
     * y, si el repositorio de ofertas está inicializado, elimina también los datos guardados
     * en caché mediante [ofertasRepository.clearCache].
     *
     * Uso típico: al cerrar sesión o al refrescar completamente los datos del módulo de ofertas.
     */
    fun clearOfertas() {
        _ofertasState.value = OfertasState()
        if (::ofertasRepository.isInitialized) ofertasRepository.clearCache()
    }

    /**
     * Inicia sesión con correo electrónico y contraseña utilizando Firebase Authentication.
     *
     * La función valida los campos de entrada antes de intentar autenticar al usuario.
     * Si los campos están vacíos, actualiza [_authState] con los errores correspondientes.
     *
     * Durante el inicio de sesión:
     * - Se muestra un estado de carga (`isLoading = true`).
     * - Si el usuario se autentica correctamente **y** su correo está verificado,
     *   se actualiza [_estaLoggeado] a `true`.
     * - Si el correo no está verificado, se cierra la sesión y se muestra un mensaje de advertencia.
     * - En caso de error, se actualizan los mensajes de error apropiados en [_authState].
     *
     * @param email Correo electrónico del usuario.
     * @param pass Contraseña asociada al correo electrónico.
     *
     * Posibles errores manejados:
     * - [FirebaseAuthInvalidUserException]: El correo no está registrado.
     * - [FirebaseAuthInvalidCredentialsException]: Contraseña incorrecta.
     * - Otros errores de autenticación: mensaje general de error.
     *
     * Estados modificados:
     * - `_authState`: Actualiza errores, carga y mensajes globales.
     * - `_estaLoggeado`: Indica si el usuario ha iniciado sesión correctamente.
     */
    fun hacerLoginEmailPassword(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState(
                emailError = if (email.isBlank()) "El correo no puede estar vacío" else null,
                passwordError = if (pass.isBlank()) "La contraseña no puede estar vacía" else null
            )
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
                            _authState.update {
                                it.copy(generalMessage = "Por favor, verifica tu correo antes de iniciar sesión.")
                            }
                            _estaLoggeado.value = false
                        }
                    } else {
                        val exception = task.exception
                        val newState = when (exception) {
                            is FirebaseAuthInvalidUserException ->
                                AuthState(emailError = "Correo no registrado.")
                            is FirebaseAuthInvalidCredentialsException ->
                                AuthState(passwordError = "Contraseña incorrecta.")
                            else ->
                                AuthState(generalMessage = "Error: ${exception?.localizedMessage}")
                        }
                        _authState.value = newState
                        _estaLoggeado.value = false
                    }

                    if (_authState.value.generalMessage == null) {
                        _authState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    /**
     * Registra un nuevo usuario con correo y contraseña mediante Firebase Authentication.
     *
     * Esta función valida los campos antes del registro:
     * - Si el correo o la contraseña están vacíos, se muestran los errores correspondientes en [_authState].
     * - Si ambos campos son válidos, se intenta crear la cuenta.
     *
     * Durante el registro:
     * - Se muestra un indicador de carga (`isLoading = true`).
     * - Si el registro es exitoso:
     *   - Se envía un correo de verificación al nuevo usuario.
     *   - Se cierra la sesión automáticamente para obligar a la verificación del correo.
     *   - Se actualiza [_authState] indicando que el registro fue exitoso y que debe verificarse el correo.
     * - Si el registro falla, se actualiza [_authState] con el error específico.
     *
     * @param email Correo electrónico con el que se registrará el usuario.
     * @param pass Contraseña elegida por el usuario (mínimo 6 caracteres).
     *
     * Posibles errores manejados:
     * - [FirebaseAuthWeakPasswordException]: Contraseña demasiado débil.
     * - [FirebaseAuthUserCollisionException]: El correo ya está registrado.
     * - Otros errores: se muestra un mensaje general con la descripción.
     *
     * Estados modificados:
     * - `_authState`: actualiza errores, progreso y resultado del registro.
     *
     * @see hacerLoginEmailPassword Para el inicio de sesión con correo y contraseña.
     */
    fun hacerSignUp(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState(
                emailError = if (email.isBlank()) "El correo no puede estar vacío" else null,
                passwordError = if (pass.isBlank()) "La contraseña no puede estar vacía" else null
            )
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, emailError = null, passwordError = null) }
            auth.createUserWithEmailAndPassword(email.trim(), pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.user?.sendEmailVerification()
                        auth.signOut()
                        _authState.value = AuthState(
                            registrationComplete = true,
                            generalMessage = "¡Registro exitoso! Revisa tu correo para verificar la cuenta."
                        )
                    } else {
                        val exception = task.exception
                        val newState = when (exception) {
                            is FirebaseAuthWeakPasswordException ->
                                AuthState(passwordError = "La contraseña es muy débil (mín. 6 caracteres).")
                            is FirebaseAuthUserCollisionException ->
                                AuthState(emailError = "Este correo ya está registrado.")
                            else ->
                                AuthState(generalMessage = "Error en el registro: ${exception?.localizedMessage}")
                        }
                        _authState.value = newState
                    }

                    if (_authState.value.generalMessage == null) {
                        _authState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    /**
     * Cierra la sesión del usuario actual y limpia los datos de autenticación locales.
     *
     * Esta función:
     * - Cierra la sesión de Firebase mediante [auth.signOut].
     * - Restablece el estado de autenticación y credenciales en memoria.
     * - Elimina la caché local de credenciales si el repositorio está inicializado.
     * - Cierra también la sesión de Google si el usuario inició con dicha cuenta.
     *
     * @param context Contexto actual, necesario para obtener el cliente de Google Sign-In.
     *
     * Estados modificados:
     * - `_estaLoggeado`: Se establece en `false` tras cerrar la sesión.
     *
     * Logs:
     * - Muestra mensajes de depuración al limpiar la caché de credenciales.
     *
     * @see hacerLoginGoogle Para el inicio de sesión mediante cuenta de Google.
     */
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


    /**
     * Crea una cuenta de usuario en el sistema usando los datos personales proporcionados.
     *
     * Esta función envía una solicitud al endpoint remoto para registrar una nueva credencial
     * de usuario asociada al correo electrónico actualmente autenticado en Firebase.
     *
     * Flujo general:
     * 1. Reinicia el estado de [_createCredentialState] mostrando un indicador de carga.
     * 2. Valida que el usuario autenticado tenga correo electrónico y que el nombre no esté vacío.
     * 3. Construye un objeto [CreateAccountRequest] con los datos proporcionados.
     * 4. Llama al servicio remoto `api.createAccount()` para registrar la cuenta.
     * 5. Si la respuesta es exitosa, guarda la credencial localmente mediante [credencialRepository].
     * 6. Actualiza el estado con `success = true` al finalizar correctamente.
     *
     * En caso de error:
     * - [retrofit2.HttpException]: se delega a `handleApiError()` para manejo centralizado.
     * - [java.net.UnknownHostException]: error de conexión (sin Internet).
     * - [java.net.SocketTimeoutException]: tiempo de espera agotado.
     * - [Exception]: error inesperado no controlado.
     *
     * @param nombre Nombre completo del usuario.
     * @param curp CURP (Clave Única de Registro de Población) del usuario.
     * @param fechaNacimiento Fecha de nacimiento del usuario en formato `YYYY-MM-DD`.
     * @param entidadRegistro Entidad federativa donde se realizó el registro.
     *
     * Estados modificados:
     * - `_createCredentialState`: indica progreso, errores, o éxito del proceso.
     * - `credencialRepository`: guarda localmente la credencial creada (si está inicializado).
     *
     * Logs:
     * - Registra en consola la creación y almacenamiento de la credencial con `Log.d`.
     *
     * @throws retrofit2.HttpException Si la API devuelve un error HTTP (400–500).
     * @throws java.net.UnknownHostException Si no hay conexión a Internet.
     * @throws java.net.SocketTimeoutException Si la solicitud excede el tiempo máximo.
     */
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

                if (email == null) {
                    _createCredentialState.update {
                        it.copy(
                            isLoading = false,
                            errorTitle = "Error de Autenticación",
                            errorMessage = "No se pudo obtener el correo electrónico. Por favor, inicia sesión nuevamente."
                        )
                    }
                    return@launch
                }

                if (nombre.isBlank()) {
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
                        errorMessage = "La verificación está tardando más de lo normal. Por favor, intenta nuevamente más tarde.",
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

    /**
     * Maneja los errores HTTP provenientes de la API al crear una cuenta o verificar datos del usuario.
     *
     * Esta función centraliza la interpretación de errores devueltos por Retrofit y actualiza
     * el estado de la UI a través de [_createCredentialState], mostrando mensajes amigables y
     * personalizados según el tipo de error.
     *
     * Flujo general:
     * 1. Intenta parsear el cuerpo de error (`errorBody`) de la respuesta HTTP usando [Gson].
     * 2. Evalúa el código de estado HTTP y determina la causa del fallo:
     *    - **400 (Bad Request):** parámetros inválidos o email faltante.
     *    - **409 (Conflict):** datos duplicados (CURP o correo ya registrados).
     *    - **422 (Unprocessable Entity):** errores de validación con el sistema VerificaMex.
     *    - **502 (Bad Gateway):** fallo del proveedor externo de verificación (VerificaMex).
     *    - **503 (Service Unavailable):** servicio temporalmente fuera de línea.
     *    - **504 (Gateway Timeout):** el servicio no encontró la CURP o tardó demasiado.
     *    - **Otros códigos (≥500):** errores genéricos del servidor.
     * 3. Si ocurre una excepción al procesar el cuerpo del error, se muestra un mensaje genérico de comunicación.
     *
     * Los mensajes y títulos mostrados en la UI son definidos según el tipo de error para mejorar
     * la experiencia del usuario y permitir reintentos en casos específicos.
     *
     * @param exception Excepción lanzada por Retrofit ([retrofit2.HttpException]) que contiene
     *                  la respuesta HTTP con el código de error y el cuerpo devuelto por la API.
     *
     * Estados modificados:
     * - `_createCredentialState`: se actualiza con información contextual (título, mensaje, retry).
     *
     * Casos principales:
     * - **Error 400:** Email faltante o datos inválidos.
     * - **Error 409:** Conflicto de registro (duplicado).
     * - **Error 422:** Validación oficial fallida (CURP o datos inconsistentes).
     * - **Error 502–504:** Problemas con los servicios externos o de red.
     * - **Error 500+:** Error interno del servidor.
     *
     * Ejemplo de uso:
     * ```
     * catch (e: retrofit2.HttpException) {
     *     handleApiError(e)
     * }
     * ```
     *
     * @see ApiErrorResponse para el modelo de error de la API.
     * @see createAccount para el flujo principal donde se usa este manejador.
     */
    private fun handleApiError(exception: retrofit2.HttpException) {
        try {
            val errorBody = exception.response()?.errorBody()?.string()
            val gson = com.google.gson.Gson()
            val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)

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

                        apiError.error.contains("CURP no coincide", ignoreCase = true) ||
                                apiError.error.contains("Formato de fecha inválido", ignoreCase = true) ||
                                apiError.error.contains("Fecha de nacimiento no coincide", ignoreCase = true) ||
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
                    _createCredentialState.update {
                        it.copy(
                            isLoading = false,
                            errorTitle = "Servicio de Verificación No Disponible",
                            errorMessage = "No se pudo verificar la CURP con el proveedor oficial. Por favor, intenta nuevamente más tarde.",
                            canRetry = true
                        )
                    }
                }

                503 -> {
                    _createCredentialState.update {
                        it.copy(
                            isLoading = false,
                            errorTitle = "Servicio Temporalmente No Disponible",
                            errorMessage = "El servicio no está disponible en este momento. Por favor, intenta nuevamente en unos momentos.",
                            canRetry = true
                        )
                    }
                }

                504 -> {
                    _createCredentialState.update {
                        it.copy(
                            isLoading = false,
                            errorTitle = "CURP No Encontrada",
                            errorMessage = "No se pudo verificar la CURP con el proveedor. Por favor, verifica tus datos e intenta nuevamente."
                        )
                    }
                }

                else -> {
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
                _idFormateado.value = formatUserId(id)
            }

            true
        } catch (e: Exception) {
            Log.e("AppVM", "Error en sincronización de credencial", e)
            false
        }
    }
    /**
     * Obtiene los datos completos del usuario para la pantalla "Mi Credencial".
     *
     * Flujo de ejecución:
     * 1. Intenta cargar la credencial desde la caché local de manera inmediata.
     *    - Si se encuentra, se muestra al usuario en <20ms.
     *    - Se actualiza el ID formateado (`_idFormateado`) para la UI.
     *    - Se muestra un mensaje de "Sincronizando..." si el caché tiene más de 1 hora.
     * 2. Sincroniza en segundo plano con el servidor si:
     *    - No hay datos en caché, o
     *    - La caché tiene más de 24 horas.
     * 3. Maneja resultados de la sincronización:
     *    - Si falla y no hay caché: muestra error y usuario = null.
     *    - Si falla pero hay caché: se muestra advertencia con la antigüedad del caché.
     *    - Si la caché está reciente: no se sincroniza.
     *
     * Requisitos:
     * - El repositorio de credenciales debe estar inicializado (`credencialRepository`).
     *
     * Manejo de errores:
     * - Captura excepciones generales y actualiza `_credencialState` con un mensaje de error.
     * - Registra errores en Logcat para depuración (`Log.e`).
     *
     * Estados modificados:
     * - `_credencialState`: indica progreso (`isLoading`), error (`error`) y datos del usuario (`usuario`).
     * - `_idFormateado`: contiene el ID de usuario formateado para la UI.
     *
     * Logs:
     * - Muestra mensajes sobre carga desde caché y sincronización.
     *
     * @see credencialRepository.getCredencial Para acceder a la credencial en caché.
     * @see syncCredencial Para sincronizar los datos con el servidor.
     */
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
                        _idFormateado.value = formatUserId(id)
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
    /**
     * Verifica la credencial del usuario al iniciar sesión.
     *
     * Esta función garantiza que la aplicación siempre tenga un estado definido,
     * incluso si el usuario no está autenticado en el momento de iniciar.
     *
     * Flujo de ejecución:
     * 1. Si el usuario está autenticado (`auth.currentUser != null`):
     *    - Llama a [checkCredencialExists] para verificar la existencia de la credencial
     *      usando el mecanismo cache-first con sincronización en background.
     * 2. Si no hay usuario autenticado:
     *    - Marca `_credencialChecked` como `true`.
     *    - Actualiza `_verificationState` indicando que no hay credencial,
     *      sin error ni problema de red.
     *
     * Estados modificados:
     * - `_credencialChecked`: indica que se completó la verificación.
     * - `_verificationState`: indica si hay credencial, si está cargando y posibles errores.
     *
     * @see checkCredencialExists Para el proceso detallado de verificación cache-first y sincronización.
     */
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

    /**
     * Resetea el estado de verificación de la credencial.
     *
     * Útil para escenarios donde se requiere reiniciar la verificación,
     * por ejemplo al hacer logout o cambiar de usuario.
     *
     * Flujo de ejecución:
     * 1. Marca `_credencialChecked` como `false`.
     * 2. Reinicia `_verificationState` con valores por defecto:
     *    - `isLoading = false`
     *    - `hasCredencial = false`
     *    - `error = null`
     *    - `isNetworkError = false`
     *
     * Estados modificados:
     * - `_credencialChecked`: vuelve a `false`.
     * - `_verificationState`: reinicia todos los campos al estado inicial.
     */
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
    /**
     * Obtiene un negocio específico por su ID.
     *
     * @param id ID del negocio a obtener.
     * @param onSuccess Callback que recibe el [Negocio] si la operación fue exitosa.
     * @param onError Callback que recibe la [Throwable] si ocurrió un error durante la consulta.
     *
     * Ejecuta la operación en un [viewModelScope.launch] para no bloquear la UI.
     */
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

    /**
     * Carga todas las ofertas de un negocio específico.
     *
     * Actualiza [_ofertasNegocioState] con los datos obtenidos de la API.
     * Maneja estados de carga y errores de manera reactiva.
     *
     * @param negocioId ID del negocio cuyas ofertas se van a cargar.
     *
     * Estados modificados:
     * - [_ofertasNegocioState.isLoading]: indica que se está cargando información.
     * - [_ofertasNegocioState.ofertas]: lista de ofertas obtenidas.
     * - [_ofertasNegocioState.error]: mensaje de error en caso de fallo.
     */
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

    /**
     * Limpia las ofertas de un negocio cuando se sale de la pantalla correspondiente.
     *
     * Restablece [_ofertasNegocioState] a su estado inicial.
     */
    fun clearOfertasNegocio() {
        _ofertasNegocioState.value = OfertasNegocioState()
    }

    /**
     * Carga todos los negocios de una sola vez (sin paginación).
     *
     * Flujo de ejecución:
     * 1. Evita cargas duplicadas si ya se está cargando o hay negocios cargados.
     * 2. Comprueba conectividad de red:
     *    - Si no hay conexión y hay caché disponible → carga desde caché.
     *    - Si hay conexión → carga todas las páginas desde la API.
     * 3. Guarda todos los negocios en caché tras la carga exitosa.
     * 4. Maneja errores y actualiza [_negociosState] con mensajes apropiados.
     *
     * Estados modificados:
     * - [_negociosState.isLoadingInitial]: indica que se está cargando información.
     * - [_negociosState.negocios]: lista de negocios obtenidos.
     * - [_negociosState.endReached]: indica si se cargaron todos los negocios.
     * - [_negociosState.error]: mensaje de error en caso de fallo.
     */
    fun loadAllNegocios() {
        viewModelScope.launch {
            val currentState = _negociosState.value

            if (currentState.isLoadingInitial || currentState.negocios.isNotEmpty()) return@launch

            _negociosState.update { it.copy(isLoadingInitial = true, error = null) }

            val online = _isNetworkAvailable.value

            if (!online && negociosRepository.hasCache()) {
                val cached = negociosRepository.getNegocios()
                _negociosState.update {
                    it.copy(
                        isLoadingInitial = false,
                        negocios = cached,
                        nextCursor = null,
                        endReached = true,
                        error = "Modo offline: mostrando datos en caché"
                    )
                }
                return@launch
            }

            try {
                val allNegocios = mutableListOf<Negocio>()
                var nextCursor: String? = null
                var endReached = false

                while (!endReached) {
                    val response = api.getNegocios(cursor = nextCursor)
                    allNegocios.addAll(response.items)

                    nextCursor = response.nextCursor
                    endReached = nextCursor == null

                    Log.d("AppVM", "📥 Cargando negocios: ${allNegocios.size} acumulados...")
                }

                negociosRepository.saveNegocios(allNegocios)
                Log.d("AppVM", "✅ Carga completa: ${allNegocios.size} negocios")

                _negociosState.update {
                    it.copy(
                        isLoadingInitial = false,
                        negocios = allNegocios,
                        nextCursor = null,
                        endReached = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("AppVM", "❌ Error al cargar negocios: ${e.message}")
                _negociosState.update {
                    it.copy(
                        isLoadingInitial = false,
                        error = "Error al cargar los negocios: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Carga la siguiente página de negocios desde la API.
     *
     * Flujo de ejecución:
     * 1. Evita llamadas duplicadas si ya se está cargando o si se llegó al final.
     * 2. Determina si es la primera carga (`isInitialLoad`) o una página adicional.
     * 3. Comprueba conectividad:
     *    - Si no hay conexión y es la primera página, carga desde caché si existe.
     *    - Si hay conexión, obtiene la página correspondiente de la API.
     * 4. Acumula los negocios cargados o reemplaza la lista si es la primera página.
     * 5. Actualiza el estado [_negociosState] con la información cargada, los indicadores de paginación y errores.
     *
     * Estados modificados:
     * - [_negociosState.isLoadingInitial]: indica si se está cargando la primera página.
     * - [_negociosState.isLoadingMore]: indica si se está cargando una página adicional.
     * - [_negociosState.negocios]: lista acumulada de negocios.
     * - [_negociosState.nextCursor]: cursor de la siguiente página.
     * - [_negociosState.endReached]: indica si se cargaron todas las páginas.
     * - [_negociosState.error]: mensaje de error en caso de fallo.
     */
    fun loadNextPageOfNegocios() {
        viewModelScope.launch {
            val currentState = _negociosState.value

            // Evita hacer llamadas si ya se está cargando o si se llegó al final
            if (currentState.isLoadingInitial || currentState.isLoadingMore || currentState.endReached) return@launch

            val isInitialLoad = currentState.negocios.isEmpty()
            if (isInitialLoad) {
                _negociosState.update { it.copy(isLoadingInitial = true, error = null) }
            } else {
                _negociosState.update { it.copy(isLoadingMore = true, error = null) }
            }

            // Revisar conectividad
            val online = _isNetworkAvailable.value

            if (!online && isInitialLoad && negociosRepository.hasCache()) {
                // 📥 Cargar desde caché
                val cached = negociosRepository.getNegocios()
                _negociosState.update {
                    it.copy(
                        isLoadingInitial = false,
                        isLoadingMore = false,
                        negocios = cached,
                        nextCursor = null, // no se puede paginar offline
                        endReached = true,
                        error = "Modo offline: mostrando datos en caché"
                    )
                }
                return@launch
            }

            // 🌐 Con internet → cargar API
            try {
                val response = api.getNegocios(cursor = currentState.nextCursor)

                // Guardar todos los negocios en caché si es la primera página
                if (currentState.nextCursor == null) {
                    negociosRepository.saveNegocios(response.items)
                }

                _negociosState.update {
                    it.copy(
                        isLoadingInitial = false,
                        isLoadingMore = false,
                        // Si es la primera página (nextCursor es null), reemplazar; si no, agregar
                        negocios = if (currentState.nextCursor == null) response.items else it.negocios + response.items,
                        nextCursor = response.nextCursor,
                        endReached = response.nextCursor == null
                    )
                }
            } catch (e: Exception) {
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

    /**
     * Actualiza el email en el estado de recuperación de contraseña.
     *
     * Flujo de ejecución:
     * - Actualiza [_forgotPasswordState.email] con el nuevo valor.
     * - Limpia cualquier error previo en [_forgotPasswordState.error].
     *
     * @param email Nuevo correo electrónico que ingresa el usuario.
     *
     * Estados modificados:
     * - [_forgotPasswordState.email]: se actualiza con el nuevo correo.
     * - [_forgotPasswordState.error]: se limpia para evitar mostrar mensajes antiguos.
     */
    fun onForgotPasswordEmailChange(email: String) {
        _forgotPasswordState.update { it.copy(email = email, error = null) }
    }
    /**
     * Envía un correo de restablecimiento de contraseña al email indicado en el estado de recuperación.
     *
     * Flujo de ejecución:
     * 1. Valida que el correo tenga un formato correcto.
     * 2. Actualiza [_forgotPasswordState] para mostrar loading.
     * 3. Configura Firebase para usar el idioma español.
     * 4. Llama a Firebase para enviar el correo de restablecimiento.
     * 5. Maneja la respuesta:
     *    - Éxito: marca [_forgotPasswordState.sent] como true.
     *    - Error: mapea errores comunes sin revelar información sensible.
     *
     * Estados modificados:
     * - [_forgotPasswordState.isLoading]: indica que se está enviando el correo.
     * - [_forgotPasswordState.error]: contiene mensajes de error en caso de fallo.
     * - [_forgotPasswordState.sent]: indica si el correo fue enviado correctamente.
     */
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

    /**
     * Resetea el estado de recuperación de contraseña.
     *
     * Flujo de ejecución:
     * - Restaura [_forgotPasswordState] a su estado inicial.
     *
     * Estados modificados:
     * - [_forgotPasswordState.email]: vacío.
     * - [_forgotPasswordState.error]: null.
     * - [_forgotPasswordState.isLoading]: false.
     * - [_forgotPasswordState.sent]: false.
     */
    fun clearForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState()
    }

    /**
     * Precarga todos los negocios desde la API y los guarda en caché.
     *
     * Flujo de ejecución:
     * 1. Inicializa una lista mutable para acumular los negocios.
     * 2. Itera por todas las páginas de la API hasta llegar al final o a un error.
     * 3. Guarda cada página en caché sobrescribiendo la previa.
     * 4. Actualiza [_negociosState] con todos los negocios precargados.
     *
     * Estados modificados:
     * - [_negociosState.negocios]: lista completa de negocios.
     * - [_negociosState.isLoadingInitial]: false al finalizar.
     * - [_negociosState.nextCursor]: null.
     * - [_negociosState.endReached]: true.
     */
    fun preloadAllNegocios() {
        viewModelScope.launch {
            val allNegocios = mutableListOf<Negocio>()
            var nextCursor: String? = null
            var endReached = false

            while (!endReached) {
                try {
                    val response = api.getNegocios(cursor = nextCursor)
                    allNegocios.addAll(response.items)

                    // Guardar en cache COMPLETA (sobrescribe cache previa)
                    negociosRepository.saveNegocios(allNegocios)

                    // Preparar siguiente página
                    nextCursor = response.nextCursor
                    endReached = nextCursor == null
                } catch (e: Exception) {
                    Log.e("AppVM", "Error al precargar negocios: ${e.message}")
                    endReached = true
                }
            }

            // Actualizar estado de la UI
            _negociosState.update {
                it.copy(
                    isLoadingInitial = false,
                    negocios = allNegocios,
                    nextCursor = null,
                    endReached = true
                )
            }

            Log.d("AppVM", "✅ Precarga de negocios completada: ${allNegocios.size} items")
        }
    }

    /**
     * Establece la lista de negocios desde la caché sin modificar la paginación.
     *
     * Flujo de ejecución:
     * - Actualiza [_negociosState.negocios] con la lista proporcionada.
     * - Reinicia los indicadores de carga inicial y carga adicional.
     *
     * @param cached Lista de negocios recuperada desde caché.
     *
     * Nota: No se modifica [_negociosState.endReached] para permitir que
     * `loadNextPageOfNegocios()` continúe cargando páginas posteriores.
     */
    fun setNegociosFromCache(cached: List<Negocio>) {
        _negociosState.value = _negociosState.value.copy(
            negocios = cached,
            isLoadingInitial = false,
            isLoadingMore = false
            // NO ponemos endReached = true, para que loadNextPageOfNegocios pueda seguir cargando
        )
    }

}
