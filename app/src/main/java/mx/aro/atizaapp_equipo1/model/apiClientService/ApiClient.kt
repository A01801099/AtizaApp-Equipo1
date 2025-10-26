package mx.aro.atizaapp_equipo1.model.apiClientService

/**
 * Provee una instancia única del cliente de API para toda la app.
 */
object ApiClient {
    val service: ApiService by lazy {
        provideApi()
    }
}