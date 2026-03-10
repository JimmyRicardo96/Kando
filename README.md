# Kando – Plataforma Kanban Offline‑First

Kando es una plataforma de gestión de proyectos basada en **metodología Kanban**, diseñada para funcionar en entornos **offline‑first**, con **sincronización bidireccional** y **colaboración en tiempo real**.  
Está pensada para escalar como producto **SaaS multi‑inquilino**.

---

## 🚀 Características principales

- **Kanban avanzado**
  - Tableros, columnas (listas) y tarjetas (tareas).
  - Subtareas recursivas (árbol de tareas).
  - Prioridades, estados, fechas de vencimiento, responsables.
  - Reordenamiento eficiente usando **fractional indexing / LexoRank**.

- **Offline‑First + Sync**
  - La app cliente (móvil/web) trabaja sobre una base local (por ejemplo, SQLite).
  - Todas las entidades tienen:
    - `id` ULID (ordenable, generado en cliente y servidor).
    - `version` para *optimistic locking*.
    - `created_at`, `updated_at`, `deleted_at` (soft delete).
  - Endpoints de **delta sync** por `updated_at` y manejo de:
    - Conflictos de edición (`409 Conflict` + payload de conflicto).
    - Sincronización masiva (*bulk sync*) con idempotencia.

- **Colaboración**
  - Múltiples usuarios por tablero (`BoardUser`) con roles:
    - `OWNER`, `ADMIN`, `MEMBER`, `VIEWER`.
  - Permisos a nivel de tablero y operación.
  - Preparado para notificaciones en tiempo real (WebSocket / SSE en fases futuras).

- **Arquitectura limpia**
  - Backend en **Spring Boot 3**, Java 17.
  - **PostgreSQL** como base de datos principal.
  - **Spring Data JPA** con auditoría (`@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`).
  - Identificadores **ULID** para todas las entidades de dominio.
  - Seguridad con **Spring Security + JWT** (stateless).
  - DTOs y mapeos con **MapStruct**.

---

## 🧱 Arquitectura del Backend

### Tecnologías

- Java 17  
- Spring Boot 3.x
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-security`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-actuator`
- Base de datos: PostgreSQL  
- JWT: `jjwt` (io.jsonwebtoken)  
- ULID: `ulid-creator`  
- MapStruct para mapeo DTO  

### Estructura de paquetes

```text
com.jrdm.Kando
├── common
│   ├── dto                 # ApiResponse, ErrorResponse, DTOs genéricos
│   └── exception           # ConflictException, GlobalExceptionHandler
├── config
│   ├── jpa                 # JpaConfig, AuditAwareImpl
│   └── security            # JwtFilter, JwtService, SecurityConfig, UserDetailsServiceImpl
├── controller              # Controladores REST (TaskController, BoardController, AuthController)
├── domain
│   ├── enums               # TaskStatus, BoardRole, Priority, etc.
│   └── model               # User, Board, BoardUser, Task, Label, etc.
├── repository              # Repositorios Spring Data (TaskRepository, BoardRepository, UserRepository)
├── service
│   ├── dto                 # DTOs de dominio (TaskDTO, BoardDTO, SyncDTO, etc.)
│   ├── mapper              # MapStruct mappers (TaskMapper, BoardMapper)
│   ├── validation          # Validaciones de negocio complejas
│   ├── AuthService         # Login/registro, emisión de JWT
│   ├── TaskService / Impl  # Lógica de tareas
│   └── SyncService / Impl  # Lógica de sincronización offline-first
└── KandoApplication        # Clase main
