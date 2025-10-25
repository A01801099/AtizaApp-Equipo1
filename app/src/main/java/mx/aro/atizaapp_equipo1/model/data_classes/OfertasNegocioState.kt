package mx.aro.atizaapp_equipo1.model.data_classes

// Data class para el estado de ofertas de un negocio específico
data class OfertasNegocioState(
    val isLoading: Boolean = false,
    val ofertas: List<Oferta> = emptyList(),
    val error: String? = null,
    val negocioId: Int? = null
)