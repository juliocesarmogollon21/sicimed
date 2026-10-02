# Capítulo 4 — Diseño de arquitectura y evaluación de alternativas

**Proyecto:** SICIMED – Sistema Web Distribuido para la Gestión de Citas Médicas
**Curso:** Soluciones Web y Aplicaciones Distribuidas · UPN · 2026

---

## 4.1 Descripción de la arquitectura propuesta

SICIMED se implanta sobre una arquitectura de tres capas, desplegada como una aplicación
distribuida: un cliente web, un servidor de aplicación que expone una API REST y una base de
datos relacional. Cada capa se despliega y se escala por separado, y el contrato entre ellas es
HTTP con JSON, de modo que el frontend puede vivir en otro equipo sin acoplarse al backend.

### Capa de presentación — Angular (SPA)

Aplicación web de una sola página construida con Angular 21 y componentes *standalone*, con
Bootstrap 5 para la maquetación. Contiene el login, el registro de paciente, el panel, el flujo
de citas, la agenda del médico y los módulos de configuración. Los componentes no escriben URLs:
consumen servicios Angular tipados, y un interceptor funcional adjunta el token JWT a cada
petición.

### Capa de negocio y API — Spring Boot

Servidor de aplicación en Java 17 sobre Spring Boot 3.3, empaquetado como JAR ejecutable. Los
`@RestController` exponen las rutas bajo `/api`, delegan en servicios `@Service` que contienen
las reglas de negocio, y la seguridad se resuelve con Spring Security. No hay SQL en los
controllers ni en los servicios.

### Capa de persistencia — Spring Data JPA sobre MySQL

Spring Data JPA con Hibernate. Las entidades son clases Java anotadas; los repositorios extienden
`JpaRepository` e incluyen consultas JPQL con parámetros nombrados donde el método derivado no
alcanza. MySQL 8 actúa como base de datos relacional, con un perfil alternativo H2 en memoria
para desarrollo y pruebas.

### Flujo normal de una operación (agendar una cita)

```
Angular (HttpClient + JWT)
  → POST /api/citas
  → JwtAuthenticationFilter valida el token y arma el principal
  → CitaController valida el body (Bean Validation) y el rol (@PreAuthorize)
  → CitaServicio (@Transactional): valida existencia, fecha y conflicto de horario
  → CitaRepositorio: JPQL con named parameters
  → Hibernate → MySQL
  → respuesta 201 con la cita creada
```

**Figura 4.1. Arquitectura en tres capas de SICIMED sobre Spring Boot.**

---

## 4.2 Alternativa A — Spring Boot + Spring Data JPA (elegida)

Backend en Java 17 con Spring Boot 3.3, empaquetado como JAR ejecutable, con Tomcat embebido.

**Ventajas**

- Spring Data JPA elimina el SQL repetitivo de los DAO: las operaciones CRUD estándar se
  resuelven heredando `JpaRepository`, y solo las consultas con criterio real (choque de horario,
  horas ocupadas, reportes con filtros) se escriben en JPQL.
- Las transacciones se declaran con `@Transactional`: el servicio declara el límite transaccional
  y el rollback ante excepción es automático, sin `try/catch` manual de SQL.
- Spring Security resuelve la autenticación y la autorización con configuración declarativa
  (`@PreAuthorize`) en lugar de filtros escritos a mano.
- La prueba automatizada y el despliegue son más simples: basta `java -jar`.
- Es el stack que exige el sílabo del curso para las unidades III y IV.

**Limitaciones**

- Introduce una curva de aprendizaje (contenedores de inyección de dependencias,
  `JpaRepository`, ciclo de vida de las entidades).
- Abstrae el acceso a datos: en consultas muy específicas se puede terminar escribiendo JPQL que
  tampoco es SQL, y hay que revisar el SQL generado cuando el rendimiento importa.
- Agrega dependencias que el proyecto debe mantener actualizadas.

---

## 4.3 Alternativa B — Jakarta Servlets 6 + JDBC manual (descartada)

Backend con Servlets 6 empaquetado como WAR, desplegado en Tomcat 10, y acceso a datos con JDBC
escrito a mano, como se تجعل en las semanas 2 y 3 del curso.

**Ventajas**

- Control explícito del ciclo HTTP: se ve exactamente qué ocurre en cada petición.
- Sin dependencias de framework: el WAR es autocontenido y el arranque es inmediato.
- Control fino del SQL: para consultas muy específicas se escribe exactamente el SQL
  que uno quiere.

