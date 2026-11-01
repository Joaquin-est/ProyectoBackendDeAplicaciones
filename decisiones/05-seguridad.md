# 05 — Seguridad

> **Decisión en una frase:** delegar la identidad en Keycloak, emitir JWT con roles y validar esos tokens tanto en el gateway como en cada microservicio.

## 1. Autenticación y autorización

Antes de entrar en las tecnologías, conviene separar dos conceptos:

- **Autenticación:** demostrar quién sos. Por ejemplo, obtener un token con tu identidad.
- **Autorización:** decidir qué podés hacer. Por ejemplo, un `CLIENTE` puede crear solicitudes, pero no administrar técnicos.

El sistema tiene tres actores:

| Rol | Uso esperado |
|---|---|
| `CLIENTE` | Crear solicitudes, consultar las propias, aceptar/rechazar presupuestos |
| `TECNICO` | Ver ofertas, aceptar/rechazar, cargar diagnóstico y presupuesto, cambiar estados |
| `ADMIN` | Administrar clientes, técnicos, certificaciones, zonas y herramientas |

Los permisos finos por endpoint todavía son un TODO. La estructura de roles ya está definida.

## 2. OAuth2 y OIDC en una explicación corta

- **OAuth2** es un marco de autorización delegada: permite que una aplicación acceda a recursos en nombre de un usuario sin manejar su contraseña.
- **OIDC** (OpenID Connect) es una capa de identidad sobre OAuth2. Agrega el **ID Token** y la posibilidad de conocer la identidad del usuario.
- **Keycloak** actúa como **Authorization Server** y **OpenID Provider**.
- El backend actúa como **Resource Server**: protege recursos y valida el token.
- La SPA actúa como **Client**.
- El usuario es el **Resource Owner**.

```text
Usuario → SPA → Keycloak (login)
                    │
                    ▼
                 JWT
                    │
SPA ── Bearer JWT ─▶ Gateway ──▶ Microservicio
                               (valida JWT y roles)
```

## 3. JWT: qué se valida y por qué

Un JWT tiene tres partes:

1. **Header:** algoritmo y tipo.
2. **Payload:** claims, como `sub`, `iss`, `exp`, `realm_access.roles`.
3. **Signature:** firma del emisor.

El Resource Server valida:

- que el token esté firmado por el emisor correcto;
- que el `issuer` coincida con el configurado;
- que no esté vencido (`exp`);
- que la firma sea válida usando las claves públicas del emisor (JWKS);
- que la identidad y los roles sean utilizables por Spring Security.

El `issuer-uri` le dice a Spring Security dónde obtener la configuración del emisor y las claves públicas. No alcanza con “leer el token”: hay que verificar su firma y vigencia.

## 4. Keycloak: realm, roles y clientes

El archivo `infra/keycloak/realm-patito.json` define:

- realm `patito-realm`;
- roles `CLIENTE`, `TECNICO`, `ADMIN`;
- cliente público `patito-web` para la SPA;
- cliente confidencial `patito-interno` para llamadas internas.

### `patito-web`

- público, porque una SPA no puede guardar un secreto de forma segura;
- tiene habilitado Authorization Code Flow;
- se recomienda PKCE para una SPA; el realm todavía no fija `pkce.code.challenge.method`, así que debe revisarse antes de usarlo en un entorno real;
- tiene redirect URIs de desarrollo (`localhost:3000`, `localhost:5173`).

### `patito-interno`

- confidencial;
- service account habilitada;
- pensado para que un servicio obtenga un token de servicio;
- el secreto está como `TODO_CHANGE_ME`: debe cambiarse en cada entorno.

### Usuarios

El realm no incluye usuarios. Hay que crearlos o importarlos para probar. En producción, la gestión de usuarios debe tener un proceso definido.

## 5. Validación en gateway y en servicios

La decisión es **defensa en profundidad**:

- el gateway valida el token antes de rutear;
- cada microservicio también valida el token y aplica sus reglas de autorización.

¿Por qué no validar solo en el gateway?

- un servicio podría ser alcanzado por error desde la red interna;
- la seguridad de un servicio no debe depender de que otro esté bien configurado;
- los permisos finos pertenecen al servicio dueño del recurso;
- el gateway no debe conocer reglas de negocio.

¿Por qué no validar solo en los servicios?

- el gateway es la única entrada pública y debe rechazar temprano;
- centraliza CORS, logs y errores de autenticación;
- evita que tráfico no autenticado llegue a los servicios.

Ambas capas son necesarias, pero con responsabilidades distintas.

## 6. Configuración en el repositorio

### Docker

