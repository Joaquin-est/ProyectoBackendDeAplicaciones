# 10 — Documentación y organización

> **Decisión en una frase:** documentar las decisiones y los contratos dentro del repositorio, mantener los PDFs fuera de Git y dejar los placeholders explícitos para que cada integrante sepa qué falta.

## 1. Documentación como parte del backend

En un proyecto de microservicios, la documentación no es un anexo: es parte del contrato de trabajo.

Sirve para:

- que una persona nueva entienda el sistema;
- que dos servicios no se desincronicen;
- que una decisión no se discuta dos veces;
- que el equipo sepa qué está implementado y qué es un TODO;
- que la defensa del trabajo tenga fundamentos.

## 2. Estructura de documentación

```text
README.md                         Punto de entrada
docs/
  arquitectura/
    README.md                     Índice de arquitectura
    decisiones.md                 Registro formal resumido
    adr-001...adr-005...          Decisiones estructurales
    c4-contexto.puml              Diagrama de contexto (placeholder)
    c4-contenedores.puml          Diagrama de contenedores (placeholder)
decisiones/
  README.md                       Esta guía didáctica
  01...11                       Explicaciones por tema
  glosario.md
  mapa-decisiones.md
  preguntas-de-repaso.md
contracts/openapi/                Contratos HTTP
infra/                            Compose, Keycloak, PostgreSQL, Prometheus
```

### ¿Por qué hay dos lugares con decisiones?

- `docs/arquitectura/` contiene el **registro formal**: ADRs y resumen para el repositorio.
- `decisiones/` contiene la **guía extendida**: explicación, contexto de la materia, ejemplos y preguntas.

No compiten: una es el registro normativo; la otra es el material de aprendizaje.

## 3. ADRs

Un ADR (Architecture Decision Record) documenta una decisión importante:

- **Contexto:** qué problema había.
- **Decisión:** qué se eligió.
- **Alternativas:** qué se descartó y por qué.
- **Consecuencias:** qué se gana y qué se pierde.
- **Estado:** propuesto, aceptado, reemplazado o rechazado.

Los ADRs del proyecto:

| ADR | Tema |
|---|---|
| 001 | Monorepo Maven multi-módulo |
| 002 | Database per Service |
| 003 | Keycloak + OAuth2 |
| 004 | Comunicación síncrona con `RestClient` |
| 005 | Gateway en el borde |

Si una decisión cambia, no se borra el ADR: se marca como reemplazado y se crea uno nuevo.

## 4. OpenAPI

`contracts/openapi/` contiene los contratos:

- `clientes-v1.yaml`;
- `tecnicos-v1.yaml`;
- `solicitudes-v1.yaml`;
- `internal/clientes-internal-v1.yaml`;
- `internal/tecnicos-internal-v1.yaml`.

Hoy son placeholders. La decisión es completarlos antes o junto con la implementación.

Un buen contrato incluye:

- rutas y métodos;
- request/response;
- códigos de error;
- esquemas;
- ejemplos;
- versión.

OpenAPI permite:

- generar documentación interactiva;
- probar desde Swagger UI;
- generar clientes;
- validar que el contrato y el código no se contradigan.

## 5. Diagramas C4

`docs/arquitectura/c4-contexto.puml` y `c4-contenedores.puml` son placeholders.

C4 propone cuatro niveles:

1. **Contexto:** el sistema y sus usuarios/sistemas externos.
2. **Contenedores:** aplicaciones, servicios y bases.
3. **Componentes:** piezas internas de un contenedor.
4. **Código:** clases y detalles.

Para este trabajo alcanza con contexto y contenedores bien hechos. Los diagramas deben reflejar:

- SPA;
- gateway;
- tres microservicios;
- Keycloak;
- PostgreSQL;
- API de mapas;
- flujo de tokens y llamadas internas.

## 6. PDFs y archivos binarios

Decisión: **no versionar PDFs**.

Se agregó a `.gitignore`:

```gitignore
# Material de clase / binarios pesados: no se versionan
*.pdf
```

