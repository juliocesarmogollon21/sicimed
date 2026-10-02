# Capítulos 5–8 — Informe SICIMED

**Proyecto:** SICIMED – Sistema Web Distribuido para la Gestión de Citas Médicas
**Caso de estudio:** Policlínico San Rafael
**Curso:** Soluciones Web y Aplicaciones Distribuidas · Ingeniería de Sistemas · Ciclo 8 · UPN · 2026
**Repositorio:** https://github.com/juliocesarmogollon21/sicimed

---

## Capítulo 5 — Desarrollo del backend: Spring Boot, Spring Data JPA y JWT

### 5.1 Arquitectura en capas (Controller → Service → Repository)

El backend es un proyecto Maven (`com.sicimed:sicimed:1.0`, Java 17, empaquetado `jar`) organizado
en siete paquetes:

| Paquete | Responsabilidad | Clases principales |
|---|---|---|
| `com.sicimed.modelo` | Entidades JPA y enumeraciones | `Usuario`, `Rol`, `Paciente`, `Medico`, `Especialidad`, `Sede`, `Cita`, `EstadoCita`, `Receta`, `Medicamento` |
| `com.sicimed.dto` | Contratos de entrada y salida (`record`) | `LoginRequest`, `RegisterRequest`, `CitaRequest`, `CitaResponse`, `RecetaRequest`, `PageResponse` |
| `com.sicimed.repositorio` | Persistencia: `JpaRepository` + JPQL | `UsuarioRepositorio`, `CitaRepositorio`, `MedicoRepositorio`, `PacienteRepositorio`, `SedeRepositorio`, `EspecialidadRepositorio`, `MedicamentoRepositorio`, `RecetaRepositorio` |
| `com.sicimed.servicio` | Reglas de negocio, con `@Transactional` | `AuthServicio`, `CitaServicio`, `CatalogoServicio`, `MedicoServicio`, `UsuarioServicio` |
| `com.sicimed.controlador` | `@RestController`: HTTP, validación y códigos de estado | `AuthController`, `CitaController`, `ReporteController`, `MedicoController`, `PacienteController`, `CatalogoController`, `UsuarioController`, `SistemaController` |
| `com.sicimed.seguridad` | JWT y principal autenticado | `JwtServicio`, `JwtAuthenticationFilter`, `SicimedPrincipal`, `ActualizadorAutenticacion` |
| `com.sicimed.configuracion` / `.excepcion` | Configuración, datos semilla, manejo de errores | `SecurityConfig`, `OpenApiConfig`, `DatosSemilla`, `GlobalExceptionHandler` |

Las entidades se relacionan entre sí con anotaciones JPA y las consultas de negocio se
escriben en JPQL dentro de los repositorios; los controladores no contienen SQL y los
repositorios no contienen reglas de negocio.

### 5.2 Acceso a datos (Spring Data JPA)

- **Entidades:** anotadas con `@Entity`. Las relaciones son `@OneToOne` (médico–usuario,
  paciente–usuario, receta–cita) y `@ManyToOne` (cita–médico/paciente/sede,
  médico–especialidad/sede).
- **Repositorios:** extienden `JpaRepository`, lo que aporta `save`, `findById`, `findAll`,
  `delete` y paginación sin escribir código. Las consultas con criterio de negocio se escriben
  como JPQL explícito:

```java
@Query("""
        SELECT COUNT(c) FROM Cita c
        WHERE c.medico.id = :medicoId
          AND c.fecha = :fecha
          AND c.hora = :hora
          AND c.estado <> :estadoExcluido
          AND (:excludeId IS NULL OR c.id <> :excludeId)
        """)
long contarConflictos(@Param("medicoId") Long medicoId,
                     @Param("fecha") LocalDate fecha,
                     @Param("hora") LocalTime hora,
                     @Param("estadoExcluido") EstadoCita estadoExcluido,
                     @Param("excludeId") Long excludeId);
```

- **Criteria API:** los listados filtrados por rol usan `Specification<Cita>`, que compone
  condiciones sin concatenar cadenas.
