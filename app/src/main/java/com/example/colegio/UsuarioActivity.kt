package com.example.colegio

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.colegio.data.ReservaRepository
import com.example.colegio.model.EstadoPuesto
import com.example.colegio.model.EstadoSolicitud
import com.example.colegio.ui.theme.*
import com.example.colegio.utils.RecursosNativosHelper
import com.example.colegio.viewmodel.ReservaViewModel

class UsuarioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ReservaViewModel(ReservaRepository(applicationContext))
        setContent {
            ColegioTheme {
                PantallaUsuario(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaUsuario(viewModel: ReservaViewModel) {
    val context = LocalContext.current
    val puestos = viewModel.obtenerPuestos()
    val modulos = viewModel.obtenerModulos()
    val fechasDisponibles = viewModel.obtenerFechasDisponibles()

    var menuPuestoAbierto by remember { mutableStateOf(false) }
    var menuModuloAbierto by remember { mutableStateOf(false) }
    var menuFechaAbierto by remember { mutableStateOf(false) }
    var menuEvidenciaAbierto by remember { mutableStateOf(false) }

    val puestoSeleccionado = puestos.getOrElse(viewModel.puestoIndex) { puestos.first() }
    val moduloSeleccionado = modulos.getOrElse(viewModel.moduloIndex) { modulos.first() }
    val fechaSeleccionada = fechasDisponibles.getOrElse(viewModel.fechaIndex) { fechasDisponibles.first() }
    val estadoPuesto = viewModel.obtenerEstadoEnModulo(puestoSeleccionado.id, moduloSeleccionado.id)

    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.onFotoCapturada(bitmap)
            Toast.makeText(context, "Foto adjuntada correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    val launcherArchivo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val nombre = uri.lastPathSegment?.substringAfterLast("/") ?: "documento_respaldo.pdf"
            viewModel.onDocumentoSeleccionado(nombre)
            Toast.makeText(context, "Archivo adjuntado: $nombre", Toast.LENGTH_SHORT).show()
        }
    }

    val launcherPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) launcherCamara.launch(null)
        else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    val launcherPermisoNotificaciones = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                launcherPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val misSolicitudes = viewModel.obtenerSolicitudes().filter {
        viewModel.nombreUsuario.isNotBlank() &&
                it.nombreUsuario.equals(viewModel.nombreUsuario.trim(), ignoreCase = true)
    }

    Scaffold(
        containerColor = FondoApp,
        topBar = {
            TopAppBar(
                title = { Text("Solicitar Espacio - Marcela Paz", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = { (context as? Activity)?.finish() }) {
                        Text("Volver", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealColegio,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TealFondoSuave),
                    border = BorderStroke(1.dp, TealColegio),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⏰ Regla Escolar: Las reservas deben realizarse hasta las 23:59 hrs del día anterior. Si necesitas un espacio para HOY por una emergencia, activa la 'Reserva Prioritaria'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TealOscuro,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = viewModel.nombreUsuario,
                    onValueChange = { viewModel.onNombreChange(it) },
                    label = { Text("Nombre del Profesor / Encargado") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TealColegio) },
                    trailingIcon = {
                        if (viewModel.errorNombre != null) Icon(Icons.Default.Warning, contentDescription = null, tint = RojoMalo)
                        else if (viewModel.nombreUsuario.length >= 3) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerdeBueno)
                    },
                    isError = viewModel.errorNombre != null,
                    supportingText = {
                        Text(
                            text = viewModel.errorNombre ?: "Campo obligatorio (mínimo 3 letras)",
                            color = if (viewModel.errorNombre != null) RojoMalo else TextoSecundario
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FondoTarjeta,
                        unfocusedContainerColor = FondoTarjeta,
                        focusedTextColor = TextoPrincipal,
                        unfocusedTextColor = TextoPrincipal
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = viewModel.motivoUso,
                    onValueChange = { viewModel.onMotivoUsoChange(it) },
                    label = { Text("Curso o Actividad / Taller") },
                    leadingIcon = { Icon(Icons.Default.Create, contentDescription = null, tint = TealColegio) },
                    trailingIcon = {
                        if (viewModel.errorMotivoUso != null) Icon(Icons.Default.Warning, contentDescription = null, tint = RojoMalo)
                        else if (viewModel.motivoUso.length >= 5) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerdeBueno)
                    },
                    isError = viewModel.errorMotivoUso != null,
                    supportingText = {
                        Text(
                            text = viewModel.errorMotivoUso ?: "Indica el curso y asignatura",
                            color = if (viewModel.errorMotivoUso != null) RojoMalo else TextoSecundario
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FondoTarjeta,
                        unfocusedContainerColor = FondoTarjeta,
                        focusedTextColor = TextoPrincipal,
                        unfocusedTextColor = TextoPrincipal
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.esPrioritaria) RojoFondo else FondoTarjeta
                    ),
                    border = BorderStroke(
                        width = if (viewModel.esPrioritaria) 1.5.dp else 1.dp,
                        color = if (viewModel.esPrioritaria) RojoMalo else BordeTarjeta
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = viewModel.esPrioritaria,
                                onCheckedChange = { viewModel.onPrioritariaCheckChange(it) },
                                colors = CheckboxDefaults.colors(checkedColor = RojoMalo)
                            )
                            Text(
                                text = "🚨 Reserva Prioritaria / Urgencia (Excepción para HOY)",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (viewModel.esPrioritaria) RojoMalo else TextoPrincipal,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        AnimatedVisibility(visible = viewModel.esPrioritaria) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                OutlinedTextField(
                                    value = viewModel.motivoPrioridad,
                                    onValueChange = { viewModel.onMotivoPrioridadChange(it) },
                                    label = { Text("Motivo de la Urgencia / Excepción") },
                                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = RojoMalo) },
                                    isError = viewModel.errorPrioridad != null,
                                    supportingText = {
                                        Text(
                                            text = viewModel.errorPrioridad ?: "Obligatorio: Explica qué imprevisto ocurrió y adjunta foto o documento abajo",
                                            color = if (viewModel.errorPrioridad != null) RojoMalo else TextoSecundario
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = FondoTarjeta,
                                        unfocusedContainerColor = FondoTarjeta
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text("1. Fecha de Reserva (Solo días hábiles):", style = MaterialTheme.typography.titleSmall, color = TextoPrincipal, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton(
                        onClick = { menuFechaAbierto = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = FondoTarjeta, contentColor = TextoPrincipal),
                        border = BorderStroke(1.dp, BordeTarjeta)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = TealColegio)
                        Spacer(Modifier.width(8.dp))
                        Text(fechaSeleccionada, fontWeight = FontWeight.Medium)
                    }
                    DropdownMenu(expanded = menuFechaAbierto, onDismissRequest = { menuFechaAbierto = false }) {
                        fechasDisponibles.forEachIndexed { index, fecha ->
                            DropdownMenuItem(
                                text = { Text(fecha, color = TextoPrincipal) },
                                onClick = {
                                    viewModel.onFechaSelected(index)
                                    menuFechaAbierto = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text("2. Espacio de la Escuela:", style = MaterialTheme.typography.titleSmall, color = TextoPrincipal, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton(
                        onClick = { menuPuestoAbierto = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = FondoTarjeta, contentColor = TextoPrincipal),
                        border = BorderStroke(1.dp, BordeTarjeta)
                    ) {
                        Text("${puestoSeleccionado.nombre} (${puestoSeleccionado.categoria})", fontWeight = FontWeight.Medium)
                    }
                    DropdownMenu(expanded = menuPuestoAbierto, onDismissRequest = { menuPuestoAbierto = false }) {
                        puestos.forEachIndexed { index, puesto ->
                            DropdownMenuItem(
                                text = { Text("${puesto.nombre} • ${puesto.ubicacion}", color = TextoPrincipal) },
                                onClick = { viewModel.onPuestoSelected(index); menuPuestoAbierto = false }
                            )
                        }
                    }
                }
            }

            item {
                Text("3. Módulo Horario (45 min):", style = MaterialTheme.typography.titleSmall, color = TextoPrincipal, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton(
                        onClick = { menuModuloAbierto = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = FondoTarjeta, contentColor = TextoPrincipal),
                        border = BorderStroke(1.dp, BordeTarjeta)
                    ) {
                        Text("${moduloSeleccionado.nombreBloque}: ${moduloSeleccionado.rangoTexto}", fontWeight = FontWeight.Medium)
                    }
                    DropdownMenu(expanded = menuModuloAbierto, onDismissRequest = { menuModuloAbierto = false }) {
                        modulos.forEachIndexed { index, modulo ->
                            val est = viewModel.obtenerEstadoEnModulo(puestoSeleccionado.id, modulo.id)
                            val ocupado = est is EstadoPuesto.EnUso
                            DropdownMenuItem(
                                text = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(modulo.rangoTexto, color = TextoPrincipal)
                                        Text(
                                            text = if (ocupado) "[OCUPADO]" else "[LIBRE]",
                                            color = if (ocupado) RojoMalo else VerdeBueno,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                onClick = { viewModel.onModuloSelected(index); menuModuloAbierto = false }
                            )
                        }
                    }
                }
            }

            item {
                val disponible = estadoPuesto is EstadoPuesto.Disponible
                val colorEstado by animateColorAsState(
                    targetValue = if (disponible) VerdeFondo else RojoFondo,
                    label = "colorDisponibilidad"
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorEstado),
                    border = BorderStroke(1.5.dp, if (disponible) VerdeBueno else RojoMalo),
                    modifier = Modifier.fillMaxWidth().animateContentSize()
                ) {
                    if (estadoPuesto is EstadoPuesto.Disponible) {
                        Text(
                            text = "✅ Espacio DISPONIBLE para el $fechaSeleccionada.",
                            color = VerdeBueno,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else if (estadoPuesto is EstadoPuesto.EnUso) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("⛔ HORARIO BLOQUEADO", color = RojoMalo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Reservado por: ${estadoPuesto.ocupadoPor}", color = TextoPrincipal, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                if (puestoSeleccionado.permiteFlexibilidad) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = viewModel.requiereFlexibilidad, onCheckedChange = { viewModel.onFlexibilidadCheckChange(it) })
                        Text("Requiere preparación especial / flexibilidad", color = TextoPrincipal, fontWeight = FontWeight.Medium)
                    }

                    AnimatedVisibility(visible = viewModel.requiereFlexibilidad) {
                        OutlinedTextField(
                            value = viewModel.motivoFlexibilidad,
                            onValueChange = { viewModel.onMotivoFlexibilidadChange(it) },
                            label = { Text("Justificar Flexibilidad") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = TealColegio) },
                            isError = viewModel.errorFlexibilidad != null,
                            supportingText = {
                                Text(
                                    text = viewModel.errorFlexibilidad ?: "Detalla cómo necesitas el espacio",
                                    color = if (viewModel.errorFlexibilidad != null) RojoMalo else TextoSecundario
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = FondoTarjeta,
                                unfocusedContainerColor = FondoTarjeta
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(visible = viewModel.requiereFlexibilidad || viewModel.esPrioritaria) {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text("Tipo de Respaldo / Documentación:", style = MaterialTheme.typography.labelLarge, color = TextoPrincipal, fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(
                                onClick = { menuEvidenciaAbierto = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = FondoTarjeta, contentColor = TextoPrincipal),
                                border = BorderStroke(1.dp, BordeTarjeta)
                            ) {
                                Text(viewModel.opcionesEvidencia[viewModel.tipoEvidenciaIndex])
                            }
                            DropdownMenu(expanded = menuEvidenciaAbierto, onDismissRequest = { menuEvidenciaAbierto = false }) {
                                viewModel.opcionesEvidencia.forEachIndexed { index, opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion, color = TextoPrincipal) },
                                        onClick = { viewModel.onTipoEvidenciaSelected(index); menuEvidenciaAbierto = false }
                                    )
                                }
                            }
                        }
                    }
                }

                if (viewModel.errorFotoObligatoria != null) {
                    Surface(
                        color = RojoFondo,
                        border = BorderStroke(1.dp, RojoMalo),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = viewModel.errorFotoObligatoria!!,
                            color = RojoMalo,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                launcherCamara.launch(null)
                            } else {
                                launcherPermisoCamara.launch(Manifest.permission.CAMERA)
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = FondoTarjeta, contentColor = TealColegio),
                        border = BorderStroke(1.dp, TealColegio),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (viewModel.fotoCapturada == null) "📷 Foto" else "📷 Cambiar", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { launcherArchivo.launch("*/*") },
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = FondoTarjeta, contentColor = TealColegio),
                        border = BorderStroke(1.dp, TealColegio),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (viewModel.documentoAdjunto.isEmpty()) "📄 Subir Archivo" else "📄 Cambiar Doc", fontWeight = FontWeight.SemiBold)
                    }
                }

                if (viewModel.documentoAdjunto.isNotEmpty()) {
                    Text(
                        text = "📄 Documento adjunto: ${viewModel.documentoAdjunto}",
                        style = MaterialTheme.typography.bodySmall,
                        color = VerdeBueno,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                AnimatedVisibility(visible = viewModel.fotoCapturada != null) {
                    viewModel.fotoCapturada?.let { bmp ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Image(bitmap = bmp.asImageBitmap(), contentDescription = "Evidencia", modifier = Modifier.size(120.dp))
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val exito = viewModel.enviarSolicitud()
                        if (exito) {
                            RecursosNativosHelper.vibrarDispositivo(context, 100L)
                            RecursosNativosHelper.mostrarNotificacionReserva(context, "Solicitud Registrada", "Enviada para ${puestoSeleccionado.nombre}")
                            Toast.makeText(context, "Solicitud guardada", Toast.LENGTH_SHORT).show()
                        } else {
                            RecursosNativosHelper.vibrarDispositivo(context, 300L)
                            Toast.makeText(context, "Revisa los campos en rojo", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = estadoPuesto is EstadoPuesto.Disponible,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.esPrioritaria) RojoMalo else VerdeBueno,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(
                        text = if (viewModel.esPrioritaria) "Enviar Reserva Prioritaria (Urgente)" else "Enviar Solicitud de Reserva",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BordeTarjeta)
                Text("Mis Solicitudes:", style = MaterialTheme.typography.titleMedium, color = TextoPrincipal, fontWeight = FontWeight.Bold)
            }

            if (misSolicitudes.isEmpty()) {
                item {
                    Text(
                        text = if (viewModel.nombreUsuario.isBlank()) {
                            "Escribe tu nombre arriba para ver tu historial de solicitudes."
                        } else {
                            "Aún no tienes solicitudes registradas."
                        },
                        color = TextoSecundario,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            items(misSolicitudes, key = { it.id }) { sol ->
                val colorEstado = when (sol.estado) {
                    EstadoSolicitud.APROBADA -> VerdeBueno
                    EstadoSolicitud.RECHAZADA -> RojoMalo
                    EstadoSolicitud.PENDIENTE -> NaranjoPendiente
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    border = BorderStroke(1.5.dp, colorEstado),
                    modifier = Modifier.fillMaxWidth().animateContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (sol.esPrioritaria) {
                            Text("🚨 PRIORITARIA / EXCEPCIÓN", color = RojoMalo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        }
                        Text("${sol.puesto.nombre} • ${sol.modulo.rangoTexto}", style = MaterialTheme.typography.titleSmall, color = TextoPrincipal, fontWeight = FontWeight.Bold)
                        Text("📅 Fecha: ${sol.fechaReserva}", color = TextoSecundario)
                        Text("Actividad: ${sol.motivoUso}", color = TextoPrincipal)
                        if (sol.tieneFotoAdjunta) Text("📷 Incluye fotografía", color = TealOscuro)
                        if (sol.documentoAdjunto.isNotEmpty()) Text("📄 Archivo: ${sol.documentoAdjunto}", color = TealOscuro)

                        Spacer(Modifier.height(2.dp))
                        Surface(
                            color = colorEstado,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Estado: ${sol.estado}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}