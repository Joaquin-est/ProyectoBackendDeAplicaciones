# 09 — Testing y calidad

> **Decisión en una frase:** preparar una estrategia de pruebas por capas con JUnit, Testcontainers y WireMock, aunque en esta entrega todavía no haya pruebas implementadas.

## 1. ¿Por qué una estrategia y no solo “probar al final”?

En microservicios hay más puntos de falla que en un monolito:

- errores de lógica de negocio;
- errores de persistencia;
- contratos entre servicios;
- timeouts y respuestas inesperadas;
- configuración de seguridad;
- rutas del gateway;
- migraciones de base.

Probar solo con el sistema levantado a mano es lento y poco confiable. La estrategia es probar en capas: lo más barato y rápido primero; lo más integrado después.

## 2. La pirámide de pruebas aplicada

```text
                 ┌─────────────────────┐
                 │  Smoke / E2E        │  pocos, lentos, contra el sistema completo
                 ├─────────────────────┤
                 │  Integración        │  Testcontainers, PostgreSQL, gateway
                 ├─────────────────────┤
                 │  Contrato           │  OpenAPI, WireMock, APIs externas
                 ├─────────────────────┤
                 │  Unitarias / slice  │  JUnit, Mockito, H2
                 └─────────────────────┘
```

### Unitarias

Prueban una clase o un service aislado. Usan mocks para repositorios y clientes. Son rápidas y sirven para reglas de negocio:

- ventana de 15 minutos;
- cupo de solicitudes;
- transiciones válidas;
- cálculo de costos;
- validaciones.

### Slice tests

Prueban una capa con Spring:

- `@WebMvcTest` para controllers;
- `@DataJpaTest` para repositorios;
- `@JsonTest` para serialización.

Pueden usar H2 para rapidez, pero no reemplazan una prueba con PostgreSQL.

### Integración

Levantan parte del sistema con dependencias reales:

- PostgreSQL con Testcontainers;
- migraciones Flyway;
- repositorios y transacciones;
- seguridad;
- llamadas a APIs simuladas con WireMock.

### Contrato

Verifican que el contrato OpenAPI y la implementación no se contradigan. También pueden verificar que un cliente externo siga funcionando.

### Smoke test

Verifica que el sistema esté vivo y que el flujo mínimo funcione:

- `GET /actuator/health` en gateway y servicios;
- una petición básica al gateway;
- login o token de prueba;
- respuesta esperada.

`scripts/smoke-test.ps1` es hoy un placeholder.

## 3. Herramientas del proyecto

| Herramienta | Uso |
|---|---|
| JUnit 5 | Framework base |
| Spring Boot Test | Contexto de Spring, slices |
| Mockito | Mocks de dependencias |
| Testcontainers | PostgreSQL real en Docker |
| WireMock | Simular APIs externas |
| `spring-security-test` | Probar seguridad y roles |
| H2 | Pruebas rápidas |
| Maven Surefire | Ejecutar tests en el build |

## 4. Estado actual

- El build ejecuta `surefire`, pero **no hay tests**.
- Los directorios de test están preparados con `.gitkeep`.
- `libs/test-support` contiene `TestFixtures` como placeholder.
- `scripts/smoke-test.ps1` imprime un TODO.
- No hay cobertura mínima configurada.
- No hay pruebas de contrato ni de seguridad.

Esto es coherente con un esqueleto: primero se define la estructura y luego se implementa. Pero antes de mergear lógica de negocio, cada módulo debe tener pruebas.

## 5. ¿H2 o Testcontainers?

| Criterio | H2 | Testcontainers/PostgreSQL |
|---|---|---|
| Velocidad | Alta | Menor |
| Simplicidad | Alta | Requiere Docker |
| Fidelidad | Baja | Alta |
| Migraciones Flyway | Limitada | Real |
| SQL específico | Puede fallar | Confiable |

Regla práctica:

- H2 para pruebas unitarias y slices rápidos;
- Testcontainers para integración, migraciones y consultas críticas;
- no usar H2 para validar el esquema de producción.

## 6. WireMock y APIs externas

`ms-solicitudes` depende de clientes, técnicos y un proveedor de rutas. En una prueba no se debe llamar a un servicio real.

WireMock permite:

- simular `GET /internal/v1/clientes/{id}`;
- devolver un 404 o un 503;
- simular latencia;
- probar timeouts;
- verificar headers y tokens;
- reproducir respuestas raras.

Esto es clave para probar resiliencia sin depender de la red.

## 7. Pruebas de seguridad

Con `spring-security-test` se puede:

- simular un JWT;
- probar roles;
- verificar 401 y 403;
- probar que un endpoint público no requiera token;
- probar que un `CLIENTE` no acceda a un endpoint de `ADMIN`.

La seguridad no se valida solo “probando con Postman”: debe tener tests automatizados.

## 8. Calidad del código y del contrato

Además de los tests:

- validaciones con Bean Validation;
- errores uniformes con `ProblemDetail`;
- contratos OpenAPI actualizados;
- nombres claros;
- capas respetadas;
- ausencia de secretos;
- build verde.

No se configuró Checkstyle, SpotBugs ni SonarQube. Son mejoras posibles, no requisitos del esqueleto.

## 9. CI y pruebas

Hoy el CI ejecuta `mvn -B verify`. Cuando existan tests:

- se ejecutarán en cada push;
- los tests de integración necesitarán Docker en el runner;
- se puede separar `verify` de un perfil `integration-test`;
- se puede publicar cobertura;
- se puede agregar un smoke test con Compose.

## 10. Criterios para considerar una funcionalidad terminada

- Tiene contrato OpenAPI.
- Tiene prueba unitaria de la regla principal.
- Tiene prueba de integración si toca base o seguridad.
- Devuelve errores `ProblemDetail`.
- Respeta el límite del módulo.
- Pasa el build completo.
- Actualiza la documentación si cambió una decisión.

## 11. Conexión con la materia

- **Apunte 16 — Spring Data:** pruebas de repositorios, consultas y paginación.
- **Apunte 17 — Transversales:** validación, errores globales y Swagger.
- **Apunte 18 — API Client:** WireMock, timeouts y manejo de errores.
- **Apunte 21 — Spring Security:** pruebas con tokens y roles.

## 12. Trade-offs

- **Ventaja:** detectar errores antes de integrar.
- **Ventaja:** documentar el comportamiento esperado.
- **Costo:** escribir y mantener pruebas.
- **Costo:** Testcontainers requiere Docker y tarda más.
- **Riesgo:** una prueba con H2 puede dar falsa confianza.
- **Riesgo:** pruebas frágiles que dependen del orden o de datos compartidos.

## 13. Para repasar

- ¿Cuál es la diferencia entre una prueba unitaria y una de integración?
- ¿Qué es un slice test?
- ¿Cuándo usar H2 y cuándo Testcontainers?
- ¿Qué permite WireMock?
- ¿Qué se prueba con `spring-security-test`?
- ¿Qué es un smoke test?
- ¿Por qué no alcanza con probar manualmente con Postman?
- ¿Qué debe cumplir una funcionalidad para considerarse terminada?
- ¿Qué falta para que el CI ejecute pruebas reales?

## 14. Enlaces relacionados

- [Test-support](../libs/test-support/)
- [Smoke test](../scripts/smoke-test.ps1)
- [CI](../.github/workflows/ci.yml)
- [04 — Datos y persistencia](04-datos-y-persistencia.md)
- [05 — Seguridad](05-seguridad.md)
- [10 — Documentación y organización](10-documentacion-y-organizacion.md)
