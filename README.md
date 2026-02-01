# DevOps Pipeline App (Java 17 + Maven)

A **real-life DevOps automation project** that demonstrates a complete CI/CD pipeline with automated builds, unit tests, Selenium UI validation, and Docker containerization.

## What This Project Does

This is a **mini web service** running on `http://localhost:8800` with a Jenkins CI pipeline that automatically:
- ✅ Builds the application with Maven
- ✅ Runs unit tests (JUnit)
- ✅ Packages into executable JAR
- ✅ Starts the application
- ✅ Runs Selenium UI test to validate the deployed app
- ✅ (Optional) Builds Docker image
- ✅ Publishes test reports

## Why This is a Real DevOps Project

In companies, the real problems are:
1. Developers push code often
2. Builds/tests must run **automatically**
3. UI must be verified **automatically** (Selenium)
4. Same build should work on any machine (Docker)
5. Jenkins should show PASS/FAIL + reports

**This project is a DevOps automation system for a web service.**

## Tech Stack
- **Java 17** - Application runtime
- **Maven** - Build automation
- **SparkJava** - Lightweight web framework
- **JUnit 5** - Unit testing
- **Selenium WebDriver** - Automated UI testing
- **Jenkins** - CI/CD pipeline automation
- **Docker** - Containerization
- **Puppet** - Configuration management
- **Nagios-style script** - Health monitoring

## Project Structure
```
src/
  main/java/com/example/devops/App.java         # Web service (port 8800)
  test/java/com/example/devops/AppTest.java     # Unit tests
  test/java/com/example/devops/SeleniumSmokeTest.java  # UI automation test
Jenkinsfile                                      # CI/CD pipeline definition
Dockerfile                                       # Container configuration
manifests/install_java_maven.pp                 # Puppet automation
scripts/check_jenkins_build.sh                  # Nagios health check
pom.xml                                          # Maven dependencies
```

## Quick Start - Run Locally

### 1. Build the project
```bash
mvn clean package
```

### 2. Run the application
```bash
java -jar target/devops-pipeline-app-1.0.0.jar
```

### 3. Test the endpoints
Open your browser:
- Main page: `http://localhost:8800`
- Health check: `http://localhost:8800/health`

### 4. Run unit tests
```bash
mvn test
```

### 5. Run Selenium UI test (optional)
```bash
mvn test -Dselenium.enabled=true
```

## Jenkins Pipeline (The DevOps Core)

The `Jenkinsfile` defines an automated pipeline with these stages:

### Pipeline Stages
1. **Checkout** - Get code from Git/GitHub
2. **Build** - Compile Java code with Maven
3. **Unit Tests** - Run JUnit tests
4. **Package** - Create executable JAR
5. **Start Application** - Launch app on port 8800
6. **Selenium UI Test** - Validate UI automatically
7. **Docker Build** - (Optional) Create container image

### Jenkins Requirements
- Windows agent with:
  - JDK 17 (configured as `JDK17` in Global Tool Configuration)
  - Maven 3 (configured as `Maven3` in Global Tool Configuration)
  - Docker (optional, for containerization)

### What Jenkins Shows You
- ✅ **Green Build** = All tests passed, UI validated
- ❌ **Red Build** = Something failed (build, test, or UI validation)
- 📊 **Test Reports** - JUnit results published automatically
- 📝 **Console Log** - See "Selenium validated app" message

## Docker (Optional)

### Build Docker image
```bash
docker build -t devops-pipeline-app:latest .
```

### Run in container
```bash
docker run -p 8800:8800 --rm devops-pipeline-app:latest
```

Then visit `http://localhost:8800`

## Final Demo Script (For Faculty/Presentation)

**"Here is my GitHub repo and Jenkinsfile (pipeline as code)."**

**"When I push a commit, Jenkins triggers automatically."**

**"Jenkins compiles using Maven and runs unit tests."**

**"Then it starts the app on port 8800."**

**"Then Selenium runs headless Chrome and checks the UI page."**

**"Jenkins publishes test reports. Green build means safe to deploy."**

**(Optional) "It also builds a Docker image for consistent deployment anywhere."**

## Configuration Files

### Puppet (Infrastructure as Code)
A basic manifest at `manifests/install_java_maven.pp` automates Java 17 and Maven installation on Linux agents.

### Nagios-style Health Check
Script at `scripts/check_jenkins_build.sh` simulates checking Jenkins availability:
```bash
./scripts/check_jenkins_build.sh https://your-jenkins.example.com
```

Exit codes: `0` = OK, `2` = CRITICAL

## Git Workflow
Initialize and commit:
```bash
git init
git add .
git commit -m "feat: real-life DevOps pipeline with automated testing"
git remote add origin <your-github-repo>
git push -u origin main
```

## Troubleshooting

### App won't start
- Check if port 8800 is already in use: `netstat -an | find "8800"`
- Kill process: `taskkill /F /PID <pid>`

### Selenium test fails
- Make sure the app is running on port 8800
- Check Chrome is installed
- Run with `-Dselenium.enabled=true`

### Jenkins build fails
- Verify JDK17 and Maven3 are configured in Jenkins Global Tools
- Check Windows agent has network access to Maven Central
- Review console output for specific errors

## Next Steps to Enhance

1. **Add database** (PostgreSQL, H2) with migrations
2. **Add REST API** endpoints with CRUD operations
3. **Add metrics** (Prometheus, Grafana)
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
