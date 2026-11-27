package com.example.colegio.data

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.example.colegio.model.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Objeto singleton que funciona como caché reactiva en memoria.
 * Jetpack Compose observa estas listas (mutableStateListOf) y redibuja
 * automáticamente la pantalla cada vez que se agrega, edita o elimina un dato.
 */
object ColegioCacheLocal {
    // Evita recargar SQLite varias veces si ya se inicializó en esta sesión
    private var inicializado = false

    // Zona horaria e idioma fijos de Chile para evitar desfases con la hora del emulador o Render (UTC)
    val ZONA_CHILE: TimeZone = TimeZone.getTimeZone("America/Santiago")
    private val LOCALE_CL = Locale("es", "CL")

    // Almacenamiento reactivo en memoria (respaldado por SQLite y Spring Boot)
    val fotosCapturadas = mutableStateMapOf<Int, Bitmap>()
    val solicitudes = mutableStateListOf<SolicitudReserva>()
    val usuarios = mutableStateListOf<Usuario>()

    // Feriados de Chile descargados desde la API externa (formato "yyyy-MM-dd")
    var feriadosChile: Set<String> = emptySet()

    // Contador interno para generar IDs temporales cuando se trabaja sin conexión
    private var contadorId = 1

    // Catálogo fijo de espacios físicos del establecimiento
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

    // Catálogo fijo de módulos horarios de 45 minutos (clases formales y talleres)
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

    // --- UTILIDADES DE FECHA Y HORA EN CHILE ---

    // Retorna un objeto Calendar configurado siempre con la hora actual de Chile
    fun calendarioChile(): Calendar = Calendar.getInstance(ZONA_CHILE, LOCALE_CL)

    // Formato estándar ISO ("yyyy-MM-dd") usado para guardar en BD y enviar al backend
    private fun formatoIsoChile(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = ZONA_CHILE }

    // Formato legible en español ("Viernes 27/11/2026") usado solo para mostrar en pantalla
    private fun formatoFechaChile(): SimpleDateFormat =
        SimpleDateFormat("EEEE dd/MM/yyyy", LOCALE_CL).apply { timeZone = ZONA_CHILE }

    // Devuelve la fecha de hoy en formato ISO (ej. "2026-11-27") para comparar vencimientos
    fun obtenerFechaHoyIso(): String {
        return formatoIsoChile().format(calendarioChile().time)
    }

    // Devuelve fecha y hora actual formateada para encabezados informativos en la UI
    fun obtenerFechaHoraActualTexto(): String {
        val fmt = SimpleDateFormat("EEEE dd/MM/yyyy • HH:mm 'hrs'", LOCALE_CL).apply {
            timeZone = ZONA_CHILE
        }
        return fmt.format(calendarioChile().time).replaceFirstChar { it.uppercase() }
    }

    /**
     * Devuelve la fecha limpia (ej. "Viernes 27/11/2026") quitando prefijos antiguos
     * en caso de que existan registros guardados con el formato anterior.
     */
    fun limpiarFechaBase(fechaTexto: String): String {
        return fechaTexto
            .removePrefix("HOY (Excepción Prioritaria) - ")
            .removePrefix("HOY - ")
            .removePrefix("Hoy - ")
            .removePrefix("Mañana - ")
            .trim()
    }

    /**
     * Traduce una fecha ISO ("2026-11-27") a un texto amigable para el usuario
     * agregando "HOY" o "Mañana" dinámicamente según la fecha real de Chile.
     */
    fun formatearFechaVisual(fechaIso: String, esPrioritaria: Boolean = false): String {
        // Si llega una fecha antigua que no está en formato ISO, usa el formateador de texto
        if (!fechaIso.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            return formatearFechaDinamica(fechaIso)
        }

        val fmtIso = formatoIsoChile()
        val fmtBonito = formatoFechaChile()
        val fechaDate = fmtIso.parse(fechaIso) ?: return fechaIso
        val textoBase = fmtBonito.format(fechaDate).replaceFirstChar { it.uppercase() }

        val cal = calendarioChile()
        val hoyIso = fmtIso.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val mananaIso = fmtIso.format(cal.time)

        return if (fechaIso == hoyIso && esPrioritaria) {
            "HOY (Excepción Prioritaria) - $textoBase"
        } else if (fechaIso == hoyIso) {
            "HOY • $textoBase"
        } else if (fechaIso == mananaIso) {
            "Mañana • $textoBase"
        } else {
            textoBase
        }
    }

    /**
     * Calcula dinámicamente si una fecha guardada corresponde a HOY o MAÑANA
     * según la fecha real de Chile (soporta tanto formato ISO como texto antiguo).
     */
    fun formatearFechaDinamica(fechaGuardada: String): String {
        if (fechaGuardada.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            return formatearFechaVisual(fechaGuardada, false)
        }

        val limpia = limpiarFechaBase(fechaGuardada)
        val fmt = formatoFechaChile()
        val cal = calendarioChile()

        val hoyTexto = fmt.format(cal.time).replaceFirstChar { it.uppercase() }
        if (limpia.equals(hoyTexto, ignoreCase = true)) {
            return "HOY • $limpia"
        }

        cal.add(Calendar.DAY_OF_YEAR, 1)
        val mananaTexto = fmt.format(cal.time).replaceFirstChar { it.uppercase() }
        if (limpia.equals(mananaTexto, ignoreCase = true)) {
            return "Mañana • $limpia"
        }

        return limpia
    }

