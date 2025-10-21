package mx.aro.atizaapp_equipo1.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import mx.aro.atizaapp_equipo1.ui.theme.AtizaAppEquipo1Theme
import mx.aro.atizaapp_equipo1.view.navigation.AppNavHost
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

class MainActivity : ComponentActivity() {
    private val viewModel: AppVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AtizaAppEquipo1Theme {
                val view = LocalView.current
                if (!view.isInEditMode) {
                    val window = (view.context as Activity).window
                    val isDarkTheme = isSystemInDarkTheme()

                    // Color de fondo de la barra de estado
                    window.statusBarColor = if (isDarkTheme) {
                        Color.Black.toArgb()
                    } else {
                        Color.White.toArgb()
                    }

                    // Ajustar el color de los íconos según el modo
                    WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = !isDarkTheme
                }

                AppNavHost(appVM = viewModel)
            }
        }
        // 092520
        //2520
    }
}