# 06 — Comunicación entre servicios

> **Decisión en una frase:** los microservicios se comunican con REST/JSON síncrono usando `RestClient`; los contratos se documentan con OpenAPI y las fallas se manejan con timeouts, `ProblemDetail` y Resilience4j.

## 1. El problema

`ms-solicitudes` no puede operar solo:

- necesita validar que el cliente existe;
- necesita el domicilio y las coordenadas;
- necesita saber qué técnicos están disponibles, cubren la zona y tienen las certificaciones;
- necesita calcular una ruta y un costo de visita.

Esos datos pertenecen a otros contextos. La pregunta es **cómo** obtenerlos sin compartir la base.

## 2. Decisión: REST/JSON síncrono

Se elige REST sobre HTTP con JSON porque:

- es el estilo que se trabaja en la materia;
- es simple de entender y depurar;
- Spring MVC y `RestClient` lo soportan de forma nativa;
- los contratos se pueden documentar con OpenAPI;
- no requiere broker ni infraestructura adicional.

```text
ms-solicitudes ── GET /internal/v1/clientes/{id} ──▶ ms-clientes
ms-solicitudes ── GET /internal/v1/tecnicos/elegibles ──▶ ms-tecnicos
ms-solicitudes ── GET /rutas ──▶ API externa de mapas
```

Las llamadas internas van por la red de Docker o por localhost en desarrollo. No pasan por el gateway.

## 3. ¿Por qué `RestClient` y no `WebClient`?

`RestClient` es la API fluida y bloqueante de Spring 6 para clientes HTTP. Se eligió porque:

- los servicios de dominio son Spring MVC (bloqueantes);
- el modelo mental es más simple que el reactivo;
- permite `baseUrl`, headers, interceptores, conversión de JSON y manejo de errores;
- es el recomendado para aplicaciones MVC;
- el apunte 18 lo presenta como opción moderna frente a `RestTemplate`.

`WebClient` se justifica cuando todo el flujo es reactivo o cuando se necesita streaming/backpressure. El gateway sí usa WebFlux, pero los servicios de negocio no.

## 4. ¿Por qué no OpenFeign?

OpenFeign agrega una capa declarativa que puede ser cómoda, pero:

- oculta parte del manejo de errores;
- requiere configuración adicional;
- no aporta una ventaja decisiva para tres o cuatro clientes;
- el equipo ya trabaja con `RestClient` en la materia.

Para el tamaño del MVP, `RestClient` explícito es más fácil de leer y probar.

## 5. Contratos OpenAPI

Los contratos están en:

```text
contracts/openapi/
  clientes-v1.yaml
  tecnicos-v1.yaml
  solicitudes-v1.yaml
  internal/
    clientes-internal-v1.yaml
    tecnicos-internal-v1.yaml
```

Actualmente son placeholders con `paths: {}`. La decisión es que **el contrato se complete antes o junto con la implementación**, no después.

Un contrato debe definir:

- rutas y métodos;
- parámetros de path y query;
- request body;
- responses y códigos HTTP;
- esquemas de error;
- ejemplos;
- versión de la API.

### APIs públicas e internas

| Tipo | Prefijo | Quién la consume | ¿Pasa por el gateway? |
|---|---|---|---|
| Pública | `/api/v1/**` | SPA, clientes externos | Sí |
| Interna | `/internal/v1/**` | Otros microservicios | No |

Separar las APIs internas reduce la superficie expuesta y permite cambiar detalles sin afectar a los clientes.

## 6. DTOs, validación y errores

### DTOs

No se exponen entidades JPA como contrato HTTP. Se usan DTOs de request y response para:

- ocultar campos internos;
- validar formatos;
- evitar acoplar la API al modelo de datos;
- permitir cambios de esquema sin romper el contrato;
- enriquecer respuestas.

### Validación

Los DTOs de entrada usan Bean Validation:

- `@NotBlank`;
- `@NotNull`;
- `@Size`;
- `@Pattern`;
- validaciones personalizadas.

El controller recibe `@Valid` y el error se transforma en una respuesta uniforme.

### ProblemDetail

El proyecto usa `ProblemDetail` (RFC 7807) para errores. Un ejemplo conceptual:

```json
{
  "type": "about:blank",
  "title": "Cliente no encontrado",
  "status": 404,
  "detail": "No existe un cliente con id 42",
  "instance": "/api/v1/clientes/42"
}
```

Ventajas:

- formato estándar;
- fácil de consumir por la SPA;
- incluye status y detalle;
- Spring 6 lo soporta de forma nativa;
- permite agregar extensiones como `correlationId`.

`libs/common-web` contiene `ProblemDetailFactory` y `GlobalExceptionHandler` como placeholders.

## 7. Paginación y filtros

Para listados se usa paginación:

- `page`;
- `size`;
- `sort`;
- respuesta con metadatos (`PageResponse`).

Esto evita respuestas gigantes y permite que el frontend navegue. La implementación concreta es TODO.

## 8. Resiliencia: timeouts, retries y circuit breaker

