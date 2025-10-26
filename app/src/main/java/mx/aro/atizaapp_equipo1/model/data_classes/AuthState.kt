package mx.aro.atizaapp_equipo1.model.data_classes

/**
 * Contiene el estado de la UI para las pantallas de autenticación.
 */
// Data class para representar el estado de la UI de autenticación
data class AuthState(
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalMessage: String? = null,
    val registrationComplete: Boolean = false
)