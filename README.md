# Memoria Abierta

Integrantes

1. Abril Peric
2. Camila Blesa
3. Santino Domeniconi
4. Samuel Trejo

nombre user_git
1. abrilperic8
2. CamilaBlesa
3. SantiDome14
4. samutrejo

# Forma de uso

```
src/main/java/ar/edu/unsam/ddso/tp/
└── TpApplication.java          # entry point
src/main/resources/
├── application.properties        # nombre de la app + perfil activo
├── application-local.properties  # H2 + JPA para desarrollo local
└── application-prod.properties   # H2 + JPA para "prod"
src/test/java/ar/edu/unsam/ddso/tp/
└── TpApplicationTests.java       # smoke test (contextLoads)
```

## Cómo correr los tests

```bash
./mvnw test
```

## Cómo ejecutar la app en local

```bash
./mvnw spring-boot:run
```

Perfil `local` por defecto (H2 in-memory, consola en `http://localhost:8080/h2-console`).


## Convención de branches y PRs

Cada tarea del tablero de Trello tiene un número `DS-XX`. Las branches y los PRs lo usan para que se pueda rastrear qué PR resolvió cada tarea.

**Nombre de la branch:** `<tipo>/DS-XX-descripcion-corta`

- `tipo`: `feat` (funcionalidad), `fix` (corrección), `test` (tests), `docs` (documentación) o `chore` (setup y configuración).
- `DS-XX`: el número de la card de Trello.
- `descripcion-corta`: en minúsculas, con guiones y sin tildes, de 2 a 4 palabras.

Ejemplos: `feat/DS-10-entidad-pieza`, `test/DS-12-pieza-service`, `docs/DS-26-api-md`.

**Título del PR:** igual al nombre de la card, por ejemplo `DS-10 · Entidad Pieza + repository`.

**Reglas:**

- Nada se commitea directo a `main`: todo entra por PR.
- Cada PR lo revisa y aprueba otro integrante (columna CODE REVIEW en Trello) antes de mergearlo.
