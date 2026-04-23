# Messages Challenge

Multi-channel notification delivery system (SMS / Email / Push). Users subscribe to categories and choose preferred channels; a message posted to a category fans out to every subscribed user through each of their preferred channels, via a persistent Redis queue with retries, exponential backoff, and a dead-letter state.

Built with **Java 21 + Spring Boot 3** (backend) and **React 19 + TypeScript** (frontend). Everything runs inside Docker.

## Quick start

Requirements: Docker + Docker Compose. Everything else (JDK, Maven, Node, Postgres, Redis) runs inside containers.

```bash
cp .env.example .env

# Start the full stack (postgres, redis, backend, frontend)
docker compose up -d
# or, equivalently:
make up

# Tail logs from all services
make logs
# or: docker compose logs -f
```

- Web UI: <http://localhost:5173>
- Backend API: <http://localhost:8080/api>
- Health: <http://localhost:8080/actuator/health>

Run `make help` to see all available targets (tests, style checks, rebuild, etc.).

## Adding a new channel

1. Flyway migration: one `INSERT INTO channels` row.
2. One `@Component` extending `AbstractChannelSender` (or implementing `ChannelSender` directly).
