# Secuencia de prueba completa · Archivo Digital (`DS-27`)

Guía paso a paso para validar de punta a punta el funcionamiento del sistema (camino feliz y validaciones clave) sobre la base en memoria H2.

Ver detalle completo de contratos y respuestas en [`docs/api.md`](api.md).

---

## 0. Levantar la aplicación en local

```bash
./mvnw spring-boot:run
```

La aplicación inicia en `http://localhost:8080` con la base H2 limpia (`create-drop`).

---

## 1. Camino feliz completo (en orden)

### Paso 1 · Crear tags administrables en runtime (`POST /tags`)

```bash
curl -i -X POST http://localhost:8080/tags \
  -H "Content-Type: application/json" \
  -d '{"name":"dictadura"}'

curl -i -X POST http://localhost:8080/tags \
  -H "Content-Type: application/json" \
  -d '{"name":"manuscrito"}'
```

**Esperado:** ambos devuelven `201 Created` (`id: 1` e `id: 2`).

Consultar el catálogo (`GET /tags`):

```bash
curl -s http://localhost:8080/tags
```

---

### Paso 2 · Crear piezas físicas (`POST /pieces`)

```bash
curl -i -X POST http://localhost:8080/pieces \
  -H "Content-Type: application/json" \
  -d '{"name":"Carta de 1978","description":"Manuscrita","date":"1978-06-25"}'

curl -i -X POST http://localhost:8080/pieces \
  -H "Content-Type: application/json" \
  -d '{"name":"Foto de 1982","description":"Plaza de Mayo","date":"1982-04-02"}'
```

**Esperado:** ambas devuelven `201 Created` (`id: 1` e `id: 2`).

---

### Paso 3 · Asociar digitalizaciones a cada pieza (`POST /pieces/{pieceId}/digitizations`)

```bash
curl -i -X POST http://localhost:8080/pieces/1/digitizations \
  -H "Content-Type: application/json" \
  -d '{"url":"https://archivo.org/carta-1978-frente.jpg","description":"Escaneo frente"}'

curl -i -X POST http://localhost:8080/pieces/1/digitizations \
  -H "Content-Type: application/json" \
  -d '{"url":"https://archivo.org/carta-1978-dorso.jpg","description":"Escaneo dorso"}'

curl -i -X POST http://localhost:8080/pieces/2/digitizations \
  -H "Content-Type: application/json" \
  -d '{"url":"https://archivo.org/foto-1982.jpg","description":"Copia digital"}'
```

**Esperado:** devuelven `201 Created` (`id: 1`, `2` y `3`), vinculadas a sus respectivas piezas (`pieceId`).

---

### Paso 4 · Catalogar las digitalizaciones con tags (`PUT /pieces/{pieceId}/digitizations/{digitizationId}/tags`)

Asociamos `"dictadura"` (`1`) y `"manuscrito"` (`2`) a la digitalización `1` (frente de la carta), `"manuscrito"` (`2`) a la digitalización `2` (dorso de la carta), y `"dictadura"` (`1`) a la digitalización `3` (foto):

```bash
curl -i -X PUT http://localhost:8080/pieces/1/digitizations/1/tags \
  -H "Content-Type: application/json" \
  -d '{"tagIds":[1,2]}'

curl -i -X PUT http://localhost:8080/pieces/1/digitizations/2/tags \
  -H "Content-Type: application/json" \
  -d '{"tagIds":[2]}'

curl -i -X PUT http://localhost:8080/pieces/2/digitizations/3/tags \
  -H "Content-Type: application/json" \
  -d '{"tagIds":[1]}'
```

**Esperado:** devuelven `200 OK` con las digitalizaciones actualizadas y su array `tags`.

---

### Paso 5 · Buscar piezas con filtros combinables (`GET /pieces`)

```bash
# 1) Por un tag (devuelve solo "Carta de 1978", una sola vez aunque tenga 2 digitalizaciones con ese tag)
curl -s "http://localhost:8080/pieces?tag=manuscrito"

# 2) Por múltiples tags con lógica OR (devuelve "Carta de 1978" y "Foto de 1982")
curl -s "http://localhost:8080/pieces?tag=dictadura&tag=manuscrito"

# 3) Combinando nombre + fecha + tag (lógica AND entre distintos filtros)
curl -s "http://localhost:8080/pieces?name=carta&date=1978-06-25&tag=dictadura"
```

**Esperado:** todas devuelven `200 OK` con objetos `Piece` (no digitalizaciones) y sin resultados duplicados.

---

### Paso 6 · Eliminar un tag ya asociado sin romper el sistema (`DELETE /tags/{id}`)

```bash
curl -i -X DELETE http://localhost:8080/tags/2

curl -s http://localhost:8080/pieces/1/digitizations
```

**Esperado:**
- `DELETE /tags/2` devuelve `204 No Content` (desasocia automáticamente el tag `"manuscrito"` de las digitalizaciones `1` y `2` antes de borrarlo).
- `GET /pieces/1/digitizations` sigue respondiendo `200 OK`, mostrando la digitalización `1` solo con el tag `"dictadura"` y la `2` con `tags: []`.

---

### Paso 7 · Visualización desde el Bot de Telegram (`DS-20`)

Una vez levantado el Bot de Telegram (`DS-20`), ejecutar el comando de búsqueda configurado (por ejemplo por nombre o tag `"dictadura"`) y verificar que devuelva de forma legible la pieza `"Carta de 1978"` y/o `"Foto de 1982"` consumiendo `GET /pieces`.

---

## 2. Validaciones de error clave (opcional)

```bash
# URL con formato inválido -> 400 Bad Request {"error":"Invalid request"}
curl -i -X POST http://localhost:8080/pieces/1/digitizations \
  -H "Content-Type: application/json" \
  -d '{"url":"texto-invalido","description":"Test"}'

# Pieza inexistente -> 404 Not Found {"error":"Piece 99 not found"}
curl -i -X POST http://localhost:8080/pieces/99/digitizations \
  -H "Content-Type: application/json" \
  -d '{"url":"https://archivo.org/test.jpg","description":"Test"}'

# Tag inexistente al catalogar -> 404 Not Found {"error":"Tag 99 not found"} (no modifica tags previos)
curl -i -X PUT http://localhost:8080/pieces/1/digitizations/1/tags \
  -H "Content-Type: application/json" \
  -d '{"tagIds":[1,99]}'
```
