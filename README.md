# DevOps Pipeline Project

A Java 17 DevOps demo app with a Vite + React frontend, a Spark-based backend, Jenkins automation, Docker, Prometheus, and Grafana.

## Architecture

```mermaid
flowchart LR
  Dev[Developer] --> Git[GitHub]
  Git --> Jenkins[Jenkins Pipeline]
  Jenkins --> Build[Build & Test Java App]
  Jenkins --> App[Backend: Spark Java on :8800]
  Jenkins --> FE[Frontend: Vite React on :3000]
  App --> Metrics[/metrics]
  Metrics --> Prom[Prometheus :9090]
  Prom --> Graf[Grafana :3001]
  Graf --> Dash[API Request Dashboard]
```

## Tech Stack

| Tool | Role |
|---|---|
| Java 17 | Backend language |
| Maven | Build and dependency management |
| Spark Java | Lightweight HTTP server |
| React + Vite | Frontend dashboard |
| Tailwind CSS | Frontend styling |
| Jenkins | CI/CD orchestration |
| Docker / Docker Compose | Local orchestration and container packaging |
| Micrometer + Prometheus | Application metrics export |
| Prometheus | Metrics scraping and storage |
| Grafana | Metrics visualization |

## Quick Start

```bash
docker compose up --build
```

Before running the frontend locally, copy `frontend/.env.example` to `frontend/.env` if you want to override the backend URL. The default value points at `http://localhost:8800`.

Open these services after the stack starts:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8800`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3001`

## Pipeline Stages

The Jenkins pipeline is staged so every step is visible and independently verifiable:

1. **Checkout** — pulls the latest source from GitHub.
2. **Build** — compiles the Java backend.
3. **Unit Tests** — runs the JUnit test suite.
4. **Package** — creates the executable JAR.
5. **Start Application** — launches the backend on port 8800 and waits for `/health`.
6. **Selenium UI Test** — runs the browser smoke test against the live app.
7. **Docker Build** — builds the backend container image.
8. **Build Frontend** — installs frontend dependencies and runs the Vite build in `frontend/`.
9. **Docker Compose Build** — validates the full multi-service stack with `docker-compose build`.
10. **Push to DockerHub** — logs in with Jenkins credentials, tags the image, and pushes both `latest` and build-number tags.

## Monitoring

The backend exposes Prometheus-format metrics at `GET /metrics`. A Micrometer counter tracks how many times each API endpoint is called, with endpoint labels such as `/health`, `/api/commit`, and `/jenkins-build`.

In `docker-compose.yml`:

- **Prometheus** runs on port `9090` and scrapes `http://backend:8800/metrics` every 15 seconds.
- **Grafana** runs on port `3001` and is auto-provisioned with Prometheus as its default datasource.
- The Grafana dashboard export lives at `monitoring/grafana-dashboard.json` and visualizes API request counts by endpoint.

Kubernetes manifests available in `k8s/` for cluster deployment.

## Running the Demo

1. Open the project folder in your editor or terminal.
2. Make sure Docker Desktop is running.
3. If you want to customize the frontend backend URL, copy `frontend/.env.example` to `frontend/.env`.
4. Start the stack with `docker compose up --build`.
5. Wait until the backend, frontend, Prometheus, and Grafana containers finish starting.
6. Open the React dashboard at `http://localhost:3000`.
7. Submit a sample commit and watch the pipeline cards update.
8. Open Prometheus at `http://localhost:9090` to see the scraped metric.
9. Open Grafana at http://localhost:3001 (admin/admin) — the dashboard loads automatically.

## What I Learned

- How to split a project into backend, frontend, and observability services.
- How Jenkins stages map to real delivery steps.
- How to expose application metrics with Micrometer and Prometheus.
- How to provision Prometheus and Grafana with Docker Compose.
- How to package a complete DevOps demo into one repeatable workflow.
