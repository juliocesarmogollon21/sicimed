# SICIMED — Sistema Web Distribuido de Citas Médicas

Proyecto académico **UPN** · *Soluciones Web y Aplicaciones Distribuidas* · **Grupo 3**
Caso de estudio: Policlínico San Rafael

Repositorio: https://github.com/juliocesarmogollon21/sicimed

---

## Índice

1. [Qué hace el sistema](#1-qué-hace-el-sistema)
2. [Lenguajes y tecnologías](#2-lenguajes-y-tecnologías)
3. [Cómo se levantó el proyecto](#3-cómo-se-levantó-el-proyecto)
4. [Puesta en marcha paso a paso](#4-puesta-en-marcha-paso-a-paso)
5. [Estructura del proyecto](#5-estructura-del-proyecto)
6. [QuéNOS-brinda](#6-qué-nos-brinda)
7. [Roles y permisos](#7-roles-y-permisos)
8. [API REST](#8-api-rest)
9. [Pruebas](#9-pruebas)
10. [Migración de arquitectura](#10-migración-de-arquitectura)
11. [Problemas encontrados y cómo se resolvieron](#11-problemas-encontrados-y-cómo-se-resolvieron)
12. [Seguridad](#12-seguridad)
13. [Trabajo pendiente](#13-trabajo-pendiente)

---

## 1. Qué hace el sistema

SICIMED es una aplicación web distribuida para gestionar las citas médicas de un policlínico con
varias sedes, varias especialidades y cuatro tipos de usuario. Sustituye el cuaderno de citas por
un flujo único: el paciente o recepción reserva, el médico atiende y deja su receta, y el
administrador mantiene los catálogos.

**Módulos que incluye**

| Módulo | Qué permite |
|---|---|
| Autenticación | Login, registro público de pacientes y cierre de sesión |
| Panel (dashboard) | Resumen con conteos y accesos según el rol |
| Citas | Listado paginado, calendario semanal, alta, reprogramación y cancelación |
| Disponibilidad | Cálculo de horas libres por médico y fecha, sin choques |
| Agenda médica | Agenda del día, diagnóstico y receta digital |
| Receta | Receta asociada 1 a 1 a la cita, con medicamentos del catálogo |
| Configuración | CRUD con baja lógica de sedes, especialidades, médicos, usuarios y medicamentos |
| Reportes | Consulta filtrada por médico, estado y rango de fechas |
| Documentación API | Swagger UI con los 35 endpoints y prueba desde el navegador |

---

## 2. Lenguajes y tecnologías

### Por lenguaje

| Lenguaje | Archivos | Líneas | Dónde se usa |
|---|---|---|---|
| **Java** | 66 | 3.003 | Backend completo (Spring Boot) |
| **TypeScript** | 37 | 1.971 | Frontend Angular |
| **HTML** | 15 | 1.132 | Plantillas de los componentes |
| **CSS** | 16 | 738 | Estilos de componentes y global |
| **SQL** | 1 | ~140 | Esquema de la base de datos |
| **Markdown** | 4 | — | Informes y documentación |
| **Bash** | 1 | — | Script de pruebas de la API |

### Stack del backend

| Tecnología | Versión | Para qué |
|---|---|---|
| Java | 17 LTS | Lenguaje del backend |
| Spring Boot | 3.3.5 | Framework base, servidor embebido y configuración automática |
| Spring Web | 3.3.x | Los 8 controladores REST |
| Spring Data JPA | 3.3.x | Los 8 repositorios y el mapeo de entidades |
| Hibernate | 6.5.x | Motor de persistencia (implementación de JPA) |
| Spring Security | 6.3.x | Autenticación stateless y autorización por rol |
| JJWT | 0.12.6 | Emisión y validación del token JWT (HS256) |
| springdoc-openapi | 2.6.0 | Swagger UI y especificación OpenAPI 3 |
| Maven | 3.9+ | Empaquetado |
| MySQL | 8.0 | Base de datos (perfil `dev`: H2 en memoria) |
| spring-security-crypto (BCrypt) | 6.3.x | Hash de contraseñas (viene con Spring Security) |

### Stack del frontend

| Tecnología | Versión | Para qué |
|---|---|---|
| Angular | 21.2.25 | Framework de la SPA, componentes standalone |
| TypeScript | 5.9 | Tipado estricto de modelos y servicios |
| Angular Signals | — | `input()` y `output()` en componentes |
| Bootstrap | 5.3 | Maquetación y responsive |
| RxJS | 7.8 | Observables de los servicios HTTP |
| Angular CLI / @angular/build | 21.2 | Servidor de desarrollo y compilación |
| Vitest | — | Runner de pruebas unitarias del frontend |

---

## 3. Cómo se levantó el proyecto

El proyecto pasó por dos etapas. La primera fue la construcción inicial con Jakarta Servlets y
JDBC sobre SQL Server. La segunda, que es la versión actual, fue la migración completa a Spring
Boot, demanded por el profesor.

### Etapa 1 — versión inicial (Servlets + JDBC)

Se construyó el sistema con Servlets 6, JSP, acceso a datos por JDBC manual sobre SQL Server
Express, autenticación con un `TokenStore` en memoria y frontend en Angular 19.

### Etapa 2 — migración a Spring Boot (versión actual)

El Capítulo 4 de la primera versión del informe **argumentaba en contra de Spring y de JPA**, con
una matriz de ponderación que daba 4,65 a Servlets frente a 3,15 a Spring. Al contrastarlo con el
sílabo del curso, esa justificación no se sostenía: la alternativa elegida no podía aportar
transacciones con rollback, Spring Security con JWT, paginación de Spring Boot ni JPQL con
parámetros nombrados, que son requisitos explícitos de las unidades III y IV.

La decisión corregida es Spring Boot 3.3 con Spring Data JPA, y la nueva ponderación es 4,90
frente a 1,80.

**Qué se migró y cómo:**

| Antes (Servlets + JDBC) | Ahora (Spring Boot + JPA) |
|---|---|
| `@WebServlet` + `doGet/doPost/doPut/doDelete` | 8 `@RestController` con anotaciones OpenAPI |
| Clases DAO con `PreparedStatement` a mano | 8 repositorios `JpaRepository` + JPQL con parámetros nombrados |
| Transacciones abiertas y cerradas a mano | `@Transactional` en la capa de servicio, con rollback automático |
| `TokenStore` en memoria (se perdía al reiniciar) | JWT HS256 firmado, stateless |
| `AuthFilter` + `AuthUtil` escritos a mano | `JwtAuthenticationFilter` + `@PreAuthorize` |
| `CorsFilter` propio | `CorsConfigurationSource` de Spring Security |
| Sin paginación | `Pageable` con tope de 200 registros por página |
| `Gson` manual | Jackson con `@JsonFormat` en fechas y horas |
| Sin documentación | Swagger UI autogenerado |
| SQL Server Express | MySQL 8 (con perfil H2 para pruebas) |
| WAR en Tomcat 10 | JAR ejecutable con Tomcat embebido |

**Qué se conservó:** toda la lógica de negocio, los mensajes en español, los códigos de estado HTTP
y los casos de prueba. El código de Servlets se archivó y sirvió de especificación funcional durante
la migración.

---

## 4. Puesta en marcha paso a paso

### Requisitos previos

| Herramienta | Versión mínima | Cómo verificar |
|---|---|---|
| Java | 17 | `java -version` |
| Maven | 3.9 | `mvn -v` |
| Node.js | 20 o superior | `node -v` |
| npm | 10 | `npm -v` |
| MySQL | 8 (opcional) | `mysql --version` |
| Git Bash (opcional) | — | Solo si vas a correr el script de pruebas |

> MySQL es **opcional**: el perfil `dev` usa H2 en memoria y crea los datos de prueba solo. Para la
> demostración en clase no hace falta instalar nada.

### Paso 1 — Clonar el repositorio

```bash
git clone https://github.com/juliocesarmogollon21/sicimed.git
cd sicimed
```

### Paso 2 — Levantar el backend

```bash
cd backend
mvn -DskipTests clean package
java -jar target/sicimed.jar
```

Salida esperada:

```
Tomcat started on port 8080 (http) with context path '/'
Started SicimedApplication in 17.8 seconds
Datos semilla cargados. Usuarios: admin/admin123, recepcion/recep123, ...
```

**Opción rápida, sin MySQL:**

```bash
java -jar target/sicimed.jar --spring.profiles.active=dev
```

Usa una base H2 en memoria: el esquema se crea solo y se cargan los datos de demostración. Ideal
para presentar; los datos se borran al apagar el servidor.

### Paso 3 — Levantar el frontend

En otra terminal:

```bash
cd frontend
npm install
npx ng serve
```

Abrir http://localhost:4200

> Con npm 11.19 o superior puede aparecer un aviso `install-scripts` sobre paquetes como `esbuild`
> o `@parcel/watcher`. **Son avisos, no errores**: el build funciona igual. Si llegaras a ver un
> fallo, usa `npm install --foreground-scripts`.

### Paso 4 — Verificar que todo responde

```bash
# Estado del backend
curl http://localhost:8080/api/sistema/estado
# {"servicio":"sicimed-backend","estado":"activo","preparado":true,...}

# Swagger UI
# http://localhost:8080/swagger-ui.html
```

El campo `preparado: true` confirma que la base terminó de inicializarse. Es importante esperarlo:
el servidor empieza a aceptar peticiones unos segundos antes de que terminen de sembrarse los datos
de prueba, y un login en esa ventana falla.

### Paso 5 — Iniciar sesión

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | Administrador |
| `recepcion` | `recep123` | Recepción |
| `medico1` | `medico123` | Médico |
| `paciente1` | `paciente123` | Paciente |

También se puede crear una cuenta nueva en `/registro`, que siempre queda con rol `PACIENTE`.

### Variables de entorno del backend

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/sicimed` | Cadena de conexión |
| `DB_USER` | `root` | Usuario de la base |
| `DB_PASSWORD` | *(vacío)* | Contraseña |
| `JWT_SECRET` | clave de desarrollo | **Cambiar en producción** (mínimo 32 caracteres) |
| `SERVER_PORT` | `8080` | Puerto del servidor |
| `DDL_AUTO` | `update` | `update`, `validate` o `create-drop` |

### Paso 5b — Usar MySQL en vez de H2 (opcional)

El comando normal usa MySQL. Si tu MySQL corre en el puerto 3306 con usuario `root` y contraseña
vacía, no hay que configurar nada. Si no es así, define las variables de entorno.

**En Windows (PowerShell):**

```powershell
$env:DB_URL    = "jdbc:mysql://localhost:3306/sicimed?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima"
$env:DB_USER   = "root"
$env:DB_PASSWORD = "tu_clave"
java -jar target/sicimed.jar
```

**En Linux / macOS:**

```bash
export DB_URL='jdbc:mysql://localhost:3306/sicimed?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima'
export DB_USER='root'
export DB_PASSWORD='tu_clave'
java -jar target/sicimed.jar
```

**Crear la base (opcional):** con `ddl-auto=update`, Hibernate crea las tablas solo al arrancar.
Para crearlas a mano:

```bash
mysql -u root -p < sql/01_create_sicimed_mysql.sql
```

Los datos de prueba (usuarios, sedes, especialidades) se cargan **solo si la tabla de usuarios
está vacía**. Si ya hay datos, no se toca nada.

**Usar un usuario de MySQL con menos privilegios** en vez de `root`:

```sql
CREATE DATABASE sicimed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sicimed'@'localhost' IDENTIFIED BY 'una_clave_fuerte';
GRANT ALL PRIVILEGES ON sicimed.* TO 'sicimed'@'localhost';
FLUSH PRIVILEGES;
```

### Problemas frecuentes al levantar

| Síntoma | Causa | Solución |
|---|---|---|
| `Port 8080 was already in use` | Otro servicio ocupa el puerto | `SERVER_PORT=8090 java -jar target/sicimed.jar`, o detener el proceso antiguo |
| Login inválido justo al arrancar | Se consultó antes de que terminara la carga de datos | Esperar `preparado: true` en `/api/sistema/estado` |
| `Access blocked by CORS` en el navegador | El frontend corre en un puerto distinto de 4200 | Agregar ese origen en `SecurityConfig.setAllowedOrigins` |
| `Public Key Retrieval is not allowed` | Driver MySQL antiguo o SSL mal configurado | Agregar `allowPublicKeyRetrieval=true` a `DB_URL` |
| `ng serve` no encuentra el puerto 4200 | Otro proceso lo ocupa | `npx ng serve --port 4300` y actualizar `apiUrl` |
| Avisos `install-scripts` al instalar | npm 11.19+ bloquea scripts de instalación por seguridad | Son avisos, no errores: el build funciona igual. Si aun así falla, `npm install --foreground-scripts` |

---

## 5. Estructura del proyecto

```
sicimed/
├── README.md
├── backend/                          Spring Boot (JAR)
│   ├── pom.xml
│   ├── sql/
│   │   └── 01_create_sicimed_mysql.sql
│   └── src/main/
│       ├── java/com/sicimed/
│   │   ├── SicimedApplication.java         Punto de entrada
│       │   ├── modelo/          10 clases   Entidades JPA y enumeraciones
│       │   │                              Usuario, Rol, Paciente, Medico,
│       │   │                              Especialidad, Sede, Cita, EstadoCita,
│       │   │                              Receta, Medicamento
│       │   ├── dto/             21 clases   Contratos de entrada/salida (records)
│       │   │                              LoginRequest, CitaRequest, CitaResponse,
│       │   │                              PageResponse, AuthResponse, ...
│       │   ├── repositorio/      8 clases   JpaRepository + JPQL
│       │   │                              CitaRepositorio, UsuarioRepositorio, ...
│       │   ├── servicio/         5 clases   Reglas de negocio (@Transactional)
│       │   │                              AuthServicio, CitaServicio,
│       │   │                              CatalogoServicio, MedicoServicio,
│       │   │                              UsuarioServicio
│       │   ├── controlador/      8 clases   @RestController
│       │   │                              AuthController, CitaController,
│       │   │                              ReporteController, MedicoController,
│       │   │                              PacienteController, CatalogoController,
│       │   │                              UsuarioController, SistemaController
│       │   ├── seguridad/        4 clases   JWT y principal autenticado
│       │   │                              JwtServicio, JwtAuthenticationFilter,
│       │   │                              SicimedPrincipal, ActualizadorAutenticacion
│       │   ├── configuracion/    4 clases   SecurityConfig, OpenApiConfig,
│       │   │                              DatosSemilla, EstadoSemilla
│       │   └── excepcion/        5 clases   Excepciones + GlobalExceptionHandler
│       └── resources/
│           ├── application.yml            Configuración (MySQL por defecto)
│           └── application-dev.yml        Perfil H2 para desarrollo
│
├── frontend/                         Angular 21
│   ├── package.json
│   ├── angular.json
│   └── src/
│       ├── main.ts
│       ├── styles.css
│       ├── environments/
│       │   ├── environment.ts             apiUrl del backend
│       │   └── environment.development.ts
│       └── app/
│           ├── app.ts / app.html / app.css        Shell + menú por rol
│           ├── app.routes.ts                      Rutas protegidas por guardas
│           ├── app.config.ts                      provideRouter + HttpClient
│           ├── interceptors/
│           │   └── auth.interceptor.ts            Adjunta Bearer, maneja 401
│           ├── modelos/                  7       Interfaces tipadas
│           │                                      Cita, Medico, Sede, Especialidad,
│           │                                      Medicamento, LoginResponse, Pagina
│           ├── servicios/               10       Un servicio por recurso
│           │                                      AuthService, CitaService, MedicoService,
│           │                                      SedeService, EspecialidadService,
│           │                                      MedicamentoService, UsuarioService,
│           │                                      PacienteService, auth.guard, role.guard
│           └── componentes/
│               ├── auth/                2        login, registro
│               ├── dashboard/           1        Panel principal
│               ├── citas/               3        lista, nueva, reprogramar
│               ├── medico/              1        agenda-medico
│               └── configuracion/       6        hub + sedes, especialidades,
│                                                 medicos, usuarios, medicamentos
│                                                 (mas shared/config-shared.css)
│
└── docs/
    ├── CAPITULO_04_ARQUITECTURA.md      Capítulo 4 del informe
    ├── CAPITULO_05_08_INFORME.md        Capítulos 5 a 8
    ├── SICIMED_openapi.json             Especificación OpenAPI exportada
    └── probar_api.sh                    46 casos de prueba de la API
```

### Cómo está dividido el backend

El flujo de una petición va siempre en el mismo sentido y cada capa tiene una sola responsabilidad:

```
Petición HTTP
    │
    ▼
JwtAuthenticationFilter          valida el token y arma el principal
    │
    ▼
@RestController                  valida el cuerpo (Bean Validation) y el rol (@PreAuthorize)
    │                            — no contiene SQL ni reglas de negocio
    ▼
@Service   (@Transactional)      reglas de negocio, control de acceso, rollback
    │                            — no sabe nada de HTTP ni de SQL
    ▼
JpaRepository                    JPQL con parámetros nombrados
    │
    ▼
Hibernate → MySQL
```

Reglas que se respetan en todo el código:
- Los controladores no escriben SQL.
- Los repositorios no contienen reglas de negocio.
- Los servicios no saben nada de HTTP.
- Los DTO son `record` inmutables; las entidades nunca se exponen directamente en la API.

### Cómo está dividido el frontend

```
Componente (HTML + TS)
    │  nunca escribe URLs
    ▼
Servicio  (src/app/servicios/)     una llamada HTTP tipada por recurso
    │  pasa por el interceptor
    ▼
auth.interceptor.ts                adjunta Authorization: Bearer
    ▼
Backend
```

- Un componente llama a un servicio; el servicio devuelve un `Observable` tipado.
- El token se añade en un solo lugar (el interceptor), no en cada componente.
- Las rutas se protegen con `authGuard` y `roleGuard`.
- Los modelos de `src/app/modelos/` definen la forma de los datos en ambos sentidos.

---

## 6. Qué nos brinda

### Para el paciente
- Registrarse sin intervención del administrador.
- Ver solo sus propias citas: el backend filtra por rol, no el frontend.
- Reservar eligiendo sede, especialidad, médico y hora realmente libre.
- Reprogramar o cancelar mientras la cita esté pendiente.
- Ver la receta cuando la cita ya fue atendida.

### Para recepción
- Buscar pacientes por DNI y autocompletar el formulario.
- Ver el listado y el calendario semanal de citas.
- Detectar choques de horario: el sistema responde 409 en vez de duplicar.
- Cancelar citas y liberar el horario al instante.

### Para el médico
- Agenda del día con separación entre «Por atender» y «Atendidos».
- Registrar el diagnóstico, que pasa la cita a ATENDIDO.
- Emitir receta digital eligiendo del catálogo de medicamentos.
- No puede ver ni tocar citas de otros médicos.

### Para el administrador
- Mantener catálogos (sedes, especialidades, médicos, usuarios, medicamentos) sin tocar la base.
- Baja lógica: desactivar en vez de borrar, para no romper el historial.
- Reportes filtrados por médico, estado y rango de fechas.

### Técnicas
- API documentada y probable desde el navegador (Swagger UI).
- Suite de 46 pruebas de API que se puede repetir.
- Despliegue como un solo JAR, sin contenedor que instalar.
- Documentación OpenAPI exportable e importable en Postman.

---

## 7. Roles y permisos

| Capacidad | ADMIN | RECEPCIONISTA | MEDICO | PACIENTE |
|---|---|---|---|---|
| Ver todas las citas | Sí | Sí | Solo su agenda | Solo las suyas |
| Crear cita | Sí | Sí | No | Sí (para sí mismo) |
| Reprogramar / cancelar | Sí | Sí | No | Sí, si es suya y está PENDIENTE |
| Registrar diagnóstico | Sí | No | Sí, su cita | No |
| Emitir receta | Sí | No | Sí, su cita | No |
| Ver la receta | Sí | — | Sí | Sí, si la cita está ATENDIDO |
| CRUD de catálogos | Sí | No | No | No |
| Ver reportes | Sí | Sí | No | No |
| Buscar paciente por DNI | Sí | Sí | Sí | No |

El aislamiento por rol se aplica **en el backend**, con `@PreAuthorize` y un control de pertenencia
en el servicio. Ocultar un botón en la interfaz no es una medida de seguridad: el frontend es
público.

---

## 8. API REST

- **Base URL:** `http://localhost:8080/api`
- **Formato:** JSON (`application/json;charset=UTF-8`)
- **Autenticación:** `Authorization: Bearer <token>` salvo en `/api/auth/**` y `/api/sistema/**`
- **Documentación:** http://localhost:8080/swagger-ui.html

### Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/sistema/estado` | Estado del backend (público) |
| POST | `/api/auth/login` | Iniciar sesión, devuelve token y rol |
| POST | `/api/auth/register` | Registrar paciente |
| GET | `/api/citas` | Listado paginado. Filtros: `dni`, `medicoId`, `fecha` |
| POST | `/api/citas` | Agendar cita (409 si el horario está ocupado) |
| GET | `/api/citas/disponibilidad` | Horas libres de un médico en una fecha |
| GET | `/api/citas/{id}` | Obtener una cita |
| PUT | `/api/citas/{id}` | Reprogramar |
| DELETE | `/api/citas/{id}` | Cancelar |
| POST | `/api/citas/{id}/diagnostico` | Registrar diagnóstico → ATENDIDO |
| GET / POST | `/api/citas/{id}/receta` | Ver y emitir receta |
| GET | `/api/reportes/citas` | Reporte con filtros y paginación |
| GET | `/api/medicos` | Catálogo, con filtros por sede y especialidad |
| CRUD | `/api/sedes` | Sedes |
| CRUD | `/api/especialidades` | Especialidades |
| CRUD | `/api/medicamentos` | Medicamentos |
| CRUD | `/api/usuarios` | Usuarios (solo ADMIN) |
| GET | `/api/pacientes?dni=` | Buscar paciente |

### Códigos de respuesta

| Código | Significado |
|---|---|
| 200 | Operación exitosa |
| 201 | Recurso creado |
| 400 | Datos inválidos o regla de negocio incumplida |
| 401 | Token ausente, inválido o expirado |
| 403 | Rol sin permiso, o la cita no le pertenece |
| 404 | Recurso inexistente |
| 409 | Conflicto: horario ocupado, usuario repetido |
| 500 | Error no controlado |

Todos los errores comparten la misma forma:

```json
{ "status": 409, "message": "Ya existe una cita para ese medico en la fecha y hora indicadas" }
```

### Paginación

Los listados largos devuelven una envoltura uniforme:

```json
{ "content": [], "page": 0, "size": 20, "totalElements": 137,
  "totalPages": 7, "first": true, "last": false }
```

El tamaño de página tiene un tope de 200 registros.

### Probar la API desde Swagger

1. Abrir http://localhost:8080/swagger-ui.html
2. Ejecutar `POST /api/auth/login` con `admin` / `admin123`
3. Copiar el valor de `token` de la respuesta
4. Pulsar **Authorize**, pegarlo y confirmar
5. Probar cualquier endpoint

---

## 9. Pruebas

El script `docs/probar_api.sh` ejecuta **46 casos** contra la API en marcha.

```bash
# Con el backend levantado
bash docs/probar_api.sh http://localhost:8080
```

```
Aprobados: 46   Fallidos: 0
Todas las pruebas pasaron.
```

> **El script es para Git Bash / Linux / WSL.** Usa `date -d` y `cygpath`, que no existen en el
> `cmd` de Windows ni en macOS. Si usas esos sistemas, cambia la línea que calcula `FECHA` por
> `FECHA=$(date +%Y-%m-%d)` y quita la línea de `cygpath`. En PowerShell conviene usar
> `Git Bash` como terminal.
>
> Si prefieres no usar Bash, la alternativa es probar los 46 casos desde Swagger UI
> (http://localhost:8080/swagger-ui.html), que documenta los mismos endpoints.

**Cobertura**

| Bloque | Casos |
|---|---|
| Autenticación | Login válido e inválido, petición sin token, token corrupto |
| Registro | Alta de paciente, usuario repetido, validaciones de entrada |
| Autorización por rol | Un paciente no puede crear ni listar usuarios |
| Ciclo de cita | Disponibilidad, creación, conflicto 409, fecha pasada, consulta |
| Atención médica | Diagnóstico, receta, bloqueo de reprogramar una cita atendida |
| Aislamiento | Paciente no ve citas ajenas ni puede diagnosticar |
| Filtros | Por `medicoId`, por `fecha`, combinados, y acotamiento del rol MÉDICO |
| Paginación y reportes | Página paginada, reporte filtrado, reporte restringido |
| Catálogos | Alta, baja lógica, validación de campos obligatorios |
| Formato y documentación | Hora en `HH:mm`, OpenAPI, Swagger UI |

El script es **idempotente**: crea un usuario distinto en cada ejecución y reserva la primera hora
realmente libre, así que puede repetirse contra un servidor ya sembrado sin fallar.

---

## 10. Migración de arquitectura

El capítulo 4 del informe documenta esta decisión. Resumen de la comparación:

| Criterio | Peso | Spring Boot + JPA | Servlets + JDBC |
|---|---|---|---|
| Alineación con los saberes del sílabo | 25% | 5 | 1 |
| Seguridad sin código propio | 20% | 5 | 2 |
| Transacciones y consistencia | 15% | 5 | 2 |
| Velocidad de desarrollo | 15% | 5 | 2 |
| Documentación OpenAPI | 10% | 5 | 1 |
| Control fino del SQL | 10% | 3 | 5 |
| Simplicidad del despliegue | 5% | 5 | 3 |
| **Total ponderado** | 100% | **4,90** | **1,80** |

La alternativa descartada no puede cubrir los requisitos del curso sin reconstruir a mano lo que el
framework ya entrega.

---

## 11. Problemas encontrados y cómo se resolvieron

Cada uno tiene ahora una prueba que lo cubre.

| # | Problema | Causa | Solución |
|---|---|---|---|
| I-01 | El navegador bloqueaba las llamadas al API | Front y back en orígenes distintos; el preflight se rechazaba | CORS configurado en Spring Security, limitado al origen del frontend |
| I-02 | Tras el login, el resto de peticiones daba 401 | Los componentes no enviaban el token | `authInterceptor` añade el encabezado en un solo lugar |
| I-03 | La hora salía como `10:00:00` pero la disponibilidad como `10:00` | Jackson serializa `LocalTime` con segundos | `@JsonFormat(pattern = "HH:mm")` en entidad y DTOs |
| I-04 | El primer login tras arrancar fallaba | El servidor acepta peticiones antes de terminar la carga de datos | Indicador `preparado` en `/api/sistema/estado` |
| I-05 | Un paciente podía ver citas de otros | El listado devolvía todo | Filtrado por rol en el servicio y control de pertenencia |
| I-06 | Reprogramar manteniendo el horario daba conflicto | La validación incluía la propia cita | `contarConflictos` recibe `excludeId` |
| I-07 | El token se perdía al reiniciar | `TokenStore` en memoria | JWT firmado, stateless |
| I-08 | Se perdían recetas antiguas en texto plano | Cambió el formato a JSON | Lectura tolerante con respaldo línea por línea |
| I-09 | La agenda de un médico mostraba citas de otros | El backend solo acotaba al rol MÉDICO y sin filtros de médico ni fecha | Filtros aplicados en SQL antes de paginar |
| I-10 | La cabecera de autorización llegaba vacía | Literal roto en el interceptor, que TypeScript compilaba sin avisar | Corregido y verificado sobre el bundle compilado |

Dos de estos merecen explicación:

**El caso I-10 es el más importante para el trabajo en equipo.** El interceptor tenía
`Authorization: *** ${token}` en vez de ``Authorization: `Bearer ${token}` ``. TypeScript lo leía
como una expresión de división, así que `ng build` terminaba **sin errores** y el bundle se
generaba con normalidad. Pero la cabecera que llegaba al servidor era inútil y toda petición
autenticada recibía 401. El error solo apareció al inspeccionar el JavaScript compilado. La lección:
en un trabajo en grupo, compilar no es verificar.

**El caso I-09 es un error de diseño, no un despiste.** La primera solución fue filtrar en el
cliente, lo que funcionaba con pocas citas pero dejaba páginas incompletas: el filtro se aplicaba
después de paginar. La corrección fue mover el filtro al servidor.

---

## 12. Seguridad

| Medida | Implementación |
|---|---|
| Contraseñas | Hash **BCrypt** con sal. Nunca en texto plano ni devueltas por la API |
| Token | **JWT HS256** firmado, stateless, con expiración configurable (8 h por defecto) |
| Autorización | `@PreAuthorize` por rol + control de pertenencia en el servicio |
| Inyección SQL | Consultas parametrizadas: JPQL con parámetros nombrados en toda la capa de datos |
| Validación | Bean Validation declarativa con mensajes en español |
| CORS | Limitado a los orígenes del frontend en desarrollo |
| Datos sensibles | El hash de contraseña nunca aparece en ninguna respuesta de la API |

**Antes de desplegar en producción:**

1. Definir `JWT_SECRET` con al menos 32 caracteres aleatorios.
2. Cambiar el CORS al dominio real del frontend.
3. Usar un usuario de MySQL con permisos mínimos, no `root`.
4. Poner `DDL_AUTO=validate` para que no se modifique el esquema en producción.

---

## 13. Trabajo pendiente

| Pendiente | Estado |
|---|---|
| Panel de indicadores avanzado (RF-18) | Parcial: hay conteos simples, no es BI |
| Bloques horarios configurables por médico | No implementado: turnos fijos de 08:00 a 17:00 |
| Envío de recordatorios por correo | No implementado |
| Exportación de reportes a PDF y Excel | No implementado |
| Pruebas automatizadas en CI | Pendiente: hay script de humo, falta JUnit |
| Migrar a OAuth 2 con proveedor externo | Pendiente: hoy es JWT propio |

---

---

## Documentación relacionada

| Documento | Contenido |
|---|---|
| [frontend/README.md](frontend/README.md) | Detalles del frontend Angular, configuración y solución de problemas |
| [docs/CAPITULO_04_ARQUITECTURA.md](docs/CAPITULO_04_ARQUITECTURA.md) | Capítulo 4 del informe: comparación de alternativas |
| [docs/CAPITULO_05_08_INFORME.md](docs/CAPITULO_05_08_INFORME.md) | Capítulos 5 a 8 del informe |
| [docs/SICIMED_openapi.json](docs/SICIMED_openapi.json) | Especificación OpenAPI (importable en Postman) |

---

## Información del curso

| Campo | Detalle |
|---|---|
| Curso | SIST1402A — Soluciones Web y Aplicaciones Distribuidas |
| Docente | Víctor Alfredo Muguerza Capristan |
| Grupo | Grupo 3 |
| Integrantes | Isaac Anderson Ballena Perez · Angel Jefferson Diaz Leyva · Julio Cesar Mogollon Carranza · Steven Edson Francescoly Suclupe Vela |
| Repositorio | https://github.com/juliocesarmogollon21/sicimed |
