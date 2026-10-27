package com.example.colegio.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.colegio.data.ReservaRepository
import com.example.colegio.model.EstadoPuesto

class ReservaViewModel(private val repository: ReservaRepository) : ViewModel() {
    var nombreUsuario by mutableStateOf(""); private set
    var motivoUso by mutableStateOf(""); private set
    var puestoIndex by mutableIntStateOf(0); private set
    var moduloIndex by mutableIntStateOf(0); private set
    var fechaIndex by mutableIntStateOf(0); private set

    var requiereFlexibilidad by mutableStateOf(false); private set
    var motivoFlexibilidad by mutableStateOf(""); private set

    var esPrioritaria by mutableStateOf(false); private set
    var motivoPrioridad by mutableStateOf(""); private set

    var fotoCapturada by mutableStateOf<Bitmap?>(null); private set
    var documentoAdjunto by mutableStateOf(""); private set

    val opcionesEvidencia = listOf(
        "Planificación de Clase / Guía",
        "Esquema de Montaje (Sillas/Mesas)",
        "Material Específico / Cables",
        "Autorización Dirección / UTP (Urgencia)"
    )
    var tipoEvidenciaIndex by mutableIntStateOf(0); private set

    var errorNombre by mutableStateOf<String?>(null); private set
    var errorMotivoUso by mutableStateOf<String?>(null); private set
    var errorFlexibilidad by mutableStateOf<String?>(null); private set
    var errorPrioridad by mutableStateOf<String?>(null); private set
    var errorFotoObligatoria by mutableStateOf<String?>(null); private set

    fun obtenerFechasDisponibles(): List<String> = repository.obtenerFechasPosibles(esPrioritaria)

    fun onNombreChange(v: String) {
        nombreUsuario = v
        errorNombre = if (v.trim().length < 3) "Mínimo 3 letras" else null
    }

    fun onMotivoUsoChange(v: String) {
        motivoUso = v
        errorMotivoUso = if (v.trim().length < 5) "Detalla la actividad" else null
    }

    fun onPuestoSelected(i: Int) {
        puestoIndex = i
        if (!obtenerPuestos()[i].permiteFlexibilidad) {
            requiereFlexibilidad = false
            motivoFlexibilidad = ""
            errorFlexibilidad = null
            validarRespaldo()
        }
    }

    fun onModuloSelected(i: Int) { moduloIndex = i }
    fun onFechaSelected(i: Int) { fechaIndex = i }
    fun onTipoEvidenciaSelected(i: Int) { tipoEvidenciaIndex = i }

    fun onFlexibilidadCheckChange(c: Boolean) {
        requiereFlexibilidad = c
        if (!c) errorFlexibilidad = null
        validarRespaldo()
    }

    fun onMotivoFlexibilidadChange(v: String) {
        motivoFlexibilidad = v
        if (requiereFlexibilidad) {
            errorFlexibilidad = if (v.trim().length < 5) "Explica la flexibilidad" else null
        }
    }

    fun onPrioritariaCheckChange(c: Boolean) {
        esPrioritaria = c
        fechaIndex = 0
        if (!c) {
            motivoPrioridad = ""
            errorPrioridad = null
        }
        validarRespaldo()
    }

    fun onMotivoPrioridadChange(v: String) {
        motivoPrioridad = v
        if (esPrioritaria) {
            errorPrioridad = if (v.trim().length < 5) "Explica la emergencia o prioridad" else null
        }
    }

    fun onFotoCapturada(b: Bitmap?) {
        fotoCapturada = b
        validarRespaldo()
    }

    fun onDocumentoSeleccionado(nombreArchivo: String) {
        documentoAdjunto = nombreArchivo
        validarRespaldo()
    }

    private fun validarRespaldo() {
        val tieneRespaldo = (fotoCapturada != null) || documentoAdjunto.isNotBlank()
        errorFotoObligatoria = if ((esPrioritaria || requiereFlexibilidad) && !tieneRespaldo) {
            "📎 RESPALDO OBLIGATORIO: Debes tomar una FOTO o adjuntar un DOCUMENTO."
        } else {
            null
        }
    }

    fun enviarSolicitud(): Boolean {
        onNombreChange(nombreUsuario)
        onMotivoUsoChange(motivoUso)
        onMotivoFlexibilidadChange(motivoFlexibilidad)
        onMotivoPrioridadChange(motivoPrioridad)
        validarRespaldo()

        if (errorNombre != null || errorMotivoUso != null || errorFlexibilidad != null || errorPrioridad != null || errorFotoObligatoria != null) {
            return false
        }

        val fechas = obtenerFechasDisponibles()
        val fechaSeleccionada = fechas.getOrElse(fechaIndex) { fechas.first() }

        val detallesExtra = mutableListOf<String>()
        if (esPrioritaria) detallesExtra.add("URGENCIA: $motivoPrioridad")
        if (requiereFlexibilidad) detallesExtra.add("FLEX: $motivoFlexibilidad")
        if (esPrioritaria || requiereFlexibilidad) {
            detallesExtra.add("Tipo: ${opcionesEvidencia[tipoEvidenciaIndex]}")
        }

        val ok = repository.crearSolicitud(
            nombre = nombreUsuario,
            motivo = motivoUso,
            puesto = obtenerPuestos()[puestoIndex],
            modulo = obtenerModulos()[moduloIndex],
            fecha = fechaSeleccionada,
            reqFlex = requiereFlexibilidad || esPrioritaria,
            motFlex = detallesExtra.joinToString(" | "),
            esPrioritaria = esPrioritaria,
            foto = fotoCapturada,
            documento = documentoAdjunto
        )

        if (ok) {
            motivoUso = ""
            requiereFlexibilidad = false
            motivoFlexibilidad = ""
            esPrioritaria = false
            motivoPrioridad = ""
            fotoCapturada = null
            documentoAdjunto = ""
            fechaIndex = 0
            tipoEvidenciaIndex = 0
            errorMotivoUso = null
            errorFlexibilidad = null
            errorPrioridad = null
            errorFotoObligatoria = null
        }
        return ok
    }

    fun obtenerPuestos() = repository.obtenerPuestos()
    fun obtenerModulos() = repository.obtenerModulos()
    fun obtenerSolicitudes() = repository.obtenerSolicitudes()
    fun obtenerEstadoEnModulo(pId: Int, mId: Int): EstadoPuesto {
        val fechas = obtenerFechasDisponibles()
        val fechaActual = fechas.getOrElse(fechaIndex) { fechas.first() }
        return repository.estadoPuesto(pId, mId, fechaActual)
    }
    fun obtenerFoto(id: Int) = repository.obtenerFoto(id)
    fun aprobarSolicitud(id: Int) = repository.aprobarSolicitud(id)
    fun rechazarSolicitud(id: Int) = repository.rechazarSolicitud(id)
    fun adminsPendientes() = repository.getAdminsPendientes()
    fun aprobarAdmin(c: String) = repository.aprobarAdmin(c)
}