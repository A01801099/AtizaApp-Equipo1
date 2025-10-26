package mx.aro.atizaapp_equipo1.model.data_classes

/**
 * Representa una respuesta de error estructurada desde la API.
 */
data class ApiErrorResponse(
    val error: String,
    val message: String? = null,
    val details: Map<String, Any>? = null,
    val expected: String? = null,
    val provided: String? = null,
    val proveedor: Map<String, Any>? = null,
    val status: Int? = null,
    val data: Any? = null,
    val code: String? = null,
    val providerValue: String? = null,
    val sqlState: String? = null
)