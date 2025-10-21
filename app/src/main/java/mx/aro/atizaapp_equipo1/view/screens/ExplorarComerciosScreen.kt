package mx.aro.atizaapp_equipo1.view.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import mx.aro.atizaapp_equipo1.R
import mx.aro.atizaapp_equipo1.ui.theme.AtizaAppEquipo1Theme
import mx.aro.atizaapp_equipo1.utils.convertGoogleDriveUrl
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

// --------- PANTALLA EXPLORAR COMERCIOS (NEGOCIOS REALES) ---------
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ExplorarComerciosScreen(
    navController: NavHostController,
    appVM: AppVM = viewModel()
) {
    val state by appVM.negociosState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var selectedCategoria by remember { mutableStateOf("Todos") }

    val categorias = listOf(
        "Todos", "Entretenimiento", "Comida", "Salud",
        "Belleza", "Educación", "Moda", "Servicios"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Explorar negocios") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de búsqueda
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar negocios") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

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
                            containerColor = if (selectedCategoria == categoria)
                                Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(categoria, color = Color.White)
                    }
                }
            }

            // Carga inicial de negocios
            if (state.isLoadingInitial) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filtered = state.negocios.filter { negocio ->
                        negocio.nombre.contains(searchText, ignoreCase = true) &&
                                (selectedCategoria == "Todos" || negocio.tipo.equals(selectedCategoria, ignoreCase = true))
                    }

                    items(filtered) { negocio ->
                        NegocioItem(negocio, navController)
                    }

                    // Spinner para paginación
                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    // Cargar siguiente página automáticamente
                    if (!state.endReached && !state.isLoadingMore) {
                        item {
                            LaunchedEffect(Unit) {
                                appVM.loadNextPageOfNegocios()
                            }
                        }
                    }
                }

                // Mostrar error si existe
                state.error?.let { errorMsg ->
                    Text(errorMsg, color = Color.Red, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

// --------- ITEM DE CADA NEGOCIO ---------
@Composable
fun NegocioItem(negocio: mx.aro.atizaapp_equipo1.model.Negocio, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("explorar_comercio/${negocio.id}") },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = convertGoogleDriveUrl(negocio.imagen),
                contentDescription = negocio.nombre,
                modifier = Modifier.size(80.dp),
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_foreground)
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(negocio.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Tipo: ${negocio.tipo}", fontSize = 14.sp)
                Text("Ubicación: ${negocio.ubicacion}", fontSize = 12.sp)
            }
        }
    }
}

// --------- BOTTOM NAVIGATION BAR ---------
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
            label = { Text("Mi credencial",textAlign = TextAlign.Center,) },
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

// --------- PREVIEW ---------
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun ExplorarComerciosPreview() {
    val fakeNavController = rememberNavController()
    val fakeViewModel = AppVM() // solo visual, permitido en Preview
    AtizaAppEquipo1Theme {
        ExplorarComerciosScreen(navController = fakeNavController, appVM = fakeViewModel)
    }
}