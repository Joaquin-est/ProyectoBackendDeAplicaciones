# 04 — Datos y persistencia

> **Decisión en una frase:** cada microservicio es dueño de su base lógica; PostgreSQL se usa en Docker, H2 en desarrollo local y Flyway es la fuente de verdad del esquema en producción.

## 1. Database per Service

Cada servicio de dominio tiene su propia base:

| Servicio | Base lógica |
|---|---|
| `ms-clientes` | `patito_clientes` |
| `ms-tecnicos` | `patito_tecnicos` |
| `ms-solicitudes` | `patito_solicitudes` |

En el MVP se usa **una instancia PostgreSQL** con varias bases lógicas, no un contenedor por servicio. La separación es de propiedad y de esquema; no se comparten tablas.

### ¿Por qué?

- cada servicio puede cambiar su modelo sin romper a otro;
- no hay acoplamiento por claves foráneas entre contextos;
- se puede razonar cada esquema por separado;
- se evita que un equipo dependa de una tabla que otro modifica;
- se practica el patrón **Database per Service**.

### Lo que no se permite

- hacer `JOIN` entre bases de servicios distintos;
- declarar una FK de `patito_solicitudes` a `patito_clientes`;
- leer directamente la base de otro servicio;
- compartir entidades JPA entre módulos.

La integración es por API. Si `ms-solicitudes` necesita el domicilio del cliente, llama a `ms-clientes`; no consulta su base.

## 2. Una decisión que se aparta del documento original

El documento del proyecto proponía H2 en modo archivo con volúmenes Docker para el MVP. La implementación del esqueleto tomó una decisión distinta:

- **PostgreSQL 16** en Docker y producción;
- **H2 en memoria** para desarrollo local y pruebas rápidas.

¿Por qué?

- PostgreSQL se parece más a un entorno real y permite practicar migraciones;
- H2 en memoria permite levantar un servicio sin infraestructura;
- Testcontainers permite probar con PostgreSQL real cuando haga falta;
- una única instancia con varias bases reduce el consumo del entorno académico.

La decisión mantiene el patrón Database per Service; cambia el motor de persistencia del entorno integrado.

## 3. Perfiles y configuración

| Perfil | Base | `ddl-auto` | Flyway | Seguridad JWT |
|---|---|---|---|---|
| `default` | H2 en memoria | `create-drop` | Deshabilitado | Sin issuer configurado |
| `local` | Overrides pendientes | Según override | Según override | Según override |
| `docker` | PostgreSQL 16 | `validate` | Habilitado | Issuer de Keycloak |

### Default

Cada servicio arranca con una URL similar a:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:patito_clientes;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
  flyway:
    enabled: false
```

`MODE=PostgreSQL` hace que H2 acepte más sintaxis compatible. Aun así, H2 no es PostgreSQL: no reemplaza una prueba de integración real.

### Docker

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/patito_clientes
    username: ${POSTGRES_USER:patito}
    password: ${POSTGRES_PASSWORD:patito}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

En Docker, Hibernate **no crea** las tablas: solo valida que existan y coincidan con las entidades. Las tablas las crea Flyway.

## 4. JPA, entidades y repositorios

La materia presenta Spring Data JPA con:

- entidades anotadas con `@Entity`;
- claves primarias con `@Id`;
- relaciones como `@ManyToOne`, `@OneToMany`, etc.;
- repositorios que extienden `JpaRepository` o `CrudRepository`;
- consultas derivadas del nombre del método;
- paginación con `Pageable` y `Page`;
- transacciones con `@Transactional`.

En el esqueleto, las entidades y repositorios son placeholders. Cuando se implementen, deben respetar estas reglas:

- las entidades no se comparten entre servicios;
- los repositorios viven en el módulo dueño de la base;
- la lógica de negocio va en el service, no en el controller ni en el repository;
- las operaciones que modifican varias tablas se encapsulan en una transacción local;
- no se usan entidades como DTO de la API.

## 5. Flyway y migraciones

Flyway aplica scripts versionados en orden:

```text
db/migration/
  V1__schema.sql
  V2__datos_iniciales.sql
  V3__lo_que_venga.sql
