# Log Analyzer

A real-time log monitoring and analysis platform — similar to Kibana or Grafana.  
Built with **Java 25**, **Spring Boot**, **PostgreSQL**, **Kafka**, and **Docker**.

![stack](https://img.shields.io/badge/Java-25-orange) ![stack](https://img.shields.io/badge/Spring%20Boot-3.5-green) ![stack](https://img.shields.io/badge/Docker-compose-blue)

---

## Features

- **Real-time dashboard** — live stream of incoming logs with auto-refresh widgets
- **Dynamic widgets** — add, remove, and drag-and-drop reorder: stat cards, charts, tables, and system gauges (CPU / RAM / Disk)
- **Log explorer** — full-text search, level/service/time filters, pagination
- **Kafka ingestion** — logs flow in via Kafka; external apps can push logs via REST
- **Authentication** — per-user accounts with email + password
- **User profiles** — display name, avatar upload, and EN/RU interface language
- **Dark / Light theme** toggle
- **Docker Compose** — one command to run everything

---

## Quick Start (Docker)

### Requirements

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (or Docker Engine + Compose)
- Git

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/log-analyzer.git
cd log-analyzer
```

### 2. Create your `.env` file

```bash
cp .env.example .env
```

Open `.env` and set your values:

```env
DB_HOST=postgres
DB_PORT=5432
DB_NAME=loganalyzer
DB_USER=loganalyzer
DB_PASSWORD=your_secure_password

KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

### 3. Start everything

```bash
docker compose up -d
```

The first run downloads images and builds the app (~2–3 minutes).

### 4. Open the app

Go to **http://localhost:8080** in your browser.  
Register an account and log in — the dashboard opens automatically.

### 5. Generate demo data

Click **"Demo Logs"** in the top bar, or press **"Start Stream"** for a live data feed.

---

## Making It Accessible to Others

### Option A — Local Network (same Wi-Fi / LAN)

Other people on your network can reach the app without any extra setup:

1. Find your local IP address:
   - **Windows**: `ipconfig` → look for *IPv4 Address* (e.g. `192.168.1.42`)
   - **macOS / Linux**: `ip a` or `ifconfig`

2. Share the URL: **`http://192.168.1.42:8080`**

> Make sure Windows Firewall (or your OS firewall) allows inbound connections on port **8080**.
> In Windows: *Windows Defender Firewall → Allow an app → add port 8080*.

---

### Option B — Temporary Public URL (ngrok)

Perfect for demos or sharing with people outside your network, no server required.

1. Install [ngrok](https://ngrok.com/) and sign up (free tier is enough).

2. Run the app locally (Docker or IntelliJ), then in a separate terminal:

```bash
ngrok http 8080
```

3. ngrok prints a public URL like `https://abc123.ngrok-free.app`.  
   Share that URL — anyone on the internet can open it.

---

### Option C — Cloud VPS (permanent, public)

Deploy to any Linux server (DigitalOcean, Hetzner, AWS EC2, etc.).

#### On the server:

```bash
# Install Docker
curl -fsSL https://get.docker.com | sh

# Clone the repo
git clone https://github.com/YOUR_USERNAME/log-analyzer.git
cd log-analyzer

# Create .env
cp .env.example .env
nano .env          # fill in your values

# Start
docker compose up -d
```

The app runs on port **8080**. To serve it on port **80** (standard HTTP) without modifying Docker, use **nginx** as a reverse proxy:

```bash
sudo apt install nginx -y
```

Create `/etc/nginx/sites-available/log-analyzer`:

```nginx
server {
    listen 80;
    server_name your-domain.com;   # or your server IP

    location / {
        proxy_pass         http://localhost:8080;
        proxy_set_header   Host $host;
        proxy_set_header   X-Real-IP $remote_addr;
        proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/log-analyzer /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

Now anyone can open **http://your-domain.com** or **http://your-server-ip**.

#### Optional: HTTPS with Let's Encrypt (free SSL)

```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

After this the app is served at **https://your-domain.com** with auto-renewing certificates.

---

### Option D — Railway / Render (free PaaS, no server management)

Platforms like [Railway](https://railway.app) and [Render](https://render.com) can deploy
your Docker Compose stack from a GitHub repository for free (with usage limits).

1. Push the project to GitHub (see below).
2. Create a new project on Railway → *Deploy from GitHub repo*.
3. Add the environment variables from `.env` in the Railway dashboard.
4. Railway gives you a public URL automatically.

---

## Sending Logs from Your Own Application

The Log Analyzer accepts logs via its REST API — no Kafka setup needed in the client app.

### Endpoint

```
POST http://YOUR_HOST:8080/api/logs
Content-Type: application/json
```

### Payload

```json
{
  "timestamp": "2025-06-01T12:00:00Z",
  "level": "ERROR",
  "serviceName": "my-service",
  "message": "Unhandled exception in PaymentController",
  "traceId": "abc123def456",
  "host": "prod-server-1"
}
```

| Field         | Required | Values                        |
|---------------|----------|-------------------------------|
| `timestamp`   | No       | ISO-8601; defaults to now     |
| `level`       | Yes      | `DEBUG` `INFO` `WARN` `ERROR` |
| `serviceName` | Yes      | Any string, max 100 chars     |
| `message`     | Yes      | Any string                    |
| `traceId`     | No       | Up to 64 chars                |
| `host`        | No       | Up to 100 chars               |

### Example — curl

```bash
curl -X POST http://localhost:8080/api/logs \
  -H "Content-Type: application/json" \
  -d '{"level":"INFO","serviceName":"my-app","message":"Server started"}'
```

### Example — Java (Spring Boot)

```java
restClient.post()
    .uri("http://log-analyzer:8080/api/logs")
    .contentType(MediaType.APPLICATION_JSON)
    .body(Map.of(
        "level",       "ERROR",
        "serviceName", "order-service",
        "message",     ex.getMessage()
    ))
    .retrieve()
    .toBodilessEntity();
```

---

## Development Setup (without Docker)

Requires: **JDK 25**, **Docker Desktop** (for Postgres + Kafka only).

```bash
# Start only infrastructure
docker compose up postgres kafka -d

# Run the app in IntelliJ with Active Profile: local
# (or from terminal)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The `local` profile connects to `localhost:5433` (Postgres) and `localhost:9094` (Kafka).

---

## Project Structure

```
src/
├── main/
│   ├── java/com/arssedot/loganalyzer/
│   │   ├── config/         # Security, Kafka configuration
│   │   ├── domain/         # JPA entities (LogEntry, User, Widget)
│   │   ├── kafka/          # Kafka producer & consumer
│   │   ├── repository/     # Spring Data JPA repositories
│   │   ├── service/        # Business logic
│   │   └── web/
│   │       ├── controller/ # REST + MVC controllers
│   │       └── dto/        # Request / response DTOs
│   └── resources/
│       ├── db/migration/   # Flyway SQL migrations
│       ├── templates/      # Thymeleaf HTML templates
│       └── application*.yml
└── test/
```

---

## Environment Variables Reference

| Variable                  | Default          | Description                    |
|---------------------------|------------------|--------------------------------|
| `DB_HOST`                 | `postgres`       | PostgreSQL hostname            |
| `DB_PORT`                 | `5432`           | PostgreSQL port                |
| `DB_NAME`                 | `loganalyzer`    | Database name                  |
| `DB_USER`                 | `loganalyzer`    | Database username              |
| `DB_PASSWORD`             | *(required)*     | Database password              |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092`     | Kafka broker address           |

---

## License

MIT
