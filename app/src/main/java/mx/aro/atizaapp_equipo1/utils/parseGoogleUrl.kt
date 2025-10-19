package mx.aro.atizaapp_equipo1.utils
/**
 * Convierte una URL de Google Drive al formato directo para visualización de imágenes.
 *
 * @param url URL original de Google Drive (puede ser null o vacía)
 * @return URL directa para mostrar la imagen, o un placeholder si no es válida
 *
 * Ejemplo:
 * Input:  "https://drive.google.com/file/d/ABC123/view?usp=sharing"
 * Output: "https://drive.google.com/uc?export=view&id=ABC123"
 */
fun convertGoogleDriveUrl(url: String?): String {
    if (url.isNullOrEmpty()) {
        return "https://via.placeholder.com/300x200?text=Sin+Imagen"
    }

    // Si ya es una URL directa de Google Drive en formato correcto
    if (url.contains("drive.google.com/uc?")) {
        return url
    }

    // Extraer el FILE_ID de la URL de Google Drive
    // Formato: https://drive.google.com/file/d/FILE_ID/view?usp=drive_link
    val fileIdRegex = """/d/([a-zA-Z0-9_-]+)""".toRegex()
    val matchResult = fileIdRegex.find(url)

    return if (matchResult != null) {
        val fileId = matchResult.groupValues[1]
        "https://drive.google.com/uc?export=view&id=$fileId"
    } else {
        // Si no es una URL de Google Drive o no se pudo extraer el ID
        url
    }
}