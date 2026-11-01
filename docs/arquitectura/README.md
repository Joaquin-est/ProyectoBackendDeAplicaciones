# Arquitectura

Documentación de las decisiones y los diagramas del backend de Técnica Patito.

## Guía extendida

- [Decisiones explicadas](../../decisiones/README.md): explicación didáctica, conexión con los apuntes y preguntas de repaso.
- [Mapa de decisiones](../../decisiones/mapa-decisiones.md)
- [Glosario](../../decisiones/glosario.md)
- [Preguntas de repaso](../../decisiones/preguntas-de-repaso.md)

## Decisiones

- [Registro consolidado de decisiones](decisiones.md)
- [ADR-001: Monorepo Maven multi-módulo](adr-001-monorepo-multimodulo.md)
- [ADR-002: Database per Service](adr-002-database-per-service.md)
- [ADR-003: Seguridad con Keycloak y OAuth2](adr-003-keycloak-oauth2.md)
- [ADR-004: Comunicación síncrona con RestClient](adr-004-comunicacion-sincrona.md)
- [ADR-005: API Gateway solo en el borde](adr-005-gateway-borde.md)

## Diagramas

- [C4 de contexto](c4-contexto.puml) — placeholder
- [C4 de contenedores](c4-contenedores.puml) — placeholder

## Convenciones

- APIs públicas: `/api/v1/**`.
- APIs internas: `/internal/v1/**`; no se exponen por el gateway.
- Errores: `ProblemDetail` (RFC 7807).
- Correlación: `X-Correlation-Id`.
- Persistencia: una base lógica por servicio, sin JOIN entre contextos.
- Seguridad: Keycloak como emisor y validación JWT en gateway y servicios.
