# Backend de OficioCR

API ASP.NET Core 8 con PostgreSQL y Entity Framework Core. El backend valida las solicitudes, aplica permisos y guarda los datos. La sesión se autentica con cookies; no se usan JWT.

## Ejecutar

Desde la raíz del proyecto:

```bash
docker compose up --build
```

La API escucha en `http://localhost:7001/api`. La conexión de base de datos se configura con `ConnectionStrings:OficioCR` o `DATABASE_URL`. El origen del frontend se configura con `FRONTEND_ORIGIN`.

## Cómo funciona la sesión con cookies

1. Antes de iniciar sesión, el cliente solicita `GET /api/auth/csrf`. El servidor guarda la cookie CSRF y devuelve un token para enviar en el encabezado `X-CSRF-TOKEN`.
2. El cliente envía `POST /api/auth/login` con correo y contraseña, el token CSRF y conserva las cookies. Si las credenciales son válidas, el servidor crea la sesión y responde con una cookie `HttpOnly`.
3. En solicitudes posteriores, el navegador o cliente HTTP envía la cookie automáticamente. No hace falta leerla ni copiar su contenido en el código.
4. Toda solicitud `POST`, `PUT`, `PATCH` o `DELETE` a `/api` debe incluir también el token CSRF. El interceptor de Angular se encarga de obtenerlo y enviarlo.
5. `POST /api/auth/logout` cierra la sesión y elimina su cookie.

En desarrollo, la cookie de sesión se llama `oficiocr.session`; en producción usa `__Host-oficiocr.session` y HTTPS. La sesión vence a los 30 minutos. El ticket se conserva en la caché distribuida en memoria, por lo que reiniciar la API invalida las sesiones activas.

### Usar al usuario de la sesión en un endpoint

Añade `RequireAuthorization()` a la ruta e inyecta `CurrentUser`. Llama a `Get()` para recibir el ID, nombre, correo y rol ya autenticados:

```csharp
private static async Task<IResult> CreateAsync(
    CreateRequest request,
    CurrentUser currentUser,
    AppDbContext database)
{
    var user = currentUser.Get();
    if (user is null) return Results.Unauthorized();

    var record = new MyEntity
    {
        OwnerId = user.Id,
        Name = request.Name
    };

    database.Add(record);
    await database.SaveChangesAsync();
    return Results.Ok();
}
```

Usa `user.Id` para asociar el registro. No recibas el ID del propietario desde el cliente: podría intentar enviar el ID de otra cuenta. `Get()` devuelve `null` si no encuentra un ID válido en la identidad de la solicitud. La autenticación y la lectura de esa identidad se configuran en `Configuration/AuthenticationExtensions.cs` y `Security/CurrentUser.cs`.

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
