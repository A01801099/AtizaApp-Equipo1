package mx.aro.atizaapp_equipo1.utils

/**
 * 🔧 Función para dar formato al ID del usuario:
 * Ejemplo:
 * id = 1      → 0000-0000-0000-0001
 * id = 10000  → 0000-0000-0001-0000
 */
fun formatUserId(id: Int): String {
    val idStr = id.toString().padStart(16, '0') // asegura 16 dígitos
    return idStr.chunked(4).joinToString("-")  // agrupa de 4 en 4 con guiones
}