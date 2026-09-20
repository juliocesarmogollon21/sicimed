# SICIMED — Sistema de Citas Médicas

Proyecto académico **UPN** · *Soluciones Web y Aplicaciones Distribuidas* · **Grupo 3**  
Caso: Policlínico San Rafael

Repositorio: https://github.com/juliocesarmogollon21/sicimed

---

## ¿Qué hace el sistema?

Gestión de citas médicas con roles, agenda del médico, diagnóstico, receta digital y configuración administrativa.

| Rol | Puede hacer |
|-----|-------------|
| **ADMIN** | Configuración (sedes, especialidades, médicos, usuarios, medicamentos), ver todas las citas, agenda |
| **RECEPCIONISTA** | Buscar paciente por DNI, crear/reprogramar/cancelar citas, ver listado |
| **MEDICO** | Ver su agenda (Por atender / Atendidos), registrar diagnóstico, receta opcional (varios medicamentos del catálogo) |
| **PACIENTE** | Registrarse, ver **solo sus** citas, agendar, reprogramar pendientes, ver receta si la cita está ATENDIDO |

### Funcionalidades principales

- Login + registro de paciente (`/registro`)
- Dashboard con accesos según rol
- Citas: tabla y **calendario semanal**; cancelar / reprogramar
- Disponibilidad de horarios por médico y fecha (sin choques)
- Agenda médico: filtros, orden, diagnóstico → estado ATENDIDO; receta en modal (opcional)
- Configuración (`/admin`): menú hub → Sedes · Especialidades · Médicos · Usuarios · Medicamentos  
  Crear/editar en **modal**, Activar/Desactivar, sin columna ID
- Backend: Jakarta Servlets + JDBC/DAO · WAR en Tomcat  
- BD: SQL Server Express (`SICIMED`)

---

## Credenciales demo (datos semilla)

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| `admin` | `admin123` | ADMIN |
| `recepcion` | `recep123` | RECEPCIONISTA |
| `medico1` | `medico123` | MEDICO |
| `paciente1` | `paciente123` | PACIENTE |

También puedes crear cuenta nueva en `/registro` (rol PACIENTE).

---

## Requisitos en tu PC

1. **Java 17+** y **Maven 3.9+**
2. **Node.js 18+** y npm
3. **SQL Server Express** con TCP puerto **1433** + SSMS
4. **Apache Tomcat 10+** (Servlet 6 / Jakarta)

Comprueba:

```powershell
java -version
mvn -v
node -v
npm -v
```

---

## Paso a paso para compilar y correr

### 0) Clonar el repo

```powershell
git clone https://github.com/juliocesarmogollon21/sicimed.git
cd sicimed
```

### 1) SQL Server

1. En **SQL Server Configuration Manager** → Protocols for SQLEXPRESS → **TCP/IP Enabled**, IPAll puerto **1433**. Reinicia el servicio `MSSQL$SQLEXPRESS`.
2. En SSMS crea un login SQL propio (usuario + contraseña **tuyos**, no los publiques en GitHub) con permisos sobre la BD `SICIMED` (p. ej. `db_owner`).
3. Ejecuta el script:

```text
backend/sql/01_create_sicimed.sql
```

(Crea BD, tablas y usuarios semilla de la **app**: admin, recepcion, etc.)

4. Configura JDBC **en local** (este archivo **no** va al repo):

```powershell
cd backend\src\main\resources
copy db.properties.example db.properties
# Edita db.properties: jdbc.user y jdbc.password con TU login SQL
```

Plantilla: `backend/src/main/resources/db.properties.example`  
Puerto típico: `localhost:1433` (solo en tu PC; no expone tu servidor a internet por sí solo).

Al desplegar el WAR, si las tablas no existen, `SchemaInitializer` también puede crearlas y sembrar datos.

### 2) Backend (WAR + Tomcat)

```powershell
cd backend
mvn -DskipTests clean package
```

Debe generar: `backend/target/sicimed.war`

1. Copia `sicimed.war` a la carpeta `webapps` de Tomcat 10.
2. Arranca Tomcat.
3. Prueba:
   - `http://localhost:8080/sicimed/inicio` → “Backend activo” (o página de inicio)
   - API base: `http://localhost:8080/sicimed/api`

**Importante:** el puerto **8080** no debe estar ocupado por Jetty u otro proceso. Si el error dice “Powered by Jetty”, cierra ese proceso y usa solo Tomcat.

### 3) Frontend (Angular)

En **otra** terminal:

```powershell
cd frontend
npm install
npm start
```

Abre: `http://localhost:4200`

La URL del API está en:

```text
frontend/src/environments/environment.ts
→ apiUrl: 'http://localhost:8080/sicimed/api'
```

Si Tomcat usa otro contexto/puerto, cámbiala ahí.

---

## Rutas de la app

| Ruta | Quién | Descripción |
|------|-------|-------------|
| `/login` | todos | Inicio de sesión |
| `/registro` | público | Alta paciente |
| `/dashboard` | autenticados | Accesos por rol |
| `/citas` | autenticados | Lista + calendario; paciente solo las suyas |
| `/nueva-cita` | ADMIN, RECEPCIONISTA, PACIENTE | Reservar cita |
| `/reprogramar-cita/:id` | dueño / recepción / admin | Reprogramar pendiente |
| `/agenda-medico` | ADMIN, MEDICO | Atender + diagnóstico + receta |
| `/admin` | ADMIN | Hub de configuración |
| `/admin/sedes` … `/admin/medicamentos` | ADMIN | Cada módulo CRUD |

---

## Estructura del código

```text
sicimed/
  README.md
  backend/                 Maven WAR (Servlets + JDBC)
    sql/01_create_sicimed.sql
    src/main/resources/db.properties
  frontend/                Angular standalone + Bootstrap 5
    src/app/componentes/
      auth/                login, registro
      citas/               lista, nueva, reprogramar
      medico/              agenda-medico
      configuracion/       hub + sedes, especialidades, medicos, usuarios, medicamentos
      dashboard/
```

---

## Problemas frecuentes

| Síntoma | Qué revisar |
|---------|-------------|
| 404 / “Powered by Jetty” | Otro servidor en 8080; usa Tomcat y el WAR en `webapps` |
| Error de conexión SQL / SSL | TCP 1433 activo; `trustServerCertificate=true`; usuario `sicimed` |
| `time` vs `datetime` | Ya corregido en el código actual; actualiza el repo |
| `npm` / Rollup falla en Windows | Borra `node_modules` y `package-lock.json`, vuelve a `npm install` en ruta corta |
| Admin desactivado y no entra | En SSMS: `UPDATE usuarios SET activo=1 WHERE username='admin'` |
| Frontend no llama al API | Revisa `environment.ts` y CORS (el backend ya tiene CorsFilter) |

---

## Integrantes (Grupo 3)

Isaac Anderson Ballena Perez · Angel Jefferson Diaz Leyva · Julio Cesar Mogollon Carranza · Steven Edson Francesscoly Suclupe Vela  

Docente: Víctor Alfredo Muguerza Capristan