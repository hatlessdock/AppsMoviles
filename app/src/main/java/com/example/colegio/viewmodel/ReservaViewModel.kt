package com.example.colegio.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.colegio.data.ReservaRepository
import com.example.colegio.model.EstadoPuesto
import com.example.colegio.model.ModuloHorario
import com.example.colegio.model.Puesto
import com.example.colegio.model.SolicitudReserva

class ReservaViewModel(private val repository: ReservaRepository) : ViewModel() {

    var nombreUsuario by mutableStateOf("")
        private set
    var motivoUso by mutableStateOf("")
        private set
    var puestoIndex by mutableIntStateOf(0)
        private set
    var moduloIndex by mutableIntStateOf(0)
        private set
    var requiereFlexibilidad by mutableStateOf(false)
        private set
    var motivoFlexibilidad by mutableStateOf("")
        private set
    var fotoCapturada by mutableStateOf<Bitmap?>(null)
        private set

    val opcionesEvidencia = listOf(
        "Planificación de Clase / Guía",
        "Esquema de Montaje (Sillas/Mesas)",
        "Material Específico / Cables"
    )
    var tipoEvidenciaIndex by mutableIntStateOf(0)
        private set

    var errorNombre by mutableStateOf<String?>(null)
        private set
    var errorMotivoUso by mutableStateOf<String?>(null)
        private set
    var errorFlexibilidad by mutableStateOf<String?>(null)
        private set
    var errorFotoObligatoria by mutableStateOf<String?>(null)
        private set

    fun onNombreChange(nuevoNombre: String) {
        nombreUsuario = nuevoNombre
        errorNombre = if (nuevoNombre.trim().length < 3) "Mínimo 3 letras" else null
    }

    fun onMotivoUsoChange(nuevoMotivo: String) {
        motivoUso = nuevoMotivo
        errorMotivoUso = if (nuevoMotivo.trim().length < 5) "Detalla la actividad" else null
    }

    fun onPuestoSelected(index: Int) {
        puestoIndex = index
        if (!obtenerPuestos()[index].permiteFlexibilidad) {
            requiereFlexibilidad = false
            motivoFlexibilidad = ""
            errorFlexibilidad = null
            errorFotoObligatoria = null
        }
    }

    fun onModuloSelected(index: Int) { moduloIndex = index }
    fun onTipoEvidenciaSelected(index: Int) { tipoEvidenciaIndex = index }

    fun onFlexibilidadCheckChange(checked: Boolean) {
        requiereFlexibilidad = checked
        if (!checked) {
            errorFlexibilidad = null
            errorFotoObligatoria = null
        } else {
            validarFoto()
        }
    }

    fun onMotivoFlexibilidadChange(nuevoMotivo: String) {
        motivoFlexibilidad = nuevoMotivo
        if (requiereFlexibilidad) {
            errorFlexibilidad = if (nuevoMotivo.trim().length < 5) "Explica la flexibilidad" else null
        }
    }

    fun onFotoCapturada(bitmap: Bitmap?) {
        fotoCapturada = bitmap
        validarFoto()
    }

    private fun validarFoto() {
        errorFotoObligatoria = if (requiereFlexibilidad && fotoCapturada == null) {
            "📷 FOTO OBLIGATORIA: Adjunte el respaldo seleccionado para solicitar flexibilidad."
        } else null
    }

    fun enviarSolicitud(): Boolean {
        onNombreChange(nombreUsuario)
        onMotivoUsoChange(motivoUso)
        onMotivoFlexibilidadChange(motivoFlexibilidad)
        validarFoto()

        if (errorNombre != null || errorMotivoUso != null || errorFlexibilidad != null || errorFotoObligatoria != null) {
            return false
        }

        val motivoFlexFinal = if (requiereFlexibilidad) {
            "[$motivoFlexibilidad] - Evidencia: ${opcionesEvidencia[tipoEvidenciaIndex]}"
        } else ""

        val ok = repository.crearSolicitud(
            nombreUsuario, motivoUso, obtenerPuestos()[puestoIndex],
            obtenerModulos()[moduloIndex], requiereFlexibilidad, motivoFlexFinal, fotoCapturada
        )

        if (ok) {
            motivoUso = ""
            requiereFlexibilidad = false
            motivoFlexibilidad = ""
            fotoCapturada = null
            tipoEvidenciaIndex = 0
            errorMotivoUso = null
            errorFlexibilidad = null
            errorFotoObligatoria = null
        }
        return ok
    }

    fun obtenerPuestos(): List<Puesto> = repository.obtenerPuestos()
    fun obtenerModulos(): List<ModuloHorario> = repository.obtenerModulos()
    fun obtenerSolicitudes(): List<SolicitudReserva> = repository.obtenerTodasLasSolicitudes()
    fun obtenerEstadoEnModulo(pId: Int, mId: Int): EstadoPuesto = repository.obtenerEstadoPuestoEnModulo(pId, mId)
    fun obtenerFoto(id: Int): Bitmap? = repository.obtenerFoto(id)
    fun aprobarSolicitud(id: Int) = repository.aprobarSolicitud(id)
    fun rechazarSolicitud(id: Int) = repository.rechazarSolicitud(id)
}