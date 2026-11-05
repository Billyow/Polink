# Polink — Plataforma Especializada de Tutorías entre Pares

Monolito modular para la gestión de tutorías académicas entre pares, con flujo supervisado de validación de asesores (promedio $\ge 4.0 / 5.0$), catálogo cerrado de asignaturas por rama, búsqueda por palabras clave, agendamiento de 1 o varias horas, grupos de estudio con descuentos por tramos, notificaciones híbridas (In-App + Correo) y evaluaciones anónimas por defecto.

---

## 1. Stack Tecnológico

- **Backend:** Java 21, Spring Boot 3.3, Spring Security + JWT, Spring Data JPA, Flyway, PostgreSQL 16, JavaMailSender (`@Async`), SpringDoc OpenAPI 3 (Swagger UI).
- **Frontend:** React 18, Vite, TypeScript, TailwindCSS.
- **Almacenamiento de Imágenes:** `StorageService` desacoplado (Almacenamiento local `/uploads` en desarrollo y Cloudinary Free Tier en producción).
- **Infraestructura Local:** Docker Compose (`postgres:16-alpine` + `axllent/mailpit` para pruebas SMTP/WebUI locales).

---

## 2. Arquitectura por Funcionalidad y Capas Internas

Tanto el Backend como el Frontend están organizados con **una carpeta por funcionalidad** y **subcarpetas por capa arquitectónica**:

```text
Polink/
├── .gitignore                  # Exclusiones de seguridad (.env, secretos, uploads, compilados, IDEs)
├── .env.example                # Plantilla de variables de entorno de Backend y Docker Compose
├── docker-compose.yml          # PostgreSQL 16 + Mailpit
├── API_CONTRACT.md             # Contrato oficial vivo de todos los endpoints REST
├── implementation_plan.md      # Plan detallado por fases para desarrolladores y agentes
├── backend/                    # Spring Boot 3.x (Java 21)
│   └── src/main/java/com/polink/
│       ├── common/             # config, exception, security, storage
│       ├── auth/               # controller, service, repository, entity, dto
│       ├── user/               # controller, service, repository, entity, dto
│       ├── catalog/            # controller, service, repository, entity, dto
│       ├── tutor/              # controller, service, repository, entity, dto
│       ├── offering/           # controller, service, repository, entity, dto
│       ├── availability/       # controller, service, repository, entity, dto
│       ├── studygroup/         # controller, service, repository, entity, dto
│       ├── booking/            # controller, service, repository, entity, dto
│       ├── review/             # controller, service, repository, entity, dto
│       ├── notification/       # controller, service, repository, entity, dto
│       └── audit/              # controller, service, repository, entity, dto
└── frontend/                   # React + Vite + TypeScript + TailwindCSS
    ├── .env.example            # Plantilla de variables de entorno de Frontend
    └── src/
        ├── core/               # Axios client, AuthContext, NotificationPollingContext, UI común
        └── features/           # Carpetas por funcionalidad con capas (pages, components, services, hooks, types)
```

---

## 3. Configuración Inicial y Seguridad (Variables de Entorno)

> **Regla de Seguridad:** Nunca subas archivos `.env`, llaves privadas, certificados ni la carpeta `uploads/` al repositorio. Verifica siempre `git status` antes de realizar un commit.

1. Copia las plantillas de variables de entorno en la raíz y en el frontend:
   ```bash
   cp .env.example .env
   cp frontend/.env.example frontend/.env
   ```
2. Ajusta los valores locales en `.env` (contraseña de PostgreSQL, `JWT_SECRET` de al menos 64 caracteres y credenciales semilla).

---

## 4. Ejecución del Entorno Local

### 4.1 Levantar Base de Datos y Servidor de Correo (Docker)
Asegúrate de tener **Docker Desktop** en ejecución y corre en la raíz `Polink/`:
```bash
docker compose up -d
```
- **PostgreSQL 16:** `localhost:5432` (`polink_db`)
- **Mailpit Web UI (Bandeja de correos locales):** `http://localhost:8025`
- **Mailpit SMTP:** `localhost:1025`

### 4.2 Ejecutar Backend (Spring Boot)
Desde la carpeta `backend/`:
```bash
./mvnw spring-boot:run
```
- **API Base URL:** `http://localhost:8080/api/v1`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

### 4.3 Ejecutar Frontend (React + Vite)
Desde la carpeta `frontend/`:
```bash
npm install
npm run dev
```
- **Aplicación Web:** `http://localhost:5173`

---

## 5. Roles del Sistema

| Rol | Permisos Principales |
| :--- | :--- |
| `ROLE_STUDENT` | Buscar asesorías por palabras clave, crear/unirse a grupos de estudio, agendar 1 o más horas y evaluar asesores (1 a 5 estrellas, anónimo por defecto). |
| `ROLE_TUTOR` | Incluye permisos de estudiante + configurar calendario de disponibilidad global, crear solicitudes de ofertas de asesoría con tramos de descuento, aceptar/rechazar reservas y finalizar asesorías. |
| `ROLE_SUPERVISOR` | Aprobar/rechazar registros de asesores (validando promedio $\ge 4.0$), aprobar/rechazar solicitudes de ofertas de asesoría y dar de baja ofertas activas con reporte obligatorio. |
| `ROLE_ADMIN` | Gestionar el catálogo cerrado de Ramas y Materias, gestionar cuentas de Supervisores, suspender cuentas por abuso y auditar acciones en `audit_logs`. |
