# Contratos API · Integrante 3 (mensajería, IA, pagos de prueba)

> Propuesta para acordar con el equipo (RF-14). Formato JSON camelCase, fechas ISO 8601 UTC. Todos requieren JWT salvo indicación.

## Mensajería (RF-10)
| Método | Endpoint | Request | Response | Errores |
|---|---|---|---|---|
| GET | `/api/conversations` | — | `ConversationDto[]` | 401 |
| POST | `/api/conversations` | `{ otherUserId }` | `ConversationDto` (201; devuelve la existente si ya hay una) | 400, 401, 404 |
| GET | `/api/conversations/{id}/messages` | — | `MessageDto[]` (orden cronológico) | 401, **403** si no participa, 404 |
| POST | `/api/conversations/{id}/messages` | `{ content }` (1–1000) | `MessageDto` (201) | 400, 401, 403, 404 |

## Recomendaciones IA (RF-06)
`POST /api/ai/recommendations` · `{ description, location }` → `{ detectedCategory, services[], aiAvailable }`.
Si el proveedor falla: 200 con `aiAvailable=false` y `services=[]` (el front ofrece búsqueda manual, RNF-13).
Backend: interfaz `IARecommendationService` con implementación `Mock` y `Real` intercambiables; clave solo en configuración del servidor.

## Pagos de prueba (RF-08, RF-09)
- `POST /api/payments/checkout` · `{ requestId, idempotencyKey }` → `PaymentDto` (`isTest: true`). El **monto lo calcula el backend**. Misma `idempotencyKey` ⇒ mismo pago (sin duplicar). Errores: 400, 401, 403, 404, 409 (ya pagado).
- `GET /api/payments/history` → `PaymentDto[]` del usuario autenticado.
- Estados: `PENDIENTE, APROBADO, RECHAZADO, CANCELADO`. Nunca aprobar por respuesta del frontend.

## Notificaciones (RF-11, P2) — pendiente de acordar
`GET /api/notifications`, `PATCH /api/notifications/{id}/read`.

## Por acordar con los otros integrantes
- Cómo obtengo el `userId` autenticado en Angular (AuthService del Integrante 1).
- Cuándo se puede pagar una solicitud (¿ACEPTADA o COMPLETADA?) — Integrante 2.
- Quién crea las migraciones iniciales de EF Core.
