package mx.aro.atizaapp_equipo1.view.screens

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.aro.atizaapp_equipo1.model.repository.NegociosRepository
import mx.aro.atizaapp_equipo1.ui.theme.AtizaAppEquipo1Theme
import mx.aro.atizaapp_equipo1.view.components.NegocioItem
import mx.aro.atizaapp_equipo1.viewmodel.AppVM

// --------- PANTALLA EXPLORAR COMERCIOS (NEGOCIOS REALES) ---------
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
// --------- PANTALLA EXPLORAR COMERCIOS (NEGOCIOS REALES) ---------

fun ExplorarComerciosScreen(
    navController: NavHostController,
    appVM: AppVM = viewModel(),
    context: Context = LocalContext.current
) {

    LaunchedEffect(Unit) {
        // Cargar TODOS los negocios al inicio
        if (appVM.negociosState.value.negocios.isEmpty()) {
            appVM.loadAllNegocios()
        }
    }

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
            // 🔍 Barra de búsqueda
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar negocios") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // 🏷️ Botones de categorías
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

            // ⏳ Carga inicial
            if (state.isLoadingInitial) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val filtered = state.negocios.filter { negocio ->
                    negocio.nombre.contains(searchText, ignoreCase = true) &&
                            (selectedCategoria == "Todos" || negocio.tipo?.equals(
                                selectedCategoria,
                                ignoreCase = true
                            ) == true)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { negocio ->
                        NegocioItem(negocio, navController)
                    }

                }

                // ⚠️ Mostrar error
                state.error?.let { errorMsg ->
                    Text(errorMsg, color = Color.Red, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}


