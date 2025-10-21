package mx.aro.atizaapp_equipo1.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

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

// Clase para errores de la API
data class ApiErrorResponse(
    val error: String,
    val message: String? = null,
    val details: Map<String, Any>? = null,
    val expected: String? = null,
    val provided: String? = null,
    val proveedor: Map<String, Any>? = null,
    val status: Int? = null,
    val data: Any? = null,
    val code: String? = null,
    val providerValue: String? = null,
    val sqlState: String? = null
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

data class OfertasApiResponse(
    val items: List<Oferta>,
    val nextCursor: String?
)

data class Oferta(
    val id: Int,
    val negocioId: Int,
    val titulo: String,
    val descripcion: String,
    val precio: String,
    val fechaInicio: String,
    val fechaFin: String?,
    val estadoId: Int
)


// Interceptor para manejar timeouts dinámicos por endpoint
private class DynamicTimeoutInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Busca el header customizado. Si no existe, usa el timeout por defecto.
        val customTimeout = request.header("X-Custom-Timeout")?.toIntOrNull()
            ?: return chain.proceed(request)

        // Si existe, aplica el timeout a la cadena y procede con la petición
        // sin el header para no enviarlo al servidor.
        val newRequest = request.newBuilder().removeHeader("X-Custom-Timeout").build()
        return chain
            .withConnectTimeout(customTimeout, TimeUnit.SECONDS)
            .withReadTimeout(customTimeout, TimeUnit.SECONDS)
            .withWriteTimeout(customTimeout, TimeUnit.SECONDS)
            .proceed(newRequest)
    }
}

interface ApiService {
    @Headers("X-Custom-Timeout: 60") // Timeout de 60 segundos solo para este endpoint
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

    @GET("/negocios/{id}")
    suspend fun getNegocioById(@Path("id") id: Int): Negocio

    @GET("/ofertas")
    suspend fun getOfertas(
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): OfertasApiResponse

    @GET("/ofertas")
    suspend fun getOfertasByNegocio(
        @Query("negocioId") negocioId: Int,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): OfertasApiResponse
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
        .addInterceptor(DynamicTimeoutInterceptor()) // Se añade el interceptor
        // No se define un timeout global para que cada endpoint pueda tener el suyo
        .build()

    val gson = provideGson()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://node-api-atizapp.onrender.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    return retrofit.create(ApiService::class.java)
}
