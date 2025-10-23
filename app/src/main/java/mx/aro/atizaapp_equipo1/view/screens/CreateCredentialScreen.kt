package mx.aro.atizaapp_equipo1.view.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.aro.atizaapp_equipo1.viewmodel.AppVM
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCredentialScreen(
    appVM: AppVM,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados para controlar el flujo de disclaimers
    var aceptoAvisoPrivacidad by remember { mutableStateOf(false) }
    var aceptoDeclaratoriaEdad by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Mostrar disclaimers en secuencia
    when {
        !aceptoAvisoPrivacidad -> {
            AvisoPrivacidadScreen(
                onAceptar = { aceptoAvisoPrivacidad = true },
                onRegresar = { appVM.hacerLogout(context) },
                modifier = modifier
            )
            return
        }
        !aceptoDeclaratoriaEdad -> {
            DeclaratoriaEdadScreen(
                onAceptar = { aceptoDeclaratoriaEdad = true },
                onRegresar = { appVM.hacerLogout(context) },
                modifier = modifier
            )
            return
        }
    }

    // Una vez aceptados ambos disclaimers, mostrar el formulario
    val purple = Color(0xFF5B2DCC)
    val white = Color(0xFFFFFFFF)

    var curp by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var entidadRegistro by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }

    var showEntidadMenu by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var edadError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    val calendar = Calendar.getInstance()

    // Función para calcular la edad
    fun calcularEdad(fechaNac: String): Int? {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthDate = dateFormat.parse(fechaNac) ?: return null
            val birthCalendar = Calendar.getInstance()
            birthCalendar.time = birthDate
            val today = Calendar.getInstance()

            var edad = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

            // Ajustar si aún no ha cumplido años este año
            if (today.get(Calendar.MONTH) < birthCalendar.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == birthCalendar.get(Calendar.MONTH) &&
                        today.get(Calendar.DAY_OF_MONTH) < birthCalendar.get(Calendar.DAY_OF_MONTH))) {
                edad--
            }

            edad
        } catch (e: Exception) {
            null
        }
    }

    // Observar el estado de creación de credencial
    val createState by appVM.createCredentialState.collectAsStateWithLifecycle()

    // Manejar estados de éxito y error
    LaunchedEffect(createState) {
        if (createState.success) {
            showSuccessDialog = true
        } else if (createState.errorTitle != null) {
            showErrorDialog = true
        }
    }

    // Lista de entidades federativas según el catálogo del CURP
    val entidadesFederativas = listOf(
        "AGUASCALIENTES",
        "BAJA CALIFORNIA",
        "BAJA CALIFORNIA SUR",
        "CAMPECHE",
        "COAHUILA",
        "COLIMA",
        "CHIAPAS",
        "CHIHUAHUA",
        "CIUDAD DE MEXICO",
        "DURANGO",
        "GUANAJUATO",
        "GUERRERO",
        "HIDALGO",
        "JALISCO",
        "MEXICO",
        "MICHOACAN",
        "MORELOS",
        "NAYARIT",
        "NUEVO LEON",
        "OAXACA",
        "PUEBLA",
        "QUERETARO",
        "QUINTANA ROO",
        "SAN LUIS POTOSI",
        "SINALOA",
        "SONORA",
        "TABASCO",
        "TAMAULIPAS",
        "TLAXCALA",
        "VERACRUZ",
        "YUCATAN",
        "ZACATECAS",
        "NACIDO EXTRANJERO"
    )

    // DatePicker Dialog compatible con API 24
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            fechaNacimiento = dateFormat.format(selectedDate.time)

            // Validar edad al seleccionar la fecha
            val edad = calcularEdad(fechaNacimiento)
            edadError = if (edad != null && (edad < 12 || edad > 29)) {
                "Tu edad debe estar entre 12 y 29 años"
            } else {
                null
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Configurar fecha máxima (hoy)
    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

    // Configurar fecha mínima (100 años atrás)
    val minCalendar = Calendar.getInstance()
    minCalendar.add(Calendar.YEAR, -100)
    datePickerDialog.datePicker.minDate = minCalendar.timeInMillis

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(purple)
    ) {
        CurvedSheet(
            title = "Crear Credencial",
            sheetColor = white,
            curveHeight = 64.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Campo CURP
                OutlinedTextField(
                    value = curp,
                    onValueChange = {
                        if (it.length <= 18) {
                            curp = it.uppercase()
                        }
                    },
                    label = { Text("CURP") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("GELG030506HMCSPR08") },
                    supportingText = { Text("18 caracteres") }
                )

                // Campo Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it.uppercase() },
                    label = { Text("Nombre(s)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nombre") }
                )

                // Campo Apellido Paterno
                OutlinedTextField(
                    value = apellidoPaterno,
                    onValueChange = { apellidoPaterno = it.uppercase() },
                    label = { Text("Apellido Paterno") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Apellido Paterno") }
                )

                // Campo Apellido Materno
                OutlinedTextField(
                    value = apellidoMaterno,
                    onValueChange = { apellidoMaterno = it.uppercase() },
                    label = { Text("Apellido Materno") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Apellido Materno") }
                )

                // Selector de Entidad de Registro
                ExposedDropdownMenuBox(
                    expanded = showEntidadMenu,
                    onExpandedChange = { showEntidadMenu = it }
                ) {
                    OutlinedTextField(
                        value = entidadRegistro,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Entidad de Registro") },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                        trailingIcon = {
                            Icon(
                                Icons.Outlined.ArrowDropDown,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        placeholder = { Text("Selecciona una entidad") }
                    )

                    ExposedDropdownMenu(
                        expanded = showEntidadMenu,
                        onDismissRequest = { showEntidadMenu = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        entidadesFederativas.forEach { entidad ->
                            DropdownMenuItem(
                                text = { Text(entidad) },
                                onClick = {
                                    entidadRegistro = entidad
                                    showEntidadMenu = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Campo Fecha de Nacimiento (con DatePicker)
                OutlinedTextField(
                    value = fechaNacimiento,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de Nacimiento") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    placeholder = { Text("YYYY-MM-DD") },
                    supportingText = {
                        if (edadError != null) {
                            Text(
                                text = edadError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Formato: 2003-05-06")
                        }
                    },
                    isError = edadError != null,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (edadError != null)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = if (edadError != null)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de Crear Credencial
                Button(
                    onClick = {
                        // Validación básica
                        when {
                            curp.length != 18 -> {
                                Toast.makeText(context, "El CURP debe tener 18 caracteres", Toast.LENGTH_SHORT).show()
                            }
                            nombre.isBlank() -> {
                                Toast.makeText(context, "El nombre es requerido", Toast.LENGTH_SHORT).show()
                            }
                            apellidoPaterno.isBlank() -> {
                                Toast.makeText(context, "El apellido paterno es requerido", Toast.LENGTH_SHORT).show()
                            }
                            apellidoMaterno.isBlank() -> {
                                Toast.makeText(context, "El apellido materno es requerido", Toast.LENGTH_SHORT).show()
                            }
                            entidadRegistro.isBlank() -> {
                                Toast.makeText(context, "Selecciona una entidad de registro", Toast.LENGTH_SHORT).show()
                            }
                            fechaNacimiento.isBlank() -> {
                                Toast.makeText(context, "Selecciona una fecha de nacimiento", Toast.LENGTH_SHORT).show()
                            }
                            edadError != null -> {
                                Toast.makeText(context, "Tu edad debe estar entre 12 y 29 años", Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                // Construir el nombre completo
                                val nombreCompleto = "$nombre $apellidoPaterno $apellidoMaterno".trim()

                                // Llamar al ViewModel para crear la cuenta
                                appVM.createAccount(
                                    nombre = nombreCompleto,
                                    curp = curp,
                                    fechaNacimiento = fechaNacimiento,
                                    entidadRegistro = entidadRegistro
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = purple),
                    enabled = !createState.isLoading
                ) {
                    Text("Crear Credencial")
                }

                // Línea de separación
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = purple.copy(alpha = 0.3f)
                )

                // Botón de Cancelar
                ElevatedButton(
                    onClick = { appVM.hacerLogout(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = white,
                        contentColor = purple
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Text("Cancelar")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        // Overlay de Loading - Cubre todo el formulario mientras se valida
        if (createState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) { }, // Bloquear interacción
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(32.dp)
                        .wrapContentSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = white),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = purple,
                            strokeWidth = 6.dp
                        )

                        Text(
                            text = "Validando Datos",
                            style = MaterialTheme.typography.titleLarge,
                            color = purple
                        )

                        Text(
                            text = "Estamos verificando la información ingresada.\nEste proceso podría tomar varios segundos.\nPor favor, espere...",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Diálogo de Éxito
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    appVM.clearCreateCredentialState()
                    onDone()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = purple,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "¡Credencial Creada!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = "Tu credencial ha sido creada exitosamente y verificada con el registro oficial de CURP.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            appVM.clearCreateCredentialState()
                            onDone()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = purple)
                    ) {
                        Text("Continuar")
                    }
                },
                containerColor = white,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Diálogo de Error
        if (showErrorDialog && createState.errorTitle != null) {
            AlertDialog(
                onDismissRequest = {
                    showErrorDialog = false
                    appVM.clearCreateCredentialState()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = createState.errorTitle ?: "Error",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = createState.errorMessage ?: "Ocurrió un error desconocido.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showErrorDialog = false
                            appVM.clearCreateCredentialState()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Entendido")
                    }
                },
                dismissButton = {
                    if (createState.canRetry) {
                        TextButton(
                            onClick = {
                                showErrorDialog = false
                                appVM.clearCreateCredentialState()
                                // Reintentar con los mismos datos
                                val nombreCompleto = "$nombre $apellidoPaterno $apellidoMaterno".trim()
                                appVM.createAccount(
                                    nombre = nombreCompleto,
                                    curp = curp,
                                    fechaNacimiento = fechaNacimiento,
                                    entidadRegistro = entidadRegistro
                                )
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                },
                containerColor = white,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
