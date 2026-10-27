package com.example.colegio.data

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.example.colegio.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object BaseDatosSimulada {
    private const val PREFS_NAME = "colegio_marcela_paz_prefs_v3"
    private const val KEY_SOLICITUDES = "solicitudes"
    private const val KEY_USUARIOS = "usuarios"
    private var inicializado = false

    val fotosCapturadas = mutableStateMapOf<Int, Bitmap>()
    val solicitudes = mutableStateListOf<SolicitudReserva>()
    val usuarios = mutableStateListOf<Usuario>()
    private var contadorId = 1

    val listaPuestos = listOf(
        Puesto(1, "Multicancha Principal", "Canchas", "Patio Central", true),
        Puesto(2, "Cancha Exterior Futbolito", "Canchas", "Patio Sur", true),
        Puesto(3, "Patio Techado", "Canchas", "Sector Norte", true),
        Puesto(4, "Laboratorio de Computación (Enlaces)", "Laboratorios", "Piso 2 - Pabellón A", true),
        Puesto(5, "Laboratorio de Ciencias", "Laboratorios", "Piso 1 - Pabellón B", true),
        Puesto(6, "Biblioteca CRA (Zona Silencio)", "Biblioteca CRA", "Piso 1 - Central", false),
        Puesto(7, "Sala de Trabajo Grupal CRA", "Biblioteca CRA", "Piso 1 - Central", true),
        Puesto(8, "Sala Audiovisual y Proyección", "Multimedia", "Piso 2 - Pabellón B", true)
    )

    val listaModulos = listOf(
        ModuloHorario(1, "Módulo 1", "08:15", "09:00", TipoActividad.CLASE_FORMAL),
        ModuloHorario(2, "Módulo 2", "09:00", "09:45", TipoActividad.CLASE_FORMAL),
        ModuloHorario(3, "Módulo 3", "10:00", "10:45", TipoActividad.CLASE_FORMAL),
        ModuloHorario(4, "Módulo 4", "10:45", "11:30", TipoActividad.CLASE_FORMAL),
        ModuloHorario(5, "Módulo 5", "11:45", "12:30", TipoActividad.CLASE_FORMAL),
        ModuloHorario(6, "Módulo 6", "12:30", "13:15", TipoActividad.CLASE_FORMAL),
        ModuloHorario(7, "Módulo 7", "14:00", "14:45", TipoActividad.CLASE_FORMAL),
        ModuloHorario(8, "Módulo 8", "14:45", "15:30", TipoActividad.CLASE_FORMAL),
        ModuloHorario(9, "Taller Bloque 1", "15:30", "16:15", TipoActividad.TALLER),
        ModuloHorario(10, "Taller Bloque 2", "16:15", "17:00", TipoActividad.TALLER),
        ModuloHorario(11, "Taller Cierre", "16:45", "17:30", TipoActividad.TALLER)
    )


    fun obtenerFechasPosibles(incluirHoyPorUrgencia: Boolean): List<String> {
        val formato = SimpleDateFormat("EEEE dd/MM/yyyy", Locale("es", "CL"))
        val calendario = Calendar.getInstance()
        val fechas = mutableListOf<String>()

        if (incluirHoyPorUrgencia) {
            val textoHoy = formato.format(calendario.time).replaceFirstChar { it.uppercase() }
            fechas.add("HOY (Excepción Prioritaria) - $textoHoy")
        }

        var diasAgregados = 0
        while (diasAgregados < 5) {
            calendario.add(Calendar.DAY_OF_YEAR, 1)
            val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)
            if (diaSemana != Calendar.SATURDAY && diaSemana != Calendar.SUNDAY) {
                val textoFecha = formato.format(calendario.time).replaceFirstChar { it.uppercase() }
                if (diasAgregados == 0) {
                    fechas.add("Mañana - $textoFecha")
                } else {
                    fechas.add(textoFecha)
                }
                diasAgregados++
            }
        }
        return fechas
    }

    fun inicializarDatos(context: Context) {
        if (inicializado) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val jsonUsu = prefs.getString(KEY_USUARIOS, null)
        if (jsonUsu != null) {
            usuarios.clear()
            val array = JSONArray(jsonUsu)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                usuarios.add(
                    Usuario(
                        correo = obj.getString("correo"),
                        contrasena = obj.getString("pass"),
                        rol = obj.getString("rol"),
                        aprobado = obj.getBoolean("aprobado")
                    )
                )
            }
        } else {
            usuarios.addAll(
                listOf(
                    Usuario("admin@colegio.cl", "admin123", "ADMIN", true),
                    Usuario("utp@colegio.cl", "utp123", "ADMIN", false),
                    Usuario("inspector@colegio.cl", "insp123", "ADMIN", false),
                    Usuario("docente@colegio.cl", "docente123", "DOCENTE", true),
                    Usuario("profe.deportes@colegio.cl", "123456", "DOCENTE", true),
                    Usuario("profe.ciencias@colegio.cl", "123456", "DOCENTE", true)
                )
            )
            guardarUsuarios(context)
        }

        if (usuarios.none { it.correo == "admin@colegio.cl" }) {
            usuarios.add(Usuario("admin@colegio.cl", "admin123", "ADMIN", true))
            guardarUsuarios(context)
        }

        val jsonSol = prefs.getString(KEY_SOLICITUDES, null)
        if (jsonSol != null) {
            solicitudes.clear()
            val array = JSONArray(jsonSol)
            var maxId = 0
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getInt("id")
                solicitudes.add(
                    SolicitudReserva(
                        id = id,
                        nombreUsuario = obj.getString("nombre"),
                        motivoUso = obj.getString("motivo"),
                        puesto = listaPuestos.find { it.id == obj.getInt("puestoId") } ?: listaPuestos[0],
                        modulo = listaModulos.find { it.id == obj.getInt("moduloId") } ?: listaModulos[0],
                        requiereFlexibilidad = obj.getBoolean("flexibilidad"),
                        motivoFlexibilidad = obj.getString("motivoFlex"),
                        tieneFotoAdjunta = obj.getBoolean("tieneFoto"),
                        estado = EstadoSolicitud.valueOf(obj.getString("estado")),
                        fechaReserva = obj.optString("fecha", obtenerFechasPosibles(false).first()),
                        esPrioritaria = obj.optBoolean("prioritaria", false),
                        documentoAdjunto = obj.optString("documento", "")
                    )
                )
                if (id > maxId) maxId = id
            }
            contadorId = maxId + 1
        } else {
            val primeraFechaNormal = obtenerFechasPosibles(false).first()
            val fechaHoyUrgencia = obtenerFechasPosibles(true).first()

            solicitudes.addAll(
                listOf(
                    SolicitudReserva(
                        id = generarId(),
                        nombreUsuario = "Prof. Carlos Soto",
                        motivoUso = "8° Básico A - Educación Física",
                        puesto = listaPuestos[0],
                        modulo = listaModulos[0],
                        requiereFlexibilidad = true,
                        motivoFlexibilidad = "[Instalar red de vóleibol] - Evidencia: Esquema de Montaje",
                        tieneFotoAdjunta = false,
                        estado = EstadoSolicitud.APROBADA,
                        fechaReserva = primeraFechaNormal,
                        esPrioritaria = false,
                        documentoAdjunto = ""
                    ),
                    SolicitudReserva(
                        id = generarId(),
                        nombreUsuario = "Prof. Andrés Silva",
                        motivoUso = "7° Básico A - Reemplazo Urgente Sala Inundada",
                        puesto = listaPuestos[3],
                        modulo = listaModulos[2],
                        requiereFlexibilidad = false,
                        motivoFlexibilidad = "[URGENCIA: Filtración de agua en sala 7°A]",
                        tieneFotoAdjunta = false,
                        estado = EstadoSolicitud.PENDIENTE,
                        fechaReserva = fechaHoyUrgencia,
                        esPrioritaria = true,
                        documentoAdjunto = "autorizacion_inspector_general.pdf"
                    )
                )
            )
            guardar(context)
        }
        inicializado = true
    }

    fun guardar(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        for (s in solicitudes) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("nombre", s.nombreUsuario)
            obj.put("motivo", s.motivoUso)
            obj.put("puestoId", s.puesto.id)
            obj.put("moduloId", s.modulo.id)
            obj.put("flexibilidad", s.requiereFlexibilidad)
            obj.put("motivoFlex", s.motivoFlexibilidad)
            obj.put("tieneFoto", s.tieneFotoAdjunta)
            obj.put("estado", s.estado.name)
            obj.put("fecha", s.fechaReserva)
            obj.put("prioritaria", s.esPrioritaria)
            obj.put("documento", s.documentoAdjunto)
            array.put(obj)
        }
        prefs.edit().putString(KEY_SOLICITUDES, array.toString()).apply()
    }

    fun guardarUsuarios(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        for (u in usuarios) {
            val obj = JSONObject()
            obj.put("correo", u.correo)
            obj.put("pass", u.contrasena)
            obj.put("rol", u.rol)
            obj.put("aprobado", u.aprobado)
            array.put(obj)
        }
        prefs.edit().putString(KEY_USUARIOS, array.toString()).apply()
    }

    fun generarId() = contadorId++
}