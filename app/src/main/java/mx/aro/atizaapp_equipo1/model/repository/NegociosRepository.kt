package mx.aro.atizaapp_equipo1.model.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.aro.atizaapp_equipo1.model.data_classes.Negocio
import java.io.File

class NegociosRepository(private val context: Context) {

    private val fileName = "negocios_cache.json"

    fun saveNegocios(negocios: List<Negocio>) {
        val json = Gson().toJson(negocios)
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    fun getNegocios(): List<Negocio> {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) return emptyList()
            val json = file.readText()
            val type = object : TypeToken<List<Negocio>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun hasCache(): Boolean {
        val file = File(context.filesDir, fileName)
        return file.exists()
    }
}