Una llamada síncrona puede fallar por:

- timeout;
- error 4xx;
- error 5xx;
- servicio caído;
- respuesta inválida.

La decisión es no dejar que una falla se propague sin control. Se incorpora Resilience4j en `ms-solicitudes` para:

- **timeout:** no esperar indefinidamente;
- **retry:** reintentar solo errores transitorios y con límite;
- **circuit breaker:** dejar de llamar a un servicio que está fallando;
- **fallback:** devolver una respuesta controlada cuando sea posible.

### Caso crítico: crear una solicitud

Si `ms-clientes` no responde al crear la solicitud, `ms-solicitudes` no debe crear una solicitud huérfana. Debe:

1. fallar la operación;
2. devolver `503 Service Unavailable`;
3. incluir un `ProblemDetail` claro;
4. permitir que el cliente reintente.

La configuración concreta de Resilience4j es TODO.

## 9. Propagación de seguridad

Si las APIs internas están protegidas, `ms-solicitudes` debe enviar un token al llamar a `ms-clientes` o `ms-tecnicos`.

Opciones:

- propagar el token del usuario;
- obtener un token de servicio con client credentials (`patito-interno`);
- usar una combinación según el caso.

El esqueleto todavía no implementa esta propagación. Es una decisión pendiente que debe resolverse junto con la seguridad.

## 10. ¿Por qué no Kafka en el MVP?

Kafka u otro broker serían útiles para:

- eventos de dominio;
- notificaciones;
- desacoplar picos de carga;
- outbox pattern;
- consumidores independientes.

Pero también agregan:

- infraestructura;
- serialización y esquemas;
- idempotencia;
- manejo de reintentos y dead letters;
- consistencia eventual;
- observabilidad distribuida.

El MVP no tiene un caso de uso que justifique ese costo. Se posterga. Si se incorpora, debe documentarse con un ADR y evaluarse el patrón outbox.

## 11. Geolocalización y cálculo de rutas

`libs/geo-client` encapsula la integración con mapas/rutas. La configuración usa:

```yaml
app:
  geo:
    base-url: ${GEO_BASE_URL:https://router.project-osrm.org}
```

La idea del dominio es:

1. calcular cercanía local con Haversine;
2. filtrar técnicos dentro de 13 km;
3. llamar a la API externa solo para el técnico elegido;
4. obtener ruta, tiempo y costo.

Esto reduce llamadas a la API externa y protege el rate limit. La implementación es TODO.

## 12. Conexión con la materia

- **Apunte 14 — Diseño de APIs:** REST, recursos, URIs, verbos, códigos HTTP, idempotencia y OpenAPI.
- **Apunte 17 — Transversales:** DTOs, validación, `ResponseEntity`, `@RestControllerAdvice`, Swagger.
- **Apunte 18 — API Client:** `RestClient`, `baseUrl`, manejo de errores, timeouts, reintentos, circuit breaker, ProblemDetail y pruebas con WireMock.
- **Apunte 19 — Gateway:** las APIs internas no se publican; el gateway solo expone el borde.

## 13. Trade-offs

- **Ventaja:** simple, observable y compatible con el stack.
- **Ventaja:** contratos explícitos y testeables.
- **Costo:** acoplamiento temporal: si un servicio está caído, la operación puede fallar.
- **Costo:** latencia acumulada por llamadas en cadena.
- **Costo:** hay que manejar idempotencia y reintentos.
- **Riesgo:** reintentar un POST sin idempotencia puede duplicar datos.
- **Riesgo:** un timeout mal configurado puede degradar todo el flujo.

## 14. Para repasar

- ¿Por qué se eligió REST síncrono?
- ¿Cuándo conviene `RestClient` y cuándo `WebClient`?
- ¿Qué diferencia hay entre API pública e interna?
- ¿Por qué no se exponen entidades JPA?
- ¿Qué es `ProblemDetail`?
- ¿Qué es un timeout, un retry y un circuit breaker?
- ¿Qué debería hacer `ms-solicitudes` si `ms-clientes` no responde al crear una solicitud?
- ¿Por qué no se usa Kafka todavía?
- ¿Cómo se protege una llamada interna?
- ¿Por qué se calcula Haversine antes de llamar a la API de mapas?
- ¿Qué riesgos tiene reintentar una operación no idempotente?

## 15. Enlaces relacionados

- [ADR-004](../docs/arquitectura/adr-004-comunicacion-sincrona.md)
- [02 — Arquitectura y bounded contexts](02-arquitectura-y-bounded-contexts.md)
- [05 — Seguridad](05-seguridad.md)
- [07 — Gateway](07-gateway.md)
- [Contratos OpenAPI](../contracts/openapi/)
- [Cliente de clientes](../services/ms-solicitudes/src/main/java/com/patitosrl/solicitudes/client/ClientesApiClient.java)
- [Cliente de técnicos](../services/ms-solicitudes/src/main/java/com/patitosrl/solicitudes/client/TecnicosApiClient.java)