**Limitaciones que justifican el descarte**

- Cada endpoint exige un Servlet, y cada consulta, un DAO con `PreparedStatement`,
  `ResultSet` y `try-with-resources`: es la opción con más código repetitivo.
- **No hay transacciones.** Sin Spring, la transacción hay que abrirla y cerrarla a mano, y un
  error a mitad de una operación deja la base en estado inconsistente. El sílabo exige
  explícitamente el uso de `@Transactional` y rollback.
- **La seguridad hay que construirla.** Autenticación, emisión de token, control de roles y
  filtros CORS se escriben a mano. El sílabo exige Spring Security con OAuth 2 y JWT.
- **No hay paginación de Spring Boot**, ni documentación OpenAPI automática, ni integración con
  un ORM, que son los demás saberes de la Unidad III.

---

## 4.4 Ponderación de alternativas

Cada criterio se califica de 1 a 5. El puntaje es `(peso × nota) / 100`.

| Criterio | Peso (%) | Spring Boot + JPA | Servlets + JDBC |
|---|---|---|---|
| Alineación con los saberes exigidos por el sílabo (Spring Data, JPA, @Transactional, Spring Security, JWT, paginación) | 25 | 5 | 1 |
| Seguridad (autenticación, roles y tokens sin código propio) | 20 | 5 | 2 |
| Transacciones y consistencia ante errores | 15 | 5 | 2 |
| Tiempo de desarrollo de un endpoint nuevo | 15 | 5 | 2 |
| Documentación de la API (OpenAPI/Swagger) | 10 | 5 | 1 |
| Control fino del SQL y del ciclo HTTP | 10 | 3 | 5 |
| Simplicidad del despliegue | 5 | 5 | 3 |
| **Total** | **100** | — | — |
| **Puntaje ponderado** | | **4,90** | **1,80** |

**Decisión:** se adopta **Spring Boot 3.3 con Spring Data JPA**. El puntaje de 4,90 frente a
1,80 no deja lugar a ambigüedad, y la diferencia no es solo de gusto técnico: la alternativa
descartada no puede satisfacer los requisitos explícitos de la Unidad III (transacciones con
rollback, Spring Security con JWT, paginación de Spring Boot, JPQL con parámetros nombrados)
sin reconstruir a mano lo que el framework ya entrega.

El código de Servlets del proyecto no se descartó sin más: se conservó y sirvió de
especificación funcional. Todas las reglas de negocio, mensajes en español, códigos de estado
y casos de prueba del desarrollo con Servlets se conservaron al migrar, y esa equivalencia
es lo que verificó el Capítulo 6.

---

## 4.5 Estrategia de persistencia: Spring Data JPA frente a JDBC manual

| Criterio | Peso (%) | Spring Data JPA | JDBC manual |
|---|---|---|---|
| Código necesario para un CRUD estándar | 25 | 5 | 2 |
| Transacciones declarativas con rollback | 20 | 5 | 1 |
| Consultas con criterio (choque de horario, disponibilidad, reportes) | 20 | 4 | 5 |
| Portabilidad a otro motor de base de datos | 15 | 4 | 2 |
| Riesgo de inyección SQL (consultas parametrizadas) | 10 | 5 | 4 |
| Control del plan de ejecución y de los índices | 10 | 3 | 5 |
| **Total ponderado** | **100** | **4,55** | **2,95** |

**Decisión: Spring Data JPA.** Se mantiene JPQL explícito (`@Query` con parámetros nombrados
`:medicoId`, `:fecha`, `:hora`) en las consultas donde la legibilidad del criterio importa, en
lugar de confiar en derivación de nombres de método para lógica de negocio. La Criteria API
(`Specification`) cubre los listados filtrados por rol.

---

## 4.6 Seguridad: JWT con Spring Security

El esquema es *stateless*: el servidor no guarda sesión.

1. `POST /api/auth/login` valida las credenciales con `BCryptPasswordEncoder` y firma un **JWT
   HS256** que incluye `userId`, `rol`, `nombre`, `medicoId` y `pacienteId`.
2. `JwtAuthenticationFilter` intercepta cada petición, extrae la cabecera `Authorization: Bearer`,
   valida la firma y la expiración, y establece el `SecurityContext`.
3. `SecurityConfig` declara las rutas públicas (`/api/auth/**`, Swagger) y exige un token
   válido para el resto, con `SessionCreationPolicy.STATELESS`.
