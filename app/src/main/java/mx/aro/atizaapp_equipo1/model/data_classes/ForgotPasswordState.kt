package mx.aro.atizaapp_equipo1.model.data_classes

// Data class para el estado de recuperación de contraseña
data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val sent: Boolean = false,
    val error: String? = null
)