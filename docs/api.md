# API web · Archivo Digital

Base URL en local: `http://localhost:8080`. Todas las requests y responses son JSON.

**Errores:** siempre devuelven el mismo formato:

```json
{ "error": "mensaje" }
```

| Status | Cuándo |
|---|---|
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

_Pendiente: digitalizaciones, tags (catalogación) y búsqueda (disponibilización). Se agregan con cada card._
