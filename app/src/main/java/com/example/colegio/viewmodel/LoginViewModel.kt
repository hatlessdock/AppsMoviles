package com.example.colegio.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.colegio.data.ReservaRepository
import com.example.colegio.model.Usuario

/**
 * ViewModel encargado de manejar el estado del formulario de login y registro,
 * validando credenciales y el estado de aprobación de cada cuenta.
 */
class LoginViewModel(private val repository: ReservaRepository) : ViewModel() {
    var correo by mutableStateOf(""); private set
    var contrasena by mutableStateOf(""); private set
    var isRegister by mutableStateOf(false); private set
    var rol by mutableStateOf("DOCENTE"); private set
    var error by mutableStateOf<String?>(null); private set

    // Limpia el mensaje de estado al escribir el correo
    fun onCorreo(v: String) { correo = v; error = null }

    // Limpia el mensaje de estado al escribir la contraseña
    fun onPass(v: String) { contrasena = v; error = null }

    // Actualiza el rol seleccionado en el formulario de registro ("DOCENTE" o "ADMIN")
    fun onRol(v: String) { rol = v }

    // Alterna entre modo inicio de sesión y modo crear cuenta
    fun toggleMode() { isRegister = !isRegister; error = null }

    // Valida existencia, contraseña y que la cuenta esté aprobada antes de permitir entrar
    fun login(): Usuario? {
        val user = repository.login(correo.trim(), contrasena)
        if (user == null) { error = "Credenciales incorrectas"; return null }
        if (!user.aprobado) { error = "Cuenta pendiente de aprobación"; return null }
        return user
    }

    // Crea la cuenta en estado pendiente y vuelve al login mostrando el aviso en verde
    fun registrar(): Boolean {
        val res = repository.registrar(correo.trim(), contrasena, rol)
        if (res == "OK") {
            error = "Cuenta creada. Espera aprobación del administrador"
            isRegister = false
            return true
        }
        error = res
        return false
    }
}