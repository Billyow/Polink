# Contrato Oficial de API — Polink MVP (`API_CONTRACT.md`)

Este documento define el contrato vivo de los endpoints REST del backend de **Polink**. Todo agente o desarrollador que agregue o modifique un endpoint debe actualizar este documento y mantener las anotaciones de Swagger/OpenAPI sincronizadas.

---

## 1. Convenciones Globales

- **Base URL:** `/api/v1`
- **Swagger UI:** `/swagger-ui.html`
- **OpenAPI Spec:** `/v3/api-docs`
- **Autenticación:** Header `Authorization: Bearer <JWT_TOKEN>` en endpoints protegidos.
- **Formato de Fechas y Horas:** ISO-8601 (`YYYY-MM-DD`, `HH:mm:ss`, `YYYY-MM-DDTHH:mm:ssZ`).

### Formato Estándar de Error (`ApiErrorResponse`)
```json
{
  "timestamp": "2026-11-04T20:15:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Descripción legible del error",
  "path": "/api/v1/recurso",
  "validationErrors": {
    "campo": "Mensaje de validación"
  }
}
```

---

## 2. Módulo Notification (`com.polink.notification`)

### 2.1 Listar notificaciones del usuario autenticado
- **Método y Ruta:** `GET /api/v1/notifications`
- **Roles Permitidos:** Cualquier usuario autenticado (`ROLE_STUDENT`, `ROLE_TUTOR`, `ROLE_SUPERVISOR`, `ROLE_ADMIN`)
- **Query Params:** `page` (default `0`), `size` (default `20`)
- **Respuesta Exitosa (`200 OK`):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Solicitud de asesoría aprobada",
      "message": "Tu oferta de Cálculo Diferencial ha sido aprobada por un supervisor.",
      "type": "OFFER_APPROVED",
      "referenceId": "770e8400-e29b-41d4-a716-446655440111",
      "read": false,
      "createdAt": "2026-11-04T20:10:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### 2.2 Obtener conteo de notificaciones no leídas (Polling cada 45–60s)
- **Método y Ruta:** `GET /api/v1/notifications/unread-count`
- **Roles Permitidos:** Cualquier usuario autenticado
- **Respuesta Exitosa (`200 OK`):**
```json
{
  "unreadCount": 3
}
```

### 2.3 Marcar una notificación como leída
- **Método y Ruta:** `PATCH /api/v1/notifications/{id}/read`
- **Roles Permitidos:** Usuario autenticado propietario de la notificación
- **Respuesta Exitosa (`200 OK`):** `NotificationResponseDto` con `"read": true`.

### 2.4 Marcar todas las notificaciones como leídas
- **Método y Ruta:** `PATCH /api/v1/notifications/read-all`
- **Roles Permitidos:** Cualquier usuario autenticado
- **Respuesta Exitosa (`204 No Content`)**

---

## 3. Módulo Audit (`com.polink.audit`)

### 3.1 Consultar registros inmutables de auditoría
- **Método y Ruta:** `GET /api/v1/admin/audit-logs`
- **Roles Permitidos:** `ROLE_ADMIN`
- **Query Params:** `actionType` (opcional), `actorUserId` (opcional, `UUID`), `page` (default `0`), `size` (default `20`)
- **Respuesta Exitosa (`200 OK`):** `PagedResponseDto<AuditLogResponseDto>`

---

## 4. Módulo Auth (`com.polink.auth`)

