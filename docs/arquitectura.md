# Arquitectura del sistema · Archivo Digital

Documento correspondiente a la card **DS-23 · Diagrama(s) de arquitectura**, alineado con el enunciado del TP Integrador, las convenciones en [`AGENTS.md`](../AGENTS.md) y el modelo en capas de Fowler visto en clase.

Tablero editable en Figma (FigJam): [DS-23 Arquitectura - Archivo Digital (UNSAM)](https://www.figma.com/board/XlGeJOCSq67QyzZ8mdJdir)

---

## 1. Vista general (Contexto y Contenedores)

La aplicación se resuelve como un **monolito** en Java 21 + Spring Boot con una **única base de datos relacional (H2)**. Las funcionalidades de Digitalización, Catalogación y Disponibilización se exponen como APIs REST (sin pantallas web), y la Visualización se resuelve mediante un **Bot de Telegram** que consume la API de búsqueda.

```mermaid
flowchart LR
    operador(["Operador / Cliente HTTP"])
    usuario(["Usuario Final"])
    telegramApp["Telegram"]

    subgraph archivoDigital ["Sistema Archivo Digital (Monolito)"]
        botTelegram["Bot de Telegram"]
        apiSpring["Aplicacion Spring Boot (Java 21)"]
        dbH2[("H2 / SQL")]
    end

    operador -->|"HTTP REST / JSON"| apiSpring
    usuario <-->|"Chat / Comandos"| telegramApp
    telegramApp <-->|"Bot API"| botTelegram
    botTelegram -->|"Consume API de busqueda"| apiSpring
    apiSpring -->|"Lee y guarda (JPA)"| dbH2
```

---

## 2. Arquitectura interna (Capas de Fowler)

Dentro del monolito (`ar.edu.unsam.ddso.tp`), las clases se organizan por capa respetando las responsabilidades vistas en clase:

- **Presentación (`controller` + `TelegramBot`):** recibe pedidos HTTP o mensajes de Telegram, valida/deserializa DTOs (`dto`) y delega en el `service`. No contiene lógica de negocio.
- **Aplicación / Orquestación (`service`):** orquesta los casos de uso y límites transaccionales (`@Transactional`), busca entidades en los repositorios, invoca las reglas del dominio y mapea a DTOs de respuesta.
- **Dominio (`model`):** entidades `@Entity` (`Piece`, `Digitization`, `Tag`) con sus atributos, relaciones e invariantes de negocio.
- **Fuente de Datos (`repository`):** interfaces Spring Data JPA (`JpaRepository`) que resuelven la persistencia y consultas sobre la base relacional H2.

```mermaid
flowchart LR
    subgraph capPresentacion ["Capa de Presentacion (controller)"]
        bot["TelegramBot"]
        pieceCtrl["PieceController"]
        digCtrl["DigitizationController"]
        tagCtrl["TagController"]
        searchCtrl["SearchController"]
    end

    subgraph capService ["Capa de Aplicacion (service)"]
        pieceSvc["PieceService"]
        digSvc["DigitizationService"]
        tagSvc["TagService"]
        searchSvc["SearchService"]
    end

    subgraph capDominio ["Capa de Dominio (model)"]
        pieceModel["Piece"]
        digModel["Digitization"]
        tagModel["Tag"]
    end

    subgraph capDatos ["Capa de Fuente de Datos (repository)"]
        pieceRepo["PieceRepository"]
        digRepo["DigitizationRepository"]
        tagRepo["TagRepository"]
        h2db[("H2 / SQL")]
    end

    bot -->|"HTTP GET"| searchCtrl
    pieceCtrl --> pieceSvc
    digCtrl --> digSvc
    tagCtrl --> tagSvc
    searchCtrl --> searchSvc

    pieceSvc --> pieceModel
    pieceSvc --> pieceRepo
    digSvc --> digModel
    digSvc --> digRepo
    digSvc --> pieceRepo
    tagSvc --> tagModel
    tagSvc --> tagRepo
    tagSvc --> digRepo
    searchSvc --> pieceRepo

    pieceRepo & digRepo & tagRepo --> h2db
```
