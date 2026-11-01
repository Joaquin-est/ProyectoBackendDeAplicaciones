# Patito Backend — Microservicios

Esqueleto inicial del backend de **Técnica Patito S.R.L.**, separado por bounded contexts.

> Estado: **estructura base con placeholders y TODOs**. La lógica de negocio todavía no está implementada.

## Módulos

| Módulo | Rol | Puerto |
|---|---|---:|
| `platform/api-gateway` | Punto de entrada, ruteo y seguridad de borde | 8080 |
| `services/ms-clientes` | Clientes y domicilios | 8082 |
| `services/ms-tecnicos` | Técnicos, certificaciones, herramientas y zonas | 8083 |
| `services/ms-solicitudes` | Solicitudes, ofertas, presupuestos, estados e historial | 8084 |
| `libs/common-web` | Errores, ProblemDetail, correlación y paginación | — |
| `libs/security-jwt` | Conversión de roles y configuración JWT | — |
| `libs/geo-client` | Integración con mapas/rutas | — |
| `libs/test-support` | Utilidades de testing | — |

## Build

```powershell
.\mvnw.cmd -B verify   # Windows
./mvnw -B verify       # Linux/macOS
```

## Docker

```powershell
docker compose -f infra/compose/docker-compose.yml up --build
```

## Decisiones de arquitectura

- [Decisiones explicadas (guía de estudio)](decisiones/README.md)
- [Mapa de decisiones](decisiones/mapa-decisiones.md)
- [Registro consolidado de decisiones](docs/arquitectura/decisiones.md)
- [ADRs](docs/arquitectura/README.md)
- [Contratos OpenAPI](contracts/openapi/)

## Documentación

- `decisiones/`: guía extendida de decisiones, glosario, mapa y preguntas de repaso.
- `docs/arquitectura/`: ADRs, registro de decisiones y diagramas C4.
- `contracts/openapi/`: contratos OpenAPI.
- `docs/materialClase/` y `docs/proyectoMateria/`: material local de la materia. Los PDFs no se versionan; están ignorados por `.gitignore`.
