# Glosario

Términos que aparecen en las decisiones y en la materia. La columna “Dónde verlo” indica el documento o archivo del repositorio que ayuda a contextualizarlo.

## A — C

| Término | Definición | Dónde verlo |
|---|---|---|
| **ADR** | Architecture Decision Record. Documento corto que registra contexto, decisión, alternativas y consecuencias de una decisión de arquitectura. | [docs/arquitectura](../docs/arquitectura/README.md) |
| **API** | Interfaz que define cómo dos aplicaciones se comunican: operaciones, formatos, errores y reglas. | [06](06-comunicacion-entre-servicios.md) |
| **Auto-configuración** | Mecanismo de Spring Boot que configura beans automáticamente según las dependencias y propiedades detectadas. | [03](03-stack-y-build.md) |
| **Bean** | Objeto administrado por el contenedor de Spring. | [03](03-stack-y-build.md) |
| **Bounded context** | Límite explícito alrededor de un modelo de dominio y su lenguaje. En este proyecto: clientes, técnicos y solicitudes. | [02](02-arquitectura-y-bounded-contexts.md) |
| **CI/CD** | Integración continua / entrega continua. Automatiza build, pruebas y despliegue. | [08](08-infraestructura-y-operacion.md) |
| **Circuit breaker** | Patrón de resiliencia que deja de llamar a un servicio que está fallando para evitar fallos en cascada. | [06](06-comunicacion-entre-servicios.md) |
| **Claim** | Dato dentro del payload de un JWT, por ejemplo `sub`, `iss`, `exp` o `realm_access`. | [05](05-seguridad.md) |
| **CORS** | Cross-Origin Resource Sharing. Mecanismo del navegador para permitir llamadas entre orígenes distintos. | [07](07-gateway.md) |
| **Correlation ID** | Identificador que se propaga entre servicios para reconstruir una operación en los logs. | [07](07-gateway.md) |

## D — G

| Término | Definición | Dónde verlo |
|---|---|---|
| **Database per Service** | Patrón por el cual cada microservicio es dueño de su propia base o esquema. | [04](04-datos-y-persistencia.md) |
| **DDL** | Data Definition Language. Sentencias que crean o modifican estructuras de base de datos. | [04](04-datos-y-persistencia.md) |
| **Docker Compose** | Herramienta para definir y levantar varios contenedores con un archivo YAML. | [08](08-infraestructura-y-operacion.md) |
| **DTO** | Data Transfer Object. Objeto usado para transportar datos entre capas o por la API, separado de la entidad JPA. | [06](06-comunicacion-entre-servicios.md) |
| **Entidad** | Clase JPA que representa una tabla o registro persistente. | [04](04-datos-y-persistencia.md) |
| **Endpoint** | Punto de acceso de una API: método HTTP + ruta. | [06](06-comunicacion-entre-servicios.md) |
| **Flyway** | Herramienta de migraciones versionadas de base de datos. | [04](04-datos-y-persistencia.md) |
| **Gateway** | Punto único de entrada que rutea, autentica y aplica políticas de borde. | [07](07-gateway.md) |
| **H2** | Base de datos relacional en memoria o archivo, usada para desarrollo y pruebas rápidas. | [04](04-datos-y-persistencia.md) |

## H — M

| Término | Definición | Dónde verlo |
|---|---|---|
| **HTTP** | Protocolo de comunicación usado por las APIs REST. | [06](06-comunicacion-entre-servicios.md) |
| **Idempotencia** | Propiedad por la cual repetir una operación produce el mismo efecto que ejecutarla una vez. | [06](06-comunicacion-entre-servicios.md) |
| **IoC / DI** | Inversión de Control / Inyección de Dependencias. Spring crea y conecta los objetos. | [03](03-stack-y-build.md) |
| **JPA** | Java Persistence API. Estándar de mapeo objeto-relacional usado por Hibernate. | [04](04-datos-y-persistencia.md) |
| **JSON** | Formato de intercambio de datos usado por las APIs REST. | [06](06-comunicacion-entre-servicios.md) |
| **JWT** | JSON Web Token. Token firmado con claims de identidad, roles y vigencia. | [05](05-seguridad.md) |
| **Keycloak** | Servidor de identidad OIDC/OAuth2 usado como Authorization Server. | [05](05-seguridad.md) |
| **Maven** | Herramienta de build y gestión de dependencias de Java. | [03](03-stack-y-build.md) |
| **Microservicio** | Componente desplegable de forma independiente, con responsabilidad de negocio acotada y datos propios. | [02](02-arquitectura-y-bounded-contexts.md) |
| **Migración** | Script versionado que cambia el esquema de la base de datos. | [04](04-datos-y-persistencia.md) |
| **Monolito** | Aplicación única que concentra todo el código y, normalmente, una única base. | [02](02-arquitectura-y-bounded-contexts.md) |
| **Monolito distribuido** | Varios servicios desplegados por separado, pero tan acoplados que funcionan como un solo sistema. | [02](02-arquitectura-y-bounded-contexts.md) |
| **Monorepo** | Un único repositorio que contiene varios módulos o servicios. | [03](03-stack-y-build.md) |
| **MVP** | Minimum Viable Product. Versión mínima del producto que entrega el flujo central. | [01](01-contexto-y-alcance.md) |

