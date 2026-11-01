# Mapa de decisiones

> Esta tabla sirve para encontrar rápido **qué se decidió**, **con qué apunte se relaciona**, **dónde está en el código** y **qué documento lo explica**.

## Cómo leerla

- **Decisión:** la idea que se eligió.
- **Apunte:** el material de la materia más relacionado.
- **Dónde está:** archivo, módulo o configuración que la aplica.
- **Explicación:** documento de esta carpeta con el desarrollo completo.

## Decisiones

| # | Decisión | Apunte | Dónde está | Explicación |
|---|---|---|---|---|
| D-01 | Microservicios por bounded context | 17, 19 | `services/*`, `platform/api-gateway` | [02](02-arquitectura-y-bounded-contexts.md) |
| D-02 | Monorepo Maven multi-módulo | 15 | `pom.xml`, `libs/*`, `services/*` | [03](03-stack-y-build.md) |
| D-03 | Cada servicio se empaqueta y despliega por separado | 15, 19 | `pom.xml`, `Dockerfile` de cada servicio | [03](03-stack-y-build.md) |
| D-04 | Database per Service | 16 | `infra/postgres/init`, `application-docker.yml` | [04](04-datos-y-persistencia.md) |
| D-05 | PostgreSQL en Docker, H2 en local/test | 16 | `application.yml`, `application-docker.yml` | [04](04-datos-y-persistencia.md) |
| D-06 | Flyway como fuente de verdad del esquema | 16 | `db/migration/*` | [04](04-datos-y-persistencia.md) |
| D-07 | Snapshot de domicilio al crear solicitud | 16, 18 | Dominio de `ms-solicitudes` (pendiente) | [04](04-datos-y-persistencia.md) |
| D-08 | Keycloak como Authorization Server | 20 | `infra/keycloak/realm-patito.json` | [05](05-seguridad.md) |
| D-09 | JWT + Resource Server en gateway y servicios | 20, 21 | `application-docker.yml`, `libs/security-jwt` | [05](05-seguridad.md) |
| D-10 | Roles `CLIENTE`, `TECNICO`, `ADMIN` | 20, 21 | `realm-patito.json` | [05](05-seguridad.md) |
| D-11 | Defensa en profundidad: gateway + servicios | 19, 21 | `SecurityConfig` de cada módulo | [05](05-seguridad.md) |
| D-12 | REST/JSON síncrono entre servicios | 14, 18 | `client/*`, `contracts/openapi` | [06](06-comunicacion-entre-servicios.md) |
| D-13 | `RestClient` en servicios MVC | 18 | `RestClientConfig`, clientes | [06](06-comunicacion-entre-servicios.md) |
| D-14 | OpenAPI como contrato público e interno | 14, 17 | `contracts/openapi` | [06](06-comunicacion-entre-servicios.md) |
| D-15 | `ProblemDetail` para errores | 17, 18 | `libs/common-web` | [06](06-comunicacion-entre-servicios.md) |
| D-16 | Timeouts, retry y circuit breaker | 18 | `resilience4j` en `ms-solicitudes` | [06](06-comunicacion-entre-servicios.md) |
| D-17 | Sin Kafka/RabbitMQ en el MVP | 18 | Decisión de arquitectura | [06](06-comunicacion-entre-servicios.md) |
| D-18 | API Gateway solo en el borde | 19 | `platform/api-gateway` | [07](07-gateway.md) |
| D-19 | Rutas estáticas sin Eureka | 19 | `application.yml` del gateway | [07](07-gateway.md) |
| D-20 | Gateway sin lógica de negocio | 19 | `platform/api-gateway` | [07](07-gateway.md) |
| D-21 | Docker Compose para el entorno integrado | 15, 19, 21 | `infra/compose/docker-compose.yml` | [08](08-infraestructura-y-operacion.md) |
| D-22 | Perfiles `default`, `local`, `docker` | 15 | `application*.yml` | [08](08-infraestructura-y-operacion.md) |
| D-23 | Configuración por variables de entorno | 15 | `application-docker.yml`, Compose | [08](08-infraestructura-y-operacion.md) |
| D-24 | Actuator y métricas básicas | 17 | `management.endpoints` | [08](08-infraestructura-y-operacion.md) |
| D-25 | Sin Kubernetes ni service mesh | 19 | Decisión de arquitectura | [08](08-infraestructura-y-operacion.md) |
| D-26 | Tests con JUnit, Testcontainers y WireMock | 16, 18, 21 | `libs/test-support`, `src/test` | [09](09-testing-y-calidad.md) |
| D-27 | Smoke test pendiente contra gateway | 19 | `scripts/smoke-test.ps1` | [09](09-testing-y-calidad.md) |
| D-28 | ADRs y guía extendida de decisiones | 14, 17 | `docs/arquitectura`, `decisiones/` | [10](10-documentacion-y-organizacion.md) |
| D-29 | PDFs fuera de Git | — | `.gitignore` | [10](10-documentacion-y-organizacion.md) |
| D-30 | Placeholders y TODOs explícitos | 17 | Todo el esqueleto | [10](10-documentacion-y-organizacion.md) |
| D-31 | No implementar lógica en esta entrega | 17 | `TODO` en clases | [11](11-decisiones-pendientes.md) |
| D-32 | Próximos pasos: contratos, seguridad, datos y flujo | 14–21 | Roadmap | [11](11-decisiones-pendientes.md) |

## Decisiones que no están tomadas todavía

| Tema | Pregunta abierta | Documento |
|---|---|---|
| Propagación de tokens | ¿Token del usuario o client credentials? | [05](05-seguridad.md) |
| IDs | ¿`Long`, `UUID` u otro? | [11](11-decisiones-pendientes.md) |
| Concurrencia | ¿Bloqueo pesimista, optimista o ambos? | [04](04-datos-y-persistencia.md) |
| Resiliencia | ¿Timeouts, retries y fallbacks concretos? | [06](06-comunicacion-entre-servicios.md) |
| Contratos | ¿Forma exacta de DTOs y errores? | [06](06-comunicacion-entre-servicios.md) |
| Eventos | ¿Kafka/outbox en una etapa futura? | [11](11-decisiones-pendientes.md) |
| Discovery | ¿Eureka/Kubernetes si crece el sistema? | [07](07-gateway.md) |

## Regla de mantenimiento

Cada vez que se agregue o cambie una decisión:

1. actualizar el ADR correspondiente;
2. actualizar el documento de esta carpeta;
3. agregar o modificar la fila en esta tabla;
4. si cambia una regla de trabajo, actualizar el README principal.
