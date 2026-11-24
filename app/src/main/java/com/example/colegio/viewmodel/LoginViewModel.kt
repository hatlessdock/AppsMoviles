package com.example.colegio.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.colegio.data.ReservaRepository
import com.example.colegio.model.Usuario

class LoginViewModel(private val repository: ReservaRepository) : ViewModel() {
    var correo by mutableStateOf(""); private set
    var contrasena by mutableStateOf(""); private set
    var isRegister by mutableStateOf(false); private set
    var rol by mutableStateOf("DOCENTE"); private set
    var error by mutableStateOf<String?>(null); private set

    fun onCorreo(v: String) { correo = v; error = null }
    fun onPass(v: String) { contrasena = v; error = null }
    fun onRol(v: String) { rol = v }
    fun toggleMode() { isRegister = !isRegister; error = null }

    fun login(): Usuario? {
        val user = repository.login(correo.trim(), contrasena)
        if (user == null) { error = "Credenciales incorrectas"; return null }
        if (!user.aprobado) { error = "Cuenta pendiente de aprobación"; return null }
        return user
    }

    fun registrar(): Boolean {
        val res = repository.registrar(correo.trim(), contrasena, rol)
        if (res == "OK") {
            error = if (rol == "ADMIN") "Cuenta creada. Espera aprobación" else "Cuenta creada con éxito"
            isRegister = false
            return true
        }
        error = res
        return false
    }
}