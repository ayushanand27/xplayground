# 🔍 Quick Troubleshooting Guide

## 🚨 Your Current Issue

**Symptoms:** Selenium UI Test and Docker Build stages failing consistently

**Quick Actions to Take:**

---

## ⚡ IMMEDIATE STEPS (Do These First)

### **Step 1: Run the Debug Script**

I've created an interactive troubleshooting tool. Run it now:

```bash
cd c:\Users\ayush\xplayground
debug-stages.bat
```

**Choose Option 1** to test Selenium - this will tell you exactly what's wrong.

---

### **Step 2: Get Jenkins Console Output**

**This is THE MOST IMPORTANT step - it shows the actual error!**

1. Open Jenkins: `http://localhost:8080`
2. Click on your job/pipeline name
3. Click on the **latest failed build** (#4 in your screenshot)
4. Click "**Console Output**" in the left sidebar
5. **Scroll to the bottom** - errors are usually at the end
6. Look for lines with **[ERROR]** or **FAILED**

**What to Look For:**

#### For Selenium Failures:
```
Search for these keywords in console:
- "ChromeDriver"
- "SessionNotCreatedException" 
- "WebDriverException"
- "Connection refused"
```

#### For Docker Failures:
```
Search for these keywords:
- "docker: command not found"
- "Cannot connect to Docker daemon"
- "permission denied"
- "no such file"
```

---

## 🔍 Common Issues & Quick Fixes

### Issue 1: Selenium - Chrome Not Found

**Error in Jenkins Console:**
```
org.openqa.selenium.SessionNotCreatedException: 
Could not find Chrome binary
```

**Fix:**
```bash
# Install Chrome
# Download from: https://www.google.com/chrome/

# After installing, verify it's in PATH:
where chrome.exe

# If not found, add Chrome to Jenkins environment:
# Jenkins > Manage Jenkins > System Configuration
# Add environment variable: CHROME_BIN=C:\Program Files\Google\Chrome\Application\chrome.exe
```

---

### Issue 2: Selenium - App Not Running

**Error in Jenkins Console:**
```
Connection refused: connect
```

**Fix in Jenkinsfile** - Increase wait time:
```groovy
// In "Start Application" stage, change:
timeout /t 10 /nobreak >nul  
// to:
timeout /t 20 /nobreak >nul
```

---

### Issue 3: Docker - Daemon Not Running

**Error in Jenkins Console:**
```
Cannot connect to the Docker daemon at unix:///var/run/docker.sock
```

**Fix:**
```bash
# Start Docker Desktop
# Wait for whale icon in system tray to stop animating
# Verify with:
docker ps

# If fails, restart Docker Desktop
```

---

### Issue 4: Docker - Permission Denied

**Error in Jenkins Console:**
```
Got permission denied while trying to connect to the Docker daemon socket
```

**Fix:**
```bash
# Add Jenkins user to docker-users group (Windows)
# 1. Open Computer Management
# 2. Local Users and Groups > Groups
# 3. Double-click "docker-users"
# 4. Add Jenkins user
# 5. Restart Jenkins service
```

---

## 🧪 Test Locally First

Before running Jenkins, test each component locally:

### Test 1: Verify Build Works
```bash
mvn clean package -DskipTests
```
**Expected:** BUILD SUCCESS

---

### Test 2: Start Application Manually
```bash
java -jar target\devops-pipeline-app-1.0.0.jar
```

**Expected:** 
```
Server started: http://localhost:8800
DevOps Pipeline Working
```

**Test it:**
```bash
# In another terminal:
curl http://localhost:8800/health
```
**Expected:** `OK`

---

### Test 3: Test Selenium Manually

**With app running** (from Test 2), in another terminal:

```bash
mvn test -Dtest=SeleniumSmokeTest -Dselenium.enabled=true
```

**What to watch for:**
- "Starting Chrome WebDriver..." ✅
- "Navigating to http://localhost:8800..." ✅
- "Page loaded successfully!" ✅
- "All Selenium tests passed!" ✅

**If it fails:**
```bash
# Check if Chrome is installed
where chrome.exe

# If not found:
# Download and install Chrome from https://www.google.com/chrome/
```

---

### Test 4: Test Docker Build

```bash
docker build -t test-app .
```

**If it fails with "command not found":**
```bash
# Docker not installed or not running
# 1. Check if Docker Desktop is installed
# 2. Start Docker Desktop
# 3. Wait for it to fully start (check system tray icon)
# 4. Test: docker ps
```

---

## 📋 Diagnosis Checklist

Run through this checklist:

- [ ] **Java installed:** `java -version` shows Java 17
- [ ] **Maven works:** `mvn -v` shows version
- [ ] **Chrome installed:** `where chrome.exe` finds Chrome
- [ ] **Docker Desktop running:** `docker ps` works
- [ ] **Port 8800 free:** Not already in use
- [ ] **Build succeeds:** `mvn clean package` works
- [ ] **App starts:** `java -jar target\devops-pipeline-app-1.0.0.jar` runs
- [ ] **Selenium works locally:** Manual test passes
- [ ] **Docker works locally:** `docker build` succeeds

---

## 🎯 Most Likely Issues (Based on Your Symptoms)

### For Selenium (failing at 1-19 seconds):

**Problem:** Chrome or ChromeDriver not available in Jenkins environment

**Solution:**
1. Open Jenkins console output (see Step 2 above)
2. Look for exact error message
3. If "Chrome not found":
   - Install Chrome on machine running Jenkins
   - Ensure it's in PATH
4. If "ChromeDriver" error:
   - Selenium Manager should auto-download it
   - Check network connectivity in Jenkins

---

### For Docker (failing at 300-400ms):

**Problem:** Docker command failing very quickly = command not found or daemon not running

**Solution:**
1. On the machine running Jenkins:
   ```bash
   docker --version
   docker ps
   ```
2. If either fails, Docker Desktop needs to be started
3. For Jenkins to access Docker:
   - Docker Desktop must be running as a service
   - Jenkins user must have permission

---

## 🔧 Use the Debug Scripts

I created 2 scripts to help you:

### **debug-stages.bat** - Interactive menu
```bash
cd c:\Users\ayush\xplayground
debug-stages.bat
```

**Options:**
- **[1]** Test Selenium Stage - **START HERE**
- **[2]** Test Docker Stage
- **[3]** Test Complete Pipeline Locally
- **[4]** View Jenkins Console Guide
- **[5]** Check System Requirements

### **troubleshoot-pipeline.bat** - Automated full test
```bash
cd c:\Users\ayush\xplayground
troubleshoot-pipeline.bat
```

This runs all tests and creates a log file.

---

## 📞 Next Steps

1. **Run:** `debug-stages.bat` → Choose option 1 (Selenium)
2. **Check:** Jenkins Console Output (see Step 2 above)
3. **Copy:** The exact error message
4. **Apply:** The corresponding fix from above

---

## 💡 Pro Tip: Jenkins Console Output

The Jenkins console output is THE KEY to solving this. Example of what to look for:

```
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0
...
org.openqa.selenium.SessionNotCreatedException: Could not start a new session
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
THIS IS YOUR ACTUAL ERROR - Google this!
```

**Always scroll to the bottom of console output - that's where the real error is!**

---

## ✅ How You'll Know It's Fixed

When working correctly, you'll see in Jenkins:

```
Stage: Selenium UI Test
✅ Starting Chrome WebDriver...
✅ Navigating to http://localhost:8800...
✅ Page loaded successfully!
✅ Selenium validated app
Duration: 18-23 seconds

Stage: Docker Build  
✅ Docker version found
✅ Building image...
✅ Successfully tagged devops-pipeline-app:latest
Duration: 341ms
```

---

**Run `debug-stages.bat` now and share what error message you get!**
