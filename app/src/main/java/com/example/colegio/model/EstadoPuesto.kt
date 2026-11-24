package com.example.colegio.model

sealed class EstadoPuesto {
    object Disponible : EstadoPuesto()
    data class EnUso(val ocupadoPor: String, val actividad: String = "", val detalleFlexibilidad: String = "") : EstadoPuesto()
}
enum class EstadoSolicitud { PENDIENTE, APROBADA, RECHAZADA }
enum class TipoActividad(val descripcion: String) {
    CLASE_FORMAL("Clase Formal (08:15 - 15:30)"),
    TALLER("Taller Extraprogramático (15:30 - 17:30)")
}