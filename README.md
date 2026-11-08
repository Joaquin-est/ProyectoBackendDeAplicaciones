# ProyectoBackendDeAplicaciones

# Módulo commons-geo (Librería Compatida)
# Propósito: Centralizar la conexión con la API externa de mapas (OSRM/Goole Maps).
# Uso: ms-solicitudes lo incluye para calcular la distancia real de la ruta (no en línea recta).

# Módulo api-gateway (Puerta de Entrada)
# Propósito: Recibir todas las peticiones, validar el token JWT con Keycloak y enrutar al microservicio correcto.
# Archivo clave:
# 1-GatewayConfig.java: Define las rutas (/api/tecnicos/** -> puerto 8082, etc.).
# 2-GatewaySecurityConfig.java: Configura Spring Security reactivo para validar el JWT.

# Módulo ms-clientes (Padrón de Clientes)
# Propósito: CRUD de clientes con sus coordenadas.
# Archivos clave:
# 1 - Cliente.java: Entidad JPA.
# 2 - Cliente.java: Lógica de negocio (valida DNI duplicado).
# 3 - ClientesController.java: Endpoints REST.
# 4 - SecurityConfig.java: Configuración de seguridad (valida JWT y extrae roles).

# Módulo ms-tecnicos (Patrón de Técnicos)
# Propósito: CRUD de técnicos con sus certificaciones y zonas de cobertura.
# Archivos clave:
# TecnicoService.java: Lógica de negocio (implementa la fórmula de Haversine para filtrar técnicos a menos de 13 km).
# TecnicoController.java: Endponits REST (incluye /candidatos).
# DataLoader.java: Carga datos de prueba al iniciar.

# Módulo ms-solicitudes (El Orquestador)
# Propósito: Es el corazón del sistema. Gestiona el ciclo de vida de la solicitud.
# SolicitudService.java: Lógica de creación, aprobación de presupuesto, cancelación y finalización.
# AsignaciónService.java: Lógica de asignación (ofertar, aceptar, rechazar). Usa GeoService y el patrón Strategy.
# OfertaScheduler.java: Tarea Programada (@Scheduled) que cada minuto revisa si alguna oferta expiró (los 15 minutos).
# CostoService.java + CostoVisitaStrategy.java: Patrón Strategy para calcular el costo de visita según prioridad y horario.
# ClienteClient.java y TecnicosCliente.java: Llamadas REST a otros microservicios usando RestClient.