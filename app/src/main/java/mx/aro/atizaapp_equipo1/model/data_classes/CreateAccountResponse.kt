package mx.aro.atizaapp_equipo1.model.data_classes

import mx.aro.atizaapp_equipo1.model.data_classes.Usuario

data class CreateAccountResponse (
    val ok: Boolean,
    val usuario: Usuario
)