package com.example.colegio.model

data class SolicitudReserva(
    val id: Int,
    val nombreUsuario: String,
    val motivoUso: String,
    val puesto: Puesto,
    val modulo: ModuloHorario,
    val requiereFlexibilidad: Boolean = false,
    val motivoFlexibilidad: String = "",
    val tieneFotoAdjunta: Boolean = false,
    var estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE
)