# ADR-004: Comunicación síncrona con RestClient

- Estado: Aceptado
- Fecha: 2026-10-31
- Implementación: parcial (clientes y contratos son placeholders)

## Contexto

`ms-solicitudes` necesita consultar clientes, técnicos y datos geográficos para crear y asignar solicitudes. El MVP debe ser comprensible para el equipo y no incorporar infraestructura de mensajería antes de tener casos de uso que la justifiquen.

## Decisión

Usar REST/JSON síncrono para la comunicación entre microservicios:

- Los servicios MVC usan `RestClient` de Spring 6.
- Los contratos se documentan en OpenAPI dentro de `contracts/openapi/`.
- Las APIs internas se publican bajo `/internal/v1/**` y no se exponen a través del gateway.
- Las URLs base se configuran por ambiente (`MS_CLIENTES_URL`, `MS_TECNICOS_URL`, `GEO_BASE_URL`).
- `ms-solicitudes` incorpora Resilience4j para timeouts, reintentos y circuit breaker; las anotaciones y políticas concretas quedan como TODO.
- No se incorpora Kafka, RabbitMQ ni otro broker en el MVP.

## Alternativas consideradas

- **WebClient:** buena opción para reactivo, pero los servicios de dominio son MVC y `RestClient` mantiene el modelo simple.
- **OpenFeign:** agrega una capa de abstracción que no es necesaria para el tamaño actual del proyecto.
- **Mensajería asíncrona desde el inicio:** postergada porque requiere eventos, outbox, idempotencia y operación adicional antes de tener un caso de uso claro.

## Consecuencias

- Una caída de un servicio puede afectar una operación que dependa de él; se deben configurar timeouts y fallbacks.
- No hay consistencia fuerte entre contextos; las operaciones distribuidas deben diseñarse como sagas o compensaciones cuando corresponda.
- Los contratos internos deben versionarse y coordinarse entre equipos.
- La latencia de una solicitud puede incluir varias llamadas de red.
- La incorporación futura de eventos requerirá definir outbox, idempotencia y consumidores.

## Implementación en el repositorio

- `services/ms-solicitudes/src/main/java/com/patitosrl/solicitudes/client/`: placeholders de clientes.
- `services/ms-solicitudes/src/main/java/com/patitosrl/solicitudes/config/RestClientConfig.java`: configuración pendiente.
- `contracts/openapi/internal/`: contratos de APIs internas.
- `services/ms-solicitudes/src/main/resources/application.yml`: URLs base y límites de ofertas.
- `libs/geo-client/`: integración con rutas/mapas.
