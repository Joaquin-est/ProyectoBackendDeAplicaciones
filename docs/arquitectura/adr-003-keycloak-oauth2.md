# ADR-003: Seguridad con Keycloak y OAuth2 Resource Server

- Estado: Aceptado
- Fecha: 2026-10-31
- Implementación: parcial (realm y propiedades creados; filtros de seguridad pendientes)

## Contexto

El sistema tiene tres tipos de actores: clientes, técnicos y administradores. La autenticación no debe implementarse dentro de cada microservicio, y la autorización debe basarse en roles y permisos verificables en el token.

## Decisión

Usar Keycloak 25 como Authorization Server / proveedor OIDC:

- Realm: `patito-realm`.
- Roles de realm: `CLIENTE`, `TECNICO` y `ADMIN`.
- Cliente público `patito-web` para la SPA, con Authorization Code Flow y PKCE.
- Cliente confidencial `patito-interno` para communication service-to-service, con service account.
- El gateway y cada microservicio actúan como OAuth2 Resource Server y validan el JWT contra el issuer de Keycloak.
- La validación en los servicios no se delega únicamente al gateway: se aplica defensa en profundidad.
- El issuer se configura con `KEYCLOAK_ISSUER_URI` en el perfil `docker`.
- En el perfil local por defecto la validación JWT queda deshabilitada/comentada para permitir levantar el esqueleto sin Keycloak. Esto es solo una comodidad de desarrollo y no debe usarse como configuración de producción.
- `JwtRoleConverter`, `JwtSecurityConfig` y los `SecurityConfig` de cada módulo son placeholders; la implementación de las cadenas de filtros, reglas por endpoint y method security queda como TODO.

## Alternativas consideradas

- **Spring Authorization Server:** válido, pero Keycloak ya provee consola, realm, clientes y administración de usuarios sin desarrollo adicional.
- **Autenticación propia con usuario/contraseña:** descartada por riesgo y por acoplar la seguridad a cada servicio.
- **Basic Auth para el MVP:** descartada porque no representa el flujo real de la aplicación ni permite roles y tokens de forma estándar.

## Consecuencias

- Keycloak es una dependencia de infraestructura del entorno Docker.
- Hay que administrar secretos, URLs de issuer, clocks y rotación de claves.
- Los servicios deben propagar el token cuando llaman a otro servicio interno.
- Los roles del token deben convertirse a authorities de Spring Security.
- El realm de ejemplo no incluye usuarios; se deben crear usuarios de prueba o importarlos en un entorno controlado.
- El secreto de `patito-interno` está marcado como `TODO_CHANGE_ME` y debe reemplazarse en cada entorno.
- En producción se debe exigir HTTPS y issuer accesible tanto desde el gateway como desde los servicios.

## Implementación en el repositorio

- `infra/keycloak/realm-patito.json`: realm, roles y clientes de ejemplo.
- `infra/compose/docker-compose.yml`: Keycloak y variables de entorno.
- `platform/api-gateway/src/main/resources/application-docker.yml`: issuer del gateway.
- `services/*/src/main/resources/application-docker.yml`: issuer de cada servicio.
- `libs/security-jwt/`: conversión de roles y configuración base pendiente.
