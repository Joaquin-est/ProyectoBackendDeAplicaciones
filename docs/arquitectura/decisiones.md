# Registro de decisiones — Backend Técnica Patito

- Estado: vigente para el esqueleto inicial
- Fecha: 2026-10-31
- Alcance: MVP de microservicios para coordinación de servicios técnicos domiciliarios

Este documento resume las decisiones tomadas para la estructura inicial. Las decisiones estructurales tienen un ADR propio; las decisiones transversales se registran aquí para que el equipo tenga una referencia única.

> Para la explicación didáctica, el contexto de la materia, ejemplos y preguntas de repaso, ver [Decisiones explicadas](../../decisiones/README.md).

## 1. Decisiones estructurales

| ID | Decisión | Estado | Documento |
|---|---|---|---|
| ADR-001 | Monorepo Maven multi-módulo con parent agregador | Aceptada | [ADR-001](adr-001-monorepo-multimodulo.md) |
| ADR-002 | Database per Service, PostgreSQL en Docker y H2 local | Aceptada | [ADR-002](adr-002-database-per-service.md) |
| ADR-003 | Keycloak + OAuth2 Resource Server con roles | Aceptada | [ADR-003](adr-003-keycloak-oauth2.md) |
| ADR-004 | Comunicación síncrona REST/JSON con `RestClient` | Aceptada | [ADR-004](adr-004-comunicacion-sincrona.md) |
| ADR-005 | Spring Cloud Gateway solo en el borde | Aceptada | [ADR-005](adr-005-gateway-borde.md) |

El parent Maven **no convierte el proyecto en un monolito**: solo centraliza versiones y permite compilar todo junto. Cada microservicio tiene su propio artefacto Spring Boot y su propio contenedor.

No se conserva una copia del esqueleto inicial de un solo módulo dentro del árbol. Se eliminó la carpeta `legacy/monolito`; la versión anterior sigue disponible en el historial de Git. El build actual incluye únicamente microservicios y librerías.

## 2. Stack técnico

| Área | Elección | Nota |
|---|---|---|
| Lenguaje | Java 17 | Compilación con `release 17`; un JDK 21 local puede compilarlo |
| Framework | Spring Boot 3.2.5 | Versión base del curso y del parent |
| Build | Maven 3.10 + Maven Wrapper | `mvnw` / `mvnw.cmd` incluidos |
| Cloud | Spring Cloud 2023.0.1 | Compatible con Boot 3.2.x |
| Gateway | Spring Cloud Gateway | Stack reactivo WebFlux |
| Persistencia | Spring Data JPA | Hibernate |
| Base principal | PostgreSQL 16 | Perfil `docker` y producción |
| Base local/test | H2 en memoria | Perfil por defecto y pruebas rápidas |
| Migraciones | Flyway | Habilitado en PostgreSQL; deshabilitado en H2 local |
| Seguridad | Keycloak 25 + OAuth2 Resource Server | JWT y roles de realm |
| Documentación API | springdoc OpenAPI 2.5.0 | Swagger UI por servicio |
| Resiliencia | Resilience4j 2.2.0 | Integrado en `ms-solicitudes`; políticas pendientes |
| Testing | JUnit 5, Spring Boot Test, Testcontainers 1.19.8, WireMock 3.5.4 | Dependencias preparadas; pruebas pendientes |
| CI | GitHub Actions | Java 17 y `mvn -B verify` |

Las versiones se gestionan en el `pom.xml` raíz. Cualquier actualización debe validarse con el reactor completo y, si cambia una decisión de arquitectura, actualizar el ADR correspondiente.

## 3. Mapa de módulos y límites

| Módulo | Responsabilidad | Puerto | Base | Rutas públicas principales |
|---|---|---:|---|---|
| `platform/api-gateway` | Borde, ruteo, CORS, correlación, seguridad de entrada | 8080 | — | `/api/v1/**` |
| `services/ms-clientes` | Clientes y domicilios | 8082 | `patito_clientes` | `/api/v1/clientes/**`, `/api/v1/domicilios/**` |
| `services/ms-tecnicos` | Técnicos, certificaciones, herramientas y zonas de cobertura | 8083 | `patito_tecnicos` | `/api/v1/tecnicos/**`, `/api/v1/zonas/**`, `/api/v1/certificaciones/**`, `/api/v1/herramientas/**` |
| `services/ms-solicitudes` | Solicitudes, ofertas, presupuestos, estados e historial | 8084 | `patito_solicitudes` | `/api/v1/solicitudes/**`, `/api/v1/ofertas/**`, `/api/v1/historial/**` |
| `libs/common-web` | ProblemDetail, paginación, correlación y validaciones | — | — | No se despliega |
| `libs/security-jwt` | Conversión de roles y configuración JWT | — | — | No se despliega |
| `libs/geo-client` | Integración con mapas y rutas | — | — | No se despliega |
| `libs/test-support` | Fixtures y utilidades de prueba | — | — | No se despliega |

Reglas de límites:

