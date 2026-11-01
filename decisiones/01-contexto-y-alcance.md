# 01 — Contexto y alcance

> **Decisión en una frase:** construir el MVP del proceso central de Técnica Patito, dejando fuera inventario, facturación y pagos, y tratando el presupuesto como parte del ciclo de vida de la solicitud.

## 1. El dominio

Técnica Patito S.R.L. es una empresa de Córdoba que brinda servicios técnicos a domicilio para equipos electrónicos y de computación: diagnóstico, reparación, instalación y mantenimiento.

La empresa creció en clientes y técnicos, pero sigue trabajando con mensajería, llamadas y planillas. Eso produce problemas concretos:

- se asignan técnicos sin las herramientas o certificaciones necesarias;
- las solicitudes quedan sin respuesta cuando un técnico demora;
- no se sabe si el cliente aprobó el presupuesto antes de empezar;
- un pedido puede quedar asignado a dos técnicos;
- un técnico puede aceptar más trabajo del que puede cumplir;
- no hay control de acceso seguro por actor.

El sistema debe reemplazar esa operatoria por un backend accesible desde web/móvil.

## 2. Las dos áreas del negocio

El documento del proyecto distingue dos grandes áreas. Esta separación es la base de los bounded contexts y, por lo tanto, de los microservicios.

### Área maestra

Son los datos que se administran y se consultan:

- **Clientes:** nombre, apellido, DNI, teléfono, correo y domicilio con calle, número, barrio/zona y coordenadas.
- **Técnicos:** legajo, datos personales, ubicación base, disponibilidad y perfil profesional.
- **Certificaciones y conocimientos:** por ejemplo, microelectrónica, redes, impresoras o notebooks Apple.
- **Herramientas:** equipamiento con el que cuenta cada técnico.
- **Zonas de cobertura:** barrios de Córdoba donde trabaja cada técnico.

### Área transaccional

Es el ciclo de vida de una solicitud de servicio:

1. Un cliente registrado crea una solicitud.
2. Indica una o más categorías: reparación, instalación y/o mantenimiento.
3. Describe la falla e informa marca y modelo del equipo.
4. La solicitud queda en estado `Pendiente`.
5. El sistema busca técnicos disponibles, de la zona y con las certificaciones/herramientas necesarias.
6. Se ofrece al técnico más cercano.
7. El técnico tiene 15 minutos para aceptar o rechazar.
8. Si acepta, la solicitud pasa a `Asignada`.
9. El técnico se traslada, diagnostica y carga un presupuesto.
10. Si el cliente acepta el presupuesto, pasa a `EnCurso`; si lo rechaza, se cancela.
11. Al finalizar el trabajo, pasa a `Finalizado`.
12. Todo cambio de estado se registra en un historial con fecha, hora y observaciones.

## 3. Reglas de negocio que condicionan la arquitectura

| Regla | Implicancia técnica |
|---|---|
| Una solicitud puede tener una, dos o tres categorías | El modelo debe permitir múltiples tipos de servicio |
| El técnico debe estar disponible, cubrir la zona y tener la certificación | La elegibilidad requiere datos de técnicos, zonas y certificaciones |
| Ventana de aceptación de 15 minutos | Se necesita un timestamp de expiración y una tarea programada (`@Scheduled`) |
| Una solicitud no puede estar asignada a más de un técnico | Control de concurrencia y validación de estado antes de aceptar |
| Un técnico no puede tener más de una solicitud `EnCurso` | Validación al ofrecer/asignar |
| Puede tener hasta dos solicitudes en espera | Validación de cupo |
| Solo se ejecuta si el cliente aprueba el presupuesto | Estado intermedio y transición condicionada |
| Una solicitud tiene un único estado a la vez, pero historial completo | Entidad de historial separada de la solicitud |
| El costo de visita depende de la distancia | Integración con cálculo de rutas y costo por km |
| La cercanía se calcula por latitud/longitud; 13 km es el máximo | Filtro local con fórmula de Haversine antes de llamar a un mapa |

Estas reglas son las que justifican que `ms-solicitudes` sea el orquestador central y no un CRUD más.

## 4. Alcance del MVP

El compromiso del proyecto es el proceso central de punta a punta:

- autenticación de los tres roles: `CLIENTE`, `TECNICO`, `ADMIN`;
- CRUD de clientes y de técnicos con sus certificaciones y zonas;
- creación de solicitudes;
- consulta de clientes y domicilios desde `ms-solicitudes`;
- búsqueda y asignación de técnicos;
- ventana de 15 minutos;
- presupuesto y transición a `EnCurso`;
- historial de estados;
- observabilidad básica con logs y correlation ID.

