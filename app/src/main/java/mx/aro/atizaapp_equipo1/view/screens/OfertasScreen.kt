package mx.aro.atizaapp_equipo1.view.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import mx.aro.atizaapp_equipo1.model.Oferta
import mx.aro.atizaapp_equipo1.viewmodel.AppVM
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.room.util.TableInfo


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfertasScreen(
    navController: NavHostController,
    appVM: AppVM
) {
    val state by appVM.ofertasState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    // Cargar la primera página automáticamente
    LaunchedEffect(Unit) {
        if (state.ofertas.isEmpty()) appVM.loadNextPageOfOfertas()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Beneficios y descuentos") }
            )
        }
    ) { paddingValues ->

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {

            // Barra de búsqueda
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar oferta") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

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
                    val filtered = state.ofertas.filter { it.titulo.contains(searchText, ignoreCase = true) }

                    items(filtered) { oferta ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Navegar a detalle de oferta si quieres */ },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(oferta.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(oferta.descripcion, fontSize = 14.sp)
                                Text("Precio: ${oferta.precio}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
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
                                appVM.loadNextPageOfOfertas()
                            }
                        }
                    }
                }

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