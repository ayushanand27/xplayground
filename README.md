# DevOps Pipeline Project - Interactive Code Editor to Automated CI/CD

A **real-life DevOps project** demonstrating complete automation: Write code → Commit → Jenkins builds it → Pipeline executes automatically.

---

## 🎯 What It Does

1. **Interactive Web Dashboard** (Port **8800**)
   - Write C++, Java, Python code directly in browser
   - Commit & push to GitHub with one click
   - Real-time pipeline visualization

2. **Automated Jenkins Pipeline** (Port **8080**)
   - Triggered automatically when code is committed
   - 6 stages: Checkout → Build → Test → Package → Selenium → Reports
   - Live execution status with visual feedback

---

## 🚀 Quick Start (Local Execution)

### Requirements
- Java 17+
- Maven 3.x
- Google Chrome

### Run Steps

**1. Build the project**
```bash
mvn clean package -DskipTests
```

**2. Start the application**
```bash
java -jar target/devops-pipeline-app-1.0.0.jar
```

**3. Open in browser**
```
http://localhost:8800
```

**4. Write code and commit**
- Type C++ code (or any language) in the editor
- Click "Commit & Push to GitHub"
- Watch pipeline execute in real-time ✨

---

## 📊 Ports

| Service | Port | URL |
|---------|------|-----|
| **Application Dashboard** | 8800 | http://localhost:8800 |
| **Jenkins CI/CD** | 8080 | http://localhost:8080 |

---

## 🧪 Run Tests

```bash
# Unit tests
mvn test

# Selenium UI test (app must be running on 8800)
mvn test -Dselenium.enabled=true
```

---

## 📁 Project Structure

```
src/main/java/com/example/devops/
  └── App.java                 (Web dashboard + API)

src/test/java/com/example/devops/
  ├── AppTest.java             (Unit tests)
  └── SeleniumSmokeTest.java   (UI automation)

Jenkinsfile                     (CI/CD pipeline definition)
Dockerfile                      (Container image)
pom.xml                        (Maven dependencies)
```

---

## ✅ Tech Stack

- **Java 17** - Application runtime
- **Maven** - Build automation
- **SparkJava** - Lightweight web server
- **JUnit 5** - Unit testing
- **Selenium WebDriver** - UI automation
- **Jenkins** - CI/CD automation
- **Docker** - Containerization

---

## 🎓 For Faculty Demo

**Show the working system:**

1. Open http://localhost:8800 → See interactive dashboard
2. Write code in editor → Click commit button
3. Watch console log in real-time → See pipeline stages complete
4. All tests pass automatically ✅

**Key Points:**
- Demonstrates **DevOps automation** (no manual steps)
- Real **CI/CD pipeline** with Jenkins
- Automated **UI testing** with Selenium
- Shows **Infrastructure as Code** (Jenkinsfile, Dockerfile)

---

## 🛑 Stop the Application

```bash
# Windows
taskkill /F /IM java.exe

# Linux/Mac
pkill java
```

---

## 📝 Notes

- Health check: `curl http://localhost:8800/health`
- Port 8800 will be auto-freed if already in use
- All tests included and passing
- Production-ready Maven build with proper packaging