### Fuera del MVP

- gestión de inventario y compra de repuestos;
- liquidación de haberes;
- facturación fiscal ante AFIP/ARCA;
- administración de pagos;
- notificaciones reales por correo o push: se simulan con registros en base de datos;
- pasarela de pagos;
- un módulo administrativo separado de presupuestos.

> El presupuesto **sí** forma parte del ciclo de vida de la solicitud. Lo que queda fuera es un sistema administrativo de presupuestos.

## 5. Decisiones de alcance tomadas

1. **Priorizar el flujo central sobre los CRUD aislados.** Los CRUD existen, pero el valor del sistema está en el ciclo de la solicitud.
2. **No implementar notificaciones reales en el MVP.** Se representan en la base para poder demostrar el flujo.
3. **No modelar pagos ni facturación.** El estado `Finalizado` cierra el trabajo técnico; el cobro real queda fuera.
4. **Mantener el presupuesto dentro de `ms-solicitudes`.** No se crea un microservicio de presupuestos.
5. **No agregar infraestructura que el MVP no justifica.** Sin Kafka, Eureka, Kubernetes ni service mesh.
6. **No implementar lógica de negocio en esta entrega.** Se entrega estructura, contratos, configuración y placeholders para que cada integrante complete su parte.

## 6. Cómo se traduce el alcance en módulos

| Módulo | Parte del dominio |
|---|---|
| `ms-clientes` | Clientes y domicilios |
| `ms-tecnicos` | Técnicos, certificaciones, herramientas y zonas |
| `ms-solicitudes` | Solicitudes, ofertas, presupuestos, estados e historial |
| `api-gateway` | Punto de entrada, ruteo y seguridad de borde |
| `libs/*` | Elementos transversales y utilidades |

La separación no es “una entidad por microservicio”. Es una separación por **responsabilidad de negocio**. Certificaciones, herramientas y zonas no justifican un microservicio propio porque pertenecen al mismo contexto: la gestión de técnicos.

## 7. Conexión con la materia

- **Apunte 14 — Diseño de APIs:** el alcance se expresa como operaciones sobre recursos (`clientes`, `domicilios`, `solicitudes`, `ofertas`). Los estados y las reglas se traducen en endpoints y códigos HTTP.
- **Apunte 17 — Elementos transversales:** la separación controller/service/repository/model/DTO permite que el alcance se implemente por capas y que las reglas de negocio queden en los servicios.
- **Apunte 19 — API Gateway:** el cliente no debe conocer los puertos internos; el alcance se expone como una API unificada.
- **Apunte 20 y 21 — Seguridad:** los roles del MVP se corresponden con los actores del dominio.

## 8. Qué está implementado y qué no

| Elemento | Estado |
|---|---|
| Módulos y puertos | Creados |
| Configuración de perfiles | Creada como base |
| Contratos OpenAPI | Placeholders |
| Entidades JPA | Placeholders |
| Controllers, services y repositories | Placeholders con `TODO` |
| Regla de 15 minutos con `@Scheduled` | Clase `OfertaExpiracionScheduler` como placeholder |
| Cálculo Haversine y ruta externa | Clases/paquetes preparados, sin lógica |
| Historial de estados | Entidad y repositorio preparados, sin lógica |
| Notificaciones simuladas | Pendiente de diseño e implementación |

## 9. Para repasar

- ¿Cuál es la diferencia entre el área maestra y el área transaccional?
- ¿Por qué `ms-solicitudes` es el orquestador y no un CRUD de solicitudes?
- ¿Qué reglas justifican la existencia de una oferta con expiración?
- ¿Por qué el presupuesto no se separa en otro microservicio?
- ¿Qué significa que el MVP no contemple pagos pero sí contemple presupuestos?
- ¿Qué decisiones de alcance reducen el riesgo de un proyecto demasiado grande?

## 10. Enlaces relacionados

- [02 — Arquitectura y bounded contexts](02-arquitectura-y-bounded-contexts.md)
- [04 — Datos y persistencia](04-datos-y-persistencia.md)
- [05 — Seguridad](05-seguridad.md)
- [11 — Decisiones pendientes](11-decisiones-pendientes.md)
- Documento del proyecto de la materia (material local, no versionado).
