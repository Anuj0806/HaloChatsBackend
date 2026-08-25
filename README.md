# Chat App — Backend

Spring Boot 3 REST + WebSocket (STOMP) API: Spring Security + JWT, Spring
Data JPA, PostgreSQL.

This is a standalone service. The frontend lives in a separate project
(`chatapp-frontend`) and talks to this API over HTTP + WebSocket — set
`VITE_API_HOST` / `VITE_API_PORT` on the frontend to point at wherever this
backend is running.

## Run with Docker (includes Postgres)

```bash
docker compose up --build
```

API is then available at `http://localhost:2000`.

## Run without Docker

1. Have PostgreSQL 14+ running with a `chatdb` database.
2. Point the app at it, either by editing
   `src/main/resources/application.properties` or via env vars:
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/chatdb
   export SPRING_DATASOURCE_USERNAME=postgres
   export SPRING_DATASOURCE_PASSWORD=postgres
   export MAIL_USERNAME=you@gmail.com
   export MAIL_PASSWORD=app-password
   ```
3. `./mvnw spring-boot:run`

## CORS

Allowed frontend origins are configured in `SecurityConfig.ALLOWED_ORIGINS`.
Add your frontend's origin (e.g. `http://localhost:3000`, or your deployed
domain) there before deploying.

## Architecture

- `controller/` — thin REST + STOMP endpoints
- `service/` — business logic (this is where validation, persistence
  orchestration, and messaging rules live)
- `repo/` — Spring Data JPA repositories
- `DTO/` — request/response shapes, including the shared
  `DTO.common.ApiResponse` envelope used by every endpoint
- `exception/` — `ApiException` + `GlobalExceptionHandler` turn every error
  into a consistent `{success, message, data}` JSON body

## Chat features

- Public (persisted) chat: send, delivery ack, typing indicator, read
  receipts, emoji reactions — all in `ChatService`.
- Private (ephemeral, AES-encrypted, never persisted) chat: same three
  interactive features, via `controller/privateChat/WebSocketController`.
