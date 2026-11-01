# Preguntas de repaso

Preguntas para estudiar, preparar una defensa o comprobar si las decisiones quedaron claras. Intentá responder antes de abrir la respuesta.

## Arquitectura

<details>
<summary>1. ¿Qué diferencia hay entre microservicio, módulo Maven y bounded context?</summary>

Un **microservicio** es un componente desplegable de forma independiente. Un **módulo Maven** es una unidad de compilación. Un **bounded context** es un límite de modelo y lenguaje de negocio. En este proyecto, `ms-clientes` es un microservicio y un bounded context; `common-web` es un módulo Maven, pero no un microservicio.
</details>

<details>
<summary>2. ¿Por qué el monorepo no es un monolito?</summary>

Porque el parent Maven solo organiza el build. Cada servicio tiene su propio `pom.xml`, su jar ejecutable y su contenedor. Un monolito sería un único proceso con todo el código y una única base de datos.
</details>

<details>
<summary>3. ¿Por qué se separaron clientes, técnicos y solicitudes?</summary>

Porque son capacidades de negocio distintas, con datos y ciclos de vida propios. Clientes y técnicos son datos maestros; solicitudes es el contexto transaccional que orquesta el ciclo de vida. Certificaciones y zonas no se separaron porque pertenecen al contexto de técnicos.
</details>

<details>
<summary>4. ¿Qué es un monolito distribuido y cómo se evita?</summary>

Es un sistema con varios servicios desplegados por separado, pero tan acoplados que funcionan como uno solo. Se evita separando por negocio (no por entidad), sin compartir bases ni entidades, sin lógica en el gateway y con contratos explícitos.
</details>

<details>
<summary>5. ¿Por qué `ms-solicitudes` es el orquestador?</summary>

Porque es el dueño del ciclo de vida de la solicitud: valida reglas, consulta clientes y técnicos, crea ofertas, controla la ventana de 15 minutos, gestiona estados y registra el historial. Los otros servicios aportan datos, pero no deciden el flujo.
</details>

## Datos

<details>
<summary>6. ¿Qué significa Database per Service?</summary>

Que cada microservicio es dueño de su base o esquema y no accede directamente a la base de otro. La integración se hace por API. En el MVP se usa una instancia PostgreSQL con bases lógicas separadas.
</details>

<details>
<summary>7. ¿Por qué no se pueden hacer JOIN entre bases de servicios?</summary>

Porque eso acoplaría los esquemas y rompería la autonomía de los servicios. Si un servicio necesita datos de otro, debe consultarlos por API o guardar un snapshot.
</details>

<details>
<summary>8. ¿Qué es un snapshot y dónde se usa?</summary>

Es una copia de datos de otro contexto tomada en un momento. En `ms-solicitudes` se guarda el domicilio y las coordenadas del cliente al crear la solicitud, para que la solicitud conserve el contexto original aunque el cliente cambie de domicilio.
</details>

<details>
<summary>9. ¿Por qué Flyway y no `ddl-auto: update`?</summary>

Porque Flyway deja migraciones versionadas y reproducibles. `ddl-auto: update` no tiene historial, puede generar cambios inesperados y no es confiable para producción.
</details>

<details>
<summary>10. ¿Cuándo usar H2 y cuándo Testcontainers?</summary>

H2 para pruebas rápidas de unidad o slice. Testcontainers/PostgreSQL para integración, migraciones, consultas críticas y validación del esquema real. H2 no garantiza compatibilidad con PostgreSQL.
</details>

## Seguridad

<details>
<summary>11. ¿Qué diferencia hay entre autenticación y autorización?</summary>

La autenticación verifica quién sos. La autorización decide qué podés hacer. Un usuario puede estar autenticado y no tener permiso para un recurso.
</details>

<details>
<summary>12. ¿Qué diferencia hay entre OAuth2 y OIDC?</summary>

OAuth2 es un marco de autorización delegada. OIDC es una capa de identidad sobre OAuth2 que agrega el ID Token y la identidad del usuario.
</details>

<details>
<summary>13. ¿Qué es un Resource Server?</summary>

Es el servicio que protege recursos y valida los tokens de acceso. En este proyecto, el gateway y los microservicios actúan como Resource Servers.
</details>

<details>
<summary>14. ¿Por qué se valida el JWT en el gateway y también en los servicios?</summary>

Por defensa en profundidad. El gateway rechaza temprano y centraliza políticas de borde; los servicios aplican autorización fina y no dependen de que el gateway esté bien configurado.
</details>

<details>
<summary>15. ¿Qué valida Spring Security de un JWT?</summary>

La firma, el issuer, la vigencia (`exp`) y las claves públicas del emisor. No alcanza con leer el payload: hay que verificar que el token sea auténtico.
</details>

<details>
<summary>16. ¿Por qué los roles de Keycloak necesitan un converter?</summary>

