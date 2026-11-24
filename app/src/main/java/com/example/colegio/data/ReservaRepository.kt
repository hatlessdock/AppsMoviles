package com.example.colegio.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.colegio.data.remote.RetrofitClient
import com.example.colegio.data.remote.SolicitudDto
import com.example.colegio.data.remote.UsuarioDto
import com.example.colegio.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ReservaRepository(private val context: Context) {

    private val scopeRed = CoroutineScope(Dispatchers.IO)

    init {
        ColegioCacheLocal.inicializarDatos(context)
        sincronizarTodo()
    }

    /**
     * Descarga feriados de Chile (API externa) y sincroniza usuarios/solicitudes
     * desde Spring Boot. Si no hay conexión, mantiene los datos locales de SQLite.
     */
    fun sincronizarTodo() {
        scopeRed.launch {
            // 1. Consumo de API Externa: Feriados de Chile
            try {
                val anioActual = Calendar.getInstance().get(Calendar.YEAR)
                val feriados = RetrofitClient.feriadosApi.obtenerFeriadosChile(anioActual)
                ColegioCacheLocal.feriadosChile = feriados.map { it.date }.toSet()
                Log.d("API_EXTERNA", "Feriados cargados: ${feriados.size}")
            } catch (e: Exception) {
                Log.w("API_EXTERNA", "Sin conexión a API de feriados, usando solo fin de semana: ${e.message}")
            }

            // 2. Conexión con Microservicio propio en Spring Boot (Usuarios)
            try {
                val remotos = RetrofitClient.colegioApi.listarUsuarios()
                if (remotos.isNotEmpty()) {
                    val lista = remotos.map { Usuario(it.correo, it.contrasena, it.rol, it.aprobado) }
                    withContext(Dispatchers.Main) {
                        ColegioCacheLocal.usuarios.clear()
                        ColegioCacheLocal.usuarios.addAll(lista)
                    }
                    ColegioCacheLocal.guardarUsuarios(context)
                } else {
                    // Si la BD remota está vacía la primera vez, sube los usuarios iniciales
                    ColegioCacheLocal.usuarios.toList().forEach { u ->
                        RetrofitClient.colegioApi.crearUsuario(
                            UsuarioDto(u.correo, u.contrasena, u.rol, u.aprobado)
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("API_SPRING", "Modo offline SQLite para usuarios: ${e.message}")
            }

            // 3. Conexión con Microservicio propio en Spring Boot (Solicitudes)
            try {
                val remotas = RetrofitClient.colegioApi.listarSolicitudes()
                if (remotas.isNotEmpty()) {
                    val lista = remotas.map { dto ->
                        SolicitudReserva(
                            id = dto.id ?: ColegioCacheLocal.generarId(),
                            nombreUsuario = dto.nombreUsuario,
                            motivoUso = dto.motivoUso,
                            puesto = ColegioCacheLocal.listaPuestos.find { it.id == dto.puestoId }
                                ?: ColegioCacheLocal.listaPuestos[0],
                            modulo = ColegioCacheLocal.listaModulos.find { it.id == dto.moduloId }
                                ?: ColegioCacheLocal.listaModulos[0],
                            requiereFlexibilidad = dto.requiereFlexibilidad,
                            motivoFlexibilidad = dto.motivoFlexibilidad,
                            tieneFotoAdjunta = dto.tieneFotoAdjunta,
                            estado = runCatching { EstadoSolicitud.valueOf(dto.estado) }
                                .getOrDefault(EstadoSolicitud.PENDIENTE),
                            fechaReserva = dto.fechaReserva,
                            esPrioritaria = dto.esPrioritaria,
                            documentoAdjunto = dto.documentoAdjunto
                        )
                    }
                    withContext(Dispatchers.Main) {
                        ColegioCacheLocal.solicitudes.clear()
                        ColegioCacheLocal.solicitudes.addAll(lista)
                    }
                    ColegioCacheLocal.guardar(context)
                }
            } catch (e: Exception) {
                Log.w("API_SPRING", "Modo offline SQLite para solicitudes: ${e.message}")
            }
        }
    }

    fun login(correo: String, pass: String): Usuario? =
        ColegioCacheLocal.usuarios.find { it.correo == correo && it.contrasena == pass }

    fun registrar(correo: String, pass: String, rol: String): String {
        if (correo.isBlank() || pass.isBlank()) return "Llena todos los campos"
        if (ColegioCacheLocal.usuarios.any { it.correo == correo }) return "El correo ya existe"

        val nuevo = Usuario(correo.trim(), pass, rol, rol == "DOCENTE")
        ColegioCacheLocal.usuarios.add(nuevo)
        ColegioCacheLocal.guardarUsuarios(context)

        // Envía a Spring Boot en segundo plano (si no hay red, ya quedó guardado en SQLite)
        scopeRed.launch {
            try {
                RetrofitClient.colegioApi.crearUsuario(
                    UsuarioDto(nuevo.correo, nuevo.contrasena, nuevo.rol, nuevo.aprobado)
                )
            } catch (e: Exception) {
                Log.w("API_SPRING", "Usuario guardado solo en SQLite: ${e.message}")
            }
        }
        return "OK"
    }

    fun getAdminsPendientes() = ColegioCacheLocal.usuarios.filter { it.rol == "ADMIN" && !it.aprobado }

    fun aprobarAdmin(correo: String) {
        val i = ColegioCacheLocal.usuarios.indexOfFirst { it.correo == correo }
        if (i != -1) {
            ColegioCacheLocal.usuarios[i] = ColegioCacheLocal.usuarios[i].copy(aprobado = true)
            ColegioCacheLocal.guardarUsuarios(context)

            scopeRed.launch {
                try {
                    RetrofitClient.colegioApi.aprobarAdmin(correo)
                } catch (e: Exception) {
                    Log.w("API_SPRING", "Aprobación admin guardada solo en SQLite: ${e.message}")
                }
            }
        }
    }

    fun obtenerPuestos() = ColegioCacheLocal.listaPuestos
    fun obtenerModulos() = ColegioCacheLocal.listaModulos
    fun obtenerSolicitudes() = ColegioCacheLocal.solicitudes
    fun obtenerFoto(id: Int): Bitmap? = ColegioCacheLocal.fotosCapturadas[id]
    fun obtenerFechasPosibles(esPrioritaria: Boolean) = ColegioCacheLocal.obtenerFechasPosibles(esPrioritaria)

    fun estadoPuesto(puestoId: Int, moduloId: Int, fecha: String = ""): EstadoPuesto {
        val aprobada = ColegioCacheLocal.solicitudes.find {
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
        val idLocal = ColegioCacheLocal.generarId()
        if (foto != null) ColegioCacheLocal.fotosCapturadas[idLocal] = foto

        val nueva = SolicitudReserva(
            id = idLocal,
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
        ColegioCacheLocal.solicitudes.add(nueva)
        ColegioCacheLocal.guardar(context)

        // Envía a Spring Boot y sincroniza el ID real de la BD
        scopeRed.launch {
            try {
                val guardadaRemota = RetrofitClient.colegioApi.crearSolicitud(
                    SolicitudDto(
                        id = null,
                        nombreUsuario = nueva.nombreUsuario,
                        motivoUso = nueva.motivoUso,
                        puestoId = nueva.puesto.id,
                        moduloId = nueva.modulo.id,
                        requiereFlexibilidad = nueva.requiereFlexibilidad,
                        motivoFlexibilidad = nueva.motivoFlexibilidad,
                        tieneFotoAdjunta = nueva.tieneFotoAdjunta,
                        estado = nueva.estado.name,
                        fechaReserva = nueva.fechaReserva,
                        esPrioritaria = nueva.esPrioritaria,
                        documentoAdjunto = nueva.documentoAdjunto
                    )
                )
                val idRemoto = guardadaRemota.id
                if (idRemoto != null && idRemoto != idLocal) {
                    withContext(Dispatchers.Main) {
                        val idx = ColegioCacheLocal.solicitudes.indexOfFirst { it.id == idLocal }
                        if (idx != -1) {
                            ColegioCacheLocal.solicitudes[idx] =
                                ColegioCacheLocal.solicitudes[idx].copy(id = idRemoto)
                        }
                        if (foto != null) {
                            ColegioCacheLocal.fotosCapturadas.remove(idLocal)
                            ColegioCacheLocal.fotosCapturadas[idRemoto] = foto
                        }
                    }
                    ColegioCacheLocal.guardar(context)
                }
            } catch (e: Exception) {
                Log.w("API_SPRING", "Solicitud guardada solo en SQLite: ${e.message}")
            }
        }
        return true
    }

    fun aprobarSolicitud(id: Int) {
        val i = ColegioCacheLocal.solicitudes.indexOfFirst { it.id == id }
        if (i != -1) {
            val sol = ColegioCacheLocal.solicitudes[i]
            ColegioCacheLocal.solicitudes[i] = sol.copy(estado = EstadoSolicitud.APROBADA)
            for (j in ColegioCacheLocal.solicitudes.indices) {
                val s = ColegioCacheLocal.solicitudes[j]
                if (s.id != id && s.puesto.id == sol.puesto.id && s.modulo.id == sol.modulo.id && s.fechaReserva == sol.fechaReserva && s.estado == EstadoSolicitud.PENDIENTE) {
                    ColegioCacheLocal.solicitudes[j] = s.copy(estado = EstadoSolicitud.RECHAZADA)
                }
            }
            ColegioCacheLocal.guardar(context)

            scopeRed.launch {
                try {
                    RetrofitClient.colegioApi.cambiarEstado(id, "APROBADA")
                } catch (e: Exception) {
                    Log.w("API_SPRING", "Aprobación guardada solo en SQLite: ${e.message}")
                }
            }
        }
    }

    fun rechazarSolicitud(id: Int) {
        val i = ColegioCacheLocal.solicitudes.indexOfFirst { it.id == id }
        if (i != -1) {
            ColegioCacheLocal.solicitudes[i] = ColegioCacheLocal.solicitudes[i].copy(estado = EstadoSolicitud.RECHAZADA)
            ColegioCacheLocal.guardar(context)

            scopeRed.launch {
                try {
                    RetrofitClient.colegioApi.cambiarEstado(id, "RECHAZADA")
                } catch (e: Exception) {
                    Log.w("API_SPRING", "Rechazo guardado solo en SQLite: ${e.message}")
                }
            }
        }
    }
}