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
    val purple = Color(0xFF5B2DCC)
    val white = Color(0xFFFFFFFF)

    var curp by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var entidadRegistro by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }

    var showEntidadMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

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
                    placeholder = { Text("GABRIEL") }
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
                    placeholder = { Text("ESPERILLA") }
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
                    placeholder = { Text("LEON") }
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
                    supportingText = { Text("Formato: 2003-05-06") },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            else -> {
                                // Construir el nombre completo
                                val nombreCompleto = "$nombre $apellidoPaterno $apellidoMaterno".trim()

                                // Llamar al ViewModel para crear la cuenta
                                appVM.createAccount(
                                    nombre = nombreCompleto,
                                    curp = curp,
                                    fechaNacimiento = fechaNacimiento,
                                    entidadRegistro = entidadRegistro,
                                    onSuccess = { response ->
                                        Toast.makeText(
                                            context,
                                            "Cuenta creada exitosamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onDone()
                                    },
                                    onError = { error ->
                                        Toast.makeText(
                                            context,
                                            "Error al crear la cuenta: ${error.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = purple)
                ) {
                    Text("Crear Credencial")
                }

                Spacer(modifier = Modifier.height(8.dp))

                ElevatedButton(onClick = {
                    appVM.hacerLogout(context)
                }) {
                    Text("Logout")
                }
            }
        }
    }
}
