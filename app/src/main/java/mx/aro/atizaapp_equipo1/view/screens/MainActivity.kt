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

/**
 * Actividad principal que sirve como punto de entrada de la aplicación.
 */
class MainActivity : ComponentActivity() {
    private val viewModel: AppVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.initialize(applicationContext)

        val isDarkMode = ThemePrefs.isDarkMode(this)

        setContent {
            AtizaAppEquipo1Theme(darkTheme = isDarkMode) {
                val view = LocalView.current

                if (!view.isInEditMode) {
                    val window = (view.context as Activity).window
                    val isDarkTheme = isDarkMode

                    window.statusBarColor = if (isDarkTheme) {
                        Color.Companion.Black.toArgb()
                    } else {
                        Color.Companion.White.toArgb()
                    }

                    WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars =
                        !isDarkTheme
                }

                AppNavHost(appVM = viewModel)
            }
        }
    }
}