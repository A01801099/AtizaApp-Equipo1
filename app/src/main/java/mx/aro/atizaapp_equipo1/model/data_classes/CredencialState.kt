package mx.aro.atizaapp_equipo1.model.data_classes

import mx.aro.atizaapp_equipo1.model.data_classes.Usuario

// Data class para representar el estado de la pantalla de la credencial
data class CredencialState(
    val isLoading: Boolean = false,
    val usuario: Usuario? = null,
    val error: String? = null
)