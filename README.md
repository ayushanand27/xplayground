# DevOps Pipeline Project — Complete Guide

> **Simple rule of this project:** You write code → Push to GitHub → Everything else happens automatically.

---

## The Problem This Project Solves

### Before DevOps (the old painful way)

Imagine a software team of 5 people, each writing code on their own laptop. When it is time to release:

1. Someone manually copies files from 5 laptops
2. Tries to combine them — things break because everyone used different settings
3. Manually runs tests — some pass, some fail, nobody knows why
4. Manually packages everything
5. Manually uploads to a server
6. Server crashes. Nobody knows which step went wrong.
7. It is 11 PM. Everyone is stressed.

**This whole process could take days. And it broke constantly.**

### What DevOps solves

DevOps says: **let a machine do all of that automatically, every single time, the exact same way.**

The moment you push code to GitHub:
- Jenkins picks it up automatically
- Compiles it
- Runs all tests
- Packages it
- Starts the app
- Opens Chrome and checks the app is working
- Packages it inside Docker so it runs anywhere
- If anything fails, it tells you exactly which step and why

**This whole process now takes ~60 seconds. A human does not touch anything.**

That is what this project demonstrates.

---

## What This Project Actually Is

This is a Java web application that:
- Runs a website at `http://localhost:8800`
- The website shows a fake code editor and a pipeline status board — a mini demo of the whole DevOps concept
- The project itself IS built using DevOps (Jenkins automates everything)

So the project is both the demo AND the demonstration of DevOps at work.

---

## The Full Flow

```
You push code to GitHub
        |
        v
Jenkins sees the new code
        |
        v
Stage 1 — Checkout     : Downloads your latest code from GitHub
        |
        v
Stage 2 — Build        : Compiles Java code, checks for syntax errors
        |
        v
Stage 3 — Unit Tests   : Runs automated logic tests on the code
        |
        v
Stage 4 — Package      : Bundles everything into one .jar file
        |
        v
Stage 5 — Start App    : Runs the .jar (starts the website on port 8800)
        |
        v
Stage 6 — Selenium     : Opens Chrome, visits the website, checks it works
        |
        v
Stage 7 — Docker       : Packages the app into a Docker container image
        |
        v
Post   — Cleanup       : Stops the app, frees port 8800
        |
        v
Result: PASS or FAIL with the exact stage that broke
```

---

## Every File Explained

---

### pom.xml — The Shopping List

**What it is:** Maven's configuration file. Maven is the build tool — it compiles your code and downloads the libraries your code needs.

**Think of it like:** A recipe card that says "to cook this dish, you need these ingredients, and here is where to buy them."

**Key parts:**

```xml
<groupId>com.example</groupId>
<artifactId>devops-pipeline-app</artifactId>
<version>1.0.0</version>
```
The name tag of your application. `groupId` = your company name, `artifactId` = the app name, `version` = 1.0.0.

```xml
<maven.compiler.source>17</maven.compiler.source>
```
"Compile this code using Java 17."

**The dependencies (libraries):**

| Library | What it does |
|---|---|
| `junit-jupiter` | Testing framework — lets you write @Test functions |
| `selenium-java` | Opens a real Chrome browser and controls it from Java code |
| `webdrivermanager` | Automatically downloads the right ChromeDriver version |
| `spark-core` | A tiny web server — makes your Java app respond to web requests |

**The Shade Plugin:**
```xml
<artifactId>maven-shade-plugin</artifactId>
```
Takes your `.jar` file and all its dependencies and merges them into one big "fat jar". This way the app runs anywhere with just `java -jar filename.jar` — no need to install anything else separately.

**Output:** `target/devops-pipeline-app-1.0.0.jar` — the final executable file.

---

### App.java — The Actual Web Application

**What it is:** The main Java program. When you run this it starts a web server on port 8800.

**Think of it like:** The actual kitchen in a restaurant. Everything else is paperwork — this is where the food gets made.

```java
public static final String MESSAGE = "DevOps Pipeline Working";
```
A constant — a fixed text value. Used in unit tests to verify the app exists and is loaded correctly.

```java
port(8800);
```
"Listen for web requests on port 8800." Like saying "our shop is at door number 8800."

**The 4 URLs the app responds to:**

