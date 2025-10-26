package mx.aro.atizaapp_equipo1.model.apiClientService

import mx.aro.atizaapp_equipo1.model.data_classes.CreateAccountRequest
import mx.aro.atizaapp_equipo1.model.data_classes.CreateAccountResponse
import mx.aro.atizaapp_equipo1.model.data_classes.Negocio
import mx.aro.atizaapp_equipo1.model.data_classes.NegociosApiResponse
import mx.aro.atizaapp_equipo1.model.data_classes.OfertasApiResponse
import mx.aro.atizaapp_equipo1.model.data_classes.Usuario
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Define los endpoints de la API para interactuar con el backend.
 */
interface ApiService {
    /**
     * Crea una nueva credencial de usuario.
     */
    @Headers("X-Custom-Timeout: 60") // Timeout de 60 segundos solo para este endpoint
    @POST("/credencial")
    suspend fun createAccount(@Body body: CreateAccountRequest): CreateAccountResponse

    /**
     * Obtiene los datos del usuario autenticado.
     */
    @GET("/credencial/me")
    suspend fun getMe(@Query("email") email: String): Usuario

    /**
     * Obtiene una lista paginada de negocios.
     */
    @GET("/negocios")
    suspend fun getNegocios(
        @Query("limit") limit: Int? = null,
        @Query("q") q: String? = null,
        @Query("cursor") cursor: String? = null
    ): NegociosApiResponse

    /**
     * Obtiene un negocio específico por su ID.
     */
    @GET("/negocios/{id}")
    suspend fun getNegocioById(@Path("id") id: Int): Negocio

    /**
     * Obtiene una lista paginada de ofertas.
     */
    @GET("/ofertas")
    suspend fun getOfertas(
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): OfertasApiResponse

    /**
     * Obtiene las ofertas de un negocio específico.
     */
    @GET("/ofertas")
    suspend fun getOfertasByNegocio(
        @Query("negocioId") negocioId: Int,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): OfertasApiResponse
}