```

Reglas:

- una migración ya aplicada no se modifica;
- para cambiar algo, se agrega una migración nueva;
- los scripts deben ser idempotentes cuando tenga sentido;
- el esquema de producción se crea con Flyway, no con `ddl-auto: update`;
- los datos iniciales van en migraciones separadas de la estructura;
- cada servicio mantiene sus propias migraciones.

En el repositorio, `V1` y `V2` son comentarios `TODO`. La estructura está preparada, pero el esquema real todavía no existe.

### ¿Por qué no `ddl-auto: update` en producción?

Porque:

- no deja un historial reproducible;
- puede generar cambios inesperados;
- no controla el orden de las migraciones;
- no es una herramienta de despliegue confiable;
- puede ocultar diferencias entre entornos.

## 6. Snapshot y consistencia entre servicios

En la creación de una solicitud, `ms-solicitudes` guarda una copia del domicilio y las coordenadas del cliente.

Ese **snapshot** resuelve dos problemas:

1. si el cliente cambia de domicilio, la solicitud en curso mantiene el lugar original;
2. si `ms-clientes` no está disponible después, la solicitud conserva los datos necesarios para operar.

No es una copia de toda la base: es solo la información necesaria para el contexto transaccional.

### ¿Hay transacciones distribuidas?

No. Cada servicio tiene transacciones locales. Las operaciones que cruzan servicios se resuelven con:

- validaciones antes de crear;
- snapshots;
- estados explícitos;
- compensaciones o reintentos;
- consistencia eventual.

Esto es una consecuencia directa de Database per Service.

## 7. Concurrencia y reglas del negocio

Las reglas de la solicitud exigen control de concurrencia:

- una solicitud no puede asignarse a dos técnicos;
- un técnico no puede tener más de una solicitud `EnCurso`;
- puede tener hasta dos solicitudes en espera;
- la oferta expira a los 15 minutos.

Para implementarlo se prevé:

- `@Transactional` en los servicios;
- validar el estado actual antes de aceptar;
- bloquear o versionar la fila cuando sea necesario;
- una tarea `@Scheduled` que busque ofertas vencidas;
- pruebas de concurrencia.

El esqueleto solo tiene las clases y los paquetes; la lógica es TODO.

## 8. Pruebas de persistencia

| Tipo | Base | Uso |
|---|---|---|
| Unidad / slice | H2 | Pruebas rápidas de servicios y controllers |
| Integración | PostgreSQL con Testcontainers | Validar migraciones, consultas y mapeo real |
| Contrato | WireMock | Simular APIs externas |

H2 es útil para agilidad, pero no garantiza que el SQL funcione en PostgreSQL. Antes de integrar, las migraciones y consultas críticas deben probarse con Testcontainers.

## 9. Conexión con la materia

- **Apunte 16 — Spring Data:** repositorios, entidades, consultas, paginación y transacciones.
- **Apunte 17 — Capas:** el repository es la capa de acceso a datos; el service contiene las reglas.
- **Apunte 14 — APIs:** los datos no se exponen directamente como entidades; se usan DTOs.
- **Apunte 18 — API Client:** la consistencia entre servicios se resuelve por API, no por base compartida.

## 10. Trade-offs

- **Ventaja:** bajo acoplamiento entre contextos.
- **Ventaja:** migraciones independientes.
- **Costo:** no hay JOIN entre contextos.
- **Costo:** aparecen datos duplicados y snapshots.
- **Costo:** no hay transacciones distribuidas.
- **Riesgo:** H2 puede ocultar incompatibilidades con PostgreSQL.
- **Riesgo:** `create-drop` en local puede dar una falsa sensación de que el esquema está bien.

## 11. Para repasar

- ¿Qué significa Database per Service?
- ¿Por qué una sola instancia PostgreSQL con varias bases sigue siendo Database per Service?
- ¿Qué diferencia hay entre `create-drop`, `validate` y `update`?
- ¿Por qué Flyway es preferible a `ddl-auto: update` en producción?
- ¿Qué es un snapshot y por qué se usa en `ms-solicitudes`?
- ¿Qué problema genera no tener transacciones distribuidas?
- ¿Por qué no se puede hacer un JOIN entre `patito_solicitudes` y `patito_clientes`?
- ¿Cuándo conviene H2 y cuándo Testcontainers?
- ¿Qué reglas de concurrencia deben probarse?

## 12. Enlaces relacionados

- [ADR-002](../docs/arquitectura/adr-002-database-per-service.md)
- [02 — Arquitectura y bounded contexts](02-arquitectura-y-bounded-contexts.md)
- [08 — Infraestructura y operación](08-infraestructura-y-operacion.md)
- [09 — Testing y calidad](09-testing-y-calidad.md)
- [Migraciones de clientes](../services/ms-clientes/src/main/resources/db/migration/)
- [Migraciones de técnicos](../services/ms-tecnicos/src/main/resources/db/migration/)
- [Migraciones de solicitudes](../services/ms-solicitudes/src/main/resources/db/migration/)
