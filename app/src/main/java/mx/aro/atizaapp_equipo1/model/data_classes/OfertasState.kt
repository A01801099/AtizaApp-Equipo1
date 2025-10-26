package mx.aro.atizaapp_equipo1.model.data_classes

import mx.aro.atizaapp_equipo1.model.data_classes.Oferta

/**
 * Contiene el estado de la UI para la lista de ofertas, incluyendo paginación.
 */
data class OfertasState(
    val isLoadingInitial: Boolean = false,
    val isLoadingMore: Boolean = false,
    val ofertas: List<Oferta> = emptyList(),
    val error: String? = null,
    val nextCursor: String? = null,
    val endReached: Boolean = false
)