# ARCHITECTURE.md

Documento transversal del proyecto **Archivo Digital** (Grupo Memoria Abierta · UNSAM). Complementa a [`AGENTS.md`](AGENTS.md) con el resumen del sistema y los enlaces a la documentación detallada dentro de [`docs/`](docs/).

## 1. Descripción general

Implementación acotada del sistema **Archivo Digital** en una arquitectura de **monolito** (único componente Spring Boot en Java 21 + única base de datos relacional H2) organizado siguiendo las **4 capas de Fowler** (`controller`, `service`, `model`, `repository`). Expone APIs REST para Digitalización, Catalogación y Disponibilización, y un Bot de Telegram para consultas.

## 2. Actores y sistemas

- **Operador / Archivista:** da de alta piezas físicas y asocia digitalizaciones y tags vía API REST.
- **Administrador:** administra el conjunto cerrado de tags en tiempo de ejecución vía API REST (`POST` / `GET` / `DELETE /tags`). *(Por definición de la cátedra, no requiere entidad `Usuario`/`Rol` ni login en el dominio).*
- **Usuario final:** realiza búsquedas de piezas mediante el **Bot de Telegram** (que consume los controladores HTTP a través de un adaptador).
- **Base de datos relacional (H2):** persiste piezas, digitalizaciones, tags y sus asociaciones.

## 3. Alcance y fuera de alcance

**Dentro de alcance:**
- Alta y consulta de piezas físicas (`Piece`).
- Asociación de digitalizaciones (`Digitization`) a piezas existentes.
- Administración en tiempo de ejecución del catálogo cerrado de etiquetas (`Tag`) y catalogación de digitalizaciones.
- Búsqueda de piezas por nombre, descripción, fecha y tags + Bot de Telegram desacoplado vía adaptador llamando a los controllers.

**Fuera de alcance:**
- Pantallas web / frontend gráfico.
- Autenticación, gestión de usuarios/roles o permisos.
- Almacenamiento binario de archivos (las digitalizaciones guardan URLs).
- Paginación obligatoria en búsquedas (confirmado por la cátedra que no es necesaria).

## 4. Glosario de dominio

| Término | Significado |
|---|---|
| `Piece` (Pieza) | Material físico del archivo (foto, carta, documento). Es el objeto principal que devuelven las búsquedas. |
| `Digitization` (Digitalización) | Recurso digital (URL + descripción) asociado a una única pieza física. |
| `Tag` (Etiqueta) | Palabra clave administrable en tiempo de ejecución para catalogar digitalizaciones. |

## 5. Documentación detallada en `/docs/`

- **Diagrama de arquitectura y capas de Fowler (`DS-23`):** [`docs/arquitectura.md`](docs/arquitectura.md)
- **Diagrama de clases de dominio (`DS-04` / `DS-22`):** [`docs/modelo-dominio.md`](docs/modelo-dominio.md)
- **Diagrama de base de datos (`DS-24`):** [`docs/base-de-datos.md`](docs/base-de-datos.md)
- **Especificación de la API REST:** [`docs/api.md`](docs/api.md)
- **Convenciones de código y testing:** [`AGENTS.md`](AGENTS.md)
