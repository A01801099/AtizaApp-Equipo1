package mx.aro.atizaapp_equipo1.model.data_classes

import mx.aro.atizaapp_equipo1.model.data_classes.Usuario

/**
 * Envuelve la credencial del usuario con metadatos para el caché.
 */
data class CachedCredencial(
    val usuario: Usuario,
    val timestampMs: Long,
    val version: Int = 1
)