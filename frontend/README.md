# Frontend de OficioCR

Aplicación web Angular. La API ASP.NET Core y PostgreSQL están en `../backend/OficioCR.Api`.

## Ejecutar

Con Docker, inicia todo desde la raíz:

```bash
docker compose up --build
```

Para desarrollar solo el frontend, instala dependencias y ejecuta Angular desde esta carpeta:

```bash
npm install
npm start
```

El frontend consume la API configurada en `src/environments/environment.ts`.

## Secciones

- `/registro` y `/login`: crear cuenta e iniciar sesión.
- `/trabajos`: buscar anuncios, publicar y aceptar trabajos disponibles.
- `/perfil`: consultar y editar nombre y correo.
- `/admin`: administración reservada al superusuario.

El interceptor HTTP envía las cookies con las solicitudes y obtiene el token CSRF requerido para las operaciones que modifican datos. La cookie de sesión es `HttpOnly`; Angular no la lee ni la almacena en `localStorage`.
