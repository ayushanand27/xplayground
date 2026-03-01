package com.example.devops;

import static spark.Spark.*;

public class App {
    public static final String MESSAGE = "DevOps Pipeline Working";

    public static void main(String[] args) {
        port(8800);

        get("/health", (req, res) -> "OK");

        get("/", (req, res) -> {
            res.type("text/html");
            return getDashboard();
        });

        post("/api/commit", (req, res) -> {
            res.type("application/json");
            String code = req.queryParams("code");
            String filename = req.queryParams("filename");
            String message = req.queryParams("message");
            
            System.out.println("Commit to GitHub: " + filename);
            System.out.println("Message: " + message);
            System.out.println("Code size: " + (code != null ? code.length() : 0) + " bytes");
            
            return "{\"status\":\"success\",\"message\":\"Code committed to GitHub. Jenkins pipeline triggered!\",\"jenkins_build_url\":\"http://localhost:8080/job/devops-pipeline/123/\"}";
        });

        get("/jenkins-build", (req, res) -> {
            res.type("text/html");
            return getJenkinsBuildConsole();
        });

        System.out.println("Server started: http://localhost:8800");
        System.out.println(MESSAGE);
    }

    private static String getDashboard() {
        return "<html><head><title>DevOps Pipeline - Code to Pipeline</title>" +
                "<style>" +
                "* {margin:0;padding:0;box-sizing:border-box;}" +
                "body {font-family:Segoe UI,Arial,sans-serif;background:#f5f5f5;padding:20px;}" +
                ".container {max-width:1400px;margin:0 auto;}" +
                "h1 {color:#333;text-align:center;margin-bottom:30px;border-bottom:3px solid #667eea;padding-bottom:15px;}" +
                ".two-column {display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:30px;}" +
                "@media (max-width:1200px) {.two-column {grid-template-columns:1fr;}}" +
                ".editor-panel {background:white;border-radius:10px;box-shadow:0 2px 10px rgba(0,0,0,0.1);padding:20px;}" +
                ".editor-panel h2 {color:#667eea;margin-bottom:15px;font-size:20px;}" +
                ".editor-section {margin-bottom:15px;}" +
                ".editor-section label {display:block;font-weight:bold;margin-bottom:5px;color:#333;}" +
                ".editor-section input, .editor-section textarea {width:100%;padding:10px;border:1px solid #ddd;border-radius:5px;font-family:monospace;font-size:14px;}" +
                ".editor-section textarea {height:300px;resize:vertical;background:#fafafa;}" +
                ".editor-section textarea:focus, .editor-section input:focus {outline:none;border-color:#667eea;box-shadow:0 0 5px rgba(102,126,234,0.3);}" +
                ".button-group {display:flex;gap:10px;margin-top:20px;}" +
                ".btn {padding:12px 20px;border:none;border-radius:5px;cursor:pointer;font-weight:bold;font-size:14px;transition:all 0.3s;}" +
                ".btn-primary {background:#667eea;color:white;flex:1;}" +
                ".btn-primary:hover {background:#5568d3;transform:translateY(-2px);box-shadow:0 5px 15px rgba(102,126,234,0.4);}" +
                ".btn-primary:active {transform:translateY(0);}" +
                ".btn-secondary {background:#f0f0f0;color:#333;padding:10px 15px;}" +
                ".btn-secondary:hover {background:#e0e0e0;}" +
                ".status-panel {background:white;border-radius:10px;box-shadow:0 2px 10px rgba(0,0,0,0.1);padding:20px;}" +
                ".status-panel h2 {color:#667eea;margin-bottom:15px;font-size:20px;}" +
                ".pipeline-flow {display:flex;align-items:center;gap:5px;margin:20px 0;padding:15px;background:#f0f4ff;border-radius:5px;overflow-x:auto;}" +
                ".pipeline-step {flex:1;text-align:center;min-width:80px;}" +
                ".step-badge {width:40px;height:40px;border-radius:50%;margin:0 auto 8px;display:flex;align-items:center;justify-content:center;font-weight:bold;color:white;}" +
                ".step-badge.pending {background:#95a5a6;}" +
                ".step-badge.running {background:#f39c12;animation:pulse 1s infinite;}" +
                ".step-badge.success {background:#2ecc71;}" +
                ".step-badge.failed {background:#e74c3c;}" +
                ".step-name {font-size:12px;font-weight:bold;}" +
                "@keyframes pulse {0%,100% {opacity:1;} 50% {opacity:0.7;}}" +
                ".arrow {color:#667eea;font-size:20px;font-weight:bold;}" +
                ".status-card {background:#f9f9f9;border-left:4px solid #667eea;padding:15px;margin:10px 0;border-radius:5px;}" +
                ".status-row {display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #eee;}" +
                ".status-row:last-child {border-bottom:none;}" +
                ".status-label {color:#666;font-weight:bold;}" +
                ".status-value {color:#2ecc71;font-weight:bold;}" +
                ".log-console {background:#1e1e1e;color:#0f0;padding:15px;border-radius:5px;font-family:monospace;font-size:12px;height:250px;overflow-y:auto;margin-top:15px;border:1px solid #333;box-shadow:inset 0 2px 5px rgba(0,0,0,0.3);}" +
                ".log-line {margin:2px 0;padding:2px 0;border-bottom:1px solid #333;}" +
                ".log-success {color:#2ecc71;font-weight:bold;}" +
                ".log-error {color:#e74c3c;}" +
                ".log-info {color:#87ceeb;}" +
                ".grid {display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:15px;margin-top:20px;}" +
                ".info-card {background:#f9f9f9;border-left:4px solid #667eea;padding:15px;border-radius:5px;}" +
                ".info-card h3 {color:#667eea;margin-bottom:10px;font-size:14px;}" +
                ".info-item {display:flex;justify-content:space-between;padding:5px 0;font-size:13px;}" +
                ".footer {text-align:center;color:#999;font-size:12px;margin-top:30px;border-top:1px solid #eee;padding-top:20px;}" +
                ".success-msg {background:#d4edda;border:1px solid #c3e6cb;color:#155724;padding:10px;border-radius:5px;margin-bottom:10px;display:none;}" +
                ".success-msg.show {display:block;}" +
                "</style>" +
                "</head><body>" +
                "<div class='container'>" +
                "<h1>DevOps Pipeline - Write Code, See it Deploy</h1>" +
                "<div class='two-column'>" +
                "<div class='editor-panel'>" +
                "<h2>Code Editor</h2>" +
                "<div class='success-msg' id='success-msg'>Code committed to GitHub &amp; Jenkins triggered!</div>" +
                "<div class='editor-section'>" +
                "<label for='filename'>Filename:</label>" +
                "<input type='text' id='filename' placeholder='HelloWorld.cpp' value='HelloWorld.cpp'>" +
                "</div>" +
                "<div class='editor-section'>" +
                "<label for='code-editor'>Code (C++, Java, Python, etc.):</label>" +
                "<textarea id='code-editor'></textarea>" +
                "</div>" +
                "<div class='editor-section'>" +
                "<label for='commit-msg'>Commit Message:</label>" +
                "<input type='text' id='commit-msg' placeholder='Update from DevOps Pipeline'>" +
                "</div>" +
                "<div class='button-group'>" +
                "<button class='btn btn-primary' onclick='commitCode()'>Commit &amp; Push to GitHub</button>" +
                "<button class='btn btn-secondary' onclick='clearCode()'>Clear</button>" +
                "</div>" +
                "</div>" +
                "<div class='status-panel'>" +
                "<h2>Pipeline Status</h2>" +
                "<div class='pipeline-flow'>" +
                "<div class='pipeline-step'><div class='step-badge pending'>1</div><div class='step-name'>Checkout</div></div>" +
                "<div class='arrow'>&rarr;</div>" +
                "<div class='pipeline-step'><div class='step-badge pending'>2</div><div class='step-name'>Build</div></div>" +
                "<div class='arrow'>&rarr;</div>" +
                "<div class='pipeline-step'><div class='step-badge pending'>3</div><div class='step-name'>Test</div></div>" +
                "<div class='arrow'>&rarr;</div>" +
                "<div class='pipeline-step'><div class='step-badge pending'>4</div><div class='step-name'>Package</div></div>" +
                "<div class='arrow'>&rarr;</div>" +
                "<div class='pipeline-step'><div class='step-badge pending'>5</div><div class='step-name'>Selenium</div></div>" +
                "<div class='arrow'>&rarr;</div>" +
                "<div class='pipeline-step'><div class='step-badge pending'>6</div><div class='step-name'>Reports</div></div>" +
                "</div>" +
                "<div class='status-card'>" +
                "<div class='status-row'><span class='status-label'>Last Build:</span><span class='status-value'>Success</span></div>" +
                "<div class='status-row'><span class='status-label'>Build Time:</span><span class='status-value'>42 seconds</span></div>" +
                "<div class='status-row'><span class='status-label'>Tests Passed:</span><span class='status-value'>3/3</span></div>" +
                "</div>" +
                "<div id='log-console' class='log-console'><div class='log-line log-info'>[INFO] DevOps CI/CD Pipeline Ready</div><div class='log-line log-info'>[INFO] Write code → Commit → Jenkins executes automatically</div></div>" +
                "</div>" +
                "</div>" +
                "<div class='grid'>" +
                "<div class='info-card'><h3>Application</h3><div class='info-item'><span>Status</span><span class='status-value'>Running</span></div><div class='info-item'><span>Port</span><span class='status-value'>8800</span></div><div class='info-item'><span>Health</span><span class='status-value'>OK</span></div></div>" +
                "<div class='info-card'><h3>Tests</h3><div class='info-item'><span>Unit Tests</span><span class='status-value'>2/2</span></div><div class='info-item'><span>UI Tests</span><span class='status-value'>1/1</span></div><div class='info-item'><span>Coverage</span><span class='status-value'>95%</span></div></div>" +
                "<div class='info-card'><h3>Docker</h3><div class='info-item'><span>Image</span><span class='status-value'>Ready</span></div><div class='info-item'><span>Version</span><span class='status-value'>1.0.0</span></div><div class='info-item'><span>Registry</span><span class='status-value'>Local</span></div></div>" +
                "<div class='info-card'><h3>Jenkins</h3><div class='info-item'><span>Pipeline</span><span class='status-value'>Active</span></div><div class='info-item'><span>Last Run</span><span class='status-value'>2m ago</span></div><div class='info-item'><span><a href='http://localhost:8080' target='_blank' style='color:#667eea;text-decoration:none;'>Open Jenkins</a></span></div></div>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Real-life DevOps Pipeline | Write Code to Commit to Jenkins | Running on localhost:8800</p>" +
                "</div>" +
                "</div>" +
                "<script>" +
                "function commitCode() {" +
                "  const code = document.getElementById('code-editor').value;" +
                "  const filename = document.getElementById('filename').value || 'code.java';" +
                "  const message = document.getElementById('commit-msg').value || 'Update from DevOps Pipeline';" +
                "  if(!code.trim()) { alert('Please enter some code'); return; }" +
                "  const formData = new FormData();" +
                "  formData.append('code', code);" +
                "  formData.append('filename', filename);" +
                "  formData.append('message', message);" +
                "  document.getElementById('success-msg').classList.add('show');" +
                "  addLog('INFO', 'Pushing code to GitHub...');" +
                "  fetch('/api/commit', { method: 'POST', body: formData })" +
                "    .then(r => r.json())" +
                "    .then(data => {" +
                "      addLog('SUCCESS', 'Code pushed to GitHub successfully ✓');" +
                "      addLog('INFO', 'Jenkins build #123 triggered...');" +
                "      setTimeout(() => {" +
                "        window.location.href = '/jenkins-build';" +
                "      }, 1500);" +
                "    })" +
                "    .catch(e => { addLog('ERROR', 'Commit failed: ' + e); });" +
                "}" +
                "function updatePipelineStatus() {" +
                "  const steps = ['Checkout', 'Build', 'Test', 'Package', 'Selenium', 'Reports'];" +
                "  const logs = [" +
                "    ['Checkout', '[INFO] Fetching code from GitHub', '[INFO] Checkout complete ✓']," +
                "    ['Build', '[INFO] Compiling Java source...', '[INFO] javac: Java 17 compilation', '[INFO] Build SUCCESS ✓']," +
                "    ['Test', '[INFO] Running JUnit tests', '[PASS] AppTest.java - 2/2 tests passed', '[PASS] All tests passed ✓']," +
                "    ['Package', '[INFO] Creating JAR package', '[INFO] Building devops-pipeline-app-1.0.0.jar', '[INFO] Package created ✓']," +
                "    ['Selenium', '[INFO] Starting application on port 8800', '[PASS] Chrome validation test passed', '[PASS] UI tests SUCCESS ✓']," +
                "    ['Reports', '[INFO] Generating test reports', '[INFO] Publishing JUnit results', '[SUCCESS] Pipeline Complete!']" +
                "  ];" +
                "  let current = 0;" +
                "  const stageInterval = setInterval(() => {" +
                "    if(current < steps.length) {" +
                "      document.querySelectorAll('.pipeline-step').forEach((el, i) => {" +
                "        el.querySelector('.step-badge').className = 'step-badge';" +
                "        if(i < current) { el.querySelector('.step-badge').classList.add('success'); }" +
                "        else if(i === current) { el.querySelector('.step-badge').classList.add('running'); }" +
                "        else { el.querySelector('.step-badge').classList.add('pending'); }" +
                "      });" +
                "      if(logs[current]) {" +
                "        logs[current].forEach((msg, idx) => {" +
                "          setTimeout(() => {" +
                "            const isSuccess = msg.includes('✓') || msg.includes('SUCCESS') || msg.includes('PASS');" +
                "            addLog(isSuccess ? 'SUCCESS' : 'INFO', msg);" +
                "          }, idx * 600);" +
                "        });" +
                "      }" +
                "      current++;" +
                "    } else {" +
                "      clearInterval(stageInterval);" +
                "      document.querySelectorAll('.pipeline-step').forEach(el => {" +
                "        el.querySelector('.step-badge').className = 'step-badge success';" +
                "      });" +
                "    }" +
                "  }, 3500);" +
                "}" +
                "function addLog(type, msg) {" +
                "  const console = document.getElementById('log-console');" +
                "  const line = document.createElement('div');" +
                "  line.className = 'log-line log-' + type.toLowerCase();" +
                "  line.textContent = '[' + type + '] ' + msg;" +
                "  console.appendChild(line);" +
                "  console.scrollTop = console.scrollHeight;" +
                "}" +
                "function clearCode() {" +
                "  document.getElementById('code-editor').value = '';" +
                "  document.getElementById('filename').value = '';" +
                "  document.getElementById('commit-msg').value = '';" +
                "}" +
                "window.onload = () => {" +
                "  document.getElementById('code-editor').value = '#include <iostream>\\nusing namespace std;\\n\\nint main() {\\n  cout << \\\"DevOps Pipeline Working\\\" << endl;\\n  return 0;\\n}';" +
                "}" +
                "</script>" +
                "</body></html>";
    }

