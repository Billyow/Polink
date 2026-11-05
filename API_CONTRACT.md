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
- **Query Params:**
  - `page` (opcional, default `0`)
  - `size` (opcional, default `20`)
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
- **Respuesta Exitosa (`200 OK`):** `NotificationResponseDto` actualizado con `"read": true`.
- **Errores:** `404 Not Found` si no existe o no pertenece al usuario.

### 2.4 Marcar todas las notificaciones como leídas
- **Método y Ruta:** `PATCH /api/v1/notifications/read-all`
- **Roles Permitidos:** Cualquier usuario autenticado
- **Respuesta Exitosa (`204 No Content`)**

---

## 3. Módulo Audit (`com.polink.audit`)

### 3.1 Consultar registros inmutables de auditoría
- **Método y Ruta:** `GET /api/v1/admin/audit-logs`
- **Roles Permitidos:** `ROLE_ADMIN`
- **Query Params:**
  - `actionType` (opcional, ej. `APPROVE_TUTOR`, `DEACTIVATE_OFFER`, `CREATE_SUBJECT`)
  - `actorUserId` (opcional, `UUID`)
  - `page` (opcional, default `0`)
  - `size` (opcional, default `20`)
- **Respuesta Exitosa (`200 OK`):**
```json
{
  "content": [
    {
      "id": "990e8400-e29b-41d4-a716-446655440222",
      "actorUserId": "110e8400-e29b-41d4-a716-446655440001",
      "actorRole": "ROLE_SUPERVISOR",
      "actionType": "DEACTIVATE_OFFER",
      "targetEntity": "TUTORING_OFFER",
      "targetId": "770e8400-e29b-41d4-a716-446655440111",
      "reasonOrDetails": "Contenido de la oferta no corresponde al temario oficial de la asignatura.",
      "createdAt": "2026-11-04T20:12:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 4. Módulos Pendientes de Fases 2 a 4 (Se actualizarán al implementar cada fase)
- **Fase 2:** `auth` (`/api/v1/auth/**`), `user` (`/api/v1/users/**`, `/api/v1/admin/users/**`), `catalog` (`/api/v1/catalog/**`, `/api/v1/admin/catalog/**`).
- **Fase 3:** `tutor` (`/api/v1/tutors/**`, `/api/v1/supervisor/tutor-applications/**`), `offering` (`/api/v1/offerings/**`, `/api/v1/tutor/offerings/**`, `/api/v1/supervisor/offerings/**`).
- **Fase 4:** `availability` (`/api/v1/tutors/{id}/availability`, `/api/v1/tutor/availability/**`), `studygroup` (`/api/v1/study-groups/**`), `booking` (`/api/v1/bookings/**`, `/api/v1/tutor/bookings/**`), `review` (`/api/v1/bookings/{id}/reviews`, `/api/v1/tutors/{id}/reviews`).
