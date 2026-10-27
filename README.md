# Nombre Equipo

Integrantes

1. Abril Peric
2. Camila Blesa
3. Santino Domeniconi
4. Samuel Trejo

nombre user_git
1. abrilperic8
2. CamilaBlesa
3. santino-ardua-tpm
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
