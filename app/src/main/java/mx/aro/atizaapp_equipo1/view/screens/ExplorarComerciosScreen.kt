package mx.aro.atizaapp_equipo1.view.screens

import androidx.compose.material.icons.filled.CardMembership
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.aro.atizaapp_equipo1.R
import mx.aro.atizaapp_equipo1.ui.theme.AtizaAppEquipo1Theme
import mx.aro.atizaapp_equipo1.viewmodel.AppVM


data class Comercio(
    val nombre: String,
    val descripcion: String,
    val imagenRes: Int
)

@Composable
fun ExplorarComerciosScreen(appVM: AppVM = AppVM()) {

    val context = LocalContext.current

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
        bottomBar = {
            BottomNavigationBar()
            ElevatedButton(onClick = {
                appVM.hacerLogout(context)
            }) {
                Text("Logout")
            }
        }
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
fun BottomNavigationBar() {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Explorar") },
            label = { Text("Explorar") },
            selected = true,
            onClick = { /* Navegar a Explorar */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.CardMembership, contentDescription = "Mi credencial") },
            label = { Text("Mi credencial") },
            selected = false,
            onClick = { /* Navegar a Mi credencial */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Call, contentDescription = "Contáctanos") },
            label = { Text("Contáctanos") },
            selected = false,
            onClick = { /* Navegar a Contacto */ }
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