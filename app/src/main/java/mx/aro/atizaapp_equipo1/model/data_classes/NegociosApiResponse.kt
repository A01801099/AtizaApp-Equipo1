package mx.aro.atizaapp_equipo1.model.data_classes

/**
 * Representa una respuesta paginada de la API para la lista de negocios.
 */
data class NegociosApiResponse(
    val items: List<Negocio>,
    val nextCursor: String?
)