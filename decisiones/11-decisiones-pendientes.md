# 11 — Decisiones pendientes

> **Idea central:** el esqueleto ya tomó las decisiones estructurales; lo que queda es implementar, validar y ajustar. Este documento ordena ese trabajo.

## 1. Principio

No todo lo que falta es una decisión pendiente. Hay que distinguir:

- **Implementación pendiente:** la decisión ya está tomada, solo falta escribir código (por ejemplo, completar un controller).
- **Decisión pendiente:** todavía hay que elegir entre alternativas (por ejemplo, cómo propagar tokens entre servicios).
- **Mejora futura:** no es necesaria para el MVP (por ejemplo, Kubernetes).

Esta distinción evita bloquear al equipo con discusiones que no corresponden a la etapa actual.

## 2. Tabla de pendientes

| Prioridad | Tema | Tipo | Qué falta | Depende de |
|---|---|---|---|---|
| Alta | Seguridad | Implementación + decisión | Cadenas de filtros, reglas por endpoint, converter de roles, propagación de tokens | Keycloak, contratos |
| Alta | Contratos OpenAPI | Implementación | Completar paths, DTOs, errores y ejemplos | Reglas de negocio |
| Alta | Migraciones Flyway | Implementación | Esquemas reales de las tres bases | Modelo de datos |
| Alta | Lógica de solicitudes | Implementación | Máquina de estados, ofertas, expiración, presupuesto, historial | Contratos, datos |
| Alta | Endpoints internos | Implementación | APIs de clientes y técnicos para solicitudes | Contratos |
| Media | Resiliencia | Decisión + implementación | Timeouts, retry, circuit breaker, fallback, 503 | Clientes internos |
| Media | Gateway | Implementación | CORS, correlación, access log, errores, límites | Seguridad |
| Media | Pruebas | Implementación | Unitarias, integración, seguridad, contrato, smoke | Lógica y contratos |
| Media | Observabilidad | Implementación | Correlación en logs, métricas, dashboards básicos | Servicios |
| Baja | Diagramas C4 | Implementación | Contexto y contenedores reales | Arquitectura estable |
| Baja | CI/CD | Mejora | Build de imágenes, smoke en CI, despliegue | Docker |
| Baja | Eventos/outbox | Decisión futura | Evaluar Kafka/RabbitMQ si aparece un caso de uso | MVP funcionando |
| Baja | Discovery | Decisión futura | Evaluar Eureka/Kubernetes si cambian las URLs | Escalado |
| Baja | Notificaciones | Decisión futura | Correo/push real o simulación persistida | Flujo central |

## 3. Orden de trabajo recomendado

### Etapa 1 — Contratos y datos

1. Definir el modelo de cada contexto.
2. Completar los OpenAPI públicos e internos.
3. Escribir las migraciones Flyway.
4. Definir DTOs de request/response.
5. Acordar códigos de error.

**Por qué primero:** sin contrato ni esquema, el resto se construye sobre arena.

### Etapa 2 — Seguridad

1. Implementar `JwtRoleConverter`.
2. Implementar `SecurityFilterChain` en servicios y `SecurityWebFilterChain` en gateway.
3. Definir endpoints públicos y protegidos.
4. Implementar propagación de token o client credentials.
5. Probar 401/403 y roles.

**Por qué temprano:** si se implementa después, hay que reescribir controllers y clientes.

### Etapa 3 — APIs internas y clientes

1. Implementar endpoints internos de clientes y técnicos.
2. Implementar `ClientesApiClient`, `TecnicosApiClient` y `GeoApiClient`.
3. Configurar `RestClient` con base URL y timeouts.
4. Manejar errores con `ProblemDetail`.

### Etapa 4 — Flujo central

1. Crear solicitud y snapshot.
2. Buscar técnicos elegibles.
3. Crear oferta con expiración.
4. Implementar aceptación/rechazo.
5. Implementar scheduler de expiración.
6. Implementar presupuesto y estados.
7. Registrar historial.

### Etapa 5 — Calidad y operación

1. Pruebas unitarias de reglas.
2. Pruebas de integración con Testcontainers.
3. Pruebas de seguridad.
4. Smoke test con Compose.
5. Correlación en logs.
6. Diagramas C4.
7. CI con Docker.

## 4. Decisiones que todavía hay que tomar

### Seguridad

- ¿Se propaga el token del usuario o se usa client credentials?
- ¿Qué endpoints son públicos?
- ¿Se usan roles, scopes o ambos?
- ¿Dónde se aplica method security?
- ¿Cómo se manejan tokens expirados en llamadas internas?

### Datos

- ¿Los IDs son `Long`, `UUID` u otro tipo?
- ¿Qué campos son obligatorios?
- ¿Qué estrategia de bloqueo se usa para la aceptación concurrente?
- ¿Cómo se versionan los snapshots?
- ¿Qué datos se auditan?

### API

- ¿Se usa un envelope común o respuestas directas?
- ¿Qué formato exacto tiene `ProblemDetail`?
- ¿Cómo se paginan los listados?
- ¿Qué versión de API se usa para cambios incompatibles?

### Resiliencia

- ¿Qué timeout tiene cada cliente?
- ¿Qué errores se reintentan?
- ¿Cuándo se abre el circuit breaker?
- ¿Qué fallback se devuelve?

### Operación

- ¿Se agrega OpenTelemetry?
- ¿Se construyen imágenes en CI?
- ¿Se usa Kubernetes?
- ¿Se incorpora un broker?

Cada una de estas decisiones debería terminar en un ADR.

## 5. Riesgos principales

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Seguridad vacía | Endpoints accesibles sin control | Implementar cadenas antes de exponer |
| Contratos incompletos | Integración tardía y retrabajo | OpenAPI primero |
| H2 vs PostgreSQL | Errores que aparecen solo en Docker | Testcontainers |
| Sin pruebas | Regresiones silenciosas | Tests por capa |
| Timeouts ausentes | Fallos en cascada | Resilience4j |
| Gateway con lógica | Monolito en el borde | Revisar límites |
| Datos compartidos | Acoplamiento entre servicios | Database per Service |
| Secretos en Git | Incidente de seguridad | `.env` ignorado y revisión |

## 6. Definición de “esqueleto terminado”

El esqueleto está terminado cuando:

- todos los módulos compilan;
- el wrapper funciona;
- Docker Compose está definido;
- los perfiles están separados;
- los contratos existen como estructura;
- las decisiones están documentadas;
- no hay lógica de negocio falsa ni datos inventados;
- cada `TODO` es claro;
- el equipo sabe qué sigue.

Eso ya se cumple. Lo que sigue es implementar el MVP.

## 7. Cómo evitar convertir el sistema en un monolito distribuido

- Separar por negocio, no por entidad.
- No compartir bases ni entidades.
- No poner reglas en el gateway.
- No hacer llamadas en cadena sin necesidad.
- Mantener contratos explícitos.
- Documentar cada dependencia entre servicios.
- Revisar si un servicio realmente necesita ser independiente.

## 8. Conexión con la materia

- **Apunte 17 — Capas:** la implementación debe respetar controller/service/repository.
- **Apunte 18 — API Client:** los clientes y la resiliencia son la parte más delicada de la integración.
- **Apunte 19 — Gateway:** el borde debe quedar delgado.
- **Apunte 20 y 21 — Seguridad:** la seguridad es una decisión transversal, no un agregado final.
- **Apunte 14 y 17:** contratos y documentación.

## 9. Para repasar

- ¿Qué diferencia hay entre implementación pendiente, decisión pendiente y mejora futura?
- ¿Por qué los contratos y las migraciones van primero?
- ¿Por qué la seguridad no debería dejarse para el final?
- ¿Qué decisión hay que tomar para las llamadas internas?
- ¿Qué riesgo tiene H2 como única base de prueba?
- ¿Qué señales indican que el sistema se está volviendo un monolito distribuido?
- ¿Qué significa que el gateway debe quedar “delgado”?
- ¿Cuál sería el próximo paso concreto del equipo?

## 10. Enlaces relacionados

- [01 — Contexto y alcance](01-contexto-y-alcance.md)
- [04 — Datos y persistencia](04-datos-y-persistencia.md)
- [05 — Seguridad](05-seguridad.md)
- [06 — Comunicación entre servicios](06-comunicacion-entre-servicios.md)
- [09 — Testing y calidad](09-testing-y-calidad.md)
- [Mapa de decisiones](mapa-decisiones.md)
