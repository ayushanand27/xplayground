# DevOps Pipeline App - Write Code, Commit, See Pipeline Execute

A **real-life DevOps automation project** with an interactive code editor GUI where you can:
- ✍️ Write code (C++, Java, Python, etc.) directly in the browser
- 🔄 Commit and push to GitHub with one click
- 🚀 Automatically trigger Jenkins pipeline
- 👀 Watch real-time pipeline execution

**Complete CI/CD flow:** **code editor → GitHub commit → Jenkins trigger → build → test → package → deploy → UI validation**

---

## Overview

This project implements a **complete DevOps workflow** with:

1. **Interactive Web Dashboard** running on **http://localhost:8800** with:
   - Code editor (supports C++, Java, Python, any language)
   - Real-time pipeline visualization
   - Live console logs
   - Build status tracking

2. **Jenkins CI/CD Pipeline** running on **http://localhost:8080** that:
   - Automatically builds when code is committed
   - Runs unit tests and UI tests
   - Packages and deploys the application
   - Provides visual feedback

The primary goal is to demonstrate **real DevOps practices** with an interactive GUI for live demonstration.

---

## What This Project Does

The Jenkins pipeline automatically performs the following steps:

- ✅ Builds the application using Maven  
- ✅ Runs unit tests using JUnit 5  
- ✅ Packages the application into an executable JAR  
- ✅ Starts the application on port **8800**  
- ✅ Runs Selenium UI smoke test using headless Chrome  
- ✅ Publishes test reports in Jenkins  
- ✅ (Optional) Builds a Docker image for consistent deployment  

**Jenkins UI:** http://localhost:8080  
**Application URL:** http://localhost:8800  

---

## Why This Is a Real DevOps Project

In real software teams:

- Developers push code frequently  
- Builds and tests must run automatically  
- UI validation should not be manual  
- Applications must behave the same across environments  
- CI tools must provide clear PASS/FAIL feedback  

This project solves these problems by implementing an **end-to-end automated CI pipeline** using industry-standard DevOps tools.

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

## Application Endpoints & Ports

### Ports Configuration

| Service | Port | URL | Description |
|---------|------|-----|-------------|
| **DevOps Dashboard** | **8800** | http://localhost:8800 | Interactive code editor + pipeline status |
| **Jenkins CI/CD** | **8080** | http://localhost:8080 | Jenkins automation server |

### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Main dashboard with code editor |
| `/health` | GET | Health check (returns OK) |
| `/api/commit` | POST | Submit code commit (triggers pipeline) |

---

## Project Structure

src/
├─ main/
│ └─ java/com/example/devops/App.java
└─ test/
├─ java/com/example/devops/AppTest.java
└─ java/com/example/devops/SeleniumSmokeTest.java

Jenkinsfile
Dockerfile
pom.xml
manifests/install_java_maven.pp
scripts/check_jenkins_build.sh
README.md


---

## 🚀 Running the Project Locally

### Prerequisites
- ✅ **Java 17** or higher installed
- ✅ **Maven 3.x** installed
- ✅ **Google Chrome** installed (for Selenium tests)
- ✅ **Jenkins** running on http://localhost:8080 (optional, for CI/CD)

### Step-by-Step Execution

#### 1️⃣ Build the project
```bash
cd c:\Users\ayush\xplayground
mvn clean package -DskipTests
```
**Expected Output:** `BUILD SUCCESS` with JAR created at `target/devops-pipeline-app-1.0.0.jar`

#### 2️⃣ Start the application
```bash
java -jar target/devops-pipeline-app-1.0.0.jar
```
**Expected Output:**
```
Server started: http://localhost:8800
DevOps Pipeline Working
```

#### 3️⃣ Access the Interactive Dashboard
Open your browser and navigate to:

🌐 **http://localhost:8800**

You will see:
- **Left Panel:** Code editor (write C++, Java, Python, etc.)
- **Right Panel:** Real-time pipeline status visualization
- **Bottom:** Application stats (tests, Docker, Jenkins)

#### 4️⃣ Try the Code Editor
1. **Write code** in the textarea (default: C++ HelloWorld)
2. **Edit filename** (e.g., `main.cpp`, `App.java`, `script.py`)
3. **Add commit message** (e.g., "Added new feature")
4. **Click "Commit & Push to GitHub"**
5. **Watch the pipeline animate** through 6 stages:
   - Checkout → Build → Test → Package → Selenium → Reports
6. **See console logs** in real-time

#### 5️⃣ Run unit tests manually
```bash
mvn test
```
**Expected:** 2 unit tests pass (AppTest)

#### 6️⃣ Run Selenium UI test (optional)
Make sure the application is running on port 8800 first:
```bash
mvn test -Dselenium.enabled=true
```
**Expected:** Browser opens headless, validates http://localhost:8800, test passes

#### 7️⃣ Check application health
```bash
curl http://localhost:8800/health
```
**Expected:** `OK`

