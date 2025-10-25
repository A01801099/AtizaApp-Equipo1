package mx.aro.atizaapp_equipo1.model.data_classes

data class OfertasApiResponse(
    val items: List<Oferta>,
    val nextCursor: String?
)