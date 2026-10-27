# OficioCR
Plataforma que conecta clientes con trabajadores de servicios para el hogar.

## Estructura
- `frontend/` Angular
- `database/` scripts SQL y datos semilla
- `docs/`     ERS, diagramas, contrato API, actas de sprint, guion de demo

El backend (ASP.NET Core) se desarrolla en un proyecto aparte. La URL de la API se configura en
`frontend/src/environments/environment.ts` (`apiUrl`).

## Ejecutar el frontend
```bash
cd frontend
npx -p @angular/cli ng new oficiocr-web --directory . --routing --style=scss --skip-git   # solo la primera vez
npm install
ng serve        # http://localhost:4200
```

## Flujo de Git
`main` estable, `develop` integración, ramas `feature/<modulo>-<descripcion>` + Pull Request.

## Reparto
1. Usuarios / Auth / Administración
2. Servicios / Búsqueda / Solicitudes / Valoraciones
3. IA / Mensajería / Notificaciones / Pagos
