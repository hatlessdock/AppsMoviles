package com.example.colegio.data.remote

import retrofit2.http.*

data class UsuarioDto(
    val correo: String,
    val contrasena: String,
    val rol: String,
    val aprobado: Boolean
)

data class SolicitudDto(
    val id: Int? = null,
    val nombreUsuario: String,
    val motivoUso: String,
    val puestoId: Int,
    val moduloId: Int,
    val requiereFlexibilidad: Boolean,
    val motivoFlexibilidad: String,
    val tieneFotoAdjunta: Boolean,
    val estado: String,
    val fechaReserva: String,
    val esPrioritaria: Boolean,
    val documentoAdjunto: String
)

interface ColegioApiService {
    @GET("api/usuarios")
    suspend fun listarUsuarios(): List<UsuarioDto>

    @POST("api/usuarios")
    suspend fun crearUsuario(@Body usuario: UsuarioDto): UsuarioDto

    @PUT("api/usuarios/{correo}/aprobar")
    suspend fun aprobarAdmin(@Path("correo") correo: String): UsuarioDto

    @GET("api/solicitudes")
    suspend fun listarSolicitudes(): List<SolicitudDto>

    @POST("api/solicitudes")
    suspend fun crearSolicitud(@Body solicitud: SolicitudDto): SolicitudDto

    @PUT("api/solicitudes/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Int,
        @Query("estado") estado: String
    ): SolicitudDto
}