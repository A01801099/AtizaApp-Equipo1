package mx.aro.atizaapp_equipo1.model.data_classes

data class NegociosApiResponse(
    val items: List<Negocio>,
    val nextCursor: String?
)