package mx.aro.atizaapp_equipo1.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.aro.atizaapp_equipo1.R
import mx.aro.atizaapp_equipo1.ui.theme.AtizaAppEquipo1Theme
import mx.aro.atizaapp_equipo1.view.screens.CodigoQRCredencialScreen
import mx.aro.atizaapp_equipo1.view.screens.ContactoScreen
import mx.aro.atizaapp_equipo1.view.screens.MiCredencialScreen


data class Comercio(
    val nombre: String,
    val descripcion: String,
    val imagenRes: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AtizaAppEquipo1Theme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomNavigationBar(navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "explorar",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("explorar") { ExplorarComerciosScreen() }
                        composable("contacto") { ContactoScreen() }
                        composable("mi_credencial") { MiCredencialScreen(navController) }
                        composable("codigo_qr_credencial") { CodigoQRCredencialScreen(navController) }
                    }
                }
            }
        }
    }
}

@Composable
fun ExplorarComerciosScreen() {
    var searchText by remember { mutableStateOf("") }
    val categorias = listOf("Todos", "Restaurantes", "Servicios", "Tiendas")

    // Mock de comercios
    val comercios = listOf(
        Comercio("Café Aroma", "Café y postres deliciosos", R.drawable.ic_launcher_foreground),
        Comercio("Ferretería López", "Todo para tu hogar", R.drawable.ic_launcher_foreground),
        Comercio("Tienda Verde", "Productos ecológicos", R.drawable.ic_launcher_foreground)
    )

    var selectedCategoria by remember { mutableStateOf("Todos") }

    Scaffold(
        topBar = {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar comercios") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Botones de categorías
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                categorias.forEach { categoria ->
                    Button(
                        onClick = { selectedCategoria = categoria },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCategoria == categoria) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(categoria, color = Color.White)
                    }
                }
            }

            // Lista de comercios filtrada por búsqueda
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filtered = comercios.filter {
                    it.nombre.contains(searchText, ignoreCase = true)
                            && (selectedCategoria == "Todos" || it.descripcion.contains(selectedCategoria, ignoreCase = true))
                }
                items(filtered) { comercio ->
                    ComercioItem(comercio)
                }
            }
        }
    }
}

@Composable
fun ComercioItem(comercio: Comercio) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Aquí irá la navegación a detalles */ },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            Image(
                painter = painterResource(id = comercio.imagenRes),
                contentDescription = comercio.nombre,
                modifier = Modifier.size(80.dp)
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(comercio.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(comercio.descripcion, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Explorar") },
            label = { Text("Explorar") },
            selected = currentRoute == "explorar",
            onClick = {
                navController.navigate("explorar") {
                    popUpTo("explorar") { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.CardMembership, contentDescription = "Mi credencial") },
            label = { Text("Mi credencial") },
            selected = currentRoute == "mi_credencial", // ✅ coincide con NavHost
            onClick = {
                navController.navigate("mi_credencial") { // ✅ coincide con NavHost
                    popUpTo("explorar") { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Call, contentDescription = "Contáctanos") },
            label = { Text("Contáctanos") },
            selected = currentRoute == "contacto",
            onClick = {
                navController.navigate("contacto") {
                    popUpTo("explorar") { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExplorarComerciosPreview() {
    AtizaAppEquipo1Theme {
        ExplorarComerciosScreen()
    }
}