package com.example.colegio.data

import android.content.Context
import android.graphics.Bitmap
import com.example.colegio.model.*

class ReservaRepository(private val context: Context) {
    init { BaseDatosSimulada.inicializarDatos(context) }


    fun login(correo: String, pass: String): Usuario? =
        BaseDatosSimulada.usuarios.find { it.correo == correo && it.contrasena == pass }

    fun registrar(correo: String, pass: String, rol: String): String {
        if (correo.isBlank() || pass.isBlank()) return "Llena todos los campos"
        if (BaseDatosSimulada.usuarios.any { it.correo == correo }) return "El correo ya existe"
        BaseDatosSimulada.usuarios.add(Usuario(correo.trim(), pass, rol, rol == "DOCENTE"))
        BaseDatosSimulada.guardarUsuarios(context)
        return "OK"
    }

    fun getAdminsPendientes() = BaseDatosSimulada.usuarios.filter { it.rol == "ADMIN" && !it.aprobado }

    fun aprobarAdmin(correo: String) {
        val i = BaseDatosSimulada.usuarios.indexOfFirst { it.correo == correo }
        if (i != -1) {
            BaseDatosSimulada.usuarios[i] = BaseDatosSimulada.usuarios[i].copy(aprobado = true)
            BaseDatosSimulada.guardarUsuarios(context)
        }
    }


    fun obtenerPuestos() = BaseDatosSimulada.listaPuestos
    fun obtenerModulos() = BaseDatosSimulada.listaModulos
    fun obtenerSolicitudes() = BaseDatosSimulada.solicitudes
    fun obtenerFoto(id: Int): Bitmap? = BaseDatosSimulada.fotosCapturadas[id]
    fun obtenerFechasPosibles(esPrioritaria: Boolean) = BaseDatosSimulada.obtenerFechasPosibles(esPrioritaria)

    fun estadoPuesto(puestoId: Int, moduloId: Int, fecha: String = ""): EstadoPuesto {
        val aprobada = BaseDatosSimulada.solicitudes.find {
            it.puesto.id == puestoId &&
                    it.modulo.id == moduloId &&
                    it.estado == EstadoSolicitud.APROBADA &&
                    (fecha.isEmpty() || it.fechaReserva == fecha)
        }
        return if (aprobada != null) {
            EstadoPuesto.EnUso(aprobada.nombreUsuario, aprobada.motivoUso, aprobada.motivoFlexibilidad)
        } else {
            EstadoPuesto.Disponible
        }
    }

    fun crearSolicitud(
        nombre: String,
        motivo: String,
        puesto: Puesto,
        modulo: ModuloHorario,
        fecha: String,
        reqFlex: Boolean,
        motFlex: String,
        esPrioritaria: Boolean,
        foto: Bitmap?,
        documento: String
    ): Boolean {
        if (estadoPuesto(puesto.id, modulo.id, fecha) is EstadoPuesto.EnUso) return false
        val id = BaseDatosSimulada.generarId()
        if (foto != null) BaseDatosSimulada.fotosCapturadas[id] = foto
        BaseDatosSimulada.solicitudes.add(
            SolicitudReserva(
                id = id,
                nombreUsuario = nombre,
                motivoUso = motivo,
                puesto = puesto,
                modulo = modulo,
                requiereFlexibilidad = reqFlex,
                motivoFlexibilidad = motFlex,
                tieneFotoAdjunta = foto != null,
                estado = EstadoSolicitud.PENDIENTE,
                fechaReserva = fecha,
                esPrioritaria = esPrioritaria,
                documentoAdjunto = documento
            )
        )
        BaseDatosSimulada.guardar(context)
        return true
    }

    fun aprobarSolicitud(id: Int) {
        val i = BaseDatosSimulada.solicitudes.indexOfFirst { it.id == id }
        if (i != -1) {
            val sol = BaseDatosSimulada.solicitudes[i]
            BaseDatosSimulada.solicitudes[i] = sol.copy(estado = EstadoSolicitud.APROBADA)
            for (j in BaseDatosSimulada.solicitudes.indices) {
                val s = BaseDatosSimulada.solicitudes[j]
                if (s.id != id && s.puesto.id == sol.puesto.id && s.modulo.id == sol.modulo.id && s.fechaReserva == sol.fechaReserva && s.estado == EstadoSolicitud.PENDIENTE) {
                    BaseDatosSimulada.solicitudes[j] = s.copy(estado = EstadoSolicitud.RECHAZADA)
                }
            }
            BaseDatosSimulada.guardar(context)
        }
    }

    fun rechazarSolicitud(id: Int) {
        val i = BaseDatosSimulada.solicitudes.indexOfFirst { it.id == id }
        if (i != -1) {
            BaseDatosSimulada.solicitudes[i] = BaseDatosSimulada.solicitudes[i].copy(estado = EstadoSolicitud.RECHAZADA)
            BaseDatosSimulada.guardar(context)
        }
    }
}