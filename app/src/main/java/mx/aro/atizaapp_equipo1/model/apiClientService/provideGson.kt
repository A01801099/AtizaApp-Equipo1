package mx.aro.atizaapp_equipo1.model.apiClientService

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * Provee una instancia configurada de Gson.
 */
fun provideGson(): Gson =
    GsonBuilder()
        .create()