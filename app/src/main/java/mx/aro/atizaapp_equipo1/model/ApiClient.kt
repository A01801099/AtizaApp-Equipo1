package mx.aro.atizaapp_equipo1.model

object ApiClient {
    val service: ApiService by lazy {
        provideApi()
    }
}