4. La autorización por rol se aplica con `@PreAuthorize` sobre los controladores
   (`hasRole('ADMIN')`, `hasAnyRole('MEDICO','ADMIN')`), y el control de pertenencia de una cita
   se valida en el servicio: un paciente solo accede a las suyas, un médico solo a las de su
   agenda.
5. CORS se configura con `CorsConfigurationSource` limitado a `http://localhost:4200`.

El token de la versión anterior (un `TokenStore` en memoria, que se perdía al reiniciar el
servidor) se sustituyó por JWT firmado: es stateless, sobrevive a reinicios y es lo que exige el
sílabo.

---

## 4.7 Diseño de la comunicación frontend–backend

**Formato:** JSON con `Content-Type: application/json;charset=UTF-8`.

**Fechas y horas:** ISO 8601. Fecha `yyyy-MM-dd`, hora `HH:mm`. La entidad `Cita` anota su campo
`hora` con `@JsonFormat(pattern = "HH:mm")` para que la API devuelva exactamente el mismo formato
que devuelve `/api/citas/disponibilidad`; sin esa anotación, Jackson serializaría `10:00:00` y el
cliente tendría que normalizar.

**Autenticación:** cabecera `Authorization: Bearer <token>` en todas las rutas salvo
`/api/auth/**` y `/api/sistema/**`.

**Paginación:** los listados de citas y el reporte devuelven una envoltura uniforme
(`content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`) construida por
`PageResponse`. Se acotó el tamaño de página a un máximo de 200 registros.

**Errores:** `GlobalExceptionHandler` traduce cada excepción de dominio a un código HTTP y a una
envoltura común, de modo que Angular muestre siempre el mensaje en español.

| Situación | Excepción | Código |
|---|---|---|
| Token ausente o inválido | `JwtAuthenticationFilter` / `SecurityConfig` | 401 |
| Rol sin permiso o cita ajena | `AccesoDenegadoException`, `@PreAuthorize` | 403 |
| Dato inválido o regla incumplida | `ReglaNegocioException` | 400 |
| Recurso inexistente | `RecursoNoEncontradoException` | 404 |
| Horario ocupado o usuario repetido | `ConflictoException` | 409 |
| Error no controlado | `Exception` | 500 |

**Figura 4.2. Comunicación frontend – API REST – Spring Data JPA – MySQL.**

---

## 4.8 Herramientas y librerías

| Área | Herramienta / librería |
|---|---|
| Backend | Java 17, Spring Boot 3.3.5, Maven |
| Seguridad | Spring Security, JJWT 0.12 (HS256), BCrypt |
| Persistencia | Spring Data JPA, Hibernate 6, JPQL y Criteria API |
| Base de datos | MySQL 8 (producción), H2 en memoria (perfil `dev`) |
| Documentación | springdoc-openapi → Swagger UI en `/swagger-ui.html` |
| Frontend | Angular 21, componentes *standalone*, Bootstrap 5, signals (`input`/`output`) |
| Pruebas | Script de humo `docs/probar_api.sh` (38 casos), Swagger UI |
| Control de versiones | Git / GitHub |

---

## 4.9 Análisis de viabilidad técnica

La arquitectura es viable dentro de las condiciones del proyecto: Java 17, Maven, MySQL y
Angular son herramientas gratuitas o de edición gratuita, y el backend se despliega como un
único JAR sin contenedor de aplicación que instalar.

Los riesgos técnicos identificados y cómo se resolvieron:

| Riesgo | Resolución |
|---|---|
| Dos pacientes reserven el mismo horario a la vez | `@Version` (bloqueo optimista) en `Cita` + verificación de conflicto en JPQL dentro de la transacción → 409 |
| Se pierde el control de acceso al migrar de `TokenStore` a JWT | `JwtAuthenticationFilter` + `@PreAuthorize`; verificado con casos 401/403 del Capítulo 6 |
| Formato de hora inconsistente entre endpoints | `@JsonFormat(pattern = "HH:mm")` en la entidad y en el DTO de entrada |
| Consultas que devuelven demasiados registros | Paginación con `Pageable` y tope de 200 por página |
| El servidor acepta peticiones antes de que la base esté sembrada | `DatosSemilla` marca una bandera de disponibilidad que `/api/sistema/estado` expone como `preparado` |

**Figura 4.3. Diagrama de arquitectura del sistema.**

---

*Capítulo 4 — versión revisada tras la migración a Spring Boot 3.3 con Spring Data JPA,
Spring Security y JWT.*