Porque Keycloak los envía en `realm_access.roles` y Spring Security no los interpreta automáticamente como authorities. El converter transforma esos roles en permisos utilizables por `hasRole` o `@PreAuthorize`.
</details>

<details>
<summary>17. ¿Por qué se deshabilita CSRF en una API stateless?</summary>

Porque no hay sesión con cookies. La autenticación viaja en el header `Authorization: Bearer`, por lo que el ataque CSRF clásico no aplica. Si se usaran cookies, habría que revisarlo.
</details>

## Comunicación y gateway

<details>
<summary>18. ¿Por qué `RestClient` y no `WebClient`?</summary>

Porque los servicios de dominio son MVC y bloqueantes. `RestClient` es la opción simple y recomendada para ese modelo. `WebClient` se justifica en flujos reactivos o streaming.
</details>

<details>
<summary>19. ¿Qué es `ProblemDetail`?</summary>

Un formato estándar de error HTTP (RFC 7807) que incluye `type`, `title`, `status`, `detail` e `instance`. Spring 6 lo soporta de forma nativa.
</details>

<details>
<summary>20. ¿Qué es un timeout, un retry y un circuit breaker?</summary>

Un **timeout** limita cuánto se espera. Un **retry** reintenta una falla transitoria. Un **circuit breaker** corta las llamadas a un servicio que está fallando para evitar fallos en cascada.
</details>

<details>
<summary>21. ¿Qué debe hacer `ms-solicitudes` si `ms-clientes` no responde al crear una solicitud?</summary>

Debe rechazar la creación, devolver `503 Service Unavailable` con un `ProblemDetail` claro y no dejar una solicitud huérfana. El cliente puede reintentar.
</details>

<details>
<summary>22. ¿Por qué no se usa Kafka en el MVP?</summary>

Porque no hay un caso de eventos que justifique broker, outbox, idempotencia y consumidores. Agregar Kafka antes de necesitarlo aumenta la complejidad sin aportar al flujo central.
</details>

<details>
<summary>23. ¿Qué problema resuelve el API Gateway?</summary>

Centraliza el punto de entrada, el ruteo, CORS, seguridad de borde, logs y correlación. El cliente no necesita conocer los puertos internos.
</details>

<details>
<summary>24. ¿Por qué el gateway no debe tener lógica de negocio?</summary>

Porque se convertiría en un monolito en el borde, acoplado a todos los dominios y en un punto crítico de fallo. Las reglas pertenecen a los servicios dueños del negocio.
</details>

<details>
<summary>25. ¿Por qué no Eureka en esta etapa?</summary>

Porque hay tres servicios con URLs conocidas y configurables por entorno. Eureka agregaría otro proceso y configuración sin un beneficio claro para el MVP.
</details>

## Testing, documentación y alcance

<details>
<summary>26. ¿Qué es un smoke test?</summary>

Una prueba mínima que verifica que el sistema está vivo y que el flujo básico responde, por ejemplo `GET /actuator/health` y una petición al gateway.
</details>

<details>
<summary>27. ¿Qué permite WireMock?</summary>

Simular APIs externas: respuestas, errores, latencia y headers. Sirve para probar clientes y resiliencia sin depender de un servicio real.
</details>

<details>
<summary>28. ¿Qué es un ADR?</summary>

Un documento que registra una decisión de arquitectura: contexto, decisión, alternativas y consecuencias. Sirve para no repetir discusiones y entender por qué el sistema es como es.
</details>

<details>
<summary>29. ¿Por qué no se versionan los PDFs?</summary>

Porque son archivos binarios pesados que no hacen falta para compilar y agrandan el repositorio. Se ignoran con `*.pdf` y quedan disponibles localmente.
</details>

<details>
<summary>30. ¿Qué significa que este repositorio sea un esqueleto?</summary>

Que la estructura, configuración y documentación están listas, pero la lógica de negocio, los contratos, las migraciones, la seguridad y las pruebas todavía son placeholders o TODOs.
</details>

## Preguntas integradoras

<details>
<summary>31. Si tuvieras que defender la arquitectura en cinco minutos, ¿qué dirías?</summary>

Que el dominio se dividió en tres bounded contexts con datos propios, un gateway de borde y Keycloak como emisor de identidad. La comunicación es REST/JSON con `RestClient`, las bases son independientes y el MVP evita infraestructura que no necesita. El repositorio es un monorepo por practicidad de build, pero cada servicio se despliega por separado.
</details>

<details>
<summary>32. ¿Cuál es el mayor riesgo técnico del proyecto?</summary>

La combinación de reglas de negocio con concurrencia (ventana de 15 minutos, asignación única, cupos) y llamadas entre servicios. Requiere transacciones, validación de estado, timeouts y pruebas de integración.
</details>

<details>
<summary>33. ¿Qué decisión revisarías primero si el sistema creciera?</summary>

La incorporación de eventos/outbox si aparecen procesos asíncronos reales, y discovery/orquestación si cambia la cantidad de instancias. Antes de eso, la seguridad y los contratos deben estar completos.
</details>
