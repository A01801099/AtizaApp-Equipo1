package mx.aro.atizaapp_equipo1.view.screens

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import mx.aro.atizaapp_equipo1.viewmodel.AppVM
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.aro.atizaapp_equipo1.view.components.OfertaItem

/**
 * Muestra la lista de ofertas y descuentos, permitiendo buscar y filtrar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OfertasScreen(
    navController: NavHostController,
    appVM: AppVM = viewModel()
) {
    val state by appVM.ofertasState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var selectedCategoria by remember { mutableStateOf("Todos") }

    val categorias = listOf(
        "Todos", "Entretenimiento", "Comida", "Salud",
        "Belleza", "Educación", "Moda", "Servicios"
    )

    LaunchedEffect(Unit) {
        if (state.ofertas.isEmpty()) appVM.loadAllOfertas()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Beneficios y descuentos") }
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
                label = { Text("Buscar oferta") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar",
                            modifier = Modifier.clickable { searchText = "" }
                        )
                    }
                },
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

            if (state.isLoadingInitial) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Filtrado por texto y categoría
                val filtered = state.ofertas.filter { oferta ->
                    (oferta.titulo.contains(searchText, ignoreCase = true) ||
                            oferta.descripcion.contains(searchText, ignoreCase = true)) &&
                            (selectedCategoria == "Todos" ||
                                    oferta.categoria.equals(selectedCategoria, ignoreCase = true))
                }

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No se encontraron ofertas", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(filtered) { oferta ->
                            OfertaItem(oferta) {
                                // Navegar a detalle de oferta si se requiere
                                // navController.navigate("detalle_oferta/${oferta.id}")
                            }
                        }
                    }
                }

                // Mostrar error si existe
                state.error?.let { errorMsg ->
                    Text(
                        errorMsg,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

