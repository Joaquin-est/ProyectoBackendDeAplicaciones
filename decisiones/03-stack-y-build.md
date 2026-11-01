# 03 — Stack y build

> **Decisión en una frase:** usar Spring Boot 3.2.5 con Java 17, Maven 3.10 y un parent multi-módulo que compile todo junto sin convertir el sistema en un monolito.

## 1. Stack elegido

| Área | Tecnología | Versión | Por qué |
|---|---|---|---|
| Lenguaje | Java | 17 (`release 17`) | LTS, base de Spring Boot 3, disponible en el aula |
| Framework | Spring Boot | 3.2.5 | Stack del curso; compatible con Spring 6 y Jakarta EE |
| Build | Maven | 3.10 + Wrapper | Estándar del proyecto; wrapper reproducible |
| Cloud | Spring Cloud | 2023.0.1 | Compatible con Boot 3.2.x |
| Gateway | Spring Cloud Gateway | Gestionado por Spring Cloud | Stack reactivo para el borde |
| Persistencia | Spring Data JPA / Hibernate | Gestionado por Boot | Repositorios y mapeo ORM |
| Base integrada | PostgreSQL | 16 | Representa el entorno de Docker/producción |
| Base local | H2 | Gestionado por Boot | Arranque rápido sin infraestructura |
| Migraciones | Flyway | 9.22.x (gestionado por Boot) | Versionado del esquema |
| Seguridad | Keycloak | 25 | OIDC/Authorization Server |
| Seguridad | Spring Security OAuth2 Resource Server | Gestionado por Boot | Validación de JWT |
| Documentación | springdoc OpenAPI | 2.5.0 | Swagger UI y contratos |
| Resiliencia | Resilience4j | 2.2.0 | Timeouts, retry y circuit breaker |
| Testing | JUnit 5, Testcontainers, WireMock | 1.19.8 / 3.5.4 | Pruebas de integración y APIs externas |
| CI | GitHub Actions | — | Build automático con Java 17 |

## 2. ¿Por qué Spring Boot 3.2.5 y Java 17?

Spring Boot 3 exige Java 17 como mínimo y migró a Jakarta EE. Eso implica:

- paquetes `jakarta.*` en lugar de `javax.*`;
- Spring Framework 6;
- `RestClient` disponible de forma nativa;
- mejor integración con observabilidad y contenedores.

Se elige Java 17 porque:

- es la versión mínima soportada por Boot 3;
- es LTS;
- es la versión que se usa en el entorno de la materia;
- permite compilar con un JDK 21 local usando `release 17`, sin obligar a todos a instalar la misma versión exacta.

La versión de Spring Boot no se elige por “ser la última”, sino por ser una base estable y compatible con el material del curso. Cualquier actualización debe probarse en todo el reactor.

## 3. El parent Maven y el reactor

El `pom.xml` raíz:

- tiene packaging `pom`;
- hereda de `spring-boot-starter-parent`;
- declara los módulos;
- centraliza versiones y `dependencyManagement`;
- configura el compilador con `release 17`.

Los módulos son:

```text
libs/common-web
libs/security-jwt
libs/geo-client
libs/test-support
platform/api-gateway
services/ms-clientes
services/ms-tecnicos
services/ms-solicitudes
```

Cuando se ejecuta `mvn verify`, Maven construye un **reactor**: calcula el orden de dependencias y compila cada módulo. Si `ms-solicitudes` depende de `common-web`, Maven compila primero la librería.

### ¿Por qué esto no es un monolito?

Porque el parent no genera un único jar con todo el negocio. Cada servicio:

- tiene su propio `pom.xml`;
- se empaqueta con `spring-boot-maven-plugin`;
- produce un jar ejecutable independiente;
- tiene su propio `Dockerfile`.

El parent es una decisión de **organización del build**, no de **arquitectura de despliegue**.

## 4. Ciclo de vida de Maven

| Fase | Qué hace |
|---|---|
| `validate` | Valida el proyecto |
| `compile` | Compila el código Java |
| `test` | Ejecuta pruebas |
| `package` | Genera el jar |
| `verify` | Ejecuta verificaciones y deja el artefacto listo |
| `install` | Publica el artefacto en el repositorio local |
| `deploy` | Publica en un repositorio remoto |

En un servicio Spring Boot, `package` genera un jar normal y el plugin de Spring Boot lo **repackagea** para que sea ejecutable con `java -jar`. Ese proceso agrega el classpath de dependencias dentro del jar.

## 5. Starters y auto-configuración

Los starters agrupan dependencias y configuraciones típicas:

