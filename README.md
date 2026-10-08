# SICIMED — Sistema Web Distribuido de Citas Médicas

Proyecto académico **UPN** · *Soluciones Web y Aplicaciones Distribuidas* · **Grupo 3**
Caso de estudio: Policlínico San Rafael

Repositorio: https://github.com/juliocesarmogollon21/sicimed

---

## Índice

- [1. Qué hace el sistema](#1-qué-hace-el-sistema)
- [2. Equipo](#2-equipo)
- [3. Tecnologías](#3-tecnologías)
- [4. Estructura del repositorio](#4-estructura-del-repositorio)
- [5. Inicio rápido](#5-inicio-rápido)
- [6. Usuarios de prueba](#6-usuarios-de-prueba)
- [7. Pantallas principales](#7-pantallas-principales)
- [8. Lineamientos del curso aplicados](#8-lineamientos-del-curso-aplicados)
- [9. Mejoras pendientes](#9-mejoras-pendientes)
- [10. Documentación relacionada](#10-documentación-relacionada)

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
| Documentación API | Swagger UI con los 38 endpoints de los 10 controladores REST y prueba desde el navegador |

**Qué ofrece a cada rol**

#### Para el paciente
- Registrarse sin intervención del administrador.
- Ver solo sus propias citas: el backend filtra por rol, no el frontend.
- Reservar eligiendo sede, especialidad, médico y hora realmente libre.
- Reprogramar o cancelar mientras la cita esté pendiente.
- Ver la receta cuando la cita ya fue atendida.

#### Para recepción
- Buscar pacientes por DNI y autocompletar el formulario.
- Ver el listado y el calendario semanal de citas.
- Detectar choques de horario: el sistema responde 409 en vez de duplicar.
- Cancelar citas y liberar el horario al instante.

#### Para el médico
- Agenda del día con separación entre «Por atender» y «Atendidos».
- Registrar el diagnóstico, que pasa la cita a ATENDIDO.
- Emitir receta digital eligiendo del catálogo de medicamentos.
- No puede ver ni tocar citas de otros médicos.

#### Para el administrador
- Mantener catálogos (sedes, especialidades, médicos, usuarios, medicamentos) sin tocar la base.
- Baja lógica: desactivar en vez de borrar, para no romper el historial.
- Reportes filtrados por médico, estado y rango de fechas.

---

## 2. Equipo

| Campo | Detalle |
|---|---|
| Curso | SIST1402A — Soluciones Web y Aplicaciones Distribuidas |
| Docente | Víctor Alfredo Muguerza Capristan |
| Grupo | Grupo 3 |
| Integrantes | Isaac Anderson Ballena Perez · Angel Jefferson Diaz Leyva · Julio Cesar Mogollon Carranza · Steven Edson Francesscoly Suclupe Vela |
| Repositorio | https://github.com/juliocesarmogollon21/sicimed |

---

## 3. Tecnologías

| Capa | Tecnologías |
|---|---|
| Backend | Java 17 · Spring Boot 3.3.5 (Web, Data JPA, Security) · JJWT · springdoc-openapi |
| Base de datos | MySQL 8 (perfil `dev`: H2 en memoria) |
| Servidor | Apache Tomcat 10.1 (WAR, contexto `/sicimed`), gestionado desde VS Code |
| Frontend | Angular 21 · TypeScript 5.9 · Bootstrap 5.3 · RxJS 7.8 |
| Pruebas | `docs/probar_api.sh` (46 casos, Bash) |

Versiones y detalle: [backend/README.md](backend/README.md#1-tecnologías) y
[frontend/README.md](frontend/README.md#tecnologías).

### Por lenguaje

| Lenguaje | Archivos | Líneas | Dónde se usa |
|---|---|---|---|
| **Java** | 71 | 3.072 | Backend completo (Spring Boot) |
| **TypeScript** | 40 | 2.602 | Frontend Angular |
| **HTML** | 18 | 1.734 | Plantillas de los componentes |
| **CSS** | 16 | 822 | Estilos de componentes y global |
| **SQL** | 1 | 124 | Esquema de la base de datos |
| **Bash** | 1 | 225 | Script de pruebas de la API |
| **YAML / XML** | 3 | 184 | Configuración (`application*.yml`, `pom.xml`) |
| **Total** | **150** | **8.763** | Sin contar los 3 README en Markdown |

Conteo sobre los archivos versionados (`git ls-files`, sin `node_modules` ni `target`); las líneas
son físicas e incluyen líneas en blanco y comentarios.

---

## 4. Estructura del repositorio

```
sicimed/
├── README.md                 Este archivo (visión general)
├── backend/                  API REST Spring Boot → backend/README.md
│   ├── pom.xml
│   ├── sql/                  Script de creación de la base MySQL
│   └── src/main/             Código Java (com.sicimed) y application*.yml
├── frontend/                 SPA Angular 21 → frontend/README.md
│   ├── public/img/           Imágenes locales
│   └── src/                  Componentes, servicios, guards, modelos
└── docs/
    └── probar_api.sh         46 pruebas de la API
```

---

## 5. Inicio rápido

```bash
git clone https://github.com/juliocesarmogollon21/sicimed.git
cd sicimed
```

Hay dos formas de levantar el sistema:

- **Forma principal (Opción A)** — MySQL y Tomcat, la que se usa en el laboratorio.
- **Alternativa (Opción B)** — sin base de datos ni Tomcat, con H2 en memoria. Si no quieres
  crear la base de datos en MySQL, ve directo a la [Opción B](#opción-b--sin-base-de-datos-ni-tomcat).

**La diferencia entre ambas es la URL de la API**, porque al desplegar el WAR en Tomcat la
aplicación queda bajo el contexto `/sicimed`:

| | Opción A — MySQL + Tomcat | Opción B — H2 sin Tomcat |
|---|---|---|
| Base de datos | MySQL / MariaDB, base `sicimed` | H2 en memoria |
| Backend | WAR desplegado en Tomcat | `java -jar` |
| URL de la API | `http://localhost:8080/sicimed/api` | `http://localhost:8080/api` |
| `apiUrl` del frontend | `http://localhost:8080/sicimed/api` | `http://localhost:8080/api` |
| Los datos se borran al reiniciar | No | Sí, se recarga la semilla |

> Si cambias de opción, cambia también `apiUrl` en
> `frontend/src/environments/environment.development.ts`. Si no coincide, el login falla con
> `Credenciales inválidas` aunque el backend esté funcionando.

---

## Opción A — MySQL y Tomcat (principal)

Todos los comandos se ejecutan desde la **terminal integrada de Visual Studio Code**
(Ctrl+` o *Terminal → Nueva terminal*).

### A1. Crear la base de datos en MySQL

La base debe existir antes de arrancar el backend. Si no, el despliegue falla con
`Unknown database 'sicimed'`.

> **Si ya la creaste antes, sáltate este paso.** Si al ejecutar el comando aparece
> `ERROR 1007 (HY000): Can't create database 'sicimed'; database exists`, la base ya está creada y
> no hay que hacer nada más.

En la terminal de VS Code:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "CREATE DATABASE sicimed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

> El `&` al principio es obligatorio: es el operador *call* de PowerShell, que permite ejecutar una
> ruta con espacios. Sin él aparece
> `Token '-u' inesperado en la expresión o la instrucción`.

Si tu usuario `root` **no tiene contraseña**, quita el `-p`:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -e "CREATE DATABASE sicimed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

Verificar que existe:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "SHOW DATABASES LIKE 'sicimed';"
```

Debe aparecer `sicimed`.

> **El backend crea solo las tablas y los datos de prueba** (2 sedes, 3 especialidades, 3 médicos,
> 4 pacientes, 6 medicamentos y 15 citas repartidas en tres semanas). La base es lo único que hay
> que crear a mano, y solo una vez por máquina.

### A2. Generar el WAR

Desde el **panel de Maven de VS Code**: clic derecho sobre la carpeta `backend` →
**Run Maven Commands...**, y ejecutar en este orden:

1. **clean** — borra `target/`
2. **package** — compila y genera `target/sicimed.war`

> Ejecutar `clean` antes de `package` es importante: sin `clean`, Maven puede dejar clases de una
> compilación anterior y el WAR se genera con código desactualizado.

También se puede desde la terminal:

```bash
cd backend
mvn clean package
```

### A3. Encender Tomcat desde VS Code

En el panel **Servers** (lado izquierdo) aparece el servidor ya registrado:

```
Community Server Connector
└── apache-tomcat-10.1.10 (Stopped)
```

Clic derecho sobre el servidor → **Start** (o el botón ▶ que aparece al seleccionarlo). El estado
pasa a **(Started)** y el puerto **8080** queda ocupado por Tomcat.

### A4. Subir el WAR con Add Deployment

Clic derecho sobre el servidor Tomcat → **Add Deployment...**

Se abre un cuadro de diálogo con dos campos:

| Campo | Valor |
|---|---|
| **File** | `Select WAR File...` → `backend\target\sicimed.war` |
| **Context path** | `/sicimed` |

Pulsar **Finish**.

El *context path* define la carpeta donde se publica la aplicación dentro de Tomcat, y por eso la
URL de la API empieza por `/sicimed`.

### A5. Esperar a que publique

En la consola de abajo aparecen estas dos líneas, que confirman el despliegue:

```
Deployment of web application archive [webapps\sicimed.war] has finished in [...] ms
Server startup in [...] milliseconds
```

### A6. Verificar que responde

```bash
curl http://localhost:8080/sicimed/api/sistema/estado
```

Respuesta esperada:

```json
{ "servicio": "sicimed-backend", "estado": "activo", "preparado": true, "version": "1.0.0" }
```

Swagger UI: http://localhost:8080/sicimed/swagger-ui/index.html

El campo `preparado: true` confirma que la base terminó de inicializarse. El servidor acepta
peticiones unos segundos antes de que terminen de sembrarse los datos, y un login en esa ventana
falla.

### A7. Levantar el frontend

El archivo `frontend/src/environments/environment.development.ts` ya viene con
`apiUrl: 'http://localhost:8080/sicimed/api'`, que es la URL de esta opción. No hay que cambiar nada.

En la terminal de VS Code:

```bash
cd frontend
npm install
npx ng serve
```

Abrir http://localhost:4200 e iniciar sesión con `admin` / `admin123`.

> Si más adelante pruebas la Opción B (sin Tomcat), tendrás que quitarle el `/sicimed` de `apiUrl`.

### A8. Volver a desplegar tras un cambio

Repetir **A2** (`clean` + `package`) y luego **A4** (*Add Deployment...*) apuntando al WAR nuevo.

---

## Opción B — sin base de datos ni Tomcat

Si no quieres crear la base en MySQL ni usar Tomcat, el backend puede correr solo, con una base
H2 **en memoria** que crea y destruye en cada arranque.

### B1. Generar el WAR

Igual que en A2: `clean` y `package` desde el panel de Maven, o `mvn clean package` en la terminal.

### B2. Arrancar el backend

```bash
cd backend
java -jar target/sicimed.war --spring.profiles.active=dev
```

El perfil `dev` reemplaza MySQL por H2 en memoria. Los datos de prueba se cargan al arrancar y se
**pierden al apagar el servidor**.

Comprobar que responde:

```bash
curl http://localhost:8080/api/sistema/estado
```

Swagger UI: http://localhost:8080/swagger-ui/index.html

### B3. El frontend ya viene apuntando bien

`environment.development.ts` viene con `http://localhost:8080/api`, que es la URL de esta opción.
No hay que cambiar nada.

### B4. Levantar el frontend

```bash
cd frontend
npm install
npx ng serve
```

Abrir http://localhost:4200 e iniciar sesión con `admin` / `admin123`.

---

## Problemas frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| Login dice `Credenciales inválidas` pero `/api/sistema/estado` responde | `apiUrl` no coincide con la opción elegida | Opción A: `http://localhost:8080/sicimed/api`. Opción B: `http://localhost:8080/api` |
| `401 No autenticado` al abrir la app | El frontend llama a `/sicimed/api` y el backend corre sin contexto | Quitar el `/sicimed` de `apiUrl` |
| 404 en `http://localhost:8080/api/...` | El backend está en Tomcat, con contexto | Usar `http://localhost:8080/sicimed/api/...` |
| `Unknown database 'sicimed'` en el log de Tomcat | La base no existe | Crearla con el comando de A1, o usar la Opción B |
| `Port 8080 was already in use` | Tomcat ya está encendido o hay otro servicio | En **Servers**, comprobar el estado; detener el proceso antiguo |
| El panel sale con todos los contadores en 0 | No hay citas registradas | La semilla carga 15 citas; si usas la Opción B, esperar `preparado: true` |
| `preparado: false` | La base todavía se inicializa | Esperar unos segundos y volver a consultar |
| `Deploy Failed` | El WAR está corrupto o se generó sin `clean` | Regenerar con `clean` + `package` y volver a añadir el despliegue |
| Error de CORS en el navegador | El frontend corre en un puerto distinto de 4200 | Agregar ese origen en `SecurityConfig.setAllowedOrigins` |
| `Failed to fetch` | El backend no está corriendo | Verificar `/api/sistema/estado` y el valor de `apiUrl` |

---

## 6. Usuarios de prueba

| Usuario | Contraseña | Rol | Qué se puede probar |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Configuración completa, reportes, todas las citas |
| `recepcion` | `recep123` | RECEPCIONISTA | Buscar paciente por DNI, agendar, reprogramar, cancelar |
| `medico1` | `medico123` | MEDICO | Agenda del día, diagnóstico, receta |
| `medico2` | `medico123` | MEDICO | Pediatrics, sede Norte |
| `medico3` | `medico123` | MEDICO | Ginecología, sede Centro |
| `paciente1` | `paciente123` | PACIENTE | María Pérez, DNI 71234567, 4 citas de ejemplo |
| `paciente2` | `paciente123` | PACIENTE | Luis Álvarez, DNI 70112233 |
| `paciente3` | `paciente123` | PACIENTE | Carmen Rojas, DNI 74558899 |
| `paciente4` | `paciente123` | PACIENTE | Jorge Díaz, DNI 70990011 |

También se puede crear una cuenta nueva en `/registro`, que siempre queda con rol `PACIENTE`.

### Datos de ejemplo

Al arrancar por primera vez, el backend carga los catálogos (2 sedes, 3 especialidades, 3 médicos,
6 medicamentos) y **15 citas repartidas en tres semanas**, para que el sistema se pueda probar sin
tener que agendar nada a mano:

| Estado | Cuántas | Para qué sirve |
|---|---|---|
| `ATENDIDO` | 3 | Ver diagnóstico y receta (tienen receta emitida) |
| `CANCELADO` | 1 | Ver el estado y comprobar que el horario se libera |
| `PENDIENTE` | 11 | Probar agenda, reprogramación y cancelación |

Las citas atendidas tienen receta con medicamentos y dosis, para comprobar el flujo completo de
atención médica: entrar como `medico1`, ver la agenda, y abrir la receta de un paciente ya
atendido.

**Con el perfil `dev` la base H2 está en memoria:** cada vez que se reinicia el backend se borra
todo y se vuelve a cargar la semilla. Con MySQL, la semilla solo se carga si la tabla de usuarios
está vacía.

---

## 7. Pantallas principales

Revisadas en navegador a 1366×768, octubre de 2026.

| Pantalla | Qué se ve |
|---|---|
| Login | Imagen institucional a la izquierda y tarjeta con usuario, contraseña y botón **Ingresar**; los campos vacíos se marcan en rojo con su mensaje debajo |
| Registro | Misma composición: nombre, usuario, contraseña, email, DNI y teléfono opcional |
| Panel | Menú lateral verde según el rol, bienvenida con el rol, cuatro contadores (total, pendientes, atendidas, canceladas) y accesos rápidos |
| Nueva cita | Formulario con DNI + Buscar (recepción), sede, especialidad, médico, fecha, hora disponible y motivo; al guardar vacío marca cada campo y resume lo que falta |
| Citas | Búsqueda por DNI (recepción/admin), vista **Calendario** semanal con citas coloreadas por estado y vista **Tabla** con badges de estado y botones Reprogramar / Cancelar / Ver receta |
| Agenda médico | Filtros de médico, fecha y paciente; pestañas «Por atender» y «Atendidos»; cada cita en una tarjeta con motivo y diagnóstico; modal de receta con filas de medicamentos |
| Configuración | Menú de submódulos (sedes, especialidades, médicos, usuarios, medicamentos) y tablas con Editar / Desactivar |
| Ruta inexistente | Página 404 con botón para volver al inicio |

---

## 8. Lineamientos del curso aplicados

Resumen; el detalle está en [backend/README.md](backend/README.md#9-lineamientos-del-curso-en-el-backend)
y [frontend/README.md](frontend/README.md#lineamientos-del-curso-en-el-frontend).

- **Backend:** capas Controller → Service → Repository con un servicio por entidad (10 controladores,
  8 servicios, 8 repositorios); CRUD completo con verificación previa a update/delete; métodos
  derivados y Named Query; `@Valid` en todos los `POST`/`PUT`; errores sin detalles internos.
- **Frontend:** `@if`/`@for` con `track`, `[class.x]` sin `ngClass`, comunicación padre ↔ hijo con
  `input()`/`output()`, solo Bootstrap con CSS mínimo, `guards/` y `componentes/compartido/`.
- **Verificación:** `mvn clean package` y `ng build` compilan, `docs/probar_api.sh` pasa **46/46** y
  las pantallas se revisaron en navegador.

---

## 9. Mejoras pendientes

| Pendiente | Estado |
|---|---|
| Panel de indicadores avanzado (RF-18) | Parcial: hay conteos simples, no es BI |
| Bloques horarios configurables por médico | No implementado: turnos fijos de 08:00 a 17:00 |
| Envío de recordatorios por correo | No implementado |
| Exportación de reportes a PDF y Excel | No implementado |
| Pruebas automatizadas en CI | Pendiente: hay script de humo (46/46 OK), falta JUnit |
| Migrar a OAuth 2 con proveedor externo | Pendiente: hoy es JWT propio |

---

## 10. Documentación relacionada

| Documento | Contenido |
|---|---|
| [backend/README.md](backend/README.md) | Instalación de JDK, Tomcat y MySQL, despliegue del WAR, API REST, seguridad, errores y pruebas |
| [frontend/README.md](frontend/README.md) | Puesta en marcha de Angular, `apiUrl`, estructura, convenciones y guía de pruebas manuales |
| http://localhost:8080/sicimed/swagger-ui/index.html | Especificación OpenAPI interactiva (con el backend desplegado) |
| `docs/probar_api.sh` | Script de 46 pruebas de la API |

El informe técnico del proyecto se entrega aparte, en formato Word; no forma parte del
repositorio.
