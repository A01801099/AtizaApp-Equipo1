package mx.aro.atizaapp_equipo1.model.data_classes

/**
 * Representa la entidad de un usuario.
 */
data class Usuario (
    val id: Int,
    val correo: String,
    val nombre: String,
    val nacimiento: String,
    val estado: String,
    val curp: String
)