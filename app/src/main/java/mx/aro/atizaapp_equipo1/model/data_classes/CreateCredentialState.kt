package mx.aro.atizaapp_equipo1.model.data_classes

/**
 * Contiene el estado de la UI para el proceso de creación de credencial.
 */
data class CreateCredentialState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorTitle: String? = null,
    val errorMessage: String? = null,
    val canRetry: Boolean = false
)