**`GET /health`**
```java
get("/health", (req, res) -> "OK");
```
When anything asks `http://localhost:8800/health`, it just returns the word "OK". This is how Jenkins knows the app started successfully after Stage 5. Like a heartbeat. Jenkins sees "OK" and knows the app is alive.

**`GET /`**
```java
get("/", (req, res) -> getDashboard());
```
The main page. When you open `http://localhost:8800` in a browser, it returns the full HTML dashboard — a code editor, pipeline visualization, status cards. This entire HTML page is built as a Java string inside the `getDashboard()` method (a very long chain of `"<html>..." + "..."` strings).

**`POST /api/commit`**
```java
post("/api/commit", (req, res) -> { ... });
```
When you type code in the browser editor and click "Commit and Push to GitHub", the browser calls this URL. The server prints what you typed to its console and returns a fake JSON success message. It does not actually push to GitHub — it is a simulation for the demo. The response says "Jenkins build triggered!" which then navigates the browser to the Jenkins console page.

**`GET /jenkins-build`**
```java
get("/jenkins-build", (req, res) -> getJenkinsBuildConsole());
```
After you "commit", the browser is sent here. This page shows a fake Jenkins build console — stages running one by one, logs scrolling, a progress bar filling up. All simulated in JavaScript. It looks exactly like a real Jenkins build to a viewer.

**Output when you start the app:**
```
Server started: http://localhost:8800
DevOps Pipeline Working
```
Then it keeps running and responds to every web request.

---

### AppTest.java — Unit Tests

**What it is:** Tests that check the logic of App.java without starting the server at all.

**Think of it like:** Checking that your car's engine works before taking it for a full test drive.

```java
@Test
void messageConstantIsCorrect() {
    assertNotNull(App.MESSAGE);
    assertEquals("DevOps Pipeline Working", App.MESSAGE);
}
```
**Test 1:** "Does `App.MESSAGE` exist, and does it equal exactly the string `DevOps Pipeline Working`?"
- `assertNotNull` = make sure it is not null/missing
- `assertEquals` = make sure it equals this exact value

```java
@Test
void messageIsNotEmpty() {
    assertEquals(false, App.MESSAGE.isEmpty());
}
```
**Test 2:** "Is the message a non-empty string?"

These are **unit tests** — they test one tiny unit of logic in isolation. No network, no browser, no server. Runs in milliseconds.

**Output Jenkins sees:**
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```
Both pass = Stage 3 is green.

---

### SeleniumSmokeTest.java — Browser Automation Test

**What it is:** An automated test that opens a real Chrome browser, visits your website, reads the page, and checks everything looks right.

**Think of it like:** A robot that pretends to be a user. It types the URL, waits for the page to load, reads the heading, and says "yes, it looks correct" or "something is wrong."

**Why it is called a "smoke test":** In engineering, a smoke test means "turn it on and see if smoke comes out." It is a quick basic check — does it run at all? Does it look right?

```java
@EnabledIfSystemProperty(named = "selenium.enabled", matches = "true")
```
This test will NOT run unless you pass `-Dselenium.enabled=true` to Maven. This is intentional. In Stage 3 (Unit Tests), Jenkins runs `mvn test` without this flag, so Selenium is skipped. Only in Stage 6 does Jenkins add the flag to activate it.

**Step by step inside the test:**

```java
ChromeOptions options = new ChromeOptions();
options.addArguments("--headless=new");
```
Set Chrome to run in **headless mode** — Chrome starts and runs completely normally but no visible window opens. Perfect for a server that has no screen.

```java
driver = new ChromeDriver(options);
```
Actually start Chrome. Selenium Manager (built into Selenium 4) automatically downloads and sets up the right ChromeDriver — you do not install anything manually.

```java
driver.get("http://localhost:8800");
```
"Navigate Chrome to this URL." Chrome opens your app.

```java
wait.until(ExpectedConditions.titleContains("DevOps"));
```
Wait up to 15 seconds for the page title to contain the word "DevOps". If the page is slow to load, this waits patiently instead of immediately failing.

```java
assertTrue(driver.getTitle().contains("DevOps"), ...);
```
Assert: "The page title MUST contain the word DevOps." If it does not, fail the test.

```java
String heading = driver.findElement(By.tagName("h1")).getText();
assertTrue(heading.contains("DevOps Pipeline"), ...);
```
Find the `<h1>` tag on the page and read its text. Then assert it contains "DevOps Pipeline". This confirms the main heading rendered correctly in the browser.

```java
} finally {
    if (driver != null) driver.quit();
}
```
Always close Chrome when done, even if the test crashed.

**Output Jenkins sees:**
```
Configuring Chrome options for headless mode...
Starting Chrome WebDriver...
Navigating to http://localhost:8800...
Page loaded successfully!
Page title: DevOps Pipeline - Code to Pipeline
Page heading: DevOps Pipeline - Write Code, See it Deploy
Selenium validated app at http://localhost:8800
All Selenium tests passed!
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

