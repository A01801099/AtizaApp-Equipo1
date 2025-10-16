package mx.aro.atizaapp_equipo1.model

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.http.GET
import retrofit2.http.Query

//TODO DATA CLASES EN OTRO ARCHIVO
// Lo que tu API espera según CreateAccountSchema
data class CreateAccountRequest (
    val correo: String,            // ✅ requerido (usa el email de Firebase)
    val nombre: String,            // ✅ requerido
    val nacimiento: String,        // ✅ "YYYY-MM-DD"
    val curp: String,               // ✅ 18 chars
    val entidadRegistro: String,   // ✅ "AGUASCALIENTES"
)

data class CreateAccountResponse (
    val ok: Boolean,
    val usuario: Usuario
)

data class Usuario (
    val id: Int,                  // auto_increment → usa Long por seguridad
    val correo: String,
    val nombre: String,
    val nacimiento: String,        // mantenlo como String por API 24
    val estado: String,              // "MEXICO" (según tu backend)
    val curp: String
)

data class NegociosApiResponse(
    val items: List<Negocio>,
    val nextCursor: String?
)

data class Negocio (
    val id: Int,
    val usuarioId : Int,
    val nombre: String,
    val tipo: String,
    val ubicacion: String,
    val calificacion: String,
    val telefono: String,
    val imagen: String,
    val email: String,
    val descripcion: String
)

interface ApiService {
    @POST("/credencial")
    suspend fun createAccount(@Body body: CreateAccountRequest): CreateAccountResponse

    // ASÍ DEBE QUEDAR:
    @GET("/credencial/me") // Asegúrate de que la ruta sea la correcta para tu API
    suspend fun getMe(@Query("email") email: String): Usuario

    @GET("/negocios")
    suspend fun getNegocios(
        @Query("limit") limit: Int? = null,
        @Query("q") q: String? = null,
        @Query("cursor") cursor: String? = null
    ): NegociosApiResponse

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
        .baseUrl("https://node-api-atizapp.onrender.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    return retrofit.create(ApiService::class.java)
}
