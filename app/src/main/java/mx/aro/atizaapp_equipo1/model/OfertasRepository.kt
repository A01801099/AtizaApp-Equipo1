package mx.aro.atizaapp_equipo1.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OfertasRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("ofertas_cache", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val CACHE_KEY = "ofertas_list"
    private val TIMESTAMP_KEY = "ofertas_timestamp"

    fun saveOfertas(ofertas: List<Oferta>) {
        val json = gson.toJson(ofertas)
        prefs.edit()
            .putString(CACHE_KEY, json)
            .putLong(TIMESTAMP_KEY, System.currentTimeMillis())
            .apply()
    }

    fun getOfertas(): List<Oferta>? {
        val json = prefs.getString(CACHE_KEY, null) ?: return null
        val type = object : TypeToken<List<Oferta>>() {}.type
        return gson.fromJson(json, type)
    }

    fun isCacheStale(hours: Int = 1): Boolean {
        val timestamp = prefs.getLong(TIMESTAMP_KEY, 0L)
        val elapsed = System.currentTimeMillis() - timestamp
        return elapsed > hours * 3600 * 1000
    }

    fun clearCache() {
        prefs.edit().clear().apply()
    }
}
