package mx.aro.atizaapp_equipo1.utils

/**
 * Formatea un ID de usuario a un formato de 16 dígitos agrupados.
 */
fun formatUserId(id: Int): String {
    val idStr = id.toString().padStart(16, '0')
    return idStr.chunked(4).joinToString("-")
}