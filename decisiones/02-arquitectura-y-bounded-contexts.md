# 02 — Arquitectura y bounded contexts

> **Decisión en una frase:** dividir el backend en tres microservicios de dominio (`ms-clientes`, `ms-tecnicos`, `ms-solicitudes`) más un gateway de borde, cada uno con su propia base y su propio despliegue.

## 1. Microservicios no es sinónimo de “muchos módulos”

Un microservicio es un componente desplegable de forma independiente, con una responsabilidad de negocio acotada y su propio modelo de datos. Un módulo Maven, en cambio, es una unidad de compilación.

En este proyecto:

- `ms-clientes`, `ms-tecnicos` y `ms-solicitudes` son microservicios;
- `common-web`, `security-jwt`, `geo-client` y `test-support` son librerías;
- `api-gateway` es un componente de infraestructura de borde;
- el `pom.xml` raíz es un agregador de build, no un artefacto desplegable.

Que todo viva en un mismo repositorio no convierte al sistema en un monolito. El monolito sería un único proceso con todo el código y una única base; acá cada servicio se empaqueta y se ejecuta por separado.

## 2. Bounded contexts del dominio

### Clientes

Responsable de:

- padrón de clientes;
- datos personales;
- domicilios;
- coordenadas geográficas.

No conoce solicitudes, ofertas ni técnicos.

### Técnicos

Responsable de:

- padrón de técnicos;
- disponibilidad;
- certificaciones y conocimientos;
- herramientas;
- zonas de cobertura.

No conoce solicitudes ni clientes.

### Solicitudes

Es el contexto transaccional y el orquestador:

- creación de solicitudes;
- ofertas a técnicos;
- ventana de aceptación;
- asignación;
- presupuestos;
- estados;
- historial.

Necesita datos de clientes y técnicos, pero **no accede a sus bases**: los consulta por API.

### Gateway

No es un contexto de negocio. Es una fachada de borde:

- rutea;
- aplica seguridad de entrada;
- centraliza CORS;
- agrega correlación y logs;
- oculta los puertos internos.

### Librerías compartidas

No representan un bounded context. Contienen infraestructura transversal:

- `common-web`: errores, paginación, correlación, validaciones;
- `security-jwt`: conversión de roles y configuración JWT;
- `geo-client`: integración con mapas/rutas;
- `test-support`: utilidades de prueba.

## 3. ¿Por qué no un microservicio por entidad?

Porque sería un **monolito distribuido**: muchos procesos, pero un mismo modelo de negocio fragmentado artificialmente.

Certificaciones, herramientas y zonas pertenecen a la gestión de técnicos. Si se separaran:

- habría que coordinar cuatro servicios para responder “¿este técnico es elegible?”;
- aumentarían las llamadas de red;
- crecería el riesgo de inconsistencias;
- se duplicarían DTOs y transacciones.

La regla es separar por **capacidad de negocio**, no por tabla.

## 4. Mapa de contextos

```text
                         ┌──────────────┐
                         │   Keycloak   │
                         │   OIDC/JWT   │
                         └──────┬───────┘
                                │ tokens
┌────────┐        ┌─────────────▼─────────────┐
│  SPA   │───────▶│        API Gateway        │
└────────┘        │  ruteo + seguridad borde  │
                  └───────┬─────────┬─────────┘
                          │         │
             ┌────────────▼──┐  ┌───▼────────────┐
             │  ms-clientes  │  │  ms-tecnicos   │
             │  clientes +   │  │  técnicos +    │
             │  domicilios   │  │  certificados  │
             └───────▲───────┘  └───────▲────────┘
                     │                  │
                     │   llamadas REST  │
             ┌───────┴──────────────────┴───────┐
             │          ms-solicitudes          │
             │  solicitudes, ofertas, estados   │
             └───────────────┬──────────────────┘
                             │
                    ┌────────▼────────┐
                    │  geo-client /   │
                    │  API de rutas   │
                    └─────────────────┘
```

Las llamadas entre servicios van directo por la red interna. No pasan por el gateway.

## 5. Flujo principal y decisiones de diseño

### Creación de una solicitud

`ms-solicitudes` necesita:

1. validar que el cliente existe;
2. obtener el domicilio y las coordenadas;
3. guardar una **copia** del domicilio y las coordenadas al momento de crear la solicitud.

Esa copia se conoce como **snapshot**. Si el cliente cambia de domicilio después, la solicitud en curso mantiene la dirección original. Es una decisión de consistencia: se prioriza que la solicitud conserve el contexto con el que fue creada.

### Búsqueda de técnicos

El proyecto propone un cálculo local de cercanía con la fórmula de **Haversine** para filtrar técnicos dentro de los 13 km. Solo al técnico seleccionado se le consulta la API externa de rutas para obtener distancia, tiempo y costo.

