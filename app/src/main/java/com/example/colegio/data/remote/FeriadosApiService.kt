package com.example.colegio.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

data class FeriadoDto(
    val date: String,      // Ej: "2026-09-18"
    val localName: String  // Ej: "Independencia Nacional"
)

interface FeriadosApiService {
    @GET("api/v3/PublicHolidays/{anio}/CL")
    suspend fun obtenerFeriadosChile(@Path("anio") anio: Int): List<FeriadoDto>
}