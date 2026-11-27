# Diagramas de secuencia con capas de la arquitectura · Archivo Digital

Documento correspondiente a la card **DS-25 · Diagramas de secuencia con capa de cada clase**, perteneciente a la épica **Documentación /docs**.

El objetivo de este documento es representar los flujos más importantes del sistema, explicitando a qué capa del modelo de capas de Martin Fowler pertenece cada clase participante.

---

## 1. Mapeo de clases a las capas de Fowler

Siguiendo el diseño arquitectónico del monolito (`ar.edu.unsam.ddso.tp`), las clases se distribuyen en cuatro capas bien diferenciadas:

| Capa (Fowler) | Paquete / Componentes | Clases participantes en los diagramas | Responsabilidad principal |
|---|---|---|---|
| **Presentación** | `controller`, `dto`, `exception` | `PieceController`, `TagController`, `DigitizationController`, `GlobalExceptionHandler`, Request/Response DTOs | Punto de entrada HTTP REST. Deserializa requests, valida contratos (`@Valid`), delega en el Service y traduce respuestas/excepciones a códigos de estado HTTP. **No contiene reglas de negocio.** |
| **Aplicación (Servicio)** | `service` | `PieceService`, `TagService`, `DigitizationService` | Orquesta los casos de uso del sistema. Maneja las fronteras transaccionales (`@Transactional`), coordina entidades y repositorios, ejecuta validaciones de negocio y mapea entidades a DTOs. |
| **Dominio** | `model` | `Piece`, `Digitization`, `Tag` | Entidades del negocio (`@Entity`). Encapsulan el estado, relaciones (`@ManyToOne`, `@ManyToMany`) e invariantes propias de cada concepto. |
| **Persistencia / Fuente de Datos** | `repository`, Base de datos | `PieceRepository`, `TagRepository`, `DigitizationRepository`, `H2 Database` | Interfaces Spring Data JPA (`JpaRepository`) que abstraen el acceso a datos y traducen operaciones y consultas JPQL a sentencias SQL sobre la base relacional H2. |

---

## 2. Flujo 1: Alta de una Pieza física (`POST /pieces`)

Este flujo representa la creación de un nuevo material físico en el archivo (`DS-11`).

```mermaid
sequenceDiagram
    autonumber

    box rgba(220, 235, 252, 0.5) Capa de Presentacion
        actor Op as Operador / Cliente
        participant PC as PieceController
    end

    box rgba(225, 245, 225, 0.5) Capa de Aplicacion
        participant PS as PieceService
    end

    box rgba(255, 248, 220, 0.5) Capa de Dominio
        participant PM as Piece (Entity)
    end

    box rgba(253, 230, 230, 0.5) Capa de Persistencia
        participant PR as PieceRepository
        participant DB as H2 Database
    end

    Op->>+PC: POST /pieces (PieceCreateRequestDTO)
    Note over PC: Spring valida @Valid (nombre obligatorio)
    PC->>+PS: create(request)
    Note over PS: Inicia transaccion @Transactional

    PS->>+PM: new Piece(null, name, description, date)
    PM-->>-PS: piece

    PS->>+PR: save(piece)
    PR->>+DB: INSERT INTO pieces (name, description, piece_date) VALUES (...)
    DB-->>-PR: registro creado con ID generado
    PR-->>-PS: savedPiece

    Note over PS: PieceResponseDTO.from(savedPiece)
    PS-->>-PC: PieceResponseDTO
    PC-->>-Op: 201 Created (PieceResponseDTO JSON)
```

### Paso a paso del flujo:
1. El **Operador** envía un request HTTP `POST /pieces` con el cuerpo JSON correspondiente.
2. `PieceController` (Presentación) valida las anotaciones de validación (`@NotBlank`). Si es válido, invoca a `PieceService.create(...)`.
3. `PieceService` (Aplicación) ejecuta bajo contexto `@Transactional`: instancia la entidad `Piece` con los datos del request.
4. `PieceService` solicita persistir la entidad invocando `PieceRepository.save(piece)`.
5. `PieceRepository` (Persistencia) traduce la entidad a una sentencia `INSERT SQL` en la base H2.
6. H2 genera la clave primaria autoincremental y confirma la persistencia.
7. `PieceService` transforma la entidad persistida a `PieceResponseDTO` mediante el método estático de fábrica `from(...)`.
8. `PieceController` responde con código HTTP `201 Created` y el recurso recién creado en formato JSON.