- **Configuración:** `src/main/resources/application.yml`, con los valores de conexión tomados de
  variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`), y `application-dev.yml` para el perfil
  H2 en memoria.
- **Concurrencia:** `Cita` incorpora `@Version` (bloqueo optimista), de modo que dos
  recepcionistas no puedan confirmar el mismo horario a la vez.
- **Esquema:** `backend/sql/01_create_sicimed_mysql.sql` para MySQL 8. Con `ddl-auto=update`
  Hibernate crea el esquema automáticamente.

### 5.3 Lógica de negocio implementada

**Autenticación (`AuthServicio`).** Valida el usuario con `BCryptPasswordEncoder`. El mismo
mensaje (`Credenciales invalidas`) se devuelve tanto si el usuario no existe como si la contraseña
no coincide, para no revelar qué cuentas hay en el sistema. El registro de paciente exige
usuario, contraseña de 6 caracteres o más, nombre, email y DNI únicos.

**Gestión de citas (`CitaServicio`).**

- *Crear:* verifica la existencia de médico, sede y paciente; rechaza fechas pasadas; comprueba
  el conflicto de horario y lanza `ConflictoException` (HTTP 409) si ya hay una cita no cancelada
  para ese médico, fecha y hora.
- *Reprogramar:* no permite citas `CANCELADO` ni `ATENDIDO`; si cambian médico, fecha u hora,
  revalida el conflicto **excluyendo la propia cita** (`excludeId`), de modo que mantener el
  mismo horario no se reporte como conflicto consigo misma.
- *Cancelar:* solo desde `PENDIENTE`; al pasar a `CANCELADO` el horario vuelve a estar disponible.
- *Disponibilidad:* genera los turnos de 08:00 a 17:00 en bloques de 30 minutos y descarta las
  horas ya ocupadas.
- *Diagnóstico:* lo registra el médico de la cita (o el administrador) y la pasa a `ATENDIDO`.
- *Receta digital:* relación 1 a 1 con la cita (`uq_recetas_cita`); si ya existe, se actualiza en
  lugar de duplicarse.
- *Control de pertenencia:* `ADMIN` y `RECEPCIONISTA` gestionan cualquier cita; el `MÉDICO` solo
  las de su agenda; el `PACIENTE` solo las suyas, y únicamente ve la receta si la cita está
  `ATENDIDO`.

**Transacciones.** Cada método público de servicio se anota con `@Transactional` (o
`@Transactional(readOnly = true)` en las consultas). El rollback ante cualquier excepción es
automático: si la creación de una cita falla a mitad de camino, no queda registro parcial.

### 5.4 Autenticación y autorización (Spring Security + JWT)

1. `POST /api/auth/login` firma un **JWT HS256** con `userId`, `rol`, `nombre`, `medicoId` y
   `pacienteId`.
2. `JwtAuthenticationFilter` extrae el token de la cabecera `Authorization: Bearer`, valida firma
   y expiración, y establece el `SecurityContext` con un `SicimedPrincipal`.
3. `SecurityConfig` es `STATELESS`: las rutas públicas son `/api/auth/**`, `/api/sistema/**`, las
   de Swagger y el `OPTIONS` de CORS; el resto exige token.
4. La autorización fina se declara con `@PreAuthorize` en los controladores, y el CORS se
   configura con `CorsConfigurationSource` limitado a `http://localhost:4200`.

Esto sustituye al `TokenStore` en memoria de la versión anterior: el token JWT es stateless y
sobrevive a reinicios del servidor.

### 5.5 Documentación de la API (Swagger / OpenAPI)

`springdoc-openapi` genera la especificación en `/v3/api-docs` y la interfaz interactiva en
**`/swagger-ui.html`**. La definición declara los 35 endpoints con su método, cuerpo, parámetros y
códigos de respuesta, y registra el esquema de seguridad `bearerAuth` para que se pueda probar la
API desde el navegador: se obtiene un token con *Authorize* y se prueban las rutas directamente.

### 5.6 Tabla de endpoints

Base URL: `http://localhost:8080/api`
Autenticación: `Authorization: Bearer <token>` salvo `/api/auth/**` y `/api/sistema/**`.

| Método | Ruta | Rol | Respuestas |
|---|---|---|---|
| GET | `/api/sistema/estado` | Público | 200 (incluye `preparado`) |
| POST | `/api/auth/login` | Público | 200 token; 400 credenciales inválidas |
| POST | `/api/auth/register` | Público | 201; 400 validación; 409 usuario repetido |
| GET | `/api/citas` | Autenticado | 200 página de citas, filtrada por rol y por `dni`, `medicoId`, `fecha` |
| GET | `/api/citas/disponibilidad` | Autenticado | 200 lista `HH:mm` |
| GET | `/api/citas/{id}` | Dueño / agenda / staff | 200; 403; 404 |
| POST | `/api/citas` | ADMIN, RECEPC, PACIENTE | 201; 400; 409 conflicto |
| PUT | `/api/citas/{id}` | Dueño / staff | 200; 400; 409 |
| DELETE | `/api/citas/{id}` | Dueño / staff | 200 CANCELADO; 400 |
| POST | `/api/citas/{id}/diagnostico` | MÉDICO (su cita), ADMIN | 200; 403 |
| POST | `/api/citas/{id}/receta` | MÉDICO (su cita), ADMIN | 200; 403 |
| GET | `/api/citas/{id}/receta` | MÉDICO, ADMIN, PACIENTE si ATENDIDO | 200; 404 |
| GET | `/api/reportes/citas` | ADMIN, RECEPC | 200 página; 400; 403 |
| GET | `/api/medicos` | Autenticado | 200 |
| GET | `/api/medicos/{id}` | Autenticado | 200; 404 |
| POST/PUT/DELETE | `/api/medicos[/{id}]` | ADMIN | 201/200/204; 400; 403 |
| GET | `/api/pacientes?dni=` | ADMIN, RECEPC, MÉDICO | 200; 400; 404 |
| GET/POST/PUT/DELETE | `/api/sedes[/{id}]` | Autenticado / ADMIN | 200/201/204 |
| GET/POST/PUT/DELETE | `/api/especialidades[/{id}]` | Autenticado / ADMIN | 200/201/204 |
| GET/POST/PUT/DELETE | `/api/medicamentos[/{id}]` | Autenticado / ADMIN | 200/201/204 |
| GET/POST/PUT/DELETE | `/api/usuarios[/{id}]` | ADMIN | 200/201/204; 409 |

Ejemplo de creación de cita:

```
POST /api/citas
Authorization: Bearer <token>
Content-Type: application/json

{ "medicoId": 1, "sedeId": 1, "fecha": "2026-10-15", "hora": "10:00", "motivo": "Control general" }
```

Respuesta `201`:

```json
{ "id": 1, "medicoId": 1, "medicoNombre": "Dr. Carlos Mendoza", "pacienteId": 1,
  "pacienteNombre": "Maria Perez", "sedeId": 1, "sedeNombre": "Sede Centro",
  "especialidad": "Medicina General", "fecha": "2026-10-15", "hora": "10:00",
  "estado": "PENDIENTE", "motivo": "Control general" }
```

Si el horario está tomado, responde `409`:

```json
{ "status": 409, "message": "Ya existe una cita para ese medico en la fecha y hora indicadas" }
```

### 5.7 Manejo de errores

`GlobalExceptionHandler` (`@RestControllerAdvice`) traduce cada excepción de dominio a un código
HTTP y a una envoltura común `{ "status": …, "message": "…" }`, de modo que Angular muestre
siempre el mensaje en español:

| Situación | Código |
|---|---|
| Token ausente o inválido | 401 `No autenticado` |
| Rol sin permiso, o cita de otro usuario | 403 |
| Dato inválido o regla incumplida | 400 |
| Recurso inexistente | 404 |
| Horario ocupado, usuario repetido | 409 |

`SecurityConfig` define además los `authenticationEntryPoint` (401) y `accessDeniedHandler` (403)
para que la cadena de seguridad responda también con el mismo formato JSON.

### 5.8 Empaquetado

```bash
cd backend
mvn -DskipTests clean package
```

Artefacto: `backend/target/sicimed.jar`, que se ejecuta con `java -jar target/sicimed.jar`. Ya no
se genera un WAR ni se requiere Tomcat instalado: Spring Boot incluye el contenedor embebido.

---

## Capítulo 6 — Integración con Angular y pruebas funcionales

### 6.1 Consumo de la API desde Angular

El frontend usa Angular con componentes *standalone* y `HttpClient`. La conexión tiene cuatro
piezas:

1. **URL base centralizada** en `src/environments/environment.ts` (`apiUrl`).
2. **Interceptor funcional** (`authInterceptor`) que adjunta `Authorization: Bearer <token>` a
   cada petición, para que ningún componente tenga que acordarse del token.
3. **Un servicio por recurso** en `src/app/servicios/`, que encapsula las llamadas HTTP y devuelve
   `Observable` tipados con los modelos de `src/app/modelos/`. Los componentes nunca escriben URLs.
4. **Guards de ruta** (`authGuard`, `roleGuard`) que protegen las pantallas según el rol.

| Servicio | Endpoints que consume |
|---|---|
| `AuthService` | `POST /auth/login`, `POST /auth/register` |
| `CitaService` | `GET/POST /citas`, `GET/PUT/DELETE /citas/{id}`, `GET /citas/disponibilidad`, `POST /citas/{id}/diagnostico`, `GET/POST /citas/{id}/receta` |
| `MedicoService` | `GET /medicos`, `POST/PUT/DELETE /medicos` |
| `PacienteService` | `GET /pacientes?dni=` |
| `SedeService`, `EspecialidadService`, `MedicamentoService`, `UsuarioService` | CRUD de sus catálogos |

**Ajuste por la migración.** El backend ahora devuelve 204 en los `DELETE` y usa `/{id}` en la
ruta en lugar de `?id=`; el `GET /citas` devuelve una página y el endpoint
`GET /medicos/{id}/agenda` se resolvió filtrando `GET /citas`, que el backend ya acota por rol.
Los servicios Angular se ajustaron a ese contrato.

### 6.2 Manejo de datos y estados

| Tipo de estado | Dónde vive | Cómo se actualiza |
|---|---|---|
| Sesión (token, rol, `medicoId`, `pacienteId`) | `localStorage` (`sicimed_auth`) | Se guarda al iniciar sesión o registrarse; sobrevive a recargar |
| Permisos de pantalla | `auth.hasRole(...)` | Menú y botones según el rol |
| Protección de rutas | `authGuard`, `roleGuard` | Sin token se redirige a `/login` |
| Datos de cada pantalla | Propiedades del componente | Se cargan en `ngOnInit()` y se vuelven a pedir tras cada operación |
| Estados de interfaz | Banderas de cargando, error y mensaje | Deshabilitan botones y muestran alertas |

### 6.3 Pruebas funcionales del flujo completo

Ejecutadas contra el backend real con datos de la base, no con listas escritas a mano.

| N.º | Flujo | Resultado |
|---|---|---|
| F-01 | Login válido (los cuatro roles) | Entra al dashboard con su menú y conteos |
| F-02 | Login incorrecto | Mensaje «Credenciales invalidas»; permanece en `/login` |
| F-03 | Registro de paciente | Cuenta creada y sesión iniciada como `PACIENTE` |
| F-04 | Registro con DNI repetido | Mensaje «El DNI ya esta registrado» |
| F-05 | Reserva de cita | «Cita registrada»; aparece en estado `PENDIENTE` |
| F-06 | Choque de horario | La hora desaparece del selector; si se fuerza, el backend responde 409 |
| F-07 | Búsqueda por DNI inexistente | Mensaje «Paciente no encontrado con DNI …» |
| F-08 | Formulario incompleto | Validación en el componente; no se llama a la API |
| F-09 | Reprogramar | «Cita reprogramada»; la disponibilidad se recalcula |
| F-10 | Cancelar | Estado `CANCELADO`; la hora vuelve a estar libre |
| F-11 | Atención médica | Diagnóstico guardado; la cita pasa a `ATENDIDO` |
| F-12 | Receta digital | Receta asociada a la cita |
| F-13 | Paciente ve su receta | Modal con medicamentos e indicaciones (solo si está `ATENDIDO`) |
| F-14 | Aislamiento por rol | El paciente solo ve sus propias citas |
| F-15 | CRUD de catálogo | La sede aparece y, al desactivarla, deja de salir en nueva cita |
| F-16 | Backend detenido | Mensaje de error; la aplicación no se cae |

### 6.4 Pruebas de la API REST

El script `docs/probar_api.sh` ejecuta **46 casos** contra la API en marcha y todos pasan. Cubre
autenticación (válida, inválida, sin token, token corrupto), registro y sus validaciones,
autorización por rol, ciclo completo de una cita (disponibilidad, creación, conflicto 409, fecha
pasada, consulta), atención médica (diagnóstico, receta, bloqueo de reprogramar una cita
atendida), aislamiento entre roles, paginación, reportes, baja lógica de catálogos y
documentación. Salida:

```
Aprobados: 46   Fallidos: 0
Todas las pruebas pasaron.
```

El script es idempotente: genera un usuario distinto en cada ejecución y reserva la
primera hora realmente libre del médico, de modo que puede repetirse contra un servidor
ya sembrado. Se verificó tres veces seguidas contra la misma instancia, con 39/39 en
cada corrida.

### 6.5 Incidencias encontradas y acciones correctivas

| N.º | Incidencia | Causa | Acción correctiva |
|---|---|---|---|
| I-01 | El navegador bloqueaba las llamadas al API (CORS) | Front y back en orígenes distintos; el preflight `OPTIONS` se rechazaba | `CorsConfigurationSource` en `SecurityConfig` con origen `http://localhost:4200` y el preflight permitido |
| I-02 | Tras el login, el resto de peticiones respondía 401 | Los componentes no enviaban el token | `authInterceptor` añade `Authorization: Bearer` a todas las peticiones |
| I-03 | La API devolvía `hora` como `10:00:00` pero la disponibilidad como `10:00` | Jackson serializa `LocalTime` con segundos por defecto | `@JsonFormat(pattern = "HH:mm")` en la entidad y en los DTO de entrada y salida |
I-09 | La agenda de un médico mostraba citas de otros médicos cuando la abría un ADMIN o recepción | El backend solo acotaba por rol cuando el usuario era `MÉDICO`, y no aceptaba filtros por `medicoId` ni por fecha; el frontend filtraba la página ya paginada, lo que además dejaba páginas incompletas | `GET /api/citas` acepta ahora `medicoId` y `fecha`, aplicados en SQL mediante `Specification` antes de paginar; el frontend deja de filtrar en el cliente |
| I-10 | La cabecera `Authorization` llegaba vacía al backend | El interceptor tenía un literal roto (`Authorization: *** ${token}`) que TypeScript compilaba como división, sin dar error, pero emitía una cabecera inútil: toda petición autenticada recibía 401 | Se corrigió a `'Bearer ' + token` y se añadió un caso de prueba que verifica el encabezado en el bundle compilado |
| I-04 | La primera consulta tras arrancar fallaba con «Credenciales inválidas» | Tomcat empieza a aceptar peticiones antes de que `ApplicationRunner` siembre la base | `DatosSemilla` publica una bandera de disponibilidad que `/api/sistema/estado` expone como `preparado` |
| I-05 | Un paciente podía ver citas de otros | El listado devolvía todas las citas | `CitaServicio` filtra por rol y `assertPuedeGestionarCita` responde 403 |
| I-06 | Reprogramar mantenía el horario y lo reportaba como conflicto | La validación incluía la propia cita | `contarConflictos` recibe `excludeId` y excluye la cita en edición |
| I-07 | El token se perdía al reiniciar el backend | `TokenStore` en memoria | JWT firmado: stateless y persistente entre reinicios |
| I-08 | Se perdían las recetas antiguas en texto plano | Cambió el formato a JSON de varios medicamentos | Lectura tolerante: si el JSON no parsea, se interpreta línea por línea |

---

## Capítulo 7 — Validación, control de calidad y documentación técnica

### 7.1 Revisión de buenas prácticas y estándares

| Aspecto | Estándar aplicado | Ejemplos |
|---|---|---|
| Nombres en Java | `PascalCase` en clases, `camelCase` en métodos y variables, español en el dominio | `CitaServicio`, `contarConflictos`, `medicoId`, `HORA_INICIO` |
| Nombres en Angular | Guía de estilo oficial, sufijos que indican el rol del archivo | `cita.service.ts`, `auth.guard.ts`, `cita.ts` |
| Organización en capas | Repositorio (persistencia) → Servicio (negocio) → Controlador (HTTP) | `CitaRepositorio` → `CitaServicio` → `CitaController` |
| Contratos de entrada/salida | `record` inmutables en `dto`, no entidades en los controladores | `CitaRequest`, `CitaResponse` |
| Validación | Bean Validation declarativa en los DTO, con mensajes en español | `@NotBlank`, `@Email`, `@Size(min = 6)` |
| Transacciones | `@Transactional` en el servicio, no en el repositorio | `CitaServicio.crear` |
| Consultas | JPQL con parámetros nombrados; sin concatenar cadenas | `CitaRepositorio.contarConflictos` |
| Seguridad | Contraseñas con BCrypt; el hash nunca sale por la API | `UsuarioResponse` no incluye `password` |
| Errores | Excepciones de dominio traducidas a HTTP en un único `@RestControllerAdvice` | `GlobalExceptionHandler` |
| Concurrencia | Bloqueo optimista con `@Version` | `Cita.version` |
| Documentación | Endpoints anotados; la especificación se genera sola | `@Operation`, `@ApiResponses` |
| Configuración | Valores sensibles por variable de entorno, nunca en el repositorio | `DB_PASSWORD`, `JWT_SECRET` |

### 7.2 Documentación técnica de la API

**Componentes**

| Componente | Descripción |
|---|---|
| Servidor | Java 17 + Spring Boot 3.3.5, empaquetado como JAR |
| Cliente | Angular 21, componentes *standalone* |
| Base de datos | MySQL 8 (perfil `dev`: H2 en memoria) |
| Formato | JSON, `application/json;charset=UTF-8` |
| Fechas y horas | ISO 8601 — fecha `yyyy-MM-dd`, hora `HH:mm` |
| Autenticación | JWT HS256 en la cabecera `Authorization` |
| Documentación interactiva | `/swagger-ui.html` |
| CORS | Solo el origen del frontend |

**Paginación.** Los listados largos devuelven:

```json
{ "content": [], "page": 0, "size": 20, "totalElements": 137,
  "totalPages": 7, "first": true, "last": false }
```

**Ejemplo de respuesta de error**

```json
{ "status": 409, "message": "Ya existe una cita para ese medico en la fecha y hora indicadas" }
```

La especificación completa está disponible en `/swagger-ui.html` (35 endpoints, 21 esquemas) y en
`/v3/api-docs`. Se exportó además como archivo estático a `docs/SICIMED_openapi.json`, que puede
importarse en Postman (Import → Link) o en cualquier editor compatible con OpenAPI 3.

### 7.3 Estado de cumplimiento de requerimientos

| Código | Requerimiento | Estado | Observación |
|---|---|---|---|
| RF-01 | Autenticación y control de acceso por roles | Cumplido | Spring Security + JWT, verificado con casos 401/403 |
| RF-02 | Emisión y validación de token de sesión | Cumplido | JWT firmado; sobrevive a reinicios |
| RF-03 | Denegar rutas por rol | Cumplido | `@PreAuthorize` + control de pertenencia |
| RF-04 | Filtrar disponibilidad por sede, especialidad y médico | Cumplido | `GET /medicos` con filtros + disponibilidad |
| RF-05 | Impedir citas duplicadas (409) | Cumplido | JPQL de conflicto + `@Version` |
| RF-06 | Paciente prellenado en nueva cita | Cumplido | El backend ignora el `pacienteId` y usa el de la sesión |
| RF-07 | Buscar paciente por DNI | Cumplido | `GET /pacientes?dni=` |
| RF-08 | Cancelar y liberar horario | Cumplido | Estado `CANCELADO` excluido del conflicto |
| RF-09 | Listar citas propias o por DNI | Cumplido | Filtrado por rol en el servicio |
| RF-10 | Agenda del día del médico | Cumplido | `GET /citas` acotado por rol `MEDICO` |
| RF-11 | Registrar diagnóstico | Cumplido | `POST /citas/{id}/diagnostico` |
| RF-12 | Emitir receta digital | Cumplido | Relación 1 a 1 con la cita |
| RF-13 | Pasar a ATENDIDO al diagnosticar | Cumplido | En la misma transacción |
| RF-14 | No editar histórico cerrado | Cumplido | Bloquea `ATENDIDO` y `CANCELADO` |
| RF-15 | CRUD de sedes con baja lógica | Cumplido | `activo = false` |
| RF-16 | CRUD de especialidades | Cumplido | Con control de nombre duplicado (409) |
| RF-17 | Registrar médicos ligados a sede y especialidad | Cumplido | Requiere usuario con rol `MEDICO` |
| RF-18 | Panel con indicadores básicos | Parcial | Conteos simples; no es BI |
| — | Reportes y paginación | Cumplido (añadido) | `GET /api/reportes/citas` con filtros y paginación |
| — | Documentación de la API | Cumplido (añadido) | Swagger UI + OpenAPI |
| — | Bloques horarios configurables por médico | No implementado | Turnos fijos de 08:00 a 17:00 |

**Resumen:** 17 de 18 requerimientos funcionales del Capítulo 3 están implementados, más tres
capacidades que la migración añadió (paginación, reportes y documentación OpenAPI). Quedan
pendientes de forma consciente el panel de indicadores avanzado (RF-18) y los bloques horarios
editables por médico.

### 7.4 Observaciones y plan de despliegue

**Pendientes**

1. Panel de indicadores avanzado (ocupación, citas por sede y mes).
2. Bloques horarios editables por médico.
3. Envío de recordatorios de cita por correo.
4. Exportación de reportes a PDF y Excel.
5. Pruebas automatizadas en integración continua (JUnit + pruebas del frontend).

**Plan de despliegue**

| N.º | Actividad | Estado |
|---|---|---|
| 1 | Crear la base MySQL e importar el script de esquema | Completado |
| 2 | Configurar `DB_URL`, `DB_USER`, `DB_PASSWORD` y `JWT_SECRET` como variables de entorno | Completado |
| 3 | `mvn -DskipTests clean package` → `target/sicimed.jar` | Completado |
| 4 | Ejecutar `java -jar target/sicimed.jar` | Completado |
| 5 | `npm install` y `npx ng build` en el frontend | Completado |
| 6 | Ajustar CORS al dominio del frontend en producción | Pendiente |
| 7 | Verificar con `GET /api/sistema/estado` que `preparado` es `true` | Completado |

---

## Capítulo 8 — Despliegue, validación final y conclusiones

### 8.1 Evidencias de despliegue

| Elemento | Ubicación |
|---|---|
| Repositorio | https://github.com/juliocesarmogollon21/sicimed |
| Backend (API) | `http://localhost:8080/api` |
| Documentación interactiva | `http://localhost:8080/swagger-ui.html` |
| Frontend | `http://localhost:4200` |
| Script de pruebas | `docs/probar_api.sh` |

El backend responde `GET /api/sistema/estado`:

```json
{ "servicio": "sicimed-backend", "estado": "activo", "preparado": true,
  "version": "1.0.0", "documentacion": "/swagger-ui.html" }
```

### 8.2 Manual breve de instalación y uso

**Requisitos:** Java 17+, Maven 3.9+, Node.js 20+, MySQL 8.

**Backend**

```bash
cd backend
# 1. Base de datos (opcional: con ddl-auto=update se crea sola)
mysql -u root -p < sql/01_create_sicimed_mysql.sql
# 2. Empaquetar y ejecutar
mvn -DskipTests clean package
java -jar target/sicimed.jar
```

Para probar sin instalar MySQL, usar el perfil de desarrollo: `java -jar target/sicimed.jar
--spring.profiles.active=dev` (H2 en memoria, con datos semilla automáticos).

**Frontend**

```bash
cd frontend
npm install
npx ng serve      # http://localhost:4200
```

Si el backend corre en otro puerto o dominio, cambiar `apiUrl` en
`frontend/src/environments/environment.ts`.

**Credenciales de demostración**

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `recepcion` | `recep123` | RECEPCIONISTA |
| `medico1` | `medico123` | MÉDICO |
| `paciente1` | `paciente123` | PACIENTE |

**Uso en tres pasos**

| Paso | Acción |
|---|---|
| 1 | Iniciar sesión con un usuario de demostración |
| 2 | Ir a **Citas** → **Nueva cita** y elegir paciente, especialidad, médico, fecha y hora |
| 3 | Guardar: la cita queda `PENDIENTE` y aparece en el listado y el calendario |

### 8.3 Resultados de pruebas finales

| Tipo de prueba | Alcance | Resultado |
|---|---|---|
| Pruebas de la API (script) | 46 casos sobre autenticación, roles, ciclo de cita, atención clínica, catálogos, paginación y documentación | **46 / 46 aprobadas** |
| Pruebas funcionales (UAT) | 16 flujos desde el navegador con los cuatro roles | 16 / 16 |
| OpenAPI | Endpoints documentados y verificables en Swagger UI | 35 endpoints, 21 esquemas |

### 8.4 Conclusiones

Se cumplió el objetivo general del Capítulo 1: se construyó una aplicación web distribuida para
la gestión de citas médicas, con control de roles, agenda médica, diagnóstico, receta digital y
configuración administrativa.

La migración de la arquitectura es el resultado técnico más relevante de esta etapa. El proyecto
arrancó sobre Jakarta Servlets con JDBC manual, una decisión que el Capítulo 4 de la primera
versión justificaba con una matriz de ponderación (4,65 frente a 3,15). Al contrastarlo con los
saberes que el sílabo exige para las unidades III y IV, esa justificación no se sostenía: la
alternativa elegida no podía aportar transacciones con rollback, Spring Security con JWT,
paginación de Spring Boot ni JPQL con parámetros nombrados, que son requisitos explícitos del
curso. La decisión corregida es Spring Boot 3.3 con Spring Data JPA, y la nueva ponderación
(4,90 frente a 1,80) refleja la distancia entre ambas.

La migración no se limitó a cambiar de framework: la verificación de 46 casos sobre la API en
marcha permitió detectar que el formato de la hora era inconsistente entre endpoints, que el
servidor aceptaba peticiones antes de que la base estuviera lista, y que reprogramar una cita
manteniendo el horario se reportaba como conflicto consigo misma. Los tres se corrigieron y hoy
tienen una prueba que los cubre.

Quedan pendientes, de forma consciente, el panel de indicadores avanzado, los bloques horarios
editables por médico, el envío de recordatorios y la exportación de reportes.

### 8.5 Recomendaciones de mejora

1. Migrar la autenticación a OAuth 2 con un proveedor externo; hoy solo hay JWT propio.
2. Añadir caché (Redis) sobre la consulta de disponibilidad y la agenda, que son las más
   repetidas.
3. Añadir pruebas automatizadas (JUnit 5 con `@SpringBootTest` y pruebas del frontend) en
   integración continua.
4. Configurar Spring Boot Actuator con métricas y salud del servicio en producción.
5. Sustituir el bloque horario fijo por un calendario laboral configurable por médico.

### 8.6 Relación con el ODS 9 (Industria, innovación e infraestructura)

| Eje del ODS 9 | Aporte del proyecto |
|---|---|
| Innovación tecnológica | Digitaliza la programación de citas, que se llevaba en cuaderno o por teléfono, con una API REST documentada y una interfaz web adaptable |
| Infraestructura resiliente | La arquitectura en capas con transacciones y control de concurrencia evita la inconsistencia de datos; el despliegue como JAR único reduce la complejidad operativa |
| Acceso a la tecnología | Herramientas gratuitas o de edición gratuita (Spring Boot, MySQL Community, Angular) permiten que un centro de salud pequeño adopte la solución sin software propietario |

---

*Fin Capítulos 4–8.*
