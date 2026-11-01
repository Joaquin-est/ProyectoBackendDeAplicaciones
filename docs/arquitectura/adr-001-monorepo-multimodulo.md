# ADR-001: Monorepo Maven multi-módulo

- Estado: Aceptado
- Fecha: 2026-10-31
- Implementación: completa para el esqueleto inicial

## Contexto

El backend de Técnica Patito se desarrolla en equipo y necesita compartir contratos, librerías transversales y versiones de dependencias sin fricción. Publicar cada librería en un repositorio Maven separado y coordinar versiones entre repositorios agrega trabajo que no aporta valor en esta etapa.

El repositorio original tenía un esqueleto Spring Boot de un solo módulo. El MVP requiere separar responsabilidades por bounded context, pero manteniendo un único build y una única integración continua.

## Decisión

Mantener un monorepo Git con un parent Maven agregador:

- El `pom.xml` raíz es `com.patitosrl:patito-backend`, con packaging `pom` y herencia de `spring-boot-starter-parent` 3.2.5.
- El parent centraliza versiones, propiedades y plugins; no contiene código de negocio.
- Los módulos se organizan en `libs/*`, `platform/api-gateway` y `services/*`.
- Cada microservicio se empaqueta con `spring-boot-maven-plugin` y tiene su propio `Dockerfile`; no se genera un único artefacto ejecutable con todos los servicios.
- No se conserva una copia del esqueleto inicial de un solo módulo dentro del árbol. La versión anterior queda disponible en el historial de Git.

## Alternativas consideradas

- **Un repositorio por microservicio:** descartado para el MVP por el costo de coordinar versiones, contratos y cambios cruzados entre cuatro personas.
- **Monolito modular:** descartado porque el objetivo explícito es practicar y desplegar microservicios.
- **Monorepo con repositorios Maven separados para las librerías:** postergado. Se puede adoptar más adelante si las librerías empiezan a versionarse de forma independiente.

## Consecuencias

- Un solo clon y un solo `mvn verify` permiten validar todo el sistema.
- Los cambios en contratos, librerías y servicios pueden hacerse en un mismo commit.
- El pipeline de CI es simple; más adelante se pueden agregar filtros por rutas para no compilar todo en cada cambio.
- El repositorio concentra todos los módulos y el CI compila el reactor completo.
- La independencia de despliegue se mantiene a nivel de contenedores y artefactos, no a nivel de repositorio.

## Implementación en el repositorio

- `pom.xml`: parent y lista de módulos.
- `libs/`: librerías compartidas.
- `platform/api-gateway/`: gateway de borde.
- `services/`: microservicios de dominio.
- `scripts/build-all.ps1`: build local de todo el reactor.
- `.github/workflows/ci.yml`: build con Java 17 y Maven.
