# DevOps Pipeline App (Java 17 + Maven + Jenkins + Selenium + Docker)

A real-life DevOps automation project that demonstrates a complete CI/CD pipeline:
**build → test → package → run service → UI validation → (optional) containerization**.

---

## Overview

This project implements a **mini web service** running on **http://localhost:8800** and a **Jenkins CI pipeline** that automatically validates the application whenever code is pushed.

The focus of this project is **DevOps automation**, not application complexity.

---

## What This Project Does

The Jenkins pipeline automatically performs the following steps:

✅ Builds the application using Maven  
✅ Runs unit tests using JUnit 5  
✅ Packages the application into an executable JAR  
✅ Starts the application on port **8800**  
✅ Runs Selenium UI smoke test (headless Chrome)  
✅ Publishes test reports in Jenkins  
✅ (Optional) Builds Docker image for consistent deployment  

> Jenkins UI runs on **http://localhost:8080**  
> Application runs on **http://localhost:8800**

---

## Why This Is a Real DevOps Project

In real software teams:

- Developers push code frequently  
- Builds and tests must run automatically  
- UI validation should not be manual  
- Applications must behave the same in all environments  
- CI tools must give clear PASS/FAIL visibility  

This project solves those problems by implementing an **end-to-end automated CI pipeline** using industry-standard DevOps tools.

---

## Technology Stack

| Tool | Purpose |
|----|----|
| Java 17 | Application runtime |
| Maven | Build automation |
| SparkJava | Lightweight HTTP server |
| JUnit 5 | Unit testing |
| Selenium WebDriver | Automated UI testing |
| Jenkins | CI/CD automation |
| Docker | Containerization (optional) |
| Puppet | Configuration management (Linux demo) |
| Nagios-style Script | Monitoring simulation |

---

## Application Endpoints

| Endpoint | Description |
|-------|------------|
| `/` | Main web page |
| `/health` | Health check (returns OK) |

---

## Project Structure

```
src/
├─ main/
│  └─ java/com/example/devops/App.java
└─ test/
   ├─ java/com/example/devops/AppTest.java
   └─ java/com/example/devops/SeleniumSmokeTest.java

Jenkinsfile
Dockerfile
pom.xml
manifests/install_java_maven.pp
scripts/check_jenkins_build.sh
README.md
```

---

## Running the Project Locally

### 1️⃣ Build the project
```bash
mvn clean package
```

### 2️⃣ Run the application
```bash
java -jar target/devops-pipeline-app-1.0.0.jar
```

### 3️⃣ Access the application
- **Main page:** http://localhost:8800
- **Health check:** http://localhost:8800/health

### 4️⃣ Run unit tests
```bash
mvn test
```

### 5️⃣ Run Selenium UI test (optional)
Make sure the app is running first:
```bash
mvn test -Dselenium.enabled=true
```

---

## Jenkins Pipeline (CI/CD Core)

The pipeline is defined using **Pipeline as Code** in the `Jenkinsfile`.

### Pipeline Stages
1. **Checkout** – Fetch source code from GitHub
2. **Build** – Compile using Maven
3. **Unit Tests** – Run JUnit tests
4. **Package** – Create executable JAR
5. **Start Application** – Launch app on port 8800
6. **Selenium UI Test** – Validate UI automatically
7. **Publish Reports** – Show test results in Jenkins
8. **Cleanup** – Stop application and free resources

### Jenkins Requirements (Windows)
- Jenkins running on Windows (http://localhost:8080)
- JDK 17 configured in Global Tool Configuration (`JDK17`)
- Maven configured in Global Tool Configuration (`Maven3`)
- Google Chrome installed (for Selenium headless mode)
- Docker installed only if container stage is used

### Jenkins Build Results
- 🟢 **Green Build** → Application built, tested, and validated
- 🔴 **Red Build** → Build/test/UI validation failed
- 📊 **Test Reports** → JUnit reports published automatically
- 📝 **Console Logs** → Detailed pipeline execution logs

---

## Docker (Optional)

### Build Docker image
```bash
docker build -t devops-pipeline-app:latest .
```

### Run Docker container
```bash
docker run --rm -p 8800:8800 devops-pipeline-app:latest
```

Then access:
- http://localhost:8800

---

## Puppet (Optional Demonstration)

The Puppet manifest:
```
manifests/install_java_maven.pp
```
automates installation of:
- Java 17
- Maven

This demonstrates **Infrastructure as Code** for Linux-based Jenkins agents.

---

## Monitoring Simulation

A Nagios-style script:
```
scripts/check_jenkins_build.sh
```
simulates monitoring Jenkins availability.

**Exit codes:**
- `0` → OK
- `2` → CRITICAL

**Usage:**
```bash
./scripts/check_jenkins_build.sh https://your-jenkins.example.com
```

---

## Git Workflow

```bash
git init
git add .
git commit -m "feat: real-life DevOps CI pipeline with automated testing"
git remote add origin <your-repository-url>
git push -u origin main
```

---

## Troubleshooting

### Application not starting
Check port usage:
```bash
netstat -ano | findstr :8800
```

Kill process:
```bash
taskkill /F /PID <pid>
```

### Selenium test failure
- Ensure app is running on port 8800
- Ensure Chrome is installed
- Run with `-Dselenium.enabled=true`

### Jenkins failure
- Verify `JDK17` and `Maven3` in Jenkins tools
- Review Jenkins console output

---

## Future Enhancements

1. Add database integration (H2 / PostgreSQL)
2. Add REST CRUD APIs
3. Add metrics and monitoring
4. Add authentication and HTTPS
5. Add code quality gates (SonarQube)
6. Deploy to cloud / Kubernetes

---

## Final Statement

**This project demonstrates a complete DevOps lifecycle:**  
from code commit to automated build, testing, UI validation, and deployment readiness.

It is suitable for academic evaluation and reflects real-world DevOps practices.
4. **Add security** (authentication, HTTPS)
5. **Deploy to cloud** (AWS, Azure, Kubernetes)
6. **Add performance tests** (JMeter, Gatling)
7. **Add code quality gates** (SonarQube)

---

**This is a complete DevOps lifecycle automation project suitable for academic presentations and real-world deployment scenarios.**
```
Run it:
```
docker run --rm devops-pipeline-app:latest
```

## Puppet
A basic manifest at `manifests/install_java_maven.pp` installs Java 17 and Maven on a Debian/Ubuntu agent. Adjust package names for your distribution.

## Nagios-style check
A simple script at `scripts/check_jenkins_build.sh` simulates checking Jenkins availability:
```
./scripts/check_jenkins_build.sh https://your-jenkins.example.com
```
Exit codes:
- 0: OK
- 2: CRITICAL

## Git
Typical Java/Maven `.gitignore` included. Initialize and commit:
```
git init
git add .
git commit -m "feat: initial DevOps pipeline project"
```
