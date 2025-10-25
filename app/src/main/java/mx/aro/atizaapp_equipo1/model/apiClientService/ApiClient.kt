package mx.aro.atizaapp_equipo1.model.apiClientService

object ApiClient {
    val service: ApiService by lazy {
        provideApi()
    }
}