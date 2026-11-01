# Decisiones explicadas — Backend Técnica Patito

> Guía didáctica de las decisiones de arquitectura del Trabajo Práctico Integrado de Backend de Aplicaciones.
>
> Esta carpeta **no reemplaza** los ADRs formales de `docs/arquitectura/`: los explica con más detalle, los conecta con los apuntes de la materia y muestra cómo se reflejan en el código.

## ¿Por qué existe esta guía?

Un ADR suele ser corto: contexto, decisión y consecuencias. Para estudiar y para incorporar gente nueva al equipo hace falta algo más:

- entender **por qué** se eligió cada tecnología;
- relacionar la decisión con los conceptos de la materia;
- ver **dónde** está implementada o dónde queda el `TODO`;
- conocer las alternativas y los costos de la decisión;
- tener preguntas de repaso para una defensa o un final.

Eso es lo que encontrás en esta carpeta.

## El proyecto en una frase

Técnica Patito S.R.L. necesita sistematizar la coordinación de servicios técnicos domiciliarios: registrar clientes y técnicos, crear solicitudes, asignar al técnico más cercano y apto, gestionar una ventana de aceptación de 15 minutos, presupuestos, estados e historial.

La solución se plantea como un backend de microservicios con Spring Boot, un API Gateway, Keycloak para seguridad, PostgreSQL para ejecución integrada y H2 para desarrollo local.

## Cómo leer esta guía

### Si recién empiezas

Leé en este orden:

1. [01 — Contexto y alcance](01-contexto-y-alcance.md)
2. [02 — Arquitectura y bounded contexts](02-arquitectura-y-bounded-contexts.md)
3. [03 — Stack y build](03-stack-y-build.md)
4. [04 — Datos y persistencia](04-datos-y-persistencia.md)
5. [05 — Seguridad](05-seguridad.md)
6. [06 — Comunicación entre servicios](06-comunicacion-entre-servicios.md)
7. [07 — Gateway](07-gateway.md)
8. [08 — Infraestructura y operación](08-infraestructura-y-operacion.md)
9. [09 — Testing y calidad](09-testing-y-calidad.md)
10. [10 — Documentación y organización](10-documentacion-y-organizacion.md)
11. [11 — Decisiones pendientes](11-decisiones-pendientes.md)

### Si buscas una decisión concreta

Usá el [mapa de decisiones](mapa-decisiones.md), que relaciona cada decisión con:

- el apunte de la materia;
- el archivo o módulo donde se aplica;
- el documento que la explica.

### Si estás preparando una defensa o un final

- Repasá el [glosario](glosario.md).
- Respondé las [preguntas de repaso](preguntas-de-repaso.md).
- Leé las secciones “Conexión con la materia” y “Para repasar” de cada documento.

## Mapa de apuntes de la materia

| Apunte | Tema | Se usa en |
|---|---|---|
| Apunte 14 | Diseño de APIs, REST, HTTP, OpenAPI | [06](06-comunicacion-entre-servicios.md), [10](10-documentacion-y-organizacion.md) |
| Apunte 15 | Spring Framework, Spring Boot, starters, perfiles | [03](03-stack-y-build.md), [08](08-infraestructura-y-operacion.md) |
| Apunte 16 | Spring Data, JPA, repositorios, paginación | [04](04-datos-y-persistencia.md) |
| Apunte 17 | Capas, DTOs, validación, errores, Swagger | [03](03-stack-y-build.md), [06](06-comunicacion-entre-servicios.md), [09](09-testing-y-calidad.md) |
| Apunte 18 | Consumo de APIs, RestClient, timeouts, resiliencia | [06](06-comunicacion-entre-servicios.md) |
| Apunte 19 | API Gateway, rutas, filtros, CORS | [07](07-gateway.md) |
| Apunte 20 | Seguridad, autenticación, autorización, OAuth2/OIDC, JWT | [05](05-seguridad.md) |
| Apunte 21 | Spring Security, Resource Server, roles con Keycloak | [05](05-seguridad.md) |

## Las decisiones más importantes en una tabla

| Decisión | Resumen | Documento |
|---|---|---|
| Arquitectura | Microservicios por bounded context, no un monolito | [02](02-arquitectura-y-bounded-contexts.md) |
| Repositorio | Monorepo Maven multi-módulo, con artefactos independientes | [03](03-stack-y-build.md) |
| Datos | Una base lógica por servicio; PostgreSQL en Docker y H2 local | [04](04-datos-y-persistencia.md) |
| Seguridad | Keycloak + OAuth2 Resource Server + JWT | [05](05-seguridad.md) |
| Comunicación | REST/JSON síncrono con `RestClient` y OpenAPI | [06](06-comunicacion-entre-servicios.md) |
| Borde | Spring Cloud Gateway reactivo, sin lógica de negocio | [07](07-gateway.md) |
| Infraestructura | Docker Compose; sin Eureka, Kafka, Kubernetes ni service mesh en el MVP | [08](08-infraestructura-y-operacion.md) |
| Calidad | JUnit, Testcontainers, WireMock y smoke tests pendientes | [09](09-testing-y-calidad.md) |
| Documentación | ADRs, OpenAPI, C4; PDFs fuera de Git | [10](10-documentacion-y-organizacion.md) |
| Alcance | Esqueleto con placeholders; la lógica de negocio es de cada equipo | [11](11-decisiones-pendientes.md) |

## Estado actual

- El reactor Maven compila los 9 módulos con `.\mvnw.cmd -B verify`.
- No hay lógica de negocio implementada: controllers, services, repositories, entidades y clientes son placeholders con `TODO`.
- La seguridad, los contratos OpenAPI, las migraciones Flyway y los diagramas C4 están preparados como estructura, pero deben completarse.
- Los PDFs de la materia no se versionan; se ignoran con `*.pdf`.

## Documentación formal relacionada

- [Registro formal de decisiones](../docs/arquitectura/decisiones.md)
- [ADRs](../docs/arquitectura/README.md)
- [Contratos OpenAPI](../contracts/openapi/)
- [README principal del repositorio](../README.md)

## Cómo se documenta una decisión nueva

1. Identificá el problema y las alternativas reales.
2. Escribí un ADR con contexto, decisión, alternativas y consecuencias.
3. Si la decisión necesita explicación didáctica, agregá o actualizá un archivo de esta carpeta.
4. Actualizá el [mapa de decisiones](mapa-decisiones.md).
5. Si cambia una regla de trabajo, actualizá también el README principal.

La documentación es parte del producto: una decisión que no está escrita tiende a repetirse o a contradecirse.
