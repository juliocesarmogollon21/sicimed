# SICIMED — Backend Spring Boot

API REST del Sistema de Citas Médicas: **Spring Boot 3.3**, empaquetada como **WAR** y desplegada en
**Apache Tomcat 10.1** bajo el contexto `/sicimed`. Tiene **10 controladores**, **8 servicios**,
**8 repositorios** y **38 endpoints**; el script `docs/probar_api.sh` la verifica con **46 casos**.

Visión general del proyecto: [README principal](../README.md). Cliente web: [frontend/README.md](../frontend/README.md).

---

## Índice

- [1. Tecnologías](#1-tecnologías)
- [2. Requisitos](#2-requisitos)
- [3. Puesta en marcha](#3-puesta-en-marcha)
  - [Paso 1 — Instalar JDK 17 y Tomcat 10.1 y registrarlo en VS Code](#paso-1--instalar-jdk-17-y-tomcat-101-y-registrarlo-en-vs-code)
  - [Paso 2 — Instalar MySQL 8 y crear la base de datos](#paso-2--instalar-mysql-8-y-crear-la-base-de-datos)
  - [Paso 3 — Generar el WAR](#paso-3--generar-el-war)
  - [Paso 4 — Desplegar el WAR en Tomcat](#paso-4--desplegar-el-war-en-tomcat)
  - [Paso 5 — Verificar que todo responde](#paso-5--verificar-que-todo-responde)
  - [Problemas frecuentes al levantar](#problemas-frecuentes-al-levantar)
- [4. Perfil `dev` con H2 (sin MySQL)](#4-perfil-dev-con-h2-sin-mysql)
- [5. Estructura de paquetes](#5-estructura-de-paquetes)
- [6. API REST](#6-api-rest)
- [7. Seguridad y manejo de errores](#7-seguridad-y-manejo-de-errores)
- [8. Pruebas](#8-pruebas)
- [9. Lineamientos del curso en el backend](#9-lineamientos-del-curso-en-el-backend)
- [10. Decisión de arquitectura](#10-decisión-de-arquitectura)
- [11. Problemas encontrados y cómo se resolvieron](#11-problemas-encontrados-y-cómo-se-resolvieron)

---

## 1. Tecnologías

| Tecnología | Versión | Para qué |
|---|---|---|
| Java | 17 LTS | Lenguaje del backend |
| Spring Boot | 3.3.5 | Framework base, servidor embebido y configuración automática |
| Spring Web | 3.3.x | Los 10 controladores REST |
| Spring Data JPA | 3.3.x | Los 8 repositorios y el mapeo de entidades |
| Hibernate | 6.5.x | Motor de persistencia (implementación de JPA) |
| Spring Security | 6.3.x | Autenticación stateless y autorización por rol |
| JJWT | 0.12.6 | Emisión y validación del token JWT (HS256) |
| springdoc-openapi | 2.6.0 | Swagger UI y especificación OpenAPI 3 |
| Maven | 3.9+ | Empaquetado |
| MySQL | 8.0 | Base de datos (perfil `dev`: H2 en memoria) |
| spring-security-crypto (BCrypt) | 6.3.x | Hash de contraseñas (viene con Spring Security) |

---

## 2. Requisitos

| Herramienta | Versión mínima | Cómo verificar |
|---|---|---|
| JDK | 17 | `java -version` |
| Maven | 3.9 o superior | `mvn -v` |
| MySQL / MariaDB | 8 / 10.4 | Servicio corriendo en el puerto 3306 |
| Apache Tomcat | 10.1 | `bin\version.bat` (ver Paso 1) |
| VS Code + Community Server Connectors | — | Panel **SERVERS** (ver Paso 1) |
| Git Bash | — | Solo para ejecutar el script de pruebas |

---

## 3. Puesta en marcha

> **Elige una forma antes de empezar.** Hay dos, y lo que cambia entre ellas es la **URL de la
> API**, porque al desplegar en Tomcat la aplicación queda bajo el contexto `/sicimed`:
>
> | | Opción A — Tomcat + MySQL (principal) | Opción B — standalone con perfil `dev` |
> |---|---|---|
> | URL de la API | `http://localhost:8080/sicimed/api` | `http://localhost:8080/api` |
> | Requisitos | JDK 17, Tomcat 10.1, MySQL 8 | Solo JDK 17 |
> | Pasos | Los de abajo | Ver [sección 4](#4-perfil-dev-con-h2-sin-mysql) |
>
> La **Opción A es la principal** y la que se usa en el laboratorio. Si no quieres crear la base
> de datos en MySQL ni usar Tomcat, ve directo a la Opción B.
>
> Si el frontend muestra `Credenciales inválidas` o `401 No autenticado`, casi siempre es porque
> el `apiUrl` no coincide con la URL de esta tabla. Ver
> [frontend/README.md](../frontend/README.md#configurar-la-url-del-api).

### Opción A — Tomcat + MySQL (la principal, usada en el laboratorio)

Orden: instalar JDK y Tomcat (una vez), instalar MySQL y crear la base (una vez), generar el WAR
y desplegarlo.

**Todos los comandos de este documento se ejecutan desde la terminal integrada de Visual Studio
Code** (Ctrl+`), salvo los que se indican en el panel de Maven o en el panel Servers.

### Paso 1 — Instalar JDK 17 y Tomcat 10.1 y registrarlo en VS Code

Este paso se hace una sola vez por computadora. Si en el panel **SERVERS** de VS Code ya aparece un
Tomcat 10.1 registrado y `java -version` muestra la versión 17, se puede pasar al Paso 2.

**1. Instalar JDK 17**

Si `java -version` no muestra una versión 17 o superior, instalar un JDK 17, por ejemplo
**Eclipse Temurin 17** desde https://adoptium.net (instalador `.msi` para Windows x64). Lo
necesitan Maven (Paso 3) y Tomcat.

**2. Configurar `JAVA_HOME`**

En Windows:

1. Buscar **Editar las variables de entorno del sistema** → botón **Variables de entorno...**
2. En **Variables del sistema** → **Nueva...**:
   - Nombre: `JAVA_HOME`
   - Valor: la carpeta del JDK, por ejemplo `C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot`
3. Seleccionar la variable **Path** → **Editar...** → **Nuevo** → `%JAVA_HOME%\bin`
4. Aceptar todo y abrir una terminal **nueva** (las que ya estaban abiertas no ven el cambio).
   Verificar:

```bat
java -version
echo %JAVA_HOME%
```

En PowerShell, el equivalente de `echo %JAVA_HOME%` es `$env:JAVA_HOME`. El instalador de Temurin
también ofrece la opción *Set JAVA_HOME variable*, que hace los puntos 2 y 3 automáticamente.

**3. Descargar Apache Tomcat 10.1**

SICIMED usa Spring Boot 3 (Jakarta EE, paquetes `jakarta.*`), así que necesita **Tomcat 10.1**
(Jakarta EE 10, compatible con JDK 17). Tomcat 9 o anterior no sirve.

1. Entrar a https://tomcat.apache.org/download-10.cgi
2. En **Binary Distributions → Core**, descargar **64-bit Windows zip** (o el
   **32-bit/64-bit Windows Service Installer** si se prefiere el instalador).
3. Descomprimir el zip en una ruta **sin espacios**, por ejemplo `C:\tomcat\apache-tomcat-10.1.x`.
   Dentro deben quedar las carpetas `bin`, `conf`, `lib` y `webapps`.

**4. Registrar Tomcat en VS Code**

1. Instalar la extensión **Community Server Connectors** de Red Hat
   (`redhat.vscode-community-server-connector`). Se instala junto con su dependencia
   **Runtime Server Protocol UI** (`redhat.vscode-rsp-ui`).
2. En el Explorador de VS Code aparece el panel **SERVERS** con el nodo
   **Community Server Connector**. Esperar a que quede en *(Started)*.
3. Clic derecho sobre **Community Server Connector** → **Create New Server...**
4. A la pregunta **Download server?** elegir **No, use server on disk**. (Con **Yes** la extensión
   descarga un Tomcat por su cuenta; en ese caso, elegir una versión 10.1.)
5. En el selector de carpetas, elegir la carpeta de Tomcat (`C:\tomcat\apache-tomcat-10.1.x`) y
   pulsar **Select desired server location**.
6. Se abre el formulario **New Server: ...** con el nombre y las propiedades detectadas. Pulsar
   **Finish**.

El servidor queda registrado así:

```
Community Server Connector
└── apache-tomcat-10.1.x (Stopped)
```

> Si la extensión no encuentra Java, indicar el JDK en la configuración de VS Code:
> `"rsp-ui.rsp.java.home": "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.x-hotspot"` (con las
> barras invertidas duplicadas).

**5. Comprobar que el puerto 8080 está libre**

Antes de encender Tomcat, verificar que ningún otro programa usa el puerto 8080:

```bat
netstat -ano | findstr :8080
```

En PowerShell: `Get-NetTCPConnection -LocalPort 8080 -State Listen` (la columna `OwningProcess`
es el PID).

Si no aparece nada, el puerto está libre. Si aparece una línea `LISTENING`, la última columna es el
PID del proceso que lo ocupa. Hay dos opciones:

- Ver qué programa es con `tasklist /FI "PID eq <pid>"` y, si se puede cerrar, terminarlo con
  `taskkill /PID <pid> /F` (puede requerir una terminal como administrador).
- O cambiar el puerto de Tomcat en `conf\server.xml`, en la línea
  `<Connector port="8080" protocol="HTTP/1.1" ...>` (por ejemplo a `8081`). En ese caso hay que
  usar el puerto nuevo en todas las URL de esta guía y en `apiUrl` del frontend (ver [frontend/README.md](../frontend/README.md#configurar-la-url-del-backend)).

### Paso 2 — Instalar MySQL 8 y crear la base de datos

**1. Instalar MySQL Server 8**

Descargar **MySQL Installer** para Windows desde https://dev.mysql.com/downloads/installer/
(`mysql-installer-web-community` si hay internet durante la instalación). En el asistente,
instalar **MySQL Server 8.0** (y, si se quiere, MySQL Workbench), dejar el puerto **3306** y
definir la contraseña de `root`. El servicio de Windows queda con el nombre **MySQL80**.

**2. Arrancar el servicio**

- `services.msc` → **MySQL80** → **Iniciar**, o
- en una terminal abierta **como administrador**: `net start MySQL80`

**3. Verificar que responde en el puerto 3306**

```bat
netstat -ano | findstr :3306
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "SELECT VERSION();"
```

Debe aparecer una línea `LISTENING` en el 3306 y, tras escribir la contraseña, la versión 8.0.x.

**4. Credenciales que usa el backend**

`backend/src/main/resources/application.yml` toma la conexión de variables de entorno, con estos
valores por defecto:

| Variable | Valor por defecto |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/sicimed?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | *(vacía)* |

Si `root` tiene contraseña (lo normal con MySQL Installer), hay que pasársela a Tomcat en
`DB_PASSWORD`, de una de estas dos formas:

- Como variable de entorno de Windows (igual que `JAVA_HOME`), y reiniciar VS Code.
- O en el panel **SERVERS**: clic derecho sobre el Tomcat → **Edit Server**, agregar al JSON
  `"mapProperty.launch.env": { "DB_PASSWORD": "tu_contraseña" }` y guardar.

Sin esto, el despliegue falla con `Access denied for user 'root'@'localhost'`.

**5. Crear la base `sicimed`**

El backend usa MySQL/MariaDB. Si la base `sicimed` no existe, el despliegue falla con
`Unknown database 'sicimed'` en el log de Tomcat.

Crearla una vez:

```bash
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "CREATE DATABASE sicimed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

O importando el script del repositorio:

```bat
REM desde la raiz del repositorio, en cmd (PowerShell no admite la redireccion <)
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < backend\sql\01_create_sicimed_mysql.sql
```

Los datos de prueba (usuarios, sedes, especialidades, medicamentos) se cargan **solo si la tabla de
usuarios está vacía** (clase `DatosSemilla`). El script SQL solo crea la base y las tablas; no
inserta catálogos, porque la semilla chocaría con el `UNIQUE` de especialidades y el backend no
arrancaría. En despliegues repetidos no se tocan los datos existentes.

Si no se quiere instalar MySQL, ver [Perfil `dev` con H2](#4-perfil-dev-con-h2-sin-mysql).

### Paso 3 — Generar el WAR

El backend se empaqueta como **WAR** para desplegarlo en Tomcat.

**Desde Visual Studio Code** (la forma usada en el laboratorio): situar el cursor sobre la carpeta
`backend`, hacer clic derecho y elegir **Run Maven Commands...**. Ejecutar en este orden:

1. **clean** — borra la carpeta `target/`
2. **package** — compila y genera `target/sicimed.war`

> Ejecutar `clean` antes de `package` es importante. Sin `clean`, Maven puede dejar clases de una
> compilación anterior y el WAR se genera con código desactualizado.

**Desde la terminal:**

```bash
cd backend
mvn clean package
```

Resultado: `backend/target/sicimed.war`

### Paso 4 — Desplegar el WAR en Tomcat

El despliegue se hace desde Visual Studio Code, en el panel **SERVERS**, con el Tomcat registrado en
el Paso 1. Antes, MySQL debe estar en marcha con la base `sicimed` creada (Paso 2) y el puerto 8080
libre.

**1. Añadir el despliegue**

Clic derecho sobre el servidor Tomcat → **Add Deployment**:

1. **What type of deployment do you want to add?** → **File**
2. En el selector de archivos, elegir `backend\target\sicimed.war`
3. **Do you want to edit optional deployment parameters?** → **No**

Tomcat publica cada WAR usando el nombre del archivo como *context path*: `sicimed.war` queda en
`/sicimed`, y por eso la URL de la API empieza por `/sicimed`. No hay que renombrar el WAR.

**2. Encender el servidor y publicar**

Clic derecho sobre el servidor → **Start Server** (o el botón ▶ que aparece al seleccionarlo). El
estado cambia a **(Started)** y el puerto **8080** queda ocupado por Tomcat. Si el despliegue no
figura como publicado, clic derecho sobre el servidor → **Publish Server (Full)**.

**3. Esperar a que publique**

En la consola de abajo aparecen dos líneas que confirman el despliegue:

```
Deployment of web application archive [webapps\sicimed.war] has finished in [...] ms
Server startup in [...] milliseconds
```

**4. Verificar que responde**

Abrir en el navegador:

```
http://localhost:8080/sicimed/api/sistema/estado
```

Respuesta esperada:

```json
{ "servicio": "sicimed-backend", "estado": "activo", "preparado": true, "version": "1.0.0" }
```

La API queda bajo el contexto **`/sicimed`**. Si se omite ese `/sicimed` en la URL, Tomcat
responde 404.

**Swagger UI:** http://localhost:8080/sicimed/swagger-ui/index.html

Para autenticar en Swagger: ejecutar `POST /api/auth/login`, copiar el `token` de la respuesta,
pulsar **Authorize** y pegarlo.

**5. Volver a desplegar tras un cambio**

Repetir el Paso 3 (`clean` + `package`) y luego, en el panel **SERVERS**, clic derecho sobre el
servidor → **Publish Server (Full)**, que vuelve a copiar `sicimed.war` a Tomcat. Si el cambio no se
refleja, quitar el despliegue con **Remove Deployment**, añadirlo otra vez con **Add Deployment** y
publicar de nuevo.

### Paso 5 — Verificar que todo responde

```bash
curl http://localhost:8080/sicimed/api/sistema/estado
```

El campo `preparado: true` confirma que la base terminó de inicializarse. Es importante esperarlo:
el servidor acepta peticiones unos segundos antes de que terminen de sembrarse los datos de
prueba, y un login en esa ventana falla.

### Problemas frecuentes al levantar

| Síntoma | Causa | Solución |
|---|---|---|
| 404 en `/api/...` | Falta el contexto `/sicimed` en la URL | Usar `http://localhost:8080/sicimed/api/...` |
| 404 en `/sicimed/api/...` | El WAR no se desplegó | En el panel **SERVERS**, comprobar que el despliegue figura bajo el servidor; si no, repetir **Add Deployment** y **Publish Server (Full)** |
| La API no responde bajo `/sicimed` | El WAR se desplegó con otro nombre (el nombre del archivo es el context path) | Desplegar `backend\target\sicimed.war` sin renombrarlo (Paso 4) |
| `Unknown database 'sicimed'` | La base no existe | Crearla como indica el Paso 2 |
| `Access denied for user 'root'@'localhost'` | Tomcat no recibe la contraseña de MySQL | Definir `DB_PASSWORD` (Paso 2, punto 4) |
| `Port 8080 was already in use` | Tomcat ya está encendido o hay otro servicio | En **SERVERS**, comprobar el estado; si es otro programa, `netstat -ano \| findstr :8080` y `taskkill /PID <pid> /F`, o cambiar el puerto (Paso 1, punto 5) |
| `Deploy Failed` | El WAR está corrupto o se generó sin `clean` | Regenerar con `clean` + `package` y volver a añadir el despliegue |
| Login inválido justo al arrancar | Se consultó antes de terminar la carga de datos | Esperar `preparado: true` |

---

## 4. Perfil `dev` con H2 (sin MySQL)

`application-dev.yml` reemplaza MySQL por una base **H2 en memoria**; los datos de prueba se cargan
solos al arrancar (`DatosSemilla`) y se pierden al detener el proceso.
La semilla incluye **15 citas de ejemplo** repartidas en tres semanas (3 `ATENDIDO` con receta,
1 `CANCELADO` y 11 `PENDIENTE`), 4 pacientes y los catálogos, para que el sistema se pueda probar
sin agendar nada a mano. Sirve para desarrollar y
para correr las pruebas sin instalar nada más. En este modo el backend corre standalone, **sin el
contexto `/sicimed`**: la API queda en `http://localhost:8080/api`.

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

o, con el WAR ya generado (Paso 3):

```bash
cd backend
java -jar target/sicimed.war --spring.profiles.active=dev
```

| Perfil | Base de datos | URL de la API |
|---|---|---|
| por defecto (`application.yml`) | MySQL `sicimed` en `localhost:3306` | `http://localhost:8080/sicimed/api` (Tomcat) |
| `dev` (`application-dev.yml`) | H2 en memoria | `http://localhost:8080/api` |

### Si el frontend dice `Credenciales inválidas` o `401 No autenticado`

El backend está bien y `/api/sistema/estado` responde, pero la aplicación no puede iniciar sesión.
La causa es que `apiUrl` del frontend sigue apuntando a la URL con contexto de Tomcat.

Con este perfil, el backend corre standalone **sin** Tomcat, así que **no hay contexto `/sicimed`**.
Hay que cambiar en `frontend/src/environments/environment.development.ts`:

```ts
// Correcto con el perfil dev (standalone, sin Tomcat)
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

Si el backend corre en otro puerto, añadirlo también:

```
DB_URL no aplica aqui; se cambia con --server.port=8090
apiUrl: 'http://localhost:8090/api'
```

Comprobación rápida de que el login funciona:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Si devuelve un `token`, el backend está bien y el problema es el `apiUrl` del frontend.

---

## 5. Estructura de paquetes

```
backend/
├── pom.xml
├── sql/
│   └── 01_create_sicimed_mysql.sql
└── src/main/
    ├── java/com/sicimed/
    │   ├── SicimedApplication.java    Punto de entrada
    │   ├── modelo/          10 clases   Entidades JPA y enumeraciones
    │   │                              Usuario, Rol, Paciente, Medico,
    │   │                              Especialidad, Sede, Cita, EstadoCita,
    │   │                              Receta, Medicamento
    │   ├── dto/             21 clases   Contratos de entrada/salida (records)
    │   │                              LoginRequest, CitaRequest, CitaResponse,
    │   │                              PageResponse, AuthResponse, ErrorResponse, ...
    │   ├── repositorio/      8 clases   JpaRepository: métodos derivados,
    │   │                              JPQL con parámetros nombrados y una
    │   │                              Named Query (Cita.findHorasOcupadas)
    │   ├── servicio/         8 clases   Reglas de negocio (@Transactional),
    │   │                              un servicio por entidad:
    │   │                              AuthServicio, CitaServicio, MedicoServicio,
    │   │                              PacienteServicio, SedeServicio,
    │   │                              EspecialidadServicio, MedicamentoServicio,
    │   │                              UsuarioServicio
    │   ├── controlador/     10 clases   @RestController, uno por recurso:
    │   │                              AuthController, CitaController,
    │   │                              ReporteController, MedicoController,
    │   │                              PacienteController, SedeController,
    │   │                              EspecialidadController, MedicamentoController,
    │   │                              UsuarioController, SistemaController
    │   ├── seguridad/        4 clases   JWT y principal autenticado
    │   │                              JwtServicio, JwtAuthenticationFilter,
    │   │                              SicimedPrincipal, ActualizadorAutenticacion
    │   ├── configuracion/    4 clases   SecurityConfig, OpenApiConfig,
    │   │                              DatosSemilla, EstadoSemilla
    │   └── excepcion/        5 clases   Excepciones + GlobalExceptionHandler
    └── resources/
        ├── application.yml            Configuración (MySQL por defecto)
        └── application-dev.yml        Perfil H2 para desarrollo
```

### Cómo está dividido

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
JpaRepository                    métodos derivados, JPQL con parámetros nombrados, Named Query
    │
    ▼
Hibernate → MySQL
```

Reglas que se respetan en todo el código:
- Los controladores no escriben SQL.
- Los repositorios no contienen reglas de negocio.
- Los servicios no saben nada de HTTP.
- Los DTO son `record` inmutables; las entidades nunca se exponen directamente en la API.

---

## 6. API REST

- **Endpoints:** 38, repartidos en 10 controladores REST
- **Base URL:** `http://localhost:8080/sicimed/api` en Tomcat, o `http://localhost:8080/api` si se
  ejecuta standalone (sin contexto)
- **Formato:** JSON (`application/json;charset=UTF-8`)
- **Autenticación:** `Authorization: Bearer <token>` salvo en `/api/auth/**` y `/api/sistema/**`
- **Documentación:** http://localhost:8080/sicimed/swagger-ui/index.html
- **Especificación OpenAPI (JSON):** http://localhost:8080/sicimed/v3/api-docs, exportable e importable en Postman

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
| CRUD | `/api/sedes` | Sedes (listar, `GET /{id}`, crear, actualizar, baja lógica) |
| CRUD | `/api/especialidades` | Especialidades (`DELETE` da 409 si tiene médicos) |
| CRUD | `/api/medicamentos` | Medicamentos (listar, `GET /{id}`, crear, actualizar, baja lógica) |
| CRUD | `/api/usuarios` | Usuarios (solo ADMIN) |
| GET | `/api/pacientes?dni=` | Buscar paciente |

### Paginación

Los listados largos devuelven una envoltura uniforme:

```json
{ "content": [], "page": 0, "size": 20, "totalElements": 137,
  "totalPages": 7, "first": true, "last": false }
```

El tamaño de página tiene un tope de 200 registros.

### Probar la API desde Swagger

1. Abrir http://localhost:8080/sicimed/swagger-ui/index.html
2. Ejecutar `POST /api/auth/login` con `admin` / `admin123`
3. Copiar el valor de `token` de la respuesta
4. Pulsar **Authorize**, pegarlo y confirmar
5. Probar cualquier endpoint

### Roles y permisos

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

## 7. Seguridad y manejo de errores

### Seguridad (JWT)

| Medida | Implementación |
|---|---|
| Contraseñas | Hash **BCrypt** con sal. Nunca en texto plano ni devueltas por la API |
| Token | **JWT HS256** firmado, stateless, con expiración configurable (8 h por defecto) |
| Autorización | `@PreAuthorize` por rol + control de pertenencia en el servicio |
| Inyección SQL | Consultas parametrizadas: JPQL con parámetros nombrados en toda la capa de datos |
| Validación | Bean Validation declarativa con mensajes en español |
| CORS | Limitado a los orígenes del frontend en desarrollo |
| Datos sensibles | El hash de contraseña nunca aparece en ninguna respuesta de la API |
| Fuga de información | Los 500 responden "Error interno del servidor"; `server.error.include-message: never` |

**Antes de desplegar en producción:**

1. Definir `JWT_SECRET` con al menos 32 caracteres aleatorios.
2. Cambiar el CORS al dominio real del frontend.
3. Usar un usuario de MySQL con permisos mínimos, no `root`.
4. Poner `DDL_AUTO=validate` para que no se modifique el esquema en producción.

### Manejo de errores

`GlobalExceptionHandler` (`@RestControllerAdvice`) traduce las excepciones a códigos HTTP con un
cuerpo uniforme `ErrorResponse`:

| Excepción | Código |
|---|---|
| `MethodArgumentNotValidException`, `ConstraintViolationException`, cuerpo o parámetro ilegible | 400 |
| `ReglaNegocioException` | 400 |
| `AccesoDenegadoException`, `AccessDeniedException` | 403 |
| `RecursoNoEncontradoException`, ruta inexistente | 404 |
| `ConflictoException`, `DataIntegrityViolationException`, `OptimisticLockingFailureException` | 409 |
| Cualquier otra `Exception` | 500 con mensaje genérico (el detalle queda solo en el log) |

El 401 (token ausente, inválido o expirado) lo devuelve Spring Security antes de llegar al
controlador.

Significado de cada código:

| Código | Significado |
|---|---|
| 200 | Operación exitosa |
| 201 | Recurso creado |
| 400 | Datos inválidos o regla de negocio incumplida |
| 401 | Token ausente, inválido o expirado |
| 403 | Rol sin permiso, o la cita no le pertenece |
| 404 | Recurso inexistente |
| 409 | Conflicto: horario ocupado, usuario o especialidad repetidos, dato en uso, edición concurrente |
| 500 | Error no controlado: mensaje genérico al cliente, el detalle solo queda en el log |

Todos los errores comparten la misma forma:

```json
{ "status": 409, "message": "Ya existe una cita para ese medico en la fecha y hora indicadas" }
```

---

## 8. Pruebas

El script `docs/probar_api.sh` ejecuta **46 casos** contra la API en marcha.

```bash
# Con el backend levantado (desplegado en Tomcat)
bash docs/probar_api.sh http://localhost:8080/sicimed
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
> (http://localhost:8080/sicimed/swagger-ui/index.html), que documenta los mismos endpoints.

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

Con el perfil `dev` (sin contexto) la URL base es otra:

```bash
bash docs/probar_api.sh http://localhost:8080
```

### Pruebas manuales desde Swagger

| Prueba | Cómo | Resultado esperado |
|---|---|---|
| Swagger abre | Entrar en `/sicimed/swagger-ui/index.html` | Interfaz con los 38 endpoints |
| Probar un endpoint | Login en Swagger → Authorize → pegar token → `GET /api/sedes` | Devuelve la lista de sedes |
| Código de error | En Swagger, `POST /api/citas` sin campos obligatorios | Responde 400 con el mensaje del backend |
| Sin token en Swagger | En Swagger, sin pulsar Authorize, probar `GET /api/citas` | Responde 401 |

Pendiente: pruebas unitarias y de integración con **JUnit** (hoy la cobertura automática es el
script de 46 casos).

---

## 9. Lineamientos del curso en el backend

| Lineamiento del curso | Cómo se aplica en SICIMED |
|---|---|
| Capas Controller → Service → Repository, un servicio por entidad (S3, S4, S5) | 10 controladores, 8 servicios y 8 repositorios; sedes, especialidades y medicamentos tienen cada uno su `XController` y `XServicio`, y la búsqueda de pacientes vive en `PacienteServicio` |
| CRUD show/create/update/delete (S5) | `GET /{id}`, `POST`, `PUT /{id}` y `DELETE /{id}` en sedes, especialidades y medicamentos |
| Verificar existencia y validez antes de update/delete (S5) | Especialidad: nombre duplicado al crear **y** al editar (409); no se elimina si tiene médicos asociados (409) |
| Métodos derivados de Spring Data (S4) | `existsByNombreIgnoreCase`, `existsByNombreIgnoreCaseAndIdNot`, `existsByEspecialidadId` |
| Named Queries (S5) | `@NamedQuery Cita.findHorasOcupadas` declarada en la entidad `Cita` |
| Validar siempre en el servidor (S1) | DTO con Bean Validation y `@Valid` en todos los `POST`/`PUT` |
| No exponer detalles internos (S1, vulnerabilidades) | Error 500 con mensaje genérico; 409 para `DataIntegrityViolation` y bloqueo optimista; `server.error.include-message: never` |
| Código limpio | Sin código muerto ni imports sin uso; errores con un cuerpo uniforme `ErrorResponse` |

---

## 10. Decisión de arquitectura

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
| I-03 | La hora salía como `10:00:00` pero la disponibilidad como `10:00` | Jackson serializa `LocalTime` con segundos | `@JsonFormat(pattern = "HH:mm")` en entidad y DTOs |
| I-04 | El primer login tras arrancar fallaba | El servidor acepta peticiones antes de terminar la carga de datos | Indicador `preparado` en `/api/sistema/estado` |
| I-05 | Un paciente podía ver citas de otros | El listado devolvía todo | Filtrado por rol en el servicio y control de pertenencia |
| I-06 | Reprogramar manteniendo el horario daba conflicto | La validación incluía la propia cita | `contarConflictos` recibe `excludeId` |
| I-07 | El token se perdía al reiniciar | Almacén de tokens en memoria | JWT firmado, stateless |
| I-08 | Se perdían recetas antiguas en texto plano | Cambió el formato a JSON | Lectura tolerante con respaldo línea por línea |
| I-09 | La agenda de un médico mostraba citas de otros | El backend solo acotaba al rol MÉDICO y sin filtros de médico ni fecha | Filtros aplicados en SQL antes de paginar |

**El caso I-09 es un error de diseño, no un despiste.** La primera solución fue filtrar en el
cliente, lo que funcionaba con pocas citas pero dejaba páginas incompletas: el filtro se aplicaba
después de paginar. La corrección fue mover el filtro al servidor.

Los casos del frontend (I-02 e I-10) están en [frontend/README.md](../frontend/README.md#problemas-resueltos).
