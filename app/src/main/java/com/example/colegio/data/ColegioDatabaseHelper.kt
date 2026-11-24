package com.example.colegio.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.colegio.model.*

class ColegioDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    NOMBRE_BD,
    null,
    VERSION_BD
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLA_USUARIOS (
                correo TEXT NOT NULL PRIMARY KEY COLLATE NOCASE,
                contrasena TEXT NOT NULL,
                rol TEXT NOT NULL,
                aprobado INTEGER NOT NULL CHECK (aprobado IN (0, 1)),
                sincronizado INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLA_SOLICITUDES (
                id INTEGER PRIMARY KEY,
                nombre_usuario TEXT NOT NULL,
                motivo_uso TEXT NOT NULL,
                puesto_id INTEGER NOT NULL,
                modulo_id INTEGER NOT NULL,
                requiere_flexibilidad INTEGER NOT NULL,
                motivo_flexibilidad TEXT NOT NULL,
                tiene_foto INTEGER NOT NULL,
                estado TEXT NOT NULL,
                fecha_reserva TEXT NOT NULL,
                es_prioritaria INTEGER NOT NULL,
                documento_adjunto TEXT NOT NULL,
                sincronizado INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_SOLICITUDES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        onCreate(db)
    }

    fun listarUsuarios(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        readableDatabase.query(TABLA_USUARIOS, null, null, null, null, null, null).use { c ->
            val colCorreo = c.getColumnIndexOrThrow("correo")
            val colPass = c.getColumnIndexOrThrow("contrasena")
            val colRol = c.getColumnIndexOrThrow("rol")
            val colAprobado = c.getColumnIndexOrThrow("aprobado")
            while (c.moveToNext()) {
                lista.add(
                    Usuario(
                        correo = c.getString(colCorreo),
                        contrasena = c.getString(colPass),
                        rol = c.getString(colRol),
                        aprobado = c.getInt(colAprobado) == 1
                    )
                )
            }
        }
        return lista
    }

    fun reemplazarUsuarios(usuarios: List<Usuario>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLA_USUARIOS, null, null)
            for (u in usuarios) {
                val values = ContentValues().apply {
                    put("correo", u.correo)
                    put("contrasena", u.contrasena)
                    put("rol", u.rol)
                    put("aprobado", if (u.aprobado) 1 else 0)
                    put("sincronizado", 0)
                }
                db.insert(TABLA_USUARIOS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun listarSolicitudes(
        listaPuestos: List<Puesto>,
        listaModulos: List<ModuloHorario>
    ): List<SolicitudReserva> {
        val lista = mutableListOf<SolicitudReserva>()
        readableDatabase.query(TABLA_SOLICITUDES, null, null, null, null, null, "id ASC").use { c ->
            val colId = c.getColumnIndexOrThrow("id")
            val colNombre = c.getColumnIndexOrThrow("nombre_usuario")
            val colMotivo = c.getColumnIndexOrThrow("motivo_uso")
            val colPuesto = c.getColumnIndexOrThrow("puesto_id")
            val colModulo = c.getColumnIndexOrThrow("modulo_id")
            val colFlex = c.getColumnIndexOrThrow("requiere_flexibilidad")
            val colMotFlex = c.getColumnIndexOrThrow("motivo_flexibilidad")
            val colFoto = c.getColumnIndexOrThrow("tiene_foto")
            val colEstado = c.getColumnIndexOrThrow("estado")
            val colFecha = c.getColumnIndexOrThrow("fecha_reserva")
            val colPrio = c.getColumnIndexOrThrow("es_prioritaria")
            val colDoc = c.getColumnIndexOrThrow("documento_adjunto")

            while (c.moveToNext()) {
                val puestoId = c.getInt(colPuesto)
                val moduloId = c.getInt(colModulo)
                lista.add(
                    SolicitudReserva(
                        id = c.getInt(colId),
                        nombreUsuario = c.getString(colNombre),
                        motivoUso = c.getString(colMotivo),
                        puesto = listaPuestos.find { it.id == puestoId } ?: listaPuestos[0],
                        modulo = listaModulos.find { it.id == moduloId } ?: listaModulos[0],
                        requiereFlexibilidad = c.getInt(colFlex) == 1,
                        motivoFlexibilidad = c.getString(colMotFlex),
                        tieneFotoAdjunta = c.getInt(colFoto) == 1,
                        estado = EstadoSolicitud.valueOf(c.getString(colEstado)),
                        fechaReserva = c.getString(colFecha),
                        esPrioritaria = c.getInt(colPrio) == 1,
                        documentoAdjunto = c.getString(colDoc)
                    )
                )
            }
        }
        return lista
    }

    fun reemplazarSolicitudes(solicitudes: List<SolicitudReserva>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLA_SOLICITUDES, null, null)
            for (s in solicitudes) {
                val values = ContentValues().apply {
                    put("id", s.id)
                    put("nombre_usuario", s.nombreUsuario)
                    put("motivo_uso", s.motivoUso)
                    put("puesto_id", s.puesto.id)
                    put("modulo_id", s.modulo.id)
                    put("requiere_flexibilidad", if (s.requiereFlexibilidad) 1 else 0)
                    put("motivo_flexibilidad", s.motivoFlexibilidad)
                    put("tiene_foto", if (s.tieneFotoAdjunta) 1 else 0)
                    put("estado", s.estado.name)
                    put("fecha_reserva", s.fechaReserva)
                    put("es_prioritaria", if (s.esPrioritaria) 1 else 0)
                    put("documento_adjunto", s.documentoAdjunto)
                    put("sincronizado", 0)
                }
                db.insert(TABLA_SOLICITUDES, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    companion object {
        private const val NOMBRE_BD = "colegio_marcela_paz.db"
        private const val VERSION_BD = 1
        const val TABLA_USUARIOS = "usuarios"
        const val TABLA_SOLICITUDES = "solicitudes"
    }
}