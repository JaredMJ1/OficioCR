# OficioCR

OficioCR conecta a personas que publican oportunidades de trabajo con quienes quieren aceptarlas. Incluye cuentas de usuario, perfil, administración y anuncios.

## Ejecutar el proyecto

Desde la raíz:

```bash
docker compose up --build
```

La aplicación web queda en `http://localhost:4200` y la API en `http://localhost:7001/api`. La base de datos PostgreSQL conserva la información en el volumen `oficiocr-data`.

Configura las variables necesarias en `.env` antes de iniciar el entorno. No subas ese archivo ni compartas sus secretos.

## Documentación

- [Backend: API, cookies y sesión](backend/README.md)
- [Frontend: estructura y ejecución](frontend/README.md)

El backend usa una cookie de sesión `HttpOnly`. El navegador la envía automáticamente; la aplicación no guarda el token de sesión en `localStorage`.