¿Por qué?

- evita saturar la API externa con una llamada por cada técnico;
- reduce latencia;
- permite comparar muchos candidatos con matemática simple;
- deja la API de mapas para el cálculo fino de la ruta elegida.

### Oferta y ventana de 15 minutos

La oferta se guarda con una fecha de expiración. Una tarea programada (`@Scheduled`) revisa las ofertas vencidas y pasa al siguiente técnico. La aceptación debe validar el estado actual y ejecutarse en una transacción para evitar que dos acciones simultáneas asignen la misma solicitud.

### Estados e historial

La solicitud tiene un estado actual, pero cada transición se registra en una entidad de historial con fecha, hora y observaciones. Esto permite auditoría y trazabilidad.

## 6. ¿Por qué no un monolito?

El monolito sería más simple para el tamaño actual del MVP. Sin embargo, el objetivo del trabajo es practicar una arquitectura de microservicios y el dominio tiene límites naturales claros.

Se aceptan estos costos:

- más configuración;
- más procesos;
- llamadas de red;
- fallos parciales;
- duplicación controlada de datos;
- necesidad de contratos y pruebas de integración.

Se mitiga:

- con un monorepo y un build único;
- con contratos OpenAPI;
- con `ProblemDetail` para errores;
- con timeouts y circuit breakers;
- con pruebas con Testcontainers y WireMock.

## 7. ¿Por qué no Eureka, Kafka, Kubernetes ni service mesh?

| Tecnología | Por qué no en el MVP | Cuándo revisar |
|---|---|---|
| Eureka | Tres servicios con URLs fijas por entorno | Si cambia la cantidad/ubicación de instancias |
| Kafka | No hay un caso de eventos que justifique outbox y consumidores | Si aparecen notificaciones o procesos asíncronos reales |
| Kubernetes | Docker Compose alcanza para el entorno académico | Si se necesita escalado/orquestación real |
| Service mesh | Agrega complejidad operativa sin un problema que resolver | Si crece la malla de servicios y se necesita tráfico avanzado |

La decisión es consciente: **no agregar infraestructura antes de que el dominio la necesite**.

## 8. Independencia de despliegue

Cada servicio:

- tiene su propio `pom.xml`;
- se empaqueta como jar ejecutable con Spring Boot;
- tiene su propio `Dockerfile`;
- escucha en su propio puerto;
- tiene su propia base lógica;
- puede reiniciarse sin reiniciar los demás.

El gateway es la única entrada pública. Las APIs internas (`/internal/v1/**`) no se exponen a través de él.

## 9. Conexión con la materia

- **Apunte 17 — Capas:** cada microservicio repite una estructura controller → service → repository → model/DTO. La separación de capas se mantiene dentro de cada contexto.
- **Apunte 18 — API Client:** `ms-solicitudes` consume APIs de clientes y técnicos con `RestClient`; `geo-client` encapsula el proveedor de mapas.
- **Apunte 19 — API Gateway:** el gateway es la fachada y el punto único de entrada.
- **Apunte 20 y 21 — Seguridad:** los actores del dominio se traducen en roles y tokens.

## 10. Trade-offs que hay que poder explicar

- **Ventaja:** cada servicio evoluciona y se despliega por separado.
- **Ventaja:** los límites del dominio son explícitos.
- **Costo:** una operación puede requerir varias llamadas y fallar parcialmente.
- **Costo:** no hay transacciones distribuidas.
- **Costo:** hay que coordinar contratos y versiones.
- **Riesgo:** si se separan servicios por entidad en lugar de por negocio, se cae en un monolito distribuido.

## 11. Para repasar

- ¿Qué diferencia hay entre microservicio, módulo Maven y bounded context?
- ¿Por qué clientes, técnicos y solicitudes son contextos distintos?
- ¿Por qué certificaciones y zonas no son microservicios independientes?
- ¿Qué problema resuelve el snapshot del domicilio?
- ¿Por qué se usa Haversine antes de llamar a la API de rutas?
- ¿Cómo se evita que dos técnicos acepten la misma solicitud?
- ¿Por qué el gateway no debe contener reglas de negocio?
- ¿Qué costo se acepta al pasar de un monolito a microservicios?
- ¿Por qué no se usa Eureka ni Kafka en esta etapa?

## 12. Enlaces relacionados

- [01 — Contexto y alcance](01-contexto-y-alcance.md)
- [04 — Datos y persistencia](04-datos-y-persistencia.md)
- [06 — Comunicación entre servicios](06-comunicacion-entre-servicios.md)
- [07 — Gateway](07-gateway.md)
- [ADR-001](../docs/arquitectura/adr-001-monorepo-multimodulo.md)
- [ADR-005](../docs/arquitectura/adr-005-gateway-borde.md)