- Un servicio no accede a la base de otro servicio.
- Un servicio no importa entidades JPA de otro contexto.
- Las librerías compartidas contienen infraestructura transversal, no reglas de negocio.
- Las APIs internas se publican bajo `/internal/v1/**` y no se exponen en el gateway.
- Si se agrega un recurso público nuevo, debe agregarse también la ruta en `platform/api-gateway/src/main/resources/application.yml`.

## 4. Perfiles y configuración

| Perfil | Base de datos | Flyway | Seguridad | Uso |
|---|---|---|---|---|
| Por defecto | H2 en memoria | Deshabilitado | Issuer JWT sin configurar | Arranque rápido y desarrollo sin Keycloak |
| `local` | Overrides locales pendientes | Según override | Según override | Personalización del desarrollador |
| `docker` | PostgreSQL 16 | Habilitado | Issuer con `KEYCLOAK_ISSUER_URI` | Docker Compose y entorno integrado |

Variables principales:

| Variable | Uso |
|---|---|
| `POSTGRES_USER`, `POSTGRES_PASSWORD` | Credenciales de PostgreSQL |
| `KEYCLOAK_ADMIN`, `KEYCLOAK_ADMIN_PASSWORD` | Administración inicial de Keycloak |
| `KEYCLOAK_ISSUER_URI` | Issuer JWT para gateway y servicios |
| `MS_CLIENTES_URL` | URL interna de `ms-clientes` |
| `MS_TECNICOS_URL` | URL interna de `ms-tecnicos` |
| `MS_SOLICITUDES_URL` | URL interna de `ms-solicitudes` |
| `GEO_BASE_URL` | Proveedor de rutas/mapas |

Reglas:

- No se versionan secretos reales; `.env` está ignorado y `.env.example` usa placeholders.
- Los perfiles `local` existen como punto de extensión, pero todavía no tienen overrides implementados.
- El perfil por defecto es deliberadamente permisivo para que el esqueleto arranque sin infraestructura. No debe usarse como configuración de producción.
- En Docker, `ddl-auto` es `validate`; el esquema debe estar cubierto por Flyway antes de levantar los servicios.

## 5. Persistencia y datos

La decisión completa está en [ADR-002](adr-002-database-per-service.md). Resumen operativo:

- Una instancia PostgreSQL con bases lógicas separadas para el MVP.
- Cada servicio es dueño de su esquema y sus migraciones.
- Sin JOIN ni claves foráneas entre bases de contextos distintos.
- Flyway es la fuente de verdad del esquema en PostgreSQL.
- H2 se usa para desarrollo/pruebas rápidas; Testcontainers/PostgreSQL es la referencia para integración.
- Las migraciones `V1__schema.sql` y `V2__datos_iniciales.sql` son placeholders y deben completarse por cada equipo.

## 6. Seguridad

La decisión completa está en [ADR-003](adr-003-keycloak-oauth2.md). Resumen operativo:

- Keycloak es el único emisor de identidad.
- Roles: `CLIENTE`, `TECNICO`, `ADMIN`.
- Cliente público de SPA: `patito-web`.
- Cliente confidencial interno: `patito-interno`.
- Gateway y servicios validan JWT; el gateway no reemplaza la autorización del servicio.
- `SecurityConfig`, `JwtSecurityConfig` y `JwtRoleConverter` son placeholders: la implementación de cadenas de filtros y reglas por endpoint está pendiente.
- El realm no incluye usuarios de prueba; deben crearse o importarse en un entorno controlado.
- El secreto `TODO_CHANGE_ME` de `patito-interno` debe cambiarse antes de usar el cliente.
- En producción se requiere HTTPS y un issuer accesible desde todos los contenedores.

## 7. Comunicación y contratos

La decisión completa está en [ADR-004](adr-004-comunicacion-sincrona.md). Resumen operativo:

- REST/JSON síncrono con `RestClient` en servicios MVC.
- Contratos públicos en `contracts/openapi/`.
- Contratos internos en `contracts/openapi/internal/`.
- APIs internas fuera del gateway.
- Resilience4j para tolerancia a fallos; timeouts, retries y circuit breakers concretos están pendientes.
- No hay broker de mensajería en el MVP.
- Los contratos OpenAPI son placeholders: deben completarse antes de implementar controllers y clientes.

Convenciones:

- APIs públicas: `/api/v1/...`.
- APIs internas: `/internal/v1/...`.
- Errores: `ProblemDetail` (RFC 7807) mediante `libs/common-web`.
- Paginación: `PageResponse` de `libs/common-web`.
- Correlación: header `X-Correlation-Id`; generación, propagación y MDC están pendientes.
- Geolocalización: se usa OSRM público (`https://router.project-osrm.org`) como valor por defecto, configurable con `GEO_BASE_URL`. Para producción se debe evaluar un proveedor con SLA y API key.

## 8. Gateway

La decisión completa está en [ADR-005](adr-005-gateway-borde.md). Resumen operativo:

