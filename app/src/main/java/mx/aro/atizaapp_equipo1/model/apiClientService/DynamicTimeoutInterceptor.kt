package mx.aro.atizaapp_equipo1.model.apiClientService

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Intercepta peticiones para aplicar un timeout dinámico si se especifica.
 */
class DynamicTimeoutInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val customTimeout = request.header("X-Custom-Timeout")?.toIntOrNull()
            ?: return chain.proceed(request)

        val newRequest = request.newBuilder().removeHeader("X-Custom-Timeout").build()
        return chain
            .withConnectTimeout(customTimeout, TimeUnit.SECONDS)
            .withReadTimeout(customTimeout, TimeUnit.SECONDS)
            .withWriteTimeout(customTimeout, TimeUnit.SECONDS)
            .proceed(newRequest)
    }
}