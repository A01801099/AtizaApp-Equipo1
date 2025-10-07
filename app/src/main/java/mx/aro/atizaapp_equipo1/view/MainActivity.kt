package mx.aro.atizaapp_equipo1.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import mx.aro.atizaapp_equipo1.ui.theme.AtizaAppEquipo1Theme
import mx.aro.atizaapp_equipo1.view.navigation.AppNavHost
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

class MainActivity : ComponentActivity() {

    private val viewModel: AppVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AtizaAppEquipo1Theme {
                AppNavHost(appVM = viewModel)
            }
        }
    }
}
