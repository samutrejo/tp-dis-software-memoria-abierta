# Memoria Abierta

## Integrantes

1. Abril Peric
2. Camila Blesa
3. Santino Domeniconi
4. Samuel Trejo

**Usuarios de GitHub**

1. abrilperic8
2. CamilaBlesa
3. SantiDome14
4. samutrejo

## Forma de uso

Monolito Spring Boot organizado **por capa**, según las capas de Fowler (ver `AGENTS.md`).

```text
src/main/java/ar/edu/unsam/ddso/tp/
├── TpApplication.java
├── bot/
│   ├── adapter/
│   │   ├── http/
│   │   └── telegram/
│   ├── config/
│   ├── exception/
│   ├── port/
│   └── service/
├── controller/
├── dto/
├── exception/
├── model/
├── repository/
└── service/

src/main/resources/
├── application.properties
├── application-local.properties
└── application-prod.properties

src/test/java/ar/edu/unsam/ddso/tp/
├── TpApplicationTests.java
├── bot/
│   └── service/
│       └── BotSearchServiceTest.java
├── dto/
├── service/
└── ...

.env.example
docs/
├── api.md
├── arquitectura.md
└── ...
```

La API está documentada en [`docs/api.md`](docs/api.md). La arquitectura del sistema está documentada en [`docs/arquitectura.md`](docs/arquitectura.md).

## Cómo correr los tests

```bash
./mvnw test
```

## Cómo ejecutar la aplicación en local

```bash
./mvnw spring-boot:run
```

Perfil `local` por defecto (H2 in-memory, consola en `http://localhost:8080/h2-console`).

## Cómo configurar el bot de Telegram

El proyecto permite realizar búsquedas de piezas del Archivo Digital mediante un bot de Telegram.

### 1. Crear el bot

1. Abrir Telegram y buscar `@BotFather`.
2. Enviar `/newbot`.
3. Seguir las instrucciones para asignar un nombre y un username.
4. Copiar el token que entrega BotFather.

El token es privado. No debe compartirse ni subirse al repositorio.

### 2. Configurar las variables de entorno

Copiar el archivo de ejemplo:

```bash
cp .env.example .env
```

Editar `.env` y completar las variables:

```properties
TELEGRAM_BOT_TOKEN=token_entregado_por_botfather
TELEGRAM_BOT_ENABLED=true
BOT_API_BASE_URL=http://localhost:8080
```

- `TELEGRAM_BOT_TOKEN`: token del bot creado con BotFather.
- `TELEGRAM_BOT_ENABLED`: habilita o deshabilita el bot.
- `BOT_API_BASE_URL`: dirección base de la API que consume el bot.

El archivo `.env` está excluido de Git y no debe subirse al repositorio. El archivo `.env.example` sí se versiona y sirve como plantilla.

### 3. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La API y el bot se ejecutan dentro de la misma aplicación Spring Boot. Para que el bot pueda buscar piezas, la API debe estar disponible en la dirección configurada en `BOT_API_BASE_URL`.

### 4. Comandos disponibles

- `/start`: muestra el mensaje de bienvenida.
- `/help`: muestra la ayuda.
- `/buscar name=carta`: busca por nombre.
- `/buscar description=manuscrita`: busca por descripción.
- `/buscar date=1978-06-25`: busca por fecha.
- `/buscar tag=historia`: busca por etiqueta.
- `/buscar name="carta familiar" tag=historia`: combina filtros.

Se pueden combinar varios filtros en una misma búsqueda.

### 5. Deshabilitar el bot

Para ejecutar la API sin iniciar el bot, configurar en `.env`:

```properties
TELEGRAM_BOT_ENABLED=false
```

El resto de la aplicación puede ejecutarse normalmente.

## Convención de branches y PRs

Cada tarea del tablero de Trello tiene un número `DS-XX`. Las branches y los PRs lo usan para que se pueda rastrear qué PR resolvió cada tarea.

**Nombre de la branch:** `<tipo>/DS-XX-descripcion-corta`

- `tipo`: `feat` (funcionalidad), `fix` (corrección), `test` (tests), `docs` (documentación) o `chore` (setup y configuración).
- `DS-XX`: el número de la card de Trello.
- `descripcion-corta`: en minúsculas, con guiones y sin tildes, de 2 a 4 palabras.

Ejemplos: `feat/DS-10-entidad-pieza`, `test/DS-12-piece-service`, `docs/DS-26-api-md`.

**Título del PR:** igual al nombre de la card, por ejemplo `DS-10 · Entidad Pieza + repository`.

**Reglas:**

- Nada se commitea directo a `main`: todo entra por PR.
- Cada PR lo revisa y aprueba otro integrante (columna CODE REVIEW en Trello) antes de mergearlo.

## Links del equipo

- Tablero de Trello (backlog): https://trello.com/b/vg9wrjD8/tp-disenodesoftware
- Discord de la cursada: https://discord.com/channels/1558480425146646560/1558480427189145611
