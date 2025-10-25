package mx.aro.atizaapp_equipo1.model.apiClientService

import com.google.gson.Gson
import com.google.gson.GsonBuilder

fun provideGson(): Gson =
    GsonBuilder()
        .create()