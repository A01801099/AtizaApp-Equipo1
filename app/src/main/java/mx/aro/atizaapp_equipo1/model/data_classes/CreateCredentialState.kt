package mx.aro.atizaapp_equipo1.model.data_classes

// Data class para el estado de creación de credencial
data class CreateCredentialState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorTitle: String? = null,
    val errorMessage: String? = null,
    val canRetry: Boolean = false  // Indica si se puede mostrar botón "Reintentar"
)