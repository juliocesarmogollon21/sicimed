# SICIMED — Frontend Angular

Cliente web del Sistema de Citas Médicas. Se ejecuta en el puerto 4200 y consume la API REST del
backend (por defecto `http://localhost:8080/api`).

Para la documentación completa del proyecto —incluidos requisitos, puesta en marcha del backend,
estructura y pruebas— ver el [README principal](../README.md).

---

## Requisitos

| Herramienta | Versión |
|---|---|
| Node.js | 20 o superior |
| npm | 10 o superior |

```bash
node -v
npm -v
```

---

## Puesta en marcha

```bash
npm install
npx ng serve
```

Abrir http://localhost:4200

El backend debe estar corriendo; si no, el login fallará con un error de red.

---

## Compilar para producción

```bash
npx ng build
```

La salida queda en `dist/frontend/`. Se sirve como archivos estáticos desde cualquier servidor
web o CDN.

> Nota: el build emite un aviso de tamaño del bundle (771 kB sobre un presupuesto de 500 kB). No
> es un error y no bloquea la compilación; el peso viene principalmente de Bootstrap.

---

## Configurar la URL del backend

Está en `src/environments/environment.ts`:

```ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

Si el backend corre en otro puerto, contexto o dominio, cambiar solo esa línea.

El CORS del backend permite únicamente el origen `http://localhost:4200`. Si el frontend se sirve
en otro origen, hay que agregarlo también en `SecurityConfig.setAllowedOrigins`.

---

## Credenciales de demostración

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `recepcion` | `recep123` | RECEPCIONISTA |
| `medico1` | `medico123` | MEDICO |
| `paciente1` | `paciente123` | PACIENTE |

También se puede crear una cuenta nueva en `/registro` (rol PACIENTE).

---

## Estructura

```
src/
├── main.ts
├── styles.css
├── index.html
├── environments/
│   ├── environment.ts               URL del backend
│   └── environment.development.ts
└── app/
    ├── app.ts / app.html / app.css          Shell y menú según rol
    ├── app.routes.ts                        Rutas protegidas por guardas
    ├── app.config.ts                        Router + HttpClient + interceptores
    ├── interceptors/
    │   └── auth.interceptor.ts              Adjunta el token; cierra sesión ante 401
    ├── modelos/                             Interfaces tipadas (Cita, Medico, Pagina, ...)
    ├── servicios/                           Un servicio por recurso + guardas de ruta
    └── componentes/
        ├── auth/            login, registro
        ├── dashboard/       Panel principal
        ├── citas/           lista, nueva, reprogramar
        ├── medico/          agenda-medico
        └── configuracion/   hub, sedes, especialidades, medicos, usuarios, medicamentos
```

---

## Convenciones

- **Un componente, un archivo trio** (`nombre.ts`, `nombre.html`, `nombre.css`) dentro de su carpeta.
- **Los componentes no escriben URLs**: llaman a un servicio de `src/app/servicios/`.
- **El token se añade en un solo lugar**: el interceptor. Ningún componente lo gestiona.
- **Angular 21**: los componentes son standalone por defecto, así que no se escribe
  `standalone: true`.
- **Signals**: se usa `input()` y `output()` en lugar de decoradores `@Input` / `@Output`.
- **Nombres en español** para el dominio (cita, sede, medico) y en camelCase para los servicios.

---

## Problemas frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| `Login inválido` y el backend está arriba | El token no viaja en la cabecera | Revisar `src/app/interceptors/auth.interceptor.ts` |
| Error de CORS en la consola del navegador | Origen no permitido | Agregar el origen en `SecurityConfig` del backend |
| `Failed to fetch` | El backend no está corriendo o el puerto cambió | Verificar `/api/sistema/estado` y `apiUrl` |
| El build falla tras `npm install` | `node_modules` incompleto | `rm -rf node_modules package-lock.json && npm install` |
| Puerto 4200 ocupado | Otro proceso | `npx ng serve --port 4300` y actualizar `apiUrl` |
| Avisos `install-scripts` al instalar | npm 11.19+ bloquea scripts de instalación por seguridad | Son avisos, no errores: el build funciona igual. Si aun así falla, `npm install --foreground-scripts` |
