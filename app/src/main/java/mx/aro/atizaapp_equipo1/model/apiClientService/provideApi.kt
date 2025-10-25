package mx.aro.atizaapp_equipo1.model.apiClientService

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun provideApi(): ApiService {
    val client = OkHttpClient.Builder()
        .addInterceptor(FirebaseAuthInterceptor())
        .addInterceptor(DynamicTimeoutInterceptor())
        .build()

    val gson = provideGson()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://node-api-atizapp.onrender.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    return retrofit.create(ApiService::class.java)
}