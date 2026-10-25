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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.colegio.data.ReservaRepository
import com.example.colegio.model.EstadoPuesto
import com.example.colegio.ui.theme.ColegioTheme
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

    var menuPuestoAbierto by remember { mutableStateOf(false) }
    var menuModuloAbierto by remember { mutableStateOf(false) }
    var menuEvidenciaAbierto by remember { mutableStateOf(false) } // NUEVO

    val puestoSeleccionado = puestos[viewModel.puestoIndex]
    val moduloSeleccionado = modulos[viewModel.moduloIndex]
    val estadoPuesto = viewModel.obtenerEstadoEnModulo(puestoSeleccionado.id, moduloSeleccionado.id)

    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.onFotoCapturada(bitmap)
            Toast.makeText(context, "Foto adjuntada correctamente", Toast.LENGTH_SHORT).show()
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
        topBar = {
            TopAppBar(
                title = { Text("Solicitar Espacio - Marcela Paz") },
                navigationIcon = {
                    TextButton(onClick = { (context as? Activity)?.finish() }) { Text("Volver") }
                }
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
                OutlinedTextField(
                    value = viewModel.nombreUsuario,
                    onValueChange = { viewModel.onNombreChange(it) },
                    label = { Text("Nombre del Profesor / Encargado") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    trailingIcon = {
                        if (viewModel.errorNombre != null) Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        else if (viewModel.nombreUsuario.length >= 3) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    isError = viewModel.errorNombre != null,
                    supportingText = { Text(viewModel.errorNombre ?: "Campo obligatorio (mínimo 3 letras)", color = if (viewModel.errorNombre != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = viewModel.motivoUso,
                    onValueChange = { viewModel.onMotivoUsoChange(it) },
                    label = { Text("Curso o Actividad / Taller") },
                    leadingIcon = { Icon(Icons.Default.Create, contentDescription = null) },
                    trailingIcon = {
                        if (viewModel.errorMotivoUso != null) Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        else if (viewModel.motivoUso.length >= 5) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    isError = viewModel.errorMotivoUso != null,
                    supportingText = { Text(viewModel.errorMotivoUso ?: "Indica el curso y asignatura", color = if (viewModel.errorMotivoUso != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Text("1. Espacio de la Escuela:", style = MaterialTheme.typography.titleSmall)
                Box {
                    OutlinedButton(onClick = { menuPuestoAbierto = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("${puestoSeleccionado.nombre} (${puestoSeleccionado.categoria})")
                    }
                    DropdownMenu(expanded = menuPuestoAbierto, onDismissRequest = { menuPuestoAbierto = false }) {
                        puestos.forEachIndexed { index, puesto ->
                            DropdownMenuItem(
                                text = { Text("${puesto.nombre} • ${puesto.ubicacion}") },
                                onClick = { viewModel.onPuestoSelected(index); menuPuestoAbierto = false }
                            )
                        }
                    }
                }
            }

            item {
                Text("2. Módulo Horario (45 min):", style = MaterialTheme.typography.titleSmall)
                Box {
                    OutlinedButton(onClick = { menuModuloAbierto = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("${moduloSeleccionado.nombreBloque}: ${moduloSeleccionado.rangoTexto}")
                    }
                    DropdownMenu(expanded = menuModuloAbierto, onDismissRequest = { menuModuloAbierto = false }) {
                        modulos.forEachIndexed { index, modulo ->
                            val est = viewModel.obtenerEstadoEnModulo(puestoSeleccionado.id, modulo.id)
                            val tag = if (est is EstadoPuesto.EnUso) " [OCUPADO]" else " [LIBRE]"
                            DropdownMenuItem(
                                text = { Text("${modulo.rangoTexto}$tag") },
                                onClick = { viewModel.onModuloSelected(index); menuModuloAbierto = false }
                            )
                        }
                    }
                }
            }

            item {
                val colorEstado by animateColorAsState(
                    targetValue = if (estadoPuesto is EstadoPuesto.Disponible) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                    label = "colorDisponibilidad"
                )
                Card(colors = CardDefaults.cardColors(containerColor = colorEstado), modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    if (estadoPuesto is EstadoPuesto.Disponible) {
                        Text("✅ Espacio DISPONIBLE en este módulo.", modifier = Modifier.padding(12.dp))
                    } else if (estadoPuesto is EstadoPuesto.EnUso) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("⛔ HORARIO BLOQUEADO", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.titleSmall)
                            Text("Reservado por: ${estadoPuesto.ocupadoPor}", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            item {
                if (puestoSeleccionado.permiteFlexibilidad) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = viewModel.requiereFlexibilidad, onCheckedChange = { viewModel.onFlexibilidadCheckChange(it) })
                        Text("Requiere preparación especial / flexibilidad")
                    }

                    AnimatedVisibility(visible = viewModel.requiereFlexibilidad) {
                        Column {
                            OutlinedTextField(
                                value = viewModel.motivoFlexibilidad,
                                onValueChange = { viewModel.onMotivoFlexibilidadChange(it) },
                                label = { Text("Justificar Flexibilidad") },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                isError = viewModel.errorFlexibilidad != null,
                                supportingText = { Text(viewModel.errorFlexibilidad ?: "Detalla por qué necesitas flexibilidad", color = if (viewModel.errorFlexibilidad != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Tipo de Evidencia Fotográfica (Para validar anticipación 24h):", style = MaterialTheme.typography.labelMedium)
                            Box {
                                OutlinedButton(onClick = { menuEvidenciaAbierto = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(viewModel.opcionesEvidencia[viewModel.tipoEvidenciaIndex])
                                }
                                DropdownMenu(expanded = menuEvidenciaAbierto, onDismissRequest = { menuEvidenciaAbierto = false }) {
                                    viewModel.opcionesEvidencia.forEachIndexed { index, opcion ->
                                        DropdownMenuItem(
                                            text = { Text(opcion) },
                                            onClick = { viewModel.onTipoEvidenciaSelected(index); menuEvidenciaAbierto = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                if (viewModel.errorFotoObligatoria != null) {
                    Text(
                        text = viewModel.errorFotoObligatoria!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            launcherCamara.launch(null)
                        } else {
                            launcherPermisoCamara.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (viewModel.errorFotoObligatoria != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(Icons.Default.Build, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (viewModel.fotoCapturada == null) "Tomar Foto de Evidencia" else "Cambiar Foto de Evidencia")
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enviar Solicitud de Reserva")
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Mis Solicitudes:", style = MaterialTheme.typography.titleMedium)
            }

            items(misSolicitudes, key = { it.id }) { sol ->
                Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${sol.puesto.nombre} • ${sol.modulo.rangoTexto}", style = MaterialTheme.typography.titleSmall)
                        Text("Actividad: ${sol.motivoUso}")
                        if (sol.tieneFotoAdjunta) Text("📷 Incluye fotografía")
                        Text("Estado: ${sol.estado}", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}