| Starter | Qué aporta |
|---|---|
| `spring-boot-starter-web` | Spring MVC, Tomcat, Jackson |
| `spring-boot-starter-data-jpa` | JPA, Hibernate, transacciones |
| `spring-boot-starter-validation` | Bean Validation (`@Valid`, `@NotNull`, etc.) |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-oauth2-resource-server` | Validación de JWT |
| `spring-boot-starter-actuator` | Health, metrics, info |
| `spring-cloud-starter-gateway` | Gateway reactivo |
| `springdoc-openapi-starter-webmvc-ui` | Swagger UI para MVC |

Spring Boot aplica **convención sobre configuración**: si detecta una dependencia, configura beans por defecto. Por eso una clase `SecurityConfig` vacía no significa “sin seguridad”: Spring Boot aplica su seguridad por defecto hasta que se defina una cadena explícita.

## 6. Librerías compartidas: una decisión con límites

`common-web`, `security-jwt` y `test-support` son jars planos. No son starters completos con auto-configuración.

Eso significa que:

- una clase anotada con `@Configuration` en una librería no se registra automáticamente solo por agregar la dependencia;
- hay que importarla, escanearla o crear un archivo de auto-configuración;
- `geo-client` sí incluye `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, por lo que es el ejemplo de starter del proyecto.

Esta decisión evita “magia” prematura: las librerías son reutilizables, pero cada servicio decide explícitamente qué configuración importa. Como TODO queda definir la auto-configuración de `common-web` y `security-jwt` o importar sus clases desde cada servicio.

## 7. Wrapper, CI y Docker

### Maven Wrapper

El repositorio incluye:

- `mvnw` para Linux/macOS;
- `mvnw.cmd` para Windows;
- `.mvn/wrapper/maven-wrapper.properties` con Maven 3.10.0.

Uso:

```powershell
.\mvnw.cmd -B verify   # Windows
./mvnw -B verify       # Linux/macOS
```

El wrapper evita depender de la versión de Maven instalada en cada máquina.

### CI

`.github/workflows/ci.yml` usa Java 17 y ejecuta:

```text
mvn -B verify
```

Todavía no construye imágenes Docker ni ejecuta pruebas de integración con servicios levantados.

### Docker

Los `Dockerfile` usan un build multi-stage:

1. imagen con Maven y JDK 17;
2. copia del repositorio;
3. `mvn -pl <módulo> -am package -DskipTests`;
4. imagen final con JRE 17 y el jar.

Esto mantiene las imágenes de ejecución más livianas.

## 8. Perfiles

Los perfiles permiten cambiar configuración sin recompilar:

- **default:** H2, Flyway deshabilitado, sin issuer JWT configurado.
- **docker:** PostgreSQL, Flyway habilitado, issuer de Keycloak.
- **local:** reservado para overrides del desarrollador; todavía es un placeholder.

La decisión completa de datos y perfiles está en [04 — Datos y persistencia](04-datos-y-persistencia.md).

## 9. Conexión con la materia

- **Apunte 15 — Spring Framework:** IoC, inyección de dependencias, beans, `@SpringBootApplication`, starters, auto-configuración y perfiles.
- **Apunte 17 — Elementos transversales:** separación en capas y uso de DTOs; cada módulo replica esa organización.
- **Apunte 14 — Diseño de APIs:** Spring MVC expone los recursos HTTP; springdoc documenta los contratos.
- **Apunte 19 — Gateway:** Spring Cloud Gateway usa un stack reactivo distinto al MVC de los servicios.

## 10. Trade-offs

- **Ventaja:** versiones centralizadas y build reproducible.
- **Ventaja:** un solo comando valida todo el sistema.
- **Costo:** el reactor compila todos los módulos aunque solo cambie uno.
- **Costo:** una versión incompatible en el parent afecta a todos.
- **Riesgo:** las librerías compartidas pueden terminar acopladas si reciben lógica de negocio.
- **Riesgo:** agregar auto-configuración sin tests puede producir efectos difíciles de rastrear.

## 11. Para repasar

- ¿Qué diferencia hay entre un parent Maven y un módulo ejecutable?
- ¿Por qué el reactor no convierte al proyecto en monolito?
- ¿Qué hace `spring-boot:repackage`?
- ¿Qué es un starter? ¿Qué diferencia tiene con una librería plana?
- ¿Por qué una `SecurityConfig` vacía no deja la aplicación sin seguridad?
- ¿Qué aporta el Maven Wrapper?
- ¿Por qué se compila con `release 17` y no directamente con la versión del JDK local?
- ¿Qué etapa del build ejecuta las pruebas?
- ¿Qué falta para que `common-web` y `security-jwt` sean starters completos?

## 12. Enlaces relacionados

- [04 — Datos y persistencia](04-datos-y-persistencia.md)
- [05 — Seguridad](05-seguridad.md)
- [08 — Infraestructura y operación](08-infraestructura-y-operacion.md)
- [ADR-001](../docs/arquitectura/adr-001-monorepo-multimodulo.md)
- [pom.xml](../pom.xml)
- [CI](../.github/workflows/ci.yml)
