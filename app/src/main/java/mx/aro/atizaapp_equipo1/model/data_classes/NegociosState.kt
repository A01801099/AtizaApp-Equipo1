package mx.aro.atizaapp_equipo1.model.data_classes

// Data class para el estado de la lista de negocios con paginación por cursor
data class NegociosState(
    val isLoadingInitial: Boolean = false, // Carga de pantalla completa la primera vez
    val isLoadingMore: Boolean = false,    // Spinner al final de la lista para paginación
    val negocios: List<Negocio> = emptyList(),
    val error: String? = null,
    val nextCursor: String? = null,        // Cursor para la siguiente página. Nulo para la primera llamada.
    val endReached: Boolean = false        // true si la API devuelve un cursor nulo
)