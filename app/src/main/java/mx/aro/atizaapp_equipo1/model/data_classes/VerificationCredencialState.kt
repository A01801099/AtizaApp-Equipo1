package mx.aro.atizaapp_equipo1.model.data_classes

/**
 * Contiene el estado de la UI para la verificación de existencia de la credencial.
 */
// Data class para verificar SOLO la existencia de la credencial (para navegación)
data class VerificationCredencialState(
    val isLoading: Boolean = false,
    val hasCredencial: Boolean = false,
    val error: String? = null,
    val isNetworkError: Boolean = false
)