---

### Dockerfile — The Container Recipe

**What it is:** Instructions to build a Docker image — a portable, self-contained package of your app that runs identically on any computer.

**Think of it like:** Writing an IKEA instruction manual for your app. Someone else can follow the exact same steps on any computer and end up with the exact same running application.

```dockerfile
FROM eclipse-temurin:17-jre-jammy
```
Start from a base image that already has Java 17 installed on Ubuntu 22.04 Linux. `eclipse-temurin` is the official Java distribution from Eclipse Adoptium — the same vendor as the JDK running in Jenkins.

```dockerfile
WORKDIR /app
```
"Inside the container, go to the `/app` folder." All remaining commands work from here.

```dockerfile
COPY target/devops-pipeline-app-1.0.0-shaded.jar app.jar
```
Copy our compiled fat JAR (produced in Stage 4) into the container and name it `app.jar`.

```dockerfile
EXPOSE 8800
```
Documents that this container listens on port 8800.

```dockerfile
ENTRYPOINT ["java","-jar","/app/app.jar"]
```
When the container starts, run this command. It starts our web server.

**Output Jenkins sees:**
```
Building Docker image: devops-pipeline-app:latest
#1 load build definition from Dockerfile
#2 load metadata for eclipse-temurin:17-jre-jammy
#3 FROM eclipse-temurin:17-jre-jammy
#4 COPY target/...shaded.jar app.jar
Successfully built and tagged devops-pipeline-app:latest
[SUCCESS] Docker image built successfully!
```

---

### Jenkinsfile — The Automation Brain

**What it is:** The script that tells Jenkins exactly what steps to run, in what order, when new code arrives. Jenkins reads this file automatically.

**Think of it like:** A director's script. Jenkins is the director. The script says: "First do this scene, then this scene, then this scene — and if any scene fails, stop and report it."

```groovy
pipeline {
    agent any
```
Run this pipeline on any available Jenkins machine.

```groovy
    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }
```
Before starting, make sure Java 17 and Maven are loaded — using the versions configured in Jenkins global settings named `JDK17` and `Maven3`.

```groovy
    options {
        timestamps()
        ansiColor('xterm')
    }
```
`timestamps()` = add a timestamp like `14:42:10` to every line in the log. `ansiColor` = show colored text in the console.

**Stage 1 — Checkout:**
```groovy
checkout scm
```
`scm` = Source Control Management. Jenkins knows the GitHub URL from the job settings. This downloads all your latest files into Jenkins's workspace folder. Output: your code appears on the Jenkins server.

**Stage 2 — Build:**
```groovy
bat 'mvn -v'
bat 'mvn -B -ntp clean compile'
```
`bat` = run a Windows command prompt command.
`mvn clean compile` — deletes old build files then compiles all `.java` files to `.class` files.
`-B` = batch mode (no interactive prompts), `-ntp` = no download progress (cleaner output).
Output: `BUILD SUCCESS` if Java code has no errors.

**Stage 3 — Unit Tests:**
```groovy
bat 'mvn -B -ntp test'
junit testResults: 'target/surefire-reports/*.xml'
```
Runs AppTest.java. Maven saves results to XML files. The `junit` step tells Jenkins to read those XML files and display test results in the Jenkins UI (green/red status, how many passed, etc.).
Output: `Tests run: 2, Failures: 0`.

**Stage 4 — Package:**
```groovy
bat 'mvn -B -ntp package -DskipTests'
archiveArtifacts artifacts: 'target/*.jar'
```
`package` triggers the Shade Plugin to create the fat JAR. `-DskipTests` skips running tests again.
`archiveArtifacts` saves the `.jar` file in Jenkins so you can download it directly from the Jenkins UI.
Output: `devops-pipeline-app-1.0.0.jar` appears as a downloadable file on the Jenkins build page.

