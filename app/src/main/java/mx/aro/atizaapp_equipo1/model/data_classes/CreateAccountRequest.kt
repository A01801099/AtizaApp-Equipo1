package mx.aro.atizaapp_equipo1.model.data_classes

/**
 * Define el cuerpo de la petición para crear una nueva cuenta.
 */
data class CreateAccountRequest (
    val correo: String,
    val nombre: String,
    val nacimiento: String,
    val curp: String,
    val entidadRegistro: String,
)