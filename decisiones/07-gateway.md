# 07 — Gateway

> **Decisión en una frase:** usar Spring Cloud Gateway como única entrada pública, con rutas estáticas, seguridad de borde y sin lógica de negocio.

## 1. ¿Qué problema resuelve un gateway?

Si el frontend se conectara directo a cada microservicio:

- tendría que conocer tres URLs y tres puertos;
- repetiría CORS en cada servicio;
- la seguridad quedaría expuesta en cada proceso;
- un cambio de ubicación de un servicio rompería al cliente;
- sería más difícil aplicar rate limiting, logs o correlación;
- habría más superficie de exposición.

El gateway actúa como **fachada**:

```text
Cliente → Gateway → ms-clientes
                  → ms-tecnicos
                  → ms-solicitudes
```

El cliente conoce una sola URL. El gateway decide a qué servicio enviar cada petición.

## 2. Spring Cloud Gateway

Se usa Spring Cloud Gateway porque:

- es parte del ecosistema Spring;
- funciona sobre WebFlux y Reactor;
- permite rutas declarativas en YAML o programáticas;
- tiene predicados y filtros;
- se integra con Spring Security;
- es el tema del apunte 19.

El gateway **no** incluye `spring-boot-starter-web`: si se agrega, se mezclan dos stacks (MVC y WebFlux) y la aplicación puede fallar. Esta es una decisión técnica importante.

## 3. Rutas actuales

La configuración está en `platform/api-gateway/src/main/resources/application.yml`.

| Ruta | Servicio destino | URL por defecto |
|---|---|---|
| `/api/v1/clientes/**` | `ms-clientes` | `http://localhost:8082` |
| `/api/v1/domicilios/**` | `ms-clientes` | `http://localhost:8082` |
| `/api/v1/tecnicos/**` | `ms-tecnicos` | `http://localhost:8083` |
| `/api/v1/zonas/**` | `ms-tecnicos` | `http://localhost:8083` |
| `/api/v1/certificaciones/**` | `ms-tecnicos` | `http://localhost:8083` |
| `/api/v1/herramientas/**` | `ms-tecnicos` | `http://localhost:8083` |
| `/api/v1/solicitudes/**` | `ms-solicitudes` | `http://localhost:8084` |
| `/api/v1/ofertas/**` | `ms-solicitudes` | `http://localhost:8084` |
| `/api/v1/historial/**` | `ms-solicitudes` | `http://localhost:8084` |

Las URLs se pueden cambiar con:

```text
MS_CLIENTES_URL
MS_TECNICOS_URL
MS_SOLICITUDES_URL
```

En Docker apuntan a los nombres de servicio (`http://ms-clientes:8082`, etc.).

## 4. Predicados y filtros

Una ruta de Spring Cloud Gateway tiene:

- **ID:** nombre de la ruta.
- **URI:** destino.
- **Predicados:** condiciones para que la ruta se active. Por ejemplo, `Path=/api/v1/clientes/**`.
- **Filtros:** transformaciones antes o después de reenviar. Por ejemplo, agregar headers, reescribir la ruta o limitar requests.

En el esqueleto:

- las rutas están declaradas;
- los filtros de correlación, access log, CORS y errores tienen clases base;
- la lógica de los filtros sigue siendo TODO.

## 5. ¿Qué debe hacer el gateway?

| Responsabilidad | ¿En el gateway? | Comentario |
|---|---|---|
| Ruteo | Sí | Path → servicio |
| Validación primaria de JWT | Sí | Seguridad de borde |
| CORS | Sí | Centralizado para la SPA |
| Correlation ID | Sí | Se propaga a los servicios |
| Access log | Sí | Registro de entrada |
| Manejo uniforme de errores de borde | Sí | ProblemDetail |
| Reglas de negocio | No | Van en los servicios |
| Acceso a base de datos | No | Cada servicio es dueño |
| Cálculo de presupuestos | No | `ms-solicitudes` |
| Elegibilidad de técnicos | No | `ms-tecnicos` |

## 6. ¿Qué NO debe hacer el gateway?

Un gateway con reglas de negocio se convierte en un **monolito en el borde**. Eso trae:

- acoplamiento con todos los dominios;
- dificultad para probar;
- un punto único de fallo con demasiada responsabilidad;
- duplicación de reglas que también deben estar en el servicio.

El gateway puede autenticar, rutear, observar y proteger. No decide si un técnico es elegible ni si una solicitud puede pasar a `EnCurso`.

## 7. Seguridad en el borde

El gateway:

- valida el JWT;
- puede rechazar temprano un token inválido;
- aplica CORS;
- puede limitar tasa de peticiones;
- puede enriquecer headers.