## N — R

| Término | Definición | Dónde verlo |
|---|---|---|
| **OAuth2** | Marco de autorización delegada. | [05](05-seguridad.md) |
| **OIDC** | OpenID Connect. Capa de identidad sobre OAuth2. | [05](05-seguridad.md) |
| **OpenAPI** | Especificación para describir APIs REST de forma legible por humanos y herramientas. | [06](06-comunicacion-entre-servicios.md) |
| **Parent Maven** | POM que centraliza configuración y versiones para varios módulos. | [03](03-stack-y-build.md) |
| **Perfil** | Configuración alternativa de Spring activada por una propiedad, por ejemplo `docker` o `local`. | [08](08-infraestructura-y-operacion.md) |
| **PostgreSQL** | Base de datos relacional usada en Docker y producción. | [04](04-datos-y-persistencia.md) |
| **Predicado** | Condición que decide si una ruta del gateway se activa, por ejemplo `Path=/api/v1/**`. | [07](07-gateway.md) |
| **ProblemDetail** | Formato estándar de error HTTP definido por RFC 7807. | [06](06-comunicacion-entre-servicios.md) |
| **Reactor** | Modelo de programación reactiva usado por Spring Cloud Gateway. | [07](07-gateway.md) |
| **Repositorio** | Interfaz de Spring Data que encapsula el acceso a datos. | [04](04-datos-y-persistencia.md) |
| **Resource Server** | Servicio que protege recursos y valida tokens de acceso. | [05](05-seguridad.md) |
| **REST** | Estilo arquitectónico basado en recursos, URIs y métodos HTTP. | [06](06-comunicacion-entre-servicios.md) |
| **RestClient** | Cliente HTTP fluido y bloqueante de Spring 6 para aplicaciones MVC. | [06](06-comunicacion-entre-servicios.md) |
| **Retry** | Reintento controlado de una operación que falló de forma transitoria. | [06](06-comunicacion-entre-servicios.md) |
| **Rol** | Agrupación de permisos o responsabilidades, por ejemplo `CLIENTE`, `TECNICO`, `ADMIN`. | [05](05-seguridad.md) |
| **Ruta** | Regla del gateway que asocia un path con un servicio destino. | [07](07-gateway.md) |

## S — Z

| Término | Definición | Dónde verlo |
|---|---|---|
| **Scope** | Permiso delegado que se solicita a un token OAuth2. No es lo mismo que un rol. | [05](05-seguridad.md) |
| **SecurityFilterChain** | Cadena de filtros de Spring Security en aplicaciones servlet. En WebFlux se usa `SecurityWebFilterChain`. | [05](05-seguridad.md) |
| **Service mesh** | Infraestructura que gestiona comunicación entre servicios (sidecars, mTLS, telemetría). No se usa en el MVP. | [08](08-infraestructura-y-operacion.md) |
| **Smoke test** | Prueba mínima que verifica que el sistema está vivo y el flujo básico responde. | [09](09-testing-y-calidad.md) |
| **Snapshot** | Copia de datos de otro contexto tomada en un momento, por ejemplo el domicilio del cliente al crear una solicitud. | [04](04-datos-y-persistencia.md) |
| **Spring Boot** | Framework que simplifica la configuración y el arranque de aplicaciones Spring. | [03](03-stack-y-build.md) |
| **Starter** | Dependencia que agrupa librerías y auto-configuración para una funcionalidad. | [03](03-stack-y-build.md) |
| **Testcontainers** | Librería que levanta contenedores reales para pruebas de integración. | [09](09-testing-y-calidad.md) |
| **Timeout** | Tiempo máximo de espera de una operación antes de considerarla fallida. | [06](06-comunicacion-entre-servicios.md) |
| **Transacción** | Unidad de trabajo que se confirma o revierte completa. | [04](04-datos-y-persistencia.md) |
| **URI** | Identificador único de un recurso, por ejemplo `/api/v1/clientes/42`. | [06](06-comunicacion-entre-servicios.md) |
| **WireMock** | Herramienta para simular APIs externas en pruebas. | [09](09-testing-y-calidad.md) |

## Cómo usar el glosario

1. Si una palabra no te resulta familiar, buscala acá.
2. Después leé el documento indicado en “Dónde verlo”.
3. Respondé las preguntas de repaso de ese documento.
4. Si la definición no alcanza, agregala o corregila: el glosario también es parte del proyecto.
