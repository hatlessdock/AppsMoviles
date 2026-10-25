package com.example.colegio

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.colegio.data.ReservaRepository
import com.example.colegio.model.EstadoSolicitud
import com.example.colegio.ui.theme.ColegioTheme
import com.example.colegio.utils.RecursosNativosHelper
import com.example.colegio.viewmodel.ReservaViewModel

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = ReservaRepository(applicationContext)
        val viewModel = ReservaViewModel(repository)
        setContent {
            ColegioTheme {
                PantallaAdmin(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAdmin(viewModel: ReservaViewModel) {
    val context = LocalContext.current
    val solicitudes = viewModel.obtenerSolicitudes()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin - Aprobar y Bloquear") },
                navigationIcon = {
                    TextButton(onClick = { (context as? Activity)?.finish() }) {
                        Text("Volver")
                    }
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
            items(solicitudes, key = { it.id }) { solicitud ->
                val colorTarjeta by animateColorAsState(
                    targetValue = when (solicitud.estado) {
                        EstadoSolicitud.PENDIENTE -> MaterialTheme.colorScheme.surfaceVariant
                        EstadoSolicitud.APROBADA -> MaterialTheme.colorScheme.primaryContainer
                        EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.errorContainer
                    },
                    label = "animColorEstado"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(containerColor = colorTarjeta)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Solicitud #${solicitud.id} • ${solicitud.puesto.nombre}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Solicitante: ${solicitud.nombreUsuario}")
                        Text("Curso / Taller: ${solicitud.motivoUso}")
                        Text("Horario: ${solicitud.modulo.nombreBloque} (${solicitud.modulo.rangoTexto})")
                        Text("Tipo: ${solicitud.modulo.tipo.descripcion}")

                        if (solicitud.requiereFlexibilidad) {
                            Text(
                                text = "⚠️ Flexibilidad solicitada: ${solicitud.motivoFlexibilidad}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        val foto = viewModel.obtenerFoto(solicitud.id)
                        if (foto != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                bitmap = foto.asImageBitmap(),
                                contentDescription = "Evidencia adjunta",
                                modifier = Modifier.size(100.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Estado: ${solicitud.estado}",
                            style = MaterialTheme.typography.labelLarge
                        )

                        if (solicitud.estado == EstadoSolicitud.PENDIENTE) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.aprobarSolicitud(solicitud.id)
                                        RecursosNativosHelper.vibrarDispositivo(context, 150L)
                                        RecursosNativosHelper.mostrarNotificacionReserva(
                                            context = context,
                                            titulo = "Reserva Aprobada y Bloqueada",
                                            mensaje = "${solicitud.puesto.nombre} bloqueado en ${solicitud.modulo.rangoTexto} para ${solicitud.nombreUsuario}"
                                        )
                                        Toast.makeText(
                                            context,
                                            "Aprobada y guardada localmente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Aceptar y Bloquear")
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.rechazarSolicitud(solicitud.id)
                                        RecursosNativosHelper.vibrarDispositivo(context, 250L)
                                        RecursosNativosHelper.mostrarNotificacionReserva(
                                            context = context,
                                            titulo = "Solicitud Rechazada",
                                            mensaje = "Se rechazó la solicitud de ${solicitud.nombreUsuario}"
                                        )
                                        Toast.makeText(
                                            context,
                                            "Solicitud Rechazada",
                                            Toast.LENGTH_SHORT
                                        ).show()
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