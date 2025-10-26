package mx.aro.atizaapp_equipo1.model.data_classes

/**
 * Contiene el estado de la UI para la lista de negocios, incluyendo paginación.
 */
data class NegociosState(
    val isLoadingInitial: Boolean = false,
    val isLoadingMore: Boolean = false,
    val negocios: List<Negocio> = emptyList(),
    val error: String? = null,
    val nextCursor: String? = null,
    val endReached: Boolean = false
)