### 4.1 Registrar cuenta de estudiante
- **Método y Ruta:** `POST /api/v1/auth/register/student`
- **Roles Permitidos:** Público
- **Request Body (`StudentRegisterRequestDto`):**
```json
{
  "fullName": "Andrés Pérez",
  "email": "andres@polink.edu.co",
  "password": "Estudiante2026!"
}
```
- **Respuesta Exitosa (`201 Created` - `AuthResponseDto`):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresInMs": 86400000,
  "user": {
    "id": "11111111-2222-3333-4444-555555555555",
    "fullName": "Andrés Pérez",
    "email": "andres@polink.edu.co",
    "status": "ACTIVE",
    "roles": ["ROLE_STUDENT"],
    "createdAt": "2026-11-04T23:25:00Z"
  }
}
```
- **Errores:** `400 Bad Request` (validación), `409 Conflict` (correo duplicado).

### 4.2 Iniciar sesión
- **Método y Ruta:** `POST /api/v1/auth/login`
- **Roles Permitidos:** Público
- **Request Body (`LoginRequestDto`):**
```json
{
  "email": "andres@polink.edu.co",
  "password": "Estudiante2026!"
}
```
- **Respuesta Exitosa (`200 OK`):** `AuthResponseDto`
- **Errores:** `401 Unauthorized` (credenciales inválidas), `403 Forbidden` (cuenta `SUSPENDED`).

### 4.3 Solicitar recuperación de contraseña
- **Método y Ruta:** `POST /api/v1/auth/forgot-password`
- **Roles Permitidos:** Público
- **Request Body (`ForgotPasswordRequestDto`):**
```json
{
  "email": "andres@polink.edu.co"
}
```
- **Respuesta Exitosa (`200 OK` - `MessageResponseDto`):**
```json
{
  "message": "Si el correo se encuentra registrado, recibirás instrucciones para restablecer tu contraseña"
}
```

### 4.4 Restablecer contraseña con token
- **Método y Ruta:** `POST /api/v1/auth/reset-password`
- **Roles Permitidos:** Público
- **Request Body (`ResetPasswordRequestDto`):**
```json
{
  "token": "64_HEX_CHARACTER_RESET_TOKEN",
  "newPassword": "NuevaClaveSegura2026!"
}
```
- **Respuesta Exitosa (`200 OK` - `MessageResponseDto`):**
```json
{
  "message": "Tu contraseña ha sido restablecida exitosamente"
}
```
- **Errores:** `400 Bad Request` (token inválido, ya usado o expirado).

---

## 5. Módulo User (`com.polink.user`)

### 5.1 Obtener perfil del usuario autenticado
- **Método y Ruta:** `GET /api/v1/users/me`
- **Roles Permitidos:** Cualquier usuario autenticado
- **Respuesta Exitosa (`200 OK`):** `UserResponseDto`

### 5.2 Listar usuarios (Administración)
- **Método y Ruta:** `GET /api/v1/admin/users`
- **Roles Permitidos:** `ROLE_ADMIN`
- **Query Params:** `status` (opcional: `ACTIVE` | `SUSPENDED`), `page` (default `0`), `size` (default `20`)
- **Respuesta Exitosa (`200 OK`):** `PagedResponseDto<UserResponseDto>`

### 5.3 Crear cuenta de Supervisor
- **Método y Ruta:** `POST /api/v1/admin/supervisors`
- **Roles Permitidos:** `ROLE_ADMIN`
- **Request Body (`CreateSupervisorRequestDto`):**
```json
{
  "fullName": "Carlos Ruiz",
  "email": "carlos.supervisor@polink.edu.co",
  "password": "Supervisor2026!"
}
```
- **Respuesta Exitosa (`201 Created`):** `UserResponseDto` con `"roles": ["ROLE_SUPERVISOR"]`.
- **Errores:** `409 Conflict` (correo ya registrado).

### 5.4 Cambiar estado de cuenta de usuario (`ACTIVE` / `SUSPENDED`)
- **Método y Ruta:** `PATCH /api/v1/admin/users/{id}/status`
- **Roles Permitidos:** `ROLE_ADMIN`
- **Request Body (`UpdateUserStatusRequestDto`):**
```json
{
  "status": "SUSPENDED",
  "reason": "Incumplimiento reiterado de las normas de la plataforma"
}
```
- **Respuesta Exitosa (`200 OK`):** `UserResponseDto` actualizado.
- **Errores:** `400 Bad Request` (intento de auto-suspensión), `404 Not Found`.

---

## 6. Módulo Catalog (`com.polink.catalog`)

### 6.1 Listar ramas académicas activas
- **Método y Ruta:** `GET /api/v1/catalog/branches`
- **Roles Permitidos:** Público
- **Respuesta Exitosa (`200 OK`):**
```json
[
  {
    "id": "20000000-0000-0000-0000-000000000001",
    "name": "Ciencias Básicas",
    "description": "Asignaturas fundamentales de matemáticas, física y estadística.",
    "active": true,
    "createdAt": "2026-11-04T20:00:00Z"
  }
]
```

### 6.2 Listar asignaturas activas
- **Método y Ruta:** `GET /api/v1/catalog/subjects`
- **Roles Permitidos:** Público
- **Query Params:** `branchId` (opcional, `UUID`)
- **Respuesta Exitosa (`200 OK`):**
```json
[
  {
    "id": "30000000-0000-0000-0000-000000000001",
    "branchId": "20000000-0000-0000-0000-000000000001",
    "branchName": "Ciencias Básicas",
    "code": "CB-101",
    "name": "Cálculo Diferencial",
    "active": true,
    "createdAt": "2026-11-04T20:00:00Z"
  }
]
```

### 6.3 Crear rama académica
- **Método y Ruta:** `POST /api/v1/admin/catalog/branches`
- **Roles Permitidos:** `ROLE_ADMIN`
- **Request Body (`BranchRequestDto`):**
```json
{
  "name": "Ciencias de Datos e IA",
  "description": "Aprendizaje automático y analítica avanzada.",
  "active": true
}
```
- **Respuesta Exitosa (`201 Created`):** `BranchResponseDto`

### 6.4 Actualizar rama académica
- **Método y Ruta:** `PUT /api/v1/admin/catalog/branches/{id}`
- **Roles Permitidos:** `ROLE_ADMIN`
- **Request Body (`BranchRequestDto`)**
- **Respuesta Exitosa (`200 OK`):** `BranchResponseDto`

### 6.5 Crear asignatura
- **Método y Ruta:** `POST /api/v1/admin/catalog/subjects`
- **Roles Permitidos:** `ROLE_ADMIN`
- **Request Body (`SubjectRequestDto`):**
```json
{
  "branchId": "20000000-0000-0000-0000-000000000001",
  "code": "CB-105",
  "name": "Ecuaciones Diferenciales",
  "active": true
}
```
- **Respuesta Exitosa (`201 Created`):** `SubjectResponseDto`

### 6.6 Actualizar asignatura
- **Método y Ruta:** `PUT /api/v1/admin/catalog/subjects/{id}`
- **Roles Permitidos:** `ROLE_ADMIN`
- **Request Body (`SubjectRequestDto`)**
- **Respuesta Exitosa (`200 OK`):** `SubjectResponseDto`

---

## 7. Módulos Pendientes de Fases 3 y 4
- **Fase 3:** `tutor` (`/api/v1/tutors/**`, `/api/v1/supervisor/tutor-applications/**`), `offering` (`/api/v1/offerings/**`, `/api/v1/tutor/offerings/**`, `/api/v1/supervisor/offerings/**`).
- **Fase 4:** `availability` (`/api/v1/tutors/{id}/availability`, `/api/v1/tutor/availability/**`), `studygroup` (`/api/v1/study-groups/**`), `booking` (`/api/v1/bookings/**`, `/api/v1/tutor/bookings/**`), `review` (`/api/v1/bookings/{id}/reviews`, `/api/v1/tutors/{id}/reviews`).
