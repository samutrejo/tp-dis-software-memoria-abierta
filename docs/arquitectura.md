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

- **Presentación (`controller`):** recibe pedidos HTTP, valida/deserializa DTOs (`dto`) y delega en los servicios. No contiene lógica de negocio.
- **Aplicación / Orquestación (`service`):** orquesta los casos de uso y límites transaccionales (`@Transactional`), busca entidades en los repositorios, invoca las reglas del dominio y mapea a DTOs de respuesta.
- **Dominio (`model`):** entidades `@Entity` (`Piece`, `Digitization`, `Tag`) con sus atributos, relaciones e invariantes de negocio.
- **Fuente de Datos (`repository`):** interfaces Spring Data JPA (`JpaRepository`) que resuelven la persistencia y consultas sobre la base relacional H2.

### Integración del bot de Telegram

El bot se integra mediante el patrón Adapter para desacoplar la plataforma de mensajería de la lógica de búsqueda y de la API del sistema.

- **Adaptador de entrada (`TelegramBotAdapter`):** recibe las actualizaciones de Telegram y procesa los comandos.
- **Servicio del bot (`BotSearchService`):** coordina la búsqueda y prepara el mensaje de respuesta.
- **Puerto (`PieceSearchPort`):** define la operación de búsqueda que necesita el bot.
- **Adaptador de salida HTTP (`PieceApiHttpAdapter`):** implementa el puerto y consume `PieceController` mediante HTTP (`GET /pieces`). No invoca directamente a `PieceService`.

Esta separación permite reemplazar Telegram por otra plataforma de mensajería sin modificar la lógica de búsqueda del bot ni las capas internas de la API.

```mermaid
flowchart LR
    subgraph bot ["Integración del bot"]
        telegramAdapter["TelegramBotAdapter"]
        botService["BotSearchService"]
        port["PieceSearchPort"]
        httpAdapter["PieceApiHttpAdapter"]

        telegramAdapter --> botService
        botService --> port
        port -. "implementado por" .-> httpAdapter
    end

    subgraph api ["API del monolito"]
        subgraph controllers ["Capa de presentación"]
            pieceCtrl["PieceController"]
            digCtrl["DigitizationController"]
            tagCtrl["TagController"]
        end

        subgraph services ["Capa de aplicación"]
            pieceSvc["PieceService"]
            digSvc["DigitizationService"]
            tagSvc["TagService"]
        end

        subgraph models ["Dominio"]
            pieceModel["Piece"]
            digModel["Digitization"]
            tagModel["Tag"]
        end

        subgraph repositories ["Persistencia"]
            pieceRepo["PieceRepository"]
            digRepo["DigitizationRepository"]
            tagRepo["TagRepository"]
            h2db[("H2 / SQL")]
        end

        pieceCtrl --> pieceSvc
        digCtrl --> digSvc
        tagCtrl --> tagSvc

        pieceSvc --> pieceModel
        pieceSvc --> pieceRepo

        digSvc --> digModel
        digSvc --> digRepo
        digSvc --> pieceRepo

        tagSvc --> tagModel
        tagSvc --> tagRepo
        tagSvc --> digRepo

        pieceRepo --> h2db
        digRepo --> h2db
        tagRepo --> h2db
    end

    telegramAdapter <-->|"Telegram Bot API"| telegram["Telegram"]
    httpAdapter -->|"HTTP GET /pieces"| pieceCtrl
```

### Estructura de paquetes

```text
bot/
├── adapter/
│   ├── http/
│   │   └── PieceApiHttpAdapter.java
│   └── telegram/
│       └── TelegramBotAdapter.java
├── config/
│   └── TelegramBotConfiguration.java
├── exception/
│   └── BotSearchException.java
├── port/
│   └── PieceSearchPort.java
└── service/
    └── BotSearchService.java
```
