package com.example.colegio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.colegio.data.ReservaRepository
import com.example.colegio.ui.theme.*
import com.example.colegio.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = LoginViewModel(ReservaRepository(applicationContext))
        setContent {
            ColegioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FondoApp
                ) {
                    PantallaLogin(vm, onLogin = { rol ->
                        vm.onPass("")
                        if (rol == "ADMIN") startActivity(Intent(this, AdminActivity::class.java))
                        else startActivity(Intent(this, UsuarioActivity::class.java))
                    })
                }
            }
        }
    }
}

@Composable
fun PantallaLogin(vm: LoginViewModel, onLogin: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_marcela_paz),
            contentDescription = "Escudo Escuela Escritora Marcela Paz",
            modifier = Modifier.size(120.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Escuela Escritora Marcela Paz",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TealOscuro
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (vm.isRegister) "Crear nueva cuenta" else "Gestión de Espacios Escolares",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario
        )

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            border = BorderStroke(1.dp, BordeTarjeta),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = vm.correo,
                    onValueChange = { vm.onCorreo(it) },
                    label = { Text("Correo institucional") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = vm.contrasena,
                    onValueChange = { vm.onPass(it) },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (vm.isRegister) {
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = vm.rol == "DOCENTE",
                            onClick = { vm.onRol("DOCENTE") }
                        )
                        Text("Docente", color = TextoPrincipal, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(16.dp))
                        RadioButton(
                            selected = vm.rol == "ADMIN",
                            onClick = { vm.onRol("ADMIN") }
                        )
                        Text("Administrador", color = TextoPrincipal, fontWeight = FontWeight.Medium)
                    }
                }

                if (vm.error != null) {
                    Spacer(Modifier.height(14.dp))
                    val esExito = vm.error!!.contains("creada", ignoreCase = true) ||
                            vm.error!!.contains("éxito", ignoreCase = true) ||
                            vm.error!!.contains("registrad", ignoreCase = true)

                    Surface(
                        color = if (esExito) VerdeFondo else RojoFondo,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (esExito) VerdeBueno else RojoMalo),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = vm.error!!,
                            color = if (esExito) VerdeBueno else RojoMalo,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                Button(
                    onClick = {
                        if (vm.isRegister) vm.registrar()
                        else vm.login()?.let { onLogin(it.rol) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealColegio,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (vm.isRegister) "Crear Cuenta" else "Entrar",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = { vm.toggleMode() }) {
                    Text(
                        text = if (vm.isRegister) "¿Ya tienes cuenta? Inicia sesión"
                        else "¿No tienes cuenta? Regístrate",
                        color = TealColegio,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}