- Spring Cloud Gateway reactivo en el puerto 8080.
- No contiene lógica de negocio ni acceso a datos.
- Rutas estáticas configurables por variables de entorno.
- No se usa Eureka ni descubrimiento de servicios en el MVP.
- El tráfico interno entre servicios no pasa por el gateway.
- CORS, access log, manejo de errores y correlación tienen clases base; su implementación está pendiente.
- Cada nueva ruta pública debe registrarse en `application.yml` y documentarse en el OpenAPI correspondiente.

## 9. Infraestructura y despliegue

| Decisión | Detalle |
|---|---|
| Orquestación local | Docker Compose con PostgreSQL, Keycloak y los tres servicios |
| Empaquetado | Un `Dockerfile` por servicio; build multi-stage con Maven y JRE 17 |
| Descubrimiento | Sin Eureka; URLs internas por variables de entorno |
| Mensajería | Sin Kafka/RabbitMQ en el MVP |
| Orquestador | Sin Kubernetes en el MVP |
| Service mesh | Sin malla de servicios en el MVP |
| CI | GitHub Actions con Java 17 y Maven |
| Observabilidad | Actuator, endpoint Prometheus y correlation ID; tracing y dashboards pendientes |

La infraestructura busca que el equipo pueda levantar el sistema con un comando sin introducir plataforma que todavía no se necesita. Si el proyecto crece, las decisiones de discovery, mensajería u orquestación se registran como nuevos ADR.

## 10. Observabilidad

- Actuator expone `health`, `info`, `metrics` y `prometheus` en cada servicio.
- `infra/observability/prometheus.yml` queda como configuración base.
- `X-Correlation-Id` es la convención para seguir una operación entre gateway y servicios.
- `ms-solicitudes` incluye un `logback-spring.xml` base; el patrón con correlación está pendiente.
- No se incorpora trazas distribuidas ni stack de métricas completo en el MVP.
- Cada servicio debe mantener `health` funcionando para el `depends_on` de Compose y para el smoke test futuro.

## 11. Testing

- JUnit 5 y Spring Boot Test como base.
- Testcontainers + PostgreSQL para pruebas de integración representativas.
- WireMock para simular APIs externas en `ms-solicitudes`.
- H2 para pruebas rápidas de unidad/slice, no como sustituto de PostgreSQL.
- Los directorios de test están preparados, pero no hay pruebas implementadas todavía.
- `scripts/smoke-test.ps1` es un placeholder; el smoke test contra el gateway queda pendiente.
- El build debe permanecer verde aunque las pruebas todavía no existan.

## 12. Documentación y artefactos

- `README.md` es el punto de entrada.
- `docs/arquitectura/` contiene ADRs, registro de decisiones y diagramas C4.
- Los diagramas C4 son placeholders y deben completarse con los contenedores y las relaciones reales.
- `contracts/openapi/` es la fuente de verdad de los contratos HTTP.
- **Los PDFs no se versionan.** Se agregó `*.pdf` a `.gitignore` para evitar que el repositorio crezca con material de clase y documentos binarios.
- Los textos extraídos por `tools/pdf-to-txt/` también están ignorados en `tools/pdf-to-txt/salida/`.
- Los PDFs quedan disponibles localmente; si el equipo necesita compartirlos, debe usar un almacenamiento externo o incluirlos en la entrega correspondiente.
- Si un PDF ya estaba trackeado antes de esta regla, `*.pdf` no lo destrackea automáticamente: hay que ejecutar `git rm --cached <archivo>` una vez.

## 13. Reglas de trabajo para el equipo

1. No implementar lógica de negocio en el gateway.
2. No compartir entidades JPA ni tablas entre servicios.
3. No hardcodear URLs, credenciales ni secretos; usar variables de entorno.
4. Completar los contratos OpenAPI antes o junto con los controllers.
5. Mantener los `TODO` acotados y reemplazarlos por implementación o por una decisión documentada.
6. Agregar pruebas junto con cada caso de uso; no dejar el módulo sin cobertura antes de integrarlo.
7. Actualizar el ADR o este registro cuando cambie una decisión estructural.
8. No commitear `target/`, `.pdf`, `.env`, logs ni textos extraídos.
9. Mantener el build completo (`mvn -B verify`) funcionando antes de abrir un PR.
10. Cada servicio es responsable de su base, sus migraciones y su contrato.

## 14. Decisiones pendientes

| Tema | Pendiente |
|---|---|
| Seguridad | Implementar cadenas de filtros, roles por endpoint, method security y propagación de tokens |
| Contratos | Completar OpenAPI público e interno; definir DTOs, errores y paginación |
| Datos | Completar esquemas Flyway y datos iniciales de cada servicio |
| Resiliencia | Definir timeouts, retries, circuit breakers y respuestas de fallback |
| Gateway | Implementar filtros de correlación, access log, CORS y manejo de errores |
| Observabilidad | Agregar correlación al logging y evaluar trazas distribuidas |
| Pruebas | Implementar pruebas unitarias, de integración y smoke tests |
| Diagramas | Completar los C4 de contexto y contenedores |
| CI/CD | Evaluar build de imágenes Docker y despliegue automatizado |
| Evolución | Evaluar eventos/outbox, service discovery y orquestación cuando el MVP lo justifique |