---

## 3. Flujo 2: Alta de Tag con validación de duplicados (`POST /tags`)

Este flujo documenta la administración en tiempo de ejecución de las etiquetas cerradas (`DS-16`), mostrando tanto el camino exitoso como el tratamiento de excepciones de negocio ante nombres duplicados.

```mermaid
sequenceDiagram
    autonumber

    box rgba(220, 235, 252, 0.5) Capa de Presentacion
        actor Op as Operador
        participant TC as TagController
        participant GEH as GlobalExceptionHandler
    end

    box rgba(225, 245, 225, 0.5) Capa de Aplicacion
        participant TS as TagService
    end

    box rgba(255, 248, 220, 0.5) Capa de Dominio
        participant TM as Tag (Entity)
    end

    box rgba(253, 230, 230, 0.5) Capa de Persistencia
        participant TR as TagRepository
        participant DB as H2 Database
    end

    Op->>+TC: POST /tags (TagCreateRequestDTO)
    Note over TC: Valida @Valid (name no blanco)
    TC->>+TS: create(request)
    Note over TS: Normaliza: tagName = request.name().trim()

    TS->>+TR: existsByNameIgnoreCase(tagName)
    TR->>+DB: SELECT COUNT(*) > 0 FROM tags WHERE LOWER(name) = LOWER(?)
    DB-->>-TR: boolean (resultado)
    TR-->>-TS: existe?

    alt Tag ya existente (Regla de negocio)
        Note over TS: Lanza BadRequestException("Tag already exists")
        TS-->>TC: lanza BadRequestException
        TC-->>+GEH: captura BadRequestException
        GEH-->>-Op: 400 Bad Request (ErrorResponseDTO)
    else Tag no existente (Camino feliz)
        TS->>+TM: new Tag(null, tagName)
        TM-->>-TS: tag
        TS->>+TR: save(tag)
        TR->>+DB: INSERT INTO tags (name) VALUES (?)
        DB-->>-TR: tag persistido con ID
        TR-->>-TS: savedTag
        Note over TS: TagResponseDTO.from(savedTag)
        TS-->>TC: TagResponseDTO
        TC-->>-Op: 201 Created (TagResponseDTO JSON)
    end
```

### Decisiones de diseño en este flujo:
- **Validación insensible a mayúsculas:** `existsByNameIgnoreCase` evita tener duplicados semánticos como `"Fotografía"` y `"fotografía"`.
- **Manejo uniforme de errores:** Cuando el servicio lanza `BadRequestException`, la capa de Presentación captura la excepción mediante `@RestControllerAdvice` (`GlobalExceptionHandler`), asegurando que el cliente reciba un código HTTP `400 Bad Request` estructurado.

---

## 4. Flujo 3: Catalogación de una Digitalización con Tags (`PUT .../tags`)

Este flujo documenta la asociación y reemplazo atómico de etiquetas sobre una digitalización existente (`DS-14` y `DS-17`).