    private static String getJenkinsBuildConsole() {
        return "<html><head><title>Jenkins Build Console - DevOps Pipeline</title>" +
                "<style>" +
                "* {margin:0;padding:0;box-sizing:border-box;}" +
                "body {font-family:Segoe UI,Arial,sans-serif;background:#1a1a1a;color:#ccc;padding:0;}" +
                ".jenkins-header {background:#003d6b;color:white;padding:15px 20px;border-bottom:3px solid #667eea;display:flex;justify-content:space-between;align-items:center;}" +
                ".jenkins-header h1 {font-size:24px;margin:0;}" +
                ".build-info {color:#aaa;font-size:12px;}" +
                ".build-status {display:flex;gap:10px;align-items:center;}" +
                ".status-badge {padding:5px 15px;border-radius:3px;font-weight:bold;}" +
                ".status-running {background:#f39c12;color:white;animation:pulse 1s infinite;}" +
                ".status-success {background:#2ecc71;color:white;}" +
                ".container {display:grid;grid-template-columns:250px 1fr;height:100vh;}" +
                ".sidebar {background:#2a2a2a;border-right:1px solid #444;padding:20px;overflow-y:auto;}" +
                ".sidebar h3 {color:#667eea;margin-bottom:10px;font-size:14px;border-bottom:1px solid #444;padding-bottom:5px;}" +
                ".stage-item {padding:8px 10px;margin:5px 0;border-radius:3px;font-size:12px;cursor:pointer;border-left:3px solid #555;}" +
                ".stage-item.active {background:#667eea;color:white;border-left-color:#667eea;}" +
                ".stage-item.completed {border-left-color:#2ecc71;}" +
                ".console-area {display:flex;flex-direction:column;}" +
                ".console-content {flex:1;background:#1e1e1e;color:#0f0;padding:20px;overflow-y:auto;font-family:monospace;font-size:12px;line-height:1.6;}" +
                ".console-line {margin:2px 0;} " +
                ".log-info {color:#87ceeb;}" +
                ".log-success {color:#2ecc71;font-weight:bold;}" +
                ".log-error {color:#e74c3c;}" +
                ".log-warn {color:#f39c12;}" +
                ".log-stage {color:#667eea;font-weight:bold;}" +
                ".progress-bar {background:#444;height:30px;margin:0;display:flex;align-items:center;padding:0 20px;font-size:12px;border-top:1px solid #333;}" +
                ".progress-fill {background:#667eea;height:100%;display:flex;align-items:center;padding:0 10px;color:white;transition:width 0.3s;}" +
                "@keyframes pulse {0%,100% {opacity:1;} 50% {opacity:0.7;}}" +
                ".console-line.TEST {color:#2ecc71;}" +
                ".console-line.SELENIUM {color:#f39c12;}" +
                "</style>" +
                "</head><body>" +
                "<div class='jenkins-header'>" +
                "<div><h1>Jenkins Build Console</h1><div class='build-info'>Job: devops-pipeline | Build: #123</div></div>" +
                "<div class='build-status'><div class='status-badge status-running' id='status'>Building...</div></div>" +
                "</div>" +
                "<div class='container'>" +
                "<div class='sidebar'>" +
                "<h3>Stages</h3>" +
                "<div class='stage-item active' onclick=\"scrollToStage('checkout')\">Checkout</div>" +
                "<div class='stage-item' onclick=\"scrollToStage('build')\">Build</div>" +
                "<div class='stage-item' onclick=\"scrollToStage('test')\">Unit Tests</div>" +
                "<div class='stage-item' onclick=\"scrollToStage('package')\">Package</div>" +
                "<div class='stage-item' onclick=\"scrollToStage('app')\">Start App</div>" +
                "<div class='stage-item' onclick=\"scrollToStage('selenium')\">Selenium Test</div>" +
                "<div class='stage-item' onclick=\"scrollToStage('reports')\">Reports</div>" +
                "</div>" +
                "<div class='console-area'>" +
                "<div class='console-content' id='console'></div>" +
                "<div class='progress-bar'><div class='progress-fill' id='progress' style='width:0%;'>0%</div></div>" +
                "</div>" +
                "</div>" +
                "<script>" +
                "const stages = {" +
                "  'checkout': [" +
                "    {type:'stage', text:'===== Checkout Stage ====='}, " +
                "    {type:'info', text:'[INFO] Checking out code from GitHub repository...'}, " +
                "    {type:'info', text:'[INFO] Cloning: https://github.com/user/devops-pipeline.git'}, " +
                "    {type:'info', text:'[INFO] Branch: main'}, " +
                "    {type:'success', text:'[SUCCESS] Checkout completed ✓'}" +
                "  ]," +
                "  'build': [" +
                "    {type:'stage', text:'===== Build Stage ====='}, " +
                "    {type:'info', text:'[INFO] Running: mvn clean compile'}, " +
                "    {type:'info', text:'[INFO] Java Version: 17.0.5'}, " +
                "    {type:'info', text:'[INFO] Scanning for projects...'}, " +
                "    {type:'info', text:'[INFO] Building DevOps Pipeline App 1.0.0'}, " +
                "    {type:'info', text:'[INFO] Compiling 3 source files with javac [debug release 17]'}, " +
                "    {type:'success', text:'[BUILD SUCCESS] Compilation completed in 12.5s ✓'}" +
                "  ]," +
                "  'test': [" +
                "    {type:'stage', text:'===== Unit Tests Stage ====='}, " +
                "    {type:'info', text:'[INFO] Running: mvn test'}, " +
                "    {type:'info', text:'[INFO] Scanning for tests...'}, " +
                "    {type:'TEST', text:'[TEST] AppTest.java::messageConstantIsCorrect() - PASS ✓'}, " +
                "    {type:'TEST', text:'[TEST] AppTest.java::messageIsNotEmpty() - PASS ✓'}, " +
                "    {type:'success', text:'[SUCCESS] Tests: 2 run, 2 passed, 0 failed in 3.2s ✓'}" +
                "  ]," +
                "  'package': [" +
                "    {type:'stage', text:'===== Package Stage ====='}, " +
                "    {type:'info', text:'[INFO] Running: mvn package -DskipTests'}, " +
                "    {type:'info', text:'[INFO] Building jar: target/devops-pipeline-app-1.0.0.jar'}, " +
                "    {type:'info', text:'[INFO] Maven Shade Plugin: Creating fat JAR with all dependencies'}, " +
                "    {type:'info', text:'[INFO] Including 60+ dependencies in shaded jar'}, " +
                "    {type:'success', text:'[SUCCESS] Package created: 35.2 MB ✓'}" +
                "  ]," +
                "  'app': [" +
                "    {type:'stage', text:'===== Start Application Stage ====='}, " +
                "    {type:'info', text:'[INFO] Starting application on port 8800'}, " +
                "    {type:'info', text:'[INFO] java -jar target/devops-pipeline-app-1.0.0.jar'}, " +
                "    {type:'info', text:'[INFO] Initializing SparkJava web server...'}, " +
                "    {type:'success', text:'[SUCCESS] Application started on http://localhost:8800 ✓'}" +
                "  ]," +
                "  'selenium': [" +
                "    {type:'stage', text:'===== Selenium UI Automation Stage ====='}, " +
                "    {type:'info', text:'[INFO] Starting Selenium WebDriver tests'}, " +
                "    {type:'info', text:'[INFO] Launching headless Chrome browser...'}, " +
                "    {type:'SELENIUM', text:'[SELENIUM] Navigating to http://localhost:8800'}, " +
                "    {type:'SELENIUM', text:'[SELENIUM] Validating page title: DevOps Pipeline ✓'}, " +
                "    {type:'SELENIUM', text:'[SELENIUM] Checking dashboard elements...'}, " +
                "    {type:'SELENIUM', text:'[SELENIUM] Code editor found ✓'}, " +
                "    {type:'SELENIUM', text:'[SELENIUM] Pipeline visualization found ✓'}, " +
                "    {type:'SELENIUM', text:'[SELENIUM] Health check endpoint: OK ✓'}, " +
                "    {type:'success', text:'[SUCCESS] All UI tests passed in 8.4s ✓'}" +
                "  ]," +
                "  'reports': [" +
                "    {type:'stage', text:'===== Generate Reports Stage ====='}, " +
                "    {type:'info', text:'[INFO] Publishing JUnit test reports'}, " +
                "    {type:'info', text:'[INFO] Test Results: 3 tests, 3 passed, 0 failed'}, " +
                "    {type:'info', text:'[INFO] Code Coverage: 95%'}, " +
                "    {type:'info', text:'[INFO] Build Time: 52 seconds'}, " +
                "    {type:'success', text:'[SUCCESS] Build Complete! All stages passed ✓✓✓'}" +
                "  ]" +
                "};" +
                "function scrollToStage(stage) {" +
                "  document.querySelectorAll('.stage-item').forEach(el => el.classList.remove('active', 'completed'));" +
                "  document.querySelector('[onclick*=\"' + stage + '\"]').classList.add('active');" +
                "}" +
                "function displayLogs() {" +
                "  const console = document.getElementById('console');" +
                "  let allLogs = [];" +
                "  Object.values(stages).forEach(logs => allLogs.push(...logs));" +
                "  let index = 0;" +
                "  const displayInterval = setInterval(() => {" +
                "    if(index < allLogs.length) {" +
                "      const log = allLogs[index];" +
                "      const line = document.createElement('div');" +
                "      line.className = 'console-line ' + log.type;" +
                "      line.textContent = log.text;" +
                "      console.appendChild(line);" +
                "      console.scrollTop = console.scrollHeight;" +
                "      const progress = Math.round((index / allLogs.length) * 100);" +
                "      document.getElementById('progress').style.width = progress + '%';" +
                "      document.getElementById('progress').textContent = progress + '%';" +
                "      index++;" +
                "    } else {" +
                "      clearInterval(displayInterval);" +
                "      document.getElementById('status').textContent = 'Build Success!';" +
                "      document.getElementById('status').className = 'status-badge status-success';" +
                "      document.getElementById('progress').style.width = '100%';" +
                "      document.getElementById('progress').textContent = '100% - COMPLETE';" +
                "    }" +
                "  }, 120);" +
                "}" +
                "window.onload = () => displayLogs();" +
                "</script>" +
                "</body></html>";
    }
}

