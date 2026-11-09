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

Monolito Spring Boot organizado **por capa**, según las capas de Fowler (ver `AGENTS.md`):

```
src/main/java/ar/edu/unsam/ddso/tp/
├── TpApplication.java   # entry point
├── controller/          # @RestController: HTTP ↔ service, sin lógica
├── dto/                 # records de request/response
├── service/             # orquestación: busca, guarda, mapea a DTO
├── repository/          # interfaces Spring Data JPA
├── model/               # entidades @Entity del dominio
└── exception/           # NotFoundException + GlobalExceptionHandler (404/400)
src/main/resources/
├── application.properties        # nombre de la app + perfil activo
├── application-local.properties  # H2 + JPA para desarrollo local
└── application-prod.properties   # H2 + JPA para "prod"
src/test/java/ar/edu/unsam/ddso/tp/
├── TpApplicationTests.java       # smoke test (contextLoads)
└── service/                      # tests unitarios de services (JUnit 5 + Mockito)
docs/
└── api.md                        # especificación de la API web
```

La API está documentada en [`docs/api.md`](docs/api.md).

## Cómo correr los tests

```bash
./mvnw test
```

## Cómo ejecutar la app en local

```bash
./mvnw spring-boot:run
```

Perfil `local` por defecto (H2 in-memory, consola en `http://localhost:8080/h2-console`).
