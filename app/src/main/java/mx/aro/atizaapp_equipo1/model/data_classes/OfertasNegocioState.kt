package mx.aro.atizaapp_equipo1.model.data_classes

/**
 * Contiene el estado de la UI para las ofertas de un negocio específico.
 */
// Data class para el estado de ofertas de un negocio específico
data class OfertasNegocioState(
    val isLoading: Boolean = false,
    val ofertas: List<Oferta> = emptyList(),
    val error: String? = null,
    val negocioId: Int? = null
)