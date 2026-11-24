package com.example.colegio

import com.example.colegio.logic.ValidadorReserva
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ValidadorReservaTest {

    @Test
    fun nombreMenorA3Caracteres_retornaError() {
        val error = ValidadorReserva.validarNombre("Jo")
        assertNotNull(error)
    }

    @Test
    fun nombreValido_noRetornaError() {
        val error = ValidadorReserva.validarNombre("Prof. Carlos Soto")
        assertNull(error)
    }

    @Test
    fun motivoMenorA5Caracteres_retornaError() {
        val error = ValidadorReserva.validarMotivo("8vo")
        assertNotNull(error)
    }

    @Test
    fun reservaPrioritariaSinFotoNiDocumento_retornaError() {
        val error = ValidadorReserva.validarRespaldoUrgencia(
            esPrioritaria = true,
            motivoUrgencia = "Filtración de agua en sala",
            tieneFoto = false,
            documentoAdjunto = ""
        )
        assertNotNull(error)
    }

    @Test
    fun reservaPrioritariaConDocumento_esValida() {
        val error = ValidadorReserva.validarRespaldoUrgencia(
            esPrioritaria = true,
            motivoUrgencia = "Filtración de agua en sala",
            tieneFoto = false,
            documentoAdjunto = "autorizacion_utp.pdf"
        )
        assertNull(error)
    }

    @Test
    fun fechaFeriadoLegal_esRechazadaComoDiaHabil() {
        val feriados = setOf("2026-09-18", "2026-09-19")
        val esHabil = ValidadorReserva.esDiaHabil(
            diaSemanaCalendar = Calendar.FRIDAY,
            fechaIso = "2026-09-18",
            feriadosChile = feriados
        )
        assertFalse(esHabil)
    }

    @Test
    fun fechaFinDeSemana_esRechazadaComoDiaHabil() {
        val esHabil = ValidadorReserva.esDiaHabil(
            diaSemanaCalendar = Calendar.SATURDAY,
            fechaIso = "2026-11-28",
            feriadosChile = emptySet()
        )
        assertFalse(esHabil)
    }

    @Test
    fun mismoPuestoModuloYFechaAprobada_detectaChoque() {
        val aprobadas = listOf(
            Triple(1, 2, "Mañana - Miércoles 25/11/2026")
        )
        val choque = ValidadorReserva.hayChoqueHorario(
            puestoId = 1,
            moduloId = 2,
            fechaReserva = "Mañana - Miércoles 25/11/2026",
            reservasAprobadas = aprobadas
        )
        assertTrue(choque)
    }

    @Test
    fun distintoModuloEnMismoPuesto_noDetectaChoque() {
        val aprobadas = listOf(
            Triple(1, 2, "Mañana - Miércoles 25/11/2026")
        )
        val choque = ValidadorReserva.hayChoqueHorario(
            puestoId = 1,
            moduloId = 3,
            fechaReserva = "Mañana - Miércoles 25/11/2026",
            reservasAprobadas = aprobadas
        )
        assertFalse(choque)
    }
}