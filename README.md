# Sistema de Reservas - Escuela Escritora Marcela Paz

## 👥 Integrantes del Equipo
* **Estudiante 1:** Sergio Cerda
* **Estudiante 2:** Cesar Galvez
* **Asignatura:** Desarrollo de Aplicaciones Móviles
* **Sección:** 005D
* **Profesor: Fabian Carvacho Guerrero**

## 📝 Descripción del Proyecto
Aplicación móvil en Android (Kotlin + Jetpack Compose) diseñada para gestionar la reserva de espacios físicos (canchas, laboratorios, biblioteca) en la Escuela Escritora Marcela Paz. Implementa reglas de negocio estrictas con bloques de 45 minutos y permisos de anticipación de 24 horas.

## 🚀 Funcionalidades Principales
* **Arquitectura MVVM:** Separación de lógica de vistas y validaciones por campo.
* **Persistencia Local:** Almacenamiento de solicitudes utilizando SharedPreferences y JSONArray de forma nativa.
* **Recursos Nativos Integrados:**
    * Uso de **Cámara** para adjuntar evidencia pedagógica (Planificaciones / Esquema de montaje).
    * **Notificaciones del Sistema** al enviar o aprobar solicitudes (Compatible con Android 13 Tiramisu).
    * **Vibración háptica (Vibrator)** para retroalimentación de éxito/error.
* **Roles de Usuario:** Interfaz para Profesor (solicitante) y Administrador (aprobación/bloqueo de espacios).
