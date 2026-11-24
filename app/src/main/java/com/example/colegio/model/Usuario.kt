package com.example.colegio.model

data class Usuario(
    val correo: String,
    val contrasena: String,
    val rol: String,
    val aprobado: Boolean
)