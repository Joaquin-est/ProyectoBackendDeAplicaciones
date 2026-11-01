# 08 — Infraestructura y operación

> **Decisión en una frase:** levantar el MVP con Docker Compose (PostgreSQL, Keycloak y los tres servicios), perfiles de Spring, variables de entorno y observabilidad básica, sin Kubernetes ni service mesh.

## 1. Topología de Docker Compose

`infra/compose/docker-compose.yml` define:

| Servicio | Imagen / build | Puerto host | Función |
|---|---|---:|---|
| `postgres` | `postgres:16` | 5432 | Bases de los servicios y de Keycloak |
| `keycloak` | `quay.io/keycloak/keycloak:25.0` | 8081 | OIDC / Authorization Server |
| `api-gateway` | Dockerfile propio | 8080 | Entrada pública |
| `ms-clientes` | Dockerfile propio | 8082 | Clientes y domicilios |
| `ms-tecnicos` | Dockerfile propio | 8083 | Técnicos y recursos |
| `ms-solicitudes` | Dockerfile propio | 8084 | Ciclo de vida de solicitudes |

```text
                    ┌──────────────┐
                    │   Keycloak   │
                    │   :8081      │
                    └──────┬───────┘
                           │
┌──────────┐     ┌─────────▼─────────┐
│ Cliente  │────▶│   api-gateway     │
└──────────┘     │      :8080        │
                 └──┬──────┬──────┬──┘
                    │      │      │
          ┌─────────▼┐ ┌───▼─────┐ ┌▼─────────────┐
          │clientes  │ │tecnicos │ │solicitudes   │
          │:8082     │ │:8083    │ │:8084         │
          └────┬─────┘ └───┬─────┘ └──────┬───────┘
               │           │              │
               └───────────┴──────────────┘
                           │
                    ┌──────▼──────┐
                    │ PostgreSQL  │
                    │   :5432     │
                    └─────────────┘
```

## 2. PostgreSQL

Se usa una instancia con varias bases lógicas:

```sql
CREATE DATABASE patito_clientes;
CREATE DATABASE patito_tecnicos;
CREATE DATABASE patito_solicitudes;
CREATE DATABASE keycloak;
```

El script se ejecuta la primera vez que se inicializa el volumen. Si el volumen ya existe, no se vuelve a ejecutar.

### Healthcheck

```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U patito"]
  interval: 5s
  timeout: 5s
  retries: 10
```

Los servicios esperan a que PostgreSQL esté sano antes de arrancar. Esto evita fallos de conexión al inicio.

### Volumen

```yaml
volumes:
  - postgres_data:/var/lib/postgresql/data
```

Persistir el volumen permite reiniciar contenedores sin perder datos. Para reiniciar desde cero:

```powershell
docker compose -f infra/compose/docker-compose.yml down -v
```

`-v` borra el volumen: usar con cuidado.

## 3. Keycloak

Se levanta con `start-dev --import-realm` y monta:

```yaml
../keycloak/realm-patito.json:/opt/keycloak/data/import/realm-patito.json
```

Al arrancar, Keycloak importa el realm si no existe. El archivo define roles, clientes y redirect URIs, pero no usuarios.

Variables:

| Variable | Uso |
|---|---|
| `KEYCLOAK_ADMIN` | Usuario administrador inicial |
| `KEYCLOAK_ADMIN_PASSWORD` | Contraseña del administrador |
| `KC_DB` | `postgres` |
| `KC_DB_URL` | JDBC de la base `keycloak` |
| `KC_DB_USERNAME` | Usuario de PostgreSQL |
| `KC_DB_PASSWORD` | Contraseña de PostgreSQL |

En un entorno real:

- no usar `start-dev`;
- usar HTTPS;
- cambiar secretos;
- configurar backup de la base;
- definir usuarios y grupos;
- revisar la expiración de tokens.

## 4. Dockerfiles

Cada servicio tiene un Dockerfile multi-stage:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -pl services/ms-clientes -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/services/ms-clientes/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

La opción `-pl` selecciona el módulo y `-am` construye también sus dependencias. El contexto de build es la raíz del repositorio, por eso el `COPY . .` incluye todo el monorepo.

## 5. Perfiles de Spring

| Perfil | Base | Flyway | Keycloak | Uso |
|---|---|---|---|---|
| `default` | H2 en memoria | No | Sin issuer | Arranque rápido |
| `local` | Overrides | Según override | Según override | Personalización |
| `docker` | PostgreSQL | Sí | Sí | Docker Compose |

En Compose, los servicios usan:

```yaml
environment:
  SPRING_PROFILES_ACTIVE: docker
  KEYCLOAK_ISSUER_URI: http://keycloak:8080/realms/patito-realm
```

## 6. Variables de entorno

