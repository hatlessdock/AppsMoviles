package com.example.colegio.data

import android.content.Context
import android.graphics.Bitmap
import com.example.colegio.model.*

class ReservaRepository(private val context: Context) {

    init {
        BaseDatosSimulada.inicializarDatosLocales(context)
    }

    fun obtenerPuestos(): List<Puesto> = BaseDatosSimulada.listaPuestos

    fun obtenerModulos(): List<ModuloHorario> = BaseDatosSimulada.listaModulos

    fun obtenerTodasLasSolicitudes(): List<SolicitudReserva> = BaseDatosSimulada.solicitudes

    fun obtenerFoto(solicitudId: Int): Bitmap? = BaseDatosSimulada.fotosCapturadas[solicitudId]

    fun obtenerEstadoPuestoEnModulo(puestoId: Int, moduloId: Int): EstadoPuesto {
        val reservaAprobada = BaseDatosSimulada.solicitudes.find {
            it.puesto.id == puestoId &&
                    it.modulo.id == moduloId &&
                    it.estado == EstadoSolicitud.APROBADA
        }

        return if (reservaAprobada != null) {
            val flexInfo = if (reservaAprobada.requiereFlexibilidad) {
                "Flexibilidad: ${reservaAprobada.motivoFlexibilidad}"
            } else ""
            EstadoPuesto.EnUso(
                ocupadoPor = reservaAprobada.nombreUsuario,
                actividad = reservaAprobada.motivoUso,
                detalleFlexibilidad = flexInfo
            )
        } else {
            EstadoPuesto.Disponible
        }
    }

    fun crearSolicitud(
        nombreUsuario: String,
        motivoUso: String,
        puesto: Puesto,
        modulo: ModuloHorario,
        requiereFlexibilidad: Boolean,
        motivoFlexibilidad: String,
        fotoBitmap: Bitmap?
    ): Boolean {
        val estadoActual = obtenerEstadoPuestoEnModulo(puesto.id, modulo.id)
        if (estadoActual is EstadoPuesto.EnUso) {
            return false
        }

        val nuevoId = BaseDatosSimulada.generarNuevoId()
        if (fotoBitmap != null) {
            BaseDatosSimulada.fotosCapturadas[nuevoId] = fotoBitmap
        }

        val nueva = SolicitudReserva(
            id = nuevoId,
            nombreUsuario = nombreUsuario.trim(),
            motivoUso = motivoUso.trim(),
            puesto = puesto,
            modulo = modulo,
            requiereFlexibilidad = requiereFlexibilidad,
            motivoFlexibilidad = if (requiereFlexibilidad) motivoFlexibilidad.trim() else "",
            tieneFotoAdjunta = fotoBitmap != null,
            estado = EstadoSolicitud.PENDIENTE
        )
        BaseDatosSimulada.solicitudes.add(nueva)
        BaseDatosSimulada.guardarEnAlmacenamientoLocal(context)
        return true
    }

    fun aprobarSolicitud(solicitudId: Int) {
        val index = BaseDatosSimulada.solicitudes.indexOfFirst { it.id == solicitudId }
        if (index != -1) {
            val aprobada = BaseDatosSimulada.solicitudes[index]
            BaseDatosSimulada.solicitudes[index] = aprobada.copy(
                estado = EstadoSolicitud.APROBADA
            )

            for (i in BaseDatosSimulada.solicitudes.indices) {
                val s = BaseDatosSimulada.solicitudes[i]
                if (s.id != solicitudId &&
                    s.puesto.id == aprobada.puesto.id &&
                    s.modulo.id == aprobada.modulo.id &&
                    s.estado == EstadoSolicitud.PENDIENTE
                ) {
                    BaseDatosSimulada.solicitudes[i] = s.copy(estado = EstadoSolicitud.RECHAZADA)
                }
            }
            BaseDatosSimulada.guardarEnAlmacenamientoLocal(context)
        }
    }

    fun rechazarSolicitud(solicitudId: Int) {
        val index = BaseDatosSimulada.solicitudes.indexOfFirst { it.id == solicitudId }
        if (index != -1) {
            val actual = BaseDatosSimulada.solicitudes[index]
            BaseDatosSimulada.solicitudes[index] = actual.copy(
                estado = EstadoSolicitud.RECHAZADA
            )
            BaseDatosSimulada.guardarEnAlmacenamientoLocal(context)
        }
    }
}