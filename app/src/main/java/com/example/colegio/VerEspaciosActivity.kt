package com.example.colegio

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.colegio.data.ReservaRepository
import com.example.colegio.model.EstadoPuesto
import com.example.colegio.ui.theme.ColegioTheme

class VerEspaciosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = ReservaRepository(applicationContext)
        setContent {
            ColegioTheme {
                PantallaVerEspacios(repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaVerEspacios(repository: ReservaRepository) {
    val context = LocalContext.current
    val puestos = repository.obtenerPuestos()
    val modulos = repository.obtenerModulos()

    var soloOcupados by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ocupación - Escuela Marcela Paz") },
                navigationIcon = {
                    TextButton(onClick = { (context as? Activity)?.finish() }) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = soloOcupados,
                    onCheckedChange = { soloOcupados = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mostrar únicamente módulos EN USO (Ocupados)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(puestos, key = { it.id }) { puesto ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${puesto.nombre} (${puesto.categoria})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = puesto.ubicacion,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val modulosAMostrar = modulos.filter { m ->
                                val est = repository.obtenerEstadoPuestoEnModulo(puesto.id, m.id)
                                if (soloOcupados) est is EstadoPuesto.EnUso else true
                            }

                            if (modulosAMostrar.isEmpty()) {
                                Text(
                                    text = "Todos los módulos están disponibles actualmente.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                modulosAMostrar.forEach { modulo ->
                                    val estado = repository.obtenerEstadoPuestoEnModulo(puesto.id, modulo.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${modulo.rangoTexto} • ${modulo.tipo.name}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            if (estado is EstadoPuesto.EnUso) {
                                                Text(
                                                    text = "${estado.actividad} (${estado.ocupadoPor})",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                if (estado.detalleFlexibilidad.isNotEmpty()) {
                                                    Text(
                                                        text = estado.detalleFlexibilidad,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                        when (estado) {
                                            is EstadoPuesto.Disponible -> {
                                                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                                    Text("DISPONIBLE", modifier = Modifier.padding(4.dp))
                                                }
                                            }
                                            is EstadoPuesto.EnUso -> {
                                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                                    Text(
                                                        text = "USADO",
                                                        color = MaterialTheme.colorScheme.onError,
                                                        modifier = Modifier.padding(4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}