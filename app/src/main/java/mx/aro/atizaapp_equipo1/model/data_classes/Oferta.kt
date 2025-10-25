package mx.aro.atizaapp_equipo1.model.data_classes

data class Oferta(
    val id: Int,
    val negocioId: Int,
    val titulo: String,
    val descripcion: String,
    val precio: String,
    val fechaInicio: String,
    val fechaFin: String?,
    val estadoId: Int,
    val categoria: String
)