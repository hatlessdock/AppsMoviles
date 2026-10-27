package com.example.colegio

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.colegio.data.ReservaRepository
import com.example.colegio.model.EstadoSolicitud
import com.example.colegio.utils.RecursosNativosHelper
import com.example.colegio.viewmodel.ReservaViewModel

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = ReservaViewModel(ReservaRepository(applicationContext))
        setContent { MaterialTheme { PantallaAdmin(vm) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAdmin(vm: ReservaViewModel) {
    val ctx = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Admin") },
                navigationIcon = { TextButton(onClick = { (ctx as? Activity)?.finish() }) { Text("Salir") } }
            )
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            item {
                OutlinedButton(onClick = { ctx.startActivity(Intent(ctx, VerEspaciosActivity::class.java)) }, Modifier.fillMaxWidth()) {
                    Text("Ver Matriz de Ocupación")
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            val pendientes = vm.adminsPendientes()
            if (pendientes.isNotEmpty()) {
                item {
                    Text("⚠️ Admins por Aprobar (${pendientes.size})", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                }
                items(pendientes, key = { it.correo }) { admin ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(admin.correo)
                            Button(onClick = {
                                vm.aprobarAdmin(admin.correo)
                                Toast.makeText(ctx, "Autorizado", Toast.LENGTH_SHORT).show()
                            }) { Text("Autorizar") }
                        }
                    }
                }
                item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            }

            item { Text("Solicitudes de Instalaciones", style = MaterialTheme.typography.titleMedium) }

            items(vm.obtenerSolicitudes(), key = { it.id }) { s ->
                val color by animateColorAsState(
                    targetValue = when (s.estado) {
                        EstadoSolicitud.PENDIENTE -> MaterialTheme.colorScheme.surfaceVariant
                        EstadoSolicitud.APROBADA -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    label = "animColor"
                )
                Card(colors = CardDefaults.cardColors(containerColor = color), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${s.puesto.nombre} - ${s.modulo.rangoTexto}")
                        Text("Profesor: ${s.nombreUsuario} | Actividad: ${s.motivoUso}")
                        Text("📅 Fecha: ${s.fechaReserva}")
                        if (s.esPrioritaria) Text("🚨 RESERVA PRIORITARIA", color = MaterialTheme.colorScheme.error)
                        if (s.documentoAdjunto.isNotEmpty()) Text("📄 Documento: ${s.documentoAdjunto}")

                        if (s.requiereFlexibilidad) {
                            Text("⚠️ Flexibilidad: ${s.motivoFlexibilidad}")
                        }

                        vm.obtenerFoto(s.id)?.let {
                            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(100.dp))
                        }

                        Text("Estado: ${s.estado}")

                        if (s.estado == EstadoSolicitud.PENDIENTE) {
                            Row(Modifier.padding(top = 8.dp)) {
                                Button(
                                    onClick = {
                                        vm.aprobarSolicitud(s.id)
                                        RecursosNativosHelper.mostrarNotificacionReserva(ctx, "Aprobada", "Bloqueado")
                                        Toast.makeText(ctx, "Aprobada", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Aprobar")
                                }

                                Spacer(Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        vm.rechazarSolicitud(s.id)
                                        Toast.makeText(ctx, "Rechazada", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Rechazar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}