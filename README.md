# DevOps Pipeline App (Java 17 + Maven + Jenkins + Selenium + Docker)

A real-life DevOps automation project that demonstrates a complete CI/CD pipeline:

**build → test → package → run service → UI validation → (optional) containerization**

---

## Overview

This project implements a **mini web service** running on **http://localhost:8800** and a **Jenkins CI pipeline** that automatically validates the application whenever code is pushed.

The primary goal of this project is to demonstrate **DevOps practices and automation**, not application complexity.

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

## Application Endpoints

| Endpoint | Description |
|-------|------------|
| `/` | Main web page |
| `/health` | Health check (returns OK) |

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

## Running the Project Locally

### 1️⃣ Build the project
```bash
mvn clean package
2️⃣ Run the application
java -jar target/devops-pipeline-app-1.0.0.jar
3️⃣ Access the application
Main page: http://localhost:8800

Health check: http://localhost:8800/health

4️⃣ Run unit tests
mvn test
5️⃣ Run Selenium UI test (optional)
Make sure the application is running first:

mvn test -Dselenium.enabled=true
Jenkins Pipeline (CI/CD Core)
The pipeline is defined using Pipeline as Code in the Jenkinsfile.

Pipeline Stages
Checkout – Fetch source code from GitHub

Build – Compile Java code using Maven

Unit Tests – Run JUnit tests

Package – Create executable JAR

Start Application – Launch app on port 8800

Selenium UI Test – Validate UI automatically

Publish Reports – Display test results in Jenkins

Cleanup – Stop application and free resources

Jenkins Requirements (Windows)
Jenkins running on Windows (http://localhost:8080)

JDK 17 configured in Jenkins Global Tool Configuration (JDK17)

Maven configured in Jenkins Global Tool Configuration (Maven3)

Google Chrome installed (for Selenium headless execution)

Docker installed only if container stage is used

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
Troubleshooting
Application not starting
Check port usage:

netstat -ano | findstr :8800
Kill the process:

taskkill /F /PID <pid>
Selenium test failure
Ensure the app is running on port 8800

Ensure Google Chrome is installed

Run with -Dselenium.enabled=true

Jenkins failure
Verify JDK17 and Maven3 are configured in Jenkins

Review Jenkins console output

Future Enhancements
Add database integration (H2 / PostgreSQL)

Add REST CRUD APIs

Add metrics and monitoring

Add authentication and HTTPS

Add performance tests (JMeter, Gatling)

Add code quality gates (SonarQube)

Deploy to cloud (AWS / Azure / Kubernetes)