| Variable | Servicio | Función |
|---|---|---|
| `POSTGRES_USER` | Varios | Usuario de PostgreSQL |
| `POSTGRES_PASSWORD` | Varios | Contraseña |
| `KEYCLOAK_ADMIN` | Keycloak | Admin inicial |
| `KEYCLOAK_ADMIN_PASSWORD` | Keycloak | Contraseña del admin |
| `KEYCLOAK_ISSUER_URI` | Gateway y servicios | Issuer JWT |
| `MS_CLIENTES_URL` | Gateway y solicitudes | URL interna de clientes |
| `MS_TECNICOS_URL` | Gateway y solicitudes | URL interna de técnicos |
| `MS_SOLICITUDES_URL` | Gateway | URL interna de solicitudes |
| `GEO_BASE_URL` | Solicitudes | Proveedor de rutas |

Nunca se versionan secretos reales. `.env` está ignorado; `.env.example` usa valores de ejemplo.

## 7. Observabilidad

### Actuator

Cada servicio expone:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

- `health`: estado del servicio.
- `info`: metadatos.
- `metrics`: métricas de Micrometer.
- `prometheus`: formato para scraping.

### Prometheus

`infra/observability/prometheus.yml` es una configuración base. Todavía no hay dashboards ni stack completo de métricas.

### Logs y correlación

- Se usa SLF4J/Logback.
- `ms-solicitudes` tiene un `logback-spring.xml` base.
- `X-Correlation-Id` es la convención para seguir una operación.
- La correlación en logs y la propagación completa son TODO.

### Lo que no hay

- trazas distribuidas;
- OpenTelemetry;
- alertas;
- dashboards;
- SLO/SLI.

Son mejoras posteriores, no requisitos del MVP.

## 8. CI

`.github/workflows/ci.yml`:

- checkout;
- Java 17 Temurin;
- caché de Maven;
- `mvn -B verify`.

No construye imágenes Docker ni levanta Compose. Eso queda como mejora.

## 9. ¿Por qué no Kubernetes ni service mesh?

- el MVP tiene 6 contenedores y un solo host;
- Docker Compose es suficiente para el aula y para demostrar el flujo;
- Kubernetes agrega manifiestos, ingress, secrets, probes y una curva de aprendizaje que no es el foco;
- un service mesh agrega sidecars y complejidad operativa sin un problema de tráfico avanzado.

La decisión se revisa si el sistema crece o si se necesita escalado real.

## 10. Operación local

```powershell
# Build de todo el reactor
.\mvnw.cmd -B verify

# Levantar el entorno completo
docker compose -f infra/compose/docker-compose.yml up --build

# Ver logs de un servicio
docker compose -f infra/compose/docker-compose.yml logs -f ms-solicitudes

# Bajar sin borrar datos
docker compose -f infra/compose/docker-compose.yml down

# Bajar y borrar el volumen
docker compose -f infra/compose/docker-compose.yml down -v
```

Si el puerto 8080, 8081, 8082, 8083, 8084 o 5432 está ocupado, hay que detener el proceso o cambiar el mapeo de puertos.

## 11. Conexión con la materia

- **Apunte 15 — Spring Boot:** perfiles, `application-*.yml`, variables de entorno y configuración externa.
- **Apunte 19 — Gateway:** despliegue del gateway como contenedor de borde.
- **Apunte 21 — Spring Security:** Keycloak como Authorization Server y configuración del issuer.
- **Apunte 17 — Transversales:** logging, errores y actuator.

## 12. Trade-offs

- **Ventaja:** entorno reproducible con un comando.
- **Ventaja:** PostgreSQL real en Docker, sin instalar la base en cada máquina.
- **Costo:** consume recursos y requiere Docker.
- **Costo:** el arranque de Keycloak puede tardar.
- **Riesgo:** un volumen viejo conserva un esquema anterior; hay que saber reiniciarlo.
- **Riesgo:** los secretos de ejemplo no son seguros para producción.

## 13. Para repasar

- ¿Qué contenedores levanta Compose y en qué puertos?
- ¿Por qué se usa una sola instancia de PostgreSQL?
- ¿Qué hace el healthcheck de PostgreSQL?
- ¿Qué pasa si el volumen ya existe y cambia el script de inicialización?
- ¿Cómo se importa el realm de Keycloak?
- ¿Qué diferencia hay entre el perfil default y docker?
- ¿Qué variables cambian las URLs internas?
- ¿Qué endpoints expone Actuator?
- ¿Qué falta para tener observabilidad completa?
- ¿Por qué no Kubernetes en el MVP?
- ¿Qué significa `-am` en el Dockerfile?

## 14. Enlaces relacionados

- [Docker Compose](../infra/compose/docker-compose.yml)
- [Variables de ejemplo](../infra/compose/.env.example)
- [Script de inicialización de PostgreSQL](../infra/postgres/init/01-create-databases.sql)
- [Realm de Keycloak](../infra/keycloak/realm-patito.json)
- [Prometheus](../infra/observability/prometheus.yml)
- [CI](../.github/workflows/ci.yml)