Pero cada servicio también valida el token. La razón está en [05 — Seguridad](05-seguridad.md): defensa en profundidad y autorización por recurso.

## 8. CORS y CSRF

### CORS

La SPA corre en otro origen (`localhost:3000` o `localhost:5173`). El navegador aplica Same-Origin Policy; para permitir la llamada, el gateway debe devolver los headers CORS correctos.

Alternativas:

- configurar CORS en cada servicio;
- centralizarlo en el gateway.

Se elige el gateway porque es el único punto de entrada público y evita repetir configuración.

### CSRF

La API es stateless y usa `Authorization: Bearer`. No hay sesión con cookies, así que CSRF no aplica de la misma forma. Se deshabilita en la configuración de seguridad. Si en el futuro se usan cookies de sesión, hay que revisar esta decisión.

## 9. Correlation ID

El gateway debe:

1. leer `X-Correlation-Id` si viene del cliente;
2. generar uno si no viene;
3. agregarlo a la respuesta;
4. propagarlo a los servicios;
5. incluirlo en los logs.

Esto permite reconstruir una operación que pasó por varios servicios. Las clases `CorrelationIdFilter` del gateway y de `common-web` son placeholders.

## 10. ¿Por qué no Eureka?

Con tres servicios y URLs conocidas, Eureka agrega:

- otro proceso;
- registro y heartbeat;
- configuración de clientes;
- resolución de nombres;
- una dependencia más para el entorno local.

Las URLs fijas por variables de entorno son suficientes. Si se escala horizontalmente o cambian las ubicaciones con frecuencia, se puede incorporar descubrimiento de servicios. La decisión debe registrarse en un ADR.

## 11. Flujo de una petición

```text
1. SPA → GET http://localhost:8080/api/v1/solicitudes/123
2. Gateway:
   - valida JWT
   - aplica CORS
   - genera/propaga X-Correlation-Id
   - hace match con /api/v1/solicitudes/**
3. Gateway → http://ms-solicitudes:8084/api/v1/solicitudes/123
4. ms-solicitudes valida el token y los permisos
5. ms-solicitudes responde
6. Gateway devuelve la respuesta al cliente
```

## 12. Manejo de errores

El gateway debe devolver errores consistentes cuando:

- el token es inválido;
- el servicio destino no responde;
- el timeout vence;
- no hay ruta;
- el cuerpo de la petición es inválido.

El formato elegido es `ProblemDetail` (RFC 7807). La clase `GatewayErrorHandler` es el punto de partida.

## 13. Conexión con la materia

- **Apunte 19 — API Gateway:** fachada, acoplamiento, aspectos transversales, rutas, predicados, filtros, CORS, rate limiting y Spring Cloud Gateway.
- **Apunte 20 y 21 — Seguridad:** validación del token en el borde y configuración reactiva.
- **Apunte 17 — Transversales:** errores uniformes, logging y correlación.

## 14. Trade-offs

- **Ventaja:** un único punto de entrada y configuración transversal.
- **Ventaja:** el cliente no conoce los puertos internos.
- **Costo:** el gateway es un punto crítico de disponibilidad.
- **Costo:** hay que mantener las rutas actualizadas.
- **Costo:** sin discovery, un cambio de URL requiere configuración.
- **Riesgo:** si se agrega lógica de negocio, se vuelve un cuello de botella y un monolito.

## 15. Para repasar

- ¿Qué problema resuelve un API Gateway?
- ¿Qué es un predicado y qué es un filtro?
- ¿Por qué el gateway no debe tener lógica de negocio?
- ¿Por qué no se incluye `spring-boot-starter-web` en el gateway?
- ¿Qué rutas expone el gateway hoy?
- ¿Qué ocurre con las APIs `/internal/v1/**`?
- ¿Por qué se centraliza CORS en el gateway?
- ¿Por qué se deshabilita CSRF?
- ¿Para qué sirve `X-Correlation-Id`?
- ¿Por qué no Eureka en esta etapa?
- ¿Qué pasa si el gateway se cae?

## 16. Enlaces relacionados

- [ADR-005](../docs/arquitectura/adr-005-gateway-borde.md)
- [Configuración del gateway](../platform/api-gateway/src/main/resources/application.yml)
- [Seguridad del gateway](../platform/api-gateway/src/main/java/com/patitosrl/gateway/config/SecurityConfig.java)
- [Filtro de correlación](../platform/api-gateway/src/main/java/com/patitosrl/gateway/filter/CorrelationIdFilter.java)
- [Manejo de errores](../platform/api-gateway/src/main/java/com/patitosrl/gateway/error/GatewayErrorHandler.java)
