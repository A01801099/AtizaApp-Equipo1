package mx.aro.atizaapp_equipo1.model.data_classes

//TODO DATA CLASES EN OTRO ARCHIVO
// Lo que tu API espera según CreateAccountSchema
data class CreateAccountRequest (
    val correo: String,            // ✅ requerido (usa el email de Firebase)
    val nombre: String,            // ✅ requerido
    val nacimiento: String,        // ✅ "YYYY-MM-DD"
    val curp: String,               // ✅ 18 chars
    val entidadRegistro: String,   // ✅ "AGUASCALIENTES"
)