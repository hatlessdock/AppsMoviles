package com.example.colegio.model

data class SolicitudReserva(
    val id: Int,
    val nombreUsuario: String,
    val motivoUso: String,
    val puesto: Puesto,
    val modulo: ModuloHorario,
    val requiereFlexibilidad: Boolean,
    val motivoFlexibilidad: String,
    val tieneFotoAdjunta: Boolean,
    val estado: EstadoSolicitud,
    val fechaReserva: String = "Mañana (Día Hábil)",
    val esPrioritaria: Boolean = false,
    val documentoAdjunto: String = ""
)