**Stage 5 — Start Application:**
```groovy
start /B java -jar target\\devops-pipeline-app-1.0.0.jar
```
`start /B` = start the app in the **background**. Without `/B`, Jenkins would wait forever because the app never exits on its own. The double backslash `\\` is needed because in Groovy strings, `\` is an escape character so you write `\\` to mean one real backslash.

```groovy
ping -n 13 127.0.0.1 > nul
curl -s http://localhost:8800/health
```
Wait 12 seconds for the app to start. We use `ping` instead of `timeout` because `timeout` fails in Jenkins's non-interactive sessions on Windows. Then curl the health endpoint — if it returns "OK" the app is up.

**Stage 6 — Selenium UI Test:**
```groovy
mvn -B -ntp test -Dtest=SeleniumSmokeTest -Dselenium.enabled=true
```
`-Dtest=SeleniumSmokeTest` = run only this one test class.
`-Dselenium.enabled=true` = activates the `@EnabledIfSystemProperty` annotation in the test, turning it on.
Output: Chrome opens, visits the site, confirms it works.

**Stage 7 — Docker Build:**
```groovy
when { expression { return fileExists('Dockerfile') } }
docker build -t devops-pipeline-app:latest .
```
Only runs if a `Dockerfile` exists. Builds the container image. The `.` at the end means "use the Dockerfile in the current directory."

**Post Actions (always runs even if pipeline failed):**
```groovy
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8800"') do (
    taskkill /F /PID %%a
)
```
`netstat -aon` = list all active network connections with Process IDs.
Filter to port 8800, extract the PID from column 5, then `taskkill` kills it.
This ensures the background Java app from Stage 5 is always cleaned up so port 8800 is free for the next build.

---

## How All Files Connect

```
pom.xml
  defines what to build and what libraries are needed
    Maven compiles App.java using those libraries
      produces devops-pipeline-app-1.0.0.jar

App.java
  the web server  
    /health  — Jenkins checks this to confirm app is running
    /        — the HTML dashboard shown in the browser

AppTest.java
  tests App.java logic (no server needed, runs in milliseconds)
    used in Stage 3 (Unit Tests)

SeleniumSmokeTest.java
  opens Chrome and visits http://localhost:8800
    used in Stage 6 (Selenium UI Test)
    requires the app to already be running (Stage 5 starts it first)

Dockerfile
  packages the .jar into a portable container
    used in Stage 7 (Docker Build)

Jenkinsfile
  orchestrates all the above in order
    runs automatically when you push to GitHub
```

---

## Quick Reference

| Jenkins Stage | Command | What happens |
|---|---|---|
| Checkout | `checkout scm` | Code downloaded from GitHub |
| Build | `mvn clean compile` | Java code compiled to .class files |
| Unit Tests | `mvn test` | AppTest.java runs (2 tests) |
| Package | `mvn package -DskipTests` | Fat .jar file created |
| Start App | `start /B java -jar ...jar` | Web server starts on port 8800 |
| Selenium | `mvn test -Dtest=SeleniumSmokeTest -Dselenium.enabled=true` | Chrome visits the site, checks heading |
| Docker | `docker build -t devops-pipeline-app:latest .` | Container image created |
| Cleanup | `taskkill /F /PID <pid>` | Background app stopped |

---

## Running Locally

```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/devops-pipeline-app-1.0.0.jar

# Open in browser
http://localhost:8800

# Run unit tests
mvn test

# Run selenium test (app must be running first)
mvn test -Dtest=SeleniumSmokeTest -Dselenium.enabled=true
```

---

## Tech Stack

| Tool | Version | Role |
|---|---|---|
| Java | 17 (Eclipse Adoptium) | Programming language |
| Maven | 3.9.12 | Build tool and dependency manager |
| Spark Java | 2.9.4 | Lightweight web server |
| JUnit 5 | 5.10.2 | Unit testing framework |
| Selenium | 4.18.1 | Browser automation |
| WebDriverManager | 5.7.0 | Auto-downloads ChromeDriver |
| Jenkins | Latest | CI/CD automation server (port 8080) |
| Docker | 28.4.0 | Containerization |
| Chrome | 145 | Browser for Selenium tests |
