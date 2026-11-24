package com.example.colegio

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colegio.data.ReservaRepository
import com.example.colegio.model.EstadoPuesto
import com.example.colegio.ui.theme.*

class VerEspaciosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = ReservaRepository(applicationContext)
        setContent { ColegioTheme { PantallaEspacios(repo) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEspacios(repo: ReservaRepository) {
    val ctx = LocalContext.current
    var ocupados by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FondoApp,
        topBar = {
            TopAppBar(
                title = { Text("Ocupación de Espacios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = { (ctx as? Activity)?.finish() }) {
                        Text("Volver", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealColegio,
                    titleContentColor = Color.White
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = ocupados, onCheckedChange = { ocupados = it })
                Spacer(Modifier.width(8.dp))
                Text("Mostrar solo ocupados", color = TextoPrincipal, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(repo.obtenerPuestos()) { p ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                        border = BorderStroke(1.dp, BordeTarjeta),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = p.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                color = TealOscuro,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(color = BordeTarjeta)

                            repo.obtenerModulos().forEach { m ->
                                val estado = repo.estadoPuesto(p.id, m.id)
                                val estaOcupado = estado is EstadoPuesto.EnUso

                                if (!ocupados || estaOcupado) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = m.rangoTexto,
                                            color = TextoPrincipal,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Surface(
                                            color = if (estaOcupado) RojoFondo else VerdeFondo,
                                            border = BorderStroke(1.dp, if (estaOcupado) RojoMalo else VerdeBueno),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (estado is EstadoPuesto.EnUso) {
                                                    "OCUPADO (${estado.ocupadoPor})"
                                                } else {
                                                    "DISPONIBLE"
                                                },
                                                color = if (estaOcupado) RojoMalo else VerdeBueno,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}