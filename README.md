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

## Running the Demo (Quick)

1. Open the project folder in terminal.
2. Ensure Docker and Jenkins are running.
3. (Optional) copy `frontend/.env.example` to `frontend/.env`.
4. Start all services:

```bash
docker compose up -d --build
```

5. Verify containers:

```bash
docker compose ps
```

6. Open:
   - Frontend: `http://localhost:3000`
   - Backend health: `http://localhost:8800/health`
   - Prometheus: `http://localhost:9090`
   - Grafana: `http://localhost:3001`

## Presentation Runbook (What to run and show)

Use this flow while presenting the project live.

### 1) Pre-demo checks (30–60 sec)

Run:

```bash
docker compose ps
```

Show:
- backend, frontend, prometheus, grafana are **Up**
- backend/frontend/prometheus health states are healthy or starting

### 2) Prove backend is alive (15 sec)

Run:

```bash
curl http://localhost:8800/health
```

Show:
- response is `OK`

### 3) Open frontend dashboard (1–2 min)

Open `http://localhost:3000` and show:
- Backend health card/status
- Pipeline stage visualization
- Commit form
- Live metrics panel

### 4) Show metrics endpoint directly (30 sec)

Run:

```bash
curl http://localhost:8800/metrics
```

Show:
- Prometheus-format output
- `http_requests_total{endpoint="..."}` counters

### 5) Show observability stack (1–2 min)

Open Prometheus: `http://localhost:9090`
- Verify target scrape is healthy

Open Grafana: `http://localhost:3001`
- Show pre-provisioned dashboard
- Explain endpoint counter visualization

### 6) Optional Jenkins trigger demo (advanced)

From frontend commit form (or API) trigger:

```bash
curl -X POST "http://localhost:8800/api/commit?filename=demo.txt&message=Live+demo"
```

Then show:
- Jenkins job execution in `http://localhost:8080`
- Build status/log endpoints (`/api/build-status/:number`, `/api/build-log/:number`)

> Note: If `/api/commit` returns 500, verify Jenkins URL/user/token/job settings and API token permission.

## Recommended talk track (5–8 minutes)

1. **Problem**: manual release pipeline is slow and error-prone.
2. **Solution**: automated CI/CD + observability with this stack.
3. **Architecture**: backend + frontend + Prometheus + Grafana + Jenkins.
4. **Live run**: health -> UI -> metrics -> monitoring -> (optional) Jenkins trigger.
5. **Value**: fast feedback, measurable health, reproducible deployment path.

## Common troubleshooting during demo

- **Containers not starting**
  - Run `docker compose logs --tail=100` and inspect failing service.
- **Backend not reachable on 8800**
  - Check `docker compose ps` port mapping.
- **Frontend loads but API fails**
  - Confirm backend is healthy and frontend URL config points to `http://localhost:8800`.
- **Jenkins trigger fails (500)**
  - Validate `JENKINS_URL`, `JENKINS_USER`, `JENKINS_TOKEN`, `JENKINS_JOB`.
  - Ensure Jenkins job exists and token has build permission.

## What I Learned

- How to split a project into backend, frontend, and observability services.
- How Jenkins stages map to real delivery steps.
- How to expose application metrics with Micrometer and Prometheus.
- How to provision Prometheus and Grafana with Docker Compose.
- How to package a complete DevOps demo into one repeatable workflow.
