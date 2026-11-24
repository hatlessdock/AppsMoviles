package com.example.colegio.logic

import java.util.Calendar

object ValidadorReserva {

    fun validarNombre(nombre: String): String? {
        return if (nombre.trim().length < 3) {
            "El nombre debe tener al menos 3 caracteres"
        } else null
    }

    fun validarMotivo(motivo: String): String? {
        return if (motivo.trim().length < 5) {
            "El motivo o curso debe tener al menos 5 caracteres"
        } else null
    }

    fun validarRespaldoUrgencia(
        esPrioritaria: Boolean,
        motivoUrgencia: String,
        tieneFoto: Boolean,
        documentoAdjunto: String
    ): String? {
        if (!esPrioritaria) return null
        if (motivoUrgencia.trim().length < 5) {
            return "Debes explicar el motivo de la urgencia"
        }
        if (!tieneFoto && documentoAdjunto.isBlank()) {
            return "Una reserva prioritaria exige foto o documento de respaldo"
        }
        return null
    }

    /**
     * Valida si una fecha (yyyy-MM-dd) es hábil: no es sábado, domingo ni feriado legal.
     */
    fun esDiaHabil(
        diaSemanaCalendar: Int,
        fechaIso: String,
        feriadosChile: Set<String>
    ): Boolean {
        val esFinDeSemana = diaSemanaCalendar == Calendar.SATURDAY ||
                diaSemanaCalendar == Calendar.SUNDAY
        val esFeriado = feriadosChile.contains(fechaIso)
        return !esFinDeSemana && !esFeriado
    }

    /**
     * Verifica si existe choque de horario contra una lista de reservas ya aprobadas.
     * Cada tupla representa (puestoId, moduloId, fechaReserva).
     */
    fun hayChoqueHorario(
        puestoId: Int,
        moduloId: Int,
        fechaReserva: String,
        reservasAprobadas: List<Triple<Int, Int, String>>
    ): Boolean {
        return reservasAprobadas.any { (pId, mId, fecha) ->
            pId == puestoId && mId == moduloId && fecha == fechaReserva
        }
    }
}