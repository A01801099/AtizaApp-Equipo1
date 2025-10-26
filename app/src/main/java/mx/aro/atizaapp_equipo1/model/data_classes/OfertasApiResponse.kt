package mx.aro.atizaapp_equipo1.model.data_classes

/**
 * Representa una respuesta paginada de la API para la lista de ofertas.
 */
data class OfertasApiResponse(
    val items: List<Oferta>,
    val nextCursor: String?
)