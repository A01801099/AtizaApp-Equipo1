package mx.aro.atizaapp_equipo1.model.data_classes

import mx.aro.atizaapp_equipo1.model.data_classes.Usuario

/**
 * Representa la respuesta de la API al crear una cuenta.
 */
data class CreateAccountResponse (
    val ok: Boolean,
    val usuario: Usuario
)