# DevOps Pipeline App (Java 17 + Maven)

A simple, production-ready Java 17 Maven project with CI/CD integrations: Git, Jenkins, Docker, Selenium, Puppet, and a Nagios-style check script.

## Tech Stack
- Java 17
- Maven
- JUnit 5
- Selenium (optional, opt-in test)
- Jenkins (Declarative Pipeline)
- Docker
- Puppet
- Nagios-style plugin script

## Project Structure
```
src/
  main/java/com/example/devops/App.java
  test/java/com/example/devops/AppTest.java
  test/java/com/example/devops/SeleniumSmokeTest.java
Jenkinsfile
Dockerfile
manifests/install_java_maven.pp
scripts/check_jenkins_build.sh
pom.xml
```

## Run Locally
- Build: `mvn clean compile`
- Test: `mvn test`
- Package (fat jar): `mvn package`
- Run: `java -jar target/devops-pipeline-app-1.0.0-shaded.jar`

The application prints:
```
DevOps Pipeline Working
```

### Selenium test (optional)
By default, the Selenium test is disabled so CI works even without a browser. To run it locally if you have Chrome installed:
```
mvn -Dselenium.enabled=true test
```
Selenium 4 uses Selenium Manager to auto-provision the ChromeDriver.

## Jenkins
The `Jenkinsfile` uses a Windows agent with globally configured tools:
- JDK: `JDK17`
- Maven: `Maven3`

Pipeline stages:
- Checkout
- Build (`mvn clean compile`)
- Test (`mvn test`)
- Package (`mvn package`) – archives shaded JAR
- Docker Build (`docker build ...`) – requires Docker on the agent

## Docker
Build an image (from project root after packaging):
```
docker build -t devops-pipeline-app:latest .
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