Gateway y servicios tienen:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://keycloak:8080/realms/patito-realm}
```

### Perfil local por defecto

El issuer está comentado para que el esqueleto arranque sin Keycloak:

```yaml
# TODO: habilitar Resource Server cuando Keycloak esté disponible.
# security:
#   oauth2:
#     resourceserver:
#       jwt:
#         issuer-uri: http://localhost:8081/realms/patito-realm
```

Esto es una comodidad de desarrollo, **no una configuración de producción**. Si el issuer no está configurado, no hay validación JWT real.

### Clases placeholder

| Clase | Rol esperado | Estado |
|---|---|---|
| `JwtSecurityConfig` | Configuración base de Resource Server y method security | Vacía |
| `JwtRoleConverter` | Convertir `realm_access.roles` en authorities | Vacía |
| `SecurityConfig` de cada servicio | `SecurityFilterChain`, reglas por endpoint | Vacía |
| `SecurityConfig` del gateway | `SecurityWebFilterChain` reactiva | Vacía |

Mientras estén vacías, Spring Boot aplica su seguridad por defecto. La cadena explícita, las reglas y la conversión de roles son TODO.

## 7. Roles de Keycloak y authorities de Spring

Keycloak suele enviar los roles de realm dentro de:

```json
{
  "realm_access": {
    "roles": ["CLIENTE", "TECNICO"]
  }
}
```

Spring Security no interpreta ese claim automáticamente como authorities. Para usar `hasRole("TECNICO")` o `@PreAuthorize("hasRole('ADMIN')")` hay que convertirlo.

El converter debe:

1. leer `realm_access.roles`;
2. transformar cada rol en una authority;
3. decidir la convención (`ROLE_` o no);
4. devolver un `JwtAuthenticationToken` con las authorities.

La clase `JwtRoleConverter` existe para eso, pero está vacía.

## 8. `SecurityFilterChain`, CORS, CSRF y method security

En una API stateless con JWT:

- se deshabilita CSRF, porque no hay sesión con cookies;
- se configura `sessionManagement` como `STATELESS`;
- se definen reglas con `authorizeHttpRequests`;
- se habilita `oauth2ResourceServer().jwt(...)`;
- se configura CORS si el navegador consume la API;
- se protege con roles y, si hace falta, con `@PreAuthorize`.

El gateway, al ser reactivo, usa `SecurityWebFilterChain` y `ServerHttpSecurity`, no la variante servlet.

## 9. Propagación de tokens entre servicios

Cuando `ms-solicitudes` llama a `ms-clientes`, el servicio interno también puede exigir un token. Hay dos escenarios:

1. **Propagar el token del usuario:** útil si la operación debe conservar la identidad del usuario.
2. **Emitir un token de servicio:** útil para llamadas técnicas con client credentials.

El documento del proyecto menciona que el gateway propaga el token; en el esqueleto, la propagación y la configuración del cliente OAuth2 son TODO. Es una decisión pendiente que debe resolverse antes de proteger las APIs internas.

## 10. ¿Por qué Keycloak y no autenticación propia?

- evita almacenar contraseñas en los servicios;
- provee login, tokens, roles y administración;
- es estándar OIDC;
- permite que varios servicios confíen en el mismo emisor;
- el material de la materia lo usa;
- separa identidad de dominio.

Alternativas descartadas:

- **Basic Auth:** simple, pero no representa el flujo real ni los roles.
- **JWT firmado a mano:** riesgo criptográfico y mantenimiento.
- **Spring Authorization Server:** válido, pero Keycloak ya ofrece la consola y el realm.

## 11. Riesgos y pendientes

- Implementar las cadenas de seguridad.
- Implementar el converter de roles.
- Definir qué endpoints son públicos, cuáles requieren autenticación y cuáles roles.
- Propagar tokens o configurar client credentials.
- Cambiar el secreto de `patito-interno`.
- Crear usuarios de prueba.
- Usar HTTPS en producción.
- Validar CORS y CSRF correctamente.
- Probar con `spring-security-test` y tokens simulados.
- Decidir si se usan scopes además de roles.

## 12. Conexión con la materia

- **Apunte 20 — Seguridad:** autenticación, autorización, OAuth2, OIDC, JWT, Resource Owner, Client, Resource Server y Authorization Server.
- **Apunte 21 — Spring Security:** configuración del Resource Server, `SecurityFilterChain`, `issuer-uri`, conversión de roles de Keycloak y uso de `@AuthenticationPrincipal`.

## 13. Para repasar

- ¿Qué diferencia hay entre autenticación y autorización?
- ¿Qué diferencia hay entre OAuth2 y OIDC?
- ¿Qué es un Resource Server?
- ¿Qué valida Spring Security de un JWT?
- ¿Por qué no alcanza con leer el payload del token?
- ¿Por qué se valida el token en el gateway y también en los servicios?
- ¿Qué es `issuer-uri`?
- ¿Por qué los roles de Keycloak necesitan un converter?
- ¿Cuándo se usa `SecurityFilterChain` y cuándo `SecurityWebFilterChain`?
- ¿Por qué se deshabilita CSRF en una API stateless?
- ¿Qué falta para que la seguridad del esqueleto sea real?

## 14. Enlaces relacionados

- [ADR-003](../docs/arquitectura/adr-003-keycloak-oauth2.md)
- [Realm de Keycloak](../infra/keycloak/realm-patito.json)
- [Configuración Docker del gateway](../platform/api-gateway/src/main/resources/application-docker.yml)
- [Configuración Docker de clientes](../services/ms-clientes/src/main/resources/application-docker.yml)
- [Librería security-jwt](../libs/security-jwt/)
