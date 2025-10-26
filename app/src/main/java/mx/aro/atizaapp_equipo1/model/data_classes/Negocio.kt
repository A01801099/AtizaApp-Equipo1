package mx.aro.atizaapp_equipo1.model.data_classes

/**
 * Representa la entidad de un negocio.
 */
data class Negocio (
    val id: Int,
    val usuarioId : Int,
    val nombre: String,
    val tipo: String,
    val ubicacion: String,
    val calificacion: String,
    val telefono: String,
    val imagen: String,
    val email: String,
    val descripcion: String
)