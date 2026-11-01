# ADR-002: Database per Service

- Estado: Aceptado
- Fecha: 2026-10-31
- Implementación: parcial (bases, perfiles y migraciones base definidos; esquemas pendientes)

## Contexto

Compartir tablas entre microservicios acopla sus modelos de datos y obliga a coordinar cambios de esquema entre equipos. El proyecto necesita que cada bounded context evolucione sus datos sin romper a los demás y sin depender de JOIN entre bases.

## Decisión

Cada microservicio de dominio administra su propia base de datos lógica:

| Servicio | Base de datos |
|---|---|
| `ms-clientes` | `patito_clientes` |
| `ms-tecnicos` | `patito_tecnicos` |
| `ms-solicitudes` | `patito_solicitudes` |

- En Docker y producción se usa PostgreSQL 16.
- Para desarrollo local y pruebas se admite H2 en memoria.
- Flyway es el mecanismo de migración versionada para PostgreSQL.
- En el perfil `docker`, Hibernate valida el esquema (`ddl-auto: validate`) y Flyway está habilitado.
- En el perfil por defecto se usa H2 con `ddl-auto: create-drop` y Flyway deshabilitado para facilitar el arranque sin infraestructura.
- Cada servicio es dueño de sus tablas. No se permiten JOIN ni claves foráneas entre bases de servicios distintos.
- La integración entre contextos se hace por API, no por acceso directo a la base de otro servicio.

Para el MVP se usa una única instancia PostgreSQL con varias bases lógicas, no un contenedor por servicio. La separación es lógica y de propiedad del esquema; el aislamiento físico puede agregarse después sin cambiar el contrato entre servicios.

## Alternativas consideradas

- **Base de datos compartida:** descartada porque genera acoplamiento y contradice la separación por bounded contexts.
- **Un contenedor PostgreSQL por servicio:** descartado para el MVP por consumo de recursos y complejidad operativa.
- **H2 como base principal:** descartado porque no representa fielmente PostgreSQL ni las migraciones de producción.

## Consecuencias

- Cada servicio puede cambiar su esquema sin romper a los demás.
- No hay transacciones distribuidas ni consistencia fuerte entre contextos.
- Los datos que un servicio necesita de otro deben obtenerse por API o mantenerse como una copia/snapshot con una estrategia de actualización explícita.
- Hay duplicación controlada de datos y necesidad de tolerar consistencia eventual.
- Las migraciones deben revisarse por servicio y no pueden depender de tablas de otro contexto.
- H2 y PostgreSQL no son idénticos; las pruebas de integración con Testcontainers/PostgreSQL son la referencia antes de desplegar.

## Implementación en el repositorio

- `infra/postgres/init/01-create-databases.sql`: crea las bases lógicas y la base de Keycloak.
- `services/*/src/main/resources/application.yml`: configuración H2 por defecto.
- `services/*/src/main/resources/application-docker.yml`: PostgreSQL, Flyway y validación del esquema.
- `services/*/src/main/resources/db/migration/`: migraciones Flyway de ejemplo (`V1`, `V2`) pendientes de completar.
