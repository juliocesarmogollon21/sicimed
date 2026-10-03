# SICIMED — Frontend Angular

Cliente web del Sistema de Citas Médicas. Se ejecuta en el puerto 4200 y consume la API REST del
backend (por defecto `http://localhost:8080/sicimed/api`, el contexto `/sicimed` de Tomcat).

Visión general del proyecto: [README principal](../README.md). Instalación y despliegue del backend:
[backend/README.md](../backend/README.md).

---

## Índice

- [Tecnologías](#tecnologías)
- [Requisitos](#requisitos)
- [Puesta en marcha](#puesta-en-marcha)
- [Compilar para producción](#compilar-para-producción)
- [Configurar la URL del backend](#configurar-la-url-del-backend)
- [Credenciales de demostración](#credenciales-de-demostración)
- [Estructura](#estructura)
- [Convenciones](#convenciones)
- [Pruebas](#pruebas)
- [Problemas frecuentes](#problemas-frecuentes)
- [Problemas resueltos](#problemas-resueltos)

---

## Tecnologías

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

## Requisitos

| Herramienta | Versión |
|---|---|
| Node.js | 20 o superior |
| npm | 10 o superior |
| Angular CLI | 21 (se usa con `npx ng`, no hace falta instalarlo global) |

```bash
node -v
npm -v
```

---

## Puesta en marcha

Todos los comandos se ejecutan desde la **terminal integrada de Visual Studio Code** (Ctrl+`).

```bash
npm install
npx ng serve
```

Abrir http://localhost:4200

El backend debe estar corriendo. Según la opción que se haya elegido:

| Opción del backend | URL para comprobar que responde |
|---|---|
| A — Tomcat + MySQL | http://localhost:8080/sicimed/api/sistema/estado |
| B — standalone con perfil `dev` | http://localhost:8080/api/sistema/estado |

Debe responder `preparado: true`. Si el backend no está corriendo, el login fallará con un error de
red; si está corriendo pero en la otra URL, el login fallará con `Credenciales inválidas`.

> Con npm 11.19 o superior puede aparecer un aviso `install-scripts` sobre paquetes como `esbuild`
> o `@parcel/watcher`. **Son avisos, no errores**: el build funciona igual. Si llegaras a ver un
> fallo, usa `npm install --foreground-scripts`.

Las imágenes de las pantallas de login, registro y panel están guardadas en el propio proyecto
(`public/img/`), así que la aplicación se ve igual sin conexión a internet.

---

## Compilar para producción

```bash
npx ng build
```

La salida queda en `dist/frontend/`. Se sirve como archivos estáticos desde cualquier servidor
web o CDN.

> Nota: el build emite un aviso de tamaño del bundle (~805 kB sobre un presupuesto de 500 kB). No
> es un error y no bloquea la compilación; el peso viene principalmente de Bootstrap (CSS + JS).

---

## Configurar la URL del API

Hay dos archivos y cada uno se usa según cómo ejecutes la aplicación:

| Archivo | Se usa con | Qué poner |
|---|---|---|
| `src/environments/environment.development.ts` | `npx ng serve` (desarrollo) | Ver abajo |
| `src/environments/environment.ts` | `npx ng build` (producción) | `http://localhost:8080/sicimed/api` |

### El valor por defecto

El archivo de desarrollo viene con `http://localhost:8080/sicimed/api`, que corresponde a la
**Opción A: el WAR desplegado en Tomcat** con context path `/sicimed`. Es la opción principal y la
que se usa en el laboratorio.

Si en su lugar levantaste el backend **sin Tomcat**, hay que quitarle el `/sicimed`:

| Cómo levantaste el backend | `apiUrl` |
|---|---|
| WAR desplegado en Tomcat con context path `/sicimed` | `http://localhost:8080/sicimed/api` |
| `java -jar target/sicimed.war --spring.profiles.active=dev` | `http://localhost:8080/api` |

Después de cambiarlo, hay que **reiniciar `ng serve`** para que tome el valor nuevo.

### Por qué importa: el síntoma cuando no coincide

Si `apiUrl` no apunta a donde está el backend, la aplicación **no muestra ningún error de red**.
Simplemente dice `Credenciales inválidas` o devuelve `401 No autenticado`, aunque el backend esté
funcionando perfectamente.

Comprobación para saber cuál de los dos es:

```bash
# Si responde, el backend está en /sicimed/api (Opción A, Tomcat)
curl http://localhost:8080/sicimed/api/sistema/estado

# Si responde, el backend está en /api (Opción B, standalone)
curl http://localhost:8080/api/sistema/estado
```

El que responda `{"estado":"activo","preparado":true,...}` es donde está tu backend, y ese es el
valor que debe llevar `apiUrl`.

### CORS

El backend permite únicamente el origen `http://localhost:4200`. Si el frontend se sirve en otro
origen (por ejemplo `localhost:4300`), hay que agregar ese origen en
`SecurityConfig.setAllowedOrigins` del backend, o el navegador bloqueará las peticiones.

---

## Credenciales de demostración

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `recepcion` | `recep123` | RECEPCIONISTA |
| `medico1` | `medico123` | MEDICO (Medicina General, Sede Centro) |
| `medico2` | `medico123` | MEDICO (Pediatría, Sede Norte) |
| `medico3` | `medico123` | MEDICO (Ginecología, Sede Centro) |
| `paciente1` | `paciente123` | PACIENTE (María Pérez) |
| `paciente2` | `paciente123` | PACIENTE (Luis Álvarez) |
| `paciente3` | `paciente123` | PACIENTE (Carmen Rojas) |
| `paciente4` | `paciente123` | PACIENTE (Jorge Díaz) |

También se puede crear una cuenta nueva en `/registro`, que siempre queda con rol PACIENTE.

### Qué hay cargado al arrancar

El backend carga catálogos y **15 citas de ejemplo** en tres semanas, para que las pantallas se
vean con contenido desde el primer momento:

- **3 citas `ATENDIDO`** con diagnóstico y receta emitida (una de ellas con dos medicamentos)
- **1 cita `CANCELADO`** para ver el estado
- **11 citas `PENDIENTE`** repartidas entre los tres médicos

Para ver el flujo médico completo: entra con `medico1` / `medico123`, abre **Agenda**, y en las
pestañas **Atendidos** puedes abrir la receta de una consulta ya cerrada.

Para probar como paciente: entra con `paciente1` / `paciente123` y verás sus 4 citas, incluida
una atendida con su receta.

> Con el perfil `dev` la base está en memoria: al reiniciar el backend se borra y se recarga la
> semilla.

---

## Estructura

```
src/
├── main.ts
├── index.html
├── styles.css                     Identidad institucional (Bootstrap va en angular.json)
├── environments/
│   ├── environment.ts             apiUrl del backend
│   └── environment.development.ts
└── app/
    ├── app.ts / app.html / app.css        Shell + menú por rol
    ├── app.routes.ts                      Rutas protegidas por guardas
    ├── app.config.ts                      provideRouter + HttpClient + interceptores
    ├── guards/
    │   ├── auth.guard.ts                  Exige sesión
    │   └── role.guard.ts                  Exige rol (data.roles)
    ├── interceptors/
    │   └── auth.interceptor.ts            Adjunta el token; cierra sesión ante 401
    ├── modelos/                  7       Interfaces tipadas
    │                                      Cita, Medico, Sede, Especialidad,
    │                                      Medicamento, LoginResponse, Pagina
    ├── servicios/                8       Un servicio por recurso (*.service.ts), providedIn: 'root'
    │                                      AuthService, CitaService, MedicoService,
    │                                      SedeService, EspecialidadService,
    │                                      MedicamentoService, UsuarioService,
    │                                      PacienteService
    └── componentes/
        ├── compartido/          2        alerta (input/output),
        │                                 estado-badge (input.required)
        ├── auth/                2        login, registro
        ├── dashboard/           1        Panel principal
        ├── citas/               3        lista, nueva, reprogramar
        ├── medico/              1        agenda-medico
        ├── configuracion/       6        hub + sedes, especialidades,
        │                                 medicos, usuarios, medicamentos
        │                                 (mas shared/config-shared.css)
        └── no-encontrado/       1        Página 404 (ruta '**')
```

### Cómo está dividido

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
- Las rutas se protegen con `authGuard` y `roleGuard` (carpeta `guards/`); las rutas inexistentes
  muestran `NoEncontradoComponent`.
- Plantillas con `@if` / `@for (...; track x.id)`, `[class.x]` en vez de `ngClass`, y comunicación
  padre ↔ hijo con `input()` / `output()` (componentes de `componentes/compartido/`).
- Los modelos de `src/app/modelos/` definen la forma de los datos en ambos sentidos.

---

## Convenciones

- **Un componente, un archivo trio** (`nombre.ts`, `nombre.html`, `nombre.css`) dentro de su carpeta.
- **Los componentes no escriben URLs**: llaman a un servicio de `src/app/servicios/`.
- **El token se añade en un solo lugar**: el interceptor. Ningún componente lo gestiona.
- **Angular 21**: los componentes son standalone por defecto, así que no se escribe
  `standalone: true`.
- **Signals**: la comunicación padre → hijo usa `input()` / `input.required()` y la hijo → padre
  `output()`, sin decoradores `@Input` / `@Output` (ver `componentes/compartido/`).
- **Control de flujo moderno**: `@if`, `@for (...; track x.id)` y `@else`. No se usa `*ngIf`, `*ngFor`,
  `ngClass` ni `ngStyle`; las clases condicionales van con `[class.nombre]="condicion"`.
- **Solo Bootstrap**: Bootstrap 5.3 se registra en `angular.json` → `styles`. Los formularios usan
  `is-invalid` + `invalid-feedback`, las alertas `alert-*` y los estados `badge text-bg-*`. No hay
  estilos en línea (`style="..."`) ni CSS que sobrescriba componentes de Bootstrap; el CSS propio
  se limita a la identidad institucional y a layouts que Bootstrap no trae (sidebar, calendario).
- **Formato**: `.editorconfig` y `.prettierrc` iguales a los del proyecto del curso (tienda-virtual).
- **Nombres en español** para el dominio (cita, sede, medico) y en camelCase para los servicios.

### Lineamientos del curso en el frontend

| Lineamiento del curso | Cómo se aplica en SICIMED |
|---|---|
| Angular moderno (S2, T1) | `@if`/`@for` con `track x.id`; `[class.x]` en lugar de `[ngClass]`; componentes standalone sin `CommonModule` |
| Comunicación padre ↔ hijo con signals (S2, S4) | Componentes `app-alerta` (`input()` + `output()`) y `app-estado-badge` (`input.required()`) |
| Solo Bootstrap, CSS mínimo (S1, T1) | Bootstrap en `angular.json` → `styles`; `is-invalid` + `invalid-feedback`, `alert-*` y `badge text-bg-*` nativos; sin `style="..."`; `styles.css` no sobrescribe componentes de Bootstrap y solo define la identidad institucional, el sidebar y el calendario semanal |
| Estructura de carpetas coherente (S3, tienda-virtual) | `guards/` separado de `servicios/`; `componentes/compartido/`; `no-encontrado/` para la ruta `'**'` (S3) |
| Configuración del proyecto (tienda-virtual) | `.editorconfig` y `.prettierrc` del proyecto del curso; `environment.ts` y `environment.development.ts` apuntan a `http://localhost:8080/sicimed/api` |

---

## Pruebas

- La API se prueba con `docs/probar_api.sh` (46 casos, ver [backend/README.md](../backend/README.md#8-pruebas)).
- El frontend no tiene pruebas unitarias todavía (no hay archivos `*.spec.ts`); queda como mejora
  pendiente, igual que las pruebas JUnit del backend.

### Cómo probar cada función

Guía para probar el sistema desde la interfaz, función por función, con los cuatro usuarios de demostración.

#### Antes de empezar

Con el WAR desplegado y el frontend levantado, tener dos pestañas abiertas:

- La aplicación en http://localhost:4200
- Swagger en http://localhost:8080/sicimed/swagger-ui/index.html (pruebas de la API: ver
  [backend/README.md](../backend/README.md#8-pruebas))

Comprobar primero que el backend responde en
http://localhost:8080/sicimed/api/sistema/estado

#### Los cuatro usuarios

| Usuario | Contraseña | Qué se puede probar con ese rol |
|---|---|---|
| `admin` | `admin123` | Configuración completa, reportes, todas las citas |
| `recepcion` | `recep123` | Buscar paciente por DNI, agendar, reprogramar, cancelar |
| `medico1` | `medico123` | Agenda del día, diagnóstico, receta |
| `paciente1` | `paciente123` | Reservar y ver sus citas, ver su receta |

#### Autenticación

| Prueba | Cómo | Resultado esperado |
|---|---|---|
| Login correcto | Entrar con `admin` / `admin123` | Entra al panel con el menú de administrador |
| Login incorrecto | Poner un usuario o contraseña equivocado | Mensaje de credenciales inválidas, no entra |
| Sesión persistente | Entrar y recargar la página (F5) | Sigue dentro, no vuelve al login |
| Cierre de sesión | Pulsar **Cerrar sesión** (parte inferior del menú lateral) | Vuelve al login y el menú desaparece |
| Rutas protegidas | Con la sesión cerrada, entrar directo a `localhost:4200/citas` | Redirige al login |
| Registro de paciente | `/registro` con datos nuevos | Cuenta creada y entra como paciente |
| Usuario repetido | Registrarse con un usuario que ya existe | Mensaje de que ya está registrado |
| Contraseña corta | Registrar con menos de 6 caracteres | Mensaje de contraseña demasiado corta |
| Email inválido | Registrar con un email mal escrito | Validación del formato del email |

#### Reserva de citas (recepción)

| Prueba | Cómo | Resultado esperado |
|---|---|---|
| Reserva completa | Nueva cita → sede → especialidad → médico → fecha → hora → Guardar | Mensaje de cita registrada y vuelve al listado |
| Horas disponibles | Elegir médico y fecha | El selector de hora solo ofrece horas libres |
| Choque de horario | Recepción agenda el mismo médico, fecha y hora ya ocupados | Mensaje de conflicto; la cita no se duplica |
| Fecha pasada | Poner una fecha anterior a hoy | Validación: no deja agendar en el pasado |
| Filtros en cascada | Cambiar la sede | Se limpian médico y hora y se recargan los médicos de esa sede |
| Búsqueda por DNI | Escribir un DNI inexistente y pulsar Buscar | Mensaje de paciente no encontrado |
| Paciente encontrado | Escribir `71234567` y pulsar Buscar | Muestra el nombre del paciente |
| DNI con letras | Escribir letras en el campo DNI | Validación: solo se permiten números |
| Motivo corto | Escribir menos de 10 caracteres en el motivo | Validación: el motivo es muy corto |

#### Gestión de citas

| Prueba | Cómo | Resultado esperado |
|---|---|---|
| Listado | Menú **Citas** | Tabla y calendario con las citas del usuario |
| Vista semanal | Botones Anterior / Hoy / Siguiente | Cambia de semana sin recargar la página |
| Ver receta | Cita atendida → Ver receta | Modal con medicamentos e indicaciones |
| Reprogramar | Cita pendiente → Reprogramar → nueva hora | Mensaje de cita reprogramada |
| Cancelar | Cita pendiente → Cancelar → confirmar | Pasa a CANCELADO y la hora vuelve a estar libre |
| No cancelar atendida | Intentar cancelar una cita atendida | El sistema lo bloquea |
| No reprogramar atendida | Intentar reprogramar una cita atendida | El sistema lo bloquea |
| Aislamiento por rol | Entrar como `paciente1` y ver Citas | Solo aparecen sus propias citas |

#### Atención médica (médico)

| Prueba | Cómo | Resultado esperado |
|---|---|---|
| Agenda del día | Menú **Agenda** | Citas del día separadas en Por atender y Atendidos |
| Diagnóstico | Escribir el diagnóstico → Guardar | Mensaje de cita atendida; pasa al panel Atendidos |
| Receta | En una cita atendida → Receta → elegir medicamentos → Guardar | Receta guardada |
| Ver receta como paciente | Entrar como `paciente1` → cita atendida → Ver receta | Ve los medicamentos indicados |
| Diagnóstico sin texto | Pulsar Guardar con el diagnóstico vacío | Validación: el diagnóstico es obligatorio |

#### Configuración (administrador)

| Prueba | Cómo | Resultado esperado |
|---|---|---|
| Menú por rol | Entrar con `paciente1` | No aparece Configuración en el menú |
| CRUD de sedes | Configuración → Sedes → Nueva → Guardar | La sede aparece en la lista |
| Baja lógica | Desactivar una sede | Deja de salir en Nueva cita, pero sigue en la lista |
| Reactivar | Volver a activarla | Vuelve a estar disponible |
| Especialidades | Configuración → Especialidades | Se pueden crear y editar; no se puede eliminar una que tenga médicos (409) |
| Médicos | Configuración → Médicos | Registrar un médico con usuario, especialidad y sede |
| Usuarios | Configuración → Usuarios | Crear un usuario con su rol |
| Validación de campos | Crear una sede sin nombre | Validación: el nombre es obligatorio |
| Especialidad duplicada | Crear una especialidad con nombre repetido | Mensaje de conflicto |

#### Seguridad y control de acceso

| Prueba | Cómo | Resultado esperado |
|---|---|---|
| Paciente no accede a admin | Entrar como `paciente1`, ir por URL a `/admin` | Sin acceso o mensaje de acceso denegado |
| Sesión caducada | Entrar y luego invalidar el token en el navegador | Al siguiente clic, vuelve al login |

---

## Problemas frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| `Login inválido` y el backend está arriba | El token no viaja en la cabecera | Revisar `src/app/interceptors/auth.interceptor.ts` |
| Error de CORS en la consola del navegador | Origen no permitido | Agregar el origen en `SecurityConfig` del backend |
| `Failed to fetch` | El backend no está corriendo o el puerto cambió | Verificar `/api/sistema/estado` y `apiUrl` |
| El frontend no llama al API | `apiUrl` sin el contexto `/sicimed` | Poner `http://localhost:8080/sicimed/api` en los dos environments |
| El build falla tras `npm install` | `node_modules` incompleto | `rm -rf node_modules package-lock.json && npm install` |
| Puerto 4200 ocupado | Otro proceso | `npx ng serve --port 4300` y agregar ese origen al CORS de `SecurityConfig` |
| Avisos `install-scripts` al instalar | npm 11.19+ bloquea scripts de instalación por seguridad | Son avisos, no errores: el build funciona igual. Si aun así falla, `npm install --foreground-scripts` |

---

## Problemas resueltos

| # | Problema | Causa | Solución |
|---|---|---|---|
| I-02 | Tras el login, el resto de peticiones daba 401 | Los componentes no enviaban el token | `authInterceptor` añade el encabezado en un solo lugar |
| I-10 | La cabecera de autorización llegaba vacía | Literal roto en el interceptor, que TypeScript compilaba sin avisar | Corregido y verificado sobre el bundle compilado |

**El caso I-10 es el más importante para el trabajo en equipo.** El interceptor tenía
`Authorization: *** ${token}` en vez de ``Authorization: `Bearer ${token}` ``. TypeScript lo leía
como una expresión de división, así que `ng build` terminaba **sin errores** y el bundle se
generaba con normalidad. Pero la cabecera que llegaba al servidor era inútil y toda petición
autenticada recibía 401. El error solo apareció al inspeccionar el JavaScript compilado. La lección:
en un trabajo en grupo, compilar no es verificar.
