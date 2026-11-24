package com.example.colegio.model

data class Puesto(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val ubicacion: String,
    val permiteFlexibilidad: Boolean = true
)


