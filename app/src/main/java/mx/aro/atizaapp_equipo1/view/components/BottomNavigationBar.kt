package mx.aro.atizaapp_equipo1.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Barra de navegación inferior de la aplicación.
 */
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Explorar") },
            label = { Text("Explorar") },
            selected = currentRoute == "explorar",
            onClick = { navController.navigate("explorar") }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.AddShoppingCart, contentDescription = "Ofertas") },
            label = { Text("Beneficios") },
            selected = currentRoute == "beneficios",
            onClick = { navController.navigate("beneficios") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.CardMembership, contentDescription = "Mi credencial") },
            label = { Text("Mi credencial", textAlign = TextAlign.Companion.Center) },
            selected = currentRoute == "mi_credencial",
            onClick = { navController.navigate("mi_credencial") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Call, contentDescription = "Contacto") },
            label = { Text("Contacto") },
            selected = currentRoute == "contacto",
            onClick = { navController.navigate("contacto") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
            label = { Text("Ajustes") },
            selected = currentRoute == "ajustes",
            onClick = { navController.navigate("ajustes") }
        )
    }
}