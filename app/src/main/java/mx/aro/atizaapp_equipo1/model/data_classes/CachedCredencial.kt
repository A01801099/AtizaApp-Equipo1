package mx.aro.atizaapp_equipo1.model.data_classes

import mx.aro.atizaapp_equipo1.model.data_classes.Usuario

/**
 * Data class para wrapper de credencial con metadata de caché
 */
data class CachedCredencial(
    val usuario: Usuario,
    val timestampMs: Long,        // Cuándo se guardó en caché
    val version: Int = 1          // Versionado para migraciones futuras
)