```mermaid
sequenceDiagram
    autonumber

    box rgba(220, 235, 252, 0.5) Capa de Presentacion
        actor Op as Operador
        participant DC as DigitizationController
    end

    box rgba(225, 245, 225, 0.5) Capa de Aplicacion
        participant DS as DigitizationService
    end

    box rgba(255, 248, 220, 0.5) Capa de Dominio
        participant DM as Digitization (Entity)
    end

    box rgba(253, 230, 230, 0.5) Capa de Persistencia
        participant DR as DigitizationRepository
        participant TR as TagRepository
        participant DB as H2 Database
    end

    Op->>+DC: PUT /pieces/{pieceId}/digitizations/{digitizationId}/tags (tagIds)
    DC->>+DS: replaceTags(pieceId, digitizationId, request)
    Note over DS: Inicia transaccion @Transactional

    DS->>+DR: findById(digitizationId)
    DR->>+DB: SELECT * FROM digitizations WHERE id = ?
    DB-->>-DR: registro
    DR-->>-DS: Optional<Digitization>
    Note over DS: Si no existe -> lanza NotFoundException

    Note over DS: Valida integracion: digitization.getPiece().getId() == pieceId

    loop Por cada tagId en request.tagIds()
        DS->>+TR: findById(tagId)
        TR->>+DB: SELECT * FROM tags WHERE id = ?
        DB-->>-TR: registro
        TR-->>-DS: Optional<Tag>
        Note over DS: Si no existe -> lanza NotFoundException
    end

    DS->>+DM: setTags(listaTagsVerificados)
    DM-->>-DS: ok (coleccion actualizada)

    DS->>+DR: save(digitization)
    DR->>+DB: Sincroniza tabla intermedia digitization_tags (DELETE + INSERT)
    DB-->>-DR: ok
    DR-->>-DS: savedDigitization

    Note over DS: DigitizationResponseDTO.from(savedDigitization)
    DS-->>-DC: DigitizationResponseDTO
    DC-->>-Op: 200 OK (DigitizationResponseDTO JSON)
```

### Puntos clave del diseño:
- **Validación atómica de tags:** Cada tag en la lista es verificado en `TagRepository`. Si al menos uno no existe, la transacción se aborta con `NotFoundException`, garantizando consistencia.
- **Validación de pertenencia jerárquica:** Se verifica que la digitalización realmente pertenezca a la pieza indicada en la URL (`pieceId`), evitando inconsistencias lógicas en rutas anidadas.
- **Mapeo relacional en H2:** La relación `@ManyToMany` entre `Digitization` y `Tag` sincroniza automáticamente la tabla de unión `digitization_tags`.

---

## 5. Flujo 4: Búsqueda de Piezas por filtros y etiquetas (`GET /pieces?tag=...`)

Este flujo documenta la disponibilización y búsqueda multicriterio (`DS-19`), consumida tanto por clientes HTTP REST como por el Bot de Telegram.

```mermaid
sequenceDiagram
    autonumber

    box rgba(220, 235, 252, 0.5) Capa de Presentacion
        actor Cl as Cliente REST / Bot Telegram
        participant PC as PieceController
    end

    box rgba(225, 245, 225, 0.5) Capa de Aplicacion
        participant PS as PieceService
    end

    box rgba(253, 230, 230, 0.5) Capa de Persistencia
        participant PR as PieceRepository
        participant DB as H2 Database
    end

    Cl->>+PC: GET /pieces?tag=historia&tag=carta (name, description, date, tag)
    PC->>+PS: search(name, description, date, tags)

    Note over PS: Normaliza tags: trim, minúsculas, distinct y elimina nulos/vacios

    alt Lista de tags no vacia
        PS->>+PR: searchByTags(name, description, date, tagsNormalizados)
        PR->>+DB: SELECT DISTINCT p FROM pieces p WHERE ... AND EXISTS (...)
        DB-->>-PR: List<Piece>
        PR-->>-PS: pieces
    else Sin filtro de tags
        PS->>+PR: search(name, description, date)
        PR->>+DB: SELECT p FROM pieces p WHERE (:name IS NULL OR ...)
        DB-->>-PR: List<Piece>
        PR-->>-PS: pieces
    end

    Note over PS: Transforma lista de entidades a List<PieceResponseDTO>
    PS-->>-PC: List<PieceResponseDTO>
    PC-->>-Cl: 200 OK (List<PieceResponseDTO> JSON)
```

### Puntos clave del diseño:
- **Lógica de tags normalizada:** La capa de Servicio asegura que las cadenas se limpien y pasen en minúsculas para que la búsqueda sea insensible a mayúsculas/minúsculas.
- **Uso de `EXISTS` en la capa de datos:** El repositorio ejecuta una subconsulta `EXISTS` sobre las digitalizaciones y sus tags vinculados, evitando que una pieza se duplique en el resultado si varias de sus digitalizaciones coinciden con los tags buscados.
- **Desacoplamiento:** El cliente sólo recibe DTOs limpios con la información requerida, sin filtrar detalles internos de la base de datos.
