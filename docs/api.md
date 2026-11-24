# API web · Archivo Digital

Base URL en local: `http://localhost:8080`. Todas las requests y responses son JSON.

**Errores:**

| Status | Cuándo | Body |
|---|---|---|
| `400 Bad Request` | El body no pasa la validación (por ejemplo, un nombre vacío). | `{ "error": "Invalid request" }` |
| `404 Not Found` | El recurso pedido no existe. | `{ "error": "Piece 99 not found" }` |
| `400 Bad Request` | El request está mal formado: JSON inválido, fecha inválida (`"1978-13-01"`) o un id que no es número (`/pieces/abc`). | Formato por defecto de Spring Boot: `{ "timestamp": "...", "status": 400, "error": "Bad Request", "path": "/pieces" }` |

Los dos primeros los arma nuestro `GlobalExceptionHandler`. El tercero lo resuelve Spring antes de llegar al controller.

---|---|
| `400 Bad Request` | El body no pasa la validación (por ejemplo, un nombre vacío). |
| `404 Not Found` | El recurso pedido no existe. |

---

## Piezas (Digitalización)

Una **pieza** es el material físico del archivo.

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | número | — (lo genera el sistema) | Identificador. |
| `name` | texto | sí | Nombre de la pieza. |
| `description` | texto | no | Descripción libre. |
| `date` | fecha `AAAA-MM-DD` | no | Fecha del material original. |

### Crear una pieza

`POST /pieces`

Request:

```json
{ "name": "Carta de 1978", "description": "Manuscrita", "date": "1978-06-25" }
```

Response `201 Created`:

```json
{ "id": 1, "name": "Carta de 1978", "description": "Manuscrita", "date": "1978-06-25" }
```

Errores: `400` si falta `name` o está vacío.

### Listar piezas

`GET /pieces`

Response `200 OK`:

```json
[
  { "id": 1, "name": "Carta de 1978", "description": "Manuscrita", "date": "1978-06-25" }
]
```

### Obtener una pieza

`GET /pieces/{id}`

Response `200 OK`:

```json
{ "id": 1, "name": "Carta de 1978", "description": "Manuscrita", "date": "1978-06-25" }
```

Errores: `404` si no existe, con `{ "error": "Piece 99 not found" }`.

---

## Tags (Catalogación)

Un **tag** es una palabra clave administrable que clasifica digitalizaciones. Se seleccionan de un conjunto cerrado que el administrador puede cambiar en tiempo de ejecución.

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | número | — (lo genera el sistema) | Identificador único del tag. |
| `name` | texto | sí | Nombre único de la etiqueta. |

### Crear un tag

`POST /tags`

Request:

```json
{ "name": "dictadura" }
```

Response `201 Created`:

```json
{ "id": 1, "name": "dictadura" }
```

Errores:
- `400 Bad Request` si falta `name` o está vacío: `{ "error": "Invalid request" }`.
- `400 Bad Request` si el tag ya existe: `{ "error": "Tag 'dictadura' already exists" }`.

### Listar tags

`GET /tags`

Response `200 OK`:

```json
[
  { "id": 1, "name": "dictadura" }
]
```

### Obtener un tag

`GET /tags/{id}`

Response `200 OK`:

```json
{ "id": 1, "name": "dictadura" }
```

Errores: `404 Not Found` si no existe, con `{ "error": "Tag 99 not found" }`.

### Eliminar un tag

`DELETE /tags/{id}`

Response `204 No Content` (sin body).

Errores: `404 Not Found` si no existe, con `{ "error": "Tag 99 not found" }`.

---

## Digitalizaciones (Digitalización)

Una **digitalización** es una URL a un recurso digital con una descripción, asociada a una pieza existente.

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | número | — (lo genera el sistema) | Identificador único de la digitalización. |
| `pieceId` | número | — (viene en la URL) | Identificador de la pieza a la que pertenece. |
| `url` | texto | sí | URL del recurso digital. |
| `description` | texto | no | Descripción de la digitalización. |

### Asociar una digitalización a una pieza

`POST /pieces/{pieceId}/digitizations`

Request:

```json
{ "url": "https://archivo.org/carta-1978-frente.jpg", "description": "Escaneo frente" }
```

Response `201 Created`:

```json
{
  "id": 1,
  "pieceId": 1,
  "url": "https://archivo.org/carta-1978-frente.jpg",
  "description": "Escaneo frente"
}
```

Errores:
- `400 Bad Request` si falta `url` o está vacío: `{ "error": "Invalid request" }`.
- `404 Not Found` si la pieza no existe: `{ "error": "Piece 99 not found" }`.

### Listar digitalizaciones de una pieza

`GET /pieces/{pieceId}/digitizations`

Response `200 OK`:

```json
[
  {
    "id": 1,
    "pieceId": 1,
    "url": "https://archivo.org/carta-1978-frente.jpg",
    "description": "Escaneo frente"
  }
]
```

Errores: `404 Not Found` si la pieza no existe, con `{ "error": "Piece 99 not found" }`.

### Obtener una digitalización

`GET /digitizations/{id}`

Response `200 OK`:

```json
{
  "id": 1,
  "pieceId": 1,
  "url": "https://archivo.org/carta-1978-frente.jpg",
  "description": "Escaneo frente"
}
```

Errores: `404 Not Found` si no existe, con `{ "error": "Digitization 99 not found" }`.

---

_Pendiente: catalogación de digitalizaciones con tags (`DS-17`) y búsqueda (`DS-19`). Se agregan con cada card._
