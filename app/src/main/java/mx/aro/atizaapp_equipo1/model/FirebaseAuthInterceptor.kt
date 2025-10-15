package mx.aro.atizaapp_equipo1.model

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response


//Cuando un usuario inicia sesión con Firebase (ya sea email/password o Google), Firebase Auth mantiene su sesión en el cliente.
//👉 Pero para que tu API backend sepa quién es ese usuario y pueda autorizarlo, debes incluir el ID Token de Firebase (JWT) en cada request HTTP.
//
//Ese ID Token es válido por una hora y luego expira, así que hay que refrescarlo automáticamente cuando el backend devuelve un 401 Unauthorized.

class FirebaseAuthInterceptor(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : Interceptor {

    private suspend fun idToken(force: Boolean): String? =
        auth.currentUser?.getIdToken(force)?.await()?.token

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // 1️⃣ Obtener token (usa caché si es válido)
        val token = runBlocking(Dispatchers.IO) { idToken(false) }

        // 2️⃣ Agregar header Authorization
        val withAuth = if (token != null)
            original.newBuilder().addHeader("Authorization", "Bearer $token").build()
        else original

        // 3️⃣ Hacer request
        var resp = chain.proceed(withAuth)

        // 4️⃣ Si es 401, refrescar token directo con Firebase y reintentar
        if (resp.code == 401) {
            resp.close()
            val refreshed = runBlocking(Dispatchers.IO) { idToken(true) }
            val retry = if (refreshed != null)
                original.newBuilder().header("Authorization", "Bearer $refreshed").build()
            else original
            resp = chain.proceed(retry)
        }
        return resp
    }
}

// **Esto maneja automáticamente:**
//- Agregar token a todas las requests
//- Refresh automático en caso de 401
//- Retry automático con nuevo token