Motivos:

- aumentan el tamaño del repositorio;
- no son necesarios para compilar;
- cambian poco y no aportan al diff;
- se pueden compartir por otro medio;
- el material de la materia ya está en el entorno local.

También se ignoran:

- `tools/pdf-to-txt/salida/` (textos extraídos);
- `tools/pdf-to-txt/node_modules/`;
- `.env`;
- `target/`;
- logs.

Los PDFs siguen en disco, pero Git no los va a subir. Si un PDF ya estaba trackeado antes de la regla, `*.pdf` no lo destrackea automáticamente: hay que ejecutar una vez:

```powershell
git rm --cached "ruta/al/archivo.pdf"
```

## 7. Placeholders y TODOs

El repositorio es un esqueleto. Eso significa:

- las clases existen para marcar la estructura;
- los métodos de negocio no están implementados;
- los contratos están vacíos;
- las migraciones son comentarios;
- la seguridad tiene clases vacías;
- los tests no existen todavía.

Un `TODO` debe ser:

- específico;
- ubicado cerca del código que falta;
- reemplazado por implementación o por una decisión documentada;
- revisado antes de cerrar una historia.

No se debe dejar un `TODO` genérico que nadie entienda.

## 8. Reglas de trabajo

1. No implementar lógica de negocio en el gateway.
2. No compartir entidades JPA entre servicios.
3. No acceder a la base de otro servicio.
4. No hardcodear URLs, usuarios ni secretos.
5. Completar el contrato OpenAPI antes o junto con el controller.
6. Agregar pruebas junto con la funcionalidad.
7. Actualizar ADR o guía si cambia una decisión.
8. No commitear PDFs, `target/`, `.env`, logs ni textos extraídos.
9. Mantener `mvn -B verify` verde.
10. Cada servicio es dueño de su esquema, sus migraciones y su contrato.

## 9. Cómo documentar una decisión nueva

1. Describir el problema.
2. Listar al menos dos alternativas.
3. Elegir una y justificarla.
4. Escribir el ADR.
5. Actualizar el mapa de decisiones.
6. Si hace falta, ampliar en esta carpeta.
7. Avisar al equipo.

Una decisión sin contexto se convierte en una regla arbitraria. El contexto es lo que permite revisarla cuando cambian las condiciones.

## 10. Conexión con la materia

- **Apunte 14 — Diseño de APIs:** OpenAPI como contrato legible por humanos y herramientas.
- **Apunte 17 — Transversales:** Swagger/springdoc, errores uniformes y validación.
- **Apunte 19 — Gateway:** los diagramas C4 ayudan a explicar el borde y las llamadas internas.
- **Apunte 21 — Spring Security:** documentar el flujo de tokens evita errores de configuración.

## 11. Trade-offs

- **Ventaja:** el equipo comparte el mismo modelo mental.
- **Ventaja:** menos retrabajo por decisiones repetidas.
- **Costo:** mantener la documentación actualizada.
- **Costo:** escribir ADRs lleva tiempo.
- **Riesgo:** documentación desactualizada es peor que no tener documentación.
- **Riesgo:** demasiados documentos pueden dispersar la información; por eso hay un mapa.

## 12. Para repasar

- ¿Qué es un ADR y qué partes tiene?
- ¿Qué diferencia hay entre el registro formal y esta guía?
- ¿Por qué se documentan las alternativas?
- ¿Qué debe contener un contrato OpenAPI?
- ¿Para qué sirven los diagramas C4?
- ¿Por qué no se versionan los PDFs?
- ¿Qué pasa si un PDF ya estaba trackeado?
- ¿Qué es un placeholder y cuándo debe reemplazarse?
- ¿Cuáles son las reglas de trabajo del equipo?

## 13. Enlaces relacionados

- [Registro formal de decisiones](../docs/arquitectura/decisiones.md)
- [ADRs](../docs/arquitectura/README.md)
- [Mapa de decisiones](mapa-decisiones.md)
- [README principal](../README.md)
- [.gitignore](../.gitignore)
