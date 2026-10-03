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

La primera vez, en este orden:

1. **Base de datos MySQL** — instalar MySQL 8, arrancar el servicio `MySQL80` y crear la base
   `sicimed`: [backend/README.md → Paso 2](backend/README.md#paso-2--instalar-mysql-8-y-crear-la-base-de-datos).
2. **Backend** — JDK 17, Tomcat 10.1 registrado en VS Code, generar `sicimed.war` y desplegarlo:
   [backend/README.md → Puesta en marcha](backend/README.md#3-puesta-en-marcha). Comprobar que
   http://localhost:8080/sicimed/api/sistema/estado responde `preparado: true`.
3. **Frontend** — `npm install` y `npx ng serve` en `frontend/`:
   [frontend/README.md → Puesta en marcha](frontend/README.md#puesta-en-marcha). Abrir http://localhost:4200.

Sin MySQL se puede usar el perfil `dev` con H2 en memoria:
[backend/README.md → Perfil dev](backend/README.md#4-perfil-dev-con-h2-sin-mysql).

---

## 6. Usuarios de prueba

| Usuario | Contraseña | Qué se puede probar con ese rol |
|---|---|---|
| `admin` | `admin123` | Configuración completa, reportes, todas las citas |
| `recepcion` | `recep123` | Buscar paciente por DNI, agendar, reprogramar, cancelar |
| `medico1` | `medico123` | Agenda del día, diagnóstico, receta |
| `paciente1` | `paciente123` | Reservar y ver sus citas, ver su receta |

También se puede crear una cuenta nueva en `/registro`, que siempre queda con rol `PACIENTE`.

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
