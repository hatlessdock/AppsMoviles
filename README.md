# Sistema de Reservas - Escuela Escritora Marcela Paz

## 👥 Integrantes del Equipo
* **Estudiante 1:** Sergio Cerda
* **Estudiante 2:** César Gálvez
* **Asignatura:** Desarrollo de Aplicaciones Móviles
* **Sección:** 005D
* **Profesor:** Fabián Carvacho Guerrero

## 📝 Descripción del Proyecto
Aplicación móvil en Android (**Kotlin + Jetpack Compose**) conectada a una **API REST en Java (Spring Boot)**, diseñada para gestionar la reserva de espacios físicos (canchas, laboratorios, biblioteca) en la Escuela Escritora Marcela Paz. Implementa reglas de negocio estrictas con bloques horarios de 45 minutos, validación de días feriados y anticipación mínima de 24 horas.

## 🚀 Funcionalidades Principales
* **Arquitectura MVVM + Cliente-Servidor:** Separación de vistas, `ViewModel`, repositorios, caché local y consumo de API REST con Retrofit.
* **Persistencia Híbrida (Local + Remota):** Sincronización con backend Spring Boot y respaldo local mediante SQLite / `SharedPreferences`.
* **Reglas de Negocio y Validaciones:** Bloqueo automático de solicitudes en conflicto al aprobar una reserva y consulta de feriados.
* **Recursos Nativos Integrados:**
  * **Cámara:** Captura de evidencia pedagógica (planificaciones o esquemas de montaje).
  * **Notificaciones del Sistema:** Alertas al enviar o aprobar solicitudes (compatible con Android 13+).
  * **Vibración Háptica (`Vibrator`):** Retroalimentación física de éxito o error.

## 🛠️ Estructura del Repositorio
* `app/` — Aplicación móvil Android (Kotlin + Jetpack Compose).
* `api/` — Backend RESTful (Java 17 + Spring Boot).