package mx.aro.atizaapp_equipo1.model.data_classes

// Data class para verificar SOLO la existencia de la credencial (para navegación)
data class VerificationCredencialState(
    val isLoading: Boolean = false,
    val hasCredencial: Boolean = false,
    val error: String? = null,
    val isNetworkError: Boolean = false
)