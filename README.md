# HuertoHogar API — Backend Spring Boot (DSY1104 EP3)

API REST versionada (`/api/v1`) para la plataforma e-commerce **HuertoHogar** (Duoc UC · Desarrollo Fullstack II · Grupo 8).

## Stack Técnico
- **Java 21** + **Spring Boot 3.5.7**
- **Base de datos relacional:** MySQL 8.4 (Docker Compose) + Spring Data JPA / Hibernate
- **Seguridad:** Spring Security + `spring-boot-starter-oauth2-resource-server` (JWT HS256 con `accessToken` + `refreshToken`, `BCryptPasswordEncoder` y control por roles `ADMIN`, `VENDEDOR`, `CLIENTE`)
- **Documentación:** OpenAPI 3 / Swagger UI (`springdoc-openapi-starter-webmvc-ui:2.8.14`)
- **Pruebas:** JUnit 5, Mockito, Spring MockMvc y H2 en memoria (perfil `test`)

## Modelo Relacional
`CATEGORIA (1) --< PRODUCTO (N) --< DETALLE_BOLETA (N) >-- (1) BOLETA (N) >-- (0..1) USUARIO`

## Ejecución Local
1. Levantar MySQL 8:
   ```bash
   docker compose up -d
   ```
2. Ejecutar tests automatizados:
   ```bash
   ./mvnw test
   ```
3. Iniciar la API:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Documentación Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Despliegue en AWS EC2 (Semanas 13–14)
La carpeta `deploy/` incluye la unidad `systemd` (`huertohogar.service`), plantilla de variables (`huertohogar.env.example`) y configuración de reverse proxy Nginx (`huertohogar-nginx.conf`) con `try_files` para SPA React Router.
