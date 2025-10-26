package mx.aro.atizaapp_equipo1.model.data_classes

/**
 * Contiene el estado de la UI para la recuperación de contraseña.
 */
// Data class para el estado de recuperación de contraseña
data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val sent: Boolean = false,
    val error: String? = null
)