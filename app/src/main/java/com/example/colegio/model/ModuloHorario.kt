package com.example.colegio.model

data class ModuloHorario(
    val id: Int,
    val nombreBloque: String,
    val horaInicio: String,
    val horaFin: String,
    val tipo: TipoActividad
) {
    val rangoTexto: String
        get() = "$horaInicio - $horaFin"
}