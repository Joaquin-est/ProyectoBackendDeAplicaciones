# ProyectoBackendDeAplicaciones - Técnica Patito S.R.L.

Backend desarrollado con arquitectura de microservicios para la gestión de servicios técnicos domiciliarios.

---

## Estructura del Proyecto (Módulos)

El backend está compuesto por 5 módulos Maven independientes:

### 1. commons-geo (Librería Compartida)
*   **Propósito:** Centralizar la conexión con la API externa de mapas (OSRM/Google Maps).
*   **Uso:** `ms-solicitudes` lo incluye para calcular la distancia real de la ruta (no en línea recta).

### 2. api-gateway (Puerta de Entrada)
*   **Propósito:** Recibir todas las peticiones, validar el token JWT con Keycloak y enrutar al microservicio correcto.
*   **Archivos clave:**
    *   `GatewayConfig.java`: Define las rutas (ej. `/api/tecnicos/**` → puerto 8082).
    *   `GatewaySecurityConfig.java`: Configura Spring Security reactivo para validar el JWT.

### 3. ms-clientes (Padrón de Clientes)
*   **Propósito:** CRUD de clientes con sus coordenadas.
*   **Archivos clave:**
    *   `Cliente.java`: Entidad JPA.
    *   `ClienteService.java`: Lógica de negocio (valida DNI duplicado).
    *   `ClienteController.java`: Endpoints REST.
    *   `SecurityConfig.java`: Configuración de seguridad (valida JWT y extrae roles).

### 4. ms-tecnicos (Padrón de Técnicos)
*   **Propósito:** CRUD de técnicos con sus certificaciones y zonas de cobertura.
*   **Archivos clave:**
    *   `TecnicoService.java`: Lógica de negocio (implementa la fórmula de Haversine para filtrar técnicos a menos de 13 km).
    *   `TecnicoController.java`: Endpoints REST (incluye `/candidatos`).
    *   `DataLoader.java`: Carga datos de prueba al iniciar.

### 5. ms-solicitudes (El Orquestador)
*   **Propósito:** Es el corazón del sistema. Gestiona el ciclo de vida de la solicitud.
*   **Archivos clave:**
    *   `SolicitudService.java`: Lógica de creación, aprobación de presupuesto, cancelación y finalización.
    *   `AsignacionService.java`: Lógica de asignación (ofertar, aceptar, rechazar). Usa `GeoService` y el patrón Strategy.
    *   `OfertaScheduler.java`: Tarea programada (`@Scheduled`) que cada minuto revisa si alguna oferta expiró (los 15 minutos).
    *   `CostoService.java` + `CostoVisitaStrategy.java`: Patrón Strategy para calcular el costo de visita según prioridad y horario.
    *   `ClientesClient.java` y `TecnicosClient.java`: Llamadas REST a otros microservicios usando `RestClient`.

---

## Cómo ejecutar los Tests

Para ejecutar toda la suite de tests del proyecto (todos los módulos), posicionate en la **raíz del proyecto** (donde está el `pom.xml` principal) y ejecutá:

```bash
mvn clean test

mvn clean test -pl ms-tecnicos -am (para ejecutar un modulo especifico)