    /**
     * Verifica si la fecha seleccionada es HOY en Chile y si la hora de término
     * del módulo (ej. "09:00") ya pasó, para impedir reservar bloques vencidos.
     */
    fun moduloYaPasoHoy(fechaSeleccionada: String, horaFin: String): Boolean {
        val ahora = calendarioChile()

        // Comprueba si la fecha elegida corresponde al día de hoy
        val esHoy = if (fechaSeleccionada.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            fechaSeleccionada == formatoIsoChile().format(ahora.time)
        } else {
            val limpia = limpiarFechaBase(fechaSeleccionada)
            val hoyTexto = formatoFechaChile().format(ahora.time).replaceFirstChar { it.uppercase() }
            limpia.equals(hoyTexto, ignoreCase = true)
        }

        // Si es un día futuro, el módulo aún no ha pasado
        if (!esHoy) return false

        // Separa "HH:mm" en hora y minuto para comparar con la hora actual de Chile
        val partes = horaFin.split(":")
        if (partes.size != 2) return false
        val horaModulo = partes[0].toIntOrNull() ?: return false
        val minModulo = partes[1].toIntOrNull() ?: return false

        val horaActual = ahora.get(Calendar.HOUR_OF_DAY)
        val minActual = ahora.get(Calendar.MINUTE)

        return (horaActual > horaModulo) || (horaActual == horaModulo && minActual >= minModulo)
    }

    /**
     * Genera los próximos 5 días hábiles reales en Chile (sin feriados ni fines de semana).
     * Retorna fechas en formato ISO ("yyyy-MM-dd") para guardarlas limpias en la BD.
     */
    fun obtenerFechasPosibles(
        incluirHoyPorUrgencia: Boolean,
        feriados: Set<String> = feriadosChile
    ): List<String> {
        val formatoIso = formatoIsoChile()
        val calendario = calendarioChile()
        val fechas = mutableListOf<String>()

        // Si el docente activa Reserva Prioritaria, se habilita la fecha de hoy al inicio
        if (incluirHoyPorUrgencia) {
            fechas.add(formatoIso.format(calendario.time))
        }

        // Busca los siguientes 5 días hábiles saltando sábados, domingos y feriados de la API
        var diasAgregados = 0
        var intentos = 0
        while (diasAgregados < 5 && intentos < 30) {
            intentos++
            calendario.add(Calendar.DAY_OF_YEAR, 1)
            val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)
            val fechaIso = formatoIso.format(calendario.time)

            val esFinDeSemana = diaSemana == Calendar.SATURDAY || diaSemana == Calendar.SUNDAY
            val esFeriado = feriados.contains(fechaIso)

            if (!esFinDeSemana && !esFeriado) {
                fechas.add(fechaIso)
                diasAgregados++
            }
        }
        return fechas
    }

    // --- CARGA INICIAL Y PERSISTENCIA LOCAL (SQLITE) ---

    /**
     * Carga usuarios y solicitudes desde SQLite al abrir la app.
     * Si la base local está vacía (primer inicio), crea los datos semilla por defecto.
     */
    fun inicializarDatos(context: Context) {
        if (inicializado) return
        val db = ColegioDatabaseHelper(context)

        // 1. Cargar usuarios desde SQLite o crear usuarios base
        val usuariosGuardados = db.listarUsuarios()
        if (usuariosGuardados.isNotEmpty()) {
            usuarios.clear()
            usuarios.addAll(usuariosGuardados)
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

        // Garantiza que siempre exista el administrador principal preaprobado
        if (usuarios.none { it.correo == "admin@colegio.cl" }) {
            usuarios.add(Usuario("admin@colegio.cl", "admin123", "ADMIN", true))
            guardarUsuarios(context)
        }

        // 2. Cargar solicitudes desde SQLite o crear 2 solicitudes de ejemplo
        val solicitudesGuardadas = db.listarSolicitudes(listaPuestos, listaModulos)
        if (solicitudesGuardadas.isNotEmpty()) {
            solicitudes.clear()
            solicitudes.addAll(solicitudesGuardadas)
            contadorId = (solicitudes.maxOfOrNull { it.id } ?: 0) + 1
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

    // Guarda la lista actual de solicitudes en SQLite para modo offline
    fun guardar(context: Context) {
        ColegioDatabaseHelper(context).reemplazarSolicitudes(solicitudes)
    }

    // Guarda la lista actual de usuarios en SQLite para modo offline
    fun guardarUsuarios(context: Context) {
        ColegioDatabaseHelper(context).reemplazarUsuarios(usuarios)
    }

    // Entrega un ID incremental para nuevas solicitudes locales
    fun generarId() = contadorId++
}