package mx.aro.atizaapp_equipo1.model.apiClientService

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

class FirebaseAuthInterceptor(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : Interceptor {

    private suspend fun idToken(force: Boolean): String? {
        return try {
            auth.currentUser?.getIdToken(force)?.await()?.token
        } catch (e: FirebaseNetworkException) {
            // Sin conexión - usar token en caché si existe
            Log.w("FirebaseAuthInterceptor", "⚠️ Sin conexión, imposible obtener token")
            null
        } catch (e: Exception) {
            Log.e("FirebaseAuthInterceptor", "❌ Error al obtener token: ${e.message}")
            null
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // 1️⃣ Obtener token (usa caché si es válido)
        val token = runBlocking(Dispatchers.IO) {
            try {
                idToken(false)
            } catch (e: Exception) {
                Log.e("FirebaseAuthInterceptor", "❌ Error al obtener token inicial: ${e.message}")
                null
            }
        }

        // 2️⃣ Agregar header Authorization
        val withAuth = if (token != null)
            original.newBuilder().addHeader("Authorization", "Bearer $token").build()
        else original

        // 3️⃣ Hacer request
        var resp = chain.proceed(withAuth)

        // 4️⃣ Si es 401, refrescar token directo con Firebase y reintentar
        if (resp.code == 401) {
            resp.close()
            val refreshed = runBlocking(Dispatchers.IO) {
                try {
                    idToken(true)
                } catch (e: Exception) {
                    Log.e("FirebaseAuthInterceptor", "❌ Error al refrescar token: ${e.message}")
                    null
                }
            }
            val retry = if (refreshed != null)
                original.newBuilder().header("Authorization", "Bearer $refreshed").build()
            else original
            resp = chain.proceed(retry)
        }
        return resp
    }
}