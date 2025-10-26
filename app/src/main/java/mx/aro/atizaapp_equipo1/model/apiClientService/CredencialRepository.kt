package mx.aro.atizaapp_equipo1.model.apiClientService

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import mx.aro.atizaapp_equipo1.model.data_classes.Usuario
import mx.aro.atizaapp_equipo1.model.data_classes.CachedCredencial

private val Context.credencialDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "credencial_store"
)

/**
 * Repository para gestionar la persistencia local de la credencial del usuario
 * con cifrado, TTL y sincronización automática
 */
class CredencialRepository(private val context: Context) {

    private val dataStore = context.credencialDataStore
    private val gson = Gson()

    private object PreferencesKeys {
        val CREDENCIAL_JSON = stringPreferencesKey("credencial_json_encrypted")
        val TIMESTAMP = longPreferencesKey("credencial_timestamp")
        val VERSION = intPreferencesKey("credencial_version")
    }

    companion object {
        const val TTL_DAYS = 7
        const val TTL_MS = TTL_DAYS * 24 * 60 * 60 * 1000L
        private const val TAG = "CredencialRepository"
    }

    /**
     * Guardar credencial con cifrado en caché local
     * @param usuario Datos del usuario a guardar
     */
    suspend fun saveCredencial(usuario: Usuario) {
        try {
            val cached = CachedCredencial(
                usuario = usuario,
                timestampMs = System.currentTimeMillis(),
                version = 1
            )

            val json = gson.toJson(cached)
            val encryptedJson = encryptData(json)

            dataStore.edit { preferences ->
                preferences[PreferencesKeys.CREDENCIAL_JSON] = encryptedJson
                preferences[PreferencesKeys.TIMESTAMP] = cached.timestampMs
                preferences[PreferencesKeys.VERSION] = cached.version
            }

            Log.d(TAG, "Credencial guardada exitosamente en caché")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar credencial", e)
            throw e
        }
    }

    /**
     * Obtener credencial del caché
     * @return CachedCredencial si existe y no está expirado, null si no
     */
    suspend fun getCredencial(): CachedCredencial? {
        return try {
            val preferences = dataStore.data.first()
            val encryptedJson = preferences[PreferencesKeys.CREDENCIAL_JSON] ?: return null
            val timestamp = preferences[PreferencesKeys.TIMESTAMP] ?: return null

            val age = System.currentTimeMillis() - timestamp
            if (age > TTL_MS) {
                Log.d(TAG, "Credencial expirada (TTL: $TTL_DAYS días). Limpiando...")
                clearCredencial()
                return null
            }

            val decryptedJson = decryptData(encryptedJson)
            val cached = gson.fromJson(decryptedJson, CachedCredencial::class.java)

            Log.d(TAG, "Credencial cargada desde caché (edad: ${age / 1000 / 60 / 60} horas)")
            cached
        } catch (e: Exception) {
            Log.e(TAG, "Error al leer credencial. Limpiando caché corrupto...", e)
            clearCredencial()
            null
        }
    }

    /**
     * Verificar si existe credencial válida (sin traer datos completos)
     * Más eficiente que getCredencial() cuando solo necesitas saber si existe
     * @return true si existe y no está expirada, false si no
     */
    suspend fun hasValidCredencial(): Boolean {
        return try {
            val preferences = dataStore.data.first()
            val encryptedJson = preferences[PreferencesKeys.CREDENCIAL_JSON]
            val timestamp = preferences[PreferencesKeys.TIMESTAMP]

            if (encryptedJson == null || timestamp == null) {
                return false
            }

            val age = System.currentTimeMillis() - timestamp
            age <= TTL_MS
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar credencial", e)
            false
        }
    }

    /**
     * Limpia la credencial del caché local.
     */
    suspend fun clearCredencial() {
        try {
            dataStore.edit { preferences ->
                preferences.remove(PreferencesKeys.CREDENCIAL_JSON)
                preferences.remove(PreferencesKeys.TIMESTAMP)
                preferences.remove(PreferencesKeys.VERSION)
            }
            Log.d(TAG, "Caché de credencial limpiado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al limpiar credencial", e)
        }
    }

    /**
     * Obtener timestamp de última sincronización
     * @return timestamp en milisegundos o null si no existe
     */
    suspend fun getLastSyncTimestamp(): Long? {
        return try {
            val preferences = dataStore.data.first()
            preferences[PreferencesKeys.TIMESTAMP]
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener timestamp", e)
            null
        }
    }

    /**
     * Verificar si el caché está obsoleto (más viejo que X horas)
     * @param hours Número de horas para considerar obsoleto
     * @return true si está obsoleto o no existe, false si es reciente
     */
    suspend fun isCacheStale(hours: Int = 24): Boolean {
        return try {
            val timestamp = getLastSyncTimestamp() ?: return true
            val ageMs = System.currentTimeMillis() - timestamp
            val staleThreshold = hours * 60 * 60 * 1000L
            ageMs > staleThreshold
        } catch (e: Exception) {
            true // En caso de error, asumir que está obsoleto
        }
    }

    private fun encryptData(data: String): String {
        return try {
            Base64.encodeToString(
                data.toByteArray(Charsets.UTF_8),
                Base64.DEFAULT
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al cifrar datos", e)
            throw e
        }
    }

    private fun decryptData(encryptedData: String): String {
        return try {
            String(
                Base64.decode(encryptedData, Base64.DEFAULT),
                Charsets.UTF_8
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al descifrar datos", e)
            throw e
        }
    }

    /**
     * Formatear timestamp para mostrar en UI (helper público)
     * @param timestampMs Timestamp en milisegundos
     * @return String formateado "hace X horas/días"
     */
    fun formatTimestamp(timestampMs: Long): String {
        val diffMs = System.currentTimeMillis() - timestampMs
        val hours = diffMs / (1000 * 60 * 60)
        return when {
            hours < 1 -> "hace menos de 1 hora"
            hours < 24 -> "hace $hours horas"
            else -> {
                val days = hours / 24
                if (days == 1L) "hace 1 día" else "hace $days días"
            }
        }
    }
}
