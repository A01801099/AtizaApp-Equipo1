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

interface ApiService {
    @Headers("X-Custom-Timeout: 60") // Timeout de 60 segundos solo para este endpoint
    @POST("/credencial")
    suspend fun createAccount(@Body body: CreateAccountRequest): CreateAccountResponse

    @GET("/credencial/me")
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

