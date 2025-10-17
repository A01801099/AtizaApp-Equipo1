
package mx.aro.atizaapp_equipo1.view.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import mx.aro.atizaapp_equipo1.R
import mx.aro.atizaapp_equipo1.model.Negocio
import mx.aro.atizaapp_equipo1.ui.theme.AtizaAppEquipo1Theme
import mx.aro.atizaapp_equipo1.viewmodel.AppVM
import mx.aro.atizaapp_equipo1.viewmodel.NegociosState

// --------- PANTALLA PRINCIPAL DE EXPLORAR NEGOCIOS ---------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorarComerciosScreen(
    appVM: AppVM,
    navController: NavHostController
) {
    val negociosState by appVM.negociosState.collectAsState()

    // Llama para cargar la primera página solo una vez
    LaunchedEffect(Unit) {
        if (negociosState.negocios.isEmpty()) {
            appVM.loadNextPageOfNegocios()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explorar Negocios") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // Estado de carga inicial (pantalla completa)
                negociosState.isLoadingInitial -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                // Estado de error
                negociosState.error != null -> {
                    Text(
                        text = "Error: ${negociosState.error}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                // Contenido de la lista
                else -> {
                    NegociosList(
                        negociosState = negociosState,
                        onLoadMore = { appVM.loadNextPageOfNegocios() },
                        onNegocioClick = { negocioId ->
                            // Navegar a la pantalla de detalle del negocio
                            navController.navigate("explorar_comercio/${negocioId}")
                        }
                    )
                }
            }
        }
    }
}

// --------- LISTA DE NEGOCIOS CON PAGINACIÓN INFINITA ---------
@Composable
fun NegociosList(
    negociosState: NegociosState,
    onLoadMore: () -> Unit,
    onNegocioClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(negociosState.negocios, key = { _, negocio -> negocio.id }) { index, negocio ->
            NegocioCard(negocio = negocio, onClick = { onNegocioClick(negocio.id.toString()) })

            // Llama para cargar más items cuando el usuario se acerca al final de la lista
            if (index == negociosState.negocios.size - 1 && !negociosState.isLoadingMore && !negociosState.endReached) {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }

        // Muestra un indicador de carga al final de la lista si se están cargando más páginas
        item {
            if (negociosState.isLoadingMore) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}


// --------- TARJETA DE NEGOCIO REUTILIZABLE Y DE ALTA CALIDAD ---------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NegocioCard(
    negocio: Negocio,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            // Imagen del negocio
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(negocio.imagen)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_launcher_foreground), // Un placeholder genérico
                contentDescription = "Logo de ${negocio.nombre}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            // Contenido de la tarjeta
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nombre del negocio y categoría
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = negocio.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    // Categoría con un chip si existe
                    if (!negocio.tipo.isNullOrBlank()) {
                        AssistChip(
                            onClick = { /* No action */ },
                            label = { Text(negocio.tipo) },
                            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }

                // Descripción (si existe)
                if (!negocio.descripcion.isNullOrBlank()) {
                    Text(
                        text = negocio.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Detalles adicionales con íconos
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!negocio.email.isNullOrBlank()) {
                        IconText(icon = Icons.Default.Schedule, text = negocio.telefono)
                    }
                    if (!negocio.telefono.isNullOrBlank()) {
                        IconText(icon = Icons.Default.Phone, text = negocio.ubicacion)
                    }
                    if (!negocio.descripcion.isNullOrBlank()) {
                        IconText(icon = Icons.Default.LocationOn, text = negocio.tipo)
                    }
                    if (!negocio.telefono.isNullOrBlank()) {
                        IconText(icon = Icons.Default.Language, text = negocio.nombre, isLink = true)
                    }
                }
            }
        }
    }
}

// --------- COMPOSABLE DE AYUDA PARA MOSTRAR TEXTO CON ÍCONO ---------
@Composable
fun IconText(icon: ImageVector, text: String, isLink: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isLink) MaterialTheme.colorScheme.primary else LocalContentColor.current
        )
    }
}


// --------- NAVIGATION BAR (Reutilizada) ---------
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
            icon = { Icon(Icons.Default.CardMembership, contentDescription = "Mi credencial") },
            label = { Text("Mi credencial") },
            selected = currentRoute == "mi_credencial",
            onClick = { navController.navigate("mi_credencial") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Call, contentDescription = "Contáctanos") },
            label = { Text("Contáctanos") },
            selected = currentRoute == "contacto",
            onClick = { navController.navigate("contacto") }
        )
    }
}

// --------- PREVIEW ---------
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun ExplorarComerciosScreenPreview() {
    val fakeNavController = rememberNavController()
    // Crea un AppVM con datos de ejemplo para el preview
    val previewVM = AppVM()

    AtizaAppEquipo1Theme {
        ExplorarComerciosScreen(appVM = previewVM, navController = fakeNavController)
    }
}

@Preview(showBackground = true)
@Composable
fun NegocioCardPreview() {
    val previewNegocio = Negocio(
        id = 1,
        nombre = "Café del Lector",
        descripcion = "Un rincón acogedor para disfrutar de un buen libro y un excelente café de especialidad. Ofrecemos postres caseros y una selección de tés del mundo.",
        tipo = "Cafetería",
        imagen = "https://example.com/cafe_del_lector.jpg",
        email = "william.henry.harrison@example-pet-store.com",
        telefono = "55 1234 5678",
        ubicacion = "Av. Siempre Viva 123, Col. Centro",
        calificacion = "4.5",
        usuarioId = 2
    )
    AtizaAppEquipo1Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            NegocioCard(negocio = previewNegocio, onClick = {})
        }
    }
}
