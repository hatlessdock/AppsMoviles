package com.example.colegio.data

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.example.colegio.model.*
import org.json.JSONArray
import org.json.JSONObject

object BaseDatosSimulada {

    private const val PREFS_NAME = "colegio_marcela_paz_prefs"
    private const val KEY_SOLICITUDES = "solicitudes_guardadas"
    private var inicializado = false
    val fotosCapturadas = mutableStateMapOf<Int, Bitmap>()

    val listaPuestos = listOf(
        Puesto(1, "Multicancha Principal Techada", "Canchas y Deporte", "Patio Central - Primer Nivel", true),
        Puesto(2, "Cancha Exterior / Patio de Juegos", "Canchas y Deporte", "Patio Sur", true),
        Puesto(3, "Sala de Computación 1 (Enlaces)", "Tecnología", "Piso 2 - Ala Norte", true),
        Puesto(4, "Sala de Computación 2 (Enlaces)", "Tecnología", "Piso 2 - Ala Sur", true),
        Puesto(5, "Laboratorio de Ciencias", "Académico", "Piso 2 - Sector Central", true),
        Puesto(6, "Biblioteca CRA", "Lectura y Recursos", "Piso 1 - Edificio Principal", false),
        Puesto(7, "Sala Programa de Integración (PIE)", "Apoyo e Inclusión", "Piso 1 - Sector Inclusión", true),
        Puesto(8, "Sala de Orientación y Psicología", "Convivencia Escolar", "Piso 1 - Administración", true),
        Puesto(9, "Sala de Expresión y Talleres", "Arte y Movimiento", "Piso 3 - Sector Cultural", true)
    )

    val listaModulos = listOf(
        ModuloHorario(1, "Módulo 1", "08:15", "09:00", TipoActividad.CLASE_FORMAL),
        ModuloHorario(2, "Módulo 2", "09:00", "09:45", TipoActividad.CLASE_FORMAL),
        ModuloHorario(3, "Módulo 3", "09:45", "10:30", TipoActividad.CLASE_FORMAL),
        ModuloHorario(4, "Módulo 4", "10:30", "11:15", TipoActividad.CLASE_FORMAL),
        ModuloHorario(5, "Módulo 5", "11:15", "12:00", TipoActividad.CLASE_FORMAL),
        ModuloHorario(6, "Módulo 6", "12:00", "12:45", TipoActividad.CLASE_FORMAL),
        ModuloHorario(7, "Módulo 7", "12:45", "13:30", TipoActividad.CLASE_FORMAL),
        ModuloHorario(8, "Módulo 8", "13:30", "14:15", TipoActividad.CLASE_FORMAL),
        ModuloHorario(9, "Módulo 9", "14:15", "15:00", TipoActividad.CLASE_FORMAL),
        ModuloHorario(10, "Módulo 10 (Cierre Formal)", "15:00", "15:30", TipoActividad.CLASE_FORMAL),
        ModuloHorario(11, "Taller Bloque 1", "15:30", "16:15", TipoActividad.TALLER),
        ModuloHorario(12, "Taller Bloque 2", "16:15", "17:00", TipoActividad.TALLER),
        ModuloHorario(13, "Taller Bloque 3 (Cierre)", "17:00", "17:30", TipoActividad.TALLER)
    )

    val solicitudes = mutableStateListOf<SolicitudReserva>()
    private var contadorIdSolicitud = 1

    fun inicializarDatosLocales(context: Context) {
        if (inicializado) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonGuardado = prefs.getString(KEY_SOLICITUDES, null)

        if (jsonGuardado != null) {
            solicitudes.clear()
            val jsonArray = JSONArray(jsonGuardado)
            var maxId = 0
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getInt("id")
                val puestoId = obj.getInt("puestoId")
                val moduloId = obj.getInt("moduloId")
                val puesto = listaPuestos.find { it.id == puestoId } ?: listaPuestos[0]
                val modulo = listaModulos.find { it.id == moduloId } ?: listaModulos[0]

                solicitudes.add(
                    SolicitudReserva(
                        id = id,
                        nombreUsuario = obj.getString("nombreUsuario"),
                        motivoUso = obj.getString("motivoUso"),
                        puesto = puesto,
                        modulo = modulo,
                        requiereFlexibilidad = obj.getBoolean("requiereFlexibilidad"),
                        motivoFlexibilidad = obj.getString("motivoFlexibilidad"),
                        tieneFotoAdjunta = obj.optBoolean("tieneFotoAdjunta", false),
                        estado = EstadoSolicitud.valueOf(obj.getString("estado"))
                    )
                )
                if (id > maxId) maxId = id
            }
            contadorIdSolicitud = maxId + 1
        } else {
            solicitudes.addAll(
                listOf(
                    SolicitudReserva(
                        id = 1,
                        nombreUsuario = "Prof. Carlos Muñoz",
                        motivoUso = "Educación Física - 6° Básico A",
                        puesto = listaPuestos[0],
                        modulo = listaModulos[0],
                        requiereFlexibilidad = true,
                        motivoFlexibilidad = "10 min extra para guardar balones",
                        estado = EstadoSolicitud.APROBADA
                    ),
                    SolicitudReserva(
                        id = 2,
                        nombreUsuario = "Prof. Andrés Gómez",
                        motivoUso = "Sala Enlaces - 8° Básico",
                        puesto = listaPuestos[2],
                        modulo = listaModulos[2],
                        requiereFlexibilidad = false,
                        motivoFlexibilidad = "",
                        estado = EstadoSolicitud.PENDIENTE
                    )
                )
            )
            contadorIdSolicitud = 3
            guardarEnAlmacenamientoLocal(context)
        }
        inicializado = true
    }

    fun guardarEnAlmacenamientoLocal(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (sol in solicitudes) {
            val obj = JSONObject()
            obj.put("id", sol.id)
            obj.put("nombreUsuario", sol.nombreUsuario)
            obj.put("motivoUso", sol.motivoUso)
            obj.put("puestoId", sol.puesto.id)
            obj.put("moduloId", sol.modulo.id)
            obj.put("requiereFlexibilidad", sol.requiereFlexibilidad)
            obj.put("motivoFlexibilidad", sol.motivoFlexibilidad)
            obj.put("tieneFotoAdjunta", sol.tieneFotoAdjunta)
            obj.put("estado", sol.estado.name)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_SOLICITUDES, jsonArray.toString()).apply()
    }

    fun generarNuevoId(): Int = contadorIdSolicitud++
}