# Backend de OficioCR

API Spring Boot 3 con Java 21, Spring Security, Spring Data JPA y PostgreSQL. El backend valida las solicitudes, aplica permisos y guarda los datos. La sesión se autentica con cookies; no se usan JWT.

## Ejecutar

Desde la raíz del proyecto:

```bash
docker compose up --build
```

La API escucha en `http://localhost:7001/api`. La conexión de base de datos se configura con `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` (Docker los prepara). El origen del frontend se configura con `FRONTEND_ORIGIN`.

## Cómo funciona la sesión con cookies

1. Antes de iniciar sesión, el cliente solicita `GET /api/auth/csrf`. El servidor guarda la cookie CSRF y devuelve un token para enviar en el encabezado `X-CSRF-TOKEN`.
2. El cliente envía `POST /api/auth/login` con correo y contraseña, el token CSRF y conserva las cookies. Si las credenciales son válidas, el servidor crea la sesión y responde con una cookie `HttpOnly`.
3. En solicitudes posteriores, el navegador o cliente HTTP envía la cookie automáticamente. No hace falta leerla ni copiar su contenido en el código.
4. Toda solicitud `POST`, `PUT`, `PATCH` o `DELETE` a `/api` debe incluir también el token CSRF. El interceptor de Angular se encarga de obtenerlo y enviarlo.
5. `POST /api/auth/logout` cierra la sesión y elimina su cookie.

La cookie de sesión se llama `oficiocr.session`, es HttpOnly y SameSite=Lax. La sesión vence a los 30 minutos y Spring Session la guarda en Redis, no en la memoria de la API. Así puede recuperarse después de reiniciar la API. En Docker Compose, Redis tiene un volumen persistente. El token CSRF se entrega por `GET /api/auth/csrf` y las operaciones que cambian datos requieren `X-CSRF-TOKEN`.

Para desplegar con HTTPS, activa `SESSION_COOKIE_SECURE=true` y configura `SESSION_COOKIE_NAME=__Host-oficiocr.session`.

### Acceder al usuario actual desde el backend

En controladores puedes inyectar `@AuthenticationPrincipal AuthController.Principal p`. En cualquier servicio o controlador también puedes inyectar `CurrentUser`: `currentUser.required()` devuelve el principal, `currentUser.id()`, `name()`, `email()` y `role()` dan sus campos, y `currentUser.optional()` permite consultar si hay sesión sin lanzar excepción. Usa el ID de la sesión para asociar datos; no aceptes del cliente un ID de propietario.

### Sesión en Redis

Spring Session guarda el contexto de autenticación y la sesión HTTP en Redis. La cookie solo identifica la sesión; el backend recupera de Redis los datos y el usuario autenticado en cada solicitud. `docker compose up --build` inicia Redis junto con PostgreSQL y la API.

## Rutas principales

| Método | Ruta | Acceso |
|---|---|---|
| `GET` | `/api/health` | Público |
| `GET` | `/api/auth/csrf` | Público; prepara el token CSRF |
| `POST` | `/api/auth/register` | Público + CSRF |
| `POST` | `/api/auth/login` | Público + CSRF |
| `POST` | `/api/auth/logout` | Sesión + CSRF |
| `GET` | `/api/auth/me` | Sesión |
| `PUT` | `/api/auth/me` | Sesión + CSRF; edita nombre y correo |
| `POST` | `/api/auth/password/forgot` | Público + CSRF |
| `POST` | `/api/auth/password/reset` | Público + CSRF |
| `GET` | `/api/jobs?search=...` | Público |
| `POST` | `/api/jobs` | Sesión + CSRF; publica un anuncio |
| `POST` | `/api/jobs/{id}/accept` | Sesión + CSRF; acepta un anuncio disponible de otra persona |
| `GET` | `/api/users` | Rol `superusuario` |
| `POST` | `/api/admin/operators` | Rol `superusuario` + CSRF |

El registro crea cuentas que pueden publicar y aceptar trabajos. Las rutas de administración siguen reservadas para `superusuario`. Los endpoints de desarrollo `/api/test/users` solo se habilitan con `ENABLE_TEST_ENDPOINTS=true`.

La recuperación de contraseña usa SMTP. En desarrollo, si SMTP no está configurado, el enlace se muestra en los logs de la API; fuera de desarrollo se requiere configurar SMTP.
