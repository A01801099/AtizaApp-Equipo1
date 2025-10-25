package mx.aro.atizaapp_equipo1.model.data_classes

data class Usuario (
    val id: Int,                  // auto_increment → usa Long por seguridad
    val correo: String,
    val nombre: String,
    val nacimiento: String,        // mantenlo como String por API 24
    val estado: String,              // "MEXICO" (según tu backend)
    val curp: String
)