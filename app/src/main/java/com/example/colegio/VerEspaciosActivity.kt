package com.example.colegio

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.colegio.data.ReservaRepository
import com.example.colegio.model.EstadoPuesto

class VerEspaciosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = ReservaRepository(applicationContext)
        setContent { MaterialTheme { PantallaEspacios(repo) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEspacios(repo: ReservaRepository) {
    val ctx = LocalContext.current
    var ocupados by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ocupación de Espacios") },
                navigationIcon = { TextButton(onClick = { (ctx as? Activity)?.finish() }) { Text("Volver") } }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = ocupados, onCheckedChange = { ocupados = it })
                Spacer(Modifier.width(8.dp))
                Text("Mostrar solo ocupados")
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(repo.obtenerPuestos()) { p ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(p.nombre, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))

                            repo.obtenerModulos().forEach { m ->
                                val estado = repo.estadoPuesto(p.id, m.id)

                                if (!ocupados || estado is EstadoPuesto.EnUso) {
                                    val textoEstado = if (estado is EstadoPuesto.EnUso) {
                                        "OCUPADO por ${estado.ocupadoPor}"
                                    } else {
                                        "DISPONIBLE"
                                    }
                                    Text("• ${m.rangoTexto} - $textoEstado")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}