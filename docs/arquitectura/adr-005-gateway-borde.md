# ADR-005: API Gateway solo en el borde

- Estado: Aceptado
- Fecha: 2026-10-31
- Implementación: parcial (rutas y configuración base; filtros pendientes)

## Contexto

La aplicación necesita un único punto de entrada para la SPA, centralizar CORS, validar tokens y evitar que los clientes conozcan los puertos internos. Al mismo tiempo, el tráfico entre servicios no debe pasar por el gateway para no agregar un salto innecesario.

## Decisión

Usar Spring Cloud Gateway (stack reactivo, WebFlux) como gateway de borde:

- Puerto `8080`.
- Expone únicamente rutas públicas `/api/v1/**`.
- Enruta a URLs internas configurables por variables de entorno; no hay descubrimiento de servicios en el MVP.
- No contiene lógica de negocio ni acceso a bases de datos.
- El gateway valida el JWT y aplica CORS, correlation ID, access log y manejo uniforme de errores.
- Las llamadas entre microservicios van directas por la red interna y no se publican a través del gateway.
- Las rutas actuales son:
  - `ms-clientes`: `/api/v1/clientes/**`, `/api/v1/domicilios/**`.
  - `ms-tecnicos`: `/api/v1/tecnicos/**`, `/api/v1/zonas/**`, `/api/v1/certificaciones/**`, `/api/v1/herramientas/**`.
  - `ms-solicitudes`: `/api/v1/solicitudes/**`, `/api/v1/ofertas/**`, `/api/v1/historial/**`.

## Alternativas consideradas

- **Sin gateway:** descartado porque cada cliente tendría que conocer varios servicios y repetir CORS/seguridad.
- **NGINX como reverse proxy:** válido, pero no integra el modelo Spring Security ni filtros Java del equipo.
- **Eureka + Gateway con discovery:** postergado. Con tres servicios y URLs fijas por ambiente, el descubrimiento agrega complejidad innecesaria.
- **Gateway con lógica de negocio:** descartado para evitar un nuevo monolito distribuido.

## Consecuencias

- El gateway es un punto crítico de disponibilidad y debe escalar/balancearse adecuadamente.
- Cada recurso público nuevo debe agregarse a la configuración de rutas.
- Sin service discovery, los cambios de ubicación se resuelven por variables de entorno.
- La seguridad debe implementarse tanto en el gateway como en los servicios.
- El gateway no debe convertirse en el lugar donde se resuelven reglas de negocio.

## Implementación en el repositorio

- `platform/api-gateway/src/main/resources/application.yml`: rutas y actuator.
- `platform/api-gateway/src/main/resources/application-docker.yml`: issuer JWT.
- `platform/api-gateway/src/main/java/com/patitosrl/gateway/`: aplicación, filtros, CORS, errores y seguridad pendiente.
- `infra/compose/docker-compose.yml`: URLs internas y puertos.
