package mx.aro.atizaapp_equipo1.utils

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Formatea una cadena de fecha en formato ISO a "dd/MM/yyyy".
 */
// Función auxiliar para formatear fechas
fun formatFecha(fecha: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(fecha)
        if (date != null) outputFormat.format(date) else fecha
    } catch (e: Exception) {
        fecha
    }
}