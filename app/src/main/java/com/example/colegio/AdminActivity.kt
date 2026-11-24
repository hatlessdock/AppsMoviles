package com.example.colegio

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colegio.data.ReservaRepository
import com.example.colegio.model.EstadoSolicitud
import com.example.colegio.ui.theme.*
import com.example.colegio.utils.RecursosNativosHelper
import com.example.colegio.viewmodel.ReservaViewModel

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = ReservaViewModel(ReservaRepository(applicationContext))
        setContent { ColegioTheme { PantallaAdmin(vm) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAdmin(vm: ReservaViewModel) {
    val ctx = LocalContext.current
    Scaffold(
        containerColor = FondoApp,
        topBar = {
            TopAppBar(
                title = { Text("Panel Admin", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = { (ctx as? Activity)?.finish() }) {
                        Text("Salir", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealColegio,
                    titleContentColor = Color.White
                )
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedButton(
                    onClick = { ctx.startActivity(Intent(ctx, VerEspaciosActivity::class.java)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = FondoTarjeta,
                        contentColor = TealColegio
                    ),
                    border = BorderStroke(1.5.dp, TealColegio)
                ) {
                    Text("Ver Matriz de Ocupación", fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = BordeTarjeta)
            }

            val pendientes = vm.adminsPendientes()
            if (pendientes.isNotEmpty()) {
                item {
                    Text(
                        text = "⚠️ Admins por Aprobar (${pendientes.size})",
                        color = RojoMalo,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(pendientes, key = { it.correo }) { admin ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                        border = BorderStroke(1.5.dp, RojoMalo),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = admin.correo,
                                color = TextoPrincipal,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    vm.aprobarAdmin(admin.correo)
                                    Toast.makeText(ctx, "Autorizado", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerdeBueno,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Autorizar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                item { HorizontalDivider(Modifier.padding(vertical = 8.dp), color = BordeTarjeta) }
            }

            item {
                Text(
                    text = "Solicitudes de Instalaciones",
                    color = TextoPrincipal,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(vm.obtenerSolicitudes(), key = { it.id }) { s ->
                val colorFondo by animateColorAsState(
                    targetValue = when (s.estado) {
                        EstadoSolicitud.PENDIENTE -> FondoTarjeta
                        EstadoSolicitud.APROBADA -> VerdeFondo
                        EstadoSolicitud.RECHAZADA -> RojoFondo
                    },
                    label = "animColor"
                )
                val colorBorde = when (s.estado) {
                    EstadoSolicitud.PENDIENTE -> NaranjoPendiente
                    EstadoSolicitud.APROBADA -> VerdeBueno
                    EstadoSolicitud.RECHAZADA -> RojoMalo
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = colorFondo),
                    border = BorderStroke(1.5.dp, colorBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${s.puesto.nombre} -${s.modulo.rangoTexto}",
                            color = TextoPrincipal,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Profesor: ${s.nombreUsuario} | Actividad: ${s.motivoUso}", color = TextoPrincipal)
                        Text("📅 Fecha: ${s.fechaReserva}", color = TextoSecundario, fontWeight = FontWeight.Medium)

                        if (s.esPrioritaria) {
                            Surface(
                                color = RojoFondo,
                                border = BorderStroke(1.dp, RojoMalo),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "🚨 RESERVA PRIORITARIA",
                                    color = RojoMalo,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (s.documentoAdjunto.isNotEmpty()) {
                            Text("📄 Documento: ${s.documentoAdjunto}", color = TealOscuro, fontWeight = FontWeight.Medium)
                        }

                        if (s.requiereFlexibilidad) {
                            Text("⚠️ Flexibilidad: ${s.motivoFlexibilidad}", color = NaranjoPendiente, fontWeight = FontWeight.SemiBold)
                        }

                        vm.obtenerFoto(s.id)?.let {
                            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(100.dp))
                        }

                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = when (s.estado) {
                                EstadoSolicitud.APROBADA -> VerdeBueno
                                EstadoSolicitud.RECHAZADA -> RojoMalo
                                EstadoSolicitud.PENDIENTE -> NaranjoPendiente
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Estado: ${s.estado}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        if (s.estado == EstadoSolicitud.PENDIENTE) {
                            Row(Modifier.padding(top = 10.dp)) {
                                Button(
                                    onClick = {
                                        vm.aprobarSolicitud(s.id)
                                        RecursosNativosHelper.mostrarNotificacionReserva(ctx, "Aprobada", "Bloqueado")
                                        Toast.makeText(ctx, "Aprobada", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VerdeBueno,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Aprobar", fontWeight = FontWeight.Bold)
                                }

                                Spacer(Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        vm.rechazarSolicitud(s.id)
                                        Toast.makeText(ctx, "Rechazada", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RojoMalo,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Rechazar", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}