#### 8️⃣ Stop the application
Press `Ctrl + C` in the terminal where the app is running

Or kill the process:
```bash
# Windows
taskkill /F /IM java.exe

# Linux/Mac
pkill java
```
---

## 🔧 Jenkins Pipeline (CI/CD Core)

**Jenkins URL:** http://localhost:8080

The pipeline is defined using **Pipeline as Code** in the `Jenkinsfile`.

### Pipeline Stages (Automated)

| Stage | Description | Port |
|-------|-------------|------|
| 1️⃣ **Checkout** | Fetch source code from GitHub/SCM | - |
| 2️⃣ **Build** | Compile Java code using Maven | - |
| 3️⃣ **Unit Tests** | Run JUnit tests | - |
| 4️⃣ **Package** | Create executable JAR | - |
| 5️⃣ **Start Application** | Launch app on port **8800** | 8800 |
| 6️⃣ **Selenium UI Test** | Validate UI at http://localhost:8800 | 8800 |
| 7️⃣ **Publish Reports** | Display test results in Jenkins | - |
| 8️⃣ **Cleanup** | Stop application and free port 8800 | - |

### Jenkins Setup Requirements (Windows)

1. **Jenkins** running on http://localhost:8080
2. **JDK 17** configured in Jenkins Global Tool Configuration as `JDK17`
3. **Maven 3.x** configured in Jenkins Global Tool Configuration as `Maven3`
4. **Google Chrome** installed (for Selenium headless execution)
5. **Port 8800** available (pipeline will kill existing processes)
6. **Docker** installed only if Docker build stage is enabled

### Triggering the Pipeline

**Manually in Jenkins:**
1. Open http://localhost:8080
2. Select your pipeline job
3. Click "Build Now"

**Automatically via Code Editor:**
1. Open http://localhost:8800
2. Write code in the editor
3. Click "Commit & Push to GitHub"
4. Jenkins webhook triggers build automatically

Jenkins Build Results
🟢 Green Build → Build, tests, and UI validation passed

🔴 Red Build → Build, test, or UI validation failed

📊 Test Reports → JUnit reports published automatically

📝 Console Logs → Detailed pipeline execution logs

Docker (Optional)
Build Docker image
docker build -t devops-pipeline-app:latest .
Run Docker container
docker run --rm -p 8800:8800 devops-pipeline-app:latest
Access the application:

http://localhost:8800
Puppet (Optional Demonstration)
The Puppet manifest located at:

manifests/install_java_maven.pp
automates installation of:

Java 17

Maven

This demonstrates Infrastructure as Code for Linux-based Jenkins agents.

Monitoring Simulation
A Nagios-style monitoring script is provided at:

scripts/check_jenkins_build.sh
It simulates Jenkins availability checks.

Exit Codes
0 → OK

2 → CRITICAL

Usage
./scripts/check_jenkins_build.sh https://your-jenkins.example.com
Git Workflow
git init
git add .
git commit -m "feat: real-life DevOps CI pipeline with automated testing"
git remote add origin <your-repository-url>
git push -u origin main
---

## 🛠️ Troubleshooting

### ❌ Application not starting

**Problem:** Port 8800 already in use

**Solution (Windows):**
```bash
# Check what's using port 8800
netstat -ano | findstr :8800

# Kill the process by PID
taskkill /F /PID <pid>

# Or kill all Java processes
taskkill /F /IM java.exe
```

**Solution (Linux/Mac):**
```bash
# Check what's using port 8800
lsof -i :8800

# Kill the process
kill -9 <pid>
```

### ❌ Selenium test failure

**Checklist:**
- ✅ App is running on http://localhost:8800
- ✅ Google Chrome is installed
- ✅ Run with: `mvn test -Dselenium.enabled=true`
- ✅ Check console logs for detailed errors

### ❌ Jenkins build failure

**Checklist:**
- ✅ Jenkins is running on http://localhost:8080
- ✅ JDK17 configured in Jenkins → Manage Jenkins → Global Tool Configuration
- ✅ Maven3 configured in Jenkins → Manage Jenkins → Global Tool Configuration
- ✅ Review Jenkins console output for specific errors
- ✅ Port 8800 is available during build

### ❌ Code editor not loading

**Solution:**
```bash
# Restart the application
taskkill /F /IM java.exe
java -jar target/devops-pipeline-app-1.0.0.jar

# Wait 3 seconds, then access:
http://localhost:8800
```

### ❌ "Commit & Push" button not working

**Check:**
1. Browser console for JavaScript errors (F12 → Console)
2. Backend logs where Java app is running
3. Network tab (F12 → Network) to see POST to `/api/commit`

Future Enhancements
Add database integration (H2 / PostgreSQL)

Add REST CRUD APIs

Add metrics and monitoring

Add authentication and HTTPS

Add performance tests (JMeter, Gatling)

Add code quality gates (SonarQube)

Deploy to cloud (AWS / Azure / Kubernetes)