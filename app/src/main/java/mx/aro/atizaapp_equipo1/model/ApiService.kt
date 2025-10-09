package mx.aro.atizaapp_equipo1.model

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.FieldNamingPolicy

//TODO DATA CLASES EN OTRO ARCHIVO
// Lo que tu API espera según CreateAccountSchema
data class CreateAccountRequest(
    val username: String,          // ✅ requerido
    val correo: String,            // ✅ requerido (usa el email de Firebase)
    val nombre: String,            // ✅ requerido
    val nacimiento: String,        // ✅ "YYYY-MM-DD"
    val curp: String,               // ✅ 18 chars
    val entidadRegistro: String,   // ✅ "AGUASCALIENTES"
)

data class CreateAccountResponse(
    val ok: Boolean,
    val usuario: Usuario
)

data class Usuario(
    val id: Long,                  // auto_increment → usa Long por seguridad
    val username: String,
    val correo: String,
    val nombre: String,
    val nacimiento: String,        // mantenlo como String por API 24
    val pais: String,              // "MEXICO" (según tu backend)
    val curp: String
)

interface ApiService {
    @POST("/credencial")
    suspend fun createAccount(@Body body: CreateAccountRequest): CreateAccountResponse
}

private fun provideGson(): Gson =
    GsonBuilder()
        // Si tu backend usa snake_case y tus data classes están en camelCase, activa esta línea:
        // .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        // Si necesitas serializar nulos:
        // .serializeNulls()
        // Si tu backend envía fechas ISO y quieres parsearlas como Date (no necesario aquí porque usas String):
        // .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

fun provideApi(): ApiService {
    val client = OkHttpClient.Builder()
        .addInterceptor(FirebaseAuthInterceptor())
        .build()

    val gson = provideGson()

    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    return retrofit.create(ApiService::class.java)
}
