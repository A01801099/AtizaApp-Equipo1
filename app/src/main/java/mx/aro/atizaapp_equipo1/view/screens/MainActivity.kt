package mx.aro.atizaapp_equipo1.view.screens

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import mx.aro.atizaapp_equipo1.ui.theme.AtizaAppEquipo1Theme
import mx.aro.atizaapp_equipo1.utils.ThemePrefs
import mx.aro.atizaapp_equipo1.view.navigation.AppNavHost
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

class MainActivity : ComponentActivity() {
    private val viewModel: AppVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el ViewModel con el contexto de la aplicación
        viewModel.initialize(applicationContext)


        // 🌓 Paso 1: leer preferencia guardada
        val isDarkMode = ThemePrefs.isDarkMode(this)

        setContent {
            // 🧩 Paso 2: pasar la preferencia al tema
            AtizaAppEquipo1Theme(darkTheme = isDarkMode) {
                val view = LocalView.current

                if (!view.isInEditMode) {
                    val window = (view.context as Activity).window

                    // Usa la variable isDarkMode en lugar de isSystemInDarkTheme()
                    val isDarkTheme = isDarkMode

                    // Cambia color de barra de estado
                    window.statusBarColor = if (isDarkTheme) {
                        Color.Companion.Black.toArgb()
                    } else {
                        Color.Companion.White.toArgb()
                    }

                    // Ajusta color de íconos según tema
                    WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars =
                        !isDarkTheme
                }

                // 🚀 Carga el contenido principal
                AppNavHost(appVM = viewModel)